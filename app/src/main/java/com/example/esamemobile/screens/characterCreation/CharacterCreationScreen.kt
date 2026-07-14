package com.example.esamemobile.screens.characterCreation

import android.Manifest.permission
import com.example.esamemobile.R
import androidx.compose.ui.layout.ContentScale
import android.widget.Toast
import com.example.esamemobile.utilities.GenericStepContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.esamemobile.data.calculateModifier
import com.example.esamemobile.utilities.GenericBasicDialog
import java.io.File

data class EditableStat(
    val label: String,
    val value: Int,
    val baseValue: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticStepContent(
    state: CharacterCreationState,
    actions: CharacterCreationActions,
    focusManager: FocusManager,
    context: Context,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            actions.onAvatarSelected(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            actions.onAvatarSelected(tempCameraUri.toString())
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

    val ageInt = state.age.toIntOrNull() ?: 0
    val showAgeMalus = ageInt > 70

    //Foto Avatar
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray, RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .clickable {
                    focusManager.clearFocus()
                    actions.onSetAvatarOptionDialogVisible(true)
                },
            contentAlignment = Alignment.Center
        ) {
            if (state.avatarUri != null) {
                AsyncImage(
                    model = state.avatarUri,
                    contentDescription = "Avatar Personaggio",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Avatar",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "Aggiungi\nFoto",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                }
            }
        }

        //Nome ed Età
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = state.name,
                onValueChange = { actions.onNameChange(it) },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.age,
                    onValueChange = { actions.onAgeChange(it) },
                    label = { Text("Età") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                if (showAgeMalus) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            actions.onSetAgeMalusDialogVisible(true)
                        }
                    ) {
                        val malusDrawableId = state.ageMalusId

                        if (malusDrawableId != null) {
                            Icon(
                                painter = painterResource(id = malusDrawableId),
                                contentDescription = "Dettagli Malus",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Dettagli Malus",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        actions.onRollAge()
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(8.dp))
                ) {
                    Icon(painterResource(id = R.drawable.ic_dice), contentDescription = "Genera Età")
                }
            }
        }

        //Contatore PE Rimasti
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Punti Evoluzione Rimasti:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${state.peLeft} PE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }

        //Controllo Modalità Statistiche
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("STATISTICHE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Inserimento\nManuale",
                    fontSize = 12.sp,
                    color = if (!state.isSpendingPEMode) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Switch(
                    checked = state.isSpendingPEMode,
                    onCheckedChange = { actions.onTogglePEMode(it) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    "Aumenta\nStatistiche",
                    fontSize = 12.sp,
                    color = if (state.isSpendingPEMode) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }

        OutlinedButton(
            onClick = {
                focusManager.clearFocus()
                actions.onRollAllStats()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSpendingPEMode
        ) {
            Text("Genera Statistiche")
            Spacer(modifier = Modifier.width(10.dp))
            Icon(painter = painterResource(id = R.drawable.ic_dice), contentDescription = "Genera Statistiche", modifier = Modifier.size(18.dp))
        }

        //Lista delle Statistiche Reattive
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val stats = listOf(
                EditableStat("Forza", state.strength, state.baseStrength),
                EditableStat("Agilità", state.agility, state.baseAgility),
                EditableStat("Intelligenza", state.intelligence, state.baseIntelligence),
                EditableStat("Carisma", state.charisma, state.baseCharisma),
                EditableStat("Potere", state.power, state.basePower),
            )

            stats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stat.label,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        IconButton(
                            onClick = { actions.onStatPointBuy(stat.label, false) },
                            enabled = state.isSpendingPEMode && stat.value > stat.baseValue
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Meno")
                        }

                        if (!state.isSpendingPEMode) {
                            OutlinedTextField(
                                value = if (stat.value == 0) "" else stat.value.toString(),
                                onValueChange = { input ->
                                    val parsed = input.toIntOrNull() ?: 0
                                    actions.onStatManualChange(stat.label, parsed)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (stat.value == 0) {
                                        actions.onStatManualChange(stat.label, 1)
                                    }
                                    focusManager.clearFocus()
                                }),
                                modifier = Modifier.size(55.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                singleLine = true
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(55.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${stat.value}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = { actions.onStatPointBuy(stat.label, true) },
                            enabled = state.isSpendingPEMode && state.peLeft >= 2 && stat.value < 10
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Più")
                        }
                    }

                    Text(
                        "Mod:\n ${if (calculateModifier(stat.value) >= 0) "+" else ""}${calculateModifier(stat.value)}",
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        softWrap = true
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        //Dati Derivati
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "HP Massimi: ${state.hpMax}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "(Base: ${state.hpBase} + Mod For: ${state.strengthModifier} + PE Extra: ${state.peSpentHP})",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { actions.onRollHpBase() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dice),
                            contentDescription = "Tira due dadi",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (state.peSpentHP > 0) {
                        IconButton(
                            onClick = { actions.onModifyHpPe(false) }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Rimuovi PE da HP")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    IconButton(
                        onClick = { actions.onModifyHpPe(true) },
                        enabled = state.peLeft > 0
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aggiungi PE a HP")
                    }
                }
            }

            Text("Capacità di Carico: ${state.inventoryCapacity}", fontSize = 16.sp)
            Text("Velocità: ${state.speed} m", fontSize = 16.sp)
            Text(
                "Danno Attacco Base: ${state.baseDamage}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        GenericBasicDialog(
            show = state.showAgeMalusDialog,
            title = state.ageMalusDescription?.name ?: "Malus Età Avanzata",
            description = state.ageMalusDescription?.desc ?: "Malus età avanzata non pervenuto, probabilmente c'è un errore.",
            onConfirmText = "Chiudi",
            onConfirm = { actions.onSetAgeMalusDialogVisible(false) }
        )

        if (state.showAvatarOptionDialog) {
            val sheetState = rememberModalBottomSheetState()

            ModalBottomSheet(
                onDismissRequest = { actions.onSetAvatarOptionDialogVisible(false) },
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
                            actions.onSetAvatarOptionDialogVisible(false)
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
                            actions.onSetAvatarOptionDialogVisible(false)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    creationState: CharacterCreationState,
    creationActions: CharacterCreationActions,
    focusManager: FocusManager,
    navController: NavController
) {
    val context = LocalContext.current

    LaunchedEffect(creationState.message) {
        creationState.message?.let { msg ->
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
            creationActions.onMessageShown()
        }
    }

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            if (creationState.currentStep == CreationStep.ABILITIES || creationState.currentStep == CreationStep.INVENTORY) {
                FloatingActionButton(
                    onClick = {
                        if (creationState.currentStep == CreationStep.ABILITIES) {
                            creationActions.onSetAbilityDialogVisible(true)
                        } else {
                            creationActions.onSetItemDialogVisible(true)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 60.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (creationState.currentStep) {
                    CreationStep.STATISTICS -> {
                        StatisticStepContent(
                            state = creationState,
                            actions = creationActions,
                            focusManager = focusManager,
                            context = context,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CreationStep.ABILITIES -> {
                        GenericStepContent(
                            title = "ABILITÀ",
                            counterTitle = "Punti Evoluzione Rimasti:",
                            counterValueText = "${creationState.peLeft} PE",
                            counterContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            counterValueColor = MaterialTheme.colorScheme.primary,
                            emptyListText = "Nessuna abilità creata.\nUsa il tasto \"+\" in basso per aggiungerne una.",
                            itemList = creationState.abilitiesList,
                            showDialog = creationState.showAbilityDialog,
                            onDialogVisibilityChange = { creationActions.onSetAbilityDialogVisible(it) },
                            dialogNewTitle = "Nuova Abilità",
                            dialogEditTitle = "Modifica Abilità",
                            dialogNameLabel = "Nome Abilità",
                            dialogNumericLabel = "Costo in PE",
                            onAddItem = { name, desc, value ->
                                creationActions.onAddAbility(name, desc, value)
                            },
                            onEditItem = { id, name, desc, value ->
                                creationActions.onEditAbility(id, name, desc, value)
                            },
                            onDeleteItem = { item ->
                                (item as? AbilityItem)?.let { creationActions.onDeleteAbility(it) }
                            },
                            focusManager = focusManager,
                            context = context,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    CreationStep.INVENTORY -> {
                        val currentWeight = creationState.inventoryList.sumOf { it.numericValue }
                        GenericStepContent(
                            title = "EQUIPAGGIAMENTO",
                            counterTitle = "Capacità di Carico:",
                            counterValueText = "${currentWeight} / ${creationState.maxWeightCapacity}",
                            counterContainerColor = if (currentWeight > creationState.maxWeightCapacity) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                            counterValueColor = if (currentWeight > creationState.maxWeightCapacity) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                            emptyListText = "L'inventario è vuoto.\nUsa il tasto \"+\" in basso per aggiungere un oggetto.",
                            itemList = creationState.inventoryList,
                            showDialog = creationState.showItemDialog,
                            onDialogVisibilityChange = { creationActions.onSetItemDialogVisible(it) },
                            dialogNewTitle = "Nuovo Oggetto",
                            dialogEditTitle = "Modifica Oggetto",
                            dialogNameLabel = "Nome Oggetto",
                            dialogNumericLabel = "Peso",
                            onAddItem = { name, desc, value ->
                                creationActions.onAddItem(name, desc, value)
                            },
                            onEditItem = { id, name, desc, value ->
                                creationActions.onEditItem(id, name, desc, value)
                            },
                            onDeleteItem = { item ->
                                (item as? InventoryItem)?.let { creationActions.onDeleteItem(item) }
                            },
                            focusManager = focusManager,
                            context = context,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        creationActions.onPreviousStep {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("< Indietro", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        creationActions.onNextStep() {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = creationState.isNextStepEnabled
                ) {
                    val buttonText = if (creationState.currentStep == CreationStep.INVENTORY) "Conferma" else "Avanti >"
                    Text(buttonText, fontSize = 16.sp)
                }
            }
        }
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


