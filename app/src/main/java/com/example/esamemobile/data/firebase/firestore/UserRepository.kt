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
    suspend fun usernameAndImageObserver(userId: String): Flow<Pair<String, String>>
    suspend fun updateUserImage(userId: String, imageUrl: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
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
                    "name" to username,
                    "imageUrl" to ""
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

    override suspend fun usernameAndImageObserver(userId: String): Flow<Pair<String, String>> {
        return callbackFlow {
            val listener = db.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Pair("",""))
                        return@addSnapshotListener
                    }
                    val username = snapshot?.getString("name") ?: ""
                    val image = snapshot?.getString("imageUrl") ?: ""
                    trySend(Pair(username,image))
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun updateUserImage(userId: String, imageUrl: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("imageUrl",imageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}