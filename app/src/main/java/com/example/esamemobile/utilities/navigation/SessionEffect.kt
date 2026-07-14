package com.example.esamemobile.utilities.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute

//Metodo migliore per tenere traccia se un utente è loggato oppure no
@Composable
fun SessionEffect(
    navController: NavHostController,
    sessionState: SessionState,
    canNavigate: (SessionState) -> Boolean
) {
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Loading) return@LaunchedEffect
        if (!canNavigate(sessionState)) return@LaunchedEffect

        when (sessionState) {
            is SessionState.Guest, is SessionState.Authenticated -> {
                navController.navigate(EsameMobileRoute.Home) {
                    popUpTo(0) {inclusive = true}
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