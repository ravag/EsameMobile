package com.example.esamemobile.data

import com.example.esamemobile.data.room.CharacterEntity
import com.example.esamemobile.data.staticData.AgeMalus
import com.example.esamemobile.data.staticData.ClassAbility
import com.example.esamemobile.data.staticData.GameClass
import com.example.esamemobile.screens.characterCreation.AbilityItem
import com.example.esamemobile.screens.characterCreation.InventoryItem
import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class Character(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val imageUrl: String = "",
    val age: Int = 0,
    val ageMalus: String? = null,
    val level: Int = 0,

    val chosenClass: String? = null,

    val peAvailable: Int = 0,

    val strength: Int = 0,
    val agility: Int = 0,
    val intelligence: Int = 0,
    val charisma: Int = 0,
    val power: Int = 0,

    val currentHP: Int = 0,
    val maxHP: Int = 0,

    val abilitiesList: List<AbilityItem> = emptyList(),
    val classAbilitiesList: List<String> = emptyList(),
    val inventoryList: List<InventoryItem> = emptyList(),

    val speed: Double = 0.0,
    val maxCapacity: Int = 0,
    val armor: ArmorTypes = ArmorTypes.NONE,
) {
    fun toEntity(): CharacterEntity = CharacterEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        age = age,
        ageMalus = ageMalus,
        level = level,
        chosenClass = chosenClass,
        peAvailable = peAvailable,
        strength = strength,
        agility = agility,
        intelligence = intelligence,
        charisma = charisma,
        power = power,
        currentHP = currentHP,
        maxHP = maxHP,
        abilitiesList = Json.encodeToString(abilitiesList),
        classAbilitiesList = Json.encodeToString(classAbilitiesList),
        inventoryList = Json.encodeToString(inventoryList),
        speed = speed,
        maxCapacity = maxCapacity,
        armor = armor.name
    )
}

enum class ArmorTypes(val text: String) { LIGHT("leggera"), MEDIUM("media"), HEAVY("pesante"), NONE("nessuna") }

fun calculateModifier(stat: Int): Int {
    return when (stat) {
        in Int.MIN_VALUE..1 -> -1
        in 2..3 -> 0
        in 4..5 -> 1
        in 6..7 -> 2
        in 8..9 -> 3
        else -> 4
    }
}