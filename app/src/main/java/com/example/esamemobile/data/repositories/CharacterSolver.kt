package com.example.esamemobile.data.repositories

import com.example.esamemobile.data.Character
import com.example.esamemobile.data.UiCharacter

class CharacterSolver (private val staticRepository: StaticDataRepository) {

    fun solve(character: Character) : UiCharacter {

        val chosenClass = character.chosenClass?.let { staticRepository.getClassById(it) }
        val chosenSubClass = character.chosenSubClass?.let { staticRepository.getClassById(it) }

        return UiCharacter(
            character = character,
            chosenClass = chosenClass,
            chosenSubClass = chosenSubClass,
            ageMalus = character.ageMalus?.let {
                id -> staticRepository.allAgeMalus.find { it.id == id }
            },
            classAbilities = chosenClass?.let { it.baseAbilities + it.advancedAbilities }
                ?.filter { it.id in character.classAbilitiesList } ?: emptyList(),
            subClassAbilities = chosenSubClass?.let { it.baseAbilities }
                ?.filter { it.id in character.classAbilitiesList } ?: emptyList()
        )
    }
}