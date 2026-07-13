package com.example.esamemobile.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map

class GuestRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val GUEST_KEY = booleanPreferencesKey("guest")
    }

    val isGuest = dataStore.data.map { preferences ->
        preferences[GUEST_KEY] ?: false
    }

    suspend fun setGuest(guest: Boolean) = dataStore.edit { preferences ->
        preferences[GUEST_KEY] = guest
    }
}