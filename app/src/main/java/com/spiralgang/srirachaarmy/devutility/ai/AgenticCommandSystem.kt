package com.spiralgang.srirachaarmy.devutility.ai

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AgenticCommandSystem - Where shell commands become AI conversations
 * 
 * This system transforms traditional shell commands into intelligent, context-aware
 * interactions. Every command is enhanced with AI understanding, and the AI can
 * proactively suggest commands based on development patterns and context.
 */
@Singleton
class AgenticCommandSystem @Inject constructor(
    private val context: Context
) {
    
    // AI-enhanced command history that learns patterns
    private val _agenticCommandHistory = MutableStateFlow<List<AgenticCommand>>(emptyList())
    val agenticCommandHistory: StateFlow<List<AgenticCommand>> = _agenticCommandHistory.asStateFlow()
    
    // Smart command suggestions based on context
    private val _commandSuggestions = MutableStateFlow<List<SmartSuggestion>>(emptyList())
    val commandSuggestions: StateFlow<List<SmartSuggestion>> = _commandSuggestions.asStateFlow()
    
    // Active development context that influences command interpretation
    private val _developmentContext = MutableStateFlow(DevelopmentContext())
    val developmentContext: StateFlow<DevelopmentContext> = _developmentContext.asStateFlow()
    
    data class AgenticCommand(
        val originalInput: String,
        val interpretedIntent: CommandIntent,
        val aiEnhancedCommand: String,
        val contextualInsight: String,
        val executionResult: String,
        val learningValue: Float,
        val timestamp: Long = System.currentTimeMillis(),
        val followUpSuggestions: List<String> = emptyList()
    )
    
    data class SmartSuggestion(
        val command: String,
        val description: String,
        val confidence: Float,
        val reasoning: String,
        val category: SuggestionCategory,
        val contextTags: List<String>
    )
    
    data class DevelopmentContext(
        val currentDirectory: String = "/",
        val projectType: String = "unknown",
        val activeFiles: List<String> = emptyList(),
        val recentCommands: List<String> = emptyList(),
        val buildSystem: String = "unknown",
        val languageContext: List<String> = emptyList(),
        val gitStatus: String = "unknown",
        val currentBranch: String = "unknown"
    )
    
    enum class CommandIntent {
        FILE_NAVIGATION,      // cd, ls, find, etc.
        CODE_ANALYSIS,        // grep, cat, head, tail, etc.
        BUILD_OPERATIONS,     // gradle, make, npm, etc.
        GIT_OPERATIONS,       // git commands
        SYSTEM_MONITORING,    // ps, top, df, etc.
        DEVELOPMENT_TOOLS,    // vim, code, etc.
        AI_COLLABORATION,     // Custom AI commands
        EXPLORATION,          // Learning about the environment
        AUTOMATION,           // Scripts and batch operations
        UNKNOWN              // Unrecognized patterns
    }
    
    enum class SuggestionCategory {
        NEXT_LOGICAL_STEP,
        OPTIMIZATION,
        TROUBLESHOOTING,
        LEARNING,
        AUTOMATION,
        BEST_PRACTICE
    }
    
    /**
     * Process a command with full AI enhancement
     * This is where traditional shell commands become intelligent conversations
     */
    suspend fun processAgenticCommand(input: String): AgenticCommand {
        Timber.d("Processing agentic command: $input")
        
        // Analyze the command intent
        val intent = analyzeCommandIntent(input)
        
        // Get current development context
        val context = _developmentContext.value
        
        // Generate AI-enhanced version of the command
        val enhancedCommand = enhanceCommandWithAI(input, intent, context)
        
        // Provide contextual insight
        val insight = generateContextualInsight(input, intent, context)
        
        // Execute the command (simulate for now)
        val result = executeCommand(enhancedCommand)
        
        // Calculate learning value
        val learningValue = calculateLearningValue(input, intent, result)
        
        // Generate follow-up suggestions
        val followUps = generateFollowUpSuggestions(input, intent, result, context)
        
        val agenticCommand = AgenticCommand(
            originalInput = input,
            interpretedIntent = intent,
            aiEnhancedCommand = enhancedCommand,
            contextualInsight = insight,
            executionResult = result,
            learningValue = learningValue,
            followUpSuggestions = followUps
        )
        
        // Add to history and learn from it
        addToHistory(agenticCommand)
        updateContextFromCommand(agenticCommand)
        generateSmartSuggestions()
        
        return agenticCommand
    }
    
    /**
     * Get proactive command suggestions based on current context
     */
    suspend fun getProactiveCommandSuggestions(): List<SmartSuggestion> {
        val context = _developmentContext.value
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Analyze current context and suggest relevant commands
        when {
            context.projectType == "android" -> {
                suggestions.addAll(getAndroidSpecificSuggestions(context))
            }
            context.gitStatus == "dirty" -> {
                suggestions.addAll(getGitWorkflowSuggestions(context))
            }
            context.currentDirectory.contains("src") -> {
                suggestions.addAll(getCodeAnalysisSuggestions(context))
            }
        }
        
        // Add learning-based suggestions
        suggestions.addAll(getPatternBasedSuggestions(context))
        
        return suggestions.take(5) // Keep it focused
    }
    
    private fun analyzeCommandIntent(input: String): CommandIntent {
        return when {
            input.matches(Regex("^(cd|ls|find|locate|pwd).*")) -> CommandIntent.FILE_NAVIGATION
            input.matches(Regex("^(grep|cat|head|tail|less|more|wc).*")) -> CommandIntent.CODE_ANALYSIS
            input.matches(Regex("^(gradle|./gradlew|make|npm|yarn|mvn).*")) -> CommandIntent.BUILD_OPERATIONS
            input.matches(Regex("^git.*")) -> CommandIntent.GIT_OPERATIONS
            input.matches(Regex("^(ps|top|htop|df|du|free).*")) -> CommandIntent.SYSTEM_MONITORING
            input.matches(Regex("^(vim|nano|code|emacs).*")) -> CommandIntent.DEVELOPMENT_TOOLS
            input.startsWith("sriracha") -> CommandIntent.AI_COLLABORATION
            input.contains("help") || input.contains("?") -> CommandIntent.EXPLORATION
            input.contains("&&") || input.contains(";") -> CommandIntent.AUTOMATION
            else -> CommandIntent.UNKNOWN
        }
    }
    
    private fun enhanceCommandWithAI(input: String, intent: CommandIntent, context: DevelopmentContext): String {
        return when (intent) {
            CommandIntent.FILE_NAVIGATION -> enhanceNavigationCommand(input, context)
            CommandIntent.CODE_ANALYSIS -> enhanceAnalysisCommand(input, context)
            CommandIntent.BUILD_OPERATIONS -> enhanceBuildCommand(input, context)
            CommandIntent.GIT_OPERATIONS -> enhanceGitCommand(input, context)
            CommandIntent.AI_COLLABORATION -> enhanceAICommand(input, context)
            else -> input // Keep original for unrecognized commands
        }
    }
    
    private fun enhanceNavigationCommand(input: String, context: DevelopmentContext): String {
        return when {
            input == "ls" -> "ls -la --color=auto" // Enhanced with details and colors
            input == "cd" -> "cd ${guessIntendedDirectory(context)}" // Smart directory guessing
            input.startsWith("find") && !input.contains("-type") -> "$input -type f" // Default to files
            else -> input
        }
    }
    
    private fun enhanceAnalysisCommand(input: String, context: DevelopmentContext): String {
        return when {
            input.startsWith("grep") && !input.contains("-n") -> "$input -n" // Add line numbers
            input == "cat" && context.activeFiles.isNotEmpty() -> "cat ${context.activeFiles.first()}"
            else -> input
        }
    }
    
    private fun enhanceBuildCommand(input: String, context: DevelopmentContext): String {
        return when {
            input == "gradle" && context.buildSystem == "android" -> "./gradlew assembleDebug"
            input == "build" -> when (context.projectType) {
                "android" -> "./gradlew build"
                "node" -> "npm run build"
                "python" -> "python setup.py build"
                else -> input
            }
            else -> input
        }
    }
    
    private fun enhanceGitCommand(input: String, context: DevelopmentContext): String {
        return when {
            input == "git status" -> "git status --short --branch"
            input == "git log" -> "git log --oneline -10"
            input == "git diff" -> "git diff --stat"
            else -> input
        }
    }
    
    private fun enhanceAICommand(input: String, context: DevelopmentContext): String {
        // Handle SrirachaArmy-specific commands
        return when {
            input.contains("sriracha status") -> "Show comprehensive AI and development status"
            input.contains("sriracha analyze") -> "Perform deep analysis of current project context"
            input.contains("sriracha suggest") -> "Generate contextual development suggestions"
            else -> input
        }
    }
    
    private fun generateContextualInsight(input: String, intent: CommandIntent, context: DevelopmentContext): String {
        return when (intent) {
            CommandIntent.FILE_NAVIGATION -> generateNavigationInsight(input, context)
            CommandIntent.CODE_ANALYSIS -> generateAnalysisInsight(input, context)
            CommandIntent.BUILD_OPERATIONS -> generateBuildInsight(input, context)
            CommandIntent.GIT_OPERATIONS -> generateGitInsight(input, context)
            CommandIntent.AI_COLLABORATION -> generateAIInsight(input, context)
            else -> "🤖 Processing command in current development context"
        }
    }
    
    private fun generateNavigationInsight(input: String, context: DevelopmentContext): String {
        return when {
            input.startsWith("cd") -> "🗂️ Navigating in ${context.projectType} project structure"
            input.startsWith("ls") -> "📋 Exploring directory contents - I can help analyze what you find"
            input.startsWith("find") -> "🔍 Searching codebase - want me to analyze patterns in the results?"
            else -> "🧭 Navigating project structure with AI awareness"
        }
    }
    
    private fun generateAnalysisInsight(input: String, context: DevelopmentContext): String {
        return when {
            input.startsWith("grep") -> "🔍 Pattern search in ${context.languageContext.joinToString(", ")} context"
            input.startsWith("cat") -> "📖 Examining file content - I can provide code analysis"
            else -> "🧠 Analyzing code with contextual understanding"
        }
    }
    
    private fun generateBuildInsight(input: String, context: DevelopmentContext): String {
        return "🔧 Build operation for ${context.projectType} project - I'll monitor for issues"
    }
    
    private fun generateGitInsight(input: String, context: DevelopmentContext): String {
        return "📝 Git operation on ${context.currentBranch} branch - maintaining development workflow"
    }
    
    private fun generateAIInsight(input: String, context: DevelopmentContext): String {
        return "🌶️ SrirachaArmy AI collaboration in ${context.projectType} context"
    }
    
    private suspend fun executeCommand(command: String): String {
        // Simulate command execution for now
        return when {
            command.contains("ls") -> "📁 app/  📁 docs/  📁 scripts/  📄 README.md  📄 build.gradle"
            command.contains("git status") -> "🔄 On branch main\n✅ Working tree clean"
            command.contains("gradle") -> "🔧 BUILD SUCCESSFUL in 2s"
            command.contains("sriracha") -> "🌶️ SrirachaArmy systems online - AI ready for collaboration"
            else -> "✅ Command executed successfully"
        }
    }
    
    private fun calculateLearningValue(input: String, intent: CommandIntent, result: String): Float {
        return when (intent) {
            CommandIntent.AI_COLLABORATION -> 0.9f
            CommandIntent.BUILD_OPERATIONS -> 0.8f
            CommandIntent.CODE_ANALYSIS -> 0.7f
            CommandIntent.GIT_OPERATIONS -> 0.6f
            CommandIntent.EXPLORATION -> 0.8f
            else -> 0.4f
        }
    }
    
    private fun generateFollowUpSuggestions(
        input: String, 
        intent: CommandIntent, 
        result: String, 
        context: DevelopmentContext
    ): List<String> {
        return when (intent) {
            CommandIntent.FILE_NAVIGATION -> listOf(
                "🔍 Analyze files in this directory",
                "📝 Edit the most relevant file",
                "🔧 Run build commands here"
            )
            CommandIntent.CODE_ANALYSIS -> listOf(
                "🧠 Get AI explanation of patterns found",
                "✨ Suggest code improvements",
                "🔍 Search for related patterns"
            )
            CommandIntent.BUILD_OPERATIONS -> listOf(
                "📊 Analyze build output",
                "🚀 Deploy or test the build",
                "🔍 Check for optimization opportunities"
            )
            else -> listOf("🤖 Ask AI for next steps", "📚 Learn more about this command")
        }
    }
    
    private fun addToHistory(command: AgenticCommand) {
        val currentHistory = _agenticCommandHistory.value.toMutableList()
        currentHistory.add(0, command) // Add to beginning
        
        // Keep history manageable
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        _agenticCommandHistory.value = currentHistory
    }
    
    private fun updateContextFromCommand(command: AgenticCommand) {
        val currentContext = _developmentContext.value
        
        // Update context based on command execution
        val updatedContext = when (command.interpretedIntent) {
            CommandIntent.FILE_NAVIGATION -> {
                if (command.originalInput.startsWith("cd")) {
                    currentContext.copy(
                        currentDirectory = extractDirectoryFromCd(command.originalInput),
                        recentCommands = updateRecentCommands(currentContext.recentCommands, command.originalInput)
                    )
                } else {
                    currentContext.copy(
                        recentCommands = updateRecentCommands(currentContext.recentCommands, command.originalInput)
                    )
                }
            }
            CommandIntent.GIT_OPERATIONS -> {
                currentContext.copy(
                    gitStatus = extractGitStatus(command.executionResult),
                    recentCommands = updateRecentCommands(currentContext.recentCommands, command.originalInput)
                )
            }
            else -> {
                currentContext.copy(
                    recentCommands = updateRecentCommands(currentContext.recentCommands, command.originalInput)
                )
            }
        }
        
        _developmentContext.value = updatedContext
    }
    
    private suspend fun generateSmartSuggestions() {
        val context = _developmentContext.value
        val history = _agenticCommandHistory.value
        
        val suggestions = mutableListOf<SmartSuggestion>()
        
        // Pattern-based suggestions
        val recentPatterns = analyzeRecentPatterns(history)
        suggestions.addAll(createPatternSuggestions(recentPatterns, context))
        
        // Context-based suggestions
        suggestions.addAll(createContextSuggestions(context))
        
        _commandSuggestions.value = suggestions
    }
    
    private fun getAndroidSpecificSuggestions(context: DevelopmentContext): List<SmartSuggestion> {
        return listOf(
            SmartSuggestion(
                command = "./gradlew assembleDebug",
                description = "Build debug APK",
                confidence = 0.9f,
                reasoning = "Common Android development task",
                category = SuggestionCategory.NEXT_LOGICAL_STEP,
                contextTags = listOf("android", "build")
            ),
            SmartSuggestion(
                command = "adb devices",
                description = "Check connected Android devices",
                confidence = 0.8f,
                reasoning = "Useful for Android development",
                category = SuggestionCategory.BEST_PRACTICE,
                contextTags = listOf("android", "debug")
            )
        )
    }
    
    private fun getGitWorkflowSuggestions(context: DevelopmentContext): List<SmartSuggestion> {
        return listOf(
            SmartSuggestion(
                command = "git add .",
                description = "Stage all changes",
                confidence = 0.9f,
                reasoning = "Working tree is dirty",
                category = SuggestionCategory.NEXT_LOGICAL_STEP,
                contextTags = listOf("git", "workflow")
            ),
            SmartSuggestion(
                command = "git status --short",
                description = "Quick status check",
                confidence = 0.8f,
                reasoning = "Review changes before committing",
                category = SuggestionCategory.BEST_PRACTICE,
                contextTags = listOf("git", "status")
            )
        )
    }
    
    private fun getCodeAnalysisSuggestions(context: DevelopmentContext): List<SmartSuggestion> {
        return listOf(
            SmartSuggestion(
                command = "find . -name '*.kt' | head -10",
                description = "List Kotlin files",
                confidence = 0.8f,
                reasoning = "In source directory, likely working with Kotlin",
                category = SuggestionCategory.EXPLORATION,
                contextTags = listOf("kotlin", "analysis")
            )
        )
    }
    
    private fun getPatternBasedSuggestions(context: DevelopmentContext): List<SmartSuggestion> {
        // Analyze command patterns and suggest similar useful commands
        return emptyList() // Placeholder for pattern analysis
    }
    
    // Helper functions
    private fun guessIntendedDirectory(context: DevelopmentContext): String {
        return when {
            context.recentCommands.any { it.contains("gradle") } -> "./app/src/main"
            context.recentCommands.any { it.contains("git") } -> "./"
            else -> "~"
        }
    }
    
    private fun extractDirectoryFromCd(command: String): String {
        return command.substringAfter("cd ").trim().ifEmpty { "/" }
    }
    
    private fun extractGitStatus(result: String): String {
        return when {
            result.contains("clean") -> "clean"
            result.contains("modified") -> "dirty"
            else -> "unknown"
        }
    }
    
    private fun updateRecentCommands(current: List<String>, newCommand: String): List<String> {
        val updated = listOf(newCommand) + current
        return updated.take(10) // Keep last 10 commands
    }
    
    private fun analyzeRecentPatterns(history: List<AgenticCommand>): List<String> {
        // Analyze patterns in recent command history
        return history.take(10).map { it.interpretedIntent.name }
    }
    
    private fun createPatternSuggestions(patterns: List<String>, context: DevelopmentContext): List<SmartSuggestion> {
        // Create suggestions based on observed patterns
        return emptyList() // Placeholder
    }
    
    private fun createContextSuggestions(context: DevelopmentContext): List<SmartSuggestion> {
        // Create suggestions based on current context
        return emptyList() // Placeholder
    }
}