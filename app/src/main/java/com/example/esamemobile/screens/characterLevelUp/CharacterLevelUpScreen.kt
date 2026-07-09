package com.example.esamemobile.screens.characterLevelUp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue

@Composable
fun LevelUpScreen(
    charId: String,
    viewModel: LevelUpViewModel,
    onNavigateBack: () -> Unit
) { 
    LaunchedEffect(charId) {
        viewModel.initLevelUp(charId)
    }

    val state by viewModel.state.collectAsState()
    val actions = viewModel.actions

    val currentState = state ?: return
}