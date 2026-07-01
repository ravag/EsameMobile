package com.example.esamemobile.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.data.Character
import com.example.esamemobile.data.Group
import com.example.esamemobile.utilities.CharacterList
import com.example.esamemobile.utilities.GroupList
import com.example.esamemobile.utilities.NavigationBottomBarWithFAB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.esamemobile.utilities.composables.SimpleSearchBar

@Composable
fun HomeScreen(
    currentUser: FirebaseUser,
    onNavigationToDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var selectedItemIndex by remember { mutableStateOf(0) }
    val textFieldState = rememberTextFieldState()

    val charactersTest = remember {
        listOf(
            Character(id = 1, name = "Joe Jostino", ""),
            Character(id = 2, name = "Gigi Pancetta", ""),
            Character(id = 2, name = "Ettore Pelacane", "")
        )
    }

    val groupsTest = remember {
        listOf(
            Group(id = 1, name = "I Zingari", ""),
            Group(id = 2, name = "I fantastici 2", ""),
            Group(id = 3, name = "I tre moschettoni", ""),
            Group(id = 4, name = "I quattro gatti", ""),
            Group(id = 5, name = "I cojo(n)ti", ""),
            Group(id = 6, name = "Giovanni", ""),
            Group(id = 7, name = "Miku club", ""),
            Group(id = 8, name = "Il gioco perso", ""),
            Group(id = 9, name = "Ci piacciono i treni", ""),
            Group(id = 10, name = "Impottibile!", "")
        )
    }

    val filteredChars by remember {
        derivedStateOf {
            val searchText = textFieldState.text.toString()
            if (searchText.isEmpty()) {
                charactersTest
            } else {
                charactersTest.filter { it.name.contains(searchText,ignoreCase = true) }
            }
        }
    }

    val filteredGroups by remember {
        derivedStateOf {
            val searchText = textFieldState.text.toString()
            if (searchText.isEmpty()) {
                groupsTest
            } else {
                groupsTest.filter { it.name.contains(searchText,ignoreCase = true) }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBottomBarWithFAB(
                selectedIndex = selectedItemIndex,
                onTabSelected = { newIndex -> selectedItemIndex = newIndex },
                onFabClick = {
                    when (selectedItemIndex) {
                        0 -> Toast.makeText(context, "Azione: CREA NUOVO PERSONAGGIO", Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(context, "Azione: CREA NUOVO GRUPPO", Toast.LENGTH_SHORT). show()
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column (
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Bottone per la schermata di debug
                Button (
                    onClick = onNavigationToDebug,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                ) {
                    Text("Debug Database", color = Color.Black, fontSize = 12.sp)
                }

                //Bottone di logout
                Button(
                    onClick = {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic("tutti")
                        auth.signOut()
                        Toast.makeText(context, "Logout effettuato", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "Logout",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SimpleSearchBar(
                textFieldState
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (selectedItemIndex) {
                        0 -> {
                            CharacterList(
                                contentPadding = innerPadding,
                                chars = filteredChars,
                                context = context
                            )
                        }

                        1 -> {
                            GroupList(
                                contentPadding = innerPadding,
                                groups = filteredGroups,
                                context = context
                            )
                        }
                    }
                }
            }
        }
    }
}
