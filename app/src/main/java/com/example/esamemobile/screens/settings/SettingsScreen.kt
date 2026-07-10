package com.example.esamemobile.screens.settings

import android.Manifest.permission
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size
import java.io.File
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsState: SettingsState,
    settingsActions: SettingsActions,
    navController: NavHostController
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarOptionDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            settingsActions.onAvatarSelected(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            settingsActions.onAvatarSelected(tempCameraUri.toString())
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempPictureUri(context)
            if (uri != null) {
                tempCameraUri = uri
                try {
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    Toast.makeText(context, "Impossibile avviare la fotocamera", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Errore nella creazione del file temporaneo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
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
                modifier = Modifier.fillMaxWidth()
                    .weight(0.2f)
                    .clickable(onClick = { showAvatarOptionDialog = true })
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ImageWithPlaceholder(settingsState.imageUrl,Size.Lg)
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

        if (showAvatarOptionDialog) {
            val sheetState = rememberModalBottomSheetState()

            ModalBottomSheet(
                onDismissRequest = { showAvatarOptionDialog = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Seleziona foto avatar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showAvatarOptionDialog = false
                            permissionLauncher.launch(permission.CAMERA)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scatta una foto")
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showAvatarOptionDialog = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scegli dalla galleria")
                    }

                    Spacer(modifier = Modifier.width(8.dp))
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

private fun createTempPictureUri(context: Context): Uri? {
    return try {
        val tempFile = File.createTempFile("avatar_capture_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}