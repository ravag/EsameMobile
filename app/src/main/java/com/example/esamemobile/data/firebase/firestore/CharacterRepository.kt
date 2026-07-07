package com.example.esamemobile.data.firebase.firestore

import com.example.esamemobile.data.Character
import com.example.esamemobile.data.firebase.DatabaseServices
import com.example.esamemobile.data.firebase.DatabaseServices.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

interface CharacterRepository {
    suspend fun insertNewCharacter(userId: String, char: Character): Result<Unit>
    suspend fun readCharacter(userId: String, charId: Int): Result<Character?>
    suspend fun getAllUserCharacters(userId: String): Result<List<Character>>
}

class CharacterRepositoryImpl(private val db: FirebaseFirestore): CharacterRepository {
    override suspend fun insertNewCharacter(
        userId: String,
        char: Character
    ): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("characters").document("${char.id}")
                .set(char).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readCharacter(
        userId: String,
        charId: Int
    ): Result<Character?> {
        return try {
            val doc = db.collection("users").document(userId)
                .collection("characters").document("$charId")
                .get().await()
            Result.success(doc.toObject<Character>())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUserCharacters(userId: String): Result<List<Character>> {
        return try {
            val docs = db.collection("users").document(userId)
                .collection("characters").get()
                .await()
            Result.success(docs.map { it.toObject<Character>() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}