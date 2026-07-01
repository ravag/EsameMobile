package com.example.esamemobile.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
