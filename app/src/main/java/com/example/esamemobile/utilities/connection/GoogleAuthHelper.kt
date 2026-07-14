package com.example.esamemobile.utilities.connection

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

suspend fun requestGoogleIdToken(
    context: Context,
    webClientId: String,
    filter: Boolean,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(filter)
        .setServerClientId(webClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            context = context,
            request = request
        )
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )
            onSuccess(googleIdTokenCredential.idToken)
        } else {
            onError(IllegalStateException("Credenziale inattesa"))
        }
    } catch (e: Exception) {
        onError(e)
    }
}
