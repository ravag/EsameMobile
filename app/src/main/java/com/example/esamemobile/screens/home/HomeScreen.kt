package com.example.esamemobile.screens.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.utilities.composables.SimpleSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var selectedItemIndex by remember { mutableStateOf(0) }
    val textFieldState = rememberTextFieldState()

    var charactersTest by remember { mutableStateOf(listOf<Character>())
//        listOf(
//            Character(id = 1, name = "Joe Jostino", ""),
//            Character(id = 2, name = "Gigi Pancetta", ""),
//            Character(id = 3, name = "Ettore Pelacane", "")
//        )
    }

    var groupsTest = remember {
        listOf(
            Group(id = "1", name = "I Zingari", ""),
            Group(id = "2", name = "I fantastici 2", ""),
            Group(id = "3", name = "I tre moschettoni", ""),
            Group(id = "4", name = "I quattro gatti", ""),
            Group(id = "5", name = "I cojo(n)ti", ""),
            Group(id = "6", name = "Giovanni", ""),
            Group(id = "7", name = "Miku club", ""),
            Group(id = "8", name = "Il gioco perso", ""),
            Group(id = "9", name = "Ci piacciono i treni", ""),
            Group(id = "10", name = "Impottibile!", "")
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

    //DatabaseServices.insertNewUser()
    //DatabaseServices.insertNewCharacter(charactersTest[2])
//    DatabaseServices.readCharacter(1) { c ->
//        if (c != null) {
//            Toast.makeText(context, "nome: ${c.name}", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(context, "ERRORAZZO", Toast.LENGTH_SHORT).show()
//        }
//    }
//    DatabaseServices.getAllUserGroups { l ->
//        if (l != null) {
//            groupsTest = l
//        } else {
//            Toast.makeText(context, "ERRORAZZO gruppi", Toast.LENGTH_SHORT).show()
//        }
//    }
//    DatabaseServices.getAllUserCharacters { l ->
//        if (l != null) {
//            charactersTest = l
//            Toast.makeText(context, l.toString(), Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(context, "ERRORAZZO personaggi", Toast.LENGTH_SHORT).show()
//        }
//    }

    LaunchedEffect(selectedItemIndex) {
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("HOME")},
                actions = {

                    IconButton({navController.navigate(EsameMobileRoute.Settings)}) {
                        Icon(Icons.Filled.Settings,"Impostazioni")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBottomBarWithFAB(
                selectedIndex = selectedItemIndex,
                onTabSelected = { newIndex ->
                    focusManager.clearFocus()
                    selectedItemIndex = newIndex
                                },
                onFabClick = {
                    focusManager.clearFocus()
                    when (selectedItemIndex) {
                        0 -> navController.navigate(EsameMobileRoute.CharacterCreation)
                        1 -> Toast.makeText(context, "Azione: CREA NUOVO GRUPPO", Toast.LENGTH_SHORT). show()
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
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
                    onClick = { navController.navigate(EsameMobileRoute.Debug) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                ) {
                    Text("Debug Database", color = Color.Black, fontSize = 12.sp)
                }

                //Bottone di logout
//                Button(
//                    onClick = {
//                        FirebaseMessaging.getInstance().unsubscribeFromTopic("tutti")
//                        Toast.makeText(context, "Logout effettuato", Toast.LENGTH_SHORT).show()
//                        navController.navigate(EsameMobileRoute.Login)
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
//                ) {
//                    Text(
//                        text = "Logout",
//                        color = Color.White,
//                        fontSize = 16.sp
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SimpleSearchBar(textFieldState)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedItemIndex) {
                    0 -> {
                        CharacterList(
                            contentPadding = PaddingValues(0.dp),
                            chars = filteredChars,
                        ) {
                            navController.navigate(EsameMobileRoute.CharacterDetails(5))
                        }
                    }

                    1 -> {
                        GroupList(
                            contentPadding = PaddingValues(0.dp),
                            groups = filteredGroups,
                            context = context
                        )
                    }
                }
            }


        }
    }
}
