package com.example.esamemobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

//Metodo migliore per tenere traccia se un utente è loggato oppure no
@Composable
fun SessionEffect(navController: NavHostController, sessionState: Boolean) {
    //Serve per evitare che al cambio di tema non da applicazione si ripartà dalla home o dal login
    var isFirstComposition by remember { mutableStateOf(true) }

    LaunchedEffect(sessionState) {
        if (isFirstComposition) {
            isFirstComposition = false
            return@LaunchedEffect
        }

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