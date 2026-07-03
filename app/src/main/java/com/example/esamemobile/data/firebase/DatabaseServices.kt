package com.example.esamemobile.data.firebase

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlin.collections.hashMapOf

object DatabaseServices {
    val auth = FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    val db = FirebaseFirestore.getInstance()

    @Composable
    fun Test(navController: NavHostController, context: Context) {
        var currentUser by remember { mutableStateOf(auth.currentUser) }
        //Listener allo stato dell'autenticazione per aggiornare la schermata in tempo reale
        LaunchedEffect(Unit) {
            auth.addAuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
            }
        }

        LaunchedEffect(currentUser) {
            if(currentUser != null) {
                navController.navigate(EsameMobileRoute.Home)
                insertNewUser()
                TokenReceiver.newToken()
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun updateToken(token: String) {
        db.collection("users").document(auth.currentUser!!.uid).update("fcmToken",token)
    }

    fun insertNewUser() {
        db.collection("users").document(auth.currentUser!!.uid).set(hashMapOf(
            "fcmToken" to ""
        ))
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

