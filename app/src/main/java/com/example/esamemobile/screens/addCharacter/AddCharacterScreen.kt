package com.example.esamemobile.screens.addCharacter

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.esamemobile.data.Character
import com.example.esamemobile.utilities.composables.CharacterItem
import com.example.esamemobile.utilities.composables.GenericBasicDialog
import com.example.esamemobile.utilities.composables.GenericList
import com.example.esamemobile.utilities.composables.SimpleSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCharacterScreen(
    addCharacterState: AddCharacterState,
    addCharacterActions: AddCharacterActions,
    navController: NavHostController
) {
    val focusManager = LocalFocusManager.current
    val textFieldState = rememberTextFieldState()
    var selectedChar by remember { mutableStateOf<Character?>(null) }
    val context = LocalContext.current

    LaunchedEffect(addCharacterState.message) {
        addCharacterState.message?.let { msg ->
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
            addCharacterActions.onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("")},
                navigationIcon = {
                    IconButton({ navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack,"Indietro")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (selectedChar != null)
        {
            GenericBasicDialog(
                show = true,
                title = "Sicuro?",
                description = "Sei sicuro di voler scegliere ${selectedChar!!.name} come personaggio per il gruppo?",
                onConfirmText = "Conferma",
                onConfirm = {
                    addCharacterActions.onChooseCharacter(selectedChar!!)
                    navController.navigateUp()
                },
                onDismissText = "Annulla",
                onDismiss = { selectedChar = null },
            )
        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("SCEGLI PERSONAGGIO",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            SimpleSearchBar(textFieldState,addCharacterActions.onCharacterSearch)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GenericList(
                    contentPadding = PaddingValues(0.dp),
                    elems = addCharacterState.filteredCharacters,
                    key = {it.id}
                ) {character ->
                    CharacterItem(character) { selectedChar = character }
                }

            }
        }
    }
}