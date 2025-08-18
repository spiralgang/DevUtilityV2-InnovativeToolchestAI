package com.sgneuronlabs.devutilityandroidv2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(viewModel: DevUtilityViewModelV2, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Row {
            RadioButton(selected = viewModel.settings.value.theme == "light", onClick = { viewModel.settings.value.theme = "light" })
            Text("Light")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = viewModel.settings.value.theme == "dark", onClick = { viewModel.settings.value.theme = "dark" })
            Text("Dark")
        }
        Text("Language", style = MaterialTheme.typography.titleMedium)
        Row {
            RadioButton(selected = viewModel.settings.value.language == "kotlin", onClick = { viewModel.settings.value.language = "kotlin" })
            Text("Kotlin")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = viewModel.settings.value.language == "javascript", onClick = { viewModel.settings.value.language = "javascript" })
            Text("JavaScript")
        }
        Text("Editor Font Size", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = viewModel.settings.value.fontSize.toFloat(),
            RadioButton(selected = viewModel.settings.value.theme == "light", onClick = { viewModel.setTheme("light") })
            Text("Light")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = viewModel.settings.value.theme == "dark", onClick = { viewModel.setTheme("dark") })
            Text("Dark")
        }
        Text("Language", style = MaterialTheme.typography.titleMedium)
        Row {
            RadioButton(selected = viewModel.settings.value.language == "kotlin", onClick = { viewModel.setLanguage("kotlin") })
            Text("Kotlin")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = viewModel.settings.value.language == "javascript", onClick = { viewModel.setLanguage("javascript") })
            Text("JavaScript")
        }
        Text("Editor Font Size", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = viewModel.settings.value.fontSize.toFloat(),
            onValueChange = { viewModel.setFontSize(it.toInt()) },
            valueRange = 10f..20f,
            steps = 9,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text("Current Size: ${viewModel.settings.value.fontSize}sp")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Save & Back")
        }
    }
}