package com.example.esamemobile.screens.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.esamemobile.R
import com.example.esamemobile.data.firebase.AuthProviderType
import com.example.esamemobile.utilities.composables.GenericBasicDialog
import com.example.esamemobile.utilities.composables.ChangeImageCard
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size
import com.example.esamemobile.utilities.connection.requestGoogleIdToken
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

    val scrollState = rememberScrollState()

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
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surfaceContainer)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        //Immagine e username
                        ChangeImageCard(
                            context = context,
                            enabled = settingsState.isLoggedIn,
                            modifier = Modifier,
                            useUri = settingsActions.onAvatarSelected
                        ) {
                            ImageWithPlaceholder(settingsState.imageUrl,Size.Lg)
                        }

                        Text(
                            text = if (settingsState.isLoggedIn) settingsState.username else "Utente Ospite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (settingsState.isLoggedIn) {
                    Text(
                        "Account",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column {
                            SettingRowItem(
                                title = "Cambia nome utente",
                                onClick = { settingsActions.onClickChangeName() }
                            )
                            if (settingsState.providerType == AuthProviderType.PASSWORD) {
                                SettingRowItem(
                                    title = "Cambia Password",
                                    onClick = { settingsActions.onClickChangePassword() }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Personalizzazione",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            "Tema applicazione",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        ThemeValues.entries.forEach { theme ->
                            RadioItem(
                                selected = theme == settingsState.theme,
                                text = theme.text,
                                onClick = { settingsActions.onThemeChange(theme) }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Colori dinamici",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        listOf(true,false).forEach { color ->
                            RadioItem(
                                selected = color == settingsState.dynamicColors,
                                text = if (color) "Colori di Sistema" else "Colori Dinamici",
                                onClick = { settingsActions.onDynamicColorsChange(color) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (settingsState.isLoggedIn) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        //Bottone logout
                        OutlinedButton(
                            onClick = { settingsActions.onLogOut() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout,"Logout")
                            Spacer(Modifier.width(8.dp))
                            Text("Logout")
                        }

                        //Bottone elimina account
                        Button(
                            onClick = { deleteAccountPopup = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                        ) {
                            Icon(Icons.Filled.Cancel,"Elimina account")
                            Spacer(Modifier.height(8.dp))
                            Text("Elimina account")
                        }
                    }
                } else {
                    Button(
                        onClick = settingsActions.goToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout,"Logout")
                        Spacer(Modifier.height(8.dp))
                        Text("Vai al login")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRowItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

@OptIn(ExperimentalMaterial3Api::class)
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

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                for (i in 0 until labels.size) {
                    OutlinedTextField(
                        value = textValues[i],
                        onValueChange = updateTexts[i],
                        label = { Text(labels[i]) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (visiblePassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        trailingIcon = if (isPassword) {
                            { val icon = if (visiblePassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                IconButton({ visiblePassword = !visiblePassword }) {
                                    Icon(
                                        icon,
                                        if (visiblePassword) "Nascondi password" else "Mostra password"
                                    )
                                }
                            }
                        } else null
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annulla")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text("Conferma")
                    }
                }
            }
        }
    }
}