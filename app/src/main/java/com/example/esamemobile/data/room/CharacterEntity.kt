package com.example.esamemobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.esamemobile.data.ArmorTypes
import com.example.esamemobile.data.Character
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val age: Int,
    val ageMalus: String?,
    val level: Int,

    val chosenClass: String?,
    val chosenSubClass: String?,

    val peAvailable: Int,

    val strength: Int,
    val agility: Int,
    val intelligence: Int,
    val charisma: Int,
    val power: Int,

    val currentHP: Int,
    val maxHP: Int,
    val tempHP: Int,

    val abilitiesList: String,
    val classAbilitiesList: String,
    val inventoryList: String,

    val speed: Double,
    val maxCapacity: Int,
    val armor: String
) {
    fun toCharacter(): Character = Character(
        id = id,
        name = name,
        imageUrl = imageUrl,
        age = age,
        ageMalus = ageMalus,
        level = level,
        chosenClass = chosenClass,
        chosenSubClass = chosenSubClass,
        peAvailable = peAvailable,
        strength = strength,
        agility = agility,
        intelligence = intelligence,
        charisma = charisma,
        power = power,
        currentHP = currentHP,
        maxHP = maxHP,
        tempHP = tempHP,
        abilitiesList = Json.decodeFromString(abilitiesList),
        classAbilitiesList = Json.decodeFromString(classAbilitiesList),
        inventoryList = Json.decodeFromString(inventoryList),
        speed = speed,
        maxCapacity = maxCapacity,
        armor = ArmorTypes.valueOf(armor)
    )
}