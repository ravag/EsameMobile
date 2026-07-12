package com.example.esamemobile.screens.groupDetails

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.data.Member
import com.example.esamemobile.utilities.NavigationBottomBarWithFAB
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupState: GroupDetailsState ,
    groupActions: GroupDetailsActions,
    navController: NavHostController
) {
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        groupActions.onLoad()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("")},
                navigationIcon = {
                    IconButton({ navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,"Indietro")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBottomBarWithFAB(
                firstOptionText = "Descrizione",
                firstOptionImage = Icons.Default.Book,
                secondOptionText = "Partecipanti",
                secondOptionImage = Icons.Outlined.Person,
                selectedIndex = groupState.selectedTab.ordinal,
                onTabSelected = { index ->  groupActions.onSelectTab(index) }
            ) {
                if (groupState.isOwner) {
                    Log.i("debug","Questo è incompleto, devi inserire il calendario")
                } else {
                    groupActions.onChangePage()
                    navController.navigate(EsameMobileRoute.AddCharacter(groupState.group!!.id))
                }
            }
        }
    ) { innerPadding ->

        when {
            groupState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            (groupState.group == null) -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    Text("Errore nel caricamento del gruppo")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    when (groupState.selectedTab) {
                        GroupDetailsTab.DESCRIPTION -> {
                            //Immagine e nome gruppo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f)
                                    .clickable(onClick = { })
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ImageWithPlaceholder(groupState.group.imageUrl, Size.Lg)
                                    Text(groupState.group.name)
                                }
                            }

                            //DM e prossima sessione questa parte adesso che abbiamo cambiato idea è da sistemare un attimo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.1f),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text("DM")
                                Text("Prossima sessione")
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.1f),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(groupState.master?.username ?: "Sconosciuto")
                                Text(groupState.group.nextSession ?: "Nessuna seesione prevista")
                            }
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text("Descrizione")
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .weight(0.7f)
                                    .fillMaxWidth()
                            ) {
                                Text(groupState.group.description)
//                                Text(
//                                    "Nel mezzo del cammin di nostra vita mi ritrovai per una selva oscura che la diretta via era smarrita" +
//                                            " ahi quanto a dir qual era è cosa dura esta selva selvaggia et aspra et forte che nel pensier rinova la paura" +
//                                            " tanto è amara che poco è più morte ma per trattar del ben che vi trovai vi dirò d'altre cose che vi ho scorte" +
//                                            " non so ben ridir come vi entrai tant'ero pien di sonno a quel punto che la verace via abbandonai"
//                                )
                            }
                            if (groupState.isOwner) {
                                Text(
                                    "Codice Invito: ${groupState.group.inviteCode}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.1f)
                                )
                            }
                        }
                        GroupDetailsTab.MEMBERS -> {
                            LazyColumn() {
                                items(groupState.members) { user ->
                                    ExpandableListItem(user) {
                                        user.characterId?.let { charId ->
                                            navController.navigate(EsameMobileRoute.CharacterDetails(
                                                user.characterId,
                                                groupActions.onCharacterClick(user.userId),
                                                user.userId
                                            ))
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

    }
}

@Composable
fun ExpandableListItem(
    user: Member,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    expanded = !expanded
                    Log.i("debug", expanded.toString())
                }),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ImageWithPlaceholder(user.userImgUrl,Size.Sm)
            Text(user.username)
            if (expanded) {
                Icon( Icons.Default.KeyboardArrowUp, "comprimi")
            } else {
                Icon(Icons.Default.KeyboardArrowDown,"espandi")
            }

        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                if (user.characterName.isNullOrBlank()) {
                    Text("Personaggio non inserito, per inserirlo premere il pulsante +")
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onClick),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ImageWithPlaceholder(user.characterImgUrl, Size.Sm)
                        Text(user.characterName)
                    }
                }
            }
        }
    }
}