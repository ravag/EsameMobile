package com.example.esamemobile.data

import com.example.esamemobile.data.staticData.AgeMalus
import com.example.esamemobile.data.staticData.ClassAbility
import com.example.esamemobile.data.staticData.GameClass
import com.example.esamemobile.screens.characterCreation.AbilityItem
import com.example.esamemobile.screens.characterCreation.InventoryItem
import com.google.firebase.firestore.DocumentId
import java.util.UUID

data class Character(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
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
    val armor: ArmorTypes? = null,
    val imageUri: String = ""
)

enum class ArmorTypes { LIGHT, MEDIUM, HEAVY }