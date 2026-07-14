package com.example.esamemobile.screens.groupDetails

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.R
import com.example.esamemobile.data.Member
import com.example.esamemobile.utilities.composables.NavigationBottomBarWithFAB
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

    val scrollState = rememberScrollState()

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
                title = {Text(groupState.group?.name ?: "Dettagli Gruppo", style = MaterialTheme.typography.titleMedium)},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
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
                            Icon(Icons.Default.Delete,"Elimina", tint = MaterialTheme.colorScheme.error)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Logout,"Abbandona", tint = MaterialTheme.colorScheme.error)
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
                                "nell'app M.U.T.A",
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
                    modifier = Modifier.fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (groupState.selectedTab) {
                        GroupDetailsTab.DESCRIPTION -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(180.dp)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            //Immagine e nome gruppo
                                            ChangeImageCard(
                                                context = context,
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                useUri = groupActions.onUpdateGroupPhoto
                                            ) {
                                                ImageWithPlaceholder(
                                                    groupState.group.imageUrl,
                                                    Size.Lg
                                                )
                                            }
                                        }

                                        if (groupState.isEditing) {
                                            OutlinedTextField(
                                                value = groupState.tempName,
                                                onValueChange = groupActions.onChangeName,
                                                label = { Text("Nome gruppo") },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true
                                            )
                                        } else {
                                            Text(
                                                groupState.group.name,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )

                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                "Game Master (GM)",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(end = 4.dp)
                                                )
                                                Text(
                                                    text = groupState.master?.username
                                                        ?: "[REDACTED]",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp))

                                        Column(
                                            modifier = Modifier.weight(1.2f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = "Prossima Sessione",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            if (groupState.isOwner) {
                                                SessionDateButton(
                                                    dateText = formatDate(groupState.group.nextSession)
                                                        ?: "Programma",
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
                                                    enabled = groupState.group.nextSession != null,
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    )
                                                ) {
                                                    Icon(
                                                        Icons.Default.Event,
                                                        null,
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    )
                                                    Text(
                                                        formatDate(groupState.group.nextSession)
                                                            ?: "Nessuna sessione prevista"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }


                                Text(
                                    "Descrizione Gruppo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                                ) {
                                    Box(modifier = Modifier.padding(16.dp)) {
                                        if (groupState.isEditing) {
                                            OutlinedTextField(
                                                value = groupState.tempDesc,
                                                onValueChange = groupActions.onChangeDescription,
                                                label = { Text("Scrivi una descrizione...") },
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 3
                                            )
                                        } else {
                                            Text(
                                                groupState.group.description.ifBlank { "Nessuna descrizione inserita." },
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                if (groupState.isOwner) {
                                    InviteCodeCard(context, groupState.group.inviteCode)
                                }
                                if (groupState.isOwner) {
                                    Spacer(Modifier.height(8.dp))
                                    if (groupState.isEditing) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = groupActions.toggleEdit,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Icon(Icons.Default.Cancel, "Annulla")
                                                Spacer(Modifier.width(8.dp))
                                                Text("Annulla")
                                            }

                                            Button(
                                                onClick = groupActions.onSaveChange,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.Save,
                                                    "Salva",
                                                    tint = MaterialTheme.colorScheme.onPrimary)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Salva", color = MaterialTheme.colorScheme.onPrimary)
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = groupActions.toggleEdit,
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        ) {
                                            Icon(Icons.Default.Edit, "Modifica")
                                            Spacer(Modifier.width(8.dp))
                                            Text("Modifica Gruppo")
                                        }
                                    }
                                }
                            }
                        }

                        GroupDetailsTab.MEMBERS -> {
                            if (groupState.members.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        modifier = Modifier.height(64.dp).width(64.dp),
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Non ci sono membri presenti, invita altri utenti con il codice invito." +
                                                " Puoi inviare direttamente il codice premendo il tasto +",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(groupState.members) { user ->
                                        ExpandableListItem(user) {
                                            user.characterId?.let { charId ->
                                                navController.navigate(EsameMobileRoute.CharacterDetails(
                                                    charId,
                                                    groupActions.onCharacterClick(user.userId),
                                                    user.userId
                                                )
                                                )
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
}

@Composable
fun ExpandableListItem(
    user: Member,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column (
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        expanded = !expanded
                    }),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ImageWithPlaceholder(user.userImgUrl, Size.Sm)
                    Text(
                        user.username,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Comprimi" else "Espandi"
                )
            }

                AnimatedVisibility(visible = expanded) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        if (user.characterName.isNullOrBlank()) {
                            Text(
                                "Personaggio non inserito, per inserirlo premere il pulsante +",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            ListItem(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onClick)
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                headlineContent = { Text(user.characterName, fontWeight = FontWeight.Bold) },
                                supportingContent = { Text("Vedi scheda personaggio", color = MaterialTheme.colorScheme.primary) },
                                leadingContent = { ImageWithPlaceholder(user.characterImgUrl, Size.Sm) }
                            )
                        }
                    }
                }
        }
    }
}

@Composable
fun InviteCodeCard(
    context: Context,
    inviteCode: String
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Codice Invito:\n $inviteCode", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(4.dp))
                SelectionContainer {
                    Text(
                        inviteCode,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            IconButton(
                onClick = {
                    scope.launch {
                        clipboardManager.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "invite code",
                                    inviteCode
                                )
                            )
                        )
                        Toast.makeText(context, "Codice invito copiato", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(
                        alpha = 0.1f
                    )
                )
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    "Copia codice invito",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
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

        Button({ showDatePicker = true }) {
            Text(dateText)
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
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
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text("Seleziona l'orario", style = MaterialTheme.typography.titleMedium)
                        TimePicker(timePickerState)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Annulla")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                selectedDateMillis?.let { dateMillis ->
                                    val calendar =
                                        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                            timeInMillis = dateMillis
                                        }

                                    val year = calendar.get(Calendar.YEAR)
                                    val month = calendar.get(Calendar.MONTH)
                                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                                    val localCalendar = Calendar.getInstance().apply {
                                        set(
                                            year,
                                            month,
                                            day,
                                            timePickerState.hour,
                                            timePickerState.minute
                                        )
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
    }

    private fun formatDate(timestamp: Timestamp?): String? {
        if (timestamp == null) return null
        val instant = timestamp.toDate().toInstant()
        val localDate = instant.atZone(ZoneId.systemDefault())
        return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }