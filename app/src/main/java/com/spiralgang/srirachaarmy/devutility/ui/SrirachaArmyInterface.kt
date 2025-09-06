package com.spiralgang.srirachaarmy.devutility.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spiralgang.srirachaarmy.devutility.core.DeepSeekEngine
import com.spiralgang.srirachaarmy.devutility.core.SrirachaArmyOrchestrator
import com.spiralgang.srirachaarmy.devutility.ui.theme.*

/**
 * SrirachaArmyInterface - Main UI for the DevUtility SrirachaArmy IDE
 * 
 * Features:
 * - Unified IDE interface with bot dashboard
 * - Real-time bot status and suggestions
 * - Adaptive layouts for portrait/landscape
 * - Material Design 3 theming with SrirachaArmy colors
 * - Code editor with syntax highlighting
 * - Terminal integration
 * - Heat level management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SrirachaArmyInterface(
    uiState: DevUtilityViewModelV2.UIState,
    onBotActivate: (SrirachaArmyOrchestrator.BotType, String, SrirachaArmyOrchestrator.HeatLevel) -> Unit,
    onExecuteUIYI: (String) -> Unit,
    onExecutePIPI: (String) -> Unit,
    onEscalateHeat: () -> Unit,
    onCoolDownHeat: () -> Unit,
    onCodeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onGenerateSuggestion: (SrirachaArmyOrchestrator.BotType) -> Unit,
    onToggleTerminal: () -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Code Editor", "Terminal", "AI Assistant")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with SrirachaArmy branding
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🌶️ SrirachaArmy IDE",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    HeatLevelIndicator(
                        heatLevel = uiState.currentHeatLevel,
                        onEscalate = onEscalateHeat,
                        onCoolDown = onCoolDownHeat
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )
        
        // Tab Navigation
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab Content
        when (selectedTab) {
            0 -> BotDashboard(
                uiState = uiState,
                onBotActivate = onBotActivate,
                onExecuteUIYI = onExecuteUIYI,
                onExecutePIPI = onExecutePIPI
            )
            1 -> CodeEditor(
                code = uiState.currentCode,
                language = uiState.selectedLanguage,
                availableLanguages = uiState.availableLanguages,
                onCodeChange = onCodeChange,
                onLanguageChange = onLanguageChange
            )
            2 -> TerminalView(
                terminalOutput = uiState.terminalOutput,
                onExecuteCommand = onExecuteCommand
            )
            3 -> AIAssistant(
                uiState = uiState,
                onGenerateSuggestion = onGenerateSuggestion
            )
        }
    }
}

/**
 * Heat Level Indicator with escalation controls
 */
@Composable
fun HeatLevelIndicator(
    heatLevel: SrirachaArmyOrchestrator.HeatLevel,
    onEscalate: () -> Unit,
    onCoolDown: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onCoolDown,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Cool Down",
                tint = Color.Blue
            )
        }
        
        val heatColor = when (heatLevel) {
            SrirachaArmyOrchestrator.HeatLevel.MILD -> SrirachaMild
            SrirachaArmyOrchestrator.HeatLevel.MEDIUM -> SrirachaMedium
            SrirachaArmyOrchestrator.HeatLevel.SPICY -> SrirachaSpicy
            SrirachaArmyOrchestrator.HeatLevel.GHOST_PEPPER -> SrirachaGhostPepper
        }
        
        Box(
            modifier = Modifier
                .background(heatColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = heatLevel.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        IconButton(
            onClick = onEscalate,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Escalate Heat",
                tint = Color.Red
            )
        }
    }
}

/**
 * Bot Dashboard showing all SrirachaArmy bot statuses
 */
@Composable
fun BotDashboard(
    uiState: DevUtilityViewModelV2.UIState,
    onBotActivate: (SrirachaArmyOrchestrator.BotType, String, SrirachaArmyOrchestrator.HeatLevel) -> Unit,
    onExecuteUIYI: (String) -> Unit,
    onExecutePIPI: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // System Status Card
        item {
            SystemStatusCard(uiState)
        }
        
        // Bot Status Cards
        items(uiState.botStatuses.entries.toList()) { (botType, status) ->
            BotStatusCard(
                botType = botType,
                status = status,
                currentHeatLevel = uiState.currentHeatLevel,
                onActivate = { context ->
                    onBotActivate(botType, context, uiState.currentHeatLevel)
                }
            )
        }
        
        // Process Controls
        item {
            ProcessControlsCard(
                uiState = uiState,
                onExecuteUIYI = onExecuteUIYI,
                onExecutePIPI = onExecutePIPI
            )
        }
    }
}

/**
 * System Status Card
 */
@Composable
fun SystemStatusCard(uiState: DevUtilityViewModelV2.UIState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🌶️ SrirachaArmy System Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("Active Bots", uiState.activeBotCount.toString(), BotActive)
                StatusItem("Heat Level", uiState.currentHeatLevel.name, getHeatLevelColor(uiState.currentHeatLevel))
                StatusItem("AI Model", uiState.aiModel.name.take(8), if (uiState.isAIOnline) BotActive else BotError)
            }
            
            if (uiState.coordinationPattern.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🚀 Active Pattern: ${uiState.coordinationPattern}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Individual Bot Status Card
 */
@Composable
fun BotStatusCard(
    botType: SrirachaArmyOrchestrator.BotType,
    status: SrirachaArmyOrchestrator.BotStatus,
    currentHeatLevel: SrirachaArmyOrchestrator.HeatLevel,
    onActivate: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (status.isActive) BotActive else Color.Gray,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getBotDisplayName(botType),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = { onActivate("Manual activation from dashboard") },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Activate", fontSize = 12.sp)
                }
            }
            
            if (status.currentTask.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Task: ${status.currentTask}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (status.lastResponse.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status.lastResponse.take(100) + if (status.lastResponse.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Responses: ${status.responseCount}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Heat: ${status.heatLevel.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = getHeatLevelColor(status.heatLevel)
                )
            }
        }
    }
}

/**
 * Process Controls Card
 */
@Composable
fun ProcessControlsCard(
    uiState: DevUtilityViewModelV2.UIState,
    onExecuteUIYI: (String) -> Unit,
    onExecutePIPI: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🚀 Process Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onExecuteUIYI("Dashboard UIYI execution") },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.uiyiProcessActive
                ) {
                    if (uiState.uiyiProcessActive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("UIYI Process")
                    }
                }
                
                Button(
                    onClick = { onExecutePIPI(uiState.currentCode) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.pipiSystemActive && uiState.currentCode.isNotEmpty()
                ) {
                    if (uiState.pipiSystemActive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("PIPI System")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TT-CCC-RCCC-LDU coordination • Preview-Implement-Push-Implement approval",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

// Helper functions
@Composable
fun StatusItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

fun getBotDisplayName(botType: SrirachaArmyOrchestrator.BotType): String {
    return when (botType) {
        SrirachaArmyOrchestrator.BotType.SSA -> "🔧 SSA (Structure Agent)"
        SrirachaArmyOrchestrator.BotType.FFA -> "💡 FFA (Creative Agent)"
        SrirachaArmyOrchestrator.BotType.AGENT_5S -> "🏃 5S Agent (Chill Hopping)"
        SrirachaArmyOrchestrator.BotType.AGENT_8S -> "🔥 8S Agent (Aggressive Crushing)"
        SrirachaArmyOrchestrator.BotType.WEBNETCASTE -> "🕸️ WebNetCaste AI"
        SrirachaArmyOrchestrator.BotType.UIYI_PROCESS -> "🚀 UIYI Process"
    }
}

fun getHeatLevelColor(heatLevel: SrirachaArmyOrchestrator.HeatLevel): Color {
    return when (heatLevel) {
        SrirachaArmyOrchestrator.HeatLevel.MILD -> SrirachaMild
        SrirachaArmyOrchestrator.HeatLevel.MEDIUM -> SrirachaMedium
        SrirachaArmyOrchestrator.HeatLevel.SPICY -> SrirachaSpicy
        SrirachaArmyOrchestrator.HeatLevel.GHOST_PEPPER -> SrirachaGhostPepper
    }
}

/**
 * Basic Code Editor
 */
@Composable
fun CodeEditor(
    code: String,
    language: String,
    availableLanguages: List<String>,
    onCodeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Language selector
        var expanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = language,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableLanguages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            onLanguageChange(lang)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Code input
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text("Enter your $language code") },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        )
    }
}

/**
 * Terminal View
 */
@Composable
fun TerminalView(
    terminalOutput: List<String>,
    onExecuteCommand: (String) -> Unit
) {
    var command by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Terminal output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            items(terminalOutput) { line ->
                Text(
                    text = line,
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Command input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                label = { Text("Enter command") },
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace
                )
            )
            Button(
                onClick = {
                    if (command.isNotBlank()) {
                        onExecuteCommand(command)
                        command = ""
                    }
                }
            ) {
                Text("Execute")
            }
        }
    }
}

/**
 * AI Assistant View
 */
@Composable
fun AIAssistant(
    uiState: DevUtilityViewModelV2.UIState,
    onGenerateSuggestion: (SrirachaArmyOrchestrator.BotType) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🧠 AI Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Model: ${uiState.aiModel.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Status: ${if (uiState.isAIOnline) "Online" else "Offline"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.isAIOnline) BotActive else BotError
                    )
                }
            }
        }
        
        // Bot suggestion buttons
        items(SrirachaArmyOrchestrator.BotType.values().toList()) { botType ->
            Button(
                onClick = { onGenerateSuggestion(botType) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get ${getBotDisplayName(botType)} Suggestion")
            }
        }
        
        // Last AI response
        uiState.lastAIResponse?.let { response ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Latest AI Response",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = response.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Model: ${response.model.name}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "Time: ${response.processingTime}ms",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}