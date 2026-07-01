package com.example.esamemobile

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.esamemobile.screens.CharacterDetailsScreen
import com.example.esamemobile.screens.HomeScreen
import kotlinx.serialization.Serializable

sealed interface EsameMobileRoute {
    @Serializable data object Home : EsameMobileRoute
    @Serializable data class CharacterDetails(val charId: Int) : EsameMobileRoute
    @Serializable data object Login : EsameMobileRoute
    @Serializable data object Debug : EsameMobileRoute //Questa è momentanea, sarà da rimuovere in futuro
}


//Prima di fare questa parte bisogna sistemare bene come passare i parametri in giro perché
//altrimenti non so come passarli in questi costruttori
//@Composable
//fun EsameMobileNavGraph(navController: NavHostController) {
//    NavHost(
//        navController = navController,
//        startDestination = EsameMobileRoute.Home
//    ) {
//        composable<EsameMobileRoute.Home> {
//            HomeScreen()
//        }
//        composable<EsameMobileRoute.CharacterDetails> { backStackEntry ->
//            val route = backStackEntry.toRoute<EsameMobileRoute.CharacterDetails>()
//            CharacterDetailsScreen()
//        }
//    }
//}