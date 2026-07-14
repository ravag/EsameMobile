package com.example.esamemobile.data.repositories

import android.util.Log
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.room.CharacterDAO
import com.example.esamemobile.utilities.ConnectivityChecker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

interface CharacterRepository {
    suspend fun insertNewCharacter(userId: String?, char: Character): Result<Unit>
    suspend fun readCharacter(userId: String?,ownerId: String?, charId: String?): Result<Character?>
    fun getAllUserCharacters(): Flow<List<Character>>
    suspend fun updateCharacter(userId: String?, character: Character): Result<Unit>
    suspend fun deleteCharacter(userId: String?, charId: String): Result<Unit>
    suspend fun fireStoreSync(userId: String?): Result<Unit>
}

class CharacterRepositoryImpl(
    private val db: FirebaseFirestore,
    private val characterDAO: CharacterDAO,
    private val connectivityChecker: ConnectivityChecker
): CharacterRepository {

    override suspend fun insertNewCharacter(userId: String?, char: Character): Result<Unit> {
        return try {
            characterDAO.upsert(char.toEntity())

            if (userId != null && connectivityChecker.isOnline()) {
                //Se non c'è la connessione non deve fallire tutta l'operazione
                try {
                    db.collection("users").document(userId)
                        .collection("characters").document(char.id)
                        .set(char).await()
                } catch (e: Exception) {
                    Log.w("debug","Riscontrato errore nel salvataggio su firebase ${e.message}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun readCharacter(userId: String?,ownerId: String?, charId: String?): Result<Character?> {
        if (charId == null) return Result.failure(IllegalStateException("Nessun personaggio selezionato"))
        return try {
            if (ownerId == null || userId == null || userId == ownerId) {
                Result.success(characterDAO.getFromId(charId).firstOrNull()?.toCharacter())
            } else {
                val doc = db.collection("users").document(ownerId)
                    .collection("characters").document(charId)
                    .get().await()
                Result.success(doc.toObject<Character>())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllUserCharacters(): Flow<List<Character>> {
        return characterDAO.getAll().map { entities -> entities.map { it.toCharacter() } }
    }

    override suspend fun updateCharacter(userId: String?, character: Character): Result<Unit> {
        return try {
            characterDAO.upsert(character.toEntity())

            if (userId != null && connectivityChecker.isOnline()) {
                try {
                    db.collection("users").document(userId)
                        .collection("characters").document(character.id)
                        .set(character).await()
                } catch (e: Exception) {
                    Log.w("debug","Errore nell'update del personaggio ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCharacter(userId: String?, charId: String): Result<Unit> {
        return try {
            characterDAO.delete(charId)

            if (userId != null && connectivityChecker.isOnline()) {
                try {
                    db.collection("users").document(userId)
                        .collection("characters").document(charId)
                        .delete().await()
                } catch (e: Exception) {
                    Log.w("debug","Errore nell'eliminazione del personaggio ${e.message}")
                }
            }


            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fireStoreSync(userId: String?): Result<Unit> {
        if (!connectivityChecker.isOnline() || userId == null) return Result.success(Unit)
        return try {
            val charactersDoc = db.collection("users").document(userId)
                .collection("characters").get().await()

            characterDAO.upsertAll(charactersDoc.map { documentSnapshot ->
                documentSnapshot.toObject<Character>().toEntity()
            })



            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}