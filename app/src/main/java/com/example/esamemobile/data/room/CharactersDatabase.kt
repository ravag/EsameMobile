package com.example.esamemobile.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CharacterEntity::class], version = 5)
abstract class CharactersDatabase : RoomDatabase() {
    abstract fun characterDAO(): CharacterDAO
}