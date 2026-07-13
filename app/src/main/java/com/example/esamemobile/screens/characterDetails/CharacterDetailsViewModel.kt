//TODO: Manca il funzionamento del tasto aggiungi abilità
//TODO: Bisonga mostrare quanti PE disponibili si hanno in caso si volessero aggiungere abilità e poteri o aumentare le stats e quindi mettere anche il modo di modificare le stats, Tutto questo si potrebbe fare richiamando il charactercreationscreen dandogli in pasto un vecchio personaggio così da avere le sue info e poterlo modificare quando si vuole
//TODO: Le abilità di classe di base vanno divise da quelle avanzate e solo in quelle avanzate va messo il contatore di usi
//TODO: Le abilità di altre classi non vengono mostrate oppure più di 3 non ne vengono mostrate
//TODO: Mettere un modo per modificare il personaggio (Nome, Età, Armatura(quindi ancher gli effetti delle armature), poteri evoluzione, oggetti)
//TODO: Da inserire un modo per segnare gli hp temporanei (come in dnd funzionano)
//TODO: Gli hp current/max ogni tanto vanno a capo e non va bene
//TODO: Devi mettere un modo per aumentare le statistiche con i PE spendibili

package com.example.esamemobile.screens.characterDetails

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.UiCharacter
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.repositories.CharacterRepository
import com.example.esamemobile.data.repositories.CharacterSolver
import com.example.esamemobile.data.repositories.StaticDataRepository
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
    val isLoading: Boolean = true
)

data class CharacterDetailsActions(
    val onTabSelected: (Int) -> Unit,
    val onLevelUp: (() -> Unit)?,
    val onMalusButton: () -> Unit,
    val onDecreaseHp: (() -> Unit)?,
    val onIncreaseHp: (() -> Unit)?,
    val onAddPower: ((Context) -> Unit)?,
    val onDecreaseUsage: (() -> Unit)?,
    val onIncreaseUsage: (() -> Unit)?,
    val onAddItem: ((Context) -> Unit)?,
    val onUseItem: ((InventoryItem) -> Unit)?,
    val onScreenExit: () -> Unit,
    val onLoad: () -> Unit,
    val onDelete: (() -> Unit)?
)

class CharacterDetailsViewModel (
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository,
    private val characterSolver: CharacterSolver,
    private val staticDataRepository: StaticDataRepository
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
            onTabSelected = { index -> _state.update { it.copy(selectedTab = CharacterDetailsTab.entries[index]) } },
            onLevelUp =  if (editable) { {
                saveCharacter(viewModelScope)
                _state.update { it.copy(isLoading = true) }
            } } else null,
            onMalusButton = { _state.update { it.copy(ageMalusDialog = !_state.value.ageMalusDialog) } },
            onDecreaseHp = if (editable) { {
                updateCharacter { it.copy(currentHP = (it.currentHP - 1).coerceAtLeast(0)) }
            } } else null,
            onIncreaseHp = if (editable) { {
                updateCharacter { it.copy(currentHP = (it.currentHP + 1).coerceAtMost(it.maxHP)) }
            } } else null,
            onAddPower = if (editable) { { context ->
                hasChanged = true
                Toast.makeText(context, "aggiungi potere", Toast.LENGTH_SHORT).show() }
            } else null,
            onDecreaseUsage = if (editable) {
                {
                    hasChanged = true
                    _state.update { it.copy(abilityUsageCurrent = (it.abilityUsageCurrent-1).coerceAtLeast(0)) }
                }
            } else null,
            onIncreaseUsage = if (editable) { {
                hasChanged = true
                _state.update {
                    it.copy(abilityUsageCurrent = (it.abilityUsageCurrent+1).coerceAtMost(it.abilityUsageMax)) }
                }
            } else null,
            onAddItem = if (editable) { {context ->
                hasChanged = true
                Toast.makeText(context, "aggiungi oggetto", Toast.LENGTH_SHORT).show() }
            } else null,
            onUseItem = if (editable) { { item ->
                updateCharacter { it.copy(inventoryList = it.inventoryList.filter { obj -> obj != item }) }
            } } else null,
            onScreenExit = {
                saveCharacter(viewModelScope)
            },
            onLoad = { load() },
            onDelete = if (editable) { {
                val userId = ownerId.value ?: return@CharacterDetailsActions

                viewModelScope.launch {
                    val result = characterRepository.deleteCharacter(userId,state.value.character?.character!!.id)
                    result.fold(
                        onSuccess = {
                            Log.i("debug","Personaggio eliminato con successo")
                            hasChanged = false
                                },
                        onFailure = { exception -> Log.w("debug","Errore eliminazione ${exception.message}") }
                    )}
            } } else null
        )

    private fun load() {
        viewModelScope.launch {
            charId.filterNotNull().collectLatest { id ->
                _state.update { it.copy(isLoading = true) }

                val currentUserId = authRepository.currentUser?.uid
                val result = characterRepository.readCharacter(currentUserId,ownerId.value,id)
                Log.i("debug","result: $result")
                result.fold(
                    onSuccess = { char ->
                        Log.i("debug", char.toString())
                        val character = char?.let { characterSolver.solve(it) }
                        Log.i("debug","character: ${character.toString()} id: $id")
                        _state.update { it.copy(
                            character = character,
                            isLoading = false,
                            malusDrawableId = character?.ageMalus?.drawableId?.let { drawId ->
                                staticDataRepository.getDrawableId(drawId) }
                        )
                        }
                    },
                    onFailure = { exception ->
                        Log.w("debug","Errorazzo ${exception.message}")
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

    //Per salvataggi in casi di crash o di tornare indietro tramite freccia del telefono e non quella della topBar
    override fun onCleared() {
        super.onCleared()
        saveCharacter(saveScope)
    }
}

