package com.example.esamemobile.screens.groupDetails

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.R
import com.example.esamemobile.data.Member
import com.example.esamemobile.utilities.NavigationBottomBarWithFAB
import com.example.esamemobile.utilities.composables.ChangeImageCard
import com.example.esamemobile.utilities.composables.ImageWithPlaceholder
import com.example.esamemobile.utilities.composables.Size
import com.example.esamemobile.utilities.intent.addEventToCalendar
import com.example.esamemobile.utilities.intent.sendMessageIntent
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupState: GroupDetailsState ,
    groupActions: GroupDetailsActions,
    navController: NavHostController
) {
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        groupActions.onLoad()
    }

    LaunchedEffect(groupState.message) {
        groupState.message?.let { msg ->
            Toast.makeText(context,msg, Toast.LENGTH_SHORT).show()
            groupActions.onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("")},
                navigationIcon = {
                    IconButton({ navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,"Indietro")
                    }
                },
                actions = {
                    IconButton( onClick = {
                        groupActions.onExitOrDelete()
                        navController.navigateUp()
                    }) {
                        if (groupState.isOwner) {
                            Icon(Icons.Default.Delete,"Elimina")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Logout,"Abbandona")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBottomBarWithFAB(
                firstOptionText = "Descrizione",
                firstOptionImage = Icons.Default.Book,
                secondOptionText = "Partecipanti",
                secondOptionImage = ImageVector.vectorResource(id = R.drawable.ic_groups),
                selectedIndex = groupState.selectedTab.ordinal,
                onTabSelected = { index ->  groupActions.onSelectTab(index) }
            ) {
                if (groupState.isOwner) {
                    Log.i("debug","Entra nel mio gruppo ${groupState.group?.name} " +
                            "inserendo il codice ${groupState.group?.inviteCode} " +
                            "nell'app EsameMobile")
                    sendMessageIntent(
                        context = context,
                        message = "Entra nel mio gruppo ${groupState.group?.name} " +
                                "inserendo il codice ${groupState.group?.inviteCode} " +
                                "nell'app EsameMobile", //TODO(CAMBIA NOME APP)
                        title = "Condividi codice invito")
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
                            ChangeImageCard(
                                context = context,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.5f),
                                useUri = groupActions.onUpdateGroupPhoto
                            ) {
                                ImageWithPlaceholder(groupState.group.imageUrl, Size.Lg)

                                if (groupState.isEditing) {
                                    OutlinedTextField(
                                        value = groupState.tempName,
                                        onValueChange = groupActions.onChangeName,
                                        label = {Text("Nome gruppo")}
                                    )
                                } else {
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
                                Text(groupState.master?.username ?: "Anonimo")
                                if (groupState.isOwner) {
                                    SessionDateButton(
                                        dateText = formatDate(groupState.group.nextSession) ?: "Nessuna sessione prevista",
                                        onDateSelected = groupActions.onChangeSessionDate
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            addEventToCalendar(
                                                context = context,
                                                title = "Sessione ${groupState.group.name}",
                                                startTime = groupState.group.nextSession!!.toDate().time
                                            )
                                        },
                                        enabled = groupState.group.nextSession != null
                                    ) {
                                        Text(formatDate(groupState.group.nextSession) ?: "Nessuna sessione prevista")
                                    }
                                }

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
                                if (groupState.isEditing) {
                                    OutlinedTextField(
                                        value = groupState.tempDesc,
                                        onValueChange = groupActions.onChangeDescription,
                                        label = {Text("Descrizione")}
                                    )
                                } else {
                                    Text(groupState.group.description)
                                }
                            }
                            if (groupState.isOwner) {
                                Row {
                                    if (groupState.isEditing) {
                                        IconButton(
                                            onClick = groupActions.toggleEdit,
                                            modifier = Modifier
                                                .weight(0.5f)
                                                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                            ) {
                                            Row() {
                                                Icon(Icons.Default.Cancel,"Annulla")
                                                Text("Annulla")
                                            }
                                        }
                                        IconButton(
                                            onClick = groupActions.onSaveChange,
                                            modifier = Modifier
                                                .weight(0.5f)
                                                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                        ) {
                                            Row() {
                                                Icon(Icons.Default.Save,"Salva modifiche")
                                                Text("Salva modifiche")
                                            }
                                        }
                                    } else {
                                        IconButton(
                                            onClick = groupActions.toggleEdit,
                                            modifier = Modifier
                                                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                                                .fillMaxWidth()
                                        ) {
                                            Row() {
                                                Icon(Icons.Default.Edit,"Modifica")
                                                Text("Modifica")
                                            }
                                        }
                                    }
                                }
                                InviteCodeRow(context,groupState.group.inviteCode, Modifier.weight(0.1f))
                            }
                        }
                        GroupDetailsTab.MEMBERS -> {
                            LazyColumn {
                                items(groupState.members) { user ->
                                    ExpandableListItem(user) {
                                        user.characterId?.let { charId ->
                                            navController.navigate(EsameMobileRoute.CharacterDetails(
                                                charId,
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

    Column {
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

@Composable
fun InviteCodeRow(
    context: Context,
    inviteCode: String,
    modifier: Modifier
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        SelectionContainer {
            Text("Codice Invito:\n $inviteCode")
        }
        Spacer(Modifier.width(5.dp))
        IconButton(onClick = {
            scope.launch {
                clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("invite code", inviteCode)))
                Toast.makeText(context,"Codice invito copiato", Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(Icons.Default.ContentCopy, "Copia codice invito")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDateButton(
    dateText: String,
    onDateSelected: (Timestamp) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    Button( {showDatePicker = true} ) {
        Text(dateText)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Conferma")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 12,
            initialMinute = 0,
            is24Hour = true
        )

        BasicAlertDialog(
            onDismissRequest = { showTimePicker = false }
        ) {
            Column() {
                TimePicker(timePickerState)
                Row() {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Annulla")
                    }
                    TextButton(onClick = {
                        selectedDateMillis?.let { dateMillis ->
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = dateMillis
                            }

                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            val localCalendar = Calendar.getInstance().apply {
                                set(year,month,day,timePickerState.hour,timePickerState.minute)
                            }

                            val finalDate = localCalendar.time
                            val timestamp = Timestamp(finalDate)

                            onDateSelected(timestamp)
                        }
                        showTimePicker = false
                    }) {
                        Text("Conferma")
                    }
                }
            }
        }


    }
}
private fun formatDate(timestamp: Timestamp?): String? {
    if (timestamp == null) return null
    val instant = timestamp.toDate().toInstant()
    val localDate = instant.atZone(ZoneId.systemDefault())
    return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}