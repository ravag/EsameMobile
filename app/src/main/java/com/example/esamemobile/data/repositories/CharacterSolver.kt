package com.example.esamemobile.data.repositories

import com.example.esamemobile.data.Character
import com.example.esamemobile.data.UiCharacter

class CharacterSolver (private val staticRepository: StaticDataRepository) {

    fun solve(character: Character) : UiCharacter {

        val chosenClass = character.chosenClass?.let { staticRepository.getClassById(it) }

        return UiCharacter(
            character = character,
            chosenClass = chosenClass,
            ageMalus = character.ageMalus?.let {
                id -> staticRepository.allAgeMalus.find { it.id == id }
            },
            classAbilities = chosenClass?.let { it.baseAbilities + it.advancedAbilities }
                ?.filter { it.id in character.classAbilitiesList } ?: emptyList()
        )
    }
}