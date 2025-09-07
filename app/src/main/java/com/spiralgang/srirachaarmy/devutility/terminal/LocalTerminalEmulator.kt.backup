package com.spiralgang.srirachaarmy.devutility.terminal

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local terminal emulator for DevUtility V2.5
 * Provides bash/zsh shell capabilities with command history and autocomplete
 * Integrated with existing AI orchestration and security sandbox
 */
@Singleton
class LocalTerminalEmulator @Inject constructor(
    private val context: Context
) {
    private val _terminalOutput = MutableSharedFlow<String>()
    val terminalOutput: SharedFlow<String> = _terminalOutput.asSharedFlow()
    
    private val _terminalState = MutableStateFlow(TerminalState.Idle)
    val terminalState: StateFlow<TerminalState> = _terminalState
    
    private var currentProcess: Process? = null
    private var processWriter: OutputStreamWriter? = null
    private var processReader: BufferedReader? = null
    
    private val commandHistory = mutableListOf<String>()
    private val shellAliases = mutableMapOf<String, String>()
    private var currentWorkingDirectory = "/data/data/${context.packageName}"
    
    companion object {
        private const val MAX_HISTORY_SIZE = 1000
        private const val SHELL_PROMPT = "devutility:$ "
    }
    
    /**
     * Initialize terminal with shell environment
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            _terminalState.value = TerminalState.Initializing
            
            // Setup default aliases for enhanced functionality
            setupDefaultAliases()
            
            // Start shell process
            startShellProcess()
            
            _terminalState.value = TerminalState.Ready
            _terminalOutput.emit("DevUtility V2.5 Terminal Initialized")
            _terminalOutput.emit("Type 'help' for available commands")
            _terminalOutput.emit(SHELL_PROMPT)
            
        } catch (e: Exception) {
            _terminalState.value = TerminalState.Error(e.message ?: "Terminal initialization failed")
            Timber.e(e, "Terminal initialization failed")
        }
    }
    
    /**
     * Execute terminal command with enhanced functionality
     */
    suspend fun executeCommand(command: String) = withContext(Dispatchers.IO) {
        if (_terminalState.value != TerminalState.Ready) {
            _terminalOutput.emit("Terminal not ready. Current state: ${_terminalState.value}")
            return@withContext
        }
        
        // Add to command history
        addToHistory(command.trim())
        
        val processedCommand = processCommand(command.trim())
        
        try {
            when {
                processedCommand.startsWith("cd ") -> {
                    handleChangeDirectory(processedCommand.substring(3).trim())
                }
                processedCommand == "pwd" -> {
                    _terminalOutput.emit(currentWorkingDirectory)
                    _terminalOutput.emit(SHELL_PROMPT)
                }
                processedCommand == "history" -> {
                    showCommandHistory()
                }
                processedCommand.startsWith("alias ") -> {
                    handleAlias(processedCommand.substring(6).trim())
                }
                processedCommand == "help" -> {
                    showHelp()
                }
                processedCommand == "clear" -> {
                    _terminalOutput.emit("\u001B[2J\u001B[H") // ANSI clear screen
                    _terminalOutput.emit(SHELL_PROMPT)
                }
                else -> {
                    executeShellCommand(processedCommand)
                }
            }
        } catch (e: Exception) {
            _terminalOutput.emit("Error executing command: ${e.message}")
            _terminalOutput.emit(SHELL_PROMPT)
            Timber.e(e, "Command execution failed: $processedCommand")
        }
    }
    
    /**
     * Get command suggestions for autocomplete
     */
    fun getCommandSuggestions(partial: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Built-in commands
        val builtinCommands = listOf(
            "cd", "pwd", "ls", "cat", "echo", "grep", "find", "ps", "kill",
            "history", "alias", "help", "clear", "exit", "chmod", "chown",
            "mkdir", "rmdir", "rm", "mv", "cp", "touch", "which", "whereis"
        )
        
        // Add matching built-in commands
        suggestions.addAll(builtinCommands.filter { it.startsWith(partial) })
        
        // Add matching aliases
        suggestions.addAll(shellAliases.keys.filter { it.startsWith(partial) })
        
        // Add matching history commands
        suggestions.addAll(commandHistory.filter { it.startsWith(partial) }.distinct().take(5))
        
        return suggestions.sorted().take(10)
    }
    
    /**
     * Process command for aliases and built-in functionality
     */
    private fun processCommand(command: String): String {
        if (command.isEmpty()) return command
        
        val parts = command.split(" ")
        val cmd = parts[0]
        
        // Check for alias substitution
        return if (shellAliases.containsKey(cmd)) {
            val aliasValue = shellAliases[cmd]!!
            if (parts.size > 1) {
                "$aliasValue ${parts.drop(1).joinToString(" ")}"
            } else {
                aliasValue
            }
        } else {
            command
        }
    }
    
    /**
     * Execute shell command through process
     */
    private suspend fun executeShellCommand(command: String) {
        try {
            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
            processBuilder.directory(File(currentWorkingDirectory))
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                _terminalOutput.emit(line!!)
            }
            
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                _terminalOutput.emit("Command exited with code: $exitCode")
            }
            
            reader.close()
            _terminalOutput.emit(SHELL_PROMPT)
            
        } catch (e: Exception) {
            _terminalOutput.emit("Error: ${e.message}")
            _terminalOutput.emit(SHELL_PROMPT)
        }
    }
    
    /**
     * Handle directory change command
     */
    private suspend fun handleChangeDirectory(path: String) {
        val targetDir = if (path.startsWith("/")) {
            path
        } else {
            File(currentWorkingDirectory, path).canonicalPath
        }
        
        val dir = File(targetDir)
        if (dir.exists() && dir.isDirectory) {
            currentWorkingDirectory = targetDir
            _terminalOutput.emit("Changed directory to: $currentWorkingDirectory")
        } else {
            _terminalOutput.emit("cd: $targetDir: No such file or directory")
        }
        _terminalOutput.emit(SHELL_PROMPT)
    }
    
    /**
     * Handle alias command
     */
    private suspend fun handleAlias(aliasCommand: String) {
        if (aliasCommand.isEmpty()) {
            // Show all aliases
            if (shellAliases.isEmpty()) {
                _terminalOutput.emit("No aliases defined")
            } else {
                shellAliases.forEach { (alias, command) ->
                    _terminalOutput.emit("$alias='$command'")
                }
            }
        } else if (aliasCommand.contains("=")) {
            // Set alias
            val parts = aliasCommand.split("=", limit = 2)
            if (parts.size == 2) {
                val alias = parts[0].trim()
                val command = parts[1].trim().removeSurrounding("'", "\"")
                shellAliases[alias] = command
                _terminalOutput.emit("Alias set: $alias='$command'")
            }
        } else {
            // Show specific alias
            val command = shellAliases[aliasCommand]
            if (command != null) {
                _terminalOutput.emit("$aliasCommand='$command'")
            } else {
                _terminalOutput.emit("alias: $aliasCommand: not found")
            }
        }
        _terminalOutput.emit(SHELL_PROMPT)
    }
    
    /**
     * Show command history
     */
    private suspend fun showCommandHistory() {
        if (commandHistory.isEmpty()) {
            _terminalOutput.emit("No command history")
        } else {
            commandHistory.forEachIndexed { index, command ->
                _terminalOutput.emit("${index + 1}  $command")
            }
        }
        _terminalOutput.emit(SHELL_PROMPT)
    }
    
    /**
     * Show help information
     */
    private suspend fun showHelp() {
        _terminalOutput.emit("DevUtility V2.5 Terminal Help")
        _terminalOutput.emit("========================")
        _terminalOutput.emit("Built-in commands:")
        _terminalOutput.emit("  cd <dir>     - Change directory")
        _terminalOutput.emit("  pwd          - Print working directory")
        _terminalOutput.emit("  history      - Show command history")
        _terminalOutput.emit("  alias        - Manage command aliases")
        _terminalOutput.emit("  help         - Show this help")
        _terminalOutput.emit("  clear        - Clear terminal")
        _terminalOutput.emit("  exit         - Exit terminal")
        _terminalOutput.emit("")
        _terminalOutput.emit("Additional commands are executed through the system shell.")
        _terminalOutput.emit("Use Tab for autocompletion (when supported by UI).")
        _terminalOutput.emit(SHELL_PROMPT)
    }
    
    /**
     * Setup default aliases for enhanced functionality
     */
    private fun setupDefaultAliases() {
        shellAliases.apply {
            put("ll", "ls -la")
            put("la", "ls -A")
            put("l", "ls -CF")
            put("...", "cd ../..")
            put("....", "cd ../../..")
            put("h", "history")
            put("c", "clear")
            put("q", "exit")
            put("grep", "grep --color=auto")
            put("fgrep", "fgrep --color=auto")
            put("egrep", "egrep --color=auto")
        }
    }
    
    /**
     * Add command to history
     */
    private fun addToHistory(command: String) {
        if (command.isNotEmpty() && command != commandHistory.lastOrNull()) {
            commandHistory.add(command)
            if (commandHistory.size > MAX_HISTORY_SIZE) {
                commandHistory.removeAt(0)
            }
        }
    }
    
    /**
     * Start shell process for advanced command execution
     */
    private suspend fun startShellProcess() {
        // Implementation would start a persistent shell process
        // For now, we use individual command execution
        Timber.d("Shell process started in directory: $currentWorkingDirectory")
    }
    
    /**
     * Clean up terminal resources
     */
    fun cleanup() {
        currentProcess?.destroy()
        processWriter?.close()
        processReader?.close()
        _terminalState.value = TerminalState.Idle
    }
}

/**
 * Terminal state enumeration
 */
sealed class TerminalState {
    object Idle : TerminalState()
    object Initializing : TerminalState()
    object Ready : TerminalState()
    data class Error(val message: String) : TerminalState()
}