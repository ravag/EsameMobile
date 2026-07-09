package com.example.esamemobile.screens.characterLevelUp

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.logging.Level
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.calculateModifier

enum class LevelUpStep {
    CHOOSE_CLASS,
    CHOOSE_PERK_TYPE,
    EDIT_STATISTICS,
    EDIT_ABILITIES
    // Potrebbero servirmi lo step subclass e lo step rollHP
}

enum class LevelUpOption {
    GAIN_PE_CHAR_3,
    GAIN_PE_CHAR_5,
    UPGRADE_ABILITY,
    BASE_CLASS_ABILITY,
    //Dal livello 6+:
    ADVANCED_CLASS_ABILITY,
    NEW_CLASS_BASE_ABILITY,
    STAT_BONUS_2
}

data class LevelUpState(
    val character: Character? = null,
    val currentStep: LevelUpStep = LevelUpStep.CHOOSE_PERK_TYPE,

    val currentLevel: Int = 1,
    val strengthModifier: Int = 0,
    val hpRolled: Int = 0,
    val isHpRolled: Boolean = false,

    val selectedOption: LevelUpOption? = null,
    val availableOptions: List<LevelUpOption> = emptyList(),

    val hasTakenCharPlus3Beforeval : Boolean = false,
    val hasAllBaseAbilitiesOfAClass: Boolean = false,

    val selectedStatToUpgrade: String? = null,
    val selectedAbilityToUpgrade: String? = null,
    val selectedClassId: String? = null,
    val selectedSubClassId: String? = null,

    val isLevelUpComplete: Boolean = false
)

data class LevelUpActions(
    val onRollHp: () -> Unit,
    val onSelectedOption: (LevelUpOption) -> Unit,
    val onSelectStatToUpgrade: (String) -> Unit,
    val onSelectAbilityToUpgrade: (String) -> Unit,
    val onSelectedClass: (String) -> Unit,
    val onSelectedSubClass: (String) -> Unit,
    val onConfirmLevelUp: (Context, () -> Unit) -> Unit
)

class LevelUpViewModel(
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow<LevelUpState?>(null)
    val state = _state.asStateFlow()

    fun initLevelUp(charId: String) {
        if (_state.value != null) return

        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            val character = characterRepository.readCharacter(userId = userId, charId = charId).getOrNull() ?: return@launch

            val nextLevel = character.level + 1
            val options = mutableListOf<LevelUpOption>()

            val hasTakenCharPlus3Before = character.classAbilitiesList.contains("BONUS_PE_3")

            if (!hasTakenCharPlus3Before) {
                options.add(LevelUpOption.GAIN_PE_CHAR_3)
            } else {
                options.add(LevelUpOption.GAIN_PE_CHAR_5)
            }
            options.add(LevelUpOption.UPGRADE_ABILITY)
            options.add(LevelUpOption.BASE_CLASS_ABILITY)

            if (nextLevel >= 6) {
                val hasAllBase = checkVisualIfHasAllBaseAbilities(character)
                if (hasAllBase) {
                    options.add(LevelUpOption.ADVANCED_CLASS_ABILITY)
                }
                options.add(LevelUpOption.NEW_CLASS_BASE_ABILITY)
                options.add(LevelUpOption.STAT_BONUS_2)
            }

            _state.value = LevelUpState(
                character = character,
                currentLevel = nextLevel,
                availableOptions = options
            )
        }
    }

    val actions = LevelUpActions(
        onRollHp = {
            _state.update { currentState ->
                if (currentState == null || currentState.isHpRolled) return@update currentState
                val char = currentState.character ?: return@update currentState

                val diceRoll = (1..6).random()
                val strengthModifier = calculateModifier(char.strength)
                val finalHpGained = maxOf(1, diceRoll + strengthModifier)
                currentState.copy(hpRolled = finalHpGained, isHpRolled = true)
            }
        },

        onSelectedOption = { option ->
            _state.update { it?.copy(selectedOption = option) }
        },

        onSelectStatToUpgrade = { stat ->
            _state.update { it?.copy(selectedStatToUpgrade = stat) }
        },

        onSelectAbilityToUpgrade = { ability ->
            _state.update { it?.copy(selectedAbilityToUpgrade = ability) }
        },

        onSelectedClass = { classId ->
            _state.update { it?.copy(selectedClassId = classId) }
        },

        onSelectedSubClass = { subClassId ->
            _state.update { it?.copy(selectedSubClassId = subClassId) }
        },

        onConfirmLevelUp = { context, navigateBack ->
            val currentState = _state.value ?: return@LevelUpActions
            val char = currentState.character ?: return@LevelUpActions
            val userId = authRepository.currentUser?.uid ?: return@LevelUpActions

            if (!currentState.isHpRolled) {
                Toast.makeText(context, "Tira il dado per gli HP", Toast.LENGTH_SHORT).show()
                return@LevelUpActions
            }
            if (currentState.selectedOption == null) {
                Toast.makeText(context, "Seleziona una ricompensa", Toast.LENGTH_SHORT).show()
                return@LevelUpActions
            }

            if (char.level == 0 && currentState.selectedClassId == null) {
                Toast.makeText(context, "Seleziona una classe", Toast.LENGTH_SHORT).show()
                return@LevelUpActions
            }

            if (currentState.currentLevel == 6 && currentState.selectedSubClassId == null) {
                Toast.makeText(context, "Seleziona una sotto-classe", Toast.LENGTH_SHORT).show()
                return@LevelUpActions
            }

            var updatedChar = char.copy(
                level = currentState.currentLevel,
                maxHP = char.maxHP + currentState.hpRolled,
                currentHP = char.currentHP + currentState.hpRolled
            )

            if (char.level == 0 && currentState.selectedClassId != null) {
                updatedChar = updatedChar.copy(chosenClass = currentState.selectedClassId)
            }

            if (currentState.currentLevel == 6 && currentState.selectedSubClassId != null) {
                updatedChar = updatedChar.copy(classAbilitiesList = updatedChar.classAbilitiesList + "SUBCLASS_${currentState.selectedSubClassId}")
            }

            val charismaModifier = calculateModifier(char.charisma)

            updatedChar = when (currentState.selectedOption) {
                LevelUpOption.GAIN_PE_CHAR_3 -> {
                    val pe = charismaModifier + 3
                    updatedChar.copy(
                        peAvailable = updatedChar.peAvailable + pe,
                        classAbilitiesList = updatedChar.classAbilitiesList + "BOUNS_PE_3"
                    )
                }
                LevelUpOption.GAIN_PE_CHAR_5 -> {
                    val pe = charismaModifier + 5
                    updatedChar.copy(
                        peAvailable = updatedChar.peAvailable + pe,
                        classAbilitiesList = updatedChar.classAbilitiesList + "BOUNS_PE_5"
                    )
                }
                LevelUpOption.STAT_BONUS_2 -> {
                    when (currentState.selectedStatToUpgrade) {
                        "Forza" -> updatedChar.copy(strength = minOf(10, updatedChar.strength + 2))
                        "Agilità" -> updatedChar.copy(agility = minOf(10, updatedChar.agility + 2))
                        "Intelligenza" -> updatedChar.copy(intelligence = minOf(10, updatedChar.intelligence + 2))
                        "Carisma" -> updatedChar.copy(charisma = minOf(10, updatedChar.charisma + 2))
                        "Potere" -> updatedChar.copy(power = minOf(10, updatedChar.power + 2))
                        else -> updatedChar
                    }
                }
                LevelUpOption.UPGRADE_ABILITY,
                LevelUpOption.BASE_CLASS_ABILITY,
                LevelUpOption.ADVANCED_CLASS_ABILITY,
                LevelUpOption.NEW_CLASS_BASE_ABILITY -> {
                    val currentAbilities = updatedChar.classAbilitiesList.toMutableList()
                    currentState.selectedAbilityToUpgrade?.let { currentAbilities.add(it) }
                    updatedChar.copy(classAbilitiesList = currentAbilities)
                }
                else -> updatedChar
            }

            viewModelScope.launch {
                val result = characterRepository.updateCharacter(userId, updatedChar)
                if (result.isSuccess) {
                    _state.update { it?.copy(isLevelUpComplete = true, character = updatedChar) }
                    navigateBack()
                } else {
                    Toast.makeText(context, "Errore durante il salvataggio: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    private fun checkVisualIfHasAllBaseAbilities(character: Character): Boolean {
        return false
    }
}

