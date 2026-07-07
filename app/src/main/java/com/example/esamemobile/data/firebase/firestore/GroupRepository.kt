package com.example.esamemobile.data.firebase.firestore

import android.util.Log
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.firebase.DatabaseServices.auth
import com.example.esamemobile.data.firebase.DatabaseServices.db
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

interface GroupRepository {
    //suspend fun addNewGroup(userId: String): Result<Unit> //nessuna implementazione al momento
    suspend fun readGroup(groupId: String): Result<Group?>
    suspend fun getAllUsersGroups(userId: String): Result<List<Group>>
}

class GroupRepositoryImpl(private val db: FirebaseFirestore): GroupRepository {

//    override suspend fun addNewGroup(userId: String): Result<Unit> {
//      TODO("Not yet implemented")
//    }

    override suspend fun readGroup(groupId: String): Result<Group?> {
        return try {
            val doc = db.collection("groups")
                .document(groupId).get()
                .await()
            Result.success(doc.toObject<Group>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsersGroups(userId: String): Result<List<Group>> {
        return try {
            val docs = db.collectionGroup("groups")
                .whereArrayContains("partecipants",userId)
                .get().await()
            Result.success(docs.map { it.toObject<Group>() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}