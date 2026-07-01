package com.example.esamemobile.utilities.composables

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.esamemobile.data.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSearchBar(
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    searchResults: List<Group>,
    context: Context
) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = {textFieldState.edit { replace(0,length,it) }},
                    onSearch = {
                        onSearch(textFieldState.text.toString())
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = {expanded = it},
                    placeholder = { Text("Cerca") }
                )
            },
            expanded = expanded,
            onExpandedChange = {expanded = it},
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                searchResults.forEach { result ->
                    ListItem(
                        headlineContent = {Text(result.name)},
                        modifier = Modifier.clickable {
                            textFieldState.edit {replace(0,length,result.name)}
                            expanded = false
                            Toast.makeText(context,"Cliccato ${result.id}", Toast.LENGTH_SHORT).show()
                        } .fillMaxWidth()
                    )
                }
            }
        }
    }
}


@Composable
fun TestScreen(modifier: Modifier) {
    val textfieldState = rememberTextFieldState()
    val context = LocalContext.current
    val groupsTest = remember {
        listOf(
            Group(id = 1, name = "I Zingari", ""),
            Group(id = 2, name = "I fantastici 2", ""),
            Group(id = 3, name = "I tre moschettoni", ""),
            Group(id = 4, name = "I quattro gatti", ""),
            Group(id = 5, name = "I cojo(n)ti", ""),
            Group(id = 6, name = "Giovanni", ""),
            Group(id = 7, name = "Miku club", ""),
            Group(id = 8, name = "Il gioco perso", ""),
            Group(id = 9, name = "Ci piacciono i treni", ""),
            Group(id = 10, name = "Impottibile!", "")
        )
    }

    val filteredGroups by remember {
        derivedStateOf {
            val searchText = textfieldState.text.toString()
            if (searchText.isEmpty()) {
                emptyList<Group>()
            } else {
                groupsTest.filter { it.name.contains(searchText, ignoreCase = true) }
            }
        }
    }

    SimpleSearchBar(
        textfieldState,
        onSearch = {},
        searchResults = filteredGroups,
        context = context
    )
}
