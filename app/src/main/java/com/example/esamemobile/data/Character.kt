package com.example.esamemobile.data

import com.example.esamemobile.data.staticData.AgeMalus
import com.example.esamemobile.data.staticData.ClassAbility
import com.example.esamemobile.data.staticData.GameClass
import com.example.esamemobile.screens.characterCreation.AbilityItem
import com.example.esamemobile.screens.characterCreation.InventoryItem
import java.util.UUID

data class Character(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val ageMalus: AgeMalus? = null,
    val level: Int = 0,

    val chosenClass: GameClass?,

    val peAvailable: Int = 0,

    val strength: Int,
    val agility: Int,
    val intelligence: Int,
    val charisma: Int,
    val power: Int,

    val currentHP: Int,
    val maxHP: Int,

    val abilitiesList: List<AbilityItem> = emptyList(),
    val classAbilitiesList: List<ClassAbility> = emptyList(),
    val inventoryList: List<InventoryItem> = emptyList(),

    val speed: Double,
    val maxCapacity: Int,
    val armor: ArmorTypes? = null
)

enum class ArmorTypes { LIGHT, MEDIUM, HEAVY }