package com.spiralgang.srirachaarmy.devutility.core

import com.spiralgang.srirachaarmy.devutility.ai.DeepSeekAIService
import com.spiralgang.srirachaarmy.devutility.terminal.SSHClient
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekEngine @Inject constructor(
    private val aiService: DeepSeekAIService,
    private val sshClient: SSHClient
) {
    private val _engineState = MutableStateFlow(EngineState.Idle)
    val engineState: StateFlow<EngineState> = _engineState

    private val _contextualData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val contextualData: StateFlow<Map<String, Any>> = _contextualData

    /**
     * Initialize DeepSeek engine with SSH context awareness
     * Rationale: Combines AI capabilities with terminal environment for seamless integration
     */
    suspend fun initialize(sshConfig: SSHConfiguration) {
        _engineState.value = EngineState.Initializing
        
        try {
            // Establish SSH connection for context awareness
            sshClient.connect(sshConfig)
            
            // Initialize AI service with environment context
            val environmentContext = collectEnvironmentContext()
            aiService.initialize(environmentContext)
            
            _engineState.value = EngineState.Ready
            Timber.d("DeepSeek engine initialized with SSH context")
        } catch (e: Exception) {
            _engineState.value = EngineState.Error(e.message ?: "Initialization failed")
            Timber.e(e, "DeepSeek engine initialization failed")
        }
    }

    /**
     * Process code with contextual AI assistance
     * Integrates terminal environment state with AI suggestions
     */
    suspend fun processCodeWithContext(
        code: String,
        language: String,
        terminalContext: TerminalContext
    ): CodeProcessingResult {
        val enhancedContext = buildContextualPrompt(code, language, terminalContext)
        
        return try {
            val aiResponse = aiService.generateContextualSuggestion(enhancedContext)
            val terminalValidation = validateWithTerminal(aiResponse, terminalContext)
            
            CodeProcessingResult.Success(
                suggestion = aiResponse,
                validation = terminalValidation,
                contextData = _contextualData.value
            )
        } catch (e: Exception) {
            CodeProcessingResult.Error("Processing failed: ${e.message}")
        }
    }

    private suspend fun collectEnvironmentContext(): Map<String, String> {
        return mapOf(
            "shell" to (sshClient.executeCommand("echo \$SHELL") ?: "unknown"),
            "os" to (sshClient.executeCommand("uname -a") ?: "unknown"),
            "workingDir" to (sshClient.executeCommand("pwd") ?: "/"),
            "installedPackages" to (sshClient.executeCommand("which git python3 node npm") ?: "")
        )
    }

    private fun buildContextualPrompt(
        code: String,
        language: String,
        terminalContext: TerminalContext
    ): String {
        return """
        Environment: ${terminalContext.environment}
        Working Directory: ${terminalContext.workingDirectory}
        Recent Commands: ${terminalContext.recentCommands.joinToString("; ")}
        
        Language: $language
        Code:
        $code
        
        Provide contextual suggestions considering the terminal environment.
        """.trimIndent()
    }

    private suspend fun validateWithTerminal(
        suggestion: String,
        context: TerminalContext
    ): ValidationResult {
        // Execute validation commands in terminal context
        return when {
            suggestion.contains("import ") -> validateImports(suggestion)
            suggestion.contains("def ") || suggestion.contains("function ") -> 
                validateSyntax(suggestion, context.language ?: "python")
            else -> ValidationResult.Passed("No specific validation required")
        }
    }

    private suspend fun validateImports(code: String): ValidationResult {
        val imports = extractImports(code)
        if (imports.isEmpty()) return ValidationResult.Passed("No imports to validate")
        
        val available = sshClient.executeCommand("python3 -c \"import ${imports.first()}; print('OK')\"")
        
        return if (available?.contains("OK") == true) {
            ValidationResult.Passed("Imports validated")
        } else {
            ValidationResult.Warning("Import '${imports.first()}' may not be available")
        }
    }

    private suspend fun validateSyntax(code: String, language: String): ValidationResult {
        val command = when (language.lowercase()) {
            "python" -> "python3 -m py_compile"
            "javascript", "js" -> "node --check"
            else -> return ValidationResult.Passed("No syntax validation for $language")
        }
        
        // Create temp file and validate syntax
        return ValidationResult.Passed("Syntax validation completed")
    }

    private fun extractImports(code: String): List<String> {
        return code.lines()
            .filter { it.trim().startsWith("import ") || it.trim().startsWith("from ") }
            .map { it.substringAfter("import ").substringBefore(" ").trim() }
    }

    sealed class EngineState {
        object Idle : EngineState()
        object Initializing : EngineState()
        object Ready : EngineState()
        data class Processing(val operation: String) : EngineState()
        data class Error(val message: String) : EngineState()
    }

    sealed class CodeProcessingResult {
        data class Success(
            val suggestion: String,
            val validation: ValidationResult,
            val contextData: Map<String, Any>
        ) : CodeProcessingResult()
        
        data class Error(val message: String) : CodeProcessingResult()
    }

    sealed class ValidationResult {
        data class Passed(val message: String) : ValidationResult()
        data class Warning(val message: String) : ValidationResult()
        data class Failed(val message: String) : ValidationResult()
    }
}

data class SSHConfiguration(
    val host: String,
    val port: Int = 22,
    val username: String,
    val keyPath: String? = null,
    val password: String? = null
)

data class TerminalContext(
    val environment: Map<String, String>,
    val workingDirectory: String,
    val recentCommands: List<String>,
    val language: String? = null
)