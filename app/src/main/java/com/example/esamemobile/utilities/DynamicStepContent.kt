package com.example.esamemobile.utilities

import android.content.Context
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

interface DisplayableItem {
    val id: String
    val name: String
    val description: String
    val numericValue: Int
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericStepContent(
   title: String,
   counterTitle: String,
   counterValueText: String,
   counterContainerColor: Color,
   counterValueColor: Color,
   emptyListText: String,
   itemList: List<DisplayableItem>,
   showDialog: Boolean,
   onDialogVisibilityChange: (Boolean) -> Unit,

   dialogNewTitle: String,
   dialogEditTitle: String,
   dialogNameLabel: String,
   dialogNumericLabel: String,

   onAddItem: (name: String, desc: String, value: Int) -> Boolean,
   onEditItem: (id: String, name: String, desc: String, value: Int) -> Boolean,
   onDeleteItem: (DisplayableItem) -> Unit,

   focusManager: FocusManager,
   context: Context,
   modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var itemIdToEdit by remember { mutableStateOf("") }

    var inputName by remember { mutableStateOf("") }
    var inputDescription by remember { mutableStateOf("") }
    var inputNumeric by remember { mutableStateOf("") }

    fun resetDialogFields() {
        inputName = ""
        inputDescription = ""
        inputNumeric = ""
        isEditing = false
        itemIdToEdit = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Titolo Schermata
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        //Card Contatore Dinamico
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = counterContainerColor)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = counterTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = counterValueText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = counterValueColor)
            }
        }

        //Lista Elementi
        if (itemList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyListText,
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
                items(itemList) { item ->
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
                                    text = "${item.name} (${item.numericValue})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.description,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            //Tasto Modifica
                            IconButton(
                                onClick = {
                                    itemIdToEdit = item.id
                                    inputName = item.name
                                    inputDescription = item.description
                                    inputNumeric = item.numericValue.toString()
                                    isEditing = true
                                    onDialogVisibilityChange(true)
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.primary)
                            }

                            //Testo Elimina
                            IconButton(onClick = { onDeleteItem(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    //Dialog unico per inserimento o modifica
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
                        text = if (isEditing) dialogEditTitle else dialogNewTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    //Campi input
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(dialogNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputDescription,
                        onValueChange = { inputDescription = it },
                        label = { Text("Descrizione") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp),
                        maxLines = 5,
                        singleLine = false
                    )

                    OutlinedTextField(
                        value = inputNumeric,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                inputNumeric = input
                            }
                        },
                        label = { Text(dialogNumericLabel) },
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
                                val costInt = if (inputNumeric.isBlank()) 1 else inputNumeric.toIntOrNull() ?: 0

                                if (inputName.isBlank()) {
                                    Toast.makeText(context, "Inserisci un nome valido!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (costInt < 0) {
                                    Toast.makeText(context, "Inserisci un costo valido (non negativo)!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val isInventory = dialogNumericLabel.lowercase().contains("peso")

                                if (isInventory) {
                                    val parts = counterValueText.split("/")
                                    if (parts.size == 2) {
                                        val currentWeight = parts[0].trim().toIntOrNull() ?: 0
                                        val maxWeight = parts[1].trim().toIntOrNull() ?: 0
                                        val oldWeight = if (isEditing) {
                                            itemList.find { it.id == itemIdToEdit }?.numericValue ?: 0
                                        } else 0

                                        if ((currentWeight - oldWeight) + costInt > maxWeight) {
                                            Toast.makeText(context, "Supereresti il peso massimo!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                    }
                                } else {
                                    val peLeft = counterValueText.replace("PE", "").trim().toIntOrNull() ?: 0
                                    val oldCost = if (isEditing) {
                                        itemList.find { it.id == itemIdToEdit }?.numericValue ?: 0
                                    } else 0

                                    if (costInt > (peLeft + oldCost)) {
                                        Toast.makeText(context, "Punti Evoluzione Insufficienti!",Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                }

                                val statusOperation = if (isEditing) {
                                    onEditItem(itemIdToEdit, inputName, inputDescription, costInt)
                                } else {
                                    onAddItem(inputName, inputDescription, costInt)
                                }

                                if (statusOperation) {
                                    onDialogVisibilityChange(false)
                                    resetDialogFields()
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
