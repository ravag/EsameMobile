package com.example.esamemobile.screens.characterDetails

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.esamemobile.data.Character
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CharacterDetailsTab {
    STATS,POWERS,INVENTORY
}

//Al momento importo ability sbagliato, sarà da sistemare una volta fatte le abilità per bene
//Come anche molte delle cose inserite qui saranno semplicemente da mettere all'interno del personaggio
data class CharacterDetailsState(
    val character: Character,
    var selectedTab: CharacterDetailsTab = CharacterDetailsTab.STATS,
    val hp: Int,
    val maxHp: Int,
    val abilities: List<Abilities>,
    val stats: List<Int>,
    val abilityUsageCurrent: Int,
    val abilityUsageMax: Int,
    val inventoryCapacityCurrent: Int,
    val inventoryCapacityMax: Int,
    val normalizedStats: List<Float>
)

data class CharacterDetailsActions(
    val onTabSelected: (Int) -> Unit,
    val onLevelUp: (Context) -> Unit,
    val onDecreaseHp: () -> Unit,
    val onIncreaseHp: () -> Unit,
    val onAddPower: (Context) -> Unit,
    val onDecreaseUsage: () -> Unit,
    val onIncreaseUsage: () -> Unit,
    val onAddItem: (Context) -> Unit,
    //val onUseItem: () -> Unit, //Al momento non utilizzata, bisogna capire come e quando usarla
)

class CharacterDetailsViewModel () : ViewModel() {
    private val _state = MutableStateFlow(CharacterDetailsState(
        character = Character(5,"ciao",""),
        hp = 10,
        maxHp = 10,
        abilities = listOf(Abilities("caio","wow caia",1),
            Abilities("aaa","wow caia",3),
            Abilities("bbb","wow caia",2),
            Abilities("cccc","nel mezzo del cammin di nostra vita mi ritrovai per una selva oscura che la diretta via era smarrita, tanto ...",5)),
        stats = listOf(15,10,7,5,3),
        abilityUsageCurrent = 2,
        abilityUsageMax = 2,
        inventoryCapacityCurrent = 2,
        inventoryCapacityMax = 2,
        normalizedStats = normalizeStats(listOf(15,10,7,5,3)) //Al momento questo è fatto male, sarà da sistemare quando il personaggio avrà le statistiche
    ))
    val state = _state.asStateFlow()

    val actions = CharacterDetailsActions(
        onTabSelected = { index -> _state.update { it.copy(selectedTab = CharacterDetailsTab.entries[index]) } },
        onLevelUp = { context -> Toast.makeText(context,"Level up", Toast.LENGTH_SHORT).show() },
        onDecreaseHp = { _state.update { it.copy(hp = (it.hp-1).coerceAtLeast(0)) } },
        onIncreaseHp = { _state.update { it.copy(hp = (it.hp+1).coerceAtMost(it.maxHp)) } },
        onAddPower = { context -> Toast.makeText(context, "aggiungi potere", Toast.LENGTH_SHORT).show() },
        onDecreaseUsage = { _state.update { it.copy(abilityUsageCurrent = (it.abilityUsageCurrent-1).coerceAtLeast(0)) } },
        onIncreaseUsage = { _state.update { it.copy(abilityUsageCurrent = (it.abilityUsageCurrent+1).coerceAtMost(it.abilityUsageMax)) }},
        onAddItem = {context -> Toast.makeText(context, "aggiungi oggetto", Toast.LENGTH_SHORT).show() }
    )
}

private fun normalizeStats(stats: List<Int>): List<Float> {
    val maxStat = 15
    return stats.map { stat -> stat.toFloat()/maxStat }
}