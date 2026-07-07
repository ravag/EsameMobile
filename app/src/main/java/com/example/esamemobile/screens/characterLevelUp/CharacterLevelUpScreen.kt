package com.example.esamemobile.screens.characterLevelUp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue

@Composable
fun LevelUpScreen(
    currentLevel: Int,
    strengthModifier: Int,
    hasCha3: Boolean,
    hasAllBase: Boolean,
    viewModel: LevelUpViewModel = viewModel(),
    onNavigateBack: () -> Unit
) { 
    LaunchedEffect(Unit) {
        viewModel.initLevelUp(currentLevel, strengthModifier, hasCha3, hasAllBase)
    }

    val state by viewModel.state.collectAsState()
    val actions = viewModel.actions

    val currentState = state ?: return
}