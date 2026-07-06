package com.example.esamemobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.esamemobile.screens.characterDetails.CharacterDetailsScreen
import com.example.esamemobile.screens.characterCreation.CharacterCreationScreen
import com.example.esamemobile.screens.DebugDatabaseScreen
import com.example.esamemobile.screens.home.HomeScreen
import com.example.esamemobile.screens.login.LoginScreen
import com.example.esamemobile.screens.characterDetails.CharacterDetailsViewModel
import kotlinx.serialization.Serializable
import com.example.esamemobile.screens.characterCreation.CharacterCreationViewModel
import com.example.esamemobile.screens.login.LoginViewModel
import com.example.esamemobile.screens.settings.SettingsScreen
import com.example.esamemobile.screens.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

sealed interface EsameMobileRoute {
    @Serializable data object Home : EsameMobileRoute
    @Serializable data class CharacterDetails(val charId: Int) : EsameMobileRoute
    @Serializable data object Login : EsameMobileRoute
    @Serializable data object Debug : EsameMobileRoute //Questa è momentanea, sarà da rimuovere in futuro
    @Serializable data object CharacterCreation : EsameMobileRoute
    @Serializable data object Settings: EsameMobileRoute
}


//Prima di fare questa parte bisogna sistemare bene come passare i parametri in giro perché
//altrimenti non so come passarli in questi costruttori
@Composable
fun EsameMobileNavGraph(navController: NavHostController, settingsVm: SettingsViewModel) {
    val focusManager = LocalFocusManager.current
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
            val loginVm = koinViewModel<LoginViewModel>()
            val loginState by loginVm.state.collectAsStateWithLifecycle()
            LoginScreen(loginState,loginVm.actions)
        }
        composable<EsameMobileRoute.CharacterDetails> { backStackEntry ->
            //val route = backStackEntry.toRoute<EsameMobileRoute.CharacterDetails>()
            val characterVm = viewModel<CharacterDetailsViewModel>()
            val charState by characterVm.state.collectAsStateWithLifecycle()
            CharacterDetailsScreen(charState,characterVm.actions, navController)
        }
        composable<EsameMobileRoute.CharacterCreation> {
            val creationVM = viewModel<CharacterCreationViewModel>()
            val creationState by creationVM.state.collectAsStateWithLifecycle()

            CharacterCreationScreen(
                creationState = creationState,
                creationActions = creationVM.actions,
                focusManager = focusManager,
                navController = navController
            )
        }
        composable<EsameMobileRoute.Settings> {
            val settingsState by settingsVm.state.collectAsStateWithLifecycle()
            SettingsScreen(settingsState,settingsVm.actions,navController)
        }
    }
}