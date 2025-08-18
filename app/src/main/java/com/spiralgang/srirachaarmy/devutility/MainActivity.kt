package com.spiralgang.srirachaarmy.devutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.spiralgang.srirachaarmy.devutility.ui.DevUtilityViewModelV2
import com.spiralgang.srirachaarmy.devutility.ui.SrirachaArmyInterface
import com.spiralgang.srirachaarmy.devutility.ui.theme.DevUtilitySrirachaArmyTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * MainActivity - Entry point for DevUtility SrirachaArmy IDE
 * 
 * This activity launches the complete SrirachaArmy development environment with:
 * - Unified IDE interface with bot dashboard
 * - Real-time SrirachaArmy bot status and suggestions
 * - Adaptive layouts for portrait/landscape orientation
 * - Material Design 3 theming with SrirachaArmy color scheme
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: DevUtilityViewModelV2 by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("🚀 SrirachaArmy IDE - MainActivity starting")
        
        setContent {
            DevUtilitySrirachaArmyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()
                    
                    SrirachaArmyInterface(
                        uiState = uiState,
                        onBotActivate = { botType, context, heatLevel ->
                            viewModel.activateBot(botType, context, heatLevel)
                        },
                        onExecuteUIYI = { context ->
                            viewModel.executeUIYIProcess(context)
                        },
                        onExecutePIPI = { code ->
                            viewModel.executePIPISystem(code)
                        },
                        onEscalateHeat = {
                            viewModel.escalateHeat()
                        },
                        onCoolDownHeat = {
                            viewModel.coolDownHeat()
                        },
                        onCodeChange = { code ->
                            viewModel.updateCode(code)
                        },
                        onLanguageChange = { language ->
                            viewModel.updateLanguage(language)
                        },
                        onGenerateSuggestion = { botType ->
                            viewModel.generateAISuggestion(botType)
                        },
                        onToggleTerminal = {
                            viewModel.toggleTerminal()
                        },
                        onExecuteCommand = { command ->
                            viewModel.executeTerminalCommand(command)
                        }
                    )
                }
            }
        }
        
        // Initialize SrirachaArmy systems
        initializeSrirachaArmySystems()
    }

    /**
     * Initialize all SrirachaArmy systems and check permissions
     */
    private fun initializeSrirachaArmySystems() {
        Timber.d("🌶️ Initializing SrirachaArmy bot systems...")
        
        // Initialize view model systems
        viewModel.initializeSystems()
        
        // Check for accessibility permissions (needed for Screen-Hop operations)
        checkAccessibilityPermissions()
        
        // Check for overlay permissions (needed for FloatWindowWatcher)
        checkOverlayPermissions()
        
        Timber.d("✅ SrirachaArmy systems initialization complete")
    }

    /**
     * Check if accessibility service permission is granted for Screen-Hop operations
     */
    private fun checkAccessibilityPermissions() {
        // In a real implementation, check if accessibility service is enabled
        // For now, we'll log the requirement
        Timber.d("🔓 Accessibility permissions needed for Screen-Hop operations (5S/8S Agents)")
        
        // TODO: Guide user to enable accessibility service
        // val accessibilityManager = getSystemService(AccessibilityManager::class.java)
        // Check if SrirachaScreenHopService is enabled
    }

    /**
     * Check if overlay permission is granted for FloatWindowWatcher
     */
    private fun checkOverlayPermissions() {
        // In a real implementation, check overlay permissions
        Timber.d("🔓 Overlay permissions needed for FloatWindowWatcher")
        
        // TODO: Request overlay permission if needed
        // if (!Settings.canDrawOverlays(this)) {
        //     // Request permission
        // }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("🔄 SrirachaArmy IDE resumed - refreshing bot status")
        viewModel.refreshBotStatus()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("⏸️ SrirachaArmy IDE paused - saving state")
        viewModel.saveCurrentState()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("🛑 SrirachaArmy IDE destroying - cleaning up bot systems")
        viewModel.cleanupSystems()
    }
}