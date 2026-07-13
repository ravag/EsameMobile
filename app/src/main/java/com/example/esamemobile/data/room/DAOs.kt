package com.example.esamemobile.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDAO {

    @Query("SELECT * FROM characters")
    fun getAll(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :id")
    fun getFromId(id: String): Flow<CharacterEntity?>

    @Upsert
    suspend fun upsert(characterEntity: CharacterEntity)

    @Upsert
    suspend fun upsertAll(characterEntitys: List<CharacterEntity>)

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun delete(id: String)
}