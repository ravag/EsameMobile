package com.example.esamemobile.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esamemobile.data.firebase.AuthErrorType
import com.example.esamemobile.data.firebase.AuthRepository
import com.example.esamemobile.data.firebase.AuthenticationResult
import com.example.esamemobile.data.firebase.firestore.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginMessage {
    data class Info(val text: String): LoginMessage()
    data class Error(val text: String): LoginMessage()
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: LoginMessage? = null
)

data class LoginActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onEmailPasswordSubmit: () -> Unit,
    val onGoogleIdTokenReceived: (String) -> Unit,
    val onGoogleSignInError: (Exception) -> Unit,    //Ci sono gia molti onGoogleSignInError, e anche onGoogleError
    val onMessageShown: () -> Unit
)

class LoginViewModel (
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    
    val actions = LoginActions(
        onEmailChange = {change -> _state.update { it.copy(email = change) }},
        onPasswordChange = { change -> _state.update { it.copy(password = change) } },
        onEmailPasswordSubmit = {
            if (_state.value.email.isBlank() || _state.value.password.isBlank()) {
                _state.update { it.copy(message = LoginMessage.Error("Riempi Tutti i campi")) }
                return@LoginActions
            }

            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                val result = authRepository.signInOrRegister(_state.value.email,_state.value.password)
                _state.update { it.copy(isLoading = false, message = loginToMessage(result)) }
            }
        },
        onGoogleIdTokenReceived = { token ->
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                val result = authRepository.singInWithGoogleIdToken(token)
                _state.update { it.copy(isLoading = false, message = loginToMessage(result)) }
            }
        },
        onGoogleSignInError = { exception -> _state.update { it.copy(message = LoginMessage.Error("Errore login Google ${exception.message}")) } },
        onMessageShown = { _state.update { it.copy(message = null) } }
    )

    private suspend fun loginToMessage(result: AuthenticationResult): LoginMessage {
        return when(result) {
            is AuthenticationResult.Success -> {
                var msg: String = ""
                if (result.isNewUser) {
                    val res = userRepository.insertNewUser(authRepository.currentUser!!.uid)
                    res.fold(
                        onSuccess = { msg = "Registrato con successo"},
                        onFailure = { msg = "Errore nel salvataggio dell'utente"}
                    )
                } else {
                    msg = "Accesso eseguito"
                }
                LoginMessage.Info(msg)
            }
            is AuthenticationResult.Error -> {
                LoginMessage.Error(
                    when(result.type) {
                        AuthErrorType.WEAK_PASSWORD -> "Password Debole! Usa lettere, numeri e simboli."
                        AuthErrorType.USER_COLLISION -> "Password Errata. Nome utente già esistente."
                        AuthErrorType.INVALID_CREDENTIALS -> "Password errata o account non valido"
                        AuthErrorType.UNKNOWN -> "Errore: ${result.message}"
                    }
                )
            }
        }
    }

}


