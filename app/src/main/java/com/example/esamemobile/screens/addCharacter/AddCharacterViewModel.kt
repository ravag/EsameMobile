package com.example.esamemobile.screens.addCharacter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.repositories.CharacterRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCharacterState(
    val characters: List<Character> = emptyList(),
    val filteredCharacters: List<Character> = emptyList(),
    val message: String? = null
)

data class AddCharacterActions(
    val onCharacterSearch: (String) -> Unit,
    val onChooseCharacter: (Character) -> Unit,
    val onMessageShown: () -> Unit
)

class AddCharacterViewModel(
    val characterRepository: CharacterRepository,
    val authRepository: AuthRepository,
    val groupRepository: GroupRepository
): ViewModel() {

    var groupId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(AddCharacterState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            characterRepository.getAllUserCharacters().collect { loadedCharacters ->
                _state.update { it.copy(characters = loadedCharacters, filteredCharacters = loadedCharacters) }
            }
        }
    }
    
    val actions = AddCharacterActions(
        onCharacterSearch = { text ->
            _state.update {
                it.copy(filteredCharacters = if (text.isBlank()) it.characters else it.characters.filter { character ->
                    character.name.contains(text, ignoreCase = true)
                })
            }
        },
        onChooseCharacter = { character ->
            val id = groupId.value ?: return@AddCharacterActions
            val currentUser = authRepository.currentUser?.uid ?: return@AddCharacterActions

            viewModelScope.launch {
                val result = groupRepository.insertMemberCharacter(id, currentUser, character)
                result.fold(
                    onSuccess = { newMsg("Il personaggio è stato selezionato") },
                    onFailure = { exception ->
                        newMsg("Si è verificato un errore durante la scelta del personaggio")
                        Log.w(
                            "debug",
                            "Errore scelta personaggio ${exception.message}"
                        )
                    }
                )
            }
        },
        onMessageShown = { _state.update { it.copy(message = null) } }
    )

    fun setId(groupId: String) {
        this.groupId.value = groupId
    }

    private fun newMsg(msg: String) = _state.update { it.copy(message = msg) }
}