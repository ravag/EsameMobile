package com.example.esamemobile.screens.addCharacter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.CharacterRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddCharacterState(
    val characters: List<Character> = emptyList(),
    val filteredCharacters: List<Character> = emptyList()
)

data class AddCharacterActions(
    val onCharacterSearch: (String) -> Unit,
    val onChooseCharacter: (Character) -> Unit
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
        //Se sono in questa schermata è impossibile che l'utente non sia loggato
        val userId = authRepository.currentUser!!.uid

        viewModelScope.launch {
            characterRepository.getAllUserCharacters(userId)
                .onSuccess { loadedCharacters ->
                    _state.update { it.copy(characters = loadedCharacters, filteredCharacters = loadedCharacters) }
                }
                .onFailure { exception -> Log.w("debug","OOPSIE ${exception.message}")}
        }
    }
    
    val actions = AddCharacterActions(
        onCharacterSearch = { text ->
            _state.update {
                it.copy(filteredCharacters = if (text.isBlank()) it.characters else it.characters.filter { character ->
                    character.name.contains(text, ignoreCase = true)
                }) } },
        onChooseCharacter = { character ->
            viewModelScope.launch {
                groupId.filterNotNull().collectLatest { id ->
                    val currentUser = authRepository.currentUser!!.uid
                    val result = groupRepository.insertMemberCharacter(id,currentUser,character)
                    result.fold(
                        onSuccess = { Log.i("debug","Personaggio scelto con successo") },
                        onFailure = { exception -> Log.w("debug","Errore scelta personaggio ${exception.message}") }
                    )
                }
            }
        }
    )

    fun setId(groupId: String) {
        this.groupId.value = groupId
    }
    
}