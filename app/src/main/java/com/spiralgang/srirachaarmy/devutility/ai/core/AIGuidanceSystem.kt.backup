package com.spiralgang.srirachaarmy.devutility.ai.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AIGuidanceSystem - Persistent Memory & Policy Engine of AIGuideNet
 * 
 * This system moves beyond simple "signs" to become a robust Knowledge & Policy Store
 * that guides the Executive Planner and ensures consistent behavior. It acts as the
 * Long-Term Knowledge/Experience Memory.
 * 
 * Part of the Autonomous Internal Guidance & Routing Network (AIGuideNet)
 */
@Singleton
class AIGuidanceSystem @Inject constructor() {
    
    private val _knowledgeBase = MutableStateFlow<Map<String, KnowledgeEntry>>(emptyMap())
    val knowledgeBase: StateFlow<Map<String, KnowledgeEntry>> = _knowledgeBase.asStateFlow()
    
    private val _policies = MutableStateFlow<Map<String, PolicyRule>>(emptyMap())
    val policies: StateFlow<Map<String, PolicyRule>> = _policies.asStateFlow()
    
    private val _userPreferences = MutableStateFlow<Map<String, UserPreference>>(emptyMap())
    val userPreferences: StateFlow<Map<String, UserPreference>> = _userPreferences.asStateFlow()
    
    private val workflowPatterns = mutableMapOf<String, WorkflowPattern>()
    private val toolUsagePatterns = mutableMapOf<String, ToolUsagePattern>()
    private val errorRemediationStrategies = mutableMapOf<String, RemediationStrategy>()
    private val reflectionInsights = mutableListOf<ReflectionInsight>()
    
    data class KnowledgeEntry(
        val id: String = UUID.randomUUID().toString(),
        val category: KnowledgeCategory,
        val topic: String,
        val content: String,
        val confidence: Float,
        val sources: List<String>,
        val tags: List<String>,
        val lastUpdated: Long = System.currentTimeMillis(),
        val accessCount: Int = 0,
        val successRate: Float = 1.0f,
        val relatedEntries: List<String> = emptyList(),
        val metadata: Map<String, Any> = emptyMap()
    )
    
    data class PolicyRule(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val description: String,
        val category: PolicyCategory,
        val condition: String, // Condition when this policy applies
        val action: String, // What action to take
        val priority: PolicyPriority,
        val active: Boolean = true,
        val constraints: Map<String, Any> = emptyMap(),
        val exceptions: List<String> = emptyList(),
        val createdAt: Long = System.currentTimeMillis(),
        val lastTriggered: Long? = null,
        val triggerCount: Int = 0
    )
    
    data class UserPreference(
        val id: String = UUID.randomUUID().toString(),
        val userId: String,
        val category: PreferenceCategory,
        val key: String,
        val value: String,
        val confidence: Float,
        val learnedFrom: String, // How this preference was learned
        val lastUpdated: Long = System.currentTimeMillis(),
        val reinforcementCount: Int = 1,
        val context: Map<String, Any> = emptyMap()
    )
    
    data class WorkflowPattern(
        val id: String = UUID.randomUUID().toString(),
        val name: String,
        val description: String,
        val steps: List<WorkflowStep>,
        val successRate: Float,
        val averageExecutionTime: Long,
        val usageCount: Int,
        val lastUsed: Long,
        val context: Map<String, Any> = emptyMap(),
        val variations: List<String> = emptyList()
    )
    
    data class WorkflowStep(
        val stepNumber: Int,
        val toolName: String,
        val parameters: Map<String, Any>,
        val expectedOutputs: List<String>,
        val conditionalLogic: String? = null
    )
    
    data class ToolUsagePattern(
        val toolName: String,
        val context: String,
        val frequency: Int,
        val averageExecutionTime: Long,
        val successRate: Float,
        val commonParameters: Map<String, Any>,
        val commonFailures: List<String>,
        val bestPractices: List<String>,
        val lastUsed: Long
    )
    
    data class RemediationStrategy(
        val id: String = UUID.randomUUID().toString(),
        val errorType: String,
        val errorPattern: String,
        val strategy: String,
        val steps: List<String>,
        val successRate: Float,
        val usageCount: Int,
        val lastUsed: Long,
        val alternativeStrategies: List<String> = emptyList()
    )
    
    data class ReflectionInsight(
        val id: String = UUID.randomUUID().toString(),
        val type: ReflectionType,
        val title: String,
        val insight: String,
        val confidence: Float,
        val supportingEvidence: List<String>,
        val actionableRecommendations: List<String>,
        val createdAt: Long = System.currentTimeMillis(),
        val validUntil: Long,
        val metadata: Map<String, Any> = emptyMap()
    )
    
    enum class KnowledgeCategory {
        TOOL_USAGE,
        WORKFLOW_PATTERNS,
        BEST_PRACTICES,
        ERROR_SOLUTIONS,
        API_DOCUMENTATION,
        CODE_PATTERNS,
        OPTIMIZATION_TECHNIQUES,
        DOMAIN_KNOWLEDGE,
        USER_PATTERNS
    }
    
    enum class PolicyCategory {
        SECURITY,
        RESOURCE_MANAGEMENT,
        WORKFLOW_OPTIMIZATION,
        ERROR_HANDLING,
        USER_INTERACTION,
        SYSTEM_CONSTRAINTS,
        QUALITY_ASSURANCE
    }
    
    enum class PolicyPriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
    
    enum class PreferenceCategory {
        CODING_STYLE,
        TOOL_PREFERENCES,
        UI_PREFERENCES,
        WORKFLOW_PREFERENCES,
        NOTIFICATION_PREFERENCES,
        LANGUAGE_PREFERENCES,
        ENVIRONMENT_PREFERENCES
    }
    
    enum class ReflectionType {
        PERFORMANCE_ANALYSIS,
        ERROR_PATTERN_ANALYSIS,
        WORKFLOW_OPTIMIZATION,
        USER_BEHAVIOR_INSIGHT,
        TOOL_EFFECTIVENESS,
        LEARNING_PROGRESS
    }
    
    /**
     * Initialize the guidance system with default knowledge and policies
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Initializing AI Guidance System")
            
            loadDefaultKnowledge()
            loadDefaultPolicies()
            loadSystemConstraints()
            
            Timber.d("AI Guidance System initialized with ${_knowledgeBase.value.size} knowledge entries and ${_policies.value.size} policies")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize AI Guidance System")
        }
    }
    
    /**
     * Query the knowledge base for relevant information
     */
    suspend fun queryKnowledge(
        query: String,
        category: KnowledgeCategory? = null,
        maxResults: Int = 10
    ): List<KnowledgeEntry> = withContext(Dispatchers.IO) {
        try {
            val entries = _knowledgeBase.value.values
                .filter { entry ->
                    (category == null || entry.category == category) &&
                    (entry.topic.contains(query, ignoreCase = true) ||
                     entry.content.contains(query, ignoreCase = true) ||
                     entry.tags.any { it.contains(query, ignoreCase = true) })
                }
                .sortedWith(
                    compareByDescending<KnowledgeEntry> { it.confidence }
                        .thenByDescending { it.successRate }
                        .thenByDescending { it.accessCount }
                )
                .take(maxResults)
            
            // Update access counts
            entries.forEach { entry ->
                updateKnowledgeAccessCount(entry.id)
            }
            
            Timber.d("Knowledge query '$query' returned ${entries.size} results")
            return@withContext entries
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to query knowledge base: $query")
            return@withContext emptyList()
        }
    }
    
    /**
     * Add new knowledge to the knowledge base
     */
    suspend fun addKnowledge(
        category: KnowledgeCategory,
        topic: String,
        content: String,
        confidence: Float = 0.8f,
        sources: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): KnowledgeEntry = withContext(Dispatchers.IO) {
        val entry = KnowledgeEntry(
            category = category,
            topic = topic,
            content = content,
            confidence = confidence,
            sources = sources,
            tags = tags
        )
        
        val currentKnowledge = _knowledgeBase.value.toMutableMap()
        currentKnowledge[entry.id] = entry
        _knowledgeBase.value = currentKnowledge
        
        Timber.d("Added knowledge entry: $topic")
        return@withContext entry
    }
    
    /**
     * Get applicable policies for a given context
     */
    suspend fun getApplicablePolicies(
        context: Map<String, Any>
    ): List<PolicyRule> = withContext(Dispatchers.IO) {
        try {
            val applicablePolicies = _policies.value.values
                .filter { policy ->
                    policy.active && evaluatePolicyCondition(policy, context)
                }
                .sortedByDescending { it.priority.ordinal }
            
            // Update trigger counts
            applicablePolicies.forEach { policy ->
                updatePolicyTriggerCount(policy.id)
            }
            
            Timber.d("Found ${applicablePolicies.size} applicable policies for context")
            return@withContext applicablePolicies
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to get applicable policies")
            return@withContext emptyList()
        }
    }
    
    /**
     * Add or update a policy rule
     */
    suspend fun addPolicy(
        name: String,
        description: String,
        category: PolicyCategory,
        condition: String,
        action: String,
        priority: PolicyPriority = PolicyPriority.NORMAL,
        constraints: Map<String, Any> = emptyMap()
    ): PolicyRule = withContext(Dispatchers.IO) {
        val policy = PolicyRule(
            name = name,
            description = description,
            category = category,
            condition = condition,
            action = action,
            priority = priority,
            constraints = constraints
        )
        
        val currentPolicies = _policies.value.toMutableMap()
        currentPolicies[policy.id] = policy
        _policies.value = currentPolicies
        
        Timber.d("Added policy: $name")
        return@withContext policy
    }
    
    /**
     * Learn user preference from interaction
     */
    suspend fun learnUserPreference(
        userId: String,
        category: PreferenceCategory,
        key: String,
        value: String,
        confidence: Float = 0.7f,
        learnedFrom: String
    ): UserPreference = withContext(Dispatchers.IO) {
        val preferenceKey = "${userId}_${category}_${key}"
        val existingPreference = _userPreferences.value[preferenceKey]
        
        val preference = if (existingPreference != null) {
            // Update existing preference
            existingPreference.copy(
                value = value,
                confidence = minOf(1.0f, existingPreference.confidence + 0.1f),
                reinforcementCount = existingPreference.reinforcementCount + 1,
                lastUpdated = System.currentTimeMillis()
            )
        } else {
            // Create new preference
            UserPreference(
                userId = userId,
                category = category,
                key = key,
                value = value,
                confidence = confidence,
                learnedFrom = learnedFrom
            )
        }
        
        val currentPreferences = _userPreferences.value.toMutableMap()
        currentPreferences[preferenceKey] = preference
        _userPreferences.value = currentPreferences
        
        Timber.d("Learned user preference: $key = $value for user $userId")
        return@withContext preference
    }
    
    /**
     * Get user preferences for a specific user and category
     */
    suspend fun getUserPreferences(
        userId: String,
        category: PreferenceCategory? = null
    ): List<UserPreference> = withContext(Dispatchers.IO) {
        return@withContext _userPreferences.value.values
            .filter { pref ->
                pref.userId == userId && (category == null || pref.category == category)
            }
            .sortedByDescending { it.confidence }
    }
    
    /**
     * Record successful workflow pattern
     */
    suspend fun recordWorkflowPattern(
        name: String,
        description: String,
        steps: List<WorkflowStep>,
        executionTime: Long,
        context: Map<String, Any> = emptyMap()
    ): WorkflowPattern = withContext(Dispatchers.IO) {
        val existingPattern = workflowPatterns[name]
        
        val pattern = if (existingPattern != null) {
            existingPattern.copy(
                averageExecutionTime = (existingPattern.averageExecutionTime + executionTime) / 2,
                usageCount = existingPattern.usageCount + 1,
                lastUsed = System.currentTimeMillis(),
                successRate = minOf(1.0f, existingPattern.successRate + 0.05f)
            )
        } else {
            WorkflowPattern(
                name = name,
                description = description,
                steps = steps,
                successRate = 0.8f,
                averageExecutionTime = executionTime,
                usageCount = 1,
                lastUsed = System.currentTimeMillis(),
                context = context
            )
        }
        
        workflowPatterns[name] = pattern
        
        Timber.d("Recorded workflow pattern: $name")
        return@withContext pattern
    }
    
    /**
     * Get recommended workflow patterns for a context
     */
    suspend fun getRecommendedWorkflows(
        context: Map<String, Any>
    ): List<WorkflowPattern> = withContext(Dispatchers.IO) {
        return@withContext workflowPatterns.values
            .filter { pattern ->
                // Simple context matching - in real implementation, use more sophisticated matching
                pattern.context.any { (key, value) ->
                    context[key] == value
                }
            }
            .sortedWith(
                compareByDescending<WorkflowPattern> { it.successRate }
                    .thenByDescending { it.usageCount }
                    .thenBy { it.averageExecutionTime }
            )
            .take(5)
    }
    
    /**
     * Record tool usage pattern for learning
     */
    suspend fun recordToolUsage(
        toolName: String,
        context: String,
        executionTime: Long,
        success: Boolean,
        parameters: Map<String, Any>
    ) = withContext(Dispatchers.IO) {
        val key = "${toolName}_${context}"
        val existingPattern = toolUsagePatterns[key]
        
        val pattern = if (existingPattern != null) {
            existingPattern.copy(
                frequency = existingPattern.frequency + 1,
                averageExecutionTime = (existingPattern.averageExecutionTime + executionTime) / 2,
                successRate = if (success) {
                    minOf(1.0f, existingPattern.successRate + 0.05f)
                } else {
                    maxOf(0.0f, existingPattern.successRate - 0.05f)
                },
                lastUsed = System.currentTimeMillis(),
                commonParameters = mergeParameters(existingPattern.commonParameters, parameters)
            )
        } else {
            ToolUsagePattern(
                toolName = toolName,
                context = context,
                frequency = 1,
                averageExecutionTime = executionTime,
                successRate = if (success) 0.8f else 0.2f,
                commonParameters = parameters,
                commonFailures = if (!success) listOf("execution_failure") else emptyList(),
                bestPractices = emptyList(),
                lastUsed = System.currentTimeMillis()
            )
        }
        
        toolUsagePatterns[key] = pattern
        
        Timber.d("Recorded tool usage pattern: $toolName in $context")
    }
    
    /**
     * Add reflection insight from task outcomes
     */
    suspend fun addReflectionInsight(
        type: ReflectionType,
        title: String,
        insight: String,
        confidence: Float,
        supportingEvidence: List<String>,
        recommendations: List<String>,
        validFor: Long = 7 * 24 * 60 * 60 * 1000 // 7 days
    ): ReflectionInsight = withContext(Dispatchers.IO) {
        val reflectionInsight = ReflectionInsight(
            type = type,
            title = title,
            insight = insight,
            confidence = confidence,
            supportingEvidence = supportingEvidence,
            actionableRecommendations = recommendations,
            validUntil = System.currentTimeMillis() + validFor
        )
        
        reflectionInsights.add(reflectionInsight)
        
        // Keep only recent insights
        val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000) // 30 days
        reflectionInsights.removeAll { it.createdAt < cutoffTime }
        
        Timber.d("Added reflection insight: $title")
        return@withContext reflectionInsight
    }
    
    /**
     * Get contextual recommendations based on stored knowledge and patterns
     */
    suspend fun getContextualRecommendations(
        currentContext: Map<String, Any>,
        taskType: String? = null
    ): List<String> = withContext(Dispatchers.IO) {
        val recommendations = mutableListOf<String>()
        
        // Knowledge-based recommendations
        val relevantKnowledge = queryKnowledge(
            taskType ?: "general", 
            null, 
            3
        )
        relevantKnowledge.forEach { knowledge ->
            recommendations.add("Based on knowledge: ${knowledge.content}")
        }
        
        // Policy-based recommendations
        val applicablePolicies = getApplicablePolicies(currentContext)
        applicablePolicies.take(2).forEach { policy ->
            recommendations.add("Policy guidance: ${policy.action}")
        }
        
        // Pattern-based recommendations
        val recommendedWorkflows = getRecommendedWorkflows(currentContext)
        recommendedWorkflows.take(2).forEach { workflow ->
            recommendations.add("Workflow suggestion: ${workflow.description}")
        }
        
        // Recent insights
        val recentInsights = reflectionInsights
            .filter { it.validUntil > System.currentTimeMillis() }
            .sortedByDescending { it.confidence }
            .take(2)
        
        recentInsights.forEach { insight ->
            recommendations.addAll(insight.actionableRecommendations)
        }
        
        return@withContext recommendations.distinct().take(10)
    }
    
    /**
     * Get guidance system statistics
     */
    suspend fun getGuidanceStatistics(): Map<String, Any> = withContext(Dispatchers.IO) {
        val stats = mutableMapOf<String, Any>()
        
        stats["knowledge_entries"] = _knowledgeBase.value.size
        stats["active_policies"] = _policies.value.values.count { it.active }
        stats["user_preferences"] = _userPreferences.value.size
        stats["workflow_patterns"] = workflowPatterns.size
        stats["tool_usage_patterns"] = toolUsagePatterns.size
        stats["reflection_insights"] = reflectionInsights.size
        
        // Knowledge distribution by category
        val knowledgeDistribution = _knowledgeBase.value.values
            .groupBy { it.category }
            .mapValues { it.value.size }
        stats["knowledge_by_category"] = knowledgeDistribution
        
        // Policy distribution by category
        val policyDistribution = _policies.value.values
            .groupBy { it.category }
            .mapValues { it.value.size }
        stats["policies_by_category"] = policyDistribution
        
        // Most accessed knowledge
        val topKnowledge = _knowledgeBase.value.values
            .sortedByDescending { it.accessCount }
            .take(5)
            .map { "${it.topic} (${it.accessCount} accesses)" }
        stats["most_accessed_knowledge"] = topKnowledge
        
        return@withContext stats
    }
    
    // Private helper methods
    
    private suspend fun loadDefaultKnowledge() = withContext(Dispatchers.IO) {
        // Load essential development knowledge
        addKnowledge(
            KnowledgeCategory.BEST_PRACTICES,
            "Code Optimization",
            "Always optimize for readability first, then performance. Use profiling tools to identify bottlenecks before optimizing.",
            0.9f,
            listOf("software_engineering"),
            listOf("optimization", "readability", "performance")
        )
        
        addKnowledge(
            KnowledgeCategory.WORKFLOW_PATTERNS,
            "Test-Driven Development",
            "Write tests before implementation. Follow Red-Green-Refactor cycle for better code quality.",
            0.9f,
            listOf("tdd_practices"),
            listOf("testing", "tdd", "quality")
        )
        
        addKnowledge(
            KnowledgeCategory.ERROR_SOLUTIONS,
            "Memory Management",
            "Use ZRAM for compressed memory. Monitor memory usage with ResourceOptimizer. Implement proper lifecycle management.",
            0.8f,
            listOf("android_development"),
            listOf("memory", "zram", "optimization")
        )
    }
    
    private suspend fun loadDefaultPolicies() = withContext(Dispatchers.IO) {
        // Security policies
        addPolicy(
            "Secure File Access",
            "Never access sensitive directories without explicit permission",
            PolicyCategory.SECURITY,
            "file_access AND sensitive_directory",
            "REQUEST_PERMISSION_FIRST",
            PolicyPriority.CRITICAL
        )
        
        // Resource management policies
        addPolicy(
            "Memory Optimization",
            "Always optimize compression before cloud upload for large files",
            PolicyCategory.RESOURCE_MANAGEMENT,
            "file_size > 10MB AND upload_target = cloud",
            "COMPRESS_BEFORE_UPLOAD",
            PolicyPriority.HIGH
        )
        
        // Workflow optimization policies
        addPolicy(
            "Tool Selection",
            "Use most successful tool pattern for repeated operations",
            PolicyCategory.WORKFLOW_OPTIMIZATION,
            "repeated_operation AND success_rate_available",
            "USE_HIGHEST_SUCCESS_RATE_TOOL",
            PolicyPriority.NORMAL
        )
    }
    
    private suspend fun loadSystemConstraints() = withContext(Dispatchers.IO) {
        // Add system-level constraints and policies
        addPolicy(
            "Resource Limits",
            "Respect system resource limits to maintain stability",
            PolicyCategory.SYSTEM_CONSTRAINTS,
            "resource_usage > 80%",
            "THROTTLE_OPERATIONS",
            PolicyPriority.HIGH
        )
    }
    
    private fun evaluatePolicyCondition(policy: PolicyRule, context: Map<String, Any>): Boolean {
        // Simple condition evaluation - in real implementation, use expression evaluator
        return when (policy.condition) {
            "file_access AND sensitive_directory" -> {
                context["operation"] == "file_access" && context["directory_type"] == "sensitive"
            }
            "file_size > 10MB AND upload_target = cloud" -> {
                (context["file_size"] as? Long ?: 0) > 10 * 1024 * 1024 && context["upload_target"] == "cloud"
            }
            "repeated_operation AND success_rate_available" -> {
                context["operation_count"] as? Int ?: 0 > 1 && context["success_rate"] != null
            }
            "resource_usage > 80%" -> {
                (context["resource_usage"] as? Float ?: 0f) > 0.8f
            }
            else -> true // Default to applying if condition not recognized
        }
    }
    
    private suspend fun updateKnowledgeAccessCount(entryId: String) = withContext(Dispatchers.IO) {
        val currentKnowledge = _knowledgeBase.value.toMutableMap()
        val entry = currentKnowledge[entryId]
        if (entry != null) {
            currentKnowledge[entryId] = entry.copy(accessCount = entry.accessCount + 1)
            _knowledgeBase.value = currentKnowledge
        }
    }
    
    private suspend fun updatePolicyTriggerCount(policyId: String) = withContext(Dispatchers.IO) {
        val currentPolicies = _policies.value.toMutableMap()
        val policy = currentPolicies[policyId]
        if (policy != null) {
            currentPolicies[policyId] = policy.copy(
                triggerCount = policy.triggerCount + 1,
                lastTriggered = System.currentTimeMillis()
            )
            _policies.value = currentPolicies
        }
    }
    
    private fun mergeParameters(existing: Map<String, Any>, new: Map<String, Any>): Map<String, Any> {
        // Simple parameter merging - in real implementation, use more sophisticated merging
        return existing + new
    }
    
    // Methods for comprehensive AI system coordination
    
    /**
     * Register AI system capability for coordinated routing
     */
    suspend fun registerAICapability(
        aiSystemName: String,
        capability: String,
        description: String,
        priority: Float = 0.7f
    ) = withContext(Dispatchers.IO) {
        try {
            // Store AI capability as knowledge for routing decisions
            addKnowledge(
                category = KnowledgeCategory.WORKFLOW_PATTERNS,
                topic = "AI_Capability_$aiSystemName",
                content = "$aiSystemName provides $capability: $description",
                confidence = priority,
                sources = listOf("ai_system_registration"),
                tags = listOf("ai_capability", capability, aiSystemName.lowercase())
            )
            
            Timber.d("Registered AI capability: $capability for $aiSystemName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register AI capability: $capability for $aiSystemName")
        }
    }
    
    /**
     * Record pattern from other AI systems for cross-AI learning
     */
    suspend fun recordPattern(
        patternType: String,
        pattern: String,
        source: String,
        metadata: Map<String, Any> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        try {
            // Store cross-AI pattern as knowledge
            addKnowledge(
                category = KnowledgeCategory.WORKFLOW_PATTERNS,
                topic = "CrossAI_Pattern_$patternType",
                content = "Pattern from $source: $pattern",
                confidence = metadata["confidence"] as? Float ?: 0.7f,
                sources = listOf(source),
                tags = listOf("cross_ai_pattern", patternType, source.lowercase()) + 
                       (metadata["tags"] as? List<String> ?: emptyList())
            )
            
            Timber.d("Recorded cross-AI pattern: $patternType from $source")
        } catch (e: Exception) {
            Timber.e(e, "Failed to record pattern: $patternType from $source")
        }
    }
    
    /**
     * Incorporate external knowledge from other AI systems
     */
    suspend fun incorporateExternalKnowledge(content: String) = withContext(Dispatchers.IO) {
        try {
            // Parse and categorize external knowledge
            val category = when {
                content.contains("security", ignoreCase = true) -> KnowledgeCategory.SECURITY_GUIDELINES
                content.contains("performance", ignoreCase = true) -> KnowledgeCategory.BEST_PRACTICES
                content.contains("error", ignoreCase = true) -> KnowledgeCategory.ERROR_SOLUTIONS
                else -> KnowledgeCategory.GENERAL_KNOWLEDGE
            }
            
            addKnowledge(
                category = category,
                topic = "External_Knowledge_${System.currentTimeMillis()}",
                content = content,
                confidence = 0.8f,
                sources = listOf("external_ai_system"),
                tags = listOf("external_knowledge", "ai_shared")
            )
            
            Timber.d("Incorporated external knowledge: ${content.take(50)}...")
        } catch (e: Exception) {
            Timber.e(e, "Failed to incorporate external knowledge")
        }
    }
}