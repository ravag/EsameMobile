package com.example.esamemobile.screens.characterDetails

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.UiCharacter
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.CharacterRepository
import com.example.esamemobile.data.repositories.CharacterSolver
import com.example.esamemobile.data.repositories.StaticDataRepository
import com.example.esamemobile.screens.characterCreation.InventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
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
    val onLevelUp: () -> Unit,   //Da modificare una volta che si ha la schermata di levelUp
    val onMalusButton: () -> Unit,
    val onDecreaseHp: () -> Unit,
    val onIncreaseHp: () -> Unit,
    val onAddPower: (Context) -> Unit,  //Da modificare una volta che si hanno i poteri fatti bene e un idea di cosa dovrebbe fare questo pulsante
    val onDecreaseUsage: () -> Unit,
    val onIncreaseUsage: () -> Unit,
    val onAddItem: (Context) -> Unit,   //Stessa cosa dei poteri, tanto sono entrambi solo nome, descrizione, costo/peso
    val onUseItem: (InventoryItem) -> Unit, //Al momento non utilizzata, bisogna capire come e quando usarla
)

class CharacterDetailsViewModel (
    private val characterRepository: CharacterRepository,
    private val authRepository: AuthRepository,
    private val characterSolver: CharacterSolver,
    private val staticDataRepository: StaticDataRepository
) : ViewModel() {

    val charId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(CharacterDetailsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            charId.filterNotNull().collectLatest { id ->
                _state.update { it.copy(isLoading = true) }

                val result = characterRepository.readCharacter(authRepository.currentUser!!.uid,id)
                result.fold(
                    onSuccess = { char ->
                        val character = char?.let { characterSolver.solve(it) }
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

    val actions = CharacterDetailsActions(
        onTabSelected = { index -> _state.update { it.copy(selectedTab = CharacterDetailsTab.entries[index]) } },
        onLevelUp = { },
        onMalusButton = { _state.update { it.copy(ageMalusDialog = !_state.value.ageMalusDialog) } },
        onDecreaseHp = { updateCharacter { it.copy(currentHP = (it.currentHP - 1).coerceAtLeast(0)) } },
        onIncreaseHp = { updateCharacter { it.copy(currentHP = (it.currentHP + 1).coerceAtMost(it.maxHP)) } },
        onAddPower = { context -> Toast.makeText(context, "aggiungi potere", Toast.LENGTH_SHORT).show() },
        onDecreaseUsage = { _state.update { it.copy(abilityUsageCurrent = (it.abilityUsageCurrent-1).coerceAtLeast(0)) } },
        onIncreaseUsage = { _state.update { it.copy(abilityUsageCurrent = (it.abilityUsageCurrent+1).coerceAtMost(it.abilityUsageMax)) }},
        onAddItem = {context -> Toast.makeText(context, "aggiungi oggetto", Toast.LENGTH_SHORT).show() },
        onUseItem = { item -> updateCharacter { it.copy(inventoryList = it.inventoryList.filter { obj -> obj != item }) } }
    )


    private fun updateCharacter(transform: (Character) -> Character) {
        val current = _state.value.character?.character ?: return
        val updated = transform(current)

        _state.update { it.copy(character = characterSolver.solve(updated)) }

        //Bisorrebbe poi salvare il personaggio, al momento non ho il metodo quindi log di debug per ricordare
        Log.i("debug","Salva modifiche")
    }
}

