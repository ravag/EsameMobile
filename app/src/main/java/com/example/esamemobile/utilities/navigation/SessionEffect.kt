package com.example.esamemobile.utilities.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute

//Tenere traccia dello stato dell'utente: se è loggato o no, se è entrato come ospite.
@Composable
fun SessionEffect(
    navController: NavHostController,
    sessionState: SessionState,
    pendingGroup: MutableState<String?>,
    canNavigate: (SessionState) -> Boolean
) {
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Loading) return@LaunchedEffect
        if (!canNavigate(sessionState)) return@LaunchedEffect

        when (sessionState) {
            is SessionState.Guest -> {
                navController.navigate(EsameMobileRoute.Home) {
                    popUpTo(0) {inclusive = true}
                }
            }
            is SessionState.Authenticated -> {
                navController.navigate(EsameMobileRoute.Home) {
                    popUpTo(0) {inclusive = true}
                }
                pendingGroup.value?.let { id ->
                    navController.navigate(EsameMobileRoute.GroupDetails(id))
                    pendingGroup.value = null
                }
            }
            is SessionState.LoggedOut -> {
                navController.navigate(EsameMobileRoute.Login) {
                    popUpTo(0) {inclusive = true}
                }
            }
            else -> {}
        }
    }
}