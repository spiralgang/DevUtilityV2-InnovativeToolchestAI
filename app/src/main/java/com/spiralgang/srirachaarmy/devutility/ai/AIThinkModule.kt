package com.spiralgang.srirachaarmy.devutility.ai

import com.spiralgang.srirachaarmy.devutility.ai.core.AIGuidanceSystem
import com.spiralgang.srirachaarmy.devutility.ai.core.TaskStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AIThinkModule - Executive Planner component of AIGuideNet
 * 
 * Enhanced from a simple "thinking" module into the central Executive Planner.
 * This module evolves beyond learning principles (SOAR/ACT-R/CLARION) to execute
 * a concrete planning loop with structured task decomposition and tool orchestration.
 * 
 * Core responsibilities:
 * - Goal Parser & Decomposer: Breaks down user prompts into hierarchical sub-goals
 * - Task State Manager integration: Manages current task status and intermediate results
 * - Action Selector & Router: Maps tasks to specific tools and internal functions
 * - Plan Validation & Prioritization: Evaluates actions against constraints and policies
 * 
 * Part of the Autonomous Internal Guidance & Routing Network (AIGuideNet)
 */
@Singleton
class AIThinkModule @Inject constructor(
    private val taskStateManager: TaskStateManager,
    private val aiGuidanceSystem: AIGuidanceSystem
) {

    /**
     * Legacy knowledge base for backward compatibility
     */
    private val knowledgeBase = mutableMapOf<String, String>()
    private val interactionHistory = mutableListOf<String>()
    private val contextPatterns = mutableMapOf<String, Int>()

    /**
     * Planning states - enhanced from simple thinking states
     */
    enum class ThinkingState {
        IDLE,
        PARSING_GOAL,
        DECOMPOSING_TASK,
        SELECTING_ACTION,
        VALIDATING_PLAN,
        EXECUTING,
        REFLECTING,
        LEARNING
    }

    private var currentState = ThinkingState.IDLE

    data class ActionPlan(
        val id: String,
        val goalDescription: String,
        val subGoals: List<SubGoal>,
        val toolSequence: List<ToolAction>,
        val validationChecks: List<ValidationCheck>,
        val estimatedDuration: Long,
        val confidence: Float,
        val dependencies: List<String> = emptyList(),
        val fallbackStrategies: List<String> = emptyList()
    )

    data class SubGoal(
        val id: String,
        val description: String,
        val priority: TaskStateManager.TaskPriority,
        val requiredTools: List<String>,
        val expectedOutputs: List<String>,
        val dependencies: List<String> = emptyList()
    )

    data class ToolAction(
        val toolName: String,
        val parameters: Map<String, Any>,
        val expectedDuration: Long,
        val successCriteria: List<String>,
        val errorHandling: String
    )

    data class ValidationCheck(
        val checkType: String,
        val criteria: String,
        val blocker: Boolean
    )

    /**
     * Executive Planning Loop - Enhanced think method for structured planning
     */
    suspend fun planAndExecute(
        userPrompt: String, 
        currentContext: Map<String, Any> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        try {
            currentState = ThinkingState.PARSING_GOAL
            Timber.d("🧠 Executive Planner starting with prompt: $userPrompt")

            // Step 1: Parse and understand the goal
            val parsedGoal = parseUserGoal(userPrompt, currentContext)
            
            currentState = ThinkingState.DECOMPOSING_TASK
            // Step 2: Create main task and decompose into subtasks
            val mainTask = taskStateManager.createTask(
                title = parsedGoal.title,
                description = parsedGoal.description,
                type = parsedGoal.taskType,
                priority = parsedGoal.priority,
                context = currentContext,
                inputs = parsedGoal.inputs
            )

            // Step 3: Generate action plan
            val actionPlan = generateActionPlan(parsedGoal, currentContext)
            
            currentState = ThinkingState.VALIDATING_PLAN
            // Step 4: Validate plan against policies and constraints
            val validatedPlan = validatePlan(actionPlan, currentContext)
            
            currentState = ThinkingState.EXECUTING
            // Step 5: Execute the plan
            val executionResult = executePlan(mainTask.id, validatedPlan)
            
            currentState = ThinkingState.REFLECTING
            // Step 6: Reflect on results and learn
            reflectOnExecution(mainTask.id, executionResult, userPrompt)
            
            currentState = ThinkingState.IDLE
            
            return@withContext formatExecutionResult(executionResult)
            
        } catch (e: Exception) {
            Timber.e(e, "Executive planning failed for prompt: $userPrompt")
            currentState = ThinkingState.IDLE
            return@withContext "I encountered an issue while planning: ${e.message}. Let me learn from this and try a different approach."
        }
    }

    /**
     * Parse user goal into structured components
     */
    private suspend fun parseUserGoal(
        userPrompt: String,
        context: Map<String, Any>
    ): ParsedGoal = withContext(Dispatchers.IO) {
        
        // Query guidance system for similar goals
        val relatedKnowledge = aiGuidanceSystem.queryKnowledge(
            userPrompt, 
            AIGuidanceSystem.KnowledgeCategory.WORKFLOW_PATTERNS, 
            3
        )
        
        // Determine task type based on prompt analysis
        val taskType = inferTaskType(userPrompt)
        val priority = inferPriority(userPrompt, context)
        
        // Extract inputs and expected outputs
        val inputs = extractInputsFromPrompt(userPrompt, context)
        
        Timber.d("🧠 Parsed goal: $taskType with priority $priority")
        
        return@withContext ParsedGoal(
            title = generateTaskTitle(userPrompt),
            description = userPrompt,
            taskType = taskType,
            priority = priority,
            inputs = inputs,
            relatedKnowledge = relatedKnowledge.map { it.content }
        )
    }

    /**
     * Generate comprehensive action plan
     */
    private suspend fun generateActionPlan(
        parsedGoal: ParsedGoal,
        context: Map<String, Any>
    ): ActionPlan = withContext(Dispatchers.IO) {
        
        // Get contextual recommendations from guidance system
        val recommendations = aiGuidanceSystem.getContextualRecommendations(
            context + mapOf("goal_type" to parsedGoal.taskType.name),
            parsedGoal.taskType.name
        )
        
        // Generate sub-goals based on task complexity
        val subGoals = decomposeIntoSubGoals(parsedGoal, recommendations)
        
        // Map sub-goals to tool actions
        val toolSequence = mapSubGoalsToTools(subGoals, context)
        
        // Generate validation checks
        val validationChecks = generateValidationChecks(parsedGoal, toolSequence)
        
        // Estimate duration based on similar past tasks
        val estimatedDuration = estimateExecutionDuration(toolSequence)
        
        Timber.d("🧠 Generated action plan with ${subGoals.size} sub-goals and ${toolSequence.size} tool actions")
        
        return@withContext ActionPlan(
            id = java.util.UUID.randomUUID().toString(),
            goalDescription = parsedGoal.description,
            subGoals = subGoals,
            toolSequence = toolSequence,
            validationChecks = validationChecks,
            estimatedDuration = estimatedDuration,
            confidence = calculatePlanConfidence(subGoals, toolSequence),
            fallbackStrategies = generateFallbackStrategies(parsedGoal)
        )
    }

    /**
     * Validate plan against policies and constraints
     */
    private suspend fun validatePlan(
        plan: ActionPlan,
        context: Map<String, Any>
    ): ActionPlan = withContext(Dispatchers.IO) {
        
        // Get applicable policies
        val policies = aiGuidanceSystem.getApplicablePolicies(
            context + mapOf(
                "plan_type" to "tool_execution",
                "tool_count" to plan.toolSequence.size,
                "estimated_duration" to plan.estimatedDuration
            )
        )
        
        // Check each validation criteria
        val validationResults = plan.validationChecks.map { check ->
            validateCheck(check, plan, context, policies)
        }
        
        // If critical validations fail, modify plan
        val criticalFailures = validationResults.filter { !it.passed && it.check.blocker }
        
        val validatedPlan = if (criticalFailures.isNotEmpty()) {
            Timber.w("🧠 Plan validation failed: ${criticalFailures.size} critical issues")
            modifyPlanForValidation(plan, criticalFailures, policies)
        } else {
            Timber.d("🧠 Plan validation successful")
            plan
        }
        
        return@withContext validatedPlan
    }

    /**
     * Execute the validated plan
     */
    private suspend fun executePlan(
        taskId: String,
        plan: ActionPlan
    ): ExecutionResult = withContext(Dispatchers.IO) {
        
        val executionStartTime = System.currentTimeMillis()
        val results = mutableListOf<ToolExecutionResult>()
        var currentSuccess = true
        
        // Update task status to in progress
        taskStateManager.updateTaskStatus(taskId, TaskStateManager.TaskStatus.IN_PROGRESS)
        
        // Execute tools in sequence
        for (toolAction in plan.toolSequence) {
            if (!currentSuccess) {
                Timber.w("🧠 Stopping execution due to previous failure")
                break
            }
            
            try {
                val toolResult = executeToolAction(taskId, toolAction)
                results.add(toolResult)
                
                // Record tool execution in task state
                taskStateManager.addToolExecution(
                    taskId = taskId,
                    toolName = toolAction.toolName,
                    inputs = toolAction.parameters,
                    outputs = toolResult.outputs,
                    success = toolResult.success,
                    executionTime = toolResult.executionTime
                )
                
                currentSuccess = toolResult.success
                
            } catch (e: Exception) {
                Timber.e(e, "🧠 Tool execution failed: ${toolAction.toolName}")
                results.add(ToolExecutionResult(
                    toolName = toolAction.toolName,
                    success = false,
                    outputs = mapOf("error" to e.message),
                    executionTime = 0L
                ))
                currentSuccess = false
            }
        }
        
        val executionTime = System.currentTimeMillis() - executionStartTime
        val overallSuccess = results.all { it.success }
        
        // Update task status based on execution results
        val finalStatus = if (overallSuccess) {
            TaskStateManager.TaskStatus.COMPLETED
        } else {
            TaskStateManager.TaskStatus.FAILED
        }
        
        taskStateManager.updateTaskStatus(
            taskId = taskId,
            status = finalStatus,
            outputs = results.flatMap { it.outputs.entries }.associate { it.key to it.value }
        )
        
        Timber.d("🧠 Plan execution completed. Success: $overallSuccess, Time: ${executionTime}ms")
        
        return@withContext ExecutionResult(
            planId = plan.id,
            taskId = taskId,
            success = overallSuccess,
            toolResults = results,
            totalExecutionTime = executionTime,
            finalOutputs = results.flatMap { it.outputs.entries }.associate { it.key to it.value }
        )
    }

    /**
     * Reflect on execution results and update learning
     */
    private suspend fun reflectOnExecution(
        taskId: String,
        result: ExecutionResult,
        originalPrompt: String
    ) = withContext(Dispatchers.IO) {
        
        currentState = ThinkingState.LEARNING
        
        // Analyze what worked and what didn't
        val successfulTools = result.toolResults.filter { it.success }.map { it.toolName }
        val failedTools = result.toolResults.filter { !it.success }.map { it.toolName }
        
        // Record successful patterns
        if (result.success) {
            successfulTools.forEach { toolName ->
                aiGuidanceSystem.recordToolUsage(
                    toolName = toolName,
                    context = "user_request",
                    executionTime = result.toolResults.find { it.toolName == toolName }?.executionTime ?: 0L,
                    success = true,
                    parameters = emptyMap()
                )
            }
            
            // Add successful workflow pattern
            if (successfulTools.isNotEmpty()) {
                val workflowSteps = successfulTools.mapIndexed { index, toolName ->
                    AIGuidanceSystem.WorkflowStep(
                        stepNumber = index + 1,
                        toolName = toolName,
                        parameters = emptyMap(),
                        expectedOutputs = listOf("success")
                    )
                }
                
                aiGuidanceSystem.recordWorkflowPattern(
                    name = "successful_user_request_${System.currentTimeMillis()}",
                    description = "Successful execution pattern for: ${originalPrompt.take(50)}",
                    steps = workflowSteps,
                    executionTime = result.totalExecutionTime
                )
            }
        }
        
        // Generate reflection insight
        val insight = generateReflectionInsight(result, originalPrompt, successfulTools, failedTools)
        aiGuidanceSystem.addReflectionInsight(
            type = AIGuidanceSystem.ReflectionType.PERFORMANCE_ANALYSIS,
            title = insight.title,
            insight = insight.content,
            confidence = insight.confidence,
            supportingEvidence = insight.evidence,
            recommendations = insight.recommendations
        )
        
        // Update legacy knowledge base for backward compatibility
        learn(originalPrompt, "execution_result", if (result.success) "success" else "failure")
        
        Timber.d("🧠 Reflection completed. Generated insights for future improvements.")
    }

    /**
     * Learn from user interactions and update knowledge base
     */
    fun learn(userInput: String, context: String, outcome: String = "") {
        currentState = ThinkingState.LEARNING
        
        Timber.d("🧠 AIThinkModule learning from input: $userInput, context: $context")
        
        // Store knowledge association
        knowledgeBase[userInput] = context
        
        // Track interaction history
        interactionHistory.add("$userInput -> $context -> $outcome")
        
        // Update context patterns
        contextPatterns[context] = contextPatterns.getOrDefault(context, 0) + 1
        
        // Keep history manageable
        if (interactionHistory.size > 100) {
            interactionHistory.removeAt(0)
        }
        
        currentState = ThinkingState.IDLE
        Timber.d("🧠 Learning complete - knowledge base size: ${knowledgeBase.size}")
    }

    /**
     * Think about a query and provide thoughtful suggestion
     */
    fun think(query: String, currentContext: String = ""): String {
        currentState = ThinkingState.THINKING
        
        Timber.d("🧠 AIThinkModule thinking about query: $query")
        
        // Analyze query against knowledge base
        val directMatch = knowledgeBase[query]
        val contextualSuggestions = findContextualSuggestions(query, currentContext)
        val patternAnalysis = analyzePatterns(query)
        
        currentState = ThinkingState.SUGGESTING
        
        val suggestion = when {
            directMatch != null -> {
                "Based on previous experience: $directMatch. ${generateEnhancement(query)}"
            }
            contextualSuggestions.isNotEmpty() -> {
                "Contextual insight: ${contextualSuggestions.first()}. Consider these patterns: ${contextualSuggestions.take(2).joinToString(", ")}"
            }
            patternAnalysis.isNotEmpty() -> {
                "Pattern analysis suggests: $patternAnalysis. This aligns with your development style."
            }
            else -> {
                "I need more context to provide a thoughtful suggestion. Let me learn from your interaction..."
            }
        }
        
        currentState = ThinkingState.IDLE
        Timber.d("🧠 Thought process complete: ${suggestion.take(100)}...")
        
        return suggestion
    }

    /**
     * Enhance bot responses with learned intelligence
     */
    fun enhanceBotResponse(
        originalResponse: String,
        botType: String,
        context: String
    ): String {
        val contextualKnowledge = findContextualSuggestions(context, "")
        val enhancement = when {
            contextualKnowledge.isNotEmpty() -> {
                " (AI Enhancement: Based on your patterns, also consider ${contextualKnowledge.first()})"
            }
            contextPatterns[context] ?: 0 > 3 -> {
                " (AI Enhancement: You work with this frequently - I've optimized my suggestions)"
            }
            else -> ""
        }
        
        return originalResponse + enhancement
    }

    /**
     * Analyze user coding patterns for personalized suggestions
     */
    fun analyzeCodingPatterns(): Map<String, Any> {
        val patterns = mutableMapOf<String, Any>()
        
        // Most common contexts
        val topContexts = contextPatterns.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { "${it.key} (${it.value} times)" }
        
        patterns["topContexts"] = topContexts
        patterns["totalInteractions"] = interactionHistory.size
        patterns["knowledgeBaseSize"] = knowledgeBase.size
        patterns["learningEfficiency"] = calculateLearningEfficiency()
        
        return patterns
    }

    private fun findContextualSuggestions(query: String, context: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Find similar contexts
        knowledgeBase.entries.forEach { (key, value) ->
            if (key.contains(query, ignoreCase = true) || value.contains(context, ignoreCase = true)) {
                suggestions.add(value)
            }
        }
        
        return suggestions.distinct()
    }

    private fun analyzePatterns(query: String): String {
        // Analyze interaction history for patterns
        val relatedInteractions = interactionHistory.filter { 
            it.contains(query, ignoreCase = true) 
        }
        
        return when {
            relatedInteractions.isNotEmpty() -> {
                "You've worked with similar queries ${relatedInteractions.size} times"
            }
            query.contains("error", ignoreCase = true) -> {
                "Error handling pattern detected - systematic debugging approach recommended"
            }
            query.contains("optimize", ignoreCase = true) -> {
                "Optimization pattern - measure first, optimize second approach"
            }
            else -> ""
        }
    }

    private fun generateEnhancement(query: String): String {
        return when {
            query.contains("function", ignoreCase = true) -> "Consider function composition and purity."
            query.contains("class", ignoreCase = true) -> "Think about single responsibility and dependency injection."
            query.contains("api", ignoreCase = true) -> "REST principles and error handling are key."
            else -> "Apply SOLID principles and test-driven development."
        }
    }

    private fun calculateLearningEfficiency(): Float {
        val uniqueContexts = contextPatterns.size
        val totalInteractions = interactionHistory.size
        
        return if (totalInteractions > 0) {
            uniqueContexts.toFloat() / totalInteractions.toFloat()
        } else {
            0f
        }
    }

    // Legacy methods for backward compatibility
    
    /**
     * Learn from user interactions and update knowledge base (legacy method)
     */
    fun learn(userInput: String, context: String, outcome: String = "") {
        currentState = ThinkingState.LEARNING
        
        Timber.d("🧠 AIThinkModule learning from input: $userInput, context: $context")
        
        // Store knowledge association
        knowledgeBase[userInput] = context
        
        // Track interaction history
        interactionHistory.add("$userInput -> $context -> $outcome")
        
        // Update context patterns
        contextPatterns[context] = contextPatterns.getOrDefault(context, 0) + 1
        
        // Keep history manageable
        if (interactionHistory.size > 100) {
            interactionHistory.removeAt(0)
        }
        
        currentState = ThinkingState.IDLE
        Timber.d("🧠 Learning complete - knowledge base size: ${knowledgeBase.size}")
    }

    /**
     * Think about a query and provide thoughtful suggestion (legacy method)
     */
    suspend fun think(query: String, currentContext: String = ""): String = withContext(Dispatchers.IO) {
        // For simple queries, use legacy thinking. For complex tasks, use planAndExecute
        if (isComplexTask(query)) {
            return@withContext planAndExecute(query, mapOf("context" to currentContext))
        }
        
        currentState = ThinkingState.THINKING
        
        Timber.d("🧠 AIThinkModule thinking about query: $query")
        
        // Analyze query against knowledge base
        val directMatch = knowledgeBase[query]
        val contextualSuggestions = findContextualSuggestions(query, currentContext)
        val patternAnalysis = analyzePatterns(query)
        
        currentState = ThinkingState.SUGGESTING
        
        val suggestion = when {
            directMatch != null -> {
                "Based on previous experience: $directMatch. ${generateEnhancement(query)}"
            }
            contextualSuggestions.isNotEmpty() -> {
                "Contextual insight: ${contextualSuggestions.first()}. Consider these patterns: ${contextualSuggestions.take(2).joinToString(", ")}"
            }
            patternAnalysis.isNotEmpty() -> {
                "Pattern analysis suggests: $patternAnalysis. This aligns with your development style."
            }
            else -> {
                "I need more context to provide a thoughtful suggestion. Let me learn from your interaction..."
            }
        }
        
        currentState = ThinkingState.IDLE
        Timber.d("🧠 Thought process complete: ${suggestion.take(100)}...")
        
        return@withContext suggestion
    }

    /**
     * Enhance bot responses with learned intelligence (legacy method)
     */
    suspend fun enhanceBotResponse(
        originalResponse: String,
        botType: String,
        context: String
    ): String = withContext(Dispatchers.IO) {
        // Get contextual recommendations from guidance system
        val recommendations = aiGuidanceSystem.getContextualRecommendations(
            mapOf("bot_type" to botType, "context" to context)
        )
        
        val contextualKnowledge = findContextualSuggestions(context, "")
        val enhancement = when {
            recommendations.isNotEmpty() -> {
                " (AI Enhancement: ${recommendations.first()})"
            }
            contextualKnowledge.isNotEmpty() -> {
                " (AI Enhancement: Based on your patterns, also consider ${contextualKnowledge.first()})"
            }
            contextPatterns[context] ?: 0 > 3 -> {
                " (AI Enhancement: You work with this frequently - I've optimized my suggestions)"
            }
            else -> ""
        }
        
        return@withContext originalResponse + enhancement
    }

    /**
     * Analyze user coding patterns for personalized suggestions (legacy method)
     */
    suspend fun analyzeCodingPatterns(): Map<String, Any> = withContext(Dispatchers.IO) {
        val patterns = mutableMapOf<String, Any>()
        
        // Get enhanced statistics from guidance system
        val guidanceStats = aiGuidanceSystem.getGuidanceStatistics()
        val taskStats = taskStateManager.getTaskStatistics()
        
        // Most common contexts
        val topContexts = contextPatterns.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { "${it.key} (${it.value} times)" }
        
        patterns["topContexts"] = topContexts
        patterns["totalInteractions"] = interactionHistory.size
        patterns["knowledgeBaseSize"] = knowledgeBase.size
        patterns["learningEfficiency"] = calculateLearningEfficiency()
        
        // Enhanced patterns from guidance system
        patterns["guidanceSystemStats"] = guidanceStats
        patterns["taskExecutionStats"] = taskStats
        
        return@withContext patterns
    }

    /**
     * Get thinking module status (legacy method)
     */
    suspend fun getThinkingStatus(): Map<String, Any> = withContext(Dispatchers.IO) {
        val currentTaskContext = taskStateManager.getCurrentTaskContext()
        
        return@withContext mapOf(
            "currentState" to currentState.name,
            "knowledgeBaseSize" to knowledgeBase.size,
            "interactionHistory" to interactionHistory.size,
            "topPatterns" to contextPatterns.entries
                .sortedByDescending { it.value }
                .take(3)
                .associate { it.key to it.value },
            "currentTaskContext" to currentTaskContext,
            "guidanceSystemActive" to true,
            "taskStateManagerActive" to true
        )
    }

    // Enhanced helper methods
    
    private data class ParsedGoal(
        val title: String,
        val description: String,
        val taskType: TaskStateManager.TaskType,
        val priority: TaskStateManager.TaskPriority,
        val inputs: Map<String, Any>,
        val relatedKnowledge: List<String>
    )

    private data class ExecutionResult(
        val planId: String,
        val taskId: String,
        val success: Boolean,
        val toolResults: List<ToolExecutionResult>,
        val totalExecutionTime: Long,
        val finalOutputs: Map<String, Any>
    )

    private data class ToolExecutionResult(
        val toolName: String,
        val success: Boolean,
        val outputs: Map<String, Any>,
        val executionTime: Long
    )

    private data class ValidationResult(
        val check: ValidationCheck,
        val passed: Boolean,
        val reason: String
    )

    private data class ReflectionInsight(
        val title: String,
        val content: String,
        val confidence: Float,
        val evidence: List<String>,
        val recommendations: List<String>
    )

    private fun isComplexTask(query: String): Boolean {
        val complexIndicators = listOf(
            "create", "build", "implement", "develop", "analyze", "optimize", 
            "process", "convert", "transform", "integrate", "deploy", "test"
        )
        return complexIndicators.any { query.lowercase().contains(it) } || query.length > 50
    }

    private fun inferTaskType(prompt: String): TaskStateManager.TaskType {
        return when {
            prompt.contains("file", ignoreCase = true) || prompt.contains("upload", ignoreCase = true) -> 
                TaskStateManager.TaskType.DATA_PROCESSING
            prompt.contains("analyze", ignoreCase = true) || prompt.contains("review", ignoreCase = true) -> 
                TaskStateManager.TaskType.VALIDATION
            prompt.contains("learn", ignoreCase = true) || prompt.contains("train", ignoreCase = true) -> 
                TaskStateManager.TaskType.LEARNING
            prompt.contains("system", ignoreCase = true) || prompt.contains("manage", ignoreCase = true) -> 
                TaskStateManager.TaskType.SYSTEM_OPERATION
            else -> TaskStateManager.TaskType.USER_REQUEST
        }
    }

    private fun inferPriority(prompt: String, context: Map<String, Any>): TaskStateManager.TaskPriority {
        return when {
            prompt.contains("urgent", ignoreCase = true) || prompt.contains("critical", ignoreCase = true) -> 
                TaskStateManager.TaskPriority.CRITICAL
            prompt.contains("important", ignoreCase = true) || prompt.contains("priority", ignoreCase = true) -> 
                TaskStateManager.TaskPriority.HIGH
            prompt.contains("later", ignoreCase = true) || prompt.contains("when possible", ignoreCase = true) -> 
                TaskStateManager.TaskPriority.LOW
            else -> TaskStateManager.TaskPriority.NORMAL
        }
    }

    private fun extractInputsFromPrompt(prompt: String, context: Map<String, Any>): Map<String, Any> {
        val inputs = mutableMapOf<String, Any>()
        inputs["original_prompt"] = prompt
        inputs["context"] = context
        inputs["timestamp"] = System.currentTimeMillis()
        
        // Extract file references
        val filePattern = Regex("""[^\s]+\.(kt|java|py|js|ts|md|txt|json|xml)""", RegexOption.IGNORE_CASE)
        val fileMatches = filePattern.findAll(prompt).map { it.value }.toList()
        if (fileMatches.isNotEmpty()) {
            inputs["referenced_files"] = fileMatches
        }
        
        return inputs
    }

    private fun generateTaskTitle(prompt: String): String {
        // Generate a concise title from the prompt
        val words = prompt.split(" ").take(6)
        return words.joinToString(" ").take(50) + if (prompt.length > 50) "..." else ""
    }

    private suspend fun decomposeIntoSubGoals(
        parsedGoal: ParsedGoal,
        recommendations: List<String>
    ): List<SubGoal> = withContext(Dispatchers.IO) {
        val subGoals = mutableListOf<SubGoal>()
        
        // Based on task type, create appropriate sub-goals
        when (parsedGoal.taskType) {
            TaskStateManager.TaskType.DATA_PROCESSING -> {
                subGoals.add(SubGoal(
                    id = java.util.UUID.randomUUID().toString(),
                    description = "Validate input data",
                    priority = TaskStateManager.TaskPriority.HIGH,
                    requiredTools = listOf("ValidationService"),
                    expectedOutputs = listOf("validation_result")
                ))
                subGoals.add(SubGoal(
                    id = java.util.UUID.randomUUID().toString(),
                    description = "Process data",
                    priority = TaskStateManager.TaskPriority.NORMAL,
                    requiredTools = listOf("DataProcessor"),
                    expectedOutputs = listOf("processed_data"),
                    dependencies = listOf(subGoals[0].id)
                ))
            }
            TaskStateManager.TaskType.SYSTEM_OPERATION -> {
                subGoals.add(SubGoal(
                    id = java.util.UUID.randomUUID().toString(),
                    description = "Check system status",
                    priority = TaskStateManager.TaskPriority.HIGH,
                    requiredTools = listOf("SystemMonitor"),
                    expectedOutputs = listOf("system_status")
                ))
                subGoals.add(SubGoal(
                    id = java.util.UUID.randomUUID().toString(),
                    description = "Execute system operation",
                    priority = TaskStateManager.TaskPriority.NORMAL,
                    requiredTools = listOf("SystemController"),
                    expectedOutputs = listOf("operation_result"),
                    dependencies = listOf(subGoals[0].id)
                ))
            }
            else -> {
                // Default decomposition
                subGoals.add(SubGoal(
                    id = java.util.UUID.randomUUID().toString(),
                    description = "Execute user request",
                    priority = TaskStateManager.TaskPriority.NORMAL,
                    requiredTools = listOf("GeneralProcessor"),
                    expectedOutputs = listOf("result")
                ))
            }
        }
        
        return@withContext subGoals
    }

    private suspend fun mapSubGoalsToTools(
        subGoals: List<SubGoal>,
        context: Map<String, Any>
    ): List<ToolAction> = withContext(Dispatchers.IO) {
        return@withContext subGoals.flatMap { subGoal ->
            subGoal.requiredTools.map { toolName ->
                ToolAction(
                    toolName = toolName,
                    parameters = mapOf(
                        "sub_goal_id" to subGoal.id,
                        "description" to subGoal.description,
                        "context" to context
                    ),
                    expectedDuration = estimateToolDuration(toolName),
                    successCriteria = subGoal.expectedOutputs,
                    errorHandling = "retry_once"
                )
            }
        }
    }

    private fun generateValidationChecks(
        parsedGoal: ParsedGoal,
        toolSequence: List<ToolAction>
    ): List<ValidationCheck> {
        val checks = mutableListOf<ValidationCheck>()
        
        // Resource validation
        checks.add(ValidationCheck(
            checkType = "resource_availability",
            criteria = "system_resources_sufficient",
            blocker = true
        ))
        
        // Permission validation
        checks.add(ValidationCheck(
            checkType = "permissions",
            criteria = "required_permissions_available",
            blocker = true
        ))
        
        // Tool availability validation
        toolSequence.map { it.toolName }.distinct().forEach { toolName ->
            checks.add(ValidationCheck(
                checkType = "tool_availability",
                criteria = "tool_${toolName}_available",
                blocker = true
            ))
        }
        
        return checks
    }

    private fun estimateExecutionDuration(toolSequence: List<ToolAction>): Long {
        return toolSequence.sumOf { it.expectedDuration }
    }

    private fun estimateToolDuration(toolName: String): Long {
        // Simple estimation based on tool type
        return when (toolName) {
            "ValidationService" -> 1000L
            "DataProcessor" -> 5000L
            "SystemMonitor" -> 2000L
            "SystemController" -> 3000L
            "GeneralProcessor" -> 2000L
            else -> 2000L
        }
    }

    private fun calculatePlanConfidence(subGoals: List<SubGoal>, toolSequence: List<ToolAction>): Float {
        // Base confidence
        var confidence = 0.7f
        
        // Adjust based on complexity
        if (subGoals.size <= 3) confidence += 0.1f
        if (toolSequence.size <= 5) confidence += 0.1f
        
        // Adjust based on tool familiarity (simplified)
        val knownTools = toolSequence.count { 
            listOf("ValidationService", "DataProcessor", "SystemMonitor").contains(it.toolName) 
        }
        confidence += (knownTools.toFloat() / toolSequence.size) * 0.1f
        
        return minOf(1.0f, confidence)
    }

    private fun generateFallbackStrategies(parsedGoal: ParsedGoal): List<String> {
        return listOf(
            "retry_with_simplified_approach",
            "request_user_clarification",
            "use_alternative_tools",
            "defer_to_manual_execution"
        )
    }

    private suspend fun validateCheck(
        check: ValidationCheck,
        plan: ActionPlan,
        context: Map<String, Any>,
        policies: List<AIGuidanceSystem.PolicyRule>
    ): ValidationResult = withContext(Dispatchers.IO) {
        
        val passed = when (check.checkType) {
            "resource_availability" -> {
                // Simple resource check - in real implementation, check actual resources
                (context["memory_available"] as? Long ?: Long.MAX_VALUE) > 100 * 1024 * 1024
            }
            "permissions" -> {
                // Check if any security policies are violated
                !policies.any { it.category == AIGuidanceSystem.PolicyCategory.SECURITY && it.priority == AIGuidanceSystem.PolicyPriority.CRITICAL }
            }
            "tool_availability" -> {
                // Assume tools are available - in real implementation, check actual availability
                true
            }
            else -> true
        }
        
        return@withContext ValidationResult(
            check = check,
            passed = passed,
            reason = if (passed) "validation_passed" else "validation_failed_${check.checkType}"
        )
    }

    private suspend fun modifyPlanForValidation(
        plan: ActionPlan,
        failures: List<ValidationResult>,
        policies: List<AIGuidanceSystem.PolicyRule>
    ): ActionPlan = withContext(Dispatchers.IO) {
        
        // Simple plan modification - in real implementation, use more sophisticated modification
        val modifiedToolSequence = plan.toolSequence.filter { toolAction ->
            !failures.any { failure -> 
                failure.check.criteria.contains(toolAction.toolName) 
            }
        }
        
        return@withContext plan.copy(
            toolSequence = modifiedToolSequence,
            confidence = plan.confidence * 0.8f, // Reduce confidence due to modifications
            fallbackStrategies = plan.fallbackStrategies + "plan_modified_for_validation"
        )
    }

    private suspend fun executeToolAction(
        taskId: String,
        toolAction: ToolAction
    ): ToolExecutionResult = withContext(Dispatchers.IO) {
        
        val startTime = System.currentTimeMillis()
        
        try {
            // Simulate tool execution - in real implementation, call actual tools
            val outputs = when (toolAction.toolName) {
                "ValidationService" -> mapOf("validation_result" to "passed", "details" to "All validations successful")
                "DataProcessor" -> mapOf("processed_data" to "data_processed_successfully", "record_count" to 100)
                "SystemMonitor" -> mapOf("system_status" to "healthy", "cpu_usage" to 25, "memory_usage" to 60)
                "SystemController" -> mapOf("operation_result" to "completed", "affected_components" to listOf("component1", "component2"))
                "GeneralProcessor" -> mapOf("result" to "task_completed", "status" to "success")
                else -> mapOf("result" to "tool_executed", "status" to "unknown_tool")
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            
            Timber.d("🧠 Tool ${toolAction.toolName} executed successfully in ${executionTime}ms")
            
            return@withContext ToolExecutionResult(
                toolName = toolAction.toolName,
                success = true,
                outputs = outputs,
                executionTime = executionTime
            )
            
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            
            Timber.e(e, "🧠 Tool ${toolAction.toolName} execution failed")
            
            return@withContext ToolExecutionResult(
                toolName = toolAction.toolName,
                success = false,
                outputs = mapOf("error" to e.message, "error_type" to e.javaClass.simpleName),
                executionTime = executionTime
            )
        }
    }

    private fun formatExecutionResult(result: ExecutionResult): String {
        return if (result.success) {
            buildString {
                append("✅ Task completed successfully!\n\n")
                append("Execution Summary:\n")
                append("- Duration: ${result.totalExecutionTime}ms\n")
                append("- Tools used: ${result.toolResults.map { it.toolName }.joinToString(", ")}\n")
                append("- Results: ${result.finalOutputs.entries.take(3).joinToString(", ") { "${it.key}: ${it.value}" }}")
                if (result.finalOutputs.size > 3) {
                    append("... and ${result.finalOutputs.size - 3} more results")
                }
            }
        } else {
            buildString {
                append("❌ Task execution encountered issues.\n\n")
                append("Execution Summary:\n")
                append("- Duration: ${result.totalExecutionTime}ms\n")
                append("- Successful tools: ${result.toolResults.filter { it.success }.map { it.toolName }.joinToString(", ")}\n")
                append("- Failed tools: ${result.toolResults.filter { !it.success }.map { it.toolName }.joinToString(", ")}\n")
                append("- I've learned from this execution and will improve next time.")
            }
        }
    }

    private fun generateReflectionInsight(
        result: ExecutionResult,
        originalPrompt: String,
        successfulTools: List<String>,
        failedTools: List<String>
    ): ReflectionInsight {
        
        val title = if (result.success) {
            "Successful execution pattern identified"
        } else {
            "Execution challenges identified"
        }
        
        val content = buildString {
            append("Analysis of execution for prompt: '${originalPrompt.take(50)}...'\n")
            append("Success rate: ${(successfulTools.size.toFloat() / (successfulTools.size + failedTools.size)) * 100}%\n")
            if (successfulTools.isNotEmpty()) {
                append("Effective tools: ${successfulTools.joinToString(", ")}\n")
            }
            if (failedTools.isNotEmpty()) {
                append("Challenging tools: ${failedTools.joinToString(", ")}\n")
            }
            append("Execution time: ${result.totalExecutionTime}ms")
        }
        
        val evidence = buildString {
            append("Tool execution results: ")
            result.toolResults.forEach { toolResult ->
                append("${toolResult.toolName}=${if (toolResult.success) "✓" else "✗"} ")
            }
        }.let { listOf(it) }
        
        val recommendations = buildList {
            if (result.success && successfulTools.isNotEmpty()) {
                add("Continue using the successful tool pattern: ${successfulTools.joinToString(" → ")}")
            }
            if (failedTools.isNotEmpty()) {
                add("Consider alternative approaches for: ${failedTools.joinToString(", ")}")
            }
            if (result.totalExecutionTime > 10000) {
                add("Look for optimization opportunities to reduce execution time")
            }
            add("Monitor similar requests for pattern confirmation")
        }
        
        return ReflectionInsight(
            title = title,
            content = content,
            confidence = if (result.success) 0.8f else 0.6f,
            evidence = evidence,
            recommendations = recommendations
        )
    }
}