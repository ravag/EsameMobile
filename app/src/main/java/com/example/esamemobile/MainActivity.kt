package com.example.esamemobile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.ui.theme.EsameMobileTheme
import com.example.esamemobile.utilities.CharacterList
import com.example.esamemobile.utilities.GroupList
import com.example.esamemobile.utilities.NavigationBottomBarWithFAB
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EsameMobileTheme {
                //Chiediamo il permesso per le notifiche
                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    } else {
                        mutableStateOf(true)
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted -> hasNotificationPermission = isGranted }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                //Mostriamo la schermata di login
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   LoginScreen(modifier=Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    //DATABASE: Inizializzazione
    val db = FirebaseFirestore.getInstance()

    var currentUser by remember { mutableStateOf(auth.currentUser) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showDebugDatabaseScreen by remember { mutableStateOf(false) }

    var selectedItemIndex by remember { mutableStateOf(0)}

    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            email = ""
            password = ""
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val channel = NotificationChannel(
                "canale_gdr",
                "Notifiche GDR",
                NotificationManager.IMPORTANCE_DEFAULT
                )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            db.collection("notifiche_bacheca")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) return@addSnapshotListener

                    val lastDocInincoming = snapshots?.documentChanges?.find { it.type == DocumentChange.Type.ADDED }
                    if (lastDocInincoming != null) {
                        val title =
                            lastDocInincoming.document.getString("titolo") ?: "Nuova Notifica"
                        val msg = lastDocInincoming.document.getString("messaggio") ?: ""
                        val authorId = lastDocInincoming.document.getString("autoreId") ?: ""

                        //Per evitare che la notifica suoni anche dal telefono che l'ha mandata
                        if (authorId != auth.currentUser?.uid) {
                            val builder = NotificationCompat.Builder(context, "canale_gdr")
                                .setSmallIcon(android.R.drawable.stat_notify_chat) // Icona fumetto nativa Android
                                .setContentTitle(title)
                                .setContentText(msg)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)

                            val notificationManager =
                                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(
                                System.currentTimeMillis().toInt(),
                                builder.build()
                            )
                        }
                    }
                }
        }
    }

    val webClientId = "803305060535-h1adgsgul2khemr3rcmvsevb6q35ieev.apps.googleusercontent.com"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (currentUser != null) {
            //Se l'utente è loggato mettiamo la schermata principale
            val displayedName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@") ?: "Utente"

            if (showDebugDatabaseScreen) {
                DebugDatabaseScreen(
                    displayedName = displayedName,
                    currentUser = currentUser,
                    db = db,
                    context = context,
                    onCloseDebug = { showDebugDatabaseScreen = false }
                )
            } else {
                val charactersTest = remember {
                    listOf(
                        Character(id = 1, name = "Joe Jostino", ""),
                        Character(id = 2, name = "Gigi Pancetta", ""),
                        Character(id = 2, name = "Ettore Pelacane", "")
                    )
                }

                val groupsTest = remember {
                    listOf(
                        Group(id = 1, name = "I Zingari", ""),
                        Group(id = 2, name = "I fantastici 2", ""),
                        Group(id = 3, name = "I tre moschettoni", ""),
                        Group(id = 4, name = "I quattro gatti", ""),
                        Group(id = 5, name = "I cojo(n)ti", ""),
                        Group(id = 6, name = "Giovanni", ""),
                        Group(id = 7, name = "Miku club", ""),
                        Group(id = 8, name = "Il gioco perso", ""),
                        Group(id = 9, name = "Ci piacciono i treni", ""),
                        Group(id = 10, name = "Impottibile!", "")
                    )
                }

                Scaffold(
                    bottomBar = {
                        NavigationBottomBarWithFAB(
                            selectedIndex = selectedItemIndex,
                            onTabSelected = { newIndex -> selectedItemIndex = newIndex },
                            onFabClick = {
                                when (selectedItemIndex) {
                                    0 -> Toast.makeText(context, "Azione: CREA NUOVO PERSONAGGIO", Toast.LENGTH_SHORT).show()
                                    1 -> Toast.makeText(context, "Azione: CREA NUOVO GRUPPO", Toast.LENGTH_SHORT). show()
                                }
                            }
                        )
                    },
                    containerColor = Color.Black
                ) { innerPadding ->
                    Column (
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //Bottone per la schermata di debug
                            Button (
                                onClick = {showDebugDatabaseScreen = true},
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                            ) {
                                Text("Debug Database", color = Color.Black, fontSize = 12.sp)
                            }

                            //Bottone di logout
                            Button(
                                onClick = {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("tutti")
                                    auth.signOut()
                                    currentUser = null
                                    Toast.makeText(context, "Logout effettuato", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text(
                                    text = "Logout",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (selectedItemIndex) {
                                0 -> {
                                    CharacterList(
                                        contentPadding = innerPadding,
                                        chars = charactersTest,
                                        context = context
                                    )
                                }
                                1 -> {
                                    GroupList(
                                        contentPadding = innerPadding,
                                        groups = groupsTest,
                                        context = context
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
                //Se non è loggato mostriamo la schermata di login
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ACCEDI",
                        color = Color.White,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    //Bottone Accedi/Registrati con email
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Accesso eseguito!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            val error = task.exception

                                            if (error is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                                error is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
                                            ) {
                                                auth.createUserWithEmailAndPassword(
                                                    email,
                                                    password
                                                )
                                                    .addOnCompleteListener { regTask ->
                                                        if (regTask.isSuccessful) {
                                                            Toast.makeText(
                                                                context,
                                                                "Registrato con successo!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            val regError = regTask.exception
                                                            if (regError is com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Password Debole! Usa lettere, numeri e simboli.",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            } else if (regError is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Password Errata. Nome utente già esistente.",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Errore: ${regError?.message}",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Password errata o account non valido",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Riempi tutti i campi!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Accedi / Registrati",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))

                    //Bottone accedi con google
                    Button(
                        onClick = {
                            val credentialManager = CredentialManager.create(context)

                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(webClientId)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            //Lancia il flusso di login
                            coroutineScope.launch {
                                try {
                                    val result = credentialManager.getCredential(
                                        context = context,
                                        request = request
                                    )
                                    val credential = result.credential

                                    if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential =
                                            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(
                                                credential.data
                                            )
                                        val idToken = googleIdTokenCredential.idToken

                                        //Passa le credenziali a Firebase
                                        val firebaseCredential =
                                            GoogleAuthProvider.getCredential(idToken, null)
                                        auth.signInWithCredential(firebaseCredential)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(
                                                        context,
                                                        "Login Riuscito!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Login Fallito! (Come te)",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Errore", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icons_google_48),
                                contentDescription = "Google Logo",
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Accedi con Google",
                                color = Color.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
            }
        }

@Composable
fun DebugDatabaseScreen(
    displayedName: String,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    db: com.google.firebase.firestore.FirebaseFirestore,
    context: android.content.Context,
    onCloseDebug: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "MODALITA' DEBUG\nBenvenuto $displayedName",
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        //DATABASE: Bottone creazione tabella
        Button(
            onClick = {
                val datiTest = hashMapOf(
                    "nomeEseguito" to displayedName,
                    "emailEseguito" to (currentUser?.email ?: "Nessuna Email"),
                    "uidCreatore" to (currentUser?.uid ?: ""),
                    "timeStamp" to java.lang.System.currentTimeMillis()
                )

                db.collection("tabella_testing")
                    .document(currentUser?.uid ?: "default_doc")
                    .set(datiTest)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Collezione creata, dati inseriti",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Errore scrittura: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(text = "Crea Collezione", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        //DATABASE: Bottone distruzione
        Button(
            onClick = {
                db.collection("tabella_testing")
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val batch = db.batch() //per eliminazioni multiple insieme
                            for (document in querySnapshot) {
                                batch.delete(document.reference)
                            }
                            batch.commit()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Tutti i documenti eliminati! Collezione distrutta!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Errore distruzione: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                context,
                                "La collezione è già vuota o inesistente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Errore nel recupero dei dati: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(text = "Distruggi Collezione", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        //NOTIFICHE CLOUD: Bottone invia notifica a tutti
        Button(
            onClick = {
                val newNotification = hashMapOf(
                    "title" to "Hai perso il gioco",
                    "message" to "Il Master $displayedName ha inviato una notifica",
                    "authorID" to (currentUser?.uid ?: ""),
                    "timeStamp" to System.currentTimeMillis()
                )

                db.collection("notifiche_bacheca")
                    .add(newNotification)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Notifica inserita nel DB!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Errore invio notifica: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Text(text = "Invia Notifica", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        //Bottone per tornare alla home
        Button(
            onClick = {
                onCloseDebug()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                "CHIUDI",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
