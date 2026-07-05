package com.example.esamemobile.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class CharacterViewModel : ViewModel() {
    var peLeft by mutableStateOf(10)
    val abilitiesList = mutableStateListOf<Ability>()
}