package com.example.esamemobile.screens.characterDetails

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import com.example.esamemobile.EsameMobileRoute
import com.example.esamemobile.data.staticData.ClassAbility
import com.example.esamemobile.screens.characterCreation.AbilityItem
import com.example.esamemobile.screens.characterCreation.InventoryItem
import com.example.esamemobile.utilities.CharacterDetailsNavigationBar
import com.example.esamemobile.utilities.CharacterHeader
import com.example.esamemobile.utilities.DisplayableItem
import com.example.esamemobile.utilities.GenericBasicDialog
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailsScreen(
    detailsState: CharacterDetailsState,
    detailsActions: CharacterDetailsActions,
    navController: NavHostController,
    onNavigateToLevelup: () -> Unit
) {

    val context = LocalContext.current
    var deleting by mutableStateOf(false)

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        detailsActions.onLoad()
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("")},
                navigationIcon = {
                    IconButton(onClick = {
                        detailsActions.onScreenExit()
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,"Indietro")
                    }
                },
                actions = {
                    detailsActions.onDelete?.let {
                        IconButton({}) {
                            Icon(Icons.Outlined.Star,"Inserisci tra i preferiti")
                        }
                        IconButton({ deleting = true }) {
                            Icon(Icons.Default.Delete,"Elimina")
                        }
                    }
                }
            )
        },
        bottomBar = {
            CharacterDetailsNavigationBar(
                selectedIndex = detailsState.selectedTab.ordinal,
                onTabSelected = detailsActions.onTabSelected
            )
        }
    ) { innerPadding ->

        if (detailsState.ageMalusDialog) {
            GenericBasicDialog(
                show = true,
                title = detailsState.character!!.ageMalus!!.name,
                description = detailsState.character.ageMalus.desc,
                onConfirm = detailsActions.onMalusButton
            )
        }

        detailsActions.onDelete?.let {
            GenericBasicDialog(
                show = deleting,
                title = "Eliminare personaggio",
                description = "Sei sicuro di volere eliminare il personaggio?",
                onConfirmText = "Elimina",
                onConfirm = {
                    detailsActions.onDelete()
                    navController.navigateUp()
                            },
                onDismissText = "Annulla",
                onDismiss = { deleting = false }
            )
        }


        when {
            detailsState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            (detailsState.character == null) -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    Text("Errore nel caricamento personaggio", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    CharacterHeader(
                        name = detailsState.character.character.name,
                        age = detailsState.character.character.age,
                        ageMalusSign = detailsState.malusDrawableId,
                        characterClass = detailsState.character.chosenClass?.name,
                        level = detailsState.character.character.level,
                        imageUrl = detailsState.character.character.imageUrl,
                        onMalusClick = detailsActions.onMalusButton,
                        modifier = Modifier,
                        onLevelUpClick = detailsActions.onLevelUp?.let { {
                                detailsActions.onLevelUp()
                                onNavigateToLevelup()
                            } }
                        )

                    Spacer(Modifier.height(8.dp))

                    when (detailsState.selectedTab) {
                        CharacterDetailsTab.STATS  -> {
                            StatSection(
                                hp = detailsState.character.character.currentHP,
                                maxHp = detailsState.character.character.maxHP,
                                onDecrease = detailsActions.onDecreaseHp,
                                onIncrease = detailsActions.onIncreaseHp,
                                stats = detailsState.character.stats,
                                speed = detailsState.character.character.speed,
                                armor = detailsState.character.character.armor.text,
                                normalizedStats = detailsState.character.normalizedStats
                            )
                        }

                        CharacterDetailsTab.POWERS ->  {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                EvolutionPowersSection(
                                    abilities = detailsState.character.character.abilitiesList,
                                    modifier = Modifier.weight(1f),
                                    onAddPower = detailsActions.onAddPower?.let { { detailsActions.onAddPower(context) } }
                                )
                                AbilitiesSection(
                                    abilities = detailsState.character.classAbilities,
                                    usageCurrent = detailsState.abilityUsageCurrent,
                                    usageMax = detailsState.abilityUsageMax,
                                    modifier = Modifier.weight(1f),
                                    onDecreaseUsage = detailsActions.onDecreaseUsage,
                                    onIncreaseUsage = detailsActions.onIncreaseUsage
                                )
                            }
                        }

                        CharacterDetailsTab.INVENTORY ->  {
                            InventorySection(
                                items = detailsState.character.character.inventoryList,
                                capacityCurrent = detailsState.character.character.inventoryList.sumOf { it.numericValue }, // Se numeric value è il peso
                                capacityMax = detailsState.character.character.maxCapacity,
                                onAddItem = detailsActions.onAddItem?.let { { detailsActions.onAddItem(context) } },
                                onUseItem = detailsActions.onUseItem
                            )
                        }
                    }

                }
            }
        }


    }
}

@Composable
private fun PowersHeader(
    title: String,
    onAddClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    titleWeight: Float = 0.85f
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(titleWeight)
        )
        onAddClick?.let {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.weight(1f - titleWeight)
            ) {
                Icon(Icons.Default.Add, contentDescription = "aggiungi", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun CountRow(
    title: String,
    current: Int,
    max: Int,
    onDecrease: (() -> Unit)?,
    onIncrease: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            onDecrease?.let {
                IconButton(onClick = onDecrease, modifier = Modifier.weight(0.1f)) {
                    Icon(Icons.Default.Remove, contentDescription = "togli un uso", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                "$current/$max",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            onIncrease?.let {
                IconButton(onClick = onIncrease, modifier = Modifier.weight(0.1f)) {
                    Icon(Icons.Default.Add, contentDescription = "aggiungi un uso", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun InventoryHeader(
    current: Int,
    max: Int,
    onAddClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "INVENTARIO",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Capacità $current/$max",
                style = MaterialTheme.typography.bodyMedium,
                color = if (current > max) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        onAddClick?.let {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "nuovo oggetto", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EvolutionPowersSection(
    abilities: List<AbilityItem>,
    modifier: Modifier,
    onAddPower: (() -> Unit)?
) {
    Column(modifier = modifier) {
        PowersHeader(
            title = "POTERI EVOLUZIONE",
            onAddClick = onAddPower,
            titleWeight = 0.9f
        )
        Spacer(Modifier.height(4.dp))
        ListItems(
            elements = abilities,
            modifier =  Modifier.fillMaxWidth().weight(1f),
            checkEnhanched = true,
            costText = { item ->
                Text(
                    "${item.numericValue} PE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}

@Composable
private fun AbilitiesSection(
    abilities: List<ClassAbility>,
    usageCurrent: Int,
    usageMax: Int,
    modifier: Modifier,
    onDecreaseUsage: (() -> Unit)?,
    onIncreaseUsage: (() -> Unit)?
) {
    Column(modifier = modifier) {
        CountRow(
            title = "ABILITA'",
            current = usageCurrent,
            max = usageMax,
            onDecrease = onDecreaseUsage,
            onIncrease = onIncreaseUsage
        )
        Spacer(Modifier.height(4.dp))
        ListItems(
            elements = abilities.map {
                AbilityItem(
                    name = it.name,
                    description = it.description,
                    numericValue = 0) }, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun InventorySection(
    items: List<InventoryItem>,
    capacityCurrent: Int,
    capacityMax: Int,
    onAddItem: (() -> Unit)?,
    onUseItem: ((InventoryItem) -> Unit)?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        InventoryHeader(
            current = capacityCurrent,
            max = capacityMax,
            onAddClick = onAddItem
        )
        Spacer(Modifier.height(8.dp))
        ListItems(
            elements = items,
            modifier = Modifier.fillMaxWidth().weight(1f),
            onUseItem = onUseItem,
            costText = { item ->
                Text(
                    "Peso: ${item.numericValue}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Composable
private fun StatSection(
    hp: Int,
    maxHp: Int,
    onDecrease: (() -> Unit)?,
    onIncrease: (() -> Unit)?,
    stats: List<Int>,
    speed: Double,
    armor: String,
    normalizedStats: List<Float>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            CountRow("HP", hp, maxHp, onDecrease, onIncrease)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp)),
                progress = { if (maxHp > 0) hp.toFloat()/maxHp else 0f },
                color = if (hp < maxHp * 0.3f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Velocità", style = MaterialTheme.typography.bodyMedium)
                Text("${speed} m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Armatura", style = MaterialTheme.typography.bodyMedium)
                Text(armor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

    }

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedCard(
            modifier = Modifier.weight(1.1f),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Statistiche", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val labels = listOf("Forza", "Agilità", "Intelligenza", "Carisma", "Potere")
                stats.forEachIndexed { index, value ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            labels[index],
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            "$value (${if (calculateModifier(value) >= 0) "+" else ""}${calculateModifier(value)})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(0.9f),
            contentAlignment = Alignment.Center
        ) {
            statChart(
                normalizedStats,
                listOf("FOR","AGI","INT","CAR","POT"),
                lineColor = MaterialTheme.colorScheme.primary,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        }
    }
}


@Composable
private fun ListItems(
    elements: List<DisplayableItem>,
    modifier: Modifier,
    checkEnhanched: Boolean = false,
    costText: @Composable (DisplayableItem) -> Unit = { },
    onUseItem: ((InventoryItem) -> Unit)? = null) {
    var selectedItem by remember { mutableStateOf<DisplayableItem?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(elements) { element ->
            val isHighLighted = checkEnhanched && element.name.endsWith("+")
            GenericListElement(
                item = element,
                onClick = { selectedItem = element },
                isHighlighted = isHighLighted,
                costText = costText
            )
        }
    }

    selectedItem?.let { item ->
        val usable = item as? InventoryItem

        GenericBasicDialog(
            show = true,
            title = item.name,
            description = item.description,
            onConfirm = {
                if (usable != null && onUseItem != null) {
                    onUseItem(item)
                }
                selectedItem = null
            },
            onConfirmText = if (usable != null && onUseItem != null) "Usa" else "Chiudi",
            onDismissText = if (usable != null && onUseItem != null) "Chiudi" else null,
            onDismiss = if (usable != null && onUseItem != null) { {selectedItem = null} } else null
        )
    }
}

@Composable
private fun GenericListElement(
    item: DisplayableItem,
    onClick: () -> Unit,
    isHighlighted: Boolean,
    costText: @Composable (DisplayableItem) -> Unit = { }
) {
    val cardColors = if (isHighlighted) {
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    } else {
        CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    }

    val modifierWithEffects = if (isHighlighted) {
        Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    }

    OutlinedCard(
        modifier = modifierWithEffects,
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.width(8.dp))

            costText(item)
        }
    }
    }

private fun calculateModifier(stat: Int): Int {
    return when (stat) {
        in Int.MIN_VALUE..1 -> -1
        in 2..3 -> 0
        in 4..5 -> 1
        in 6..7 -> 2
        in 8..9 -> 3
        else -> 4
    }
}

@Composable
fun statChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    fillColor: Color
) {
    require(values.size == labels.size)
    val sides = values.size
    val textMeasurer = rememberTextMeasurer()

    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val labelColor = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(24.dp)
    ) {
        val center = Offset(size.width / 2,size.height / 2)
        val radius = size.minDimension / 2
        val vertexAngle = (2* Math.PI / sides)

        //Disegna i pentagoni concentrici per ora 4 pentagoni concentrici
        for (level in 1..4) {
            val pentagonLevel = radius * level / 4
            val path = Path()
            for (i in 0 until sides) {
                val angle = -Math.PI / 2 + i*vertexAngle
                val point = Offset(
                    x = center.x + (pentagonLevel * cos(angle)).toFloat(),
                    y = center.y + (pentagonLevel * sin(angle)).toFloat()
                )
                if (i == 0) path.moveTo(point.x,point.y) else path.lineTo(point.x,point.y)
            }
            path.close()
            drawPath(path, gridColor ,style = Stroke(width = 1.dp.toPx()))
        }

        //Disegna una riga dal centro al vertice del pentagono
        for (i in 0 until sides) {
            val angle = -Math.PI / 2 + vertexAngle * i
            val end = Offset(
                x = center.x + (radius * cos(angle)).toFloat(),
                y = center.y + (radius * sin(angle)).toFloat()
            )
            drawLine(gridColor, center, end, strokeWidth = 1.dp.toPx())
        }

        val dataPath = Path()
        values.forEachIndexed { i, value ->
            val angle = -Math.PI / 2 + i * vertexAngle
            val point = Offset(
                x = center.x + (radius * value * cos(angle)).toFloat(),
                y = center.y + (radius * value * sin(angle)).toFloat()
            )
            if (i == 0) dataPath.moveTo(point.x,point.y) else dataPath.lineTo(point.x,point.y)
        }
        dataPath.close()

        drawPath(dataPath,fillColor)
        drawPath(dataPath,lineColor, style = Stroke(width = 2.dp.toPx()))

        //Disegna le etichette sui vertici
        for (i in 0 until sides) {
            val angle = -Math.PI / 2 + i * vertexAngle
            val labelRadius = radius + 14.dp.toPx()
            val labelPosition = Offset(
                x = center.x + (labelRadius * cos(angle)).toFloat(),
                y = center.y + (labelRadius * sin(angle)).toFloat()
            )

            val textLayoutResult = textMeasurer.measure(
                text = labels[i],
                style = TextStyle(fontSize = 11.sp, color = labelColor, fontWeight = FontWeight.Bold)
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = labelPosition.x - textLayoutResult.size.width / 2,
                    y = labelPosition.y - textLayoutResult.size.height / 2
                )
            )
        }
    }
}