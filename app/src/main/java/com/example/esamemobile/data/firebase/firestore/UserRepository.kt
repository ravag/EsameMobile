package com.example.esamemobile.data.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun insertNewUser(userId: String): Result<Unit>
    suspend fun updateToken(token: String, userId: String): Result<Unit>
}

class UserRepositoryImpl(private val db: FirebaseFirestore): UserRepository {
    override suspend fun insertNewUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .set(hashMapOf(
                    "fcmToken" to ""
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateToken(token: String, userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("fcmToken",token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}