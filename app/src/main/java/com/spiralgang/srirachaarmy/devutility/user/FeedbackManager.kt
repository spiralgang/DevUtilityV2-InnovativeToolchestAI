package com.spiralgang.srirachaarmy.devutility.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User feedback management system
 * Part of DevUtility V2.5 user feedback and analytics
 */
@Singleton
class FeedbackManager @Inject constructor() {
    
    private val feedbackQueue = mutableListOf<FeedbackEntry>()
    private val featureRequests = mutableListOf<FeatureRequest>()
    private val bugReports = mutableListOf<BugReport>()
    
    data class FeedbackEntry(
        val id: String,
        val type: FeedbackType,
        val title: String,
        val description: String,
        val rating: Int, // 1-5
        val category: String,
        val context: String,
        val timestamp: Long,
        val userId: String?,
        val deviceInfo: DeviceInfo,
        val appVersion: String,
        val status: FeedbackStatus = FeedbackStatus.PENDING
    )
    
    data class FeatureRequest(
        val id: String,
        val title: String,
        val description: String,
        val priority: Priority,
        val category: String,
        val votes: Int = 0,
        val estimatedEffort: String,
        val status: RequestStatus = RequestStatus.SUBMITTED,
        val submittedAt: Long,
        val userId: String?
    )
    
    data class BugReport(
        val id: String,
        val title: String,
        val description: String,
        val severity: Severity,
        val reproducible: Boolean,
        val stepsToReproduce: List<String>,
        val expectedBehavior: String,
        val actualBehavior: String,
        val stackTrace: String?,
        val deviceInfo: DeviceInfo,
        val appVersion: String,
        val status: BugStatus = BugStatus.OPEN,
        val reportedAt: Long,
        val userId: String?
    )
    
    data class DeviceInfo(
        val manufacturer: String,
        val model: String,
        val androidVersion: String,
        val apiLevel: Int,
        val architecture: String,
        val availableMemory: Long,
        val totalMemory: Long
    )
    
    enum class FeedbackType { GENERAL, BUG_REPORT, FEATURE_REQUEST, PERFORMANCE, UI_UX, DOCUMENTATION }
    enum class FeedbackStatus { PENDING, REVIEWED, IMPLEMENTED, REJECTED }
    enum class Priority { LOW, MEDIUM, HIGH, CRITICAL }
    enum class RequestStatus { SUBMITTED, UNDER_REVIEW, IN_DEVELOPMENT, COMPLETED, REJECTED }
    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
    enum class BugStatus { OPEN, IN_PROGRESS, FIXED, CLOSED, WONT_FIX }
    
    /**
     * Submit general feedback
     */
    suspend fun submitFeedback(
        title: String,
        description: String,
        rating: Int,
        category: String,
        context: String = "",
        userId: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val feedbackId = generateId("feedback")
            val deviceInfo = collectDeviceInfo()
            val appVersion = getAppVersion()
            
            val feedback = FeedbackEntry(
                id = feedbackId,
                type = FeedbackType.GENERAL,
                title = title,
                description = description,
                rating = rating,
                category = category,
                context = context,
                timestamp = System.currentTimeMillis(),
                userId = userId,
                deviceInfo = deviceInfo,
                appVersion = appVersion
            )
            
            feedbackQueue.add(feedback)
            Timber.d("Feedback submitted: $feedbackId - $title")
            
            // Process feedback asynchronously
            processFeedback(feedback)
            
            return@withContext feedbackId
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to submit feedback")
            return@withContext ""
        }
    }
    
    /**
     * Submit feature request
     */
    suspend fun submitFeatureRequest(
        title: String,
        description: String,
        priority: Priority,
        category: String,
        estimatedEffort: String = "Unknown",
        userId: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val requestId = generateId("feature")
            
            val request = FeatureRequest(
                id = requestId,
                title = title,
                description = description,
                priority = priority,
                category = category,
                estimatedEffort = estimatedEffort,
                submittedAt = System.currentTimeMillis(),
                userId = userId
            )
            
            featureRequests.add(request)
            Timber.d("Feature request submitted: $requestId - $title")
            
            return@withContext requestId
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to submit feature request")
            return@withContext ""
        }
    }
    
    /**
     * Submit bug report
     */
    suspend fun submitBugReport(
        title: String,
        description: String,
        severity: Severity,
        stepsToReproduce: List<String>,
        expectedBehavior: String,
        actualBehavior: String,
        stackTrace: String? = null,
        userId: String? = null
    ): String = withContext(Dispatchers.IO) {
        try {
            val bugId = generateId("bug")
            val deviceInfo = collectDeviceInfo()
            val appVersion = getAppVersion()
            
            val bugReport = BugReport(
                id = bugId,
                title = title,
                description = description,
                severity = severity,
                reproducible = stepsToReproduce.isNotEmpty(),
                stepsToReproduce = stepsToReproduce,
                expectedBehavior = expectedBehavior,
                actualBehavior = actualBehavior,
                stackTrace = stackTrace,
                deviceInfo = deviceInfo,
                appVersion = appVersion,
                reportedAt = System.currentTimeMillis(),
                userId = userId
            )
            
            bugReports.add(bugReport)
            Timber.d("Bug report submitted: $bugId - $title")
            
            // Auto-categorize and prioritize
            categorizeBugReport(bugReport)
            
            return@withContext bugId
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to submit bug report")
            return@withContext ""
        }
    }
    
    /**
     * Vote for feature request
     */
    fun voteForFeature(featureId: String, userId: String?): Boolean {
        return try {
            val feature = featureRequests.find { it.id == featureId }
            if (feature != null) {
                val updatedFeature = feature.copy(votes = feature.votes + 1)
                val index = featureRequests.indexOf(feature)
                featureRequests[index] = updatedFeature
                
                Timber.d("Vote added for feature: $featureId (total votes: ${updatedFeature.votes})")
                true
            } else {
                Timber.w("Feature request not found: $featureId")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to vote for feature: $featureId")
            false
        }
    }
    
    /**
     * Get feedback statistics
     */
    fun getFeedbackStatistics(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        stats["total_feedback"] = feedbackQueue.size
        stats["total_feature_requests"] = featureRequests.size
        stats["total_bug_reports"] = bugReports.size
        
        // Feedback ratings distribution
        val ratingDistribution = feedbackQueue.groupBy { it.rating }.mapValues { it.value.size }
        stats["rating_distribution"] = ratingDistribution
        
        // Average rating
        val avgRating = if (feedbackQueue.isNotEmpty()) {
            feedbackQueue.map { it.rating }.average()
        } else 0.0
        stats["average_rating"] = avgRating
        
        // Category distribution
        val categoryDistribution = feedbackQueue.groupBy { it.category }.mapValues { it.value.size }
        stats["category_distribution"] = categoryDistribution
        
        // Bug severity distribution
        val severityDistribution = bugReports.groupBy { it.severity }.mapValues { it.value.size }
        stats["bug_severity_distribution"] = severityDistribution
        
        // Feature priority distribution
        val priorityDistribution = featureRequests.groupBy { it.priority }.mapValues { it.value.size }
        stats["feature_priority_distribution"] = priorityDistribution
        
        // Top voted features
        val topFeatures = featureRequests.sortedByDescending { it.votes }.take(5)
        stats["top_voted_features"] = topFeatures.map { "${it.title}: ${it.votes} votes" }
        
        return stats
    }
    
    /**
     * Get feedback by category
     */
    fun getFeedbackByCategory(category: String): List<FeedbackEntry> {
        return feedbackQueue.filter { it.category == category }
    }
    
    /**
     * Get recent feedback
     */
    fun getRecentFeedback(limit: Int = 10): List<FeedbackEntry> {
        return feedbackQueue.sortedByDescending { it.timestamp }.take(limit)
    }
    
    /**
     * Export feedback data
     */
    suspend fun exportFeedbackData(): String = withContext(Dispatchers.IO) {
        try {
            val exportData = mapOf(
                "feedback" to feedbackQueue,
                "feature_requests" to featureRequests,
                "bug_reports" to bugReports,
                "statistics" to getFeedbackStatistics(),
                "exported_at" to System.currentTimeMillis()
            )
            
            // In a real implementation, this would serialize to JSON or CSV
            val exportString = exportData.toString()
            
            Timber.d("Feedback data exported: ${exportString.length} characters")
            return@withContext exportString
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to export feedback data")
            return@withContext ""
        }
    }
    
    /**
     * Process automatic feedback based on app usage
     */
    suspend fun processAutomaticFeedback(
        action: String,
        context: String,
        performance: Map<String, Any>,
        userId: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            // Analyze performance metrics
            val executionTime = performance["execution_time"] as? Long ?: 0
            val memoryUsage = performance["memory_usage"] as? Long ?: 0
            val errorCount = performance["error_count"] as? Int ?: 0
            
            // Generate automatic feedback based on metrics
            when {
                executionTime > 5000 -> {
                    submitFeedback(
                        title = "Performance Issue Detected",
                        description = "Action '$action' took ${executionTime}ms to complete in context '$context'",
                        rating = 2,
                        category = "performance",
                        context = context,
                        userId = userId
                    )
                }
                errorCount > 0 -> {
                    submitFeedback(
                        title = "Errors Detected",
                        description = "Action '$action' generated $errorCount errors in context '$context'",
                        rating = 2,
                        category = "reliability",
                        context = context,
                        userId = userId
                    )
                }
                executionTime < 1000 && errorCount == 0 -> {
                    submitFeedback(
                        title = "Smooth Operation",
                        description = "Action '$action' completed successfully in ${executionTime}ms",
                        rating = 5,
                        category = "performance",
                        context = context,
                        userId = userId
                    )
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to process automatic feedback")
        }
    }
    
    // Private helper methods
    private fun generateId(prefix: String): String {
        return "${prefix}_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun collectDeviceInfo(): DeviceInfo {
        val runtime = Runtime.getRuntime()
        
        return DeviceInfo(
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            androidVersion = android.os.Build.VERSION.RELEASE,
            apiLevel = android.os.Build.VERSION.SDK_INT,
            architecture = System.getProperty("os.arch") ?: "unknown",
            availableMemory = runtime.freeMemory(),
            totalMemory = runtime.totalMemory()
        )
    }
    
    private fun getAppVersion(): String {
        return "2.5.0-sriracha" // In real app, get from BuildConfig
    }
    
    private suspend fun processFeedback(feedback: FeedbackEntry) = withContext(Dispatchers.IO) {
        try {
            // Automatic categorization and prioritization
            when (feedback.rating) {
                1, 2 -> {
                    // Poor rating - investigate immediately
                    Timber.w("Poor rating received: ${feedback.rating}/5 for '${feedback.title}'")
                }
                4, 5 -> {
                    // Good rating - identify positive patterns
                    Timber.d("Positive feedback: ${feedback.rating}/5 for '${feedback.title}'")
                }
            }
            
            // Update feedback status
            val index = feedbackQueue.indexOf(feedback)
            if (index >= 0) {
                feedbackQueue[index] = feedback.copy(status = FeedbackStatus.REVIEWED)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to process feedback: ${feedback.id}")
        }
    }
    
    private fun categorizeBugReport(bugReport: BugReport) {
        try {
            // Auto-prioritize based on severity and reproducibility
            val priority = when (bugReport.severity) {
                Severity.CRITICAL -> "Immediate attention required"
                Severity.HIGH -> "High priority investigation"
                Severity.MEDIUM -> "Standard investigation"
                Severity.LOW -> "Low priority"
            }
            
            Timber.d("Bug report categorized: ${bugReport.id} - $priority")
            
            // In a real implementation, this might trigger notifications or automated workflows
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to categorize bug report: ${bugReport.id}")
        }
    }
}