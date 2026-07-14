package com.example.esamemobile.utilities.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    textFieldState: TextFieldState,
    onQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
            value = textFieldState.text.toString(),
            onValueChange = { text ->
                textFieldState.edit { replace(0,this.length, text)
                onQuery(text) } },
            readOnly = false,
            placeholder = { Text("Cerca") },
            modifier = modifier.fillMaxWidth()
        )

}



