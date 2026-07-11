package com.example.esamemobile.screens.groupDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.Member
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GroupDetailsTab {
    DESCRIPTION,MEMBERS
}


data class GroupDetailsState(
    val group: Group? = null,
    val selectedTab: GroupDetailsTab = GroupDetailsTab.DESCRIPTION,
    val members: List<Member> = emptyList(),
    val tempDesc: String = "",
    val isLoading: Boolean = true,
    val isOwner: Boolean = false
)

data class GroupDetailsActions(
    val onCharacterClick: (String) -> Boolean,
    val onSelectTab: (Int) -> Unit,
    val onFabClick: () -> Unit,
    val onExitOrDelete: () -> Unit,
    val onChangeDescription: (String) -> Unit,
    val onSaveChangeDescription: () -> Unit
)

class GroupDetailsViewModel (
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    val groupId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(GroupDetailsState())
    val state = _state.asStateFlow()

    val actions: GroupDetailsActions
        get() = GroupDetailsActions(
            onCharacterClick = { text -> true },
            onSelectTab = { index -> _state.update { it.copy(selectedTab = GroupDetailsTab.entries[index]) } },
            onFabClick = {},
            //Ho bisogno di fare le query per il db prima
            onExitOrDelete = {
                if (_state.value.isOwner) {
                    Log.i("debug","proprietario")
                } else {
                    Log.i("debug","Non proprietario")
                }
            },
            onChangeDescription = { text -> _state.update { it.copy(tempDesc = text) } },
            onSaveChangeDescription = {
                val current = _state.value.group ?: return@GroupDetailsActions
                val updated = current.copy(description = _state.value.tempDesc)
                _state.update { it.copy(group = updated) }
            }
        )

    fun setId(groupId: String) {
        this.groupId.value = groupId
    }

    fun load() {
        viewModelScope.launch {
            groupId.filterNotNull().collectLatest { id ->
                _state.update { it.copy(isLoading = true) }

                val result = groupRepository.readGroup(id)
                result.fold(
                    onSuccess = { group ->
                        _state.update { it.copy(
                            group = group,
                            tempDesc = group?.description ?: "",
                            isLoading = false,
                            isOwner = group?.masterId == authRepository.currentUser!!.uid
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
}