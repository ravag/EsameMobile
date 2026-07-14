//TODO: Le abilità di classe di base vanno divise da quelle avanzate e solo in quelle avanzate va messo il contatore di usi
//TODO: Le abilità di altre classi non vengono mostrate, vengono solo mostrate le abilità della mia classe principale
//TODO: Mettere un modo per modificare il personaggio (Armatura(quindi ancher gli effetti delle armature)) cardclickable che fa vedere le opzioni e le descrizioni
//TODO: Da inserire un modo per segnare gli hp temporanei (come in dnd funzionano)

package com.example.esamemobile.screens.characterDetails

import android.util.Log
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.ArmorTypes
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.UiCharacter
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.repositories.CharacterRepository
import com.example.esamemobile.data.repositories.CharacterSolver
import com.example.esamemobile.data.repositories.FileRepository
import com.example.esamemobile.data.repositories.StaticDataRepository
import com.example.esamemobile.data.supabase.ImagesRepository
import com.example.esamemobile.screens.characterCreation.AbilityItem
import com.example.esamemobile.screens.characterCreation.InventoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CharacterDetailsTab {
    STATS,POWERS,INVENTORY
}

data class CharacterDetailsState(
    val character: UiCharacter? = null,
    val selectedTab: CharacterDetailsTab = CharacterDetailsTab.STATS,
    val abilityUsageCurrent: Int = 2,
    val abilityUsageMax: Int = 2,
    val malusDrawableId: Int? = null,
    val ageMalusDialog: Boolean = false,
    val isLoading: Boolean = true,
    val showAddPowerDialog: Boolean = false,
    val showAddItemDialog: Boolean = false,
    val showArmorDialog: Boolean = false,
    val tempName: String = "",
    val tempDesc: String = "",
    val tempValue: Int = 0,
    val isOwner: Boolean = false,
    val message: String? = null
)

data class CharacterDetailsActions(
    val onTabSelected: (Int) -> Unit,
    val onLevelUp: (() -> Unit)? = null,
    val onMalusButton: () -> Unit,
    val onDecreaseHp: (() -> Unit)? = null,
    val onIncreaseHp: (() -> Unit)? = null,

    val onOpenAddPowerDialog: (() -> Unit)? = null,
    val onOpenAddItemDialog: (() -> Unit)? = null,
    val onOpenArmorDialog: (() -> Unit)? = null,
    val onCloseArmorDialog: () -> Unit = {},
    val onChangeArmor: ((ArmorTypes) -> Unit)? = null,
    val onCloseDialogs: () -> Unit,
    val onTempDataChanged: (String, String, Int) -> Unit,
    val onConfirmAddPower: (() -> Unit)? = null,
    val onConfirmAddItem: (() -> Unit)? = null,

    val onDecreaseUsage: (() -> Unit)? = null,
    val onIncreaseUsage: (() -> Unit)? = null,
    val onUseItem: ((InventoryItem) -> Unit)? = null,
    val onScreenExit: () -> Unit,
    val onLoad: () -> Unit,
    val onDelete: (() -> Unit)? = null,

    val onChangeImage: (String) -> Unit,
    val onMessageShown: () -> Unit
)

class CharacterDetailsViewModel (
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository,
    private val characterSolver: CharacterSolver,
    private val staticDataRepository: StaticDataRepository,
    private val imagesRepository: ImagesRepository,
    private val fileRepository: FileRepository
) : ViewModel() {

    var editable: Boolean = false
    var hasChanged: Boolean = false
    val charId = MutableStateFlow<String?>(null)
    //Potrei vedere un personaggio non mio, ho bisogno di tenermi userId per recuperare il personaggio
    private var ownerId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(CharacterDetailsState())
    val state = _state.asStateFlow()

    private val saveScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun setIds(charId: String, userId: String?) {
        this.charId.value = charId
        this.ownerId.value = userId ?: authRepository.currentUser?.uid
    }

    val actions: CharacterDetailsActions
        get() = CharacterDetailsActions(
            onTabSelected = { index ->
                _state.update { it.copy(selectedTab = CharacterDetailsTab.entries[index]) }
            },

            onLevelUp = if (editable && (_state.value.character?.character?.level ?: 0) < 11) {
                {
                    saveCharacter(viewModelScope)
                    _state.update { it.copy(isLoading = true) }
                }
            } else null,

            onMalusButton = { _state.update { it.copy(ageMalusDialog = !_state.value.ageMalusDialog) } },

            onDecreaseHp = if (editable) {
                {
                    updateCharacter { it.copy(currentHP = (it.currentHP - 1).coerceAtLeast(0)) }
                }
            } else null,

            onIncreaseHp = if (editable) {
                {
                    updateCharacter { it.copy(currentHP = (it.currentHP + 1).coerceAtMost(it.maxHP)) }
                }
            } else null,

            onOpenAddPowerDialog = if (editable) {
                {
                    _state.update {
                        it.copy(
                            showAddPowerDialog = true,
                            tempName = "",
                            tempDesc = "",
                            tempValue = 1
                        )
                    }
                }
            } else null,

            onOpenAddItemDialog = if (editable) {
                {
                    _state.update {
                        it.copy(
                            showAddItemDialog = true,
                            tempName = "",
                            tempDesc = "",
                            tempValue = 1
                        )
                    }
                }
            } else null,

            onOpenArmorDialog = if (editable) {
                { _state.update { it.copy(showArmorDialog = true) } }
            } else null,

            onCloseArmorDialog = {
                _state.update { it.copy(showArmorDialog = false) }
            },

            onChangeArmor = if (editable) {
                { newArmor ->
                    updateCharacter { it.copy(armor = newArmor) }
                    _state.update { it.copy(showArmorDialog = false) }
                }
            } else null,

            onCloseDialogs = {
                _state.update {
                    it.copy(
                        showAddPowerDialog = false,
                        showAddItemDialog = false,
                        tempName = "",
                        tempDesc = "",
                        tempValue = 0
                    )
                }
            },

            onTempDataChanged = { name, desc, value ->
                _state.update { it.copy(tempName = name, tempDesc = desc, tempValue = value) }
            },

            onConfirmAddPower = if (editable) {
                {
                    val name = _state.value.tempName
                    val desc = _state.value.tempDesc
                    val value = _state.value.tempValue

                    val currentCharacter = _state.value.character?.character

                    if (currentCharacter != null && name.isNotBlank()) {

                        val endsWithPlus = name.trim().endsWith("+")
                        val alreadyExists = currentCharacter.abilitiesList.any {
                            it.name.equals(name.trim(), ignoreCase = true)
                        }
                        val costTooHigh = value > currentCharacter.peAvailable

                        if (!endsWithPlus && !alreadyExists && !costTooHigh && value >= 0) {
                            updateCharacter { char ->
                                val newPower = AbilityItem(
                                    name = name.trim(),
                                    description = desc,
                                    numericValue = value
                                )
                                char.copy(
                                    abilitiesList = char.abilitiesList + newPower,
                                    peAvailable = currentCharacter.peAvailable - value
                                )
                            }
                            _state.update { it.copy(showAddPowerDialog = false) }
                        } else {
                            if (endsWithPlus) {
                                newMsg("Il nome dell'abilità non può terminare con il carattere \"+\"")
                            }
                            if (alreadyExists) {
                                newMsg("Esiste già un'abilità con questo nome")
                            }
                            if (costTooHigh) {
                                newMsg("Non possiedi abbastanza PE")
                            }
                        }
                    } else if (name.isBlank()) {
                        newMsg("Devi dare un nome al nuovo potere!")
                    } else {
                        newMsg("E' capitato un errore imprevisto")
                    }
                }
            } else null,

            onConfirmAddItem = if (editable) {
                {
                    val name = _state.value.tempName
                    val desc = _state.value.tempDesc
                    val weight = _state.value.tempValue

                    if (name.isNotBlank()) {
                        var spaceAvailable = true
                        updateCharacter { currentCharacter ->
                            val newItem = InventoryItem(
                                name = name,
                                description = desc,
                                numericValue = weight
                            )
                            val totalWeight =
                                currentCharacter.inventoryList.sumOf { it.numericValue }

                            if (totalWeight + weight <= currentCharacter.maxCapacity) {
                                currentCharacter.copy(inventoryList = currentCharacter.inventoryList + newItem)
                            } else {
                                spaceAvailable = false
                                currentCharacter
                            }
                        }

                        if (spaceAvailable) {
                            _state.update { it.copy(showAddItemDialog = false) }
                        } else {
                            newMsg("Capacità di carico superata!")
                        }
                    } else {
                        newMsg("Dai un nome all'oggetto")
                    }
                }
            } else null,

            onDecreaseUsage = if (editable) {
                {
                    hasChanged = true
                    _state.update {
                        it.copy(
                            abilityUsageCurrent = (it.abilityUsageCurrent - 1).coerceAtLeast(
                                0
                            )
                        )
                    }
                }
            } else null,

            onIncreaseUsage = if (editable) {
                {
                    hasChanged = true
                    _state.update {
                        it.copy(abilityUsageCurrent = (it.abilityUsageCurrent + 1).coerceAtMost(it.abilityUsageMax))
                    }
                }
            } else null,


            onUseItem = if (editable) {
                { item ->
                    updateCharacter { it.copy(inventoryList = it.inventoryList.filter { obj -> obj != item }) }
                }
            } else null,

            onScreenExit = {
                saveCharacter(viewModelScope)
            },

            onLoad = {
                load()
                Log.i("DEBUG", state.value.character?.subClassAbilities.toString())
                     },

            onDelete = if (editable) {
                {
                    val userId = ownerId.value ?: return@CharacterDetailsActions

                    viewModelScope.launch {
                        val result = characterRepository.deleteCharacter(
                            userId,
                            state.value.character?.character!!.id
                        )
                        result.fold(
                            onSuccess = {
                                newMsg("Personaggio eliminato")
                                hasChanged = false
                            },
                            onFailure = { newMsg("Errore nell'eliminazione del personaggio") }
                        )
                    }
                }
            } else null,
            onChangeImage = { uri ->
                if (authRepository.currentUser != null) {
                    viewModelScope.launch {
                        var url = ""
                        val bytes = fileRepository.readBytes(uri.toUri())

                        bytes?.let {
                            val result = imagesRepository.uploadImage(
                                it,
                                authRepository.currentUser!!.uid,
                                "users"
                            )
                            result.fold(
                                onSuccess = { path -> url = path },
                                onFailure = { newMsg("Errore nel salvataggio dell'immagine") }
                            )
                        }
                        updateCharacter { it.copy(imageUrl = url) }
                    }
                }
            },
            onMessageShown = { _state.update { it.copy(message = null) } }
        )

    private fun load() {
        viewModelScope.launch {
            charId.filterNotNull().collectLatest { id ->
                _state.update { it.copy(isLoading = true) }

                val currentUserId = authRepository.currentUser?.uid
                val result = characterRepository.readCharacter(currentUserId,ownerId.value,id)
                result.fold(
                    onSuccess = { char ->
                        val character = char?.let { characterSolver.solve(it) }
                        _state.update { it.copy(
                            character = character,
                            isLoading = false,
                            malusDrawableId = character?.ageMalus?.drawableId?.let { drawId ->
                                staticDataRepository.getDrawableId(drawId) },
                            isOwner = editable
                        )
                        }
                    },
                    onFailure = {
                        newMsg("Errore nel caricamento del personaggio")
                        _state.update { it.copy(isLoading = false) }
                    }
                )
            }
        }
    }

    private fun updateCharacter(transform: (Character) -> Character) {
        val current = _state.value.character?.character ?: return
        val updated = transform(current)
        hasChanged = true

        _state.update { it.copy(character = characterSolver.solve(updated)) }
    }

    private fun saveCharacter(scope: CoroutineScope) {
        val userId = ownerId.value ?: return
        val char = _state.value.character?.character ?: return

        if (hasChanged) {
            scope.launch { characterRepository.updateCharacter(userId,char) }
        }
    }

    private fun newMsg(msg: String) {
        _state.update { it.copy(message = msg) }
    }

    //Per salvataggi in casi di crash o di tornare indietro tramite freccia del telefono e non quella della topBar
    override fun onCleared() {
        super.onCleared()
        saveCharacter(saveScope)
    }
}

