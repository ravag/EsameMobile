package com.example.esamemobile.screens.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.esamemobile.R
import com.example.esamemobile.data.firebase.AuthProviderType
import com.example.esamemobile.utilities.GenericBasicDialog
import com.example.esamemobile.utilities.composables.ChangeImageCard
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size
import com.example.esamemobile.utilities.requestGoogleIdToken
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    settingsActions: SettingsActions,
    navController: NavHostController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.default_web_client_id)
    var deleteAccountPopup by remember { mutableStateOf(false) }

    LaunchedEffect(settingsState.message) {
        settingsState.message?.let { msg ->
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
            settingsActions.onMessageShown()
        }
    }

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
            InputDialog(
                text = "Inserisci nuovo nome\n",
                labels = listOf("Nome"),
                textValues = listOf(settingsState.tempName),
                updateTexts = listOf(settingsActions.onUsernameChange),
                onDismiss = settingsActions.cancelChangeName,
                onConfirm = settingsActions.onConfirmNameChange
            )
        }

        if (deleteAccountPopup) {
            if (settingsState.providerType == AuthProviderType.PASSWORD) {
                InputDialog(
                    text = "Reinserisci la password per eliminare,\n Una volta confermata, questa operazione non sarà reversibile",
                    labels = listOf("Password"),
                    textValues = listOf(settingsState.currentPassword),
                    updateTexts = listOf(settingsActions.onChangePasswordInput),
                    onDismiss = { deleteAccountPopup = false },
                    onConfirm = {
                        settingsActions.onPasswordDeleteAccount()
                        deleteAccountPopup = false
                    }
                )
            } else {
                GenericBasicDialog(
                    show = true,
                    title = "Elimina account",
                    description = "Sei sicuro di volere eliminare l'account?\n L'azione non sarà reversibile",
                    onConfirmText = "Elimina",
                    onConfirm = {
                        coroutineScope.launch {
                            requestGoogleIdToken(
                                context = context,
                                webClientId = webClientId,
                                filter = true,
                                onSuccess = settingsActions.onGoogleReauth,
                                onError = settingsActions.onGoogleError
                            )
                        }
                    },
                    onDismissText = "Annulla",
                    onDismiss = { deleteAccountPopup = false },
                )
            }
        }

        if (settingsState.changePassword) {
            InputDialog(
                text = "Inserisci vecchia e nuova password",
                labels = listOf("password attuale","nuova password"),
                textValues = listOf(settingsState.currentPassword,settingsState.newPassword),
                updateTexts = listOf(settingsActions.onChangePasswordInput,settingsActions.onChangeNewPasswordInput),
                isPassword = true,
                onDismiss = settingsActions.onCancelChangePassword,
                onConfirm = settingsActions.onConfirmPasswordChange
            )
        }

        if (settingsState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            ) {
                //Immagine e username
                ChangeImageCard(
                    context = context,
                    enabled = settingsState.isLoggedIn,
                    modifier = Modifier.weight(0.2f),
                    useUri = settingsActions.onAvatarSelected
                ) {
                    ImageWithPlaceholder(settingsState.imageUrl,Size.Lg)
                    Text(settingsState.username)
                }

                if (settingsState.isLoggedIn) {
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
                    if (settingsState.providerType == AuthProviderType.PASSWORD) {
                        Row(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color.Magenta
                                )
                                .weight(0.1f)
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    settingsActions.onClickChangePassword()
                                    Log.i("debug","premuto cambio password")
                                })
                        ) {
                            Text("cambia password")
                            Spacer(Modifier.width(15.dp))
                            Icon(Icons.Filled.KeyboardDoubleArrowRight,"avanti?")
                        }
                    }
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

                if (settingsState.isLoggedIn) {
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
                                deleteAccountPopup = true
                            }
                        ) {
                            Text("Elimina account")
                            Icon(Icons.Filled.Cancel,"Elimina account")
                        }
                    }
                } else {
                    Button(
                        onClick = settingsActions.goToLogin
                    ) {
                        Text("Vai al login")
                        Icon(Icons.AutoMirrored.Filled.Logout,"Logout")
                    }
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

@Composable
private fun InputDialog(
    text: String,
    labels: List<String>,
    textValues:List <String>,
    updateTexts: List<(String) -> Unit>,
    isPassword: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var visiblePassword by remember { mutableStateOf(!isPassword) }

    Dialog(
        onDismissRequest = onDismiss,
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
                Text(text)
                for (i in 0 until labels.size) {
                    OutlinedTextField(
                        value = textValues[i],
                        onValueChange = updateTexts[i],
                        label = { Text(labels[i]) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (visiblePassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions(keyboardType = KeyboardType.Text),
                        trailingIcon =  if (isPassword) {{
                            val icon = if (visiblePassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton( { visiblePassword = !visiblePassword } ) {
                                Icon(icon,if (visiblePassword) "Nascondi password" else "Mostra password")
                            }
                        }} else { { } }
                    )
                }
                Row() {
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Annulla")
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm
                    ) {
                        Text("Conferma")
                    }
                }
            }
        }

    }
}