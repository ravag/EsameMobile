package com.example.esamemobile.utilities.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group

@Composable
fun NavigationBottomBarWithFAB(
    firstOptionText: String,
    firstOptionImage: ImageVector,
    secondOptionText: String,
    secondOptionImage: ImageVector,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFabClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            //Tasto sinistro: Personaggi
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(0) }
            ) {
                Icon(
                    firstOptionImage,
                    contentDescription = firstOptionText,
                    tint = if (selectedIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    firstOptionText,
                    color = if (selectedIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            //Spazio per il FAB
            Spacer(modifier = Modifier.weight(1f))

            //Tasto destro: Gruppi
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(1) }
            ) {
                Icon(
                    secondOptionImage,
                    contentDescription = secondOptionText,
                    tint = if (selectedIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    secondOptionText,
                    color = if (selectedIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

        }

        //Floating Action Button (FAB)
        FloatingActionButton(
            onClick = onFabClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape,
            modifier = Modifier
                .size(72.dp)
                .offset(y = (-28).dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Aggiungi",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
fun CharacterDetailsNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        //Tasto sinistro: Statistiche
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onTabSelected(0) },
            label = { Text("Statistiche") },
            icon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Statistiche",
                )
            }
        )

        //Tasto centrale: Abilità
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Abilità") },
            icon = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Abilità",
                )
            }
        )

        //Tasto destro: Equipaggiamento
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Equipaggiamento") },
            icon = {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = "Equipaggiamento",
                )
            }
        )
    }
}

@Composable
fun <T> GenericList(
    contentPadding: PaddingValues,
    elems: List<T>,
    key: ((T) -> Any)? = null,
    elemContent: @Composable (T) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp,8.dp,8.dp,80.dp),
        modifier = Modifier.padding(contentPadding)
    ) {
        items(elems, key) { elem ->
            elemContent(elem)
        }
    }
}

@Composable
fun GroupItem(group: Group, onClick: () -> Unit) {
    Card(
        onClick =  onClick,
        modifier = Modifier
            .size(150.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageWithPlaceholder(group.imageUrl, Size.Sm)
            Spacer(Modifier.size(8.dp))
            Text(
                group.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CharacterItem(char: Character, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(150.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageWithPlaceholder(char.imageUrl, Size.Sm)
            Spacer(Modifier.size(8.dp))
            Text(
                char.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CharacterHeader(
    name: String,
    age: Int,
    ageMalusSign: Int?,
    characterClass: String?,
    level: Int,
    imageUrl: String?,
    points: Int,
    context: Context,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onMalusClick: () -> Unit,
    onLevelUpClick: (() -> Unit)?,
    useUri: (String) -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Nome: $name",color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Età: $age",color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodyMedium)
                    if (age > 70) {
                        IconButton(
                            onClick = onMalusClick
                        ) {
                            if (ageMalusSign != null) {
                                Icon(
                                    painter = painterResource(ageMalusSign),
                                    "age malus",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Dettagli Malus",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                Text("Classe: ${characterClass ?: "nessuna"}",color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodyMedium)
                Text("Livello: $level",color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                ChangeImageCard(
                    context = context,
                    enabled = enabled,
                    modifier = modifier,
                    useUri = useUri
                ) {
                    ImageWithPlaceholder(
                        url = imageUrl,
                        size = Size.Lg
                    )
                }

            }
        }

        Spacer(Modifier.height(12.dp))
        onLevelUpClick?.let {
            Button(
                onClick = onLevelUpClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "Level Up",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (points != 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
                ) {
                Text("Hai $points PE da spendere",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericBasicDialog(
    show: Boolean,
    title: String,
    description: String,
    onConfirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!show) return

    BasicAlertDialog(
        onDismissRequest = { onDismiss?.invoke() ?: onConfirm() },
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Titolo
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                //Descrizione
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                //Riga dei Pulsanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDismissText != null && onDismiss != null) {
                        TextButton(onClick = onDismiss) {
                            Text(text = onDismissText)
                        }
                    }

                    TextButton(onConfirm) {
                        Text(text = onConfirmText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GenericFormDialog(
    show: Boolean,
    title: String,
    valueLabel: String,
    tempName: String,
    tempDesc: String,
    tempValue: Int,
    onTempDataChanged: (String, String, Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { onTempDataChanged(it, tempDesc, tempValue) },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tempDesc,
                    onValueChange = { onTempDataChanged(tempName, it, tempValue) },
                    label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = if (tempValue == 0) "" else tempValue.toString(),
                    onValueChange = { valor ->
                        val intVal = valor.toIntOrNull() ?: 0
                        onTempDataChanged(tempName, tempDesc, intVal)
                    },
                    label = { Text(valueLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Conferma", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}