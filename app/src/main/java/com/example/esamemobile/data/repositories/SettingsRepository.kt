package com.example.esamemobile.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.esamemobile.screens.settings.ThemeValues
import kotlinx.coroutines.flow.map

class SettingsRepository (
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")

        private val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamicColors")
    }

    val theme = dataStore.data.map { preferences ->
        try {
            ThemeValues.valueOf(preferences[THEME_KEY] ?: "SYSTEM")
        } catch (_: Exception) {
            ThemeValues.SYSTEM
        }
    }

    val dynamicColors = dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLORS_KEY] ?: false
    }

    suspend fun setTheme(theme: ThemeValues) = dataStore.edit { preferences ->
        preferences[THEME_KEY] = theme.toString()
    }

    suspend fun setDynamicColors(colors: Boolean) = dataStore.edit { preferences ->
        preferences[DYNAMIC_COLORS_KEY] = colors
    }
}