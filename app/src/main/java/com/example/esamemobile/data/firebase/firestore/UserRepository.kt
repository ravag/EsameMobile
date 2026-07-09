package com.example.esamemobile.data.firebase.firestore

import android.util.Log
import com.example.esamemobile.data.firebase.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun insertNewUser(userId: String): Result<Unit>
    suspend fun updateToken(token: String): Result<Unit>
    suspend fun updateUsername(userId: String, username: String): Result<Unit>
    suspend fun usernameObserver(userId: String): Flow<String>
}

class UserRepositoryImpl(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
): UserRepository {
    override suspend fun insertNewUser(userId: String): Result<Unit> {
        val username = authRepository.currentUser!!.displayName ?: authRepository.currentUser!!.email?.substringBefore("@") ?: "Utente"

        return try {
            db.collection("users").document(userId)
                .set(hashMapOf(
                    "fcmToken" to "",
                    "name" to username
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateToken(token: String): Result<Unit> {
        val userId = authRepository.currentUser?.uid ?: return Result.failure(IllegalStateException("Utente non loggato"))
        return try {
            db.collection("users").document(userId)
                .update("fcmToken",token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUsername(userId: String, username: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("name",username).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun usernameObserver(userId: String): Flow<String> {
        return callbackFlow {
            val listener = db.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend("")
                        Log.i("bug","mandato:  ")
                        return@addSnapshotListener
                    }
                    val username = snapshot?.getString("name") ?: ""
                    trySend(username)
                }
            awaitClose { listener.remove() }
        }
    }

}