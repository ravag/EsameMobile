package com.example.esamemobile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.esamemobile.ui.theme.EsameMobileTheme
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.esamemobile.screens.settings.SettingsViewModel
import com.example.esamemobile.screens.settings.ThemeValues
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        setContent {
            val settingsVm = koinViewModel<SettingsViewModel>()
            val settingsState by settingsVm.state.collectAsStateWithLifecycle()
            val sessionVm = koinViewModel<SessionViewModel>()
            val sessionState by sessionVm.sessionState.collectAsStateWithLifecycle()

            EsameMobileTheme(
                darkTheme = when(settingsState.theme) {
                    ThemeValues.DARK -> true
                    ThemeValues.LIGHT -> false
                    ThemeValues.SYSTEM -> isSystemInDarkTheme()
                },
                dynamicColor = settingsState.dynamicColors
            ) {
                val context = LocalContext.current

                val navController = rememberNavController()

                //Chiediamo il permesso per le notifiche
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                    } else {
                        mutableStateOf(true)
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted -> hasNotificationPermission = isGranted }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val channel = NotificationChannel(
                    "canale_gdr",
                    "Notifiche GDR",
                    NotificationManager.IMPORTANCE_DEFAULT
                    )

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)

                EsameMobileNavGraph(
                    navController,
                    settingsVm,
                    when (sessionState) {
                        is SessionState.Authenticated, is SessionState.Guest -> EsameMobileRoute.Home
                        is SessionState.LoggedOut -> EsameMobileRoute.Login
                        is SessionState.Loading -> EsameMobileRoute.Loading
                    }
                )
                SessionEffect(navController,sessionState,sessionVm::canNavigate)
            }
        }
    }
}