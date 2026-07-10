package com.example.esamemobile.screens.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.firestore.UserRepository
import com.example.esamemobile.data.repositories.FileRepository
import com.example.esamemobile.data.repositories.SettingsRepository
import com.example.esamemobile.data.supabase.ImagesRepository
import com.example.esamemobile.screens.characterCreation.toCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ThemeValues(val text: String) {
    LIGHT("Chiaro"),
    DARK("Scuro"),
    SYSTEM("Sistema")
}

data class SettingsState(
    val username: String = "",
    val tempName: String = "",
    val imageUrl: String = "",
    val isLoggedIn: Boolean = true,
    val password: String = "",
    val theme: ThemeValues = ThemeValues.SYSTEM,
    val dynamicColors: Boolean = false,
    val changeName: Boolean = false,
    val changePassword: Boolean = false
)

data class SettingsActions(
    val onClickChangeName: () -> Unit,
    val onUsernameChange: (String) -> Unit,
    val onConfirmNameChange: () -> Unit,
    val cancelChangeName: () -> Unit,
    val onClickChangePassword: () -> Unit,
    val onThemeChange: (ThemeValues) -> Unit,
    val onDynamicColorsChange: (Boolean) -> Unit,
    val onLogOut: () -> Unit,
    val onAvatarSelected: (String) -> Unit
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    val repository: SettingsRepository,
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val imagesRepository: ImagesRepository,
    val fileRepository: FileRepository
) : ViewModel() {

    private val userData: Flow<Pair<String, String>> = authRepository.authState.flatMapLatest { user ->
        if (user == null) {
            flowOf(Pair("",""))
        } else {
            userRepository.usernameAndImageObserver(user.uid)
        }
    }
    private val _state = MutableStateFlow(SettingsState())
    val state = combine(
        repository.theme,
        repository.dynamicColors,
        authRepository.authState,
        userData,
        _state
    ) { theme,colors,user, fetchedData,currentState -> currentState.copy(
            username = fetchedData.first,
            tempName = if (currentState.changeName) currentState.tempName else fetchedData.first,
            imageUrl = fetchedData.second,
            theme= theme,
            dynamicColors = colors,
            isLoggedIn = user != null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _state.value.copy(theme = ThemeValues.SYSTEM, dynamicColors = false)
        )
        

    val actions = SettingsActions(
        onClickChangeName = { _state.update { it.copy(changeName = true) }
                            Log.i("debug",state.value.changeName.toString())},
        onUsernameChange = {name -> _state.update { it.copy(tempName = name) } },
        onConfirmNameChange = {
            viewModelScope.launch {
                val result = userRepository.updateUsername(
                    userId = authRepository.currentUser!!.uid,
                    username = _state.value.tempName)
                result.fold(
                    onSuccess = {
                        Log.i("debug","Nome cambiato con successo")
                        _state.update { it.copy(changeName = false, username = it.tempName) }
                    },
                    onFailure = {
                        Log.w("debug","Errore a cambiare nome nel db")
                    }
                )
            }
            },
        cancelChangeName = { _state.update { it.copy(changeName = false, tempName = it.username) } },
        onClickChangePassword = { _state.update { it.copy(changePassword = true) } },
        onThemeChange = { themeValue -> viewModelScope.launch { repository.setTheme(themeValue) } },
        onDynamicColorsChange = {colors -> viewModelScope.launch { repository.setDynamicColors(colors) }},
        onLogOut = {authRepository.logout()},
        onAvatarSelected = {uri ->
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
                            onFailure = {exception -> Log.w("debug","Errore salvataggio supabase ${exception.message}") }
                        )
                    }
                    val result = userRepository.updateUserImage(
                        authRepository.currentUser!!.uid,
                        url
                    )
                    result.fold(
                        onSuccess = {
                            Log.i("debug","immagine forse salvata con successo")
                        },
                        onFailure = { exception ->
                            Log.w("debug", "Errore ${exception.message}")
                        }
                    )
                }
            }
        }
    )
}