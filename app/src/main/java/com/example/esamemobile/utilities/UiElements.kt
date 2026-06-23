package com.example.esamemobile.utilities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size

data class NavigationItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun NavigationBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Personaggi", Icons.Outlined.Person),
        NavigationItem("Gruppi", Icons.Outlined.AccountBox),
    )

    val selectedItemIndex = remember { mutableStateOf(0) }

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = {
                    onTabSelected(index)
                },
                label = { Text(text = item.title) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                }
            )
        }
    }
}

@Composable
fun CharacterList(contentPadding: PaddingValues, chars: List<Character>,context: Context) {
    GenericList(contentPadding,chars,context,true)
}

@Composable
fun GroupList(contentPadding: PaddingValues, groups: List<Group>, context: Context) {
    GenericList(contentPadding, groups, context, false )
}

@Composable
private fun GenericList(contentPadding: PaddingValues, elems: List<Any>, context: Context, isChar: Boolean) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp,8.dp,8.dp,80.dp),
        modifier = Modifier.padding(contentPadding)
    ) {
        if(isChar) {
            elems as List<Character>
            items(elems) { elem ->
                CharacterItem(elem) {
                    Toast.makeText(context,"cliccato personaggio ${elem.id}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            elems as List<Group>
            items(elems) {elem ->
                GroupItem(elem) {
                    Toast.makeText(context,"Cliccato gruppo ${elem.id}", Toast.LENGTH_SHORT).show()
                }
            }
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

