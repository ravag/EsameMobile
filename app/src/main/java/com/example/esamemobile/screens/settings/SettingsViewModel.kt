package com.example.esamemobile.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ThemeValues(val text: String) {
    LIGHT("Chiaro"),
    DARK("Scuro"),
    SYSTEM("Sistema")
}

data class SettingsState(
    val username: String,
    val tempName: String = username,
    val isLoggedIn: Boolean,
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
    val onLogOut: () -> Unit
)

class SettingsViewModel(
    val repository: SettingsRepository,
    val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(
        SettingsState(
            username = "Alessandro",
            isLoggedIn = true))
    val state = combine(
        repository.theme,
        repository.dynamicColors,
        authRepository.authState,
        _state
    ) { theme,colors,user, currentState -> currentState.copy(
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
        onConfirmNameChange = { Log.i("debug","Cambia nome a ${_state.value.username}")
                              _state.update { it.copy(changeName = false, username = it.tempName) }},
        cancelChangeName = { _state.update { it.copy(changeName = false, tempName = it.username) } },
        onClickChangePassword = { _state.update { it.copy(changePassword = true) } },
        onThemeChange = { themeValue -> viewModelScope.launch { repository.setTheme(themeValue) } },
        onDynamicColorsChange = {colors -> viewModelScope.launch { repository.setDynamicColors(colors) }},
        onLogOut = {authRepository.logout()}
    )
}