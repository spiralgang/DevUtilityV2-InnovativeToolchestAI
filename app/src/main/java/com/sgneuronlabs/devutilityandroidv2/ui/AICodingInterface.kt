package com.sgneuronlabs.devutilityandroidv2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AICodingInterface(viewModel: DevUtilityViewModelV2) {
    Row(Modifier.fillMaxSize()) {
        CodeEditorWithAI(viewModel, Modifier.weight(1f))
        AISuggestionPanel(viewModel, Modifier.weight(1f))
    }
}

@Composable
fun CodeEditorWithAI(viewModel: DevUtilityViewModelV2, modifier: Modifier) {
    var code by remember { mutableStateOf("") }
    Column(modifier.padding(16.dp)) {
        TextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Code Editor") },
            modifier = Modifier.fillMaxHeight().fillMaxWidth(),
            maxLines = 20
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.executeCode(code) }) {
            Text("Execute")
        }
    }
}

@Composable
fun AISuggestionPanel(viewModel: DevUtilityViewModelV2, modifier: Modifier) {
    val suggestion = viewModel.getAISuggestion("")
    Column(modifier.padding(16.dp)) {
        Text("AI Suggestion:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (suggestion.isNotEmpty()) {
            Text(suggestion, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { viewModel.suggestCode(suggestion) }) {
                Text("Insert & Suggest")
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Execution Output:", style = MaterialTheme.typography.titleMedium)
        Text(viewModel.executionOutput.value, style = MaterialTheme.typography.bodyMedium)
    }
}