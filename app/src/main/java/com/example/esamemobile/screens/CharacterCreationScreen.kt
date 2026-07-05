package com.example.esamemobile.screens

import android.widget.Toast
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableIntStateOf
import kotlin.collections.remove
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import io.ktor.websocket.Frame

data class Ability(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val cost: Int
)

data class EditableStat(
    val label: String,
    val value: Int,
    val baseValue: Int,
    val onValueChange: (Int) -> Unit
)

fun calculateBaseDamage(power: Int): String {
    return when (power) {
        in Int.MIN_VALUE..1 -> "1d2"
        in 2..3 -> "1d4"
        in 4..5 -> "1d6"
        in 6..7 -> "1d8"
        in 8..9 -> "1d10"
        else -> "1d12"
    }
}

fun calculateModifier(stat: Int): Int {
    return when (stat) {
        in Int.MIN_VALUE..1 -> -1
        in 2..3 -> 0
        in 4..5 -> 1
        in 6..7 -> 2
        in 8..9 -> 3
        else -> 4
    }
}

@Composable
fun StatisticStepContent(
    viewModel: CharacterViewModel,
    focusManager: FocusManager,
    context: Context,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    //Dati anagrafici
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    var isSpendingPEMode by remember { mutableStateOf(false) }

    //Stati delle statistiche base
    var strength by remember { mutableIntStateOf(1) }
    var agility by remember { mutableIntStateOf(1) }
    var intelligence by remember { mutableIntStateOf(1) }
    var charisma by remember { mutableIntStateOf(1) }
    var power by remember { mutableIntStateOf(1) }

    //Valori base delle stat congelati dai quali si conta la spesa dei PE
    var baseStrength by remember { mutableIntStateOf(1) }
    var baseAgility by remember { mutableIntStateOf(1) }
    var baseIntelligence by remember { mutableIntStateOf(1) }
    var baseCharisma by remember { mutableIntStateOf(1) }
    var basePower by remember { mutableIntStateOf(1) }

    var peSpentHP by remember { mutableIntStateOf(0) }
    var hpBase by remember { mutableIntStateOf(0) }

    //Stati derivati
    val strengthModifier by remember { derivedStateOf { calculateModifier(strength) } }
    val agilityModifier by remember { derivedStateOf { calculateModifier(agility) } }
    val intelligenceModifier by remember { derivedStateOf { calculateModifier(intelligence) } }
    val charismaModifier by remember { derivedStateOf { calculateModifier(charisma) } }
    val powerModifier by remember { derivedStateOf { calculateModifier(power) } }

    val hpMax by remember { derivedStateOf { hpBase + strengthModifier + peSpentHP } }
    val speed by remember { derivedStateOf { 6 + (1.5 * agilityModifier) } }
    val inventoryCapacity by remember { derivedStateOf { maxOf(1, strengthModifier + 1) } }
    val baseDamage by remember { derivedStateOf { calculateBaseDamage(power) } }

    //Funzione per gestire il point buy sulle statistiche
    fun handleStatChange(currentValue: Int, baseValue: Int, increment: Boolean, onUpdate: (Int) -> Unit) {
        if (!isSpendingPEMode) return

        if (increment) {
            if (viewModel.peLeft >= 2 && currentValue < 10) {
                onUpdate(currentValue + 1)
                viewModel.peLeft -= 2
            }
        } else {
            if (currentValue > baseValue) {
                onUpdate(currentValue - 1)
                viewModel.peLeft += 2
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header (Nome, Età, Foto Avatar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = {
                            age = it
                            val parsedAge = it.toIntOrNull() ?: 0
                            if (parsedAge > 70) {
                                Toast.makeText(context, "Età superiore a 70, TODO Malus Casuale", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = { Text("Età") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            val randomAge = (1..100).random()
                            age = randomAge.toString()
                            if (randomAge > 70) {
                                Toast.makeText(context, "Età superiore a 70, TODO Malus Casuale", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tira Età")
                    }
                }
            }

            // Foto Avatar
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable {
                        focusManager.clearFocus()
                        Toast.makeText(
                            context,
                            "TODO Apri Galleria o Fotocamera",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                contentAlignment = Alignment.Center
            ) {
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
                Text("${viewModel.peLeft} PE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
                    color = if (!isSpendingPEMode) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Switch(
                    checked = isSpendingPEMode,
                    onCheckedChange = { checked ->
                        focusManager.clearFocus()
                        isSpendingPEMode = checked
                        if (checked) {
                            baseStrength = strength
                            baseAgility = agility
                            baseIntelligence = intelligence
                            baseCharisma = charisma
                            basePower = power
                        } else {
                            strength = baseStrength
                            agility = baseAgility
                            intelligence = baseIntelligence
                            charisma = baseCharisma
                            power = basePower
                            viewModel.peLeft = 10 - peSpentHP
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    "Aumenta\nStatistiche",
                    fontSize = 12.sp,
                    color = if (isSpendingPEMode) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }

        //Lista delle Statistiche Reattive
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val stats = listOf(
                EditableStat("Forza", strength, baseStrength) { v:Int -> strength = v },
                EditableStat("Agilità", agility, baseAgility) { v:Int -> agility = v },
                EditableStat("Intelligenza", intelligence, baseIntelligence) { v:Int -> intelligence = v },
                EditableStat("Carisma", charisma, baseCharisma) { v:Int -> charisma = v },
                EditableStat("Potere", power, basePower) { v:Int -> power = v },
            )

            stats.forEach { stat ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stat.label,
                        modifier = Modifier.width(100.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { handleStatChange(stat.value, stat.baseValue,  false, stat.onValueChange) },
                            enabled = isSpendingPEMode && stat.value > stat.baseValue
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Meno")
                        }

                        if (!isSpendingPEMode) {
                            OutlinedTextField(
                                value = if (stat.value == 0) "" else stat.value.toString(),
                                onValueChange = { input ->
                                    val parsed = input.toIntOrNull() ?: 0
                                    if (parsed in 1..10 || input.isEmpty()) {
                                        stat.onValueChange(parsed)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                            onClick = { handleStatChange(stat.value, stat.baseValue, true, stat.onValueChange) },
                            enabled = isSpendingPEMode && viewModel.peLeft >= 2 && stat.value < 10
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Più")
                        }
                    }

                    Text(
                        "Mod: ${if (calculateModifier(stat.value) >= 0) "+" else ""}${calculateModifier(stat.value)}",
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End,
                        fontWeight = FontWeight.SemiBold
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
                        "HP Massimi: $hpMax",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "(Base: $hpBase + Mod For: $strengthModifier + PE Extra: $peSpentHP)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { hpBase = (1..6).random() + (1..6).random() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("2d6", fontSize = 12.sp)
                    }

                    if (peSpentHP > 0) {
                        IconButton(
                            onClick = {
                                peSpentHP --
                                viewModel.peLeft += 1
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Rimuovi PE da HP")
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    IconButton(
                        onClick = {
                            if (viewModel.peLeft > 0) {
                                peSpentHP ++
                                viewModel.peLeft -= 1
                            }
                        },
                        enabled = viewModel.peLeft > 0
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Aggiungi PE a HP")
                    }
                }
            }

            Text("Capacità di Carico: $inventoryCapacity", fontSize = 16.sp)
            Text("Velocità: $speed m", fontSize = 16.sp)
            Text(
                "Danno Attacco Base: $baseDamage",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilitiesStepContent(
    viewModel: CharacterViewModel,
    focusManager: FocusManager,
    context: Context,
    showDialog: Boolean,
    onDialogVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val abilitiesList = viewModel.abilitiesList

    var isEditing by remember { mutableStateOf(false) }
    var abilityIdToEdit by remember { mutableStateOf("") }

    var abilityName by remember { mutableStateOf("") }
    var abilityDescription by remember { mutableStateOf("") }
    var abilityCost by remember { mutableStateOf("") }

    fun resetDialogFields() {
        abilityName = ""
        abilityDescription = ""
        abilityCost = ""
        isEditing = false
        abilityIdToEdit = ""
    }

    Column(
            modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Header Titolo
            Text(
                "ABILITÀ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            //Contatore PE rimasti
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
                    Text("Punti Evoluzione Rimasti: ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${viewModel.peLeft} PE", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            //Lista Abilità Aggiunte
            if (abilitiesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nessuna abilità creata.\nUsa il tasto \"+\" in basso per aggiungerne una.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(abilitiesList) { ability ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ability.name} (${ability.cost} PE)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (ability.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ability.description,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                //Tasto Modifica
                                IconButton(
                                    onClick = {
                                        abilityIdToEdit = ability.id
                                        abilityName = ability.name
                                        abilityDescription = ability.description
                                        abilityCost = ability.cost.toString()
                                        isEditing = true
                                        onDialogVisibilityChange(true)
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.primary)
                                }

                                //Testo Elimina
                                IconButton(
                                    onClick = {
                                        viewModel.peLeft = viewModel.peLeft + ability.cost
                                        abilitiesList.remove(ability)
                                        Toast.makeText(context, "Abilità rimossa", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

    //Dialog di Modifica Abilità
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = {
                onDialogVisibilityChange(false)
                resetDialogFields()
            }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AlertDialogDefaults.containerColor),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    //Titolo
                    Text(
                        text = if (isEditing) "Modifica Abilità" else "Nuova Abilità",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    //Campi input
                    OutlinedTextField(
                        value = abilityName,
                        onValueChange = { abilityName = it },
                        label = { Text("Nome Abilità") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = abilityDescription,
                        onValueChange = { abilityDescription = it },
                        label = { Text("Descrizione") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = abilityCost,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                abilityCost = input
                            }
                        },
                        label = { Text("Costo in PE") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    //Pulsanti Conferma e Annulla
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDialogVisibilityChange(false)
                                resetDialogFields()
                            }
                        ) {
                            Text("Annulla")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val costInt = abilityCost.toIntOrNull() ?: 0

                                if (abilityName.isBlank()) {
                                    Toast.makeText(context, "Inserisci un nome valido!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (costInt < 0) {
                                    Toast.makeText(context, "Inserisci un costo valido (non negativo)!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (isEditing) {
                                    val oldAbility = abilitiesList.find { it.id == abilityIdToEdit }
                                    if (oldAbility != null) {
                                        val currentPePool = viewModel.peLeft + oldAbility.cost
                                        if (costInt <= currentPePool) {
                                            viewModel.peLeft = currentPePool - costInt
                                            val index = abilitiesList.indexOf(oldAbility)
                                            abilitiesList[index] = Ability(oldAbility.id, abilityName, abilityDescription, costInt)
                                            onDialogVisibilityChange(false)
                                            resetDialogFields()
                                        } else {
                                            Toast.makeText(context, "PE insufficienti!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    if (costInt <= viewModel.peLeft) {
                                        viewModel.peLeft -= costInt
                                        abilitiesList.add(Ability(name = abilityName, description = abilityDescription, cost = costInt))
                                        onDialogVisibilityChange(false)
                                        resetDialogFields()
                                    } else {
                                        Toast.makeText(context, "PE insufficienti per questa abilità", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text(if (isEditing) "Salva" else "Aggiungi")
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun InventoryStepContent() {
    TODO()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationScreen(
    navController: NavController,
    viewModel: CharacterViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var currentStep by remember { mutableStateOf(CreationStep.STATISTICS) }

    var showAbilityDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (currentStep == CreationStep.ABILITIES) {
                FloatingActionButton(
                    onClick = { showAbilityDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
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
                when (currentStep) {
                    CreationStep.STATISTICS -> {
                        StatisticStepContent(viewModel, focusManager, context, modifier = Modifier.fillMaxSize())
                    }
                    CreationStep.ABILITIES -> {
                        AbilitiesStepContent(
                            viewModel = viewModel,
                            focusManager = focusManager,
                            context = context,
                            showDialog = showAbilityDialog,
                            onDialogVisibilityChange = { showAbilityDialog = it },
                            modifier = Modifier.fillMaxSize()
                            )
                    }
                    CreationStep.INVENTORY -> {
                        // TODO: Text("Schermata inventario da aggiungere")
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
                        when (currentStep) {
                            CreationStep.ABILITIES -> currentStep = CreationStep.STATISTICS
                            CreationStep.INVENTORY -> currentStep = CreationStep.ABILITIES
                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = currentStep != CreationStep.STATISTICS
                ) {
                    Text("< Indietro", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        when (currentStep) {
                            CreationStep.STATISTICS -> currentStep = CreationStep.ABILITIES
                            CreationStep.ABILITIES -> currentStep = CreationStep.INVENTORY
                            CreationStep.INVENTORY -> TODO()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                ) {
                    val buttonText = if (currentStep == CreationStep.INVENTORY) "Conferma" else "Avanti >"
                    Text(buttonText, fontSize = 16.sp)
                }
            }
        }
    }
}




