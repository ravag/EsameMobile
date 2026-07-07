package com.example.esamemobile.screens.home

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.CharacterRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import com.example.esamemobile.data.firebase.firestore.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomePage {
    CHARACTERS,GROUPS
}

data class HomeState(
    val homePage: HomePage = HomePage.CHARACTERS,
    val characters: List<Character> = emptyList(),
    val groups: List<Group> = emptyList(),
    val filteredCharacters: List<Character> = emptyList(),
    val filteredGroups: List<Group> = emptyList()
    )

data class HomeActions(
    val onPageSelect: (Int) -> Unit,
    val getAllCharacters: () -> Unit,
    val getAllGroups: () -> Unit,
    val onCharactersSearch: (String) -> Unit,
    val onGroupsSearch: (String) -> Unit
)

class HomeViewModel(
    val authRepository: AuthRepository,
    val characterRepository: CharacterRepository,
    val groupRepository: GroupRepository
): ViewModel() {
    private val _state = MutableStateFlow(HomeState())

    val state = _state.asStateFlow()

    val actions = HomeActions(
        onPageSelect = {index -> _state.update { it.copy(homePage = HomePage.entries[index]) }},
        getAllCharacters = {},
        getAllGroups = {},
        onCharactersSearch = { text -> _state.update {
            it.copy(filteredCharacters = if(text.isBlank()) it.characters else it.characters.filter { char -> char.name.contains(text,ignoreCase = true) } ) } },
        onGroupsSearch = { text -> _state.update {
            it.copy(filteredGroups = if(text.isBlank()) it.groups else it.groups.filter { group -> group.name.contains(text,ignoreCase = true) } ) } }
    )

    init {
        load()
    }

    //Funzione che carica tutti i personaggi e i gruppi
    private fun load() {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            characterRepository.getAllUserCharacters(userId)
                .onSuccess { loadedCharacters ->
                    _state.update { it.copy(characters = loadedCharacters, filteredCharacters = loadedCharacters) }
                }
                .onFailure { exception -> Log.w("debug","OOPSIE ${exception.message}")}
            _state.update { it.copy(groups = listOf(
                    Group(id = "1", name = "I Zingari", ""),
                    Group(id = "2", name = "I fantastici 2", ""),
                    Group(id = "3", name = "I tre moschettoni", ""),
                    Group(id = "4", name = "I quattro gatti", ""),
                    Group(id = "5", name = "I cojo(n)ti", ""),
                    Group(id = "6", name = "Giovanni", ""),
                    Group(id = "7", name = "Miku club", ""),
                    Group(id = "8", name = "Il gioco perso", ""),
                    Group(id = "9", name = "Ci piacciono i treni", ""),
                    Group(id = "10", name = "Impottibile!", "")),
                filteredGroups = listOf(
                    Group(id = "1", name = "I Zingari", ""),
                    Group(id = "2", name = "I fantastici 2", ""),
                    Group(id = "3", name = "I tre moschettoni", ""),
                    Group(id = "4", name = "I quattro gatti", ""),
                    Group(id = "5", name = "I cojo(n)ti", ""),
                    Group(id = "6", name = "Giovanni", ""),
                    Group(id = "7", name = "Miku club", ""),
                    Group(id = "8", name = "Il gioco perso", ""),
                    Group(id = "9", name = "Ci piacciono i treni", ""),
                    Group(id = "10", name = "Impottibile!", ""))
                ) }
        }
    }
}