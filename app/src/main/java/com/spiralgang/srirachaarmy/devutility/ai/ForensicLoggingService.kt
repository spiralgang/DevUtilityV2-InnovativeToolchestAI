package com.spiralgang.srirachaarmy.devutility.ai

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Forensic Logging Integration Service
 * 
 * Integrates Android app with scripts/forensic-log.sh for comprehensive logging.
 * Provides audit trail, security monitoring, and debugging capabilities.
 * 
 * Features:
 * - Integration with forensic-log.sh script
 * - Android 10+ compatibility
 * - Local file-based logging
 * - JSON-structured log entries
 * - Security event tracking
 * - Performance monitoring
 */
@Singleton
class ForensicLoggingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "ForensicLoggingService"
        private const val LOG_DIR = "logs"
        private const val ACTIVATION_LOG = "activation.jsonl"
        private const val AI_INTERACTIONS_LOG = "ai_interactions.jsonl" 
        private const val SECURITY_LOG = "security_events.jsonl"
        private const val PERFORMANCE_LOG = "performance_metrics.jsonl"
        private const val MAX_LOG_SIZE = 10 * 1024 * 1024 // 10MB
        private const val MAX_LOG_FILES = 5
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private var isInitialized = false
    private lateinit var logDirectory: File
    
    /**
     * Initialize forensic logging system
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Timber.tag(TAG).i("Initializing Forensic Logging Service...")
            
            // Setup log directory
            setupLogDirectory()
            
            // Initialize log files
            initializeLogFiles()
            
            // Log initialization
            logEvent(LogType.ACTIVATION, "forensic_logging_init", "start", mapOf(
                "android_version" to android.os.Build.VERSION.SDK_INT,
                "app_version" to getAppVersion(),
                "device_model" to android.os.Build.MODEL,
                "timestamp" to getCurrentTimestamp()
            ))
            
            isInitialized = true
            Timber.tag(TAG).i("Forensic Logging Service initialized successfully")
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initialize Forensic Logging Service")
            false
        }
    }
    
    /**
     * Log AI interaction event
     */
    suspend fun logAIInteraction(
        agent: String,
        task: String,
        response: String,
        success: Boolean,
        duration: Long = 0
    ) = withContext(Dispatchers.IO) {
        logEvent(LogType.AI_INTERACTIONS, "ai_interaction", "info", mapOf(
            "agent" to agent,
            "task_length" to task.length,
            "response_length" to response.length,
            "success" to success,
            "duration_ms" to duration,
            "task_hash" to task.hashCode(),
            "local_only" to true,
            "no_copilot" to true
        ))
    }
    
    /**
     * Log security event
     */
    suspend fun logSecurityEvent(
        eventType: String,
        severity: SecuritySeverity,
        details: Map<String, Any?> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        logEvent(LogType.SECURITY, eventType, severity.level, mapOf(
            "severity" to severity.name,
            "event_type" to eventType,
            "android_security_patch" to android.os.Build.VERSION.SECURITY_PATCH
        ) + details)
    }
    
    /**
     * Log performance metrics
     */
    suspend fun logPerformanceMetrics(
        operation: String,
        duration: Long,
        memoryUsage: Long = 0,
        cpuUsage: Double = 0.0,
        additionalMetrics: Map<String, Any?> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        logEvent(LogType.PERFORMANCE, "performance_metric", "info", mapOf(
            "operation" to operation,
            "duration_ms" to duration,
            "memory_usage_bytes" to memoryUsage,
            "cpu_usage_percent" to cpuUsage,
            "available_memory" to Runtime.getRuntime().freeMemory(),
            "total_memory" to Runtime.getRuntime().totalMemory()
        ) + additionalMetrics)
    }
    
    /**
     * Log activation event (compatible with forensic-log.sh format)
     */
    suspend fun logActivationEvent(
        phase: String,
        actor: String = "android_app",
        event: String = "ai_operation",
        additionalData: Map<String, Any?> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        logEvent(LogType.ACTIVATION, phase, "info", mapOf(
            "phase" to phase,
            "actor" to actor,
            "event" to event,
            "ref" to "android_local",
            "run_id" to generateRunId()
        ) + additionalData)
    }
    
    /**
     * Start a logged operation (forensic-log.sh start equivalent)
     */
    suspend fun startOperation(
        operationName: String,
        actor: String = "android_app",
        event: String = "ai_operation"
    ): String = withContext(Dispatchers.IO) {
        val operationId = generateOperationId()
        
        logActivationEvent("start", actor, event, mapOf(
            "operation_id" to operationId,
            "operation_name" to operationName
        ))
        
        operationId
    }
    
    /**
     * Log a step in an operation (forensic-log.sh step equivalent)
     */
    suspend fun logOperationStep(
        operationId: String,
        stepName: String,
        message: String = ""
    ) = withContext(Dispatchers.IO) {
        logActivationEvent("step", additionalData = mapOf(
            "operation_id" to operationId,
            "name" to stepName,
            "msg" to message
        ))
    }
    
    /**
     * End a logged operation (forensic-log.sh end equivalent)
     */
    suspend fun endOperation(
        operationId: String,
        success: Boolean = true,
        error: String? = null
    ) = withContext(Dispatchers.IO) {
        logActivationEvent("end", additionalData = mapOf(
            "operation_id" to operationId,
            "success" to success,
            "error" to error
        ))
    }
    
    /**
     * Get log statistics
     */
    suspend fun getLogStatistics(): LogStatistics = withContext(Dispatchers.IO) {
        try {
            val stats = LogStatistics(
                activationLogSize = getLogFileSize(ACTIVATION_LOG),
                aiInteractionsLogSize = getLogFileSize(AI_INTERACTIONS_LOG),
                securityLogSize = getLogFileSize(SECURITY_LOG),
                performanceLogSize = getLogFileSize(PERFORMANCE_LOG),
                totalLogSize = getTotalLogSize(),
                isInitialized = isInitialized,
                lastLogTime = getLastLogTime()
            )
            stats
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to get log statistics")
            LogStatistics.empty()
        }
    }
    
    /**
     * Cleanup old log files
     */
    suspend fun cleanupOldLogs() = withContext(Dispatchers.IO) {
        try {
            if (!::logDirectory.isInitialized) return@withContext
            
            val logFiles = logDirectory.listFiles { file -> file.name.endsWith(".jsonl") }
                ?.sortedBy { it.lastModified() } ?: return@withContext
            
            // Keep only the most recent files if we exceed the limit
            if (logFiles.size > MAX_LOG_FILES) {
                val filesToDelete = logFiles.dropLast(MAX_LOG_FILES)
                for (file in filesToDelete) {
                    if (file.delete()) {
                        Timber.tag(TAG).d("Deleted old log file: ${file.name}")
                    }
                }
            }
            
            // Rotate large log files
            for (file in logFiles) {
                if (file.length() > MAX_LOG_SIZE) {
                    rotateLogFile(file)
                }
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to cleanup old logs")
        }
    }
    
    private fun setupLogDirectory() {
        logDirectory = File(context.getExternalFilesDir(null), LOG_DIR)
        if (!logDirectory.exists()) {
            logDirectory.mkdirs()
        }
    }
    
    private fun initializeLogFiles() {
        val logFiles = listOf(ACTIVATION_LOG, AI_INTERACTIONS_LOG, SECURITY_LOG, PERFORMANCE_LOG)
        for (logFile in logFiles) {
            val file = File(logDirectory, logFile)
            if (!file.exists()) {
                file.createNewFile()
            }
        }
    }
    
    private suspend fun logEvent(
        logType: LogType,
        event: String,
        level: String,
        data: Map<String, Any?>
    ) {
        if (!isInitialized) return
        
        try {
            val logEntry = JSONObject().apply {
                put("ts", getCurrentTimestamp())
                put("event", event)
                put("level", level)
                put("source", "android_app")
                put("log_type", logType.name.lowercase())
                
                for ((key, value) in data) {
                    put(key, value)
                }
            }
            
            val logFile = File(logDirectory, getLogFileName(logType))
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry.toString())
                writer.append("\n")
            }
            
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to write log entry")
        }
    }
    
    private fun getLogFileName(logType: LogType): String {
        return when (logType) {
            LogType.ACTIVATION -> ACTIVATION_LOG
            LogType.AI_INTERACTIONS -> AI_INTERACTIONS_LOG
            LogType.SECURITY -> SECURITY_LOG
            LogType.PERFORMANCE -> PERFORMANCE_LOG
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun generateRunId(): String {
        return "android_${System.currentTimeMillis()}_${android.os.Process.myPid()}"
    }
    
    private fun generateOperationId(): String {
        return "op_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"
    }
    
    private fun getLogFileSize(fileName: String): Long {
        return try {
            File(logDirectory, fileName).length()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getTotalLogSize(): Long {
        return try {
            logDirectory.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getLastLogTime(): Long {
        return try {
            logDirectory.listFiles()?.maxOfOrNull { it.lastModified() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun rotateLogFile(file: File) {
        try {
            val backupFile = File(file.parentFile, "${file.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}")
            file.renameTo(backupFile)
            file.createNewFile()
            
            Timber.tag(TAG).d("Rotated log file: ${file.name} -> ${backupFile.name}")
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to rotate log file: ${file.name}")
        }
    }
}

/**
 * Log types for different categories of events
 */
enum class LogType {
    ACTIVATION,
    AI_INTERACTIONS, 
    SECURITY,
    PERFORMANCE
}

/**
 * Security severity levels
 */
enum class SecuritySeverity(val level: String) {
    LOW("info"),
    MEDIUM("warning"), 
    HIGH("error"),
    CRITICAL("critical")
}

/**
 * Log statistics data class
 */
data class LogStatistics(
    val activationLogSize: Long,
    val aiInteractionsLogSize: Long,
    val securityLogSize: Long,
    val performanceLogSize: Long,
    val totalLogSize: Long,
    val isInitialized: Boolean,
    val lastLogTime: Long
) {
    companion object {
        fun empty(): LogStatistics {
            return LogStatistics(0, 0, 0, 0, 0, false, 0)
        }
    }
}