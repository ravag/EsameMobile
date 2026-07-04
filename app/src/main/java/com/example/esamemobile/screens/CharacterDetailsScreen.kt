package com.example.esamemobile.screens

import android.graphics.drawable.Icon
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.esamemobile.data.Character
import com.example.esamemobile.utilities.CharacterDetailsNavigationBar
import com.example.esamemobile.utilities.CharacterHeader
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size

private class Abilities(
    val name: String,
    val description: String,
    val cost: Int
)
@Composable
fun CharacterDetailsScreen(character: Character, navController: NavHostController) {

    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(0) }

    var abilities: List<Abilities> = listOf(Abilities("caio","wow caia",1),
        Abilities("aaa","wow caia",3),
        Abilities("bbb","wow caia",2),
        Abilities("cccc","nel mezzo del cammin di nostra vita mi ritrovai per una selva oscura che la diretta via era smarrita, tanto ...",5))

    Scaffold(
        bottomBar = {
            CharacterDetailsNavigationBar(
                selectedIndex = selectedIndex,
                onTabSelected = {index -> selectedIndex = index}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            CharacterHeader(character.name,0,"a ne so",0,character.imageUri, Modifier) {
                Toast.makeText(context,"Level up", Toast.LENGTH_SHORT).show()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("POTERI EVOLUZIONE", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.9f))
                IconButton (
                    onClick = { Toast.makeText(context,"aggiungi", Toast.LENGTH_SHORT).show()},
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "nuovo potere",
                        tint = Color.Magenta
                    )
                }
            }

            ListItems(abilities, Modifier.weight(1f))
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ABILITA'", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold,modifier = Modifier.weight(0.7f))
                IconButton (
                    onClick = { Toast.makeText(context,"rimuovi", Toast.LENGTH_SHORT).show()},
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "togli un uso",
                        tint = Color.Magenta
                    )
                }
                Text("2/2",modifier = Modifier.weight(0.1f))
                IconButton (
                    onClick = { Toast.makeText(context,"aggiungi", Toast.LENGTH_SHORT).show()},
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "aggiungi un uso",
                        tint = Color.Magenta
                    )
                }
            }
            ListItems(abilities, Modifier.weight(1f))
        }

    }
}

@Composable
private fun ListItems(abilities: List<Abilities>,modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(abilities) { ability ->
            AbilityItem(ability)
            Spacer(Modifier.height(5.dp))
        }
    }
}

@Composable
private fun AbilityItem(ability: Abilities) {
    Column() {
        Row(
            modifier = Modifier.border(
                width = 1.dp,
                color = Color.Magenta
                ).padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(ability.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(ability.description, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text("${ability.cost} PE", fontSize = 18.sp)
        }
    }
}