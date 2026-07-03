package com.example.esamemobile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.esamemobile.ui.theme.EsameMobileTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.esamemobile.data.firebase.DatabaseServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        setContent {
            EsameMobileTheme {
                val context = LocalContext.current
//                val auth = remember { FirebaseAuth.getInstance() }
//                val db = remember { FirebaseFirestore.getInstance() }

                val navController = rememberNavController()

                //var currentUser by remember { mutableStateOf(auth.currentUser) }
//                var showDebugDatabaseScreen by remember { mutableStateOf(false) }
//
                //Chiediamo il permesso per le notifiche
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    } else {
                        mutableStateOf(true)
                    }
                }
//
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted -> hasNotificationPermission = isGranted }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val channel = NotificationChannel(
                    "canale_gdr",
                    "Notifiche GDR",
                    NotificationManager.IMPORTANCE_DEFAULT
                    )

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)

//                //Listener allo stato dell'autenticazione per aggiornare la schermata in tempo reale
//                LaunchedEffect(Unit) {
//                    auth.addAuthStateListener { firebaseAuth ->
//                        currentUser = firebaseAuth.currentUser
//                    }
//                }
//
//                //Listener per le notifiche della bacheca quando l'utente è loggato
//                LaunchedEffect(currentUser) {
//                    if(currentUser != null) {
//
//                        navController.navigate(EsameMobileRoute.Home)
//
//                        val channel = NotificationChannel(
//                            "canale_gdr",
//                            "Notifiche GDR",
//                            NotificationManager.IMPORTANCE_DEFAULT
//                        )
//
//                        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                        manager.createNotificationChannel(channel)
//
//                        db.collection("Notifiche_bacheca")
//                            .addSnapshotListener { snapshots, e ->
//                                if (e != null) return@addSnapshotListener
//
//                                val lastDocIncoming = snapshots?.documentChanges?.find { it.type == DocumentChange.Type.ADDED }
//                                if (lastDocIncoming != null) {
//                                    val title = lastDocIncoming.document.getString("titolo") ?: "Nuova Notifica"
//                                    val msg = lastDocIncoming.document.getString("messaggio") ?: ""
//                                    val authorId = lastDocIncoming.document.getString("autoreId") ?: ""
//
//                                    if (authorId != auth.currentUser?.uid) {
//                                        val builder = NotificationCompat.Builder(context, "canale_gdr")
//                                            .setSmallIcon(android.R.drawable.stat_notify_chat)
//                                            .setContentTitle(title)
//                                            .setContentText(msg)
//                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                                            .setAutoCancel(true)
//
//                                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                                        notificationManager.notify(
//                                            System.currentTimeMillis().toInt(),
//                                            builder.build()
//                                        )
//                                    }
//                                }
//                            }
//                    }
//                }


                //Smistamento Schermate
//                Scaffold(
//                    modifier = Modifier.fillMaxSize(),
//                    containerColor = Color.Black
//                ) { innerPadding ->
//                    val user = currentUser
//
//                    if (user != null) {
//                        //Utente loggato
//                        if (showDebugDatabaseScreen) {
//                            val displayedName = user.displayName ?: user.email?.substringBefore("@") ?: "Utente"
//                            DebugDatabaseScreen(
//                                displayedName = displayedName,
//                                currentUser = user,
//                                db = db,
//                                context = context,
//                                onCloseDebug = { showDebugDatabaseScreen = false }
//                            )
//                        } else {
//                            HomeScreen(
//                                onNavigationToDebug = { showDebugDatabaseScreen = true },
//                                modifier = Modifier.padding(innerPadding)
//                            )
//                        }
//                    } else {
//                        //Utente non loggato
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(Color.Black),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            LoginScreen(modifier=Modifier.padding(innerPadding))
//                        }
//                    }
//                }

                DatabaseServices.Test(navController,context)
                EsameMobileNavGraph(navController)
            }
        }
    }
}