package com.example.esamemobile.screens.CharacterCreation

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.esamemobile.utilities.DisplayableItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

enum class CreationStep { STATISTICS, ABILITIES, INVENTORY }

data class CharacterCreationState(
    val currentStep: CreationStep = CreationStep.STATISTICS,
    val peLeft: Int = 10,
    val maxWeightCapacity: Int = 1,
    val abilitiesList: List<AbilityItem> = emptyList(),
    val inventoryList: List<InventoryItem> = emptyList(),
    val showAbilityDialog: Boolean = false,
    val showItemDialog: Boolean = false,

    val name: String = "",
    val age: String = "",
    val isSpendingPEMode: Boolean = false,

    val strength: Int = 1,
    val agility: Int = 1,
    val intelligence: Int = 1,
    val charisma: Int = 1,
    val power: Int = 1,

    val baseStrength: Int = 1,
    val baseAgility: Int = 1,
    val baseIntelligence: Int = 1,
    val baseCharisma: Int = 1,
    val basePower: Int = 1,

    val peSpentHP: Int = 0,
    val hpBase: Int = 0
) {
    val strengthModifier: Int get() = calculateModifier(strength)
    val agilityModifier: Int get() = calculateModifier(agility)
    val intelligenceModifier: Int get() = calculateModifier(intelligence)
    val charismaModifier: Int get() = calculateModifier(charisma)
    val powerModifier: Int get() = calculateModifier(power)

    val hpMax: Int get() = hpBase + strengthModifier + peSpentHP
    val speed: Double get() = 6 + (1.5 * agilityModifier)
    val inventoryCapacity: Int get() = maxOf(1, strengthModifier + 1)
    val baseDamage: String get() = calculateBaseDamage(power)
}

data class CharacterCreationActions(
    val onNextStep: (Context, () -> Unit) -> Unit,
    val onPreviousStep: () -> Unit,
    val onSetAbilityDialogVisible: (Boolean) -> Unit,
    val onSetItemDialogVisible: (Boolean) -> Unit,

    val onNameChange: (String) -> Unit,
    val onAgeChange: (String, Context) -> Unit,
    val onRollAge: (Context) -> Unit,
    val onTogglePEMode: (Boolean) -> Unit,
    val onStatManualChange: (String, Int) -> Unit,
    val onStatPointBuy: (String, Boolean) -> Unit,
    val onRollHpBase: () -> Unit,
    val onModifyHpPe: (Boolean) -> Unit,

    val onAddAbility: (String, String, Int, Context) -> Unit,
    val onEditAbility: (String, String, String, Int, Context) -> Unit,
    val onDeleteAbility: (AbilityItem, Context) -> Unit,

    val onAddItem: (String, String, Int, Context) -> Unit,
    val onEditItem: (String, String, String, Int, Context) -> Unit,
    val onDeleteItem: (InventoryItem, Context) -> Unit,
)

class CharacterCreationViewModel: ViewModel() {
    private val _state = MutableStateFlow(CharacterCreationState())
    val state = _state.asStateFlow()

    val actions = CharacterCreationActions(
        onNextStep = { context, onCreationComplete ->
            _state.update { currentState ->
                when (currentState.currentStep) {
                    CreationStep.STATISTICS -> currentState.copy(currentStep = CreationStep.ABILITIES)
                    CreationStep.ABILITIES -> currentState.copy(currentStep = CreationStep.INVENTORY)
                    CreationStep.INVENTORY -> {
                        Toast.makeText(context, "Personaggio Creato!", Toast.LENGTH_SHORT).show()
                        onCreationComplete()
                        currentState
                    }
                }
            }
        },

        onPreviousStep = {
            _state.update { currentState ->
                when (currentState.currentStep) {
                    CreationStep.ABILITIES -> currentState.copy(currentStep = CreationStep.STATISTICS)
                    CreationStep.INVENTORY -> currentState.copy(currentStep = CreationStep.ABILITIES)
                    else -> currentState
                }
            }
        },

        onSetAbilityDialogVisible = { visible ->
            _state.update { it.copy(showAbilityDialog = visible) }
        },

        onSetItemDialogVisible = { visible ->
            _state.update { it.copy(showItemDialog = visible) }
        },

        onNameChange = { newName ->
            _state.update { it.copy(name = newName) }
        },

        onAgeChange = { newAge, context ->
            val parsedAge = newAge.toIntOrNull() ?: 0
            if (parsedAge > 70) {
                Toast.makeText(
                    context,
                    "Età superiore a 70, TODO: Malus Casuale",
                    Toast.LENGTH_SHORT
                ).show()
            }
            _state.update { it.copy(age = newAge) }
        },

        onRollAge = { context ->
            val randomAge = (1..100).random()
            if (randomAge > 70) {
                Toast.makeText(
                    context,
                    "Età superiore a 70, TODO: Malus Casuale",
                    Toast.LENGTH_SHORT
                ).show()
            }
            _state.update { it.copy(age = randomAge.toString()) }
        },

        onTogglePEMode = { enabled ->
            _state.update { currentState ->
                if (enabled) {
                    currentState.copy(
                        isSpendingPEMode = true,
                        baseStrength = currentState.strength,
                        baseAgility = currentState.agility,
                        baseIntelligence = currentState.intelligence,
                        baseCharisma = currentState.charisma,
                        basePower = currentState.power
                    )
                } else {
                    currentState.copy(
                        isSpendingPEMode = false,
                        strength = currentState.baseStrength,
                        agility = currentState.baseAgility,
                        intelligence = currentState.baseIntelligence,
                        charisma = currentState.baseCharisma,
                        power = currentState.basePower,
                        peLeft = 10 - currentState.peSpentHP
                    )
                }
            }
        },

        onStatManualChange = { statLabel, value ->
            if (value in 1..10) {
                _state.update { currentState ->
                    val newState = when (statLabel) {
                        "Forza" -> currentState.copy(strength = value)
                        "Agilità" -> currentState.copy(agility = value)
                        "Intelligenza" -> currentState.copy(intelligence = value)
                        "Carisma" -> currentState.copy(charisma = value)
                        "Potere" -> currentState.copy(power = value)
                        else -> currentState
                    }
                    newState.copy(maxWeightCapacity = newState.inventoryCapacity)
                }
            }
        },

        onStatPointBuy = { statLabel, increment ->
            _state.update { currentState ->
                if (!currentState.isSpendingPEMode) return@update currentState

                val currentVal = when (statLabel) {
                    "Forza" -> currentState.strength
                    "Agilità" -> currentState.agility
                    "Intelligenza" -> currentState.intelligence
                    "Carisma" -> currentState.charisma
                    "Potere" -> currentState.power
                    else -> 1
                }

                val baseVal = when (statLabel) {
                    "Forza" -> currentState.baseStrength
                    "Agilità" -> currentState.baseAgility
                    "Intelligenza" -> currentState.baseIntelligence
                    "Carisma" -> currentState.baseCharisma
                    "Potere" -> currentState.basePower
                    else -> 1
                }

                if (increment) {
                    if (currentState.peLeft >= 2 && currentVal < 10) {
                        val newState = when (statLabel) {
                            "Forza" -> currentState.copy(
                                strength = currentVal + 1,
                                peLeft = currentState.peLeft - 2
                            )

                            "Agilità" -> currentState.copy(
                                agility = currentVal + 1,
                                peLeft = currentState.peLeft - 2
                            )

                            "Intelligenza" -> currentState.copy(
                                intelligence = currentVal + 1,
                                peLeft = currentState.peLeft - 2
                            )

                            "Carisma" -> currentState.copy(
                                charisma = currentVal + 1,
                                peLeft = currentState.peLeft - 2
                            )

                            "Potere" -> currentState.copy(
                                power = currentVal + 1,
                                peLeft = currentState.peLeft - 2
                            )

                            else -> currentState
                        }
                        newState.copy(maxWeightCapacity = newState.inventoryCapacity)
                    } else currentState
                } else {
                    if (currentVal > baseVal) {
                        val newState = when (statLabel) {
                            "Forza" -> currentState.copy(
                                strength = currentVal - 1,
                                peLeft = currentState.peLeft + 2
                            )

                            "Agilità" -> currentState.copy(
                                agility = currentVal - 1,
                                peLeft = currentState.peLeft + 2
                            )

                            "Intelligenza" -> currentState.copy(
                                intelligence = currentVal - 1,
                                peLeft = currentState.peLeft + 2
                            )

                            "Carisma" -> currentState.copy(
                                charisma = currentVal - 1,
                                peLeft = currentState.peLeft + 2
                            )

                            "Potere" -> currentState.copy(
                                power = currentVal - 1,
                                peLeft = currentState.peLeft + 2
                            )

                            else -> currentState
                        }
                        newState.copy(maxWeightCapacity = newState.inventoryCapacity)
                    } else currentState
                }
            }
        },

        onAddAbility = { name, desc, cost, context ->
            _state.update { currentState ->
                if (cost <= currentState.peLeft) {
                    currentState.copy(
                        peLeft = currentState.peLeft - cost,
                        abilitiesList = currentState.abilitiesList + AbilityItem(
                            name = name,
                            description = desc,
                            numericValue = cost
                        )
                    )
                } else {
                    Toast.makeText(context, "PE insufficienti!", Toast.LENGTH_SHORT).show()
                    currentState
                }
            }
        },

        onEditAbility = { id, name, desc, newCost, context ->
            _state.update { currentState ->
                val old = currentState.abilitiesList.find { it.id == id }
                if (old != null) {
                    val currentPool = currentState.peLeft + old.numericValue
                    if (newCost <= currentPool) {
                        val updatedList = currentState.abilitiesList.map {
                            if (it.id == id) AbilityItem(id, name, desc, newCost) else it
                        }
                        currentState.copy(
                            peLeft = currentPool - newCost,
                            abilitiesList = updatedList
                        )
                    } else {
                        Toast.makeText(context, "PE insufficienti!", Toast.LENGTH_SHORT).show()
                        currentState
                    }
                } else currentState
            }
        },

        onDeleteAbility = { item, context ->
            _state.update { currentState ->
                Toast.makeText(context, "Abilità rimossa", Toast.LENGTH_SHORT).show()
                currentState.copy(
                    peLeft = currentState.peLeft + item.numericValue,
                    abilitiesList = currentState.abilitiesList - item
                )
            }
        },

        onAddItem = { name, desc, weight, context ->
            _state.update { currentState ->
                val currentWeight = currentState.inventoryList.sumOf { it.numericValue }
                if (currentWeight + weight <= currentState.maxWeightCapacity) {
                    currentState.copy(
                        inventoryList = currentState.inventoryList + InventoryItem(
                            name = name,
                            description = desc,
                            numericValue = weight
                        )
                    )
                } else {
                    Toast.makeText(context, "Capacità di carico superata!", Toast.LENGTH_SHORT)
                        .show()
                    currentState
                }
            }
        },

        onEditItem = { id, name, desc, newWeight, context ->
            _state.update { currentState ->
                val old = currentState.inventoryList.find { it.id == id }
                if (old != null) {
                    val weightWithoutOld =
                        currentState.inventoryList.sumOf { it.numericValue } - old.numericValue
                    if (weightWithoutOld + newWeight <= currentState.maxWeightCapacity) {
                        val updatedList = currentState.inventoryList.map {
                            if (it.id == id) InventoryItem(id, name, desc, newWeight) else it
                        }
                        currentState.copy(inventoryList = updatedList)
                    } else {
                        Toast.makeText(
                            context,
                            "Supereresti il carico trasportabile!",
                            Toast.LENGTH_SHORT
                        ).show()
                        currentState
                    }
                } else currentState
            }
        },

        onDeleteItem = { item, context ->
            _state.update { currentState ->
                Toast.makeText(context, "Oggetto rimosso", Toast.LENGTH_SHORT).show()
                currentState.copy(inventoryList = currentState.inventoryList - item)
            }
        },

        onRollHpBase = {
            _state.update {
                it.copy(hpBase = (1..6).random() + (1..6).random())
            }
        },

        onModifyHpPe = { incrememnt ->
            _state.update { currentState ->
                if (incrememnt && currentState.peLeft > 0) {
                    currentState.copy(peSpentHP = currentState.peSpentHP + 1, peLeft = currentState.peLeft - 1)
                } else if (!incrememnt && currentState.peSpentHP > 0) {
                    currentState.copy(peSpentHP = currentState.peSpentHP - 1, peLeft = currentState.peLeft + 1)
                } else currentState
            }
        }
    )
}

data class LocalEditableStats(
    val label: String,
    val value: Int,
    val baseValue: Int
)

fun calculateBaseDamage(power: Int): String {
    return when (power) {
        in Int.MIN_VALUE..1 -> "1d2"
        in 2..3 -> "1d4"
        in 4..5 -> "1d6"
        in 6..7 -> "1d8"
        in 8..9 -> "1d10"
        else -> "1d12"
    }
}

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

data class AbilityItem(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String,
    override val description: String,
    override val numericValue: Int
) : DisplayableItem

data class InventoryItem(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String,
    override val description: String,
    override val numericValue: Int = 1
) : DisplayableItem