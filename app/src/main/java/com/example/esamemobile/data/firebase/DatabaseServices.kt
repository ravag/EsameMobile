package com.example.esamemobile.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

object DatabaseServices {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    @Composable
    fun test(navController: NavHostController, context: Context) {
        var currentUser by remember { mutableStateOf(auth.currentUser) }
        //Listener allo stato dell'autenticazione per aggiornare la schermata in tempo reale
        LaunchedEffect(Unit) {
            auth.addAuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
            }
        }

        //Listener per le notifiche della bacheca quando l'utente è loggato
        LaunchedEffect(currentUser) {
            if(currentUser != null) {
                navController.navigate(EsameMobileRoute.Home)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun insertNewUser() {
        db.collection("users").document(auth.currentUser!!.uid).set("ciao")
    }
    fun insertNewCharacter(char: Character) {
        db.collection("users").document(auth.currentUser!!.uid).collection("characters").document("${char.id}").set(char)
    }

    fun readCharacter(charId: Int,onComplete:(Character?) -> Unit) {

        db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("characters")
                .document("$charId")
                .get()
            .addOnSuccessListener { documentSnapshot ->
                val c = documentSnapshot.toObject<Character>()
                onComplete(c)
            }
            .addOnFailureListener { exception -> onComplete(null) }


    }

    fun getAllUserCharacters(onComplete: (List<Character>?) -> Unit) {
        db.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("characters")
            .get()
            .addOnSuccessListener { results ->
                val list = mutableListOf<Character>()
                for (doc in results) {
                    list.add(doc.toObject<Character>())
                }
                onComplete(list)
            }
            .addOnFailureListener { e ->
                Log.w("Errore",e)
                onComplete(null)
            }
    }

    fun getAllUserGroups(onComplete: (List<Group>?) -> Unit) {
        db.collectionGroup("groups")
            .whereArrayContains("partecipants",auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { results ->
                val list = mutableListOf<Group>()
                for (doc in results) {
                    list.add(doc.toObject<Group>())
                }
                onComplete(list)
            }
            .addOnFailureListener { e ->
                Log.w("Errore",e)
                onComplete(null)}
    }

    fun readGroup(groupId: String,onComplete:(Group?) -> Unit) {

        db.collection("users")
            .document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val g = documentSnapshot.toObject<Group>()
                onComplete(g)
            }
            .addOnFailureListener { exception -> onComplete(null) }


    }
}

