package com.spiralgang.srirachaarmy.devutility.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anonymous usage analytics for data-driven improvements
 * Part of DevUtility V2.5 user feedback and analytics
 */
@Singleton
class AnalyticsManager @Inject constructor() {
    
    private val analyticsEvents = mutableListOf<AnalyticsEvent>()
    private val sessionData = mutableMapOf<String, SessionInfo>()
    private val featureUsage = mutableMapOf<String, FeatureUsageStats>()
    private val performanceMetrics = mutableListOf<PerformanceMetric>()
    
    data class AnalyticsEvent(
        val eventId: String,
        val eventType: String,
        val eventName: String,
        val properties: Map<String, Any>,
        val timestamp: Long,
        val sessionId: String,
        val userId: String? = null, // Anonymous by default
        val context: String
    )
    
    data class SessionInfo(
        val sessionId: String,
        val startTime: Long,
        val endTime: Long? = null,
        val duration: Long? = null,
        val eventsCount: Int = 0,
        val crashCount: Int = 0,
        val deviceInfo: DeviceInfo,
        val appVersion: String
    )
    
    data class FeatureUsageStats(
        val featureName: String,
        val usageCount: Int,
        val totalTimeSpent: Long,
        val averageSessionTime: Long,
        val lastUsed: Long,
        val popularActions: Map<String, Int>,
        val errorCount: Int = 0
    )
    
    data class PerformanceMetric(
        val metricType: String,
        val metricName: String,
        val value: Double,
        val unit: String,
        val timestamp: Long,
        val context: String,
        val tags: Map<String, String>
    )
    
    data class DeviceInfo(
        val deviceType: String,
        val manufacturer: String,
        val model: String,
        val osVersion: String,
        val appVersion: String,
        val screenResolution: String,
        val memorySize: Long,
        val storageSize: Long
    )
    
    private var currentSessionId: String? = null
    private var isAnalyticsEnabled = true
    
    /**
     * Initialize analytics system
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Initializing Analytics Manager")
            
            // Start new session
            startSession()
            
            // Load previous analytics data if needed
            loadPreviousData()
            
            Timber.d("Analytics Manager initialized")
            
        } catch (e: Exception) {
            Timber.e(e, "Analytics Manager initialization failed")
        }
    }
    
    /**
     * Track user event
     */
    fun trackEvent(
        eventName: String,
        properties: Map<String, Any> = emptyMap(),
        context: String = "general"
    ) {
        if (!isAnalyticsEnabled) return
        
        try {
            val eventId = generateEventId()
            val sessionId = currentSessionId ?: "no_session"
            
            val event = AnalyticsEvent(
                eventId = eventId,
                eventType = "user_action",
                eventName = eventName,
                properties = properties,
                timestamp = System.currentTimeMillis(),
                sessionId = sessionId,
                context = context
            )
            
            analyticsEvents.add(event)
            updateSessionEventCount(sessionId)
            
            Timber.d("Event tracked: $eventName in $context")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to track event: $eventName")
        }
    }
    
    /**
     * Track feature usage
     */
    fun trackFeatureUsage(
        featureName: String,
        action: String,
        timeSpent: Long = 0,
        context: String = "general"
    ) {
        if (!isAnalyticsEnabled) return
        
        try {
            val currentTime = System.currentTimeMillis()
            
            val existingStats = featureUsage[featureName]
            val updatedStats = if (existingStats != null) {
                val updatedActions = existingStats.popularActions.toMutableMap()
                updatedActions[action] = updatedActions.getOrDefault(action, 0) + 1
                
                existingStats.copy(
                    usageCount = existingStats.usageCount + 1,
                    totalTimeSpent = existingStats.totalTimeSpent + timeSpent,
                    averageSessionTime = (existingStats.totalTimeSpent + timeSpent) / (existingStats.usageCount + 1),
                    lastUsed = currentTime,
                    popularActions = updatedActions
                )
            } else {
                FeatureUsageStats(
                    featureName = featureName,
                    usageCount = 1,
                    totalTimeSpent = timeSpent,
                    averageSessionTime = timeSpent,
                    lastUsed = currentTime,
                    popularActions = mapOf(action to 1)
                )
            }
            
            featureUsage[featureName] = updatedStats
            
            // Track as event
            trackEvent("feature_used", mapOf(
                "feature" to featureName,
                "action" to action,
                "time_spent" to timeSpent
            ), context)
            
            Timber.d("Feature usage tracked: $featureName - $action")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to track feature usage: $featureName")
        }
    }
    
    /**
     * Track performance metric
     */
    fun trackPerformanceMetric(
        metricName: String,
        value: Double,
        unit: String,
        context: String = "general",
        tags: Map<String, String> = emptyMap()
    ) {
        if (!isAnalyticsEnabled) return
        
        try {
            val metric = PerformanceMetric(
                metricType = "performance",
                metricName = metricName,
                value = value,
                unit = unit,
                timestamp = System.currentTimeMillis(),
                context = context,
                tags = tags
            )
            
            performanceMetrics.add(metric)
            
            // Also track as event
            trackEvent("performance_metric", mapOf(
                "metric_name" to metricName,
                "value" to value,
                "unit" to unit
            ), context)
            
            Timber.d("Performance metric tracked: $metricName = $value $unit")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to track performance metric: $metricName")
        }
    }
    
    /**
     * Track error/exception
     */
    fun trackError(
        errorType: String,
        errorMessage: String,
        stackTrace: String? = null,
        context: String = "general",
        isFatal: Boolean = false
    ) {
        if (!isAnalyticsEnabled) return
        
        try {
            val properties = mutableMapOf<String, Any>(
                "error_type" to errorType,
                "error_message" to errorMessage,
                "is_fatal" to isFatal,
                "context" to context
            )
            
            stackTrace?.let { properties["stack_trace"] = it }
            
            trackEvent("error_occurred", properties, "error")
            
            // Update feature error count if context indicates a specific feature
            if (context != "general") {
                val stats = featureUsage[context]
                if (stats != null) {
                    featureUsage[context] = stats.copy(errorCount = stats.errorCount + 1)
                }
            }
            
            // Update session crash count if fatal
            if (isFatal) {
                currentSessionId?.let { sessionId ->
                    val session = sessionData[sessionId]
                    if (session != null) {
                        sessionData[sessionId] = session.copy(crashCount = session.crashCount + 1)
                    }
                }
            }
            
            Timber.d("Error tracked: $errorType - $errorMessage")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to track error: $errorType")
        }
    }
    
    /**
     * Start new session
     */
    fun startSession(): String {
        try {
            val sessionId = generateSessionId()
            currentSessionId = sessionId
            
            val session = SessionInfo(
                sessionId = sessionId,
                startTime = System.currentTimeMillis(),
                deviceInfo = collectDeviceInfo(),
                appVersion = getAppVersion()
            )
            
            sessionData[sessionId] = session
            
            trackEvent("session_started", mapOf("session_id" to sessionId))
            
            Timber.d("New session started: $sessionId")
            return sessionId
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to start session")
            return "error_session"
        }
    }
    
    /**
     * End current session
     */
    fun endSession() {
        try {
            val sessionId = currentSessionId
            if (sessionId != null) {
                val session = sessionData[sessionId]
                if (session != null) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - session.startTime
                    
                    sessionData[sessionId] = session.copy(
                        endTime = endTime,
                        duration = duration
                    )
                    
                    trackEvent("session_ended", mapOf(
                        "session_id" to sessionId,
                        "duration" to duration,
                        "events_count" to session.eventsCount
                    ))
                    
                    Timber.d("Session ended: $sessionId (duration: ${duration}ms)")
                }
                
                currentSessionId = null
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to end session")
        }
    }
    
    /**
     * Get analytics summary
     */
    fun getAnalyticsSummary(): Map<String, Any> {
        return try {
            val summary = mutableMapOf<String, Any>()
            
            // Basic counts
            summary["total_events"] = analyticsEvents.size
            summary["total_sessions"] = sessionData.size
            summary["total_features_tracked"] = featureUsage.size
            summary["total_performance_metrics"] = performanceMetrics.size
            
            // Session statistics
            val completedSessions = sessionData.values.filter { it.endTime != null }
            if (completedSessions.isNotEmpty()) {
                val avgSessionDuration = completedSessions.mapNotNull { it.duration }.average()
                val avgEventsPerSession = completedSessions.map { it.eventsCount }.average()
                
                summary["average_session_duration"] = avgSessionDuration
                summary["average_events_per_session"] = avgEventsPerSession
                summary["total_crashes"] = completedSessions.sumOf { it.crashCount }
            }
            
            // Feature usage statistics
            val mostUsedFeatures = featureUsage.values
                .sortedByDescending { it.usageCount }
                .take(5)
                .map { "${it.featureName}: ${it.usageCount} uses" }
            summary["most_used_features"] = mostUsedFeatures
            
            // Event type distribution
            val eventTypeDistribution = analyticsEvents
                .groupBy { it.eventType }
                .mapValues { it.value.size }
            summary["event_type_distribution"] = eventTypeDistribution
            
            // Performance metrics summary
            val performanceSummary = performanceMetrics
                .groupBy { it.metricName }
                .mapValues { metrics ->
                    mapOf(
                        "count" to metrics.value.size,
                        "average" to metrics.value.map { it.value }.average(),
                        "min" to metrics.value.minOfOrNull { it.value },
                        "max" to metrics.value.maxOfOrNull { it.value }
                    )
                }
            summary["performance_summary"] = performanceSummary
            
            summary
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate analytics summary")
            mapOf("error" to "Failed to generate summary")
        }
    }
    
    /**
     * Export analytics data
     */
    suspend fun exportAnalyticsData(): String = withContext(Dispatchers.IO) {
        try {
            val exportData = mapOf(
                "events" to analyticsEvents,
                "sessions" to sessionData.values,
                "feature_usage" to featureUsage.values,
                "performance_metrics" to performanceMetrics,
                "summary" to getAnalyticsSummary(),
                "exported_at" to System.currentTimeMillis(),
                "export_format_version" to "1.0"
            )
            
            // In real implementation, this would serialize to JSON
            val exportString = exportData.toString()
            
            Timber.d("Analytics data exported: ${exportString.length} characters")
            return@withContext exportString
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to export analytics data")
            return@withContext ""
        }
    }
    
    /**
     * Clear analytics data (privacy compliance)
     */
    suspend fun clearAnalyticsData() = withContext(Dispatchers.IO) {
        try {
            analyticsEvents.clear()
            sessionData.clear()
            featureUsage.clear()
            performanceMetrics.clear()
            
            Timber.d("Analytics data cleared")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear analytics data")
        }
    }
    
    /**
     * Enable/disable analytics
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        isAnalyticsEnabled = enabled
        Timber.d("Analytics ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            trackEvent("analytics_enabled")
        }
    }
    
    fun isAnalyticsEnabled(): Boolean = isAnalyticsEnabled
    
    // Private helper methods
    private fun generateEventId(): String {
        return "event_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun collectDeviceInfo(): DeviceInfo {
        val runtime = Runtime.getRuntime()
        
        return DeviceInfo(
            deviceType = "mobile", // Could be "mobile", "tablet", etc.
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            osVersion = android.os.Build.VERSION.RELEASE,
            appVersion = getAppVersion(),
            screenResolution = "unknown", // Would get from DisplayMetrics in real app
            memorySize = runtime.totalMemory(),
            storageSize = 0 // Would get from StorageManager in real app
        )
    }
    
    private fun getAppVersion(): String {
        return "2.5.0-sriracha"
    }
    
    private fun updateSessionEventCount(sessionId: String) {
        val session = sessionData[sessionId]
        if (session != null) {
            sessionData[sessionId] = session.copy(eventsCount = session.eventsCount + 1)
        }
    }
    
    private suspend fun loadPreviousData() = withContext(Dispatchers.IO) {
        try {
            // In real implementation, this would load from persistent storage
            Timber.d("Previous analytics data loaded")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load previous analytics data")
        }
    }
}