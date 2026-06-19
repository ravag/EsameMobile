package com.example.esamemobile

import android.R
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.esamemobile.ui.theme.EsameMobileTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EsameMobileTheme {
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

    var currentUser by remember { mutableStateOf(auth.currentUser) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            email = ""
            password = ""
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
            //Se l'utente è loggato mettiamo la schermata di benvenuto
            val displayedName = currentUser?.displayName
                ?: currentUser?.email?.substringBefore("@")
                ?: "Utente"

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Benvenuto\n${displayedName}",
                    color = Color.White,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
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
                    onValueChange = {email = it},
                    label = {Text("Email", color = Color.Gray)},
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
                    onValueChange = {password = it},
                    label = {Text("Password", color = Color.Gray)},
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
                                        Toast.makeText(context, "Accesso eseguito!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val error = task.exception

                                        if (error is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                            error is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                                            auth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener { regTask ->
                                                    if (regTask.isSuccessful) {
                                                        Toast.makeText(context,"Registrato con successo!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        val regError = regTask.exception
                                                        if (regError is com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                                                            Toast.makeText(context, "Password Debole! Usa lettere, numeri e simboli.", Toast.LENGTH_LONG).show()
                                                        } else if (regError is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                                            Toast.makeText(context, "Password Errata. Nome utente già esistente.", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "Errore: ${regError?.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(context, "Password errata o account non valido", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Riempi tutti i campi!", Toast.LENGTH_SHORT).show()
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
                                val result = credentialManager.getCredential(context = context, request = request)
                                val credential = result.credential

                                if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken

                                    //Passa le credenziali a Firebase
                                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                                    auth.signInWithCredential(firebaseCredential)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Login Riuscito!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Login Fallito! (Come te)", Toast.LENGTH_SHORT).show()
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