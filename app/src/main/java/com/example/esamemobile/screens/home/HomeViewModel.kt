package com.example.esamemobile.screens.home

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.Member
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.CharacterRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import com.example.esamemobile.data.firebase.firestore.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomePage {
    CHARACTERS,GROUPS
}

enum class HomeDialog {
    CHOICE,NEW_GROUP,JOIN_GROUP
}

data class HomeState(
    val homePage: HomePage = HomePage.CHARACTERS,
    val characters: List<Character> = emptyList(),
    val groups: List<Group> = emptyList(),
    val filteredCharacters: List<Character> = emptyList(),
    val filteredGroups: List<Group> = emptyList(),
    val currentDialog: HomeDialog? = null
    )

data class HomeActions(
    val onPageSelect: (Int) -> Unit,
    val getAllCharacters: () -> Unit,
    val getAllGroups: () -> Unit,
    val onCharactersSearch: (String) -> Unit,
    val onGroupsSearch: (String) -> Unit,
    val onGroupCreate: (String) -> Unit,
    val onGroupJoin: (String) -> Unit,
    val onOpenDialog: (HomeDialog) -> Unit,
    val onDismissDialog: () -> Unit
)

class HomeViewModel(
    val authRepository: AuthRepository,
    val characterRepository: CharacterRepository,
    val groupRepository: GroupRepository,
    val userRepository: UserRepository
): ViewModel() {
    private val _state = MutableStateFlow(HomeState())

    val state = _state.asStateFlow()

    val actions = HomeActions(
        onPageSelect = { index -> _state.update { it.copy(homePage = HomePage.entries[index]) } },
        getAllCharacters = { loadCharacters() },
        getAllGroups = { loadGroups() },
        onCharactersSearch = { text ->
            _state.update {
                it.copy(filteredCharacters = if (text.isBlank()) it.characters else it.characters.filter { char ->
                    char.name.contains(
                        text,
                        ignoreCase = true
                    )
                })
            }
        },
        onGroupsSearch = { text ->
            _state.update {
                it.copy(filteredGroups = if (text.isBlank()) it.groups else it.groups.filter { group ->
                    group.name.contains(
                        text,
                        ignoreCase = true
                    )
                })
            }
        },
        onGroupCreate = { groupName ->
            val uid = authRepository.currentUser?.uid ?: return@HomeActions
            viewModelScope.launch {
                val user = userRepository.usernameAndImageObserver(uid).first()
                val group = Group(
                    name = groupName,
                    masterId = uid
                )
                val result = groupRepository.addNewGroup(group, user.first, user.second)
                result.fold(
                    onSuccess = { Log.i("debug", "Gruppo creato con successo") },
                    onFailure = { exception ->
                        Log.w(
                            "debug",
                            "Errore creazione gruppo ${exception.message}"
                        )
                    }
                )

                loadGroups()
            }
        },
        onGroupJoin = { id ->
            val uid = authRepository.currentUser?.uid ?: return@HomeActions
            viewModelScope.launch {
                val user = userRepository.usernameAndImageObserver(uid).first()
                val member = Member(
                    userId = uid,
                    username = user.first,
                    userImgUrl = user.second)
                val result = groupRepository.insertNewGroupMember(member,id)
                result.fold(
                    onSuccess = {
                        Log.i("debug","Successo nell'unione")
                        loadGroups()
                        },
                    onFailure = { exception ->
                        Log.w("debug","Errore nell'unirsi al gruppo ${exception.message}")
                    }
                )
            }
        },
        onOpenDialog = { dialog -> _state.update { it.copy(currentDialog = dialog) } },
        onDismissDialog = { _state.update { it.copy(currentDialog = null) } }
    )

    init {
        loadCharacters()
        loadGroups()
    }

    //Funzione che carica tutti i personaggi e i gruppi
    private fun loadCharacters() {
        val userId = authRepository.currentUser?.uid ?: return

        viewModelScope.launch {
            characterRepository.getAllUserCharacters(userId)
                .onSuccess { loadedCharacters ->
                    _state.update { it.copy(characters = loadedCharacters, filteredCharacters = loadedCharacters) }
                }
                .onFailure { exception -> Log.w("debug","OOPSIE ${exception.message}")}
        }
    }

    private fun loadGroups() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            groupRepository.getAllUsersGroups(userId)
                .onSuccess { loadedGroups ->
                    _state.update { it.copy(groups = loadedGroups, filteredGroups = loadedGroups) }
                }
                .onFailure {  exception -> Log.w("debug","OOPSIE ${exception.message}") }
        }
//        _state.update { it.copy(groups = listOf(
//                Group(id = "1", name = "I Zingari", ""),
//                Group(id = "2", name = "I fantastici 2", ""),
//                Group(id = "3", name = "I tre moschettoni", ""),
//                Group(id = "4", name = "I quattro gatti", ""),
//                Group(id = "5", name = "I cojo(n)ti", ""),
//                Group(id = "6", name = "Giovanni", ""),
//                Group(id = "7", name = "Miku club", ""),
//                Group(id = "8", name = "Il gioco perso", ""),
//                Group(id = "9", name = "Ci piacciono i treni", ""),
//                Group(id = "10", name = "Impottibile!", "")),
//            filteredGroups = listOf(
//                Group(id = "1", name = "I Zingari", ""),
//                Group(id = "2", name = "I fantastici 2", ""),
//                Group(id = "3", name = "I tre moschettoni", ""),
//                Group(id = "4", name = "I quattro gatti", ""),
//                Group(id = "5", name = "I cojo(n)ti", ""),
//                Group(id = "6", name = "Giovanni", ""),
//                Group(id = "7", name = "Miku club", ""),
//                Group(id = "8", name = "Il gioco perso", ""),
//                Group(id = "9", name = "Ci piacciono i treni", ""),
//                Group(id = "10", name = "Impottibile!", ""))
//        ) }
    }
}