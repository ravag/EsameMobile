package com.example.esamemobile.screens.groupDetails

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.Group
import com.example.esamemobile.data.Member
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.GroupRepository
import com.example.esamemobile.data.repositories.FileRepository
import com.example.esamemobile.data.supabase.ImagesRepository
import com.google.firebase.Timestamp
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
    val master: Member? = null,
    val members: List<Member> = emptyList(),
    val tempDesc: String = "",
    val tempName: String = "",
    val isLoading: Boolean = true,
    val isOwner: Boolean = false,
    val isEditing: Boolean = false,
    val message: String? = null
)

data class GroupDetailsActions(
    val onCharacterClick: (String) -> Boolean,
    val onSelectTab: (Int) -> Unit,
    val onExitOrDelete: () -> Unit,
    val onChangeDescription: (String) -> Unit,
    val onChangeName: (String) -> Unit,
    val onSaveChange: () -> Unit,
    val onUpdateGroupPhoto:(String) -> Unit,
    val onLoad: () -> Unit,
    val onChangePage: () -> Unit,
    val toggleEdit: () -> Unit,
    val onChageSessionDate: (Timestamp) -> Unit,
    val onMessageShown: () -> Unit
)

class GroupDetailsViewModel (
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository,
    private val fileRepository: FileRepository,
    private val imagesRepository: ImagesRepository
): ViewModel() {
    val groupId = MutableStateFlow<String?>(null)
    private val _state = MutableStateFlow(GroupDetailsState())
    val state = _state.asStateFlow()

    val actions: GroupDetailsActions
        get() = GroupDetailsActions(
            onCharacterClick = { text ->
                _state.update { it.copy(isLoading = true) }
                text == authRepository.currentUser?.uid
            },
            onSelectTab = { index -> _state.update { it.copy(selectedTab = GroupDetailsTab.entries[index]) } },
            //Ho bisogno di fare le query per il db prima
            onExitOrDelete = {
                viewModelScope.launch {
                    val result = if (_state.value.isOwner) {
                        groupRepository.deleteGroup(_state.value.group!!.id)
                    } else {
                        groupRepository.removeGroupMember(
                            authRepository.currentUser!!.uid,
                            _state.value.group!!.id
                        )
                    }
                    result.fold(
                        onSuccess = { Log.i("debug", "Eliminazione avvenuta con successo") },
                        onFailure = { exception ->
                            Log.w(
                                "debug",
                                "Errore nell'eliminazione ${exception.message}"
                            )
                        }
                    )
                }

            },
            onChangeDescription = { text -> _state.update { it.copy(tempDesc = text) } },
            onChangeName = { text -> _state.update { it.copy(tempName = text) } },
            onSaveChange = {
                val current = _state.value.group ?: return@GroupDetailsActions
                viewModelScope.launch {
                    val updated = current.copy(
                        description = _state.value.tempDesc,
                        name = _state.value.tempName
                    )
                    _state.update { it.copy(group = updated, isEditing = false) }
                    val result = groupRepository.updateGroup(_state.value.group!!)
                    result.fold(
                        onSuccess = { Log.i("debug", "salvataggio gruppo con successo") },
                        onFailure = { exception ->
                            Log.w(
                                "debug",
                                "Errore nel salvataggio del gruppo ${exception.message}"
                            )
                        }
                    )
                }
            },
            onUpdateGroupPhoto = { uri ->
                val current = _state.value.group ?: return@GroupDetailsActions
                viewModelScope.launch {
                    var url = ""
                    val bytes = fileRepository.readBytes(uri.toUri())

                    bytes?.let {
                        val result = imagesRepository.uploadImage(
                            it,
                            current.id,
                            "groups"
                        )
                        result.fold(
                            onSuccess = { path -> url = path },
                            onFailure = { exception ->
                                Log.w("debug", "Errore salvataggio supabase ${exception.message}")
                                return@launch
                            }
                        )
                    }
                    val result = groupRepository.updateGroupImage(
                        current.id,
                        url
                    )
                    result.fold(
                        onSuccess = {
                            val updated = current.copy(imageUrl = url)
                            _state.update { it.copy(group = updated) }
                            Log.i("debug", "immagine forse salvata con successo")

                        },
                        onFailure = { exception ->
                            Log.w("debug", "Errore ${exception.message}")
                        }
                    )
                }
            },
            onLoad = { load() },
            onChangePage = { _state.update { it.copy(isLoading = true) } },
            toggleEdit = {
                _state.update {
                    it.copy(
                        isEditing = !it.isEditing,
                        tempDesc = it.group!!.description
                    )
                }
            },
            onChageSessionDate = { timestamp ->
                val current = _state.value.group ?: return@GroupDetailsActions
                viewModelScope.launch {
                    val updated = current.copy(nextSession = timestamp)
                    _state.update { it.copy(group = updated) }
                    val result =
                        groupRepository.insertSessionDate(_state.value.group!!.id, timestamp)
                    result.fold(
                        onSuccess = { newMsg("Nuova sessione inserita con successo") },
                        onFailure = { exception ->
                            newMsg("Si è verificato un errore nell'inserimento della sessione")
                            Log.w(
                                "debug",
                                "Errore nell'inserimento data ${exception.message}"
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

    fun load() {
        viewModelScope.launch {
            groupId.filterNotNull().collectLatest { id ->
                _state.update { it.copy(isLoading = true) }
                var tempGroup: Group? = null
                val result = groupRepository.readGroup(id)
                result.fold(
                    onSuccess = { group -> tempGroup = group },
                    onFailure = { exception ->
                        Log.w("debug","Errorazzo ${exception.message}")
                        _state.update { it.copy(isLoading = false) }
                        return@collectLatest
                    }
                )
                val membersResult = groupRepository.getAllGroupMembers(id)
                membersResult.fold(
                    onSuccess = { membersList ->
                        val masterMember = membersList.find { it.userId == tempGroup?.masterId }
                        _state.update { it.copy(
                            group = tempGroup,
                            master = masterMember,
                            members = membersList.filter { member -> member.userId != tempGroup?.masterId },
                            tempDesc = tempGroup?.description ?: "",
                            tempName = tempGroup?.name ?: "",
                            isLoading = false,
                            isOwner = tempGroup?.masterId == authRepository.currentUser!!.uid
                        )
                        }
                        Log.i("debug","yippie")
                    },
                    onFailure = { exception ->
                        Log.w("debug","Errorazzo2 ${exception.message}")
                        _state.update { it.copy(isLoading = false) }
                    }
                )
            }
        }
    }

    private fun newMsg(msg: String) = _state.update { it.copy(message = msg) }
}