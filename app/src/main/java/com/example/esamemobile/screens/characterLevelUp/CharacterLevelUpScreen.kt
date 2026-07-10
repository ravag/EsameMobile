package com.example.esamemobile.screens.characterLevelUp

import android.text.Layout
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.data.staticData.GameClass
import com.example.esamemobile.screens.characterCreation.AbilityItem
import com.example.esamemobile.utilities.GenericStepContent
import java.util.logging.Level

@Composable
fun LevelUpScreen(
    charId: String,
    viewModel: LevelUpViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(charId) {
        viewModel.initLevelUp(charId)
    }

    val state by viewModel.state.collectAsState()
    val actions = viewModel.actions

    val currentState = state
    if (currentState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var currentStep by remember(currentState.currentStep) { mutableStateOf(currentState.currentStep) }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        when (currentStep) {
                            LevelUpStep.CHOOSE_CLASS -> onNavigateBack()
                            LevelUpStep.CHOOSE_PERK_TYPE -> {
                                if (currentState.character?.level == 0) currentStep = LevelUpStep.CHOOSE_CLASS
                                else onNavigateBack()
                            }
                            LevelUpStep.EDIT_STATISTICS, LevelUpStep.EDIT_ABILITIES -> {
                                currentStep = LevelUpStep.CHOOSE_PERK_TYPE
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(
                        if (currentStep == LevelUpStep.CHOOSE_CLASS || currentStep == LevelUpStep.CHOOSE_PERK_TYPE && currentState.character?.level != 0) "Annulla"
                        else "< Indietro"
                    )
                }

                val isFinalStep = currentStep == LevelUpStep.EDIT_STATISTICS ||
                        currentStep == LevelUpStep.EDIT_ABILITIES ||
                        (
                                currentStep == LevelUpStep.CHOOSE_PERK_TYPE &&
                                currentState.selectedOption != null &&
                                currentState.selectedOption != LevelUpOption.STAT_BONUS_2
                                )
                if (isFinalStep) {
                    Button(
                        onClick = { actions.onConfirmLevelUp(context, onNavigateBack) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Conferma", fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            when (currentStep) {
                                LevelUpStep.CHOOSE_CLASS -> {
                                        currentStep = LevelUpStep.CHOOSE_PERK_TYPE
                                }
                                LevelUpStep.CHOOSE_PERK_TYPE -> {
                                    if (currentState.selectedOption == LevelUpOption.STAT_BONUS_2) {
                                        currentStep = LevelUpStep.EDIT_STATISTICS
                                    } else if (currentState.selectedOption == LevelUpOption.UPGRADE_ABILITY ||
                                        currentState.selectedOption == LevelUpOption.BASE_CLASS_ABILITY ||
                                        currentState.selectedOption == LevelUpOption.ADVANCED_CLASS_ABILITY ||
                                        currentState.selectedOption == LevelUpOption.NEW_CLASS_BASE_ABILITY) {
                                        currentStep = LevelUpStep.EDIT_ABILITIES
                                    }
                                }
                                else -> {}
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Avanti >")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "LEVEL UP: LIVELLO ${currentState.currentLevel}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            when (currentStep) {
                LevelUpStep.CHOOSE_CLASS -> {
                    ChooseClassContent(state = currentState, actions = actions, allClasses = currentState.gameClasses)
                }

                LevelUpStep.CHOOSE_PERK_TYPE -> {
                    ChoosePerkContent(state = currentState, actions = actions)
                }

                LevelUpStep.EDIT_STATISTICS -> {
                    EditStatContent(state = currentState, actions = actions)
                }

                LevelUpStep.EDIT_ABILITIES -> {
                    EditAbilitiesContent(state = currentState, actions = actions)
                }
            }
        }

    }
}

@Composable
fun ChooseClassContent(
    state: LevelUpState,
    actions: LevelUpActions,
    allClasses: List<GameClass>
) {
    var expandedClassId by remember { mutableStateOf<String?>(null) }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (state.selectedClassId == null) "Seleziona Classe" else "Seleziona Sotto-Classe",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Di seguito, puoi visualizzare l'elenco di tutte le classi con relative abilitò di base e avanzate per aiutarti a scegliere una classe consapevolmente.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        allClasses.forEach { gameClass ->
            val isExpanded = expandedClassId == gameClass.id
            val isSelected = state.selectedClassId == gameClass.id

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedClassId = if (isExpanded) null else gameClass.id
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = gameClass.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gameClass.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Riduci" else "Espandi",
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Text(
                                text = "Abilità di Base",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                            gameClass.baseAbilities.forEach { ability ->
                                ClassAbilityPreviewItem(name = ability.name, description = ability.description)
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Abilità Avanzate",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                            gameClass.advancedAbilities.forEach { ability ->
                                ClassAbilityPreviewItem(name = ability.name, description = ability.description)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))

        Text(
            "Fai la tua scelta:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            allClasses.forEach { gameClass ->
                val isSelected = state.selectedClassId == gameClass.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { actions.onSelectedClass(gameClass.id) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { actions.onSelectedClass(gameClass.id) }
                    )
                    Text(
                        text = gameClass.name,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassAbilityPreviewItem(name: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ChoosePerkContent(
    state: LevelUpState,
    actions: LevelUpActions
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Incremento HP e Scelta del Perk di Livello",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        //TODO: Parte 1: Interfaccia di roll del dado per nuovi HP massimi con magari preview dell'aggiornamento dei HP totali

        //TODO: Parte 2: Elenco delle opzioni di level up disponibili con breve disclaimer per avvertire che ognuna è selezionabile una singola volta
    }
}

@Composable
fun EditStatContent(
    state: LevelUpState,
    actions: LevelUpActions
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Potenziamento Caratteristica di +2 (max 10)",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        //TODO: Inserisci qui la versione semplificata del edit stat del character creation screen
    }
}

@Composable
fun EditAbilitiesContent(
    state: LevelUpState,
    actions: LevelUpActions
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Gestione e Sblocco Abilità",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        //TODO: Qui sarebbe bene replicare una sorta della edit abilities list in character details o character creation, col problema che se sceglie da una classe è diverso pk mi si deve aprire uno schermo dove vedo tutte le abilità della mia classe e scegliere quella che voglio da aggiungere se invece si sceglie di potenziare un'abilità deve farmi vedere solo le abilità non di una classe che un personaggio possiede e quindi farmela modificare
    }
}