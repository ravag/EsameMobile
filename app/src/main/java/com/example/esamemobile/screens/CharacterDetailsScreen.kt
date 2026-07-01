package com.example.esamemobile.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.esamemobile.data.Character
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size

@Composable
fun CharacterDetailsScreen(character: Character, modifier: Modifier) {
    Scaffold {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            //Character info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nome: ${character.name}")
                Text("Età: ")
                Text("Classe: ")
                Text("Livello: ")
            }

            //Character image
            val imageUri = Uri.parse(character.imageUri)
            ImageWithPlaceholder(imageUri,Size.Lg)
        }
    }
}