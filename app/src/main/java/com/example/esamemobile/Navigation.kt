package com.example.esamemobile

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.esamemobile.screens.CharacterCreationPart1Screen
import com.example.esamemobile.screens.CharacterDetailsScreen
import com.example.esamemobile.screens.DebugDatabaseScreen
import com.example.esamemobile.screens.HomeScreen
import com.example.esamemobile.screens.LoginScreen
import kotlinx.serialization.Serializable

sealed interface EsameMobileRoute {
    @Serializable data object Home : EsameMobileRoute
    @Serializable data class CharacterDetails(val charId: Int) : EsameMobileRoute
    @Serializable data object Login : EsameMobileRoute
    @Serializable data object Debug : EsameMobileRoute //Questa è momentanea, sarà da rimuovere in futuro
    @Serializable data object CharacterCreation : EsameMobileRoute
}


//Prima di fare questa parte bisogna sistemare bene come passare i parametri in giro perché
//altrimenti non so come passarli in questi costruttori
@Composable
fun EsameMobileNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = EsameMobileRoute.Login
    ) {
        composable<EsameMobileRoute.Home> {
            HomeScreen(navController)
        }
        composable<EsameMobileRoute.Debug> {
            DebugDatabaseScreen(navController)
        }
        composable<EsameMobileRoute.Login> {
            LoginScreen()
        }
        composable<EsameMobileRoute.CharacterCreation> {
            CharacterCreationPart1Screen(navController)
        }
//        composable<EsameMobileRoute.CharacterDetails> { backStackEntry ->
//            val route = backStackEntry.toRoute<EsameMobileRoute.CharacterDetails>()
//            CharacterDetailsScreen()
//        }
    }
}