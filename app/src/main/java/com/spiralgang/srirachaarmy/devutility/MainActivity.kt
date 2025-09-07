// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spiralgang.srirachaarmy.devutility.ai.LivingAINativeInterface
import com.spiralgang.srirachaarmy.devutility.ui.LivingDynamicInterface
import com.spiralgang.srirachaarmy.devutility.ui.DevUtilityViewModelV2
import com.spiralgang.srirachaarmy.devutility.ui.theme.SrirachaArmyTheme
import com.spiralgang.srirachaarmy.devutility.agentic.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlinx.coroutines.launch

/**
 * MainActivity for SrirachaArmy Agentic Living-Code Augmentation System
 * 
 * This is the entry point for the revolutionary AI-native development experience where:
 * - Every interaction teaches the system to work better with you (Intimate AI&i)
 * - Code evolves and adapts based on your patterns (Living-Code Augmentation)
 * - AI agents autonomously handle complex development workflows (Agentic Intelligence)
 * - The interface becomes a living, breathing extension of your development process
 * 
 * Core Philosophy:
 * - The Shell & Terminal IS the AI && the AI IS the Development Environment
 * - Living-Code: Code that learns, adapts, and evolves from usage patterns
 * - Agentic AI: Autonomous agents working as intelligent development partners
 * - Intimate AI&i: Every interaction creates a deeper understanding between human and AI
 * 
 * This creates a truly revolutionary agentic living-code augmentation experience.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: DevUtilityViewModelV2 by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("MainActivity starting - SrirachaArmy Agentic Living-Code Augmentation System")
        
        enableEdgeToEdge()
        setContent {
            SrirachaArmyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use the ViewModel-managed UI state
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    
                    // Show the enhanced DevUtility interface
                    DevUtilityMainInterface(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        Timber.d("Agentic Living-Code Augmentation System initialized successfully")
    }
}

// Base interface for the living AI system
interface LivingAINativeInterface {
    val livingState: kotlinx.coroutines.flow.StateFlow<*>
    val conversationFlow: kotlinx.coroutines.flow.SharedFlow<*>
    val contextAwareness: kotlinx.coroutines.flow.StateFlow<*>
    
    suspend fun initialize()
    suspend fun processAINativeInput(input: String): Any
    suspend fun getProactiveSuggestions(): List<String>
}

/**
 * Main DevUtility interface using the ViewModel
 */
@Composable
fun DevUtilityMainInterface(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    // Simple workspace interface for now
    DevUtilityWorkspace(
        uiState = uiState,
        viewModel = viewModel,
        modifier = modifier
    )
}

/**
 * Main workspace interface
 */
@Composable
fun DevUtilityWorkspace(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    // Main workspace with tabs showing different components
    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "üî• SrirachaArmy DevUtility v${BuildConfig.SRIRACHA_ARMY_VERSION}",
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Status indicators
                    Text(
                        text = "ü§ñ ${uiState.activeBotCount} bots",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (uiState.isAIOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "üü¢ AI Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Main content tabs
        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf("üè† Workspace", "‚ö° Agentic", "üêß Terminal", "üîß Settings")
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFFFF5722)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            0 -> WorkspacePanel(uiState, viewModel, Modifier.fillMaxSize())
            1 -> AgenticPanel(Modifier.fillMaxSize())
            2 -> TerminalPanel(uiState, viewModel, Modifier.fillMaxSize())
            3 -> SettingsPanel(uiState, Modifier.fillMaxSize())
        }
    }
}

/**
 * Main workspace panel
 */
@Composable
fun WorkspacePanel(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "üî• SrirachaArmy Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Heat Level: ${uiState.currentHeatLevel}")
                    Text("Active Bots: ${uiState.activeBotCount}")
                    Text("Current Language: ${uiState.selectedLanguage}")
                }
            }
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "üìÅ Development Environment",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Terminal Ready: ${if (uiState.terminalReady) "‚úÖ" else "‚ùå"}")
                    Text("RootFS Ready: ${if (uiState.rootfsReady) "‚úÖ" else "‚ùå"}")
                    Text("Editor Ready: ${if (uiState.editorReady) "‚úÖ" else "‚ùå"}")
                    Text("Container Engine: ${if (uiState.containerEngineReady) "‚úÖ" else "‚ùå"}")
                }
            }
        }
        
        if (uiState.openFiles.isNotEmpty()) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "üìù Open Files",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.openFiles.forEach { file ->
                            Text(
                                text = "‚Ä¢ $file",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Agentic systems panel
 */
@Composable
fun AgenticPanel(modifier: Modifier = Modifier) {
    val repositoryManager = remember { AgenticRepositoryManager(LocalContext.current) }
    val livingCodeAdapter = remember { LivingCodeAdapter(LocalContext.current, repositoryManager) }
    val workflowEngine = remember { AgenticWorkflowEngine(repositoryManager, livingCodeAdapter) }
    val resourceLoader = remember { DynamicResourceLoader(LocalContext.current, repositoryManager) }
    
    AgenticIntegrationInterface(
        repositoryManager = repositoryManager,
        livingCodeAdapter = livingCodeAdapter,
        workflowEngine = workflowEngine,
        resourceLoader = resourceLoader,
        modifier = modifier
    )
}

/**
 * Terminal panel
 */
@Composable
fun TerminalPanel(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "üêß Terminal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(12.dp)
            ) {
                item {
                    Text(
                        text = "Working Directory: ${uiState.currentWorkingDirectory}",
                        color = Color.Green,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                items(uiState.terminalOutput) { line ->
                    Text(
                        text = line,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                if (uiState.terminalOutput.isEmpty()) {
                    item {
                        Text(
                            text = "Terminal ready - Enter commands above or use agentic interface",
                            color = Color.Gray,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * Settings panel
 */
@Composable
fun SettingsPanel(
    uiState: DevUtilityViewModelV2.UIState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "‚öôÔ∏è Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ü§ñ AI Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Model: ${uiState.aiModel}")
                    Text("Status: ${if (uiState.isAIOnline) "Online" else "Offline"}")
                }
            }
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "üå∂Ô∏è SrirachaArmy Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Heat Level: ${uiState.currentHeatLevel}")
                    Text("Coordination Pattern: ${uiState.coordinationPattern.ifEmpty { "None" }}")
                }
            }
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "üîß System Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version: ${BuildConfig.SRIRACHA_ARMY_VERSION}")
                    Text("Build: ${BuildConfig.BUILD_TIMESTAMP}")
                    Text("Available Languages: ${uiState.availableLanguages.size}")
                }
            }
        }
    }
}