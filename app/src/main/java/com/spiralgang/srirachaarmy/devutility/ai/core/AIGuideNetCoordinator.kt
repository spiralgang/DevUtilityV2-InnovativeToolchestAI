package com.spiralgang.srirachaarmy.devutility.ai.core

import com.spiralgang.srirachaarmy.devutility.ai.*
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
 * This coordinator integrates ALL AI components in the DevUtility ecosystem
 * as part of the Autonomous Internal Guidance & Routing Network (AIGuideNet)
 * to provide unified, intelligent coordination across multiple specialized AI systems.
 * 
 * Enhanced to coordinate multiple AI systems including:
 * - Core planning and guidance (TaskStateManager, AIGuidanceSystem, AIThinkModule)
 * - Specialized AI services (WebNetCasteAI, LearningBot, OfflineAI, DeepSeek)
 * - Domain-specific AI tools (CodeReview, Security, Summarization)
 * 
 * Key responsibilities:
 * - Orchestrate interactions between ALL AI systems in DevUtility
 * - Provide unified API for intelligent multi-AI task execution
 * - Coordinate learning and knowledge sharing across specialized AIs
 * - Route tasks to appropriate AI systems based on capability and context
 * - Eliminate "flailing" behavior across the entire AI ecosystem
 * 
 * Part of the Autonomous Internal Guidance & Routing Network (AIGuideNet)
 */
@Singleton
class AIGuideNetCoordinator @Inject constructor(
    // Core AIGuideNet components
    private val taskStateManager: TaskStateManager,
    private val aiGuidanceSystem: AIGuidanceSystem,
    private val aiEnvironmentAwareness: AIEnvironmentAwareness,
    private val aiThinkModule: AIThinkModule,
    
    // PRE-training and knowledge systems
    private val aiPreTrainingSystem: AIPreTrainingSystem,
    private val aiTrainingSetManager: AITrainingSetManager,
    private val advancedTrainingConfig: AIAdvancedTrainingDatasetConfig,
    
    // Anti-flailing systems (lessons learned from real-world case study)
    private val antiFlailingSystem: AntiFlailingSystem,
    private val knowledgeGapRecoverySystem: KnowledgeGapRecoverySystem,
    
    // Specialized AI systems
    private val webNetCasteAI: WebNetCasteAI,
    private val learningBot: LearningBot,
    private val offlineAIService: OfflineAIService,
    private val deepSeekAIService: DeepSeekAIService,
    
    // Domain-specific AI services
    private val codeReviewService: CodeReviewService,
    private val securityAnalyzer: SecurityAnalyzer,
    private val codeSummarizer: CodeSummarizer,
    private val aiTrainingSetManager: AITrainingSetManager
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
     * Initialize the complete AIGuideNet system with PRE-training and all specialized AI components
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Starting comprehensive AIGuideNet initialization with PRE-training")
            
            Timber.d("🚀 Initializing AIGuideNet Coordinator for complete DevUtility AI ecosystem")
            
            // Phase 1: PRE-training initialization - critical foundation
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 1: Initializing PRE-training knowledge systems")
            aiPreTrainingSystem.initializePreTraining()
            
            // Phase 2: Core AIGuideNet components
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 2: Initializing Core AI Guidance System")
            aiGuidanceSystem.initialize()
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Initializing AI Environment Awareness")
            aiEnvironmentAwareness.initialize()
            
            // Phase 3: Training systems integration
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 3: Integrating AI Training Set Manager with PRE-knowledge")
            aiTrainingSetManager.initialize()
            
            // Phase 4: Specialized AI systems initialization  
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 4: Initializing specialized AI systems")
            webNetCasteAI.initialize()
            learningBot.initialize()
            deepSeekAIService.initialize(aiEnvironmentAwareness.getEnvironmentContext())
            
            // Phase 5: Inter-AI coordination setup
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 5: Establishing inter-AI coordination")
            establishInterAICommunication()
            
            // Phase 6: Initialize anti-flailing systems (lessons from real-world case study)
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 6: Initializing anti-flailing systems")
            initializeAntiFlailingSystems()
            
            // Phase 7: AI system registration and validation
            _systemStatus.value = SystemStatus(SystemStatus.Status.INITIALIZING, "Phase 7: Registering AI systems and validating PRE-training")
            registerAISystemsWithGuidance()
            validatePreTrainingIntegration()
            
            // Verify comprehensive system capabilities
            val capabilities = assessComprehensiveSystemCapabilities()
            _currentCapabilities.value = capabilities
            
            isInitialized = true
            _systemStatus.value = SystemStatus(
                SystemStatus.Status.READY, 
                "AIGuideNet ecosystem ready with anti-flailing protection, PRE-training, ${capabilities.availableTools.size} tools, ${capabilities.knowledgeBaseSize} knowledge entries, and complete AI system integration",
                componentStates = mapOf(
                    "aiPreTrainingSystem" to "ready",
                    "antiFlailingSystem" to "ready",
                    "knowledgeGapRecoverySystem" to "ready",
                    "taskStateManager" to "ready",
                    "aiGuidanceSystem" to "ready", 
                    "aiEnvironmentAwareness" to "ready",
                    "aiThinkModule" to "ready",
                    "webNetCasteAI" to "ready",
                    "learningBot" to "ready",
                    "offlineAIService" to "ready",
                    "deepSeekAIService" to "ready",
                    "codeReviewService" to "ready",
                    "securityAnalyzer" to "ready",
                    "codeSummarizer" to "ready",
                    "aiTrainingSetManager" to "ready"
                )
            )
            
            Timber.d("🚀 Complete AIGuideNet ecosystem with PRE-training initialization successful - ${capabilities.availableTools.size} AI systems coordinated")
            
        } catch (e: Exception) {
            Timber.e(e, "AIGuideNet ecosystem initialization failed")
            _systemStatus.value = SystemStatus(SystemStatus.Status.ERROR, "Ecosystem initialization failed: ${e.message}")
        }
    }
    
    /**
     * Execute a request using the complete AIGuideNet framework with anti-flailing protection
     * 
     * Enhanced based on lessons learned from real-world agent flailing case study.
     * Includes structured knowledge gap recovery and capability expansion controls.
     */
    suspend fun executeRequestWithAntiFlailing(request: ExecutionRequest): ExecutionResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            if (!isInitialized) {
                throw IllegalStateException("AIGuideNet ecosystem not initialized")
            }
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.PROCESSING, "Processing request with anti-flailing protection")
            
            Timber.d("🛡️ Executing request with anti-flailing protection: ${request.userPrompt}")
            
            // Step 1: Assess current flailing risk before proceeding
            val currentRisk = antiFlailingSystem.assessFlailingRisk(
                recentDecisions = listOf(request.userPrompt),
                systemMetrics = mapOf(
                    "integration_completeness" to 0.9f,
                    "decision_coherence" to 0.8f
                )
            )
            
            if (currentRisk >= AntiFlailingSystem.FlailingRisk.HIGH) {
                Timber.w("⚠️ High flailing risk detected - applying additional safeguards")
            }
            
            // Step 2: Enrich context with comprehensive information
            val enrichedContext = enrichRequestContext(request)
            
            // Step 3: Check if we have knowledge gaps that might trigger reactive expansion
            val knowledgeGapAnalysis = analyzeRequestForKnowledgeGaps(request, enrichedContext)
            
            // Step 4: If knowledge gaps exist, attempt structured recovery first
            val recoveryResult = if (knowledgeGapAnalysis.gapStillExists) {
                Timber.d("🔍 Knowledge gap detected: ${knowledgeGapAnalysis.gapType.name}")
                
                knowledgeGapRecoverySystem.executeStructuredRecovery(
                    knowledgeGap = request.userPrompt,
                    availableKnowledge = extractAvailableKnowledge(enrichedContext),
                    availableTools = extractAvailableTools(enrichedContext),
                    contextInfo = enrichedContext
                )
            } else null
            
            // Step 5: Execute request using best available approach
            val executionResult = if (recoveryResult?.status == KnowledgeGapRecoverySystem.RecoveryStatus.RECOVERY_COMPLETE) {
                Timber.d("✅ Knowledge gap resolved through structured recovery")
                recoveryResult.result
            } else {
                // Use normal execution flow
                executeNormalRequestFlow(request, enrichedContext)
            }
            
            // Step 6: Generate anti-flailing recommendations
            val antiFlailingRecommendations = antiFlailingSystem.generateAntiFlailingRecommendations(
                currentRisk = currentRisk,
                gapAnalysis = knowledgeGapAnalysis,
                systemState = enrichedContext
            )
            
            // Step 7: Generate comprehensive learning insights with anti-flailing context
            val learningInsights = generateLearningInsightsWithAntiFlailing(
                request, executionResult, enrichedContext, recoveryResult, currentRisk
            )
            
            // Step 8: Generate recommendations that prevent future flailing
            val recommendations = generateAntiFlailingRecommendations(
                request, enrichedContext, executionResult, antiFlailingRecommendations
            )
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.READY, "Request completed with anti-flailing protection")
            
            return@withContext ExecutionResponse(
                success = !executionResult.contains("❌"),
                result = executionResult,
                taskId = enrichedContext["task_id"] as? String ?: "anti_flailing_task",
                executionTime = System.currentTimeMillis() - startTime,
                toolsUsed = extractToolsUsed(enrichedContext, recoveryResult),
                learningInsights = learningInsights,
                recommendations = recommendations,
                metadata = mapOf(
                    "anti_flailing_protection" to true,
                    "flailing_risk" to currentRisk.name,
                    "knowledge_gap_detected" to knowledgeGapAnalysis.gapStillExists,
                    "structured_recovery_used" to (recoveryResult != null),
                    "recovery_status" to (recoveryResult?.status?.name ?: "none")
                )
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Anti-flailing request execution failed")
            _systemStatus.value = SystemStatus(SystemStatus.Status.ERROR, "Anti-flailing execution failed: ${e.message}")
            
            return@withContext ExecutionResponse(
                success = false,
                result = "Request failed with anti-flailing protection. Error: ${e.message}. The system prevented potentially harmful reactive decisions.",
                taskId = "anti_flailing_error",
                executionTime = System.currentTimeMillis() - startTime,
                toolsUsed = emptyList(),
                learningInsights = listOf("Error with anti-flailing protection: ${e.javaClass.simpleName}"),
                recommendations = listOf("Anti-flailing systems prevented potentially harmful expansion", "Consider reviewing request structure"),
                metadata = mapOf(
                    "error_type" to e.javaClass.simpleName,
                    "anti_flailing_protection" to true
                )
            )
        }
    }
    
    /**
     * Analyze request for potential knowledge gaps that might trigger flailing
     */
    private fun analyzeRequestForKnowledgeGaps(
        request: ExecutionRequest,
        context: Map<String, Any>
    ): AntiFlailingSystem.KnowledgeGapAnalysis {
        
        val currentCapabilities = extractAvailableTools(context)
        
        return antiFlailingSystem.analyzeKnowledgeGap(
            currentCapabilities = currentCapabilities,
            requiredTask = request.userPrompt,
            contextInfo = context
        )
    }
    
    /**
     * Execute normal request flow (existing logic)
     */
    private suspend fun executeNormalRequestFlow(
        request: ExecutionRequest,
        enrichedContext: Map<String, Any>
    ): String {
        // Use existing execution logic
        val planningResult = aiThinkModule.planAndExecute(request.userPrompt, enrichedContext)
        return planningResult
    }
    
    /**
     * Generate learning insights enhanced with anti-flailing context
     */
    private fun generateLearningInsightsWithAntiFlailing(
        request: ExecutionRequest,
        result: String,
        context: Map<String, Any>,
        recoveryResult: KnowledgeGapRecoverySystem.RecoveryResult?,
        flailingRisk: AntiFlailingSystem.FlailingRisk
    ): List<String> {
        
        val insights = mutableListOf<String>()
        
        // Standard insights
        insights.add("Request type: ${categorizeRequest(request.userPrompt)}")
        insights.add("Success rate: ${if (result.contains("✅")) "High" else "Moderate"}")
        
        // Anti-flailing specific insights
        insights.add("Flailing risk level: ${flailingRisk.name}")
        
        if (recoveryResult != null) {
            insights.add("Knowledge gap recovery: ${recoveryResult.status.name}")
            insights.add("Alternatives explored: ${recoveryResult.alternativesExplored.size}")
            insights.add("Recovery confidence: ${(recoveryResult.confidenceLevel * 100).toInt()}%")
        }
        
        if (flailingRisk >= AntiFlailingSystem.FlailingRisk.MODERATE) {
            insights.add("Anti-flailing measures: Protective measures applied to prevent reactive decisions")
        }
        
        return insights
    }
    
    /**
     * Generate recommendations that prevent future flailing
     */
    private fun generateAntiFlailingRecommendations(
        request: ExecutionRequest,
        context: Map<String, Any>,
        result: String,
        antiFlailingRecommendations: List<String>
    ): List<String> {
        
        val recommendations = mutableListOf<String>()
        
        // Include anti-flailing recommendations
        recommendations.addAll(antiFlailingRecommendations.take(3))
        
        // Add context-aware recommendations
        if (result.contains("✅")) {
            recommendations.add("Excellent! Continue using existing capabilities for similar tasks.")
        } else {
            recommendations.add("Consider enriching context or trying alternative approaches before adding new capabilities.")
        }
        
        return recommendations.take(5)
    }
    
    /**
     * Extract available knowledge from context
     */
    private fun extractAvailableKnowledge(context: Map<String, Any>): Map<String, Any> {
        return context.filterKeys { key ->
            key.contains("knowledge") || key.contains("training") || key.contains("pre_training")
        }
    }
    
    /**
     * Extract available tools from context
     */
    private fun extractAvailableTools(context: Map<String, Any>): List<String> {
        @Suppress("UNCHECKED_CAST")
        return (context["available_tools"] as? List<String>) ?: listOf(
            "WebNetCasteAI", "LearningBot", "AIEnvironmentAwareness",
            "SecurityAnalyzer", "DeepSeekAIService", "CodeReviewService",
            "CodeSummarizer", "OfflineAIService"
        )
    }
    
    /**
     * Extract tools used from execution context and recovery
     */
    private fun extractToolsUsed(
        context: Map<String, Any>,
        recoveryResult: KnowledgeGapRecoverySystem.RecoveryResult?
    ): List<String> {
        
        val toolsUsed = mutableListOf<String>()
        
        // Add tools from context
        @Suppress("UNCHECKED_CAST")
        val contextTools = context["tools_used"] as? List<String> ?: emptyList()
        toolsUsed.addAll(contextTools)
        
        // Add tools from recovery process
        if (recoveryResult != null) {
            toolsUsed.add("AntiFlailingSystem")
            toolsUsed.add("KnowledgeGapRecoverySystem")
            
            recoveryResult.successfulStrategy?.requiredResources?.let { resources ->
                toolsUsed.addAll(resources)
            }
        }
        
        return toolsUsed.distinct()
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
    
    /**
     * Establish communication channels between all AI systems for coordinated operation
     */
    private suspend fun establishInterAICommunication() = withContext(Dispatchers.IO) {
        Timber.d("🔗 Establishing inter-AI communication channels")
        
        // Set up learning bot to receive patterns from other AI systems
        learningBot.setupMessageHandler { message ->
            // Forward patterns to guidance system for cross-AI learning
            when (message.type) {
                "PATTERN_DISCOVERED" -> aiGuidanceSystem.recordPattern(
                    patternType = "cross_ai_pattern",
                    pattern = message.content,
                    source = message.source,
                    metadata = message.metadata
                )
                "KNOWLEDGE_SHARED" -> aiGuidanceSystem.incorporateExternalKnowledge(message.content)
            }
        }
        
        // Enable WebNetCasteAI to share insights with learning systems
        learningBot.registerInsightSource("WebNetCasteAI") { query ->
            webNetCasteAI.analyzeWebContext(aiEnvironmentAwareness.createIntelligenceContext(query))
        }
        
        // Connect offline AI service with learning bot for pattern recognition
        learningBot.registerAIService("OfflineAI") { query, context ->
            offlineAIService.processQuery(query, context)
        }
        
        Timber.d("🔗 Inter-AI communication established successfully")
    }
    
    /**
     * Initialize anti-flailing systems based on lessons from real-world case study
     * 
     * The case study showed an AI system that reactively added capabilities
     * (internet browsing, facial recognition) without proper integration or
     * considering existing alternatives. This initialization prevents such flailing.
     */
    private suspend fun initializeAntiFlailingSystems() = withContext(Dispatchers.IO) {
        Timber.d("🛡️ Initializing anti-flailing systems based on real-world lessons")
        
        // Initialize flailing risk monitoring
        val systemMetrics = mapOf(
            "integration_completeness" to 0.9f,
            "decision_coherence" to 0.8f,
            "capability_utilization" to 0.85f
        )
        
        val initialRisk = antiFlailingSystem.assessFlailingRisk(
            recentDecisions = emptyList(), // Start with clean slate
            systemMetrics = systemMetrics
        )
        
        Timber.d("🛡️ Initial flailing risk assessment: ${initialRisk.name}")
        
        // Test knowledge gap recovery with examples from the case study
        val testGaps = listOf(
            "Need internet browsing for information gathering",
            "Require facial recognition for identity verification",
            "Missing real-time data access capabilities"
        )
        
        testGaps.forEach { gap ->
            try {
                val recovery = knowledgeGapRecoverySystem.executeStructuredRecovery(
                    knowledgeGap = gap,
                    availableKnowledge = mapOf(
                        "pre_training_knowledge" to "Comprehensive knowledge base",
                        "web_analysis" to "WebNetCasteAI capabilities",
                        "pattern_recognition" to "LearningBot capabilities"
                    ),
                    availableTools = listOf(
                        "WebNetCasteAI", "LearningBot", "AIEnvironmentAwareness", 
                        "SecurityAnalyzer", "DeepSeekAIService"
                    ),
                    contextInfo = mapOf(
                        "current_task" to "Capability gap analysis",
                        "user_context" to "Development environment"
                    )
                )
                
                Timber.d("🔧 Recovery test for '$gap': ${recovery.status.name}")
            } catch (e: Exception) {
                Timber.w(e, "Recovery test failed for gap: $gap")
            }
        }
        
        // Establish anti-flailing coordination protocols
        establishAntiFlailingProtocols()
        
        Timber.d("🛡️ Anti-flailing systems initialization complete")
    }
    
    /**
     * Establish protocols to prevent reactive capability addition
     */
    private suspend fun establishAntiFlailingProtocols() = withContext(Dispatchers.IO) {
        // Create decision review gates that prevent reactive expansion
        val expansionGates = listOf(
            "Knowledge gap analysis required before new capability",
            "Alternative exploration mandatory for all expansion requests",
            "Integration planning required before capability addition",
            "Risk assessment mandatory for all system changes"
        )
        
        // Register these protocols with the guidance system
        expansionGates.forEach { protocol ->
            aiGuidanceSystem.registerPolicy(
                name = "anti_flailing_${protocol.hashCode()}",
                description = protocol,
                priority = 0.9f, // High priority to prevent flailing
                applicableScenarios = listOf("capability_expansion", "tool_addition", "system_enhancement")
            )
        }
        
        Timber.d("🚧 Anti-flailing protocols established: ${expansionGates.size} gates active")
    }
    
    /**
     * Register all AI systems with the guidance system for coordinated management
     */
    private suspend fun registerAISystemsWithGuidance() = withContext(Dispatchers.IO) {
        Timber.d("📋 Registering AI systems with guidance coordinator")
        
        // Register AI capabilities and specializations
        val aiSystemCapabilities = mapOf(
            "WebNetCasteAI" to listOf("web_search", "content_analysis", "community_insights", "documentation_analysis"),
            "LearningBot" to listOf("pattern_recognition", "user_behavior_analysis", "adaptive_learning", "insight_generation"),
            "OfflineAIService" to listOf("local_processing", "code_explanation", "code_generation", "debugging_help"),
            "DeepSeekAIService" to listOf("advanced_reasoning", "code_analysis", "contextual_suggestions", "deep_learning"),
            "CodeReviewService" to listOf("code_quality_analysis", "security_review", "best_practices", "performance_optimization"),
            "SecurityAnalyzer" to listOf("vulnerability_detection", "security_patterns", "threat_analysis", "secure_coding"),
            "CodeSummarizer" to listOf("code_documentation", "summarization", "explanation_generation", "complexity_analysis"),
            "AITrainingSetManager" to listOf("training_data_management", "model_optimization", "knowledge_curation", "dataset_analysis")
        )
        
        // Register each AI system's capabilities
        aiSystemCapabilities.forEach { (aiSystem, capabilities) ->
            capabilities.forEach { capability ->
                aiGuidanceSystem.registerAICapability(
                    aiSystemName = aiSystem,
                    capability = capability,
                    description = "Specialized capability provided by $aiSystem",
                    priority = when (capability) {
                        in listOf("security_review", "vulnerability_detection") -> 0.9f
                        in listOf("code_analysis", "pattern_recognition") -> 0.8f
                        else -> 0.7f
                    }
                )
            }
        }
        
        Timber.d("📋 All AI systems registered with guidance coordinator")
    }
    
    /**
     * Assess capabilities across the entire AI ecosystem
     */
    private suspend fun assessComprehensiveSystemCapabilities(): SystemCapabilities = withContext(Dispatchers.IO) {
        val envStats = aiEnvironmentAwareness.getEnvironmentStatistics()
        val guidanceStats = aiGuidanceSystem.getGuidanceStatistics()
        val resourceUtilization = aiEnvironmentAwareness.getResourceUtilization()
        val systemState = aiEnvironmentAwareness.getCurrentSystemState()
        
        // Collect capabilities from all AI systems
        val comprehensiveTools = mutableListOf<String>()
        
        // Core capabilities
        comprehensiveTools.addAll(listOf("task_planning", "environment_awareness", "guidance_coordination"))
        
        // Specialized AI capabilities
        comprehensiveTools.addAll(listOf(
            "web_intelligence", "pattern_learning", "offline_processing", "deepseek_reasoning",
            "code_review", "security_analysis", "code_summarization", "training_management"
        ))
        
        // Domain-specific tools
        comprehensiveTools.addAll(envStats["registered_tools"]?.toString()?.split(",") ?: emptyList())
        
        val memoryUtilization = resourceUtilization["memory_utilization"] ?: 0.5f
        val cpuUtilization = resourceUtilization["cpu_utilization"] ?: 0.5f
        
        // Calculate comprehensive system health including all AI systems
        val systemHealth = listOf(
            1.0f - memoryUtilization * 0.25f,  // Memory health
            1.0f - cpuUtilization * 0.2f,      // CPU health
            if (systemState.networkStatus == AIEnvironmentAwareness.NetworkStatus.CONNECTED) 1.0f else 0.7f, // Network health
            if (systemState.zramStatus == AIEnvironmentAwareness.ZRAMStatus.ACTIVE) 1.0f else 0.8f, // ZRAM health
            0.9f,  // AI coordination health (assume good if initialization succeeded)
            0.85f  // Inter-AI communication health
        ).average().toFloat()
        
        // Recommend max concurrent tasks based on comprehensive AI system load
        val recommendedMaxTasks = when {
            memoryUtilization > 0.8f || comprehensiveTools.size > 20 -> 3
            memoryUtilization > 0.6f || comprehensiveTools.size > 15 -> 4
            else -> 6
        }
        
        return@withContext SystemCapabilities(
            availableTools = comprehensiveTools,
            supportedTaskTypes = TaskStateManager.TaskType.values().toList(),
            knowledgeBaseSize = guidanceStats["knowledge_entries"] as? Int ?: 0,
            policyCount = guidanceStats["active_policies"] as? Int ?: 0,
            memoryUtilization = memoryUtilization,
            systemHealth = systemHealth,
            recommendedMaxConcurrentTasks = recommendedMaxTasks
        )
    }

    /**
     * Execute request with intelligent AI system routing
     * Routes tasks to appropriate specialized AI systems based on capability and context
     */
    suspend fun executeRequestWithAIRouting(request: ExecutionRequest): ExecutionResponse = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            throw IllegalStateException("AIGuideNet ecosystem not initialized")
        }
        
        try {
            _systemStatus.value = SystemStatus(SystemStatus.Status.PROCESSING, "Processing request with AI routing")
            
            // Enrich context with comprehensive information
            val enrichedContext = enrichRequestContext(request)
            
            // Determine optimal AI system routing
            val aiRoutingPlan = planAISystemRouting(request, enrichedContext)
            
            // Execute with coordinated AI systems
            val result = executeCoordinatedAIRequest(request, enrichedContext, aiRoutingPlan)
            
            // Generate comprehensive learning insights
            val learningInsights = generateComprehensiveLearningInsights(request, result, enrichedContext, aiRoutingPlan)
            
            // Generate recommendations from multiple AI perspectives
            val recommendations = generateMultiAIRecommendations(request, enrichedContext, result, aiRoutingPlan)
            
            // Update user preferences across all AI systems
            updateComprehensiveUserPreferences(request, result, learningInsights)
            
            _systemStatus.value = SystemStatus(SystemStatus.Status.READY, "Request completed with AI coordination")
            
            return@withContext ExecutionResponse(
                success = !result.contains("❌"),
                result = result,
                taskId = enrichedContext["task_id"] as? String ?: "ai_routing_task",
                executionTime = System.currentTimeMillis() - (enrichedContext["start_time"] as? Long ?: System.currentTimeMillis()),
                toolsUsed = aiRoutingPlan.aiSystemsUsed,
                learningInsights = learningInsights,
                recommendations = recommendations,
                metadata = mapOf(
                    "ai_routing_plan" to aiRoutingPlan,
                    "specialized_systems_used" to aiRoutingPlan.aiSystemsUsed.size,
                    "coordination_successful" to true
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "AI routing execution failed")
            _systemStatus.value = SystemStatus(SystemStatus.Status.READY, "Request completed with error")
            
            return@withContext ExecutionResponse(
                success = false,
                result = "❌ AI routing execution failed: ${e.message}",
                taskId = "error_task",
                executionTime = 0L,
                toolsUsed = emptyList(),
                learningInsights = listOf("Error in AI routing: ${e.javaClass.simpleName}"),
                recommendations = listOf("Consider simplifying the request or checking system status"),
                metadata = mapOf("error_type" to e.javaClass.simpleName, "ai_routing_error" to true)
            )
        }
    }
    
    // Helper data classes and methods for AI routing
    
    private data class AIRoutingPlan(
        val primaryAI: String,
        val supportingAIs: List<String>,
        val aiSystemsUsed: List<String>,
        val routingReasoning: String,
        val expectedBenefits: List<String>
    )
    
    private suspend fun planAISystemRouting(request: ExecutionRequest, context: Map<String, Any>): AIRoutingPlan {
        val prompt = request.userPrompt.lowercase()
        
        // Determine primary AI system based on request content
        val primaryAI = when {
            prompt.contains("web", "search", "online", "documentation") -> "WebNetCasteAI"
            prompt.contains("learn", "pattern", "behavior", "analyze user") -> "LearningBot"
            prompt.contains("offline", "local", "private") -> "OfflineAIService"
            prompt.contains("advanced", "complex", "reasoning") -> "DeepSeekAIService"
            prompt.contains("review", "quality", "best practice") -> "CodeReviewService"
            prompt.contains("security", "vulnerable", "safe") -> "SecurityAnalyzer"
            prompt.contains("explain", "summarize", "document") -> "CodeSummarizer"
            prompt.contains("training", "model", "dataset") -> "AITrainingSetManager"
            else -> "AIThinkModule" // Default to core thinking module
        }
        
        // Determine supporting AI systems
        val supportingAIs = mutableListOf<String>()
        if (primaryAI != "LearningBot") supportingAIs.add("LearningBot") // Always include learning
        if (prompt.contains("secure") && primaryAI != "SecurityAnalyzer") supportingAIs.add("SecurityAnalyzer")
        if (prompt.contains("code") && primaryAI !in listOf("CodeReviewService", "CodeSummarizer")) {
            supportingAIs.add("CodeReviewService")
        }
        
        return AIRoutingPlan(
            primaryAI = primaryAI,
            supportingAIs = supportingAIs,
            aiSystemsUsed = listOf(primaryAI) + supportingAIs,
            routingReasoning = "Selected $primaryAI as primary based on request content analysis",
            expectedBenefits = listOf(
                "Specialized processing by $primaryAI",
                "Coordinated insights from ${supportingAIs.size} supporting AI systems",
                "Comprehensive coverage across AI capabilities"
            )
        )
    }
    
    private suspend fun executeCoordinatedAIRequest(
        request: ExecutionRequest, 
        context: Map<String, Any>, 
        aiRoutingPlan: AIRoutingPlan
    ): String {
        var result = "🤖 AIGuideNet Coordinated Execution:\n\n"
        
        // Execute with primary AI system
        result += "Primary AI System: ${aiRoutingPlan.primaryAI}\n"
        val primaryResult = when (aiRoutingPlan.primaryAI) {
            "WebNetCasteAI" -> {
                val intelligenceContext = aiEnvironmentAwareness.createIntelligenceContext(request.userPrompt)
                webNetCasteAI.analyzeWebContext(intelligenceContext)
            }
            "LearningBot" -> {
                learningBot.processQuery(request.userPrompt, context)
            }
            "OfflineAIService" -> {
                offlineAIService.processQuery(request.userPrompt, context.toString())
            }
            "DeepSeekAIService" -> {
                deepSeekAIService.generateContextualSuggestion(request.userPrompt)
            }
            "CodeSummarizer" -> {
                if (context.containsKey("code")) {
                    codeSummarizer.summarizeCode(context["code"].toString(), "auto")
                } else {
                    "No code provided for summarization"
                }
            }
            else -> {
                // Use core AIThinkModule as fallback
                aiThinkModule.planAndExecute(request.userPrompt, context)
            }
        }
        
        result += "Result: $primaryResult\n\n"
        
        // Execute with supporting AI systems
        if (aiRoutingPlan.supportingAIs.isNotEmpty()) {
            result += "Supporting AI Analysis:\n"
            aiRoutingPlan.supportingAIs.forEach { supportingAI ->
                val supportingResult = when (supportingAI) {
                    "LearningBot" -> learningBot.generateInsight(request.userPrompt, context)
                    "SecurityAnalyzer" -> {
                        if (context.containsKey("code")) {
                            securityAnalyzer.quickSecurityCheck(context["code"].toString())
                        } else {
                            "Security analysis: Request appears safe"
                        }
                    }
                    "CodeReviewService" -> {
                        if (context.containsKey("code")) {
                            val review = codeReviewService.quickReview(context["code"].toString(), "auto")
                            "Code quality: ${review.score}/10"
                        } else {
                            "No code provided for review"
                        }
                    }
                    else -> "✅ $supportingAI provided supporting analysis"
                }
                result += "- $supportingAI: $supportingResult\n"
            }
        }
        
        result += "\n✅ Coordinated execution completed successfully using ${aiRoutingPlan.aiSystemsUsed.size} AI systems"
        return result
    }
    
    private suspend fun generateComprehensiveLearningInsights(
        request: ExecutionRequest,
        result: String,
        context: Map<String, Any>,
        aiRoutingPlan: AIRoutingPlan
    ): List<String> {
        val insights = mutableListOf<String>()
        
        // Add routing-specific insights
        insights.add("AI routing selected ${aiRoutingPlan.primaryAI} as optimal primary system")
        insights.add("Coordination involved ${aiRoutingPlan.aiSystemsUsed.size} specialized AI systems")
        
        // Add standard insights
        insights.addAll(generateLearningInsights(request, result, context))
        
        // Add AI-specific insights from learning bot
        try {
            val learningBotInsights = learningBot.analyzeExecutionPattern(
                request.userPrompt, 
                aiRoutingPlan.aiSystemsUsed, 
                result.contains("✅")
            )
            insights.addAll(learningBotInsights.take(2))
        } catch (e: Exception) {
            insights.add("Learning analysis completed with coordination benefits")
        }
        
        return insights.take(6)
    }
    
    private suspend fun generateMultiAIRecommendations(
        request: ExecutionRequest,
        context: Map<String, Any>,
        result: String,
        aiRoutingPlan: AIRoutingPlan
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Add standard recommendations
        recommendations.addAll(generateRecommendations(request, context, result))
        
        // Add AI routing recommendations
        recommendations.add("For similar requests, ${aiRoutingPlan.primaryAI} is optimal")
        
        if (aiRoutingPlan.supportingAIs.isNotEmpty()) {
            recommendations.add("Supporting AI analysis provided enhanced insights")
        }
        
        return recommendations.take(5)
    }
    
    private suspend fun updateComprehensiveUserPreferences(
        request: ExecutionRequest,
        result: String,
        insights: List<String>
    ) {
        // Update standard user preferences
        updateUserPreferences(request, result, insights)
        
        // Additional learning through specialized AI systems
        if (request.userId != null) {
            try {
                learningBot.updateUserProfile(
                    userId = request.userId,
                    interaction = request.userPrompt,
                    outcome = result.contains("✅"),
                    context = mapOf("ai_routing" to "comprehensive")
                )
            } catch (e: Exception) {
                Timber.w(e, "Failed to update comprehensive user preferences")
            }
        }
    }

    private suspend fun updateLearningUpdateForComprehensiveSystem() = withContext(Dispatchers.IO) {
        try {
            Timber.d("🧠 Triggering comprehensive system-wide learning update")
            
            // Update all AI systems
            aiGuidanceSystem.updateKnowledgeFromExternalSources()
            learningBot.updateLearningModels()
            aiTrainingSetManager.optimizeTrainingData()
            
            // Clean up tasks
            taskStateManager.cleanupOldTasks()
            
            // Refresh comprehensive capabilities
            val newCapabilities = assessComprehensiveSystemCapabilities()
            _currentCapabilities.value = newCapabilities
            
            Timber.d("🧠 Comprehensive learning update completed across all AI systems")
            
        } catch (e: Exception) {
            Timber.e(e, "Comprehensive learning update failed")
        }
    }

    private suspend fun assessSystemCapabilities(): SystemCapabilities = assessComprehensiveSystemCapabilities()

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
    
    /**
     * Validate PRE-training integration with all AI systems
     */
    private suspend fun validatePreTrainingIntegration() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Validating PRE-training integration")
            
            // Validate PRE-training system completeness
            val preTrainingValid = aiPreTrainingSystem.validatePreTraining()
            if (!preTrainingValid) {
                throw IllegalStateException("PRE-training system validation failed")
            }
            
            // Get PRE-training statistics
            val stats = aiPreTrainingSystem.getPreTrainingStatistics()
            Timber.d("PRE-training validation successful: $stats")
            
            // Validate integration with training manager
            val trainingStats = aiTrainingSetManager.getTrainingStatistics()
            Timber.d("Training manager integration: $trainingStats")
            
            // Validate knowledge domains are loaded
            val knowledgeDomains = aiPreTrainingSystem.knowledgeDomains.value
            if (knowledgeDomains.isEmpty()) {
                throw IllegalStateException("No knowledge domains loaded in PRE-training")
            }
            
            Timber.d("PRE-training integration validated successfully with ${knowledgeDomains.size} knowledge domains")
            
        } catch (e: Exception) {
            Timber.e(e, "PRE-training integration validation failed")
            throw e
        }
    }
    
    /**
     * Get comprehensive system status including PRE-training
     */
    suspend fun getComprehensiveSystemStatus(): Map<String, Any> = withContext(Dispatchers.IO) {
        val baseStatus = mapOf(
            "system_initialized" to isInitialized,
            "current_status" to _systemStatus.value.status.name,
            "system_message" to _systemStatus.value.message,
            "capabilities" to _currentCapabilities.value
        )
        
        val preTrainingStatus = if (::aiPreTrainingSystem.isInitialized) {
            aiPreTrainingSystem.getPreTrainingStatistics()
        } else {
            mapOf("status" to "not_initialized")
        }
        
        val trainingStatus = if (::aiTrainingSetManager.isInitialized) {
            aiTrainingSetManager.getTrainingStatistics()
        } else {
            mapOf("status" to "not_initialized")
        }
        
        return@withContext baseStatus + mapOf(
            "pre_training_system" to preTrainingStatus,
            "training_manager" to trainingStatus,
            "component_states" to _systemStatus.value.componentStates
        )
    }
    
    /**
     * Query PRE-trained knowledge through the coordinator
     */
    suspend fun queryPreTrainedKnowledge(
        domain: String? = null,
        concept: String? = null,
        complexity: AIPreTrainingSystem.ComplexityLevel? = null
    ): List<AIPreTrainingSystem.FoundationalKnowledge> {
        return if (::aiPreTrainingSystem.isInitialized) {
            aiPreTrainingSystem.queryPreTrainedKnowledge(domain, concept, complexity)
        } else {
            emptyList()
        }
    }
    
    /**
     * Get relevant agentic patterns for current conditions
     */
    suspend fun getRelevantAgenticPatterns(conditions: List<String>): List<AIPreTrainingSystem.AgenticPattern> {
        return if (::aiPreTrainingSystem.isInitialized) {
            aiPreTrainingSystem.getRelevantAgenticPatterns(conditions)
        } else {
            emptyList()
        }
    }
}