package com.example.esamemobile.screens.characterCreation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.ArmorTypes
import com.example.esamemobile.data.repositories.StaticDataRepository
import com.example.esamemobile.data.staticData.AgeMalus
import com.example.esamemobile.utilities.DisplayableItem
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.calculateModifier
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.repositories.CharacterRepository
import com.example.esamemobile.data.supabase.ImagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.core.net.toUri
import com.example.esamemobile.data.repositories.FileRepository

enum class CreationStep { STATISTICS, ABILITIES, INVENTORY }

data class CharacterCreationState(
    val currentStep: CreationStep = CreationStep.STATISTICS,
    val peLeft: Int = 10,
    val maxWeightCapacity: Int = 1,
    val abilitiesList: List<AbilityItem> = emptyList(),
    val inventoryList: List<InventoryItem> = emptyList(),
    val showAbilityDialog: Boolean = false,
    val showItemDialog: Boolean = false,
    val showAgeMalusDialog: Boolean = false,
    val showAvatarOptionDialog: Boolean = false,
    val avatarUri: String? = null,

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
    val hpBase: Int = 0,

    val ageMalusDescription: AgeMalus? = null,
    val ageMalusId: Int? = null,

    val message: String? = null
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

    val isNextStepEnabled: Boolean get() = when (currentStep) {
        CreationStep.STATISTICS -> hpBase > 0
        else -> true
    }
}

data class CharacterCreationActions(
    val onNextStep: ((Character) -> Unit) -> Unit,
    val onPreviousStep: (() -> Unit) -> Unit,
    val onSetAbilityDialogVisible: (Boolean) -> Unit,
    val onSetItemDialogVisible: (Boolean) -> Unit,
    val onSetAgeMalusDialogVisible: (Boolean) -> Unit,

    val onNameChange: (String) -> Unit,
    val onAgeChange: (String) -> Unit,
    val onRollAge: () -> Unit,
    val onTogglePEMode: (Boolean) -> Unit,
    val onStatManualChange: (String, Int) -> Unit,
    val onStatPointBuy: (String, Boolean) -> Unit,
    val onRollHpBase: () -> Unit,
    val onModifyHpPe: (Boolean) -> Unit,
    val onRollAllStats: () -> Unit,
    val onSetAvatarOptionDialogVisible: (Boolean) -> Unit,
    val onAvatarSelected: (String?) -> Unit,

    val onAddAbility: (String, String, Int) -> Boolean,
    val onEditAbility: (String, String, String, Int) -> Boolean,
    val onDeleteAbility: (AbilityItem) -> Unit,

    val onAddItem: (String, String, Int) -> Boolean,
    val onEditItem: (String, String, String, Int) -> Boolean,
    val onDeleteItem: (InventoryItem) -> Unit,

    val onMessageShown: () -> Unit
)

class CharacterCreationViewModel(
    private val staticDataRepository: StaticDataRepository,
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository,
    private val imagesRepository: ImagesRepository,
    private val fileRepository: FileRepository
): ViewModel() {
    private val _state = MutableStateFlow(CharacterCreationState())
    val state = _state.asStateFlow()

    val malusList = staticDataRepository.allAgeMalus

    private fun getMalusForAge(ageString: String): AgeMalus? {
        val parsedAge = ageString.toIntOrNull() ?: 0
        return if (parsedAge > 70) malusList.random() else null
    }

    private fun getMalusDrawableId(malus: AgeMalus?): Int? {
        return malus?.drawableId?.let { staticDataRepository.getDrawableId(it) }
    }

    val actions = CharacterCreationActions(
        onNextStep = { onCreationComplete ->
            _state.update { currentState ->
                when (currentState.currentStep) {
                    CreationStep.STATISTICS -> {
                        if (currentState.hpBase <= 0) {
                            newMsg("Non puoi proseguire senza tirare prima per determinare i tuoi HP.")
                            currentState
                        } else {
                            currentState.copy(currentStep = CreationStep.ABILITIES)
                        }
                    }

                    CreationStep.ABILITIES -> currentState.copy(currentStep = CreationStep.INVENTORY)
                    CreationStep.INVENTORY -> {
                        viewModelScope.launch {
                            val id = UUID.randomUUID().toString()
                            var url = ""
                            if (currentState.avatarUri != null) {
                                val bytes = fileRepository.readBytes(currentState.avatarUri.toUri())

                                bytes?.let {
                                    val result = imagesRepository.uploadImage(it, id, "characters")
                                    result.fold(
                                        onSuccess = { res -> url = res },
                                        onFailure = { exception ->
                                            newMsg("Si è verificato un errore durante il salvataggio dell'immagine")
                                            Log.w(
                                                "debug",
                                                "Errore salvataggio supabase ${exception.message}"
                                            )
                                        }
                                    )
                                }
                            }
                            val newCharacter = currentState.toCharacter(id, url)
                            val result = characterRepository.insertNewCharacter(
                                authRepository.currentUser?.uid,
                                newCharacter
                            )
                            result.fold(
                                onSuccess = {
                                    newMsg("Personaggio Creato!")
                                    onCreationComplete(newCharacter)
                                },
                                onFailure = { exception ->
                                    newMsg("Si è verificato un errore durante la creazione del personaggio")
                                    Log.w("debug", "Errore ${exception.message}")
                                }
                            )
                        }
                        currentState
                    }
                }
            }
        },

        onPreviousStep = { onCancelCreation ->
            _state.update { currentState ->
                when (currentState.currentStep) {
                    CreationStep.STATISTICS -> {
                        onCancelCreation()
                        currentState
                    }

                    CreationStep.ABILITIES -> currentState.copy(currentStep = CreationStep.STATISTICS)
                    CreationStep.INVENTORY -> currentState.copy(currentStep = CreationStep.ABILITIES)
                }
            }
        },

        onSetAbilityDialogVisible = { visible ->
            _state.update { it.copy(showAbilityDialog = visible) }
        },

        onSetItemDialogVisible = { visible ->
            _state.update { it.copy(showItemDialog = visible) }
        },

        onSetAgeMalusDialogVisible = { visible ->
            _state.update { it.copy(showAgeMalusDialog = visible) }
        },

        onSetAvatarOptionDialogVisible = { visible ->
            _state.update { it.copy(showAvatarOptionDialog = visible) }
        },

        onAvatarSelected = { uriString ->
            _state.update { it.copy(avatarUri = uriString) }
        },

        onNameChange = { newName ->
            _state.update { it.copy(name = newName) }
        },

        onAgeChange = { newAge ->
            val malus = getMalusForAge(newAge)
            _state.update {
                it.copy(
                    age = newAge,
                    ageMalusDescription = malus,
                    ageMalusId = getMalusDrawableId(malus)
                )
            }
        },

        onRollAge = {
            val randomAge = (1..100).random()
            val malus = getMalusForAge(randomAge.toString())
            if (randomAge > 70) {
                newMsg("Età superiore a 70 malus età")
            }
            _state.update {
                it.copy(
                    age = randomAge.toString(),
                    ageMalusDescription = malus,
                    ageMalusId = getMalusDrawableId(malus)
                )
            }
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
            if (value in 0..10) {
                _state.update { currentState ->
                    val newState = when (statLabel) {
                        "Forza" -> currentState.copy(strength = value, baseStrength = value)
                        "Agilità" -> currentState.copy(agility = value, baseAgility = value)
                        "Intelligenza" -> currentState.copy(
                            intelligence = value,
                            baseIntelligence = value
                        )

                        "Carisma" -> currentState.copy(charisma = value, baseCharisma = value)
                        "Potere" -> currentState.copy(power = value, basePower = value)
                        else -> currentState
                    }
                    newState.copy(maxWeightCapacity = newState.inventoryCapacity)
                }
            }
        },

        onRollAllStats = {
            _state.update { currentState ->
                val rollStr = (1..6).random()
                val rollAgi = (1..6).random()
                val rollInt = (1..6).random()
                val rollCha = (1..6).random()
                val rollPow = (1..6).random()

                val newState = currentState.copy(
                    strength = rollStr,
                    agility = rollAgi,
                    intelligence = rollInt,
                    charisma = rollCha,
                    power = rollPow,

                    baseStrength = rollStr,
                    baseAgility = rollAgi,
                    baseIntelligence = rollInt,
                    baseCharisma = rollCha,
                    basePower = rollPow
                )

                newState.copy(maxWeightCapacity = newState.inventoryCapacity)
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

        onAddAbility = { name, desc, cost ->
            var success = false
            _state.update { currentState ->
                if (name.trim().endsWith("+")) {
                    newMsg("Il nome dell'abilità non può terminare con \"+\"")
                    return@update currentState.copy(showAbilityDialog = true)
                }

                if (currentState.abilitiesList.any {
                        it.name.equals(
                            name.trim(),
                            ignoreCase = true
                        )
                    }) {
                    newMsg("Esiste già un'abilità con questo nome!")
                    return@update currentState.copy(showAbilityDialog = true)
                }

                if (cost <= currentState.peLeft) {
                    success = true
                    currentState.copy(
                        peLeft = currentState.peLeft - cost,
                        abilitiesList = currentState.abilitiesList + AbilityItem(
                            name = name,
                            description = desc,
                            numericValue = cost
                        )
                    )
                } else {
                    newMsg("PE insufficienti!")
                    currentState
                }
            }
            success
        },

        onEditAbility = { id, name, desc, newCost ->
            var success = false
            _state.update { currentState ->
                if (name.trim().endsWith("+")) {
                    newMsg("Il nome dell'abilità non può terminare con \"+\"")
                    return@update currentState.copy(showAbilityDialog = true)
                }

                if (currentState.abilitiesList.any {
                        it.id != id && it.name.equals(
                            name.trim(),
                            ignoreCase = true
                        )
                    }) {
                    newMsg("Esiste già un'abilità con questo nome!")
                    return@update currentState.copy(showAbilityDialog = true)
                }

                val old = currentState.abilitiesList.find { it.id == id }
                if (old != null) {
                    val currentPool = currentState.peLeft + old.numericValue
                    if (newCost <= currentPool) {
                        success = true
                        val updatedList = currentState.abilitiesList.map {
                            if (it.id == id) AbilityItem(id, name, desc, newCost) else it
                        }
                        currentState.copy(
                            peLeft = currentPool - newCost,
                            abilitiesList = updatedList
                        )
                    } else {
                        newMsg("PE insufficienti!")
                        currentState
                    }
                } else currentState
            }
            success
        },

        onDeleteAbility = { item ->
            _state.update { currentState ->
                newMsg("Abilità rimossa")
                currentState.copy(
                    peLeft = currentState.peLeft + item.numericValue,
                    abilitiesList = currentState.abilitiesList - item
                )
            }
        },

        onAddItem = { name, desc, weight ->
            val safeWeight = if (weight < 0) 0 else weight
            var success = false

            _state.update { currentState ->
                val currentWeight = currentState.inventoryList.sumOf { it.numericValue }
                if (currentWeight + safeWeight <= currentState.maxWeightCapacity) {
                    success = true
                    currentState.copy(
                        inventoryList = currentState.inventoryList + InventoryItem(
                            name = name,
                            description = desc,
                            numericValue = safeWeight
                        )
                    )
                } else {
                    newMsg("Capacità di carico superata!")
                    currentState
                }
            }
            success
        },

        onEditItem = { id, name, desc, newWeight ->
            val safeNewWeight = if (newWeight < 0) 0 else newWeight
            var success = false
            _state.update { currentState ->
                val old = currentState.inventoryList.find { it.id == id }
                if (old != null) {
                    val weightWithoutOld =
                        currentState.inventoryList.sumOf { it.numericValue } - old.numericValue
                    if (weightWithoutOld + safeNewWeight <= currentState.maxWeightCapacity) {
                        success = true
                        val updatedList = currentState.inventoryList.map {
                            if (it.id == id) InventoryItem(id, name, desc, safeNewWeight) else it
                        }
                        currentState.copy(inventoryList = updatedList)
                    } else {
                        newMsg("Supereresti il carico trasportabile!")
                        currentState
                    }
                } else currentState
            }
            success
        },

        onDeleteItem = { item ->
            _state.update { currentState ->
                newMsg("Oggetto rimosso")
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
                    currentState.copy(
                        peSpentHP = currentState.peSpentHP + 1,
                        peLeft = currentState.peLeft - 1
                    )
                } else if (!incrememnt && currentState.peSpentHP > 0) {
                    currentState.copy(
                        peSpentHP = currentState.peSpentHP - 1,
                        peLeft = currentState.peLeft + 1
                    )
                } else currentState
            }
        },
        onMessageShown = { _state.update { it.copy(message = null) } }
    )

    private fun newMsg(msg: String) = _state.update { it.copy(message = msg) }
}

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



fun CharacterCreationState.toCharacter(id: String, url: String): Character {
    return Character(
        id = id,
        name = this.name.ifBlank { "Soggetto Ignoto" },
        imageUrl = url,
        level = 0,
        age = this.age.toIntOrNull() ?: 0,
        ageMalus = this.ageMalusDescription?.id,
        chosenClass = null,
        peAvailable = this.peLeft,
        strength = this.strength,
        agility = this.agility,
        intelligence = this.intelligence,
        charisma = this.charisma,
        power = this.power,
        currentHP = this.hpMax,
        maxHP = this.hpMax,
        abilitiesList = this.abilitiesList,
        classAbilitiesList = emptyList(),
        inventoryList = this.inventoryList,
        speed = this.speed,
        maxCapacity = this.maxWeightCapacity,
        armor = ArmorTypes.NONE
    )
}

@Serializable
data class AbilityItem(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "",
    override val description: String = "",
    override val numericValue: Int = 0
) : DisplayableItem

@Serializable
data class InventoryItem(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "",
    override val description: String = "",
    override val numericValue: Int = 1
) : DisplayableItem
