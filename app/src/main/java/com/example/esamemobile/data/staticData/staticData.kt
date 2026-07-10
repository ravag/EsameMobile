package com.example.esamemobile.data.staticData

import kotlinx.serialization.Serializable

@Serializable
data class AgeMalus(
    val id: String,
    val drawableId: String? = null,
    val name: String,
    val desc: String
)

@Serializable
data class ClassAbility(
    val id: String,
    val name: String,
    val description: String,
    val isAdvanced: Boolean
)

@Serializable
data class GameClass(
    val id: String,
    val name: String,
    val description: String,
    val baseAbilities: List<ClassAbility>,
    val advancedAbilities: List<ClassAbility>
)