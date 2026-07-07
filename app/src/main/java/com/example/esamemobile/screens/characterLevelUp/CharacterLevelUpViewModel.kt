package com.example.esamemobile.screens.characterLevelUp

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val currentLevel: Int = 1,
    val strengthModifier: Int = 0,
    val hpRolled: Int = 0,
    val isHpRolled: Boolean = false,

    val selectedOption: LevelUpOption? = null,

    val hasTakenCharPlus3Beforeval : Boolean = false,
    val hasAllBaseAbilitiesOfAClass: Boolean = false,

    val selectedStatToUpgrade: String? = null,
    val selectedAbilityToUpgrade: String? = null,

    val isLevelUpComplete: Boolean = false
)

data class LevelUpActions(
    val onRollHp: (strengthModifier: Int) -> Unit,
    val onSelectedOption: (LevelUpOption) -> Unit,
    val onSelectStatToUpgrade: (String) -> Unit,
    val onSelectAbilityToUpgrade: (String) -> Unit,
    val onConfirmLevelUp: (Context, () -> Unit) -> Unit
)

class LevelUpViewModel : ViewModel() {
    private val _state = MutableStateFlow<LevelUpState?>(null)
    val state = _state.asStateFlow()

    fun initLevelUp(
        currentLevel: Int,
        strengthModifier: Int,
        hasChar3: Boolean,
        hasAllBase: Boolean
    ) {
        if (_state.value != null) return

        _state.value = LevelUpState(
            currentLevel = currentLevel,
            strengthModifier = strengthModifier,
            hasTakenCharPlus3Beforeval = hasChar3,
            hasAllBaseAbilitiesOfAClass = hasAllBase
        )
    }

    val actions = LevelUpActions(
        onRollHp = {
            _state.update { currentState ->
                if (currentState == null || currentState.isHpRolled) return@update currentState
                val diceRoll = (1..6).random()
                val finalHpGained = maxOf(1, diceRoll + currentState.strengthModifier)
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

        onConfirmLevelUp = { context, navigateBack ->
            val currentState = _state.value ?: return@LevelUpActions

            if (!currentState.isHpRolled) {
                Toast.makeText(context, "Tira il dado per gli HP", Toast.LENGTH_SHORT).show()
                return@LevelUpActions
            }
            if (currentState.selectedOption == null) {
                Toast.makeText(context, "Seleziona una ricompensa", Toast.LENGTH_SHORT).show()
                return@LevelUpActions
            }
        }
    )
}

