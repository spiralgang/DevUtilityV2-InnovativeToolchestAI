package com.spiralgang.srirachaarmy.devutility.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Learning Bot with user pattern recognition
 * Part of DevUtility V2.5 active learning environment bots
 */
@Singleton
class LearningBot @Inject constructor(
    private val aiEnvironmentAwareness: AIEnvironmentAwareness,
    private val aiTrainingSetManager: AITrainingSetManager
) {
    
    private val userPatterns = mutableMapOf<String, UserPattern>()
    private val codingPatterns = mutableMapOf<String, CodingPattern>()
    private val learningInsights = mutableListOf<LearningInsight>()
    private var messageHandler: ((BotMessage) -> Unit)? = null
    private var isInitialized = false
    
    data class UserPattern(
        val userId: String,
        val patternType: PatternType,
        val frequency: Int,
        val confidence: Float,
        val lastObserved: Long,
        val context: String,
        val metadata: Map<String, Any>
    )
    
    data class CodingPattern(
        val patternId: String,
        val language: String,
        val category: String,
        val pattern: String,
        val effectiveness: Float,
        val occurrences: Int,
        val averageTime: Long,
        val successRate: Float,
        val relatedPatterns: List<String>
    )
    
    data class LearningInsight(
        val insightId: String,
        val type: InsightType,
        val title: String,
        val description: String,
        val confidence: Float,
        val supportingData: Map<String, Any>,
        val recommendations: List<String>,
        val createdAt: Long,
        val validUntil: Long
    )
    
    data class BotMessage(
        val type: MessageType,
        val content: String,
        val metadata: Map<String, Any> = emptyMap(),
        val timestamp: Long = System.currentTimeMillis()
    )
    
    enum class PatternType {
        CODING_STYLE,
        LANGUAGE_PREFERENCE,
        TOOL_USAGE,
        TIME_PATTERNS,
        ERROR_PATTERNS,
        LEARNING_SPEED,
        HELP_SEEKING,
        FEATURE_USAGE
    }
    
    enum class InsightType {
        PRODUCTIVITY_TIP,
        LEARNING_OPPORTUNITY,
        EFFICIENCY_IMPROVEMENT,
        SKILL_DEVELOPMENT,
        PATTERN_RECOGNITION,
        PERSONALIZED_SUGGESTION
    }
    
    enum class MessageType {
        LEARNING_UPDATE,
        PATTERN_DETECTED,
        INSIGHT_GENERATED,
        RECOMMENDATION,
        PROGRESS_REPORT
    }
    
    /**
     * Initialize learning bot
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) {
                Timber.d("Learning Bot already initialized")
                return@withContext
            }
            
            Timber.d("Initializing Learning Bot")
            
            // Load existing patterns and insights
            loadExistingPatterns()
            
            isInitialized = true
            
            Timber.d("Learning Bot initialized with ${userPatterns.size} user patterns and ${codingPatterns.size} coding patterns")
            
        } catch (e: Exception) {
            Timber.e(e, "Learning Bot initialization failed")
        }
    }
    
    /**
     * Monitor user activity and learn patterns
     */
    suspend fun monitorUserActivity(
        userId: String,
        activity: String,
        context: String,
        metadata: Map<String, Any> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        try {
            Timber.d("Monitoring user activity: $activity for user $userId")
            
            // Analyze activity for patterns
            val detectedPatterns = analyzeActivityForPatterns(userId, activity, context, metadata)
            
            // Update user patterns
            detectedPatterns.forEach { pattern ->
                updateUserPattern(pattern)
            }
            
            // Check if new insights can be generated
            val insights = generateInsights(userId, activity, context)
            insights.forEach { insight ->
                learningInsights.add(insight)
                notifyInsightGenerated(insight)
            }
            
            // Update AI training based on patterns
            updateAITraining(userId, activity, context, detectedPatterns)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to monitor user activity: $activity")
        }
    }
    
    /**
     * Learn from code interactions
     */
    suspend fun learnFromCode(
        userId: String,
        code: String,
        language: String,
        action: String,
        result: String,
        timeSpent: Long
    ) = withContext(Dispatchers.IO) {
        try {
            Timber.d("Learning from code interaction: $action in $language")
            
            // Extract coding patterns
            val patterns = extractCodingPatterns(code, language, action, result, timeSpent)
            
            // Update coding pattern database
            patterns.forEach { pattern ->
                updateCodingPattern(pattern)
            }
            
            // Analyze effectiveness
            val effectiveness = calculateEffectiveness(action, result, timeSpent)
            
            // Generate personalized suggestions
            val suggestions = generatePersonalizedSuggestions(userId, language, action, effectiveness)
            
            // Broadcast learning update
            broadcastLearningUpdate(userId, language, patterns, suggestions)
            
            // Update environment awareness
            aiEnvironmentAwareness.learnFromInteraction(code, language, action, action)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to learn from code interaction")
        }
    }
    
    /**
     * Provide personalized suggestions based on learning
     */
    suspend fun getPersonalizedSuggestions(
        userId: String,
        context: String = "general"
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val suggestions = mutableListOf<String>()
            
            // Get user patterns for this user
            val userSpecificPatterns = userPatterns.values.filter { it.userId == userId }
            
            // Generate suggestions based on patterns
            userSpecificPatterns.forEach { pattern ->
                when (pattern.patternType) {
                    PatternType.CODING_STYLE -> {
                        suggestions.add("Based on your coding style, consider using ${pattern.metadata["preferred_construct"]}")
                    }
                    PatternType.LANGUAGE_PREFERENCE -> {
                        private const val LANGUAGE_KEY = "language"
                        if (preferredLang != null) {
                            suggestions.add("You seem to prefer $preferredLang. Here are some advanced $preferredLang features you might like.")
                        }
                    }
                    PatternType.ERROR_PATTERNS -> {
                        val commonError = pattern.metadata["common_error"] as? String
                        if (commonError != null) {
                            suggestions.add("I noticed you often encounter $commonError. Here's how to prevent it.")
                        }
                    }
                    PatternType.LEARNING_SPEED -> {
                        val speed = pattern.metadata["speed"] as? String
                        when (speed) {
                            "fast" -> suggestions.add("You're a fast learner! Try these advanced concepts.")
                            "methodical" -> suggestions.add("You learn methodically. Here's a step-by-step guide.")
                        }
                    }
                    else -> { /* Handle other pattern types */ }
                }
            }
            
            // Add context-specific suggestions
            val contextSuggestions = aiEnvironmentAwareness.getContextualSuggestions(context)
            suggestions.addAll(contextSuggestions)
            
            // Get recent insights for this user
            val recentInsights = learningInsights
                .filter { it.supportingData["user_id"] == userId }
                .sortedByDescending { it.createdAt }
                .take(3)
                .flatMap { it.recommendations }
            
            suggestions.addAll(recentInsights)
            
            Timber.d("Generated ${suggestions.size} personalized suggestions for user $userId")
            return@withContext suggestions.distinct().take(10) // Limit and deduplicate
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate personalized suggestions for user $userId")
            return@withContext emptyList()
        }
    }
    
    /**
     * Get learning progress report for user
     */
    suspend fun getLearningProgressReport(userId: String): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val userSpecificPatterns = userPatterns.values.filter { it.userId == userId }
            val userInsights = learningInsights.filter { it.supportingData["user_id"] == userId }
            
            val report = mutableMapOf<String, Any>()
            
            report["user_id"] = userId
            report["total_patterns_detected"] = userSpecificPatterns.size
            report["insights_generated"] = userInsights.size
            
            // Pattern distribution
            val patternDistribution = userSpecificPatterns
                .groupBy { it.patternType }
                .mapValues { it.value.size }
            report["pattern_distribution"] = patternDistribution
            
            // Learning speed analysis
            val learningSpeedPattern = userSpecificPatterns
                .find { it.patternType == PatternType.LEARNING_SPEED }
            report["learning_speed"] = learningSpeedPattern?.metadata?.get("speed") ?: "unknown"
            
            // Preferred languages
            val languagePatterns = userSpecificPatterns
                .filter { it.patternType == PatternType.LANGUAGE_PREFERENCE }
                .sortedByDescending { it.frequency }
                .take(3)
                .map { it.metadata["language"] }
            report["preferred_languages"] = languagePatterns
            
            // Recent insights
            val recentInsights = userInsights
                .sortedByDescending { it.createdAt }
                .take(5)
                .map { mapOf("title" to it.title, "confidence" to it.confidence) }
            report["recent_insights"] = recentInsights
            
            // Improvement areas
            val improvementAreas = identifyImprovementAreas(userId)
            report["improvement_areas"] = improvementAreas
            
            report["report_generated_at"] = System.currentTimeMillis()
            
            return@withContext report
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate learning progress report for user $userId")
            return@withContext mapOf("error" to "Failed to generate report")
        }
    }
    
    /**
     * Receive messages from other bots
     */
    suspend fun receiveMessage(message: BotMessage) = withContext(Dispatchers.IO) {
        try {
            Timber.d("Learning Bot received message: ${message.type}")
            
            when (message.type) {
                MessageType.LEARNING_UPDATE -> {
                    proceseLearningUpdate(message)
                }
                MessageType.PATTERN_DETECTED -> {
                    processPatternDetection(message)
                }
                else -> {
                    // Handle other message types
                    Timber.d("Processing generic message: ${message.type}")
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to process message: ${message.type}")
        }
    }
    
    /**
     * Set message handler for bot communication
     */
    fun setMessageHandler(handler: (BotMessage) -> Unit) {
        messageHandler = handler
        Timber.d("Message handler set for Learning Bot")
    }
    
    // Private helper methods
    private suspend fun loadExistingPatterns() = withContext(Dispatchers.IO) {
        // In real implementation, load from persistent storage
        Timber.d("Loading existing patterns and insights")
    }
    
    private fun analyzeActivityForPatterns(
        userId: String,
        activity: String,
        context: String,
        metadata: Map<String, Any>
    ): List<UserPattern> {
        val patterns = mutableListOf<UserPattern>()
        
        // Time-based patterns
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        patterns.add(UserPattern(
            userId = userId,
            patternType = PatternType.TIME_PATTERNS,
            frequency = 1,
            confidence = 0.7f,
            lastObserved = System.currentTimeMillis(),
            context = context,
            metadata = mapOf("activity" to activity, "hour" to hour)
        ))
        
        // Feature usage patterns
        if (activity.startsWith("use_")) {
            val feature = activity.removePrefix("use_")
            patterns.add(UserPattern(
                userId = userId,
                patternType = PatternType.FEATURE_USAGE,
                frequency = 1,
                confidence = 0.8f,
                lastObserved = System.currentTimeMillis(),
                context = context,
                metadata = mapOf("feature" to feature, "activity" to activity)
            ))
        }
        
        return patterns
    }
    
    private fun updateUserPattern(pattern: UserPattern) {
        val key = "${pattern.userId}_${pattern.patternType}_${pattern.context}"
        val existingPattern = userPatterns[key]
        
        if (existingPattern != null) {
            // Update existing pattern
            val updatedPattern = existingPattern.copy(
                frequency = existingPattern.frequency + 1,
                confidence = minOf(1.0f, existingPattern.confidence + 0.1f),
                lastObserved = pattern.lastObserved,
                metadata = existingPattern.metadata + pattern.metadata
            )
            userPatterns[key] = updatedPattern
        } else {
            // Add new pattern
            userPatterns[key] = pattern
        }
        
        Timber.d("User pattern updated: ${pattern.patternType} for user ${pattern.userId}")
    }
    
    private fun generateInsights(
        userId: String,
        activity: String,
        context: String
    ): List<LearningInsight> {
        val insights = mutableListOf<LearningInsight>()
        
        // Generate insights based on accumulated patterns
        val userSpecificPatterns = userPatterns.values.filter { it.userId == userId }
        
        if (userSpecificPatterns.size >= 5) {
            insights.add(LearningInsight(
                insightId = "insight_${System.currentTimeMillis()}",
                type = InsightType.PATTERN_RECOGNITION,
                title = "Learning Patterns Detected",
                description = "I've identified several patterns in your development workflow.",
                confidence = 0.8f,
                supportingData = mapOf(
                    "user_id" to userId,
                    "pattern_count" to userSpecificPatterns.size,
                    "context" to context
                ),
                recommendations = generateRecommendationsFromPatterns(userSpecificPatterns),
                createdAt = System.currentTimeMillis(),
                validUntil = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // 24 hours
            ))
        }
        
        return insights
    }
    
    private fun extractCodingPatterns(
        code: String,
        language: String,
        action: String,
        result: String,
        timeSpent: Long
    ): List<CodingPattern> {
        val patterns = mutableListOf<CodingPattern>()
        
        // Extract common coding constructs
        val constructs = when (language.lowercase()) {
            "kotlin" -> extractKotlinPatterns(code)
            "java" -> extractJavaPatterns(code)
            "python" -> extractPythonPatterns(code)
            else -> extractGenericPatterns(code)
        }
        
        constructs.forEach { construct ->
            patterns.add(CodingPattern(
                patternId = "${language}_${construct}_${System.currentTimeMillis()}",
                language = language,
                category = "construct",
                pattern = construct,
                effectiveness = if (result == "success") 0.8f else 0.3f,
                occurrences = 1,
                averageTime = timeSpent,
                successRate = if (result == "success") 1.0f else 0.0f,
                relatedPatterns = emptyList()
            ))
        }
        
        return patterns
    }
    
    private fun extractKotlinPatterns(code: String): List<String> {
        val patterns = mutableListOf<String>()
        
        if (code.contains("data class")) patterns.add("data_class")
        if (code.contains("suspend")) patterns.add("coroutines")
        if (code.contains("?.")) patterns.add("null_safety")
        if (code.contains("let")) patterns.add("scope_functions")
        if (code.contains("when")) patterns.add("when_expression")
        
        return patterns
    }
    
    private fun extractJavaPatterns(code: String): List<String> {
        val patterns = mutableListOf<String>()
        
        if (code.contains("interface")) patterns.add("interface")
        if (code.contains("extends")) patterns.add("inheritance")
        if (code.contains("implements")) patterns.add("interface_implementation")
        if (code.contains("synchronized")) patterns.add("synchronization")
        
        return patterns
    }
    
    private fun extractPythonPatterns(code: String): List<String> {
        val patterns = mutableListOf<String>()
        
        if (code.contains("def ")) patterns.add("function_definition")
        if (code.contains("class ")) patterns.add("class_definition")
        if (code.contains("import ")) patterns.add("imports")
        if (code.contains("for ")) patterns.add("for_loop")
        
        return patterns
    }
    
    private fun extractGenericPatterns(code: String): List<String> {
        val patterns = mutableListOf<String>()
        
        if (code.contains("function")) patterns.add("function")
        if (code.contains("class")) patterns.add("class")
        if (code.contains("if")) patterns.add("conditional")
        if (code.contains("loop") || code.contains("for") || code.contains("while")) patterns.add("loop")
        
        return patterns
    }
    
    private fun updateCodingPattern(pattern: CodingPattern) {
        val existingPattern = codingPatterns[pattern.patternId]
        
        if (existingPattern != null) {
            val updatedPattern = existingPattern.copy(
                occurrences = existingPattern.occurrences + 1,
                averageTime = (existingPattern.averageTime + pattern.averageTime) / 2,
                successRate = (existingPattern.successRate * existingPattern.occurrences + pattern.successRate) / (existingPattern.occurrences + 1)
            )
            codingPatterns[pattern.patternId] = updatedPattern
        } else {
            codingPatterns[pattern.patternId] = pattern
        }
    }
    
    private fun calculateEffectiveness(action: String, result: String, timeSpent: Long): Float {
        var effectiveness = 0.5f // Base effectiveness
        
        if (result == "success") effectiveness += 0.3f
        if (timeSpent < 30000) effectiveness += 0.2f // Fast completion
        
        return minOf(1.0f, effectiveness)
    }
    
    private fun generatePersonalizedSuggestions(
        userId: String,
        language: String,
        action: String,
        effectiveness: Float
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (effectiveness < 0.5f) {
            suggestions.add("Consider reviewing $language best practices for $action")
        } else if (effectiveness > 0.8f) {
            suggestions.add("Great job with $action! You might enjoy exploring advanced $language features")
        }
        
        return suggestions
    }
    
    private fun generateRecommendationsFromPatterns(patterns: List<UserPattern>): List<String> {
        val recommendations = mutableListOf<String>()
        
        val patternTypes = patterns.groupBy { it.patternType }
        
        patternTypes.forEach { (type, typePatterns) ->
            when (type) {
                PatternType.TIME_PATTERNS -> {
                    val mostActiveHour = typePatterns
                        .groupBy { it.metadata["hour"] }
                        .maxByOrNull { it.value.size }
                        ?.key as? Int
                    
                    if (mostActiveHour != null) {
                        recommendations.add("You're most productive around ${mostActiveHour}:00. Consider scheduling important tasks during this time.")
                    }
                }
                PatternType.FEATURE_USAGE -> {
                    val mostUsedFeature = typePatterns
                        .groupBy { it.metadata["feature"] }
                        .maxByOrNull { it.value.size }
                        ?.key as? String
                    
                    if (mostUsedFeature != null) {
                        recommendations.add("You frequently use $mostUsedFeature. Here are some advanced tips for this feature.")
                    }
                }
                else -> { /* Handle other pattern types */ }
            }
        }
        
        return recommendations
    }
    
    private fun identifyImprovementAreas(userId: String): List<String> {
        val areas = mutableListOf<String>()
        
        val userPatterns = userPatterns.values.filter { it.userId == userId }
        val errorPatterns = userPatterns.filter { it.patternType == PatternType.ERROR_PATTERNS }
        
        if (errorPatterns.isNotEmpty()) {
            areas.add("Error handling and debugging")
        }
        
        val codingStylePatterns = userPatterns.filter { it.patternType == PatternType.CODING_STYLE }
        if (codingStylePatterns.any { it.confidence < 0.7f }) {
            areas.add("Code style consistency")
        }
        
        return areas
    }
    
    private fun updateAITraining(
        userId: String,
        activity: String,
        context: String,
        patterns: List<UserPattern>
    ) {
        // Update AI training sets based on learned patterns
        patterns.forEach { pattern ->
            // This would update the AI training sets with new examples
            // based on successful user patterns
        }
    }
    
    private fun broadcastLearningUpdate(
        userId: String,
        language: String,
        patterns: List<CodingPattern>,
        suggestions: List<String>
    ) {
        val message = BotMessage(
            type = MessageType.LEARNING_UPDATE,
            content = "Learning update for user $userId in $language",
            metadata = mapOf(
                "user_id" to userId,
                "language" to language,
                "patterns_count" to patterns.size,
                "suggestions_count" to suggestions.size
            )
        )
        
        messageHandler?.invoke(message)
    }
    
    private fun notifyInsightGenerated(insight: LearningInsight) {
        val message = BotMessage(
            type = MessageType.INSIGHT_GENERATED,
            content = insight.title,
            metadata = mapOf(
                "insight_id" to insight.insightId,
                "type" to insight.type,
                "confidence" to insight.confidence
            )
        )
        
        messageHandler?.invoke(message)
    }
    
    private fun proceseLearningUpdate(message: BotMessage) {
        // Process learning updates from other bots
        Timber.d("Processing learning update: ${message.content}")
    }
    
    private fun processPatternDetection(message: BotMessage) {
        // Process pattern detection notifications from other bots
        Timber.d("Processing pattern detection: ${message.content}")
    }
}