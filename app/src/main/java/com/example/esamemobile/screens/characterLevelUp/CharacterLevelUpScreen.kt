package com.example.esamemobile.screens.characterLevelUp

import android.graphics.Paint
import android.text.Layout
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.esamemobile.R
import com.example.esamemobile.data.calculateModifier
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

    val currentState = state ?: return
    val currentStep = currentState.currentStep

    var showEditDescriptionDialog by remember { mutableStateOf(false) }

    if (showEditDescriptionDialog && currentState.selectedOption == LevelUpOption.UPGRADE_ABILITY) {

    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { actions.onBackStep(onNavigateBack) },
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(
                        if (currentStep == LevelUpStep.CHOOSE_CLASS || currentStep == LevelUpStep.CHOOSE_PERK_TYPE && currentState.character?.level != 0) "Annulla"
                        else "< Indietro"
                    )
                }

                val isFinalStep = when (currentStep) {
                    LevelUpStep.EDIT_STATISTICS, LevelUpStep.EDIT_ABILITIES -> true
                    LevelUpStep.CHOOSE_PERK_TYPE -> {
                        currentState.selectedOption == LevelUpOption.GAIN_PE_CHAR_3 ||
                                currentState.selectedOption == LevelUpOption.GAIN_PE_CHAR_5
                    }
                    else -> false
                }


                if (isFinalStep) {
                    Button(
                        onClick = { actions.onConfirmLevelUp(context, onNavigateBack) },
                        enabled = currentState.isCurrentStepValid,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Conferma", fontSize = 16.sp)
                    }
                } else {
                    Button(
                        onClick = { actions.onNextStep() },
                        enabled = currentState.isCurrentStepValid,
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
    val isSubClassSelection = state.currentLevel == 6

    val filteredClasses = if (isSubClassSelection && state.character != null) {
        allClasses.filter { it.id != state.character.chosenClass }
    } else {
        allClasses
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (!isSubClassSelection) "Seleziona Classe" else "Seleziona Sotto-Classe",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (isSubClassSelection) {
                "Scegli la tua sottoclasse di specializzazione avanzata.\n\n" +
                        "Dalla classe scelta potrai imparare una abilità di base in aggiunta a quelle che hai già dalla tua classe principale."
            } else {
                "Di seguito, puoi visualizzare l'elenco di tutte le classi con relative abilitò di base e avanzate per aiutarti a scegliere una classe consapevolmente."
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        filteredClasses.forEach { gameClass ->
            val isExpanded = expandedClassId == gameClass.id
            val isSelected = if (isSubClassSelection) state.selectedSubClassId == gameClass.id else state.selectedClassId == gameClass.id

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
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
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

                            if (!isSubClassSelection) {
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
            filteredClasses.forEach { gameClass ->
                val isSelected = if (isSubClassSelection) state.selectedSubClassId == gameClass.id else state.selectedClassId == gameClass.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (isSubClassSelection) actions.onSelectedSubClass(gameClass.id) else actions.onSelectedClass(gameClass.id) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { if (isSubClassSelection) actions.onSelectedSubClass(gameClass.id) else actions.onSelectedClass(gameClass.id) }
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
    val strengthValue = state.character?.strength ?: 0
    val strengthModifier = calculateModifier(strengthValue)
    val charismaValue = state.character?.charisma ?: 0
    val charismaModifier = calculateModifier(charismaValue)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Incremento HP e Scelta del Perk di Livello",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        //.::PARTE 1: ROLL HP::.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Incrememnto HP",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Lancia il dado per determinare di quanti HP aumenterà il massimo dei tuoi HP (1d6 + Modificatore di Forza)",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val hpRollValue = state.hpRolled
                val pureDice = state.pureDiceRoll
                val currentMaxHp = state.character?.maxHP ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "HP Attuali: $currentMaxHp",
                            fontSize = 14.sp
                        )
                        if (hpRollValue != null && pureDice != null) {
                            val totalNewHp = currentMaxHp + hpRollValue
                            Text(
                                "Nuovi HP totali: $totalNewHp",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Dettaglio: $currentMaxHp (Base) + $pureDice (Dado) + $strengthModifier (Modificatore di Forza) = $totalNewHp",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    if (hpRollValue == null) {
                        Card(
                            modifier = Modifier
                                .clickable { actions.onRollHp() }
                                .padding(4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_dice),
                                    contentDescription = "Lancia il dado",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "+$hpRollValue HP",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        //.::PARTE 2: SCELTA BONUS::.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "2. Seleziona un Perk di Livello",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Attenzione: puoi selezionare ciascuna opzione una sola volta per livello.\n" +
                        "Se un'opzione appare più volte significa che quella scelta può essere ripetuta più volte nel corso dei diversi livelli.\n\n" +
                        "Scegli il bonus che preferisci ottenere:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                state.availableOptions.forEachIndexed { index, availableOption ->
                    val isSelected = state.selectedOption == availableOption

                    val label = when (availableOption) {
                        LevelUpOption.STAT_BONUS_2 -> "+2 a una statistica (massimo 10)"
                        LevelUpOption.UPGRADE_ABILITY -> "Potenzia un potere evoluzione"
                        LevelUpOption.BASE_CLASS_ABILITY -> "Nuova abilità dalla tua classe"
                        LevelUpOption.ADVANCED_CLASS_ABILITY -> "Nuova abilità avanzata dalla tua classe"
                        LevelUpOption.NEW_CLASS_BASE_ABILITY -> "Nuova abilità base da un'altra classe"
                        LevelUpOption.GAIN_PE_CHAR_3 -> "Guadagna ${3 + charismaModifier} PE"
                        LevelUpOption.GAIN_PE_CHAR_5 -> "Guadagna ${5 + charismaModifier} PE"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actions.onSelectedOption(availableOption) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { actions.onSelectedOption(availableOption) }
                        )
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(start = 8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditStatContent(
    state: LevelUpState,
    actions: LevelUpActions
) {
    val character = state.character ?: return

    val stats = listOf(
        AbilityItem(id = "Forza", name = "Forza", numericValue = character.strength),
        AbilityItem(id = "Agilità", name = "Agilità", numericValue = character.agility),
        AbilityItem(id = "Intelligenza", name = "Intelligenza", numericValue = character.intelligence),
        AbilityItem(id = "Carisma", name = "Carisma", numericValue = character.charisma),
        AbilityItem(id = "Potere", name = "Potere", numericValue = character.power)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Potenziamento Caratteristica di +2 (max 10)",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            stats.forEach { stat ->
                val isSelected = state.selectedStatToUpgrade == stat.id
                val potentialValue = state.getStatPreview(stat.id)
                val isStatMaxed = stat.numericValue >= 10

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isStatMaxed) {
                            actions.onSelectStatToUpgrade(stat.id)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isStatMaxed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isStatMaxed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                            )
                            if (isStatMaxed) {
                                Text(
                                    text = "Punteggio massimo (10)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "Attuale: ${stat.numericValue} -> Nuovo: $potentialValue",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val activeValue = if (isSelected) potentialValue else stat.numericValue
                        val modifier = calculateModifier(activeValue)
                        val modifierText = if (modifier >= 0) "+$modifier" else "$modifier"

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Mod: $modifierText",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { actions.onSelectStatToUpgrade(stat.id) },
                                enabled = !isStatMaxed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditAbilitiesContent(
    state: LevelUpState,
    actions: LevelUpActions
) {
    val character = state.character ?: return
    val allClasses = state.gameClasses

    val optionChosen = state.selectedOption ?: when {
        character.level == 0 -> LevelUpOption.BASE_CLASS_ABILITY
        state.currentLevel == 6 -> LevelUpOption.NEW_CLASS_BASE_ABILITY
        else -> return
    }

    val classIdToSearch = if (character.level == 0) state.selectedClassId else character.chosenClass
    val primaryClass = allClasses.find { it.id.equals(classIdToSearch, ignoreCase = true) }

    val subClassId = state.selectedSubClassId ?: character.classAbilitiesList.find { it.startsWith("SUBCLASS_", ignoreCase = true) }?.substringAfter("SUBCLASS_")
    val subClass = allClasses.find { it.id.equals(subClassId, ignoreCase = true) }

    val titleText: String
    val subtitleText: String
    val listToDisplay = mutableListOf<Pair<String, String>>()

    val learnedAbilitiesIds = character.classAbilitiesList.map { it.trim().lowercase() }

    when (optionChosen) {
        LevelUpOption.BASE_CLASS_ABILITY -> {
            titleText = "Seleziona un'Abilità Base"
            subtitleText = "Scegli una nuova abilità di base dalla classe ${primaryClass?.name ?: ""}:"

            primaryClass?.baseAbilities?.forEach { ability ->
                if (!learnedAbilitiesIds.contains(ability.id.trim().lowercase())) {
                    listToDisplay.add(Pair(ability.id, "${ability.name}\n${ability.description}"))
                }
            }
        }

        LevelUpOption.ADVANCED_CLASS_ABILITY -> {
            titleText = "Seleziona un'Abilità Avanzata"
            subtitleText = "Hai sbloccato l'accesso alle tecniche superiori della classe ${primaryClass?.name ?: ""}:"

            primaryClass?.advancedAbilities?.forEach { ability ->
                if (!learnedAbilitiesIds.contains(ability.id.trim().lowercase())) {
                    listToDisplay.add(Pair(ability.id, "${ability.name}\n${ability.description}"))
                }
            }
        }

        LevelUpOption.NEW_CLASS_BASE_ABILITY -> {
            titleText = "Seleziona un'Abilità dalla Sotto-Classe"
            subtitleText = "Scegli un'abilità di base dalla tua sottoclasse di specializzazione ${subClass?.name ?: ""}:"

            val targetSubClass = subClass ?: allClasses.find { it.id.equals(character.chosenClass, ignoreCase = true) }

            targetSubClass?.baseAbilities?.forEach { ability ->
                val isAlreadyLearned = learnedAbilitiesIds.any { learnedId ->
                    learnedId.contains(ability.id.trim().lowercase()) || ability.id.trim().lowercase().contains(learnedId)
                }

                if (!isAlreadyLearned) {
                    listToDisplay.add(Pair(ability.id, "${ability.name}\n${ability.description}"))
                }
            }

            if (listToDisplay.isEmpty() && targetSubClass != null) {
                targetSubClass.baseAbilities.forEach { ability ->
                    listToDisplay.add(Pair(ability.id, "${ability.name}\n${ability.description}"))
                }
            }
        }

        LevelUpOption.UPGRADE_ABILITY -> {
            titleText = "Potenzia un Potere Evoluzione"
            subtitleText = "Seleziona uno dei tuoi poteri evoluzione attuali per potenziarlo:"

            character.abilitiesList.forEach { abilityItem ->
                listToDisplay.add(
                    Pair(
                        abilityItem.name,
                        "${abilityItem.name}\n${abilityItem.description}"
                    )
                )
            }

//            val allGameClassAbilitiesNames = allClasses.flatMap { it.baseAbilities + it.advancedAbilities }
//
//            character.classAbilitiesList.forEach { learnedAbilityRaw ->
//                val learnedAbilityId = learnedAbilityRaw.trim().lowercase()
//                val isSystemTag = learnedAbilityId.startsWith("subclass_") ||
//                        learnedAbilityId == "bonus_pe_3" ||
//                        learnedAbilityId == "bonus_pe_5" ||
//                        learnedAbilityId == "upgrade_ability" ||
//                        learnedAbilityId == "bonus_stat_2"
//
//                if (!isSystemTag && !learnedAbilityId.endsWith("+")) {
//                    val matchingAbility = allGameClassAbilitiesNames.find { it.id.trim().lowercase() == learnedAbilityId }
//                    if (matchingAbility != null) {
//                        listToDisplay.add(
//                            Pair(learnedAbilityRaw,
//                                "Potenzia ${matchingAbility.name} in ${matchingAbility.name}+.\nOriginale: ${matchingAbility.description}")
//                        )
//                    }
//                }
//            }
        }
        else -> return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = titleText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitleText,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        if (listToDisplay.isEmpty()) {
            Text(
                "Non ci sono abilità disponibili da selezionare.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listToDisplay.forEach { (abilityId, fullNameAndDesc) ->
                    val targetId = abilityId
                    val isSelected = state.selectedAbilityToUpgrade == targetId

                    val parts = fullNameAndDesc.split("\n", limit = 2)
                    val displayName = parts.getOrNull(0) ?: abilityId
                    val displayDesc = parts.getOrNull(1) ?: ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actions.onSelectAbilityToUpgrade(targetId) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (optionChosen == LevelUpOption.UPGRADE_ABILITY) "$displayName -> $displayName+" else displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = displayDesc,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { actions.onSelectAbilityToUpgrade(targetId) }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = optionChosen == LevelUpOption.UPGRADE_ABILITY && state.selectedAbilityToUpgrade != null
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Personalizza gli effetti del potenziamento:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = state.customAbilityDescription,
                            onValueChange = { actions.onUpdateAbilityDescription(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Scrivi qui come cambia la descrizione dell'abilità...") },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}