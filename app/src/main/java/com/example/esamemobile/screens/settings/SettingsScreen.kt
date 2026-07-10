package com.example.esamemobile.screens.settings

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun SettingsScreen(
    settingsState: SettingsState,
    settingsActions: SettingsActions,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,"Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->

        //Popup cambio nome
        if (settingsState.changeName) {
            Dialog(
                onDismissRequest = settingsActions.cancelChangeName,
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(25.dp))
                        OutlinedTextField(
                            value = settingsState.tempName,
                            onValueChange = settingsActions.onUsernameChange,
                            label = { Text("Nome") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row() {
                            Button(
                                onClick = settingsActions.cancelChangeName
                            ) {
                                Text("Annulla")
                            }
                            Spacer(Modifier.width(10.dp))
                            Button(
                                onClick = settingsActions.onConfirmNameChange
                            ) {
                                Text("Conferma")
                            }
                        }
                    }
                }

            }
        }

        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(0.2f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ImageWithPlaceholder("",Size.Lg)
                    Text(settingsState.username)
                }
            }
            Row(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.Magenta
                    )
                    .weight(0.1f)
                    .fillMaxWidth()
                    .clickable(onClick = {
                        settingsActions.onClickChangeName()
                        Log.i("debug","premuto cambio nome")
                    })
            ) {
                Text("Cambia nome utente")
                Spacer(Modifier.width(15.dp))
                Icon(Icons.Filled.KeyboardDoubleArrowRight,"avanti?")
            }
            Row(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.Magenta
                    )
                    .weight(0.1f)
                    .fillMaxWidth()
                    .clickable(onClick = {
                        settingsActions.onClickChangePassword
                        Log.i("debug","premuto cambio password")
                    })
            ) {
                Text("cambia password")
                Spacer(Modifier.width(15.dp))
                Icon(Icons.Filled.KeyboardDoubleArrowRight,"avanti?")
            }
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.Magenta
                    )
                    .weight(0.4f)
                    .fillMaxWidth()
            ) {
                Text("Tema applicazione:")
                ThemeValues.entries.forEach { theme ->
                    RadioItem(
                        selected = theme == settingsState.theme,
                        text = theme.text,
                        onClick = { settingsActions.onThemeChange(theme) }
                    )
                }

                Text("Colori dinamici:")
                listOf(true,false).forEach { color ->
                    RadioItem(
                        selected = color == settingsState.dynamicColors,
                        text = if (color) "Colori di sistema" else "colori personalizzati",
                        onClick = { settingsActions.onDynamicColorsChange(color) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f)
            ) {
                //Bottone logout
                Button(
                    onClick = {
                        settingsActions.onLogOut()
                    }
                ) {
                    Text("Logout")
                    Icon(Icons.AutoMirrored.Filled.Logout,"Logout")
                }

                Spacer(Modifier.width(10.dp))

                //Bottone elimina account
                Button(
                    onClick = {
                        Log.i("debug","Elimina")
                    }
                ) {
                    Text("Elimina account")
                    Icon(Icons.Filled.Cancel,"Elimina account")
                }
            }
        }
    }
}

@Composable
private fun RadioItem(selected: Boolean, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}