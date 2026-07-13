package com.example.esamemobile.screens.characterLevelUp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.repositories.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.calculateModifier
import com.example.esamemobile.data.repositories.StaticDataRepository
import com.example.esamemobile.data.staticData.GameClass

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
    val hpRolled: Int? = null,
    val pureDiceRoll: Int? = null,
    val isHpRolled: Boolean = false,

    val selectedOption: LevelUpOption? = null,
    val availableOptions: List<LevelUpOption> = emptyList(),

    val hasTakenCharPlus3Beforeval : Boolean = false,
    val hasAllBaseAbilitiesOfAClass: Boolean = false,

    val selectedStatToUpgrade: String? = null,
    val selectedAbilityToUpgrade: String? = null,
    val customAbilityDescription: String = "",
    val selectedClassId: String? = null,
    val selectedSubClassId: String? = null,

    val gameClasses: List<GameClass>,

    val isLevelUpComplete: Boolean = false
) {
    fun getStatPreview(statName: String): Int {
        val baseValue = when (statName.lowercase().trim()) {
            "forza" -> character?.strength
            "agilità" -> character?.agility
            "intelligenza" -> character?.intelligence
            "carisma" -> character?.charisma
            "potere" -> character?.power
            else -> null
        } ?: return 0
        return minOf(10, baseValue + 2)
    }

    val isCurrentStepValid: Boolean
        get() = when (currentStep) {
            LevelUpStep.CHOOSE_CLASS -> {
                if (character?.level == 0) selectedClassId != null
                else if (currentLevel == 6) selectedSubClassId != null
                else true
            }
            LevelUpStep.CHOOSE_PERK_TYPE -> {
                isHpRolled && selectedOption != null
            }
            LevelUpStep.EDIT_STATISTICS -> selectedStatToUpgrade != null
            LevelUpStep.EDIT_ABILITIES -> selectedAbilityToUpgrade != null
        }
}

data class LevelUpActions(
    val onRollHp: () -> Unit,
    val onSelectedOption: (LevelUpOption) -> Unit,
    val onSelectStatToUpgrade: (String) -> Unit,
    val onSelectAbilityToUpgrade: (String) -> Unit,
    val onUpdateAbilityDescription: (String) -> Unit,
    val onSelectedClass: (String) -> Unit,
    val onSelectedSubClass: (String) -> Unit,
    val onConfirmLevelUp: (Context, () -> Unit) -> Unit,
    val onNextStep: () -> Unit,
    val onBackStep: (() -> Unit) -> Unit
)

class LevelUpViewModel(
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository,
    private val staticDataRepository: StaticDataRepository
) : ViewModel() {
    private val _state = MutableStateFlow<LevelUpState?>(null)
    val state = _state.asStateFlow()

    fun initLevelUp(charId: String) {
        if (_state.value != null) return

        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid
            val character = characterRepository.readCharacter(userId = userId, ownerId = userId ,charId = charId).getOrNull() ?: return@launch

            val nextLevel = character.level + 1
            val options = mutableListOf<LevelUpOption>()

            val hasTakenCharPlus3Before = character.classAbilitiesList.contains("BONUS_PE_3")
            val hasTakenCharPlus5Before = character.classAbilitiesList.contains("BONUS_PE_5")
            val hasTakenUpgradeAbility = character.classAbilitiesList.contains("UPGRADE_ABILITY")
            val hasTakenStatBonus = character.classAbilitiesList.contains("BONUS_STAT_2")
            val hasTakenNewClassBaseAbility = character.classAbilitiesList.contains("TAKEN_NEW_CLASS_BASE_ABILITIES")

            if (!hasTakenCharPlus3Before) {
                options.add(LevelUpOption.GAIN_PE_CHAR_3)
            } else if (!hasTakenCharPlus5Before){
                options.add(LevelUpOption.GAIN_PE_CHAR_5)
            }

            if (!hasTakenUpgradeAbility) {
                options.add(LevelUpOption.UPGRADE_ABILITY)
            }

            val classesFromJson = staticDataRepository.allGameClasses

            val learnedAbilitiesIds = character.classAbilitiesList.map { it.trim().lowercase() }

            if (character.level == 0) {
                options.add(LevelUpOption.BASE_CLASS_ABILITY)
            } else {
                val currentCharacterClass = classesFromJson.find { it.id == character.chosenClass }

                if (currentCharacterClass != null) {
                    val learnedBaseAbilitiesCount = currentCharacterClass.baseAbilities.count { baseAbility ->
                        learnedAbilitiesIds.contains(baseAbility.id.trim().lowercase())
                    }
                    val totalBaseAbilities = currentCharacterClass.baseAbilities.size
                    val hasAllBaseAbilities = learnedBaseAbilitiesCount == totalBaseAbilities

                    if (!hasAllBaseAbilities) {
                        options.add(LevelUpOption.BASE_CLASS_ABILITY)
                    }

                    if (hasAllBaseAbilities && nextLevel >= 6) {
                        val learnedAdvancedAbilitiesCount = currentCharacterClass.advancedAbilities.count { advAbility ->
                            learnedAbilitiesIds.contains(advAbility.id.trim().lowercase())
                        }
                        val totalAdvancedAbilities = currentCharacterClass.advancedAbilities.size

                        if (learnedAdvancedAbilitiesCount < totalAdvancedAbilities) {
                            options.add(LevelUpOption.ADVANCED_CLASS_ABILITY)
                        }
                    }
                }
            }

            if (nextLevel >= 6) {
                if (!hasTakenStatBonus) {
                    options.add(LevelUpOption.STAT_BONUS_2)
                }

                if (!hasTakenNewClassBaseAbility) {
                    options.add(LevelUpOption.NEW_CLASS_BASE_ABILITY)
                }
            }

            val initialStep = when {
                character.level == 0 -> LevelUpStep.CHOOSE_CLASS
                nextLevel == 6 -> LevelUpStep.CHOOSE_CLASS
                else -> LevelUpStep.CHOOSE_PERK_TYPE
            }

            _state.value = LevelUpState(
                character = character,
                currentStep = initialStep,
                currentLevel = nextLevel,
                availableOptions = options,
                gameClasses = classesFromJson
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
                currentState.copy(pureDiceRoll = diceRoll, hpRolled = finalHpGained, isHpRolled = true)
            }
        },

        onSelectedOption = { option ->
            _state.update {
                it?.copy(
                    selectedOption = option,
                    selectedAbilityToUpgrade = null,
                    selectedStatToUpgrade = null
                )
            }
        },

        onSelectStatToUpgrade = { stat ->
            _state.update { it?.copy(selectedStatToUpgrade = stat.trim()) }
        },

        onSelectAbilityToUpgrade = { ability ->
            _state.update { currentState ->
                val currentDesc = currentState?.character?.abilitiesList?.find { it.name == ability }?.description ?: ""

                currentState?.copy(
                    selectedAbilityToUpgrade = ability,
                    customAbilityDescription = currentDesc
                )
            }
        },

        onUpdateAbilityDescription = { text ->
            _state.update { it?.copy(customAbilityDescription = text) }
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
            val userId = authRepository.currentUser?.uid

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
                maxHP = char.maxHP + (currentState.hpRolled ?: 0),
                currentHP = char.currentHP + (currentState.hpRolled ?: 0)
            )

            if (char.level == 0) {
                updatedChar = updatedChar.copy(chosenClass = currentState.selectedClassId)
            }

            if (currentState.currentLevel == 6) {
                updatedChar = updatedChar.copy(classAbilitiesList = updatedChar.classAbilitiesList + "SUBCLASS_${currentState.selectedSubClassId}")
            }

            val charismaModifier = calculateModifier(char.charisma)

            updatedChar = when (currentState.selectedOption) {
                LevelUpOption.GAIN_PE_CHAR_3 -> {
                    val pe = charismaModifier + 3
                    updatedChar.copy(
                        peAvailable = updatedChar.peAvailable + pe,
                        classAbilitiesList = updatedChar.classAbilitiesList + "BONUS_PE_3"
                    )
                }
                LevelUpOption.GAIN_PE_CHAR_5 -> {
                    val pe = charismaModifier + 5
                    updatedChar.copy(
                        peAvailable = updatedChar.peAvailable + pe,
                        classAbilitiesList = updatedChar.classAbilitiesList + "BONUS_PE_5"
                    )
                }
                LevelUpOption.STAT_BONUS_2 -> {
                    val currentAbilities = updatedChar.classAbilitiesList.toMutableList()
                    currentAbilities.add("BONUS_STAT_2")

                    val baseUpgradedChar = when (currentState.selectedStatToUpgrade?.lowercase()?.trim()) {
                        "forza" -> updatedChar.copy(strength = minOf(10, updatedChar.strength + 2))
                        "agilità" -> updatedChar.copy(agility = minOf(10, updatedChar.agility + 2))
                        "intelligenza" -> updatedChar.copy(intelligence = minOf(10, updatedChar.intelligence + 2))
                        "carisma" -> updatedChar.copy(charisma = minOf(10, updatedChar.charisma + 2))
                        "potere" -> updatedChar.copy(power = minOf(10, updatedChar.power + 2))
                        else -> updatedChar
                    }
                        baseUpgradedChar.copy(classAbilitiesList = currentAbilities)
                }
                LevelUpOption.UPGRADE_ABILITY -> {
                    val chosenAbilityName = currentState.selectedAbilityToUpgrade
                    val hasEvolution = updatedChar.abilitiesList.any { it.name == chosenAbilityName }

                    if (hasEvolution) {
                        val updatedAbilities = updatedChar.abilitiesList.map { ability ->
                            if (ability.name == chosenAbilityName) {
                                val newName = if (!ability.name.endsWith("+")) "${ability.name}+" else ability.name
                                val cleanDesc = ability.description.substringBefore("\n[Potenziata]")

                                ability.copy(
                                    name = newName,
                                    description = currentState.customAbilityDescription.toString()
                                )
                            } else {
                                ability
                            }
                        }
                        updatedChar.copy(
                            abilitiesList = updatedAbilities,
                            classAbilitiesList = updatedChar.classAbilitiesList + "UPGRADE_ABILITY"
                        )
                    } else {
                        val currentAbilities = updatedChar.classAbilitiesList.toMutableList()
                        currentAbilities.add("UPGRADE_ABILITY")
                        chosenAbilityName?.let { currentAbilities.add(it) }
                        updatedChar.copy(classAbilitiesList = currentAbilities)
                    }
                }
                LevelUpOption.BASE_CLASS_ABILITY,
                LevelUpOption.ADVANCED_CLASS_ABILITY -> {
                    val currentAbilities = updatedChar.classAbilitiesList.toMutableList()
                    currentState.selectedAbilityToUpgrade?.let { currentAbilities.add(it) }
                    updatedChar.copy(classAbilitiesList = currentAbilities)
                }
                LevelUpOption.NEW_CLASS_BASE_ABILITY -> {
                    val currentAbilities = updatedChar.classAbilitiesList.toMutableList()
                    currentAbilities.add("TAKEN_NEW_CLASS_BASE_ABILITIES")
                    currentState.selectedAbilityToUpgrade?.let { currentAbilities.add(it) }
                    updatedChar.copy(classAbilitiesList = currentAbilities)
                }
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
        },

        onNextStep = {
            _state.update { currentState ->
                if (currentState == null || !currentState.isCurrentStepValid) return@update currentState

                val nextStep = when (currentState.currentStep) {
                    LevelUpStep.CHOOSE_CLASS -> LevelUpStep.CHOOSE_PERK_TYPE
                    LevelUpStep.CHOOSE_PERK_TYPE -> {
                        when (currentState.selectedOption) {
                            LevelUpOption.STAT_BONUS_2 -> LevelUpStep.EDIT_STATISTICS
                            LevelUpOption.UPGRADE_ABILITY,
                            LevelUpOption.BASE_CLASS_ABILITY,
                            LevelUpOption.ADVANCED_CLASS_ABILITY,
                            LevelUpOption.NEW_CLASS_BASE_ABILITY -> LevelUpStep.EDIT_ABILITIES
                            else -> currentState.currentStep
                        }
                    }
                    else -> currentState.currentStep
                }
                currentState.copy(currentStep = nextStep)
            }
        },

        onBackStep = { onNavigateBack ->
            _state.update { currentState ->
                if (currentState == null) return@update currentState

                when (currentState.currentStep) {
                    LevelUpStep.CHOOSE_CLASS -> {
                        onNavigateBack()
                        currentState
                    }
                    LevelUpStep.CHOOSE_PERK_TYPE -> {
                        if (currentState.character?.level == 0 || currentState.character?.level == 6) {
                            currentState.copy(currentStep = LevelUpStep.CHOOSE_CLASS)
                        } else {
                            onNavigateBack()
                            currentState
                        }
                    }
                    LevelUpStep.EDIT_STATISTICS, LevelUpStep.EDIT_ABILITIES -> {
                        currentState.copy(currentStep = LevelUpStep.CHOOSE_PERK_TYPE)
                    }
                }
            }
        }
    )
}

