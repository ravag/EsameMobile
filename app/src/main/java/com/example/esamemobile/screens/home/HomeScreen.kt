package com.example.esamemobile.screens.home

import android.widget.Toast
import com.example.esamemobile.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.utilities.composables.NavigationBottomBarWithFAB
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.utilities.composables.CharacterItem
import com.example.esamemobile.utilities.composables.GenericBasicDialog
import com.example.esamemobile.utilities.composables.GenericList
import com.example.esamemobile.utilities.composables.GroupItem
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
    val groupTextFieldState = rememberTextFieldState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeActions.syncCharacters()
        homeActions.getAllGroups()
    }

    LaunchedEffect(homeState.homePage) {
        focusManager.clearFocus()
    }

    LaunchedEffect(homeState.message) {
        homeState.message?.let { msg ->
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
            homeActions.onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("")},
                actions = {
                    IconButton({navController.navigate(EsameMobileRoute.Settings)}) {
                        Icon(Icons.Filled.Settings,"Impostazioni")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBottomBarWithFAB(
                firstOptionText = "Personaggi",
                firstOptionImage = Icons.Outlined.Person,
                secondOptionText = "Gruppi",
                secondOptionImage = ImageVector.vectorResource(id = R.drawable.ic_groups),
                selectedIndex = homeState.homePage.ordinal,
                onTabSelected = { newIndex ->
                    focusManager.clearFocus()
                    homeActions.onPageSelect(newIndex)
                                },
                onFabClick = {
                    focusManager.clearFocus()
                    when (homeState.homePage) {
                        HomePage.CHARACTERS ->  navController.navigate(EsameMobileRoute.CharacterCreation)
                        HomePage.GROUPS ->  homeActions.onOpenDialog(if (homeState.isLoggedIn) HomeDialog.CHOICE else HomeDialog.OFFLINE)
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->

        when (homeState.currentDialog) {
            HomeDialog.CHOICE -> {
                Dialog(
                    onDismissRequest = homeActions.onDismissDialog,
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = { homeActions.onOpenDialog(HomeDialog.NEW_GROUP) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Nuovo gruppo")
                            }
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Button(
                                onClick = { homeActions.onOpenDialog(HomeDialog.JOIN_GROUP) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Entra in un gruppo")
                            }
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Button(
                                onClick = homeActions.onDismissDialog,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Annulla")
                            }
                        }
                    }
                }
            }
            HomeDialog.NEW_GROUP -> {
                GenericInputPopup(
                    textFieldState = groupTextFieldState,
                    title = "Nuovo gruppo",
                    query = "Nome gruppo",
                    onDismiss = {
                        homeActions.onDismissDialog()
                        groupTextFieldState.edit { replace(0,this.length,"") }
                    },
                    onConfirm = {
                        homeActions.onGroupCreate(groupTextFieldState.text.toString())
                        groupTextFieldState.edit { replace(0,this.length,"") }
                        homeActions.onDismissDialog()
                    }
                )
            }
            HomeDialog.JOIN_GROUP -> {
                GenericInputPopup(
                    textFieldState = groupTextFieldState,
                    title = "Entra in un gruppo",
                    query = "Codice gruppo",
                    limit = 8,
                    onDismiss = {
                        homeActions.onDismissDialog()
                        groupTextFieldState.edit { replace(0,this.length,"") }
                    },
                    onConfirm = {
                        homeActions.onGroupJoin(groupTextFieldState.text.toString())
                        groupTextFieldState.edit { replace(0,this.length,"") }
                        homeActions.onDismissDialog()
                    }
                )
            }
            HomeDialog.OFFLINE -> {
                GenericBasicDialog(
                    show = true,
                    title = "Sei offline",
                    description = "Impossibile creare o unirsi ai gruppi da offline o senza un account",
                    onConfirmText = "OK",
                    onConfirm = homeActions.onDismissDialog
                )
            }
            null -> { }
        }

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
                        if (homeState.characters.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxHeight(0.8f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Non ci sono personaggi presenti\nCreane uno nuovo oppure premendo il tasto +",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            GenericList(
                                contentPadding = PaddingValues(0.dp),
                                elems = homeState.filteredCharacters,
                                key = { it.id }
                            ) { character ->
                                CharacterItem(character) {
                                    navController.navigate(
                                        EsameMobileRoute.CharacterDetails(character.id, true)
                                    )
                                }
                            }
                        }
                    }
                    HomePage.GROUPS ->  {
                        if (homeState.groups.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxHeight(0.8f),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Non ci sono gruppi presenti\nCreane uno nuovo oppure entra in un gruppo premendo il tasto +",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            GenericList(
                                contentPadding = PaddingValues(0.dp),
                                elems = homeState.filteredGroups,
                                key = {it.id}
                            ) { group ->
                                GroupItem(group) { navController.navigate(EsameMobileRoute.GroupDetails(group.id)) }
                            }
                        }
                    }
                }
            }


        }
    }
}

@Composable
fun GenericInputPopup(
    textFieldState: TextFieldState,
    title: String,
    query: String,
    limit: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontSize = 15.sp)
                Spacer(Modifier.height(25.dp))
                OutlinedTextField(
                    value = textFieldState.text.toString(),
                    onValueChange = { s ->
                        if (limit == null || s.length <= limit) {
                            textFieldState.edit { replace(0,this.length,s) }
                        }
                    },
                    label = { Text(query) },
                    supportingText = if (limit != null) { {
                        Text("${textFieldState.text.length}/$limit")
                    } } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Row{
                    Button(
                        onClick = onDismiss
                    ) {
                        Text("Annulla")
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = onConfirm
                    ) {
                        Text("Conferma")
                    }
                }
            }
        }
    }
}

