package com.example.esamemobile.utilities.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.TokenReceiver
import com.example.esamemobile.data.firebase.firestore.UserRepository
import com.example.esamemobile.data.repositories.GuestRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SessionState {
    object Loading: SessionState()
    object LoggedOut: SessionState()
    object Guest: SessionState()
    data class Authenticated(val uid: String): SessionState()
}

class SessionViewModel (
    authRepository: AuthRepository,
    userRepository: UserRepository,
    guestRepository: GuestRepository
): ViewModel() {

    private var lastNavigationState: SessionState? = null
    val sessionState = combine(
        authRepository.authState,
        guestRepository.isGuest
    ) {user, isGuest ->
        when {
            user != null -> SessionState.Authenticated(user.uid)
            isGuest -> SessionState.Guest
            else -> SessionState.LoggedOut
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = SessionState.Loading
    )

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                if (user != null ) {
                    TokenReceiver.newToken(userRepository)
                }
            }
        }
    }

    fun canNavigate(newState: SessionState): Boolean {
        if (newState == lastNavigationState) return false
        lastNavigationState = newState
        return true
    }
}