package com.example.esamemobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController

//Metodo migliore per tenere traccia se un utente è loggato oppure no
@Composable
fun SessionEffect(navController: NavHostController, sessionState: Boolean) {
    LaunchedEffect(sessionState) {
        if (sessionState) {
            navController.navigate(EsameMobileRoute.Home) {
                popUpTo(0) {inclusive = true}
            }
        } else {
            navController.navigate(EsameMobileRoute.Login) {
                popUpTo(0) {inclusive = true}
            }
        }
    }
}