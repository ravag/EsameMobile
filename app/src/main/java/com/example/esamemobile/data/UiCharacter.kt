package com.example.esamemobile.data

import com.example.esamemobile.data.staticData.AgeMalus
import com.example.esamemobile.data.staticData.ClassAbility
import com.example.esamemobile.data.staticData.GameClass

data class UiCharacter(
    val character: Character,
    val chosenClass: GameClass?,
    val chosenSubClass: GameClass?,
    val ageMalus: AgeMalus?,
    val classAbilities: List<ClassAbility>,
    val subClassAbilities: List<ClassAbility>
) {
    val stats: List<Int> = listOf(
        character.strength,
        character.agility,
        character.intelligence,
        character.charisma,
        character.power
    )

    val normalizedStats: List<Float>
        get() = stats.map { it.toFloat() / 10 }
}