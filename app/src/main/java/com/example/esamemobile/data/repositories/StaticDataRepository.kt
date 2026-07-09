package com.example.esamemobile.data.repositories

import android.content.Context
import com.example.esamemobile.R
import com.example.esamemobile.data.staticData.AgeMalus
import com.example.esamemobile.data.staticData.GameClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StaticData(
    val ageMalus: List<AgeMalus>,
    val gameClasses: List<GameClass>
)

class StaticDataRepository(private val context: Context) {
    private val data: StaticData by lazy {
        val json = context.assets.open("StaticData.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(json)
    }

    private val drawables = hashMapOf(
        "ic_young" to R.drawable.ic_young,
        "ic_nohand" to R.drawable.ic_nohand,
        "ic_cognition" to R.drawable.ic_cognition,
        "ic_noeye" to R.drawable.ic_noeye,
        "ic_badbone" to R.drawable.ic_badbone,
        "ic_waterloss" to R.drawable.ic_waterloss,
        "ic_breath" to R.drawable.ic_breath,
        "ic_nomouth" to R.drawable.ic_nomouth,
        "ic_nohearing" to R.drawable.ic_nohearing,
        "ic_climate" to R.drawable.ic_climate
    )

    val allGameClasses get() = data.gameClasses
    val allAgeMalus get() = data.ageMalus

    fun getClassById(id: String) = data.gameClasses.find { it.id == id }

    fun getDrawableId(name: String?): Int? {
        return if (name == null  || !drawables.contains(name)) null else  drawables[name]
    }
}