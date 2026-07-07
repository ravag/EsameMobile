package com.example.esamemobile.data.firebase.firestore

import com.example.esamemobile.data.firebase.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun insertNewUser(userId: String): Result<Unit>
    suspend fun updateToken(token: String): Result<Unit>
}

class UserRepositoryImpl(
    private val db: FirebaseFirestore,
    private val authRepository: AuthRepository
): UserRepository {
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

}