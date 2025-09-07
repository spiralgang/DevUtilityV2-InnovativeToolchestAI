// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spiralgang.srirachaarmy.devutility.core.DeepSeekEngine
import com.spiralgang.srirachaarmy.devutility.core.SrirachaArmyOrchestrator
import com.spiralgang.srirachaarmy.devutility.terminal.LocalTerminalEmulator
import com.spiralgang.srirachaarmy.devutility.system.RootFSManager
import com.spiralgang.srirachaarmy.devutility.system.SystemInfoManager
import com.spiralgang.srirachaarmy.devutility.editor.CodeEditor
import com.spiralgang.srirachaarmy.devutility.vm.ContainerEngine
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
 * - Code editing and syntax highlighting with multi-language support
 * - Local terminal emulator with shell command history and autocomplete
 * - RootFS management with chroot/proot functionality
 * - Container engine with Docker-like capabilities and Python venv support
 * - UIYI process execution (TT-CCC-RCCC-LDU)
 * - PIPI approval system (Preview-Implement-Push-Implement)
 * - Screen-Hop operations coordination
 * - WebNetCaste AI integration
 * - Enhanced development environment with VM/container support
 */
@HiltViewModel
class DevUtilityViewModelV2 @Inject constructor(
    private val srirachaOrchestrator: SrirachaArmyOrchestrator,
    private val deepSeekEngine: DeepSeekEngine,
    private val terminalEmulator: LocalTerminalEmulator,
    private val rootfsManager: RootFSManager,
    private val codeEditor: CodeEditor,
    private val containerEngine: ContainerEngine,
    private val systemInfoManager: SystemInfoManager
) : ViewModel() {

    /**
     * Complete UI state for SrirachaArmy IDE with enhanced development capabilities
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
            "C#", "Swift", "Go", "Rust", "HTML", "CSS", "Shell", "Markdown"
        ),
        // Enhanced terminal and development environment state
        val terminalReady: Boolean = false,
        val rootfsReady: Boolean = false,
        val editorReady: Boolean = false,
        val containerEngineReady: Boolean = false,
        val openFiles: List<String> = emptyList(),
        val currentFile: String? = null,
        val availableDistributions: List<String> = emptyList(),
        val activeContainers: List<String> = emptyList(),
        val pythonEnvironments: List<String> = emptyList(),
        val commandHistory: List<String> = emptyList(),
        val terminalSuggestions: List<String> = emptyList()
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
     * Initialize ViewModel with SrirachaArmy systems and enhanced development environment
     */
    private fun initializeViewModelState() {
        viewModelScope.launch {
            Timber.d("🚀 Initializing SrirachaArmy ViewModel with enhanced development environment...")
            
            // Initialize all development components
            initializeDevelopmentEnvironment()
            
            // Combine all state flows into UI state
            combine(
                _currentCode,
                _selectedLanguage,
                srirachaOrchestrator.botStatuses,
                srirachaOrchestrator.currentHeatLevel,
                srirachaOrchestrator.coordinationPattern,
                deepSeekEngine.currentModel,
                deepSeekEngine.isOnline,
                _terminalOutput,
                terminalEmulator.terminalState,
                rootfsManager.rootfsState,
                codeEditor.editorState,
                containerEngine.engineState
            ) { currentCode, selectedLanguage, botStatuses, heatLevel, coordinationPattern, aiModel, isOnline, terminalOutput, terminalState, rootfsState, editorState, containerState ->
                
                _uiState.value.copy(
                    currentCode = currentCode,
                    selectedLanguage = selectedLanguage,
                    botStatuses = botStatuses,
                    currentHeatLevel = heatLevel,
                    coordinationPattern = coordinationPattern,
                    aiModel = aiModel,
                    isAIOnline = isOnline,
                    terminalOutput = terminalOutput,
                    activeBotCount = srirachaOrchestrator.getActiveBotCount(),
                    terminalReady = terminalState.toString().contains("Ready"),
                    rootfsReady = rootfsState.toString().contains("Ready"),
                    editorReady = editorState.toString().contains("Ready"),
                    containerEngineReady = containerState.toString().contains("Ready")
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
        
        Timber.d("✅ SrirachaArmy ViewModel initialized")
    }

    /**
     * Initialize enhanced development environment components
     */
    private suspend fun initializeDevelopmentEnvironment() {
        try {
            Timber.d("🔧 Initializing development environment components...")
            
            // Initialize system info manager first
            systemInfoManager.initialize()
            
            // Initialize terminal emulator
            terminalEmulator.initialize()
            
            // Initialize RootFS manager
            rootfsManager.initialize()
            
            // Initialize code editor
            codeEditor.initialize()
            
            // Initialize container engine
            containerEngine.initialize()
            
            // Setup terminal output listener
            viewModelScope.launch {
                terminalEmulator.terminalOutput.collect { output ->
                    addTerminalOutput(output)
                }
            }
            
            // Update state with component readiness
            updateComponentStates()
            
            Timber.d("✅ Development environment initialized successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to initialize development environment")
            addTerminalOutput("Error initializing development environment: ${e.message}")
        }
    }
    
    /**
     * Update component states in UI
     */
    private suspend fun updateComponentStates() {
        val distributions = rootfsManager.availableDistributions.value.map { it.name }
        val containers = containerEngine.runningContainers.value.map { it.name }
        val pythonEnvs = containerEngine.pythonEnvironments.value.map { it.name }
        val openFiles = codeEditor.openFiles.value.map { it.name }
        val currentFile = codeEditor.currentFile.value?.name
        
        _uiState.value = _uiState.value.copy(
            availableDistributions = distributions,
            activeContainers = containers,
            pythonEnvironments = pythonEnvs,
            openFiles = openFiles,
            currentFile = currentFile
        )
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
     * Execute terminal command with enhanced functionality
     */
    fun executeTerminalCommand(command: String) {
        viewModelScope.launch {
            when {
                command.startsWith("sriracha ") -> {
                    executeSrirachaCommand(command.substringAfter("sriracha "))
                }
                command.startsWith("devutil ") -> {
                    executeDevUtilCommand(command.substringAfter("devutil "))
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
                command == "status" -> {
                    showSystemStatus()
                }
                else -> {
                    // Use enhanced terminal emulator for command execution
                    terminalEmulator.executeCommand(command)
                    updateCommandHistory(command)
                }
            }
        }
    }
    
    /**
     * Execute DevUtility-specific commands for enhanced development environment
     */
    private fun executeDevUtilCommand(command: String) {
        viewModelScope.launch {
            when (command.trim()) {
                "containers" -> {
                    val containers = containerEngine.runningContainers.value
                    if (containers.isEmpty()) {
                        addTerminalOutput("No running containers")
                    } else {
                        addTerminalOutput("Running containers:")
                        containers.forEach { container ->
                            addTerminalOutput("  📦 ${container.name} (${container.state})")
                        }
                    }
                }
                "distributions" -> {
                    val distributions = rootfsManager.availableDistributions.value
                    if (distributions.isEmpty()) {
                        addTerminalOutput("No distributions available")
                    } else {
                        addTerminalOutput("Available distributions:")
                        distributions.forEach { dist ->
                            addTerminalOutput("  🐧 ${dist.displayName} (${dist.packageManager})")
                        }
                    }
                }
                "files" -> {
                    val files = codeEditor.openFiles.value
                    if (files.isEmpty()) {
                        addTerminalOutput("No files open")
                    } else {
                        addTerminalOutput("Open files:")
                        files.forEach { file ->
                            val status = if (file.isModified) " *" else ""
                            addTerminalOutput("  📄 ${file.name} (${file.language.displayName})$status")
                        }
                    }
                }
                "python-envs" -> {
                    val envs = containerEngine.pythonEnvironments.value
                    if (envs.isEmpty()) {
                        addTerminalOutput("No Python environments")
                    } else {
                        addTerminalOutput("Python virtual environments:")
                        envs.forEach { env ->
                            val active = if (env.isActive) " (active)" else ""
                            addTerminalOutput("  🐍 ${env.name} (${env.pythonVersion})$active")
                        }
                    }
                }
                "create-container" -> {
                    addTerminalOutput("Usage: devutil create-container <name> <distribution>")
                }
                "create-venv" -> {
                    addTerminalOutput("Usage: devutil create-venv <name> [python-version]")
                }
                else -> {
                    if (command.startsWith("create-container ")) {
                        val parts = command.split(" ")
                        if (parts.size >= 3) {
                            createContainer(parts[1], parts[2])
                        } else {
                            addTerminalOutput("Usage: devutil create-container <name> <distribution>")
                        }
                    } else if (command.startsWith("create-venv ")) {
                        val parts = command.split(" ")
                        if (parts.size >= 2) {
                            val pythonVersion = if (parts.size > 2) parts[2] else "3.9"
                            createPythonVenv(parts[1], pythonVersion)
                        } else {
                            addTerminalOutput("Usage: devutil create-venv <name> [python-version]")
                        }
                    } else {
                        addTerminalOutput("Unknown DevUtility command: $command")
                        addTerminalOutput("Available commands: containers, distributions, files, python-envs, create-container, create-venv")
                    }
            }
        }
    }
    
    /**
     * Create container via terminal command
     */
    private suspend fun createContainer(name: String, distributionName: String) {
        try {
            val distributions = rootfsManager.availableDistributions.value
            val distribution = distributions.find { it.name.equals(distributionName, ignoreCase = true) }
            
            if (distribution == null) {
                addTerminalOutput("❌ Distribution '$distributionName' not found")
                return
            }
            
            addTerminalOutput("🔧 Creating chroot environment for $name...")
            val environment = rootfsManager.createChrootEnvironment(distribution.name, name)
            
            if (environment != null) {
                addTerminalOutput("📦 Creating container...")
                val container = containerEngine.createContainer(name, environment)
                
                if (container != null) {
                    addTerminalOutput("✅ Container '$name' created successfully")
                    updateComponentStates()
                } else {
                    addTerminalOutput("❌ Failed to create container")
                }
            } else {
                addTerminalOutput("❌ Failed to create chroot environment")
            }
        } catch (e: Exception) {
            addTerminalOutput("❌ Error creating container: ${e.message}")
        }
    }
    
    /**
     * Create Python virtual environment via terminal command
     */
    private suspend fun createPythonVenv(name: String, pythonVersion: String) {
        try {
            addTerminalOutput("🐍 Creating Python virtual environment '$name'...")
            val env = containerEngine.createPythonVenv(name, pythonVersion)
            
            if (env != null) {
                addTerminalOutput("✅ Python environment '$name' created successfully")
                updateComponentStates()
            } else {
                addTerminalOutput("❌ Failed to create Python environment")
            }
        } catch (e: Exception) {
            addTerminalOutput("❌ Error creating Python environment: ${e.message}")
        }
    }
    
    /**
     * Show comprehensive system status
     */
    private fun showSystemStatus() {
        addTerminalOutput("🔧 DevUtility V2.5 System Status:")
        addTerminalOutput("  🖥️  Terminal: ${if (_uiState.value.terminalReady) "Ready" else "Not Ready"}")
        addTerminalOutput("  📁 RootFS: ${if (_uiState.value.rootfsReady) "Ready" else "Not Ready"}")
        addTerminalOutput("  📝 Editor: ${if (_uiState.value.editorReady) "Ready" else "Not Ready"}")
        addTerminalOutput("  📦 Containers: ${if (_uiState.value.containerEngineReady) "Ready" else "Not Ready"}")
        addTerminalOutput("  🐧 Distributions: ${_uiState.value.availableDistributions.size}")
        addTerminalOutput("  📦 Containers: ${_uiState.value.activeContainers.size}")
        addTerminalOutput("  🐍 Python Envs: ${_uiState.value.pythonEnvironments.size}")
        addTerminalOutput("  📄 Open Files: ${_uiState.value.openFiles.size}")
        showBotStatus()
    }
    
    /**
     * Update command history
     */
    private fun updateCommandHistory(command: String) {
        val currentHistory = _uiState.value.commandHistory.toMutableList()
        currentHistory.add(command)
        if (currentHistory.size > 50) {
            currentHistory.removeAt(0)
        }
        _uiState.value = _uiState.value.copy(commandHistory = currentHistory)
    }
    
    /**
     * Get command suggestions for autocomplete
     */
    fun getCommandSuggestions(partial: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // SrirachaArmy commands
        val srirachaCommands = listOf(
            "sriracha activate ssa", "sriracha activate ffa", "sriracha activate 5s",
            "sriracha activate 8s", "sriracha activate webnetcaste", "sriracha heat up",
            "sriracha cool down", "sriracha uiyi", "sriracha pipi"
        )
        
        // DevUtility commands
        val devutilCommands = listOf(
            "devutil containers", "devutil distributions", "devutil files",
            "devutil python-envs", "devutil create-container", "devutil create-venv"
        )
        
        // Basic commands
        val basicCommands = listOf("clear", "bots", "heat", "status")
        
        // Terminal emulator suggestions
        val terminalSuggestions = terminalEmulator.getCommandSuggestions(partial)
        
        suggestions.addAll(srirachaCommands.filter { it.startsWith(partial) })
        suggestions.addAll(devutilCommands.filter { it.startsWith(partial) })
        suggestions.addAll(basicCommands.filter { it.startsWith(partial) })
        suggestions.addAll(terminalSuggestions)
        
        return suggestions.distinct().take(10)
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
        viewModelScope.launch {
            // Save current editor file
            codeEditor.saveCurrentFile()
        }
    }

    /**
     * Cleanup systems (called from MainActivity onDestroy)
     */
    fun cleanupSystems() {
        Timber.d("🧹 Cleaning up SrirachaArmy systems")
        srirachaOrchestrator.deactivateAllBots()
        terminalEmulator.cleanup()
    }
    
    // ==== Enhanced Code Editor Integration ====
    
    /**
     * Open file in code editor
     */
    fun openFile(filePath: String) {
        viewModelScope.launch {
            val success = codeEditor.openFile(filePath)
            if (success) {
                addTerminalOutput("📄 Opened file: $filePath")
                updateComponentStates()
                
                // Update current code from editor
                val currentFile = codeEditor.currentFile.value
                if (currentFile != null) {
                    _currentCode.value = currentFile.content
                    _selectedLanguage.value = currentFile.language.displayName
                }
            } else {
                addTerminalOutput("❌ Failed to open file: $filePath")
            }
        }
    }
    
    /**
     * Create new file in editor
     */
    fun createNewFile(fileName: String) {
        viewModelScope.launch {
            val language = when (fileName.substringAfterLast('.', "").lowercase()) {
                "kt" -> com.spiralgang.srirachaarmy.devutility.editor.Language.KOTLIN
                "java" -> com.spiralgang.srirachaarmy.devutility.editor.Language.JAVA
                "py" -> com.spiralgang.srirachaarmy.devutility.editor.Language.PYTHON
                "js" -> com.spiralgang.srirachaarmy.devutility.editor.Language.JAVASCRIPT
                "html" -> com.spiralgang.srirachaarmy.devutility.editor.Language.HTML
                "css" -> com.spiralgang.srirachaarmy.devutility.editor.Language.CSS
                "md" -> com.spiralgang.srirachaarmy.devutility.editor.Language.MARKDOWN
                "sh" -> com.spiralgang.srirachaarmy.devutility.editor.Language.SHELL
                else -> com.spiralgang.srirachaarmy.devutility.editor.Language.PLAIN_TEXT
            }
            
            val file = codeEditor.createNewFile(fileName, language)
            addTerminalOutput("📄 Created new file: $fileName")
            updateComponentStates()
            
            _currentCode.value = ""
            _selectedLanguage.value = language.displayName
        }
    }
    
    /**
     * Save current file
     */
    fun saveCurrentFile() {
        viewModelScope.launch {
            val success = codeEditor.saveCurrentFile()
            if (success) {
                addTerminalOutput("💾 File saved successfully")
            } else {
                addTerminalOutput("❌ Failed to save file")
            }
            updateComponentStates()
        }
    }
    
    /**
     * Close file in editor
     */
    fun closeFile(filePath: String) {
        viewModelScope.launch {
            val success = codeEditor.closeFile(filePath)
            if (success) {
                addTerminalOutput("📄 Closed file: $filePath")
                updateComponentStates()
                
                // Update current code if no files are open
                val currentFile = codeEditor.currentFile.value
                if (currentFile != null) {
                    _currentCode.value = currentFile.content
                    _selectedLanguage.value = currentFile.language.displayName
                } else {
                    _currentCode.value = ""
                    _selectedLanguage.value = "Kotlin"
                }
            } else {
                addTerminalOutput("❌ Failed to close file: $filePath")
            }
        }
    }
    
    /**
     * Switch to a different file
     */
    fun switchToFile(filePath: String) {
        viewModelScope.launch {
            val success = codeEditor.switchToFile(filePath)
            if (success) {
                addTerminalOutput("📄 Switched to file: $filePath")
                
                val currentFile = codeEditor.currentFile.value
                if (currentFile != null) {
                    _currentCode.value = currentFile.content
                    _selectedLanguage.value = currentFile.language.displayName
                }
                
                updateComponentStates()
            } else {
                addTerminalOutput("❌ Failed to switch to file: $filePath")
            }
        }
    }
    
    /**
     * Update editor content and sync with UI
     */
    fun updateCodeWithEditor(code: String) {
        _currentCode.value = code
        codeEditor.updateContent(code)
        Timber.d("📝 Code updated: ${code.length} characters")
    }
    
    // ==== Container and Environment Management ====
    
    /**
     * Start container
     */
    fun startContainer(containerId: String) {
        viewModelScope.launch {
            val success = containerEngine.startContainer(containerId)
            if (success) {
                addTerminalOutput("🚀 Container started: $containerId")
            } else {
                addTerminalOutput("❌ Failed to start container: $containerId")
            }
            updateComponentStates()
        }
    }
    
    /**
     * Stop container
     */
    fun stopContainer(containerId: String) {
        viewModelScope.launch {
            val success = containerEngine.stopContainer(containerId)
            if (success) {
                addTerminalOutput("⏹️ Container stopped: $containerId")
            } else {
                addTerminalOutput("❌ Failed to stop container: $containerId")
            }
            updateComponentStates()
        }
    }
    
    /**
     * Execute command in container
     */
    fun executeInContainer(containerId: String, command: String) {
        viewModelScope.launch {
            val result = containerEngine.executeInContainer(containerId, command)
            if (result != null) {
                addTerminalOutput("📦 Container output:")
                addTerminalOutput(result)
            } else {
                addTerminalOutput("❌ Failed to execute command in container")
            }
        }
    }
    
    /**
     * Activate Python virtual environment
     */
    fun activatePythonVenv(name: String) {
        viewModelScope.launch {
            val success = containerEngine.activatePythonVenv(name)
            if (success) {
                addTerminalOutput("🐍 Activated Python environment: $name")
            } else {
                addTerminalOutput("❌ Failed to activate Python environment: $name")
            }
            updateComponentStates()
        }
    }
    
    /**
     * Get system information for display in UI
     */
    fun getSystemInfo() = systemInfoManager.systemInfo
    
    /**
     * Get system health status
     */
    fun getHealthStatus() = systemInfoManager.healthStatus
    
    /**
     * Perform system health check
     */
    fun performHealthCheck() {
        viewModelScope.launch {
            systemInfoManager.performHealthCheck()
        }
    }
}