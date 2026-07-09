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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun CharacterDetailsScreen(detailsState: CharacterDetailsState, detailsActions: CharacterDetailsActions, navController: NavHostController, onNavigateToLevelup: () -> Unit) {

    val context = LocalContext.current
    //var selectedIndex by remember { mutableStateOf(0) }

//    var abilities: List<Abilities> = listOf(Abilities("caio","wow caia",1),
//        Abilities("aaa","wow caia",3),
//        Abilities("bbb","wow caia",2),
//        Abilities("cccc","nel mezzo del cammin di nostra vita mi ritrovai per una selva oscura che la diretta via era smarrita, tanto ...",5))
//
//    var hp by remember { mutableStateOf(10) }
//    val maxHp = 10

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {Text("")},
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,"Indietro")
                    }
                },
                actions = {
                    IconButton({}) {
                        Icon(Icons.Outlined.Star,"Inserisci tra i preferiti")
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
                    Text("Errore nel caricamento personaggio")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CharacterHeader(
                        name = detailsState.character.character.name,
                        age = detailsState.character.character.age,
                        ageMalusSign = detailsState.malusDrawableId,
                        characterClass = detailsState.character.chosenClass?.name,
                        level = detailsState.character.character.level,
                        imageUri = detailsState.character.character.imageUri,
                        onMalusClick = detailsActions.onMalusButton,
                        modifier = Modifier,
                        onLevelUpClick = {
                            onNavigateToLevelup()
                        }
                        )
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
                            EvolutionPowersSection(
                                abilities = detailsState.character.character.abilitiesList,
                                modifier = Modifier.weight(1f),
                                onAddPower = { detailsActions.onAddPower(context) }
                            )
                            Spacer(Modifier.height(10.dp))
                            AbilitiesSection(
                                abilities = detailsState.character.classAbilities,
                                usageCurrent = detailsState.abilityUsageCurrent,
                                usageMax = detailsState.abilityUsageMax,
                                modifier = Modifier.weight(1f),
                                onDecreaseUsage = detailsActions.onDecreaseUsage,
                                onIncreaseUsage = detailsActions.onIncreaseUsage
                            )
                        }

                        CharacterDetailsTab.INVENTORY ->  {
                            InventorySection(
                                items = detailsState.character.character.inventoryList,
                                capacityCurrent = detailsState.character.character.inventoryList.sumOf { it.numericValue }, // Se numeric value è il peso
                                capacityMax = detailsState.character.character.maxCapacity,
                                onAddItem = { detailsActions.onAddItem(context) },
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
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleWeight: Float = 0.9f
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(titleWeight)
        )
        IconButton(
            onClick = onAddClick,
            modifier = Modifier.weight(1f - titleWeight)
        ) {
            Icon(Icons.Default.Add, contentDescription = "aggiungi", tint = Color.Magenta)
        }
    }
}

@Composable
private fun CountRow(
    title: String,
    current: Int,
    max: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(0.7f)
        )
        IconButton(onClick = onDecrease, modifier = Modifier.weight(0.1f)) {
            Icon(Icons.Default.Remove, contentDescription = "togli un uso", tint = Color.Magenta)
        }
        Text("$current/$max", modifier = Modifier.weight(0.1f))
        IconButton(onClick = onIncrease, modifier = Modifier.weight(0.1f)) {
            Icon(Icons.Default.Add, contentDescription = "aggiungi un uso", tint = Color.Magenta)
        }
    }
}

@Composable
private fun InventoryHeader(
    current: Int,
    max: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "INVENTARIO",
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(0.7f)
        )
        Text("Capacità $current/$max", modifier = Modifier.weight(0.2f))
        IconButton(onClick = onAddClick, modifier = Modifier.weight(0.1f)) {
            Icon(Icons.Default.Add, contentDescription = "nuovo oggetto", tint = Color.Magenta)
        }
    }
}

@Composable
private fun EvolutionPowersSection(
    abilities: List<AbilityItem>,
    modifier: Modifier,
    onAddPower: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        PowersHeader(
            title = "POTERI EVOLUZIONE",
            onAddClick = onAddPower,
            titleWeight = 0.9f
        )
        ListItems(
            elements = abilities,
            modifier =  Modifier.fillMaxWidth().weight(1f),
            costText = { item -> Text("${item.numericValue} PE", fontSize = 18.sp) })
    }
}

@Composable
private fun AbilitiesSection(
    abilities: List<ClassAbility>,
    usageCurrent: Int,
    usageMax: Int,
    modifier: Modifier,
    onDecreaseUsage: () -> Unit,
    onIncreaseUsage: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        CountRow(
            title = "ABILITA'",
            current = usageCurrent,
            max = usageMax,
            onDecrease = onDecreaseUsage,
            onIncrease = onIncreaseUsage
        )
        ListItems(
            elements = abilities.map {
                AbilityItem(
                    name = it.name,
                    description = it.description,
                    numericValue = 0) }, Modifier.fillMaxWidth().weight(1f))
    }
}

//Per testare tengo capacità a 2 al momento
@Composable
private fun InventorySection(
    items: List<InventoryItem>, //Al momento uso abilities per testare, sarà da costruire anche un nuovo metodo per le liste quando faremo gli oggetti
    capacityCurrent: Int,   //Da reperire dal personaggio
    capacityMax: Int,       //Da reperire dal personaggio
    onAddItem: () -> Unit,
    onUseItem: (InventoryItem) -> Unit
) {
    Column {
        InventoryHeader(
            current = capacityCurrent,
            max = capacityMax,
            onAddClick = onAddItem
        )
        ListItems(
            elements = items,
            modifier = Modifier.fillMaxWidth().weight(1f),
            onUseItem = onUseItem,
            costText = { item -> Text("Peso: ${item.numericValue}", fontSize = 18.sp) })
    }
}

@Composable
private fun StatSection(
    hp: Int,
    maxHp: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    stats: List<Int>,
    speed: Double,
    armor: String,
    normalizedStats: List<Float>
) {
    CountRow("HP",hp,maxHp,onDecrease,onIncrease)
    //Questa è la soluzione più rapida che ho trovato, si potrebbe provare se no a usare due rettangoli sovrapposti per fare l'effetto, ci si pensa
    LinearProgressIndicator(
        modifier = Modifier.height(15.dp).fillMaxWidth(),
        progress = { if (maxHp > 0) hp.toFloat()/maxHp else 0f },
        color = Color.Green,
        trackColor = Color.Red
    )

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Velocità")
            Text("${speed}m", fontSize = 25.sp)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Armatura")
            Text(armor, fontSize = 25.sp)
        }
    }

    Spacer(Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Statistiche")
            Text("Forza ${stats[0]}  (${calculateModifier(stats[0])})")
            Text("Agilità ${stats[1]} (${calculateModifier(stats[1])})")
            Text("Intelligenza ${stats[2]} (${calculateModifier(stats[2])})")
            Text("Carisma ${stats[3]} (${calculateModifier(stats[3])})")
            Text("Potenza ${stats[4]} (${calculateModifier(stats[4])})")
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            statChart(normalizedStats,listOf("FOR","AGI","INT","CAR","POT"), lineColor = Color.Magenta, fillColor = Color.Magenta.copy(alpha = 0.3f))
        }
    }
}


@Composable
private fun ListItems(
    elements: List<DisplayableItem>,
    modifier: Modifier,
    costText: @Composable (DisplayableItem) -> Unit = { },
    onUseItem: ((InventoryItem) -> Unit)? = null) { //Per quando avremo oggetti funzionanti e utilizzabili
    var selectedItem by remember { mutableStateOf<DisplayableItem?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp)
    ) {
        items(elements) { element ->
            GenericListElement(
                item = element,
                onClick = { selectedItem = element },
                costText = costText
            )
            Spacer(Modifier.height(5.dp))
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
    costText: @Composable (DisplayableItem) -> Unit = { }) {
    Column() {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Magenta
                )
                .padding(8.dp)
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(item.description, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

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
    values: List<Float>,    //Serve normalizzato tra 0 e 1, farò una funzione per farlo
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color,
    fillColor: Color
) {
    require(values.size == labels.size)
    val sides = values.size
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(40.dp)
    ) {
        val center = Offset(size.width/2,size.height/2)
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
            drawPath(path, Color.Gray,style = Stroke(width = 1.dp.toPx()))
        }

        //Disegna una riga dal centro al vertice del pentagono
        for (i in 0 until sides) {
            val angle = -Math.PI / 2 + vertexAngle * i
            val end = Offset(
                x = center.x + (radius * cos(angle)).toFloat(),
                y = center.y + (radius * sin(angle)).toFloat()
            )
            drawLine(Color.Gray,center,end, strokeWidth = 1.dp.toPx())
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
            val labelRadius = radius + 20.dp.toPx()
            val labelPosition = Offset(
                x = center.x + (labelRadius * cos(angle)).toFloat(),
                y = center.y + (labelRadius * sin(angle)).toFloat()
            )

            val textLayoutResult = textMeasurer.measure(
                text = labels[i],
                style = TextStyle(fontSize = 12.sp, color = Color.White)
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