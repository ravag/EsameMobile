package com.example.esamemobile.utilities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Context
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size

data class NavigationItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun NavigationBottomBarWithFAB(
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
                .background(Color(0xFF1E1E1E)),
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
                    Icons.Outlined.Person,
                    contentDescription = "Personaggi",
                    tint = if (selectedIndex == 0) Color.Green else Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    "Personaggi",
                    color = if (selectedIndex == 0) Color.Green else Color.White,
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
                    Icons.Outlined.AccountBox,
                    contentDescription = "Gruppi",
                    tint = if (selectedIndex == 1) Color.Green else Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    "Gruppi",
                    color = if (selectedIndex == 1) Color.Green else Color.White,
                    fontSize = 12.sp
                )
            }

        }

        //Floating Action Button (FAB)
        FloatingActionButton(
            onClick = onFabClick,
            containerColor = Color.Green,
            contentColor = Color.Black,
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
    NavigationBar(containerColor = Color(0xFF1E1E1E)) {
        //Tasto sinistro: Statistiche
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onTabSelected(0) },
            label = { Text("Statistiche", color = Color.White) },
            icon = {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Statistiche",
                    tint = Color.White
                )
            }
        )

        //Tasto centrale: Abilità
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Abilità", color = Color.White) },
            icon = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = "Abilità",
                    tint = Color.White
                )
            }
        )

        //Tasto destro: Equipaggiamento
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Equipaggiamento", color = Color.White) },
            icon = {
                Icon(
                    Icons.Outlined.ShoppingCart,
                    contentDescription = "Equipaggiamento",
                    tint = Color.White
                )
            }
        )
    }
}

@Composable
fun CharacterList(
    contentPadding: PaddingValues,
    chars: List<Character>,
    onClick: () -> Unit) {
    GenericList( contentPadding,chars) { char ->
        CharacterItem(char, onClick)
    }
}

@Composable
fun GroupList(
    contentPadding: PaddingValues,
    groups: List<Group>,
    context: Context) {
    GenericList(contentPadding, groups ){ group ->
        GroupItem(group) {
            Toast.makeText(context, "Cliccato gruppo ${group.id}", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun <T> GenericList(
    contentPadding: PaddingValues,
    elems: List<T>,
    elemContent: @Composable (T) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp,8.dp,8.dp,80.dp),
        modifier = Modifier.padding(contentPadding)
    ) {
        items(elems) { elem ->
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
            val imageUri = Uri.parse(group.imageUri)
            ImageWithPlaceholder(imageUri, Size.Sm)
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
            val imageUri = Uri.parse(char.imageUri)
            ImageWithPlaceholder(imageUri, Size.Sm)
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
    characterClass: String,
    level: Int,
    imageUri: String?,
    modifier: Modifier = Modifier,
    onLevelUpClick: () -> Unit,
) {
    val parsedUri = remember(imageUri) {
        if (!imageUri.isNullOrEmpty()) {
            Uri.parse(imageUri)
        } else {
            Uri.EMPTY
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
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
                Text("Nome: $name", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Età: $age", color = Color.White, fontSize = 16.sp)
                Text("Classe: $characterClass", color = Color.White, fontSize = 16.sp)
                Text("Livello: $level", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                ImageWithPlaceholder(
                    uri = parsedUri,
                    size = Size.Lg
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onLevelUpClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                "Level Up",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = Color.Gray, thickness = 1.dp)
    }
}