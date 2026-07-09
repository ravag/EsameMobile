package com.example.esamemobile.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

enum class AuthErrorType {
    WEAK_PASSWORD, USER_COLLISION, INVALID_CREDENTIALS, UNKNOWN
}

interface AuthRepository {
    val currentUser: FirebaseUser?
    val authState: Flow<FirebaseUser?>
    suspend fun signInOrRegister(email: String, password: String): AuthenticationResult
    suspend fun singInWithGoogleIdToken(idToken: String): AuthenticationResult
    fun logout()
}

//Volevo chiamare la classe AuthResult, ma esiste gia una tale classe,
//Esiste già anche AuthenticationResult, io non so più come chiamarla diversamente
sealed class AuthenticationResult {
    data class Success(val isNewUser: Boolean): AuthenticationResult()
    data class Error(val type: AuthErrorType, val message: String?): AuthenticationResult()
}

class AuthRepositoryImpl( val firebaseAuth: FirebaseAuth): AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInOrRegister(
        email: String,
        password: String
    ): AuthenticationResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthenticationResult.Success(isNewUser = false)
        } catch (e: FirebaseAuthInvalidUserException) { //Problemi relatiiv all'utente, potrebbe derivare da cambi password
            registerNewUser(email, password)
        } catch (e: FirebaseAuthInvalidCredentialsException) { //Deriva principalmete da password deboli
            registerNewUser(email,password)
        } catch (e: Exception) { //Si prende tutto il resto
            AuthenticationResult.Error(AuthErrorType.UNKNOWN,e.message)
        }
    }

    override suspend fun singInWithGoogleIdToken(idToken: String): AuthenticationResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken,null)
            firebaseAuth.signInWithCredential(credential).await()
            AuthenticationResult.Success(isNewUser = false)
        } catch (e: Exception) { //Questo non dovrebbe quasi mai fallire, ma non si sa mai
            AuthenticationResult.Error(AuthErrorType.UNKNOWN,e.message)
        }
    }

    private suspend fun registerNewUser(email: String, password: String) : AuthenticationResult {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            AuthenticationResult.Success(isNewUser = true)
        } catch (e: FirebaseAuthWeakPasswordException) { //Caso password debole
            AuthenticationResult.Error(AuthErrorType.WEAK_PASSWORD, e.message)
        } catch (e: FirebaseAuthUserCollisionException) { //Errori di credenziali che coincidono con altri (1 email per account)
            AuthenticationResult.Error(AuthErrorType.USER_COLLISION,e.message)
        } catch (e: Exception) { // Caso di emergenza, non si sa mai che ci possa essere un qualche altro tipo di eccezione
            AuthenticationResult.Error(AuthErrorType.UNKNOWN,e.message)
        }

    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}