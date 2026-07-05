package com.example.esamemobile.screens

import com.example.esamemobile.screens.CharacterViewModel
import androidx.compose.foundation.lazy.items
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.compose.ui.text.input.ImeAction
import com.example.esamemobile.EsameMobileRoute

data class Ability(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val cost: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreationPart2Screen(
    navController: NavHostController,
    viewModel: CharacterViewModel,
    onBackClick: (Int) -> Unit = { navController.navigate(EsameMobileRoute.CharacterCreation) },
    onNextClick: (List<Ability>, Int) -> Unit = { _, _ -> }
) {
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current

    val abilitiesList = viewModel.abilitiesList

    var showDialog by remember { mutableStateOf(false) }
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

    Scaffold(
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    resetDialogFields()
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Abilità")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                        showDialog = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.primary)
                                }

                                //Testo Elimina
                                IconButton(
                                    onClick = {
                                        viewModel.peLeft = viewModel.peLeft + ability.cost
                                        abilitiesList.remove(ability)
                                        Toast.makeText(ctx, "Abilità rimossa", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            //Pulsanti navigazione a fondo schermo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { onBackClick(viewModel.peLeft) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("< Indietro", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        onNextClick(abilitiesList.toList(), viewModel.peLeft)
                        Toast.makeText(ctx, "TODO: Prossima Schermata Inventario", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Avanti >", fontSize = 16.sp)
                }
            }
        }
    }

    //Dialog di Modifica Abilità
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false }
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
                            onClick = { showDialog = false }
                        ) {
                            Text("Annulla")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val costInt = abilityCost.toIntOrNull() ?: 0

                                if (abilityName.isBlank()) {
                                    Toast.makeText(ctx, "Inserisci un nome valido!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (costInt < 0) {
                                    Toast.makeText(ctx, "Inserisci un costo valido (non negativo)!", Toast.LENGTH_SHORT).show()
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
                                            showDialog = false
                                            resetDialogFields()
                                        } else {
                                            Toast.makeText(ctx, "PE insufficienti!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    if (costInt <= viewModel.peLeft) {
                                        viewModel.peLeft = viewModel.peLeft - costInt
                                        abilitiesList.add(Ability(name = abilityName, description = abilityDescription, cost = costInt))
                                        showDialog = false
                                        resetDialogFields()
                                    } else {
                                        Toast.makeText(ctx, "PE insufficienti per questa abilità", Toast.LENGTH_SHORT).show()
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