package com.spiralgang.srirachaarmy.devutility.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spiralgang.srirachaarmy.devutility.core.DeepSeekEngine
import com.spiralgang.srirachaarmy.devutility.core.SrirachaArmyOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * DevUtilityViewModelV2 - Main ViewModel for SrirachaArmy IDE
 * 
 * Coordinates all SrirachaArmy bot systems and manages the complete IDE state.
 * This ViewModel integrates:
 * - Bot orchestration and personality management
 * - DeepSeek AI engine with TensorFlow Lite fallback
 * - Code editing and syntax highlighting
 * - Terminal emulation and SSH client
 * - UIYI process execution (TT-CCC-RCCC-LDU)
 * - PIPI approval system (Preview-Implement-Push-Implement)
 * - Screen-Hop operations coordination
 * - WebNetCaste AI integration
 */
@HiltViewModel
class DevUtilityViewModelV2 @Inject constructor(
    private val srirachaOrchestrator: SrirachaArmyOrchestrator,
    private val deepSeekEngine: DeepSeekEngine
) : ViewModel() {

    /**
     * Complete UI state for SrirachaArmy IDE
     */
    data class UIState(
        val currentCode: String = "",
        val selectedLanguage: String = "Kotlin",
        val botStatuses: Map<SrirachaArmyOrchestrator.BotType, SrirachaArmyOrchestrator.BotStatus> = emptyMap(),
        val currentHeatLevel: SrirachaArmyOrchestrator.HeatLevel = SrirachaArmyOrchestrator.HeatLevel.MILD,
        val coordinationPattern: String = "",
        val aiModel: DeepSeekEngine.AIModel = DeepSeekEngine.AIModel.DEEPSEEK_CHAT,
        val isAIOnline: Boolean = false,
        val terminalVisible: Boolean = false,
        val terminalOutput: List<String> = emptyList(),
        val currentWorkingDirectory: String = "/",
        val activeBotCount: Int = 0,
        val lastAIResponse: DeepSeekEngine.AIResponse? = null,
        val uiyiProcessActive: Boolean = false,
        val pipiSystemActive: Boolean = false,
        val webNetCasteSearching: Boolean = false,
        val screenHopOperationActive: Boolean = false,
        val availableLanguages: List<String> = listOf(
            "Kotlin", "Java", "Python", "JavaScript", "TypeScript", 
            "C#", "Swift", "Go", "Rust", "HTML", "CSS"
        )
    )

    // UI State management
    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    // Code state
    private val _currentCode = MutableStateFlow("")
    private val _selectedLanguage = MutableStateFlow("Kotlin")

    // Terminal state
    private val _terminalOutput = MutableStateFlow<List<String>>(emptyList())

    init {
        initializeViewModelState()
    }

    /**
     * Initialize ViewModel with SrirachaArmy systems
     */
    private fun initializeViewModelState() {
        viewModelScope.launch {
            Timber.d("🚀 Initializing SrirachaArmy ViewModel state...")
            
            // Combine all state flows into UI state
            combine(
                _currentCode,
                _selectedLanguage,
                srirachaOrchestrator.botStatuses,
                srirachaOrchestrator.currentHeatLevel,
                srirachaOrchestrator.coordinationPattern,
                deepSeekEngine.currentModel,
                deepSeekEngine.isOnline,
                _terminalOutput
            ) { currentCode, selectedLanguage, botStatuses, heatLevel, coordinationPattern, aiModel, isOnline, terminalOutput ->
                
                _uiState.value.copy(
                    currentCode = currentCode,
                    selectedLanguage = selectedLanguage,
                    botStatuses = botStatuses,
                    currentHeatLevel = heatLevel,
                    coordinationPattern = coordinationPattern,
                    aiModel = aiModel,
                    isAIOnline = isOnline,
                    terminalOutput = terminalOutput,
                    activeBotCount = srirachaOrchestrator.getActiveBotCount()
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
        
        Timber.d("✅ SrirachaArmy ViewModel initialized")
    }

    /**
     * Initialize all SrirachaArmy systems
     */
    fun initializeSystems() {
        viewModelScope.launch {
            Timber.d("🌶️ Initializing SrirachaArmy systems...")
            
            // Initialize DeepSeek engine (would use real API key in production)
            deepSeekEngine.initialize(null) // Using local fallback for now
            
            // Add welcome message to terminal
            addTerminalOutput("🌶️ SrirachaArmy DevUtility IDE - Ready for action!")
            addTerminalOutput("🤖 Bot systems online and ready")
            addTerminalOutput("🔥 Heat level: ${_uiState.value.currentHeatLevel.name}")
        }
    }

    /**
     * Activate a SrirachaArmy bot with context
     */
    fun activateBot(
        botType: SrirachaArmyOrchestrator.BotType,
        context: String,
        heatLevel: SrirachaArmyOrchestrator.HeatLevel = _uiState.value.currentHeatLevel
    ) {
        viewModelScope.launch {
            Timber.d("🤖 Activating ${botType.name} with context: $context")
            
            val response = srirachaOrchestrator.activateBot(botType, context, heatLevel)
            addTerminalOutput("${botType.name}: $response")
        }
    }

    /**
     * Execute UIYI Process with TT-CCC-RCCC-LDU coordination
     */
    fun executeUIYIProcess(context: String) {
        viewModelScope.launch {
            Timber.d("🚀 Executing UIYI Process: $context")
            
            _uiState.value = _uiState.value.copy(uiyiProcessActive = true)
            
            val responses = srirachaOrchestrator.executeUIYIProcess(context)
            
            addTerminalOutput("🚀 UIYI Process started: TT-CCC-RCCC-LDU")
            responses.forEachIndexed { index, response ->
                val phase = when(index) {
                    0 -> "TT (Think-Through)"
                    1 -> "CCC (Coordinate-Code-Create)"
                    2 -> "RCCC (Review-Code-Correct-Compile)"
                    3 -> "LDU (Linear-Development-Updates)"
                    else -> "Phase $index"
                }
                addTerminalOutput("$phase: $response")
            }
            
            _uiState.value = _uiState.value.copy(uiyiProcessActive = false)
            addTerminalOutput("✅ UIYI Process completed")
        }
    }

    /**
     * Execute PIPI approval system
     */
    fun executePIPISystem(code: String) {
        viewModelScope.launch {
            Timber.d("✅ Executing PIPI System for code")
            
            _uiState.value = _uiState.value.copy(pipiSystemActive = true)
            
            val results = srirachaOrchestrator.executePIPISystem(code)
            
            addTerminalOutput("✅ PIPI System started: Preview-Implement-Push-Implement")
            results.forEach { (phase, response) ->
                addTerminalOutput("$phase: $response")
            }
            
            _uiState.value = _uiState.value.copy(pipiSystemActive = false)
            addTerminalOutput("✅ PIPI System completed")
        }
    }

    /**
     * Generate AI suggestion using current code and selected bot
     */
    fun generateAISuggestion(botType: SrirachaArmyOrchestrator.BotType) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.currentCode.isBlank()) {
                addTerminalOutput("⚠️ No code to analyze - please enter some code first")
                return@launch
            }
            
            Timber.d("🧠 Generating AI suggestion with ${botType.name}")
            addTerminalOutput("🧠 ${botType.name} is analyzing your ${currentState.selectedLanguage} code...")
            
            try {
                val response = deepSeekEngine.generateCodeSuggestion(
                    code = currentState.currentCode,
                    language = currentState.selectedLanguage,
                    botType = botType,
                    heatLevel = currentState.currentHeatLevel
                )
                
                _uiState.value = _uiState.value.copy(lastAIResponse = response)
                addTerminalOutput("${botType.name} (AI): ${response.content}")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate AI suggestion")
                addTerminalOutput("❌ AI suggestion failed: ${e.message}")
            }
        }
    }

    /**
     * Update current code
     */
    fun updateCode(code: String) {
        _currentCode.value = code
        Timber.d("📝 Code updated: ${code.length} characters")
    }

    /**
     * Update selected language
     */
    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
        Timber.d("🔤 Language changed to: $language")
        addTerminalOutput("📝 Language switched to $language")
    }

    /**
     * Escalate heat level
     */
    fun escalateHeat() {
        srirachaOrchestrator.escalateHeatLevel()
        addTerminalOutput("🔥 Heat level escalated to ${_uiState.value.currentHeatLevel.name}")
    }

    /**
     * Cool down heat level
     */
    fun coolDownHeat() {
        srirachaOrchestrator.coolDownHeatLevel()
        addTerminalOutput("❄️ Heat level cooled down to ${_uiState.value.currentHeatLevel.name}")
    }

    /**
     * Toggle terminal visibility
     */
    fun toggleTerminal() {
        _uiState.value = _uiState.value.copy(
            terminalVisible = !_uiState.value.terminalVisible
        )
    }

    /**
     * Execute terminal command
     */
    fun executeTerminalCommand(command: String) {
        viewModelScope.launch {
            addTerminalOutput("$ $command")
            
            when {
                command.startsWith("sriracha ") -> {
                    executeSrirachaCommand(command.substringAfter("sriracha "))
                }
                command == "clear" -> {
                    _terminalOutput.value = emptyList()
                }
                command == "bots" -> {
                    showBotStatus()
                }
                command == "heat" -> {
                    addTerminalOutput("Current heat level: ${_uiState.value.currentHeatLevel.name}")
                }
                else -> {
                    // Simulate basic command execution
                    addTerminalOutput("Command executed: $command")
                }
            }
        }
    }

    /**
     * Execute SrirachaArmy-specific commands
     */
    private fun executeSrirachaCommand(command: String) {
        when (command) {
            "activate ssa" -> activateBot(SrirachaArmyOrchestrator.BotType.SSA, "Manual activation")
            "activate ffa" -> activateBot(SrirachaArmyOrchestrator.BotType.FFA, "Manual activation")
            "activate 5s" -> activateBot(SrirachaArmyOrchestrator.BotType.AGENT_5S, "Manual activation")
            "activate 8s" -> activateBot(SrirachaArmyOrchestrator.BotType.AGENT_8S, "Manual activation")
            "activate webnetcaste" -> activateBot(SrirachaArmyOrchestrator.BotType.WEBNETCASTE, "Manual activation")
            "heat up" -> escalateHeat()
            "cool down" -> coolDownHeat()
            "uiyi" -> executeUIYIProcess("Manual UIYI execution")
            "pipi" -> executePIPISystem(_uiState.value.currentCode)
            else -> addTerminalOutput("Unknown SrirachaArmy command: $command")
        }
    }

    /**
     * Show bot status in terminal
     */
    private fun showBotStatus() {
        addTerminalOutput("🤖 SrirachaArmy Bot Status:")
        _uiState.value.botStatuses.forEach { (botType, status) ->
            val statusIcon = if (status.isActive) "🟢" else "⚫"
            addTerminalOutput("  $statusIcon ${botType.name}: ${status.currentTask.ifBlank { "Idle" }}")
        }
        addTerminalOutput("🔥 Heat Level: ${_uiState.value.currentHeatLevel.name}")
        addTerminalOutput("📊 Active Bots: ${_uiState.value.activeBotCount}")
    }

    /**
     * Add message to terminal output
     */
    private fun addTerminalOutput(message: String) {
        val currentOutput = _terminalOutput.value.toMutableList()
        currentOutput.add(message)
        // Keep only last 100 messages to prevent memory issues
        if (currentOutput.size > 100) {
            currentOutput.removeAt(0)
        }
        _terminalOutput.value = currentOutput
    }

    /**
     * Refresh bot status (called from MainActivity onResume)
     */
    fun refreshBotStatus() {
        Timber.d("🔄 Refreshing SrirachaArmy bot status")
        addTerminalOutput("🔄 Refreshing bot status...")
    }

    /**
     * Save current state (called from MainActivity onPause)
     */
    fun saveCurrentState() {
        Timber.d("💾 Saving SrirachaArmy IDE state")
        // In a real implementation, save state to preferences or database
    }

    /**
     * Cleanup systems (called from MainActivity onDestroy)
     */
    fun cleanupSystems() {
        Timber.d("🧹 Cleaning up SrirachaArmy systems")
        srirachaOrchestrator.deactivateAllBots()
    }
}