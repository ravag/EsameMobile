package com.example.esamemobile.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoggedIn: Boolean,
    val password: String = "",
    val theme: ThemeValues,
    val dynamicColors: Boolean,
    val changeName: Boolean = false,
    val changePassword: Boolean = false
)

data class IncompleteSettingsState(
    val username: String,
    val isLoggedIn: Boolean,
    val password: String = "",
    val changeName: Boolean = false,
    val changePassword: Boolean = false
)

data class SettingsActions(
    val onClickChangeName: () -> Unit,
    val onUsernameChange: (String) -> Unit,
    val onConfirmNameChange: () -> Unit,
    val onClickChangePassword: () -> Unit,
    val onThemeChange: (ThemeValues) -> Unit,
    val onDynamicColorsChange: (Boolean) -> Unit
)

class SettingsViewModel(repository: SettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(
        IncompleteSettingsState(
            username = "Alessandro",
            isLoggedIn = true))
    val state = combine(
        repository.theme,
        repository.dynamicColors,
        _state
    ) { theme,colors, state -> SettingsState(
            state.username,
            state.isLoggedIn,
            state.password,
            theme,
            colors) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = SettingsState(
                username = _state.value.username,
                isLoggedIn = _state.value.isLoggedIn,
                theme = ThemeValues.SYSTEM,
                dynamicColors = false
            )
        )

    val actions = SettingsActions(
        onClickChangeName = { _state.update { it.copy(changeName = true) } },
        onUsernameChange = {name -> _state.update { it.copy(username = name) } },
        onConfirmNameChange = { Log.i("debug","Cambia nome a ${_state.value.username}") },
        onClickChangePassword = { _state.update { it.copy(changePassword = true) } },
        onThemeChange = { themeValue -> viewModelScope.launch { repository.setTheme(themeValue) } },
        onDynamicColorsChange = {colors -> viewModelScope.launch { repository.setDynamicColors(colors) }}
    )
}