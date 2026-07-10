package com.example.esamemobile.screens.home

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.utilities.CharacterList
import com.example.esamemobile.utilities.GroupList
import com.example.esamemobile.utilities.NavigationBottomBarWithFAB
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.utilities.CharacterItem
import com.example.esamemobile.utilities.GenericList
import com.example.esamemobile.utilities.composables.SimpleSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeState: HomeState,
    homeActions: HomeActions,
    navController: NavHostController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textFieldState = rememberTextFieldState()

    //Soluzione temporanea per refresh dopo creazione personaggio
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeActions.getAllCharacters()
    }

    LaunchedEffect(homeState.homePage) {
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
                selectedIndex = homeState.homePage.ordinal,
                onTabSelected = { newIndex ->
                    focusManager.clearFocus()
                    homeActions.onPageSelect(newIndex)
                                },
                onFabClick = {
                    focusManager.clearFocus()
                    when (homeState.homePage) {
                        HomePage.CHARACTERS ->  navController.navigate(EsameMobileRoute.CharacterCreation)
                        HomePage.GROUPS ->  Toast.makeText(context, "Azione: CREA NUOVO GRUPPO", Toast.LENGTH_SHORT). show()
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

            SimpleSearchBar(textFieldState,if (homeState.homePage == HomePage.CHARACTERS) homeActions.onCharactersSearch else homeActions.onGroupsSearch)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (homeState.homePage) {
                    HomePage.CHARACTERS -> {
                        GenericList(
                            contentPadding = PaddingValues(0.dp),
                            elems = homeState.filteredCharacters,
                        ) {character ->
                            CharacterItem(character) { navController.navigate(EsameMobileRoute.CharacterDetails(character.id,true)) }
                        }
                    }

                    HomePage.GROUPS ->  {
                        GroupList(
                            contentPadding = PaddingValues(0.dp),
                            groups = homeState.filteredGroups,
                            context = context
                        )
                    }
                }
            }


        }
    }
}
