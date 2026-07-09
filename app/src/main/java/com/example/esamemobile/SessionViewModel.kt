package com.example.esamemobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.TokenReceiver
import com.example.esamemobile.data.firebase.firestore.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SessionViewModel (
    authRepository: AuthRepository,
    userRepository: UserRepository
): ViewModel() {

    val isLoggedIn = authRepository.authState
        .map { user -> user != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = authRepository.currentUser != null
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
}