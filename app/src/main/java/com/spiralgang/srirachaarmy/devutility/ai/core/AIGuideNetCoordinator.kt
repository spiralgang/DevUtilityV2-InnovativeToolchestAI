package com.spiralgang.srirachaarmy.devutility.ai.core

import com.spiralgang.srirachaarmy.devutility.ai.AIEnvironmentAwareness
import com.spiralgang.srirachaarmy.devutility.ai.AIThinkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AIGuideNetCoordinator - Central coordinator for the AIGuideNet framework
 * 
 * This coordinator integrates all components of the Autonomous Internal Guidance 
 * & Routing Network (AIGuideNet) to provide a unified interface for intelligent
 * AI task planning, execution, and learning.
 * 
 * Key responsibilities:
 * - Orchestrate interactions between TaskStateManager, AIGuidanceSystem, and AIEnvironmentAwareness
 * - Provide unified API for task execution with intelligent guidance
 * - Coordinate learning and adaptation across all components
 * - Ensure proper initialization and state management
 * 
 * Part of the Autonomous Internal Guidance & Routing Network (AIGuideNet)
 */
@Singleton
class AIGuideNetCoordinator @Inject constructor(
    private val taskStateManager: TaskStateManager,
    private val aiGuidanceSystem: AIGuidanceSystem,
    private val aiEnvironmentAwareness: AIEnvironmentAwareness,
    private val aiThinkModule: AIThinkModule
) {
    
    private val _systemStatus = MutableStateFlow<SystemStatus>(SystemStatus.INITIALIZING)
    val systemStatus: StateFlow<SystemStatus> = _systemStatus.asStateFlow()
    
    private val _currentCapabilities = MutableStateFlow<SystemCapabilities?>(null)
    val currentCapabilities: StateFlow<SystemCapabilities?> = _currentCapabilities.asStateFlow()
    
    private var isInitialized = false
    
    data class SystemStatus(
        val status: Status,
        val message: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val componentStates: Map<String, String> = emptyMap()
    ) {
        enum class Status {
            INITIALIZING,
            READY,
            PROCESSING,
            ERROR,
            MAINTENANCE
        }
    }
    
    data class SystemCapabilities(
        val availableTools: List<String>,
        val supportedTaskTypes: List<TaskStateManager.TaskType>,
        val knowledgeBaseSize: Int,
        val policyCount: Int,
        val memoryUtilization: Float,
        val systemHealth: Float,
        val recommendedMaxConcurrentTasks: Int
    )
    
    data class ExecutionRequest(
        val userPrompt: String,
        val context: Map<String, Any> = emptyMap(),
        val priority: TaskStateManager.TaskPriority = TaskStateManager.TaskPriority.NORMAL,
        val userId: String? = null,
        val sessionId: String? = null,
        val preferences: Map<String, Any> = emptyMap()
    )
    
    data class ExecutionResponse(
        val success: Boolean,
        val result: String,
        val taskId: String,
        val executionTime: Long,
        val toolsUsed: List<String>,
        val learningInsights: List<String>,
        val recommendations: List<String>,
        val metadata: Map<String, Any>
    )
    
    /**
     * Initialize the complete AIGuideNet system
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Starting AIGuideNet initialization")
            
            Timber.d("🚀 Initializing AIGuideNet Coordinator")
            
            // Initialize components in proper order
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Initializing AI Guidance System")
            aiGuidanceSystem.initialize()
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Initializing AI Environment Awareness")
            aiEnvironmentAwareness.initialize()
            
            // Task state manager doesn't need explicit initialization
            
            // Verify system capabilities
            val capabilities = assessSystemCapabilities()
            _currentCapabilities.value = capabilities
            
            isInitialized = true
            _systemStatus.value = SystemStatus(
                SystemStatus.Status.READY, 
                "AIGuideNet ready with ${capabilities.availableTools.size} tools and ${capabilities.knowledgeBaseSize} knowledge entries",
                componentStates = mapOf(
                    "taskStateManager" to "ready",
                    "aiGuidanceSystem" to "ready",
                    "aiEnvironmentAwareness" to "ready",
                    "aiThinkModule" to "ready"
                )
            )
            
            Timber.d("🚀 AIGuideNet Coordinator initialization complete")
            
        } catch (e: Exception) {
            Timber.e(e, "AIGuideNet Coordinator initialization failed")
            _systemStatus.value = SystemStatus(SystemStatus.Status.ERROR, "Initialization failed: ${e.message}")
        }
    }
    
    /**
     * Execute a request using the complete AIGuideNet framework
     */
    suspend fun executeRequest(request: ExecutionRequest): ExecutionResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            if (!isInitialized) {
                throw IllegalStateException("AIGuideNet not initialized")
            }
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.PROCESSING, "Processing request: ${request.userPrompt.take(50)}...")
            
            Timber.d("🎯 Executing request: ${request.userPrompt}")
            
            // Step 1: Enrich context with environment awareness and user preferences
            val enrichedContext = enrichRequestContext(request)
            
            // Step 2: Use enhanced AIThinkModule for planning and execution
            val executionResult = aiThinkModule.planAndExecute(request.userPrompt, enrichedContext)
            
            // Step 3: Get current task context for metadata
            val taskContext = taskStateManager.getCurrentTaskContext()
            val taskId = taskContext["current_task_id"] as? String ?: "unknown"
            
            // Step 4: Extract tools used and execution time
            val toolsUsed = (taskContext["tools_used"] as? List<String>) ?: emptyList()
            val executionTime = System.currentTimeMillis() - startTime
            
            // Step 5: Generate learning insights and recommendations
            val insights = generateLearningInsights(request, executionResult, taskContext)
            val recommendations = generateRecommendations(request, enrichedContext, executionResult)
            
            // Step 6: Update user preferences if user ID is provided
            if (request.userId != null) {
                updateUserPreferences(request, executionResult, insights)
            }
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.READY, "Request completed successfully")
            
            val response = ExecutionResponse(
                success = !executionResult.contains("❌") && !executionResult.contains("issue"),
                result = executionResult,
                taskId = taskId,
                executionTime = executionTime,
                toolsUsed = toolsUsed,
                learningInsights = insights,
                recommendations = recommendations,
                metadata = mapOf(
                    "context_enriched" to enrichedContext.size,
                    "system_health" to (_currentCapabilities.value?.systemHealth ?: 0.8f),
                    "memory_utilization" to (_currentCapabilities.value?.memoryUtilization ?: 0.5f)
                )
            )
            
            Timber.d("🎯 Request execution completed in ${executionTime}ms")
            return@withContext response
            
        } catch (e: Exception) {
            Timber.e(e, "Request execution failed")
            _systemStatus.value = SystemStatus(SystemStatus.Status.ERROR, "Request execution failed: ${e.message}")
            
            return@withContext ExecutionResponse(
                success = false,
                result = "I encountered an error while processing your request: ${e.message}. I'll learn from this to improve future responses.",
                taskId = "error",
                executionTime = System.currentTimeMillis() - startTime,
                toolsUsed = emptyList(),
                learningInsights = listOf("Error encountered: ${e.javaClass.simpleName}"),
                recommendations = listOf("Consider retrying with more specific instructions"),
                metadata = mapOf("error_type" to e.javaClass.simpleName)
            )
        }
    }
    
    /**
     * Get comprehensive system status and health information
     */
    suspend fun getSystemHealth(): Map<String, Any> = withContext(Dispatchers.IO) {
        val health = mutableMapOf<String, Any>()
        
        health["coordinator_status"] = _systemStatus.value.status.name
        health["coordinator_message"] = _systemStatus.value.message
        health["is_initialized"] = isInitialized
        
        // Component health
        health["task_manager_stats"] = taskStateManager.getTaskStatistics()
        health["guidance_system_stats"] = aiGuidanceSystem.getGuidanceStatistics()
        health["environment_stats"] = aiEnvironmentAwareness.getEnvironmentStatistics()
        health["think_module_status"] = aiThinkModule.getThinkingStatus()
        
        // System capabilities
        health["current_capabilities"] = _currentCapabilities.value
        
        // Resource utilization
        val resourceUtilization = aiEnvironmentAwareness.getResourceUtilization()
        health["resource_utilization"] = resourceUtilization
        
        // Calculate overall health score
        val components = listOf(
            if (isInitialized) 1.0f else 0.0f,
            if (_systemStatus.value.status == SystemStatus.Status.READY) 1.0f else 0.5f,
            1.0f - (resourceUtilization["memory_utilization"] ?: 0.5f) * 0.5f,
            1.0f - (resourceUtilization["cpu_utilization"] ?: 0.5f) * 0.3f
        )
        health["overall_health_score"] = components.average()
        
        return@withContext health
    }
    
    /**
     * Get intelligent recommendations for optimizing system performance
     */
    suspend fun getSystemOptimizationRecommendations(): List<String> = withContext(Dispatchers.IO) {
        val recommendations = mutableListOf<String>()
        val capabilities = _currentCapabilities.value
        val resourceUtilization = aiEnvironmentAwareness.getResourceUtilization()
        
        // Memory optimization recommendations
        val memoryUtilization = resourceUtilization["memory_utilization"] ?: 0.5f
        if (memoryUtilization > 0.8f) {
            recommendations.add("High memory usage detected (${(memoryUtilization * 100).toInt()}%). Consider cleaning up old tasks or enabling ZRAM compression.")
        }
        
        // CPU optimization recommendations
        val cpuUtilization = resourceUtilization["cpu_utilization"] ?: 0.5f
        if (cpuUtilization > 0.7f) {
            recommendations.add("High CPU usage detected (${(cpuUtilization * 100).toInt()}%). Consider reducing concurrent operations or optimizing tool selection.")
        }
        
        // Task management recommendations
        val taskStats = taskStateManager.getTaskStatistics()
        val failureRate = (taskStats["failed_tasks"] as? Int ?: 0).toFloat() / 
                         (taskStats["total_tasks"] as? Int ?: 1).toFloat()
        
        if (failureRate > 0.2f) {
            recommendations.add("Task failure rate is ${(failureRate * 100).toInt()}%. Consider reviewing tool configurations or adding more validation steps.")
        }
        
        // Knowledge base recommendations
        val guidanceStats = aiGuidanceSystem.getGuidanceStatistics()
        val knowledgeSize = guidanceStats["knowledge_entries"] as? Int ?: 0
        
        if (knowledgeSize < 10) {
            recommendations.add("Knowledge base is small ($knowledgeSize entries). System learning will improve with more user interactions.")
        }
        
        // Tool usage recommendations
        val envStats = aiEnvironmentAwareness.getEnvironmentStatistics()
        val toolCount = envStats["registered_tools"] as? Int ?: 0
        
        if (toolCount < 5) {
            recommendations.add("Limited tool registry ($toolCount tools). Consider registering more specialized tools for better task execution.")
        }
        
        // System health recommendations
        val systemHealth = capabilities?.systemHealth ?: 0.8f
        if (systemHealth < 0.7f) {
            recommendations.add("System health is below optimal (${(systemHealth * 100).toInt()}%). Consider system maintenance or component updates.")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("System is operating optimally. All metrics are within healthy ranges.")
        }
        
        return@withContext recommendations
    }
    
    /**
     * Trigger learning update across all components
     */
    suspend fun triggerLearningUpdate() = withContext(Dispatchers.IO) {
        try {
            Timber.d("🧠 Triggering system-wide learning update")
            
            // Update knowledge from external sources
            aiGuidanceSystem.updateKnowledgeFromExternalSources()
            
            // Clean up old completed tasks
            taskStateManager.cleanupOldTasks()
            
            // Refresh system capabilities
            val newCapabilities = assessSystemCapabilities()
            _currentCapabilities.value = newCapabilities
            
            Timber.d("🧠 Learning update completed")
            
        } catch (e: Exception) {
            Timber.e(e, "Learning update failed")
        }
    }
    
    // Private helper methods
    
    private suspend fun enrichRequestContext(request: ExecutionRequest): Map<String, Any> = withContext(Dispatchers.IO) {
        val enrichedContext = request.context.toMutableMap()
        
        // Add environment context
        val envContext = aiEnvironmentAwareness.getCurrentEnvironmentContext()
        enrichedContext["environment"] = envContext
        
        // Add system state
        val systemState = aiEnvironmentAwareness.getCurrentSystemState()
        enrichedContext["system_state"] = systemState
        
        // Add user preferences if available
        if (request.userId != null) {
            val userPreferences = aiGuidanceSystem.getUserPreferences(request.userId)
            enrichedContext["user_preferences"] = userPreferences
        }
        
        // Add applicable policies
        val policies = aiGuidanceSystem.getApplicablePolicies(enrichedContext)
        enrichedContext["applicable_policies"] = policies.map { it.name }
        
        // Add tool recommendations
        val toolRecommendations = aiEnvironmentAwareness.getToolRecommendations(enrichedContext)
        enrichedContext["recommended_tools"] = toolRecommendations.map { it.tool.toolName }
        
        return@withContext enrichedContext
    }
    
    private suspend fun assessSystemCapabilities(): SystemCapabilities = withContext(Dispatchers.IO) {
        val envStats = aiEnvironmentAwareness.getEnvironmentStatistics()
        val guidanceStats = aiGuidanceSystem.getGuidanceStatistics()
        val resourceUtilization = aiEnvironmentAwareness.getResourceUtilization()
        val systemState = aiEnvironmentAwareness.getCurrentSystemState()
        
        val availableTools = (envStats["registered_tools"] as? Int ?: 0).let { toolCount ->
            List(toolCount) { "tool_$it" } // Simplified for now
        }
        
        val memoryUtilization = resourceUtilization["memory_utilization"] ?: 0.5f
        val cpuUtilization = resourceUtilization["cpu_utilization"] ?: 0.5f
        
        // Calculate system health based on multiple factors
        val systemHealth = listOf(
            1.0f - memoryUtilization * 0.3f,
            1.0f - cpuUtilization * 0.2f,
            if (systemState.networkStatus == AIEnvironmentAwareness.NetworkStatus.CONNECTED) 1.0f else 0.7f,
            if (systemState.zramStatus == AIEnvironmentAwareness.ZRAMStatus.ACTIVE) 1.0f else 0.8f
        ).average().toFloat()
        
        // Recommend max concurrent tasks based on resources
        val recommendedMaxTasks = when {
            memoryUtilization > 0.8f -> 2
            memoryUtilization > 0.6f -> 3
            else -> 5
        }
        
        return@withContext SystemCapabilities(
            availableTools = availableTools,
            supportedTaskTypes = TaskStateManager.TaskType.values().toList(),
            knowledgeBaseSize = guidanceStats["knowledge_entries"] as? Int ?: 0,
            policyCount = guidanceStats["active_policies"] as? Int ?: 0,
            memoryUtilization = memoryUtilization,
            systemHealth = systemHealth,
            recommendedMaxConcurrentTasks = recommendedMaxTasks
        )
    }
    
    private suspend fun generateLearningInsights(
        request: ExecutionRequest,
        result: String,
        taskContext: Map<String, Any>
    ): List<String> = withContext(Dispatchers.IO) {
        val insights = mutableListOf<String>()
        
        val success = !result.contains("❌") && !result.contains("issue")
        val toolsUsed = taskContext["tools_used"] as? List<String> ?: emptyList()
        val errorCount = taskContext["error_count"] as? Int ?: 0
        
        if (success) {
            insights.add("Request completed successfully using ${toolsUsed.size} tools")
            if (toolsUsed.isNotEmpty()) {
                insights.add("Effective tool pattern: ${toolsUsed.joinToString(" → ")}")
            }
        } else {
            insights.add("Request encountered challenges - analyzing for future improvement")
            if (errorCount > 0) {
                insights.add("$errorCount errors occurred - updating error handling strategies")
            }
        }
        
        // Context-specific insights
        if (request.userPrompt.length > 100) {
            insights.add("Complex request detected - detailed task decomposition was beneficial")
        }
        
        if (request.context.isNotEmpty()) {
            insights.add("Context information helped guide execution strategy")
        }
        
        return@withContext insights
    }
    
    private suspend fun generateRecommendations(
        request: ExecutionRequest,
        context: Map<String, Any>,
        result: String
    ): List<String> = withContext(Dispatchers.IO) {
        val recommendations = mutableListOf<String>()
        
        // Get contextual recommendations from guidance system
        val guidanceRecommendations = aiGuidanceSystem.getContextualRecommendations(context)
        recommendations.addAll(guidanceRecommendations.take(2))
        
        // Get tool recommendations
        val toolRecommendations = aiEnvironmentAwareness.getToolRecommendations(context, 2)
        toolRecommendations.forEach { recommendation ->
            recommendations.add("Consider using ${recommendation.tool.toolName}: ${recommendation.reason}")
        }
        
        // Add follow-up suggestions
        if (result.contains("✅")) {
            recommendations.add("Great! You can now try similar operations or explore related features.")
        } else {
            recommendations.add("For better results, try providing more specific details or context.")
        }
        
        return@withContext recommendations.take(5)
    }
    
    private suspend fun updateUserPreferences(
        request: ExecutionRequest,
        result: String,
        insights: List<String>
    ) = withContext(Dispatchers.IO) {
        if (request.userId == null) return@withContext
        
        val success = !result.contains("❌") && !result.contains("issue")
        
        // Learn task type preferences
        val taskType = when {
            request.userPrompt.contains("file", ignoreCase = true) -> "file_operations"
            request.userPrompt.contains("analyze", ignoreCase = true) -> "analysis_tasks"
            request.userPrompt.contains("create", ignoreCase = true) -> "creation_tasks"
            else -> "general_tasks"
        }
        
        aiGuidanceSystem.learnUserPreference(
            userId = request.userId,
            category = AIGuidanceSystem.PreferenceCategory.WORKFLOW_PREFERENCES,
            key = "preferred_task_type",
            value = taskType,
            confidence = if (success) 0.8f else 0.6f,
            learnedFrom = "task_execution_${if (success) "success" else "failure"}"
        )
        
        // Learn interaction style preferences
        val interactionStyle = when {
            request.userPrompt.length > 100 -> "detailed_instructions"
            request.userPrompt.length < 20 -> "brief_instructions"
            else -> "moderate_instructions"
        }
        
        aiGuidanceSystem.learnUserPreference(
            userId = request.userId,
            category = AIGuidanceSystem.PreferenceCategory.UI_PREFERENCES,
            key = "interaction_style",
            value = interactionStyle,
            confidence = 0.7f,
            learnedFrom = "prompt_analysis"
        )
    }
}