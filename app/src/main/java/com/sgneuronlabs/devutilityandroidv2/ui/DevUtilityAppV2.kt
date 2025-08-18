package com.sgneuronlabs.devutilityandroidv2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevUtilityAppV2(
    viewModel: DevUtilityViewModelV2 = viewModel(),
    onSignIn: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    val error by viewModel.error.collectAsState()

    if (showSettings) {
        SettingsScreen(viewModel) { showSettings = false }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("DevUtility V2") },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp)) {
                if (error != null) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = onSignIn) {
                    Text("Get OpenAI API Key")
                }
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Paste Your OpenAI API Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { viewModel.saveApiKey(apiKeyInput) }) {
                    Text("Save API Key")
                }
                Spacer(Modifier.height(16.dp))
                AICodingInterface(viewModel)
            }
        }
    }
}