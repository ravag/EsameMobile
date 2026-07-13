package com.example.esamemobile.screens.settings

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthProviderType
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.AuthenticationResult
import com.example.esamemobile.data.firebase.firestore.UserRepository
import com.example.esamemobile.data.repositories.FileRepository
import com.example.esamemobile.data.repositories.GuestRepository
import com.example.esamemobile.data.repositories.SettingsRepository
import com.example.esamemobile.data.supabase.ImagesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val currentPassword: String = "",
    val newPassword: String = "",
    val theme: ThemeValues = ThemeValues.SYSTEM,
    val dynamicColors: Boolean = false,
    val changeName: Boolean = false,
    val changePassword: Boolean = false,
    val isLoading: Boolean = false,
    val providerType: AuthProviderType = AuthProviderType.UNKNOWN
)

data class SettingsActions(
    val onClickChangeName: () -> Unit,
    val onUsernameChange: (String) -> Unit,
    val onConfirmNameChange: () -> Unit,
    val cancelChangeName: () -> Unit,
    val onClickChangePassword: () -> Unit,
    val onConfirmPasswordChange: () -> Unit,
    val onChangePasswordInput: (String) -> Unit,
    val onChangeNewPasswordInput: (String) -> Unit,
    val onCancelChangePassword: () -> Unit,
    val onThemeChange: (ThemeValues) -> Unit,
    val onDynamicColorsChange: (Boolean) -> Unit,
    val onLogOut: () -> Unit,
    val onAvatarSelected: (String) -> Unit,
    val onGoogleReauth: (String) -> Unit,
    val onGoogleError: (Exception) -> Unit,
    val onPasswordDeleteAccount: () -> Unit,
    val goToLogin: () -> Unit
)

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    val repository: SettingsRepository,
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val imagesRepository: ImagesRepository,
    val fileRepository: FileRepository,
    val guestRepository: GuestRepository
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
    ) { theme,colors,user, fetchedData,currentState -> 
        currentState.copy(
            username = fetchedData.first,
            tempName = if (currentState.changeName) currentState.tempName else fetchedData.first,
            imageUrl = fetchedData.second,
            theme= theme,
            dynamicColors = colors,
            isLoggedIn = user != null,
            providerType = authRepository.providerType) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _state.value.copy(theme = ThemeValues.SYSTEM, dynamicColors = false, providerType = authRepository.providerType)
        )
        

    val actions = SettingsActions(
        onClickChangeName = {
            _state.update { it.copy(changeName = true) }
            Log.i("debug", state.value.changeName.toString())
        },
        onUsernameChange = { name -> _state.update { it.copy(tempName = name) } },
        onConfirmNameChange = {
            viewModelScope.launch {
                val result = userRepository.updateUsername(
                    userId = authRepository.currentUser!!.uid,
                    username = _state.value.tempName
                )
                result.fold(
                    onSuccess = {
                        Log.i("debug", "Nome cambiato con successo")
                        _state.update { it.copy(changeName = false, username = it.tempName) }
                    },
                    onFailure = {
                        Log.w("debug", "Errore a cambiare nome nel db")
                    }
                )
            }
        },
        cancelChangeName = {
            _state.update {
                it.copy(
                    changeName = false,
                    tempName = it.username
                )
            }
        },
        onClickChangePassword = { _state.update { it.copy(changePassword = true) } },
        onThemeChange = { themeValue -> viewModelScope.launch { repository.setTheme(themeValue) } },
        onDynamicColorsChange = { colors ->
            viewModelScope.launch {
                repository.setDynamicColors(
                    colors
                )
            }
        },
        onLogOut = { authRepository.logout() },
        onAvatarSelected = { uri ->
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
                            onFailure = { exception ->
                                Log.w(
                                    "debug",
                                    "Errore salvataggio supabase ${exception.message}"
                                )
                            }
                        )
                    }
                    val result = userRepository.updateUserImage(
                        authRepository.currentUser!!.uid,
                        url
                    )
                    result.fold(
                        onSuccess = {
                            Log.i("debug", "immagine forse salvata con successo")
                        },
                        onFailure = { exception ->
                            Log.w("debug", "Errore ${exception.message}")
                        }
                    )
                }
            }
        },
        onConfirmPasswordChange = {
            viewModelScope.launch {
                val current = _state.value.currentPassword
                val new = _state.value.newPassword
                _state.update { it.copy(isLoading = true) }

                val reauthResult = authRepository.reauthenticateWithPassword(current)
                if (reauthResult is AuthenticationResult.Error) {
                    Log.w("debug", "Errore riautenticazione: ${reauthResult.message}")
                    return@launch
                }
                when (val result = authRepository.updatePassword(new)) {
                    is AuthenticationResult.Error -> {
                        Log.w("debug", "Errore cambio password: ${result.message}")
                    }

                    is AuthenticationResult.Success -> {
                        _state.update {
                            it.copy(
                                changePassword = false,
                                currentPassword = "",
                                newPassword = ""
                            )
                        }
                    }
                }
            }
        },
        onGoogleReauth = { idToken ->
            viewModelScope.launch {
                val reauthResult = authRepository.reauthenticateWithGoogle(idToken)
                if (reauthResult is AuthenticationResult.Error) {
                    Log.w("debug", "Errore riautenticazione: ${reauthResult.message}")
                    return@launch
                }
                deleteAccount()
            }
        },
        onGoogleError = { exception -> Log.w("debug", "Errore google: ${exception.message}") },
        onPasswordDeleteAccount = {
            viewModelScope.launch {
                val reauthResult =
                    authRepository.reauthenticateWithPassword(_state.value.currentPassword)
                if (reauthResult is AuthenticationResult.Error) {
                    Log.w("debug", "Errore eliminazione account password: ${reauthResult.message}")
                    return@launch
                }
                deleteAccount()
            }
        },
        onChangePasswordInput = { text -> _state.update { it.copy(currentPassword = text) } },
        onChangeNewPasswordInput = { text -> _state.update { it.copy(newPassword = text) } },
        onCancelChangePassword = {
            _state.update {
                it.copy(
                    changePassword = false,
                    newPassword = "",
                    currentPassword = ""
                )
            }
        },
        goToLogin = { viewModelScope.launch { guestRepository.setGuest(false) } }
    )

    private suspend fun deleteAccount() {
        val user = authRepository.currentUser?.uid ?: return
        val result = userRepository.deleteUser(user)
        result.fold(
            onSuccess = {
                authRepository.deleteAccount()
                _state.update { it.copy(isLoggedIn = false) }
            },
            onFailure = { exception ->
                Log.w("debug","Errore eliminazione account db ${exception.message}")
            }
        )
    }
}