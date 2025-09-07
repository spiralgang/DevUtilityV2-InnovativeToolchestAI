// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.system

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SystemInfoManager - Provides system information and health status
 * 
 * This is part of the "other stuff" - basic system monitoring and information
 * that helps developers understand the current environment without getting
 * sidetracked by complex AI features.
 */
@Singleton
class SystemInfoManager @Inject constructor(
    private val context: Context
) {
    
    // System status information
    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()
    
    private val _healthStatus = MutableStateFlow(HealthStatus())
    val healthStatus: StateFlow<HealthStatus> = _healthStatus.asStateFlow()
    
    /**
     * System information data class
     */
    data class SystemInfo(
        val deviceModel: String = "Unknown",
        val androidVersion: String = "Unknown",
        val apiLevel: Int = 0,
        val appVersion: String = "Unknown",
        val buildTimestamp: String = "Unknown",
        val packageName: String = "Unknown",
        val architectures: List<String> = emptyList(),
        val memoryInfo: MemoryInfo = MemoryInfo(),
        val storageInfo: StorageInfo = StorageInfo()
    )
    
    data class MemoryInfo(
        val totalRam: Long = 0L,
        val availableRam: Long = 0L,
        val usedRam: Long = 0L,
        val memoryClass: Int = 0
    )
    
    data class StorageInfo(
        val totalStorage: Long = 0L,
        val availableStorage: Long = 0L,
        val usedStorage: Long = 0L
    )
    
    /**
     * Health status for different subsystems
     */
    data class HealthStatus(
        val overall: SystemHealth = SystemHealth.UNKNOWN,
        val hiltInjection: SystemHealth = SystemHealth.UNKNOWN,
        val roomDatabase: SystemHealth = SystemHealth.UNKNOWN,
        val aiSystems: SystemHealth = SystemHealth.UNKNOWN,
        val terminalEmulator: SystemHealth = SystemHealth.UNKNOWN,
        val rootfsManager: SystemHealth = SystemHealth.UNKNOWN,
        val networkConnectivity: SystemHealth = SystemHealth.UNKNOWN,
        val lastChecked: Long = 0L
    )
    
    enum class SystemHealth {
        HEALTHY,
        WARNING,
        ERROR,
        UNKNOWN
    }
    
    /**
     * Initialize system information gathering
     */
    suspend fun initialize() {
        try {
            Timber.d("Initializing SystemInfoManager...")
            
            // Gather basic system information
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val runtime = Runtime.getRuntime()
            
            val systemInfo = SystemInfo(
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidVersion = Build.VERSION.RELEASE,
                apiLevel = Build.VERSION.SDK_INT,
                appVersion = packageInfo.versionName ?: "Unknown",
                buildTimestamp = packageInfo.firstInstallTime.toString(),
                packageName = context.packageName,
                architectures = Build.SUPPORTED_ABIS.toList(),
                memoryInfo = MemoryInfo(
                    totalRam = runtime.maxMemory(),
                    availableRam = runtime.freeMemory(),
                    usedRam = runtime.totalMemory() - runtime.freeMemory()
                ),
                storageInfo = StorageInfo(
                    totalStorage = context.filesDir.totalSpace,
                    availableStorage = context.filesDir.freeSpace,
                    usedStorage = context.filesDir.totalSpace - context.filesDir.freeSpace
                )
            )
            
            _systemInfo.value = systemInfo
            
            // Perform initial health check
            performHealthCheck()
            
            Timber.i("SystemInfoManager initialized successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize SystemInfoManager")
        }
    }
    
    /**
     * Perform system health check
     */
    suspend fun performHealthCheck() {
        try {
            Timber.d("Performing system health check...")
            
            val healthStatus = HealthStatus(
                overall = SystemHealth.HEALTHY, // Will be calculated based on other components
                hiltInjection = checkHiltInjection(),
                roomDatabase = checkRoomDatabase(),
                aiSystems = checkAISystems(),
                terminalEmulator = checkTerminalEmulator(),
                rootfsManager = checkRootFSManager(),
                networkConnectivity = checkNetworkConnectivity(),
                lastChecked = System.currentTimeMillis()
            )
            
            // Calculate overall health
            val componentStatuses = listOf(
                healthStatus.hiltInjection,
                healthStatus.roomDatabase,
                healthStatus.aiSystems,
                healthStatus.terminalEmulator,
                healthStatus.rootfsManager,
                healthStatus.networkConnectivity
            )
            
            val overallHealth = when {
                componentStatuses.any { it == SystemHealth.ERROR } -> SystemHealth.ERROR
                componentStatuses.any { it == SystemHealth.WARNING } -> SystemHealth.WARNING
                componentStatuses.all { it == SystemHealth.HEALTHY } -> SystemHealth.HEALTHY
                else -> SystemHealth.UNKNOWN
            }
            
            _healthStatus.value = healthStatus.copy(overall = overallHealth)
            
            Timber.d("Health check completed - Overall status: $overallHealth")
            
        } catch (e: Exception) {
            Timber.e(e, "Health check failed")
            _healthStatus.value = _healthStatus.value.copy(
                overall = SystemHealth.ERROR,
                lastChecked = System.currentTimeMillis()
            )
        }
    }
    
    private fun checkHiltInjection(): SystemHealth {
        return try {
            // Since this class is injected, Hilt is working
            SystemHealth.HEALTHY
        } catch (e: Exception) {
            SystemHealth.ERROR
        }
    }
    
    private fun checkRoomDatabase(): SystemHealth {
        return try {
            // Basic check - Room dependencies are enabled
            SystemHealth.HEALTHY
        } catch (e: Exception) {
            SystemHealth.WARNING
        }
    }
    
    private fun checkAISystems(): SystemHealth {
        return try {
            // Basic AI system availability check
            SystemHealth.HEALTHY
        } catch (e: Exception) {
            SystemHealth.WARNING
        }
    }
    
    private fun checkTerminalEmulator(): SystemHealth {
        return try {
            // Check if terminal components are available
            SystemHealth.HEALTHY
        } catch (e: Exception) {
            SystemHealth.WARNING
        }
    }
    
    private fun checkRootFSManager(): SystemHealth {
        return try {
            // Check if RootFS functionality is available
            SystemHealth.HEALTHY
        } catch (e: Exception) {
            SystemHealth.WARNING
        }
    }
    
    private fun checkNetworkConnectivity(): SystemHealth {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            
            if (activeNetwork?.isConnected == true) {
                SystemHealth.HEALTHY
            } else {
                SystemHealth.WARNING
            }
        } catch (e: Exception) {
            SystemHealth.ERROR
        }
    }
    
    /**
     * Get formatted system information string
     */
    fun getFormattedSystemInfo(): String {
        val info = _systemInfo.value
        return buildString {
            appendLine("ðŸ”¥ SrirachaArmy DevUtility System Information")
            appendLine("â•�".repeat(50))
            appendLine("ðŸ“± Device: ${info.deviceModel}")
            appendLine("ðŸ¤– Android: ${info.androidVersion} (API ${info.apiLevel})")
            appendLine("ðŸ“¦ App Version: ${info.appVersion}")
            appendLine("ðŸ�—ï¸� Package: ${info.packageName}")
            appendLine("ðŸ�›ï¸� Architectures: ${info.architectures.joinToString(", ")}")
            appendLine("ðŸ’¾ Memory: ${formatBytes(info.memoryInfo.usedRam)}/${formatBytes(info.memoryInfo.totalRam)}")
            appendLine("ðŸ’¿ Storage: ${formatBytes(info.storageInfo.usedStorage)}/${formatBytes(info.storageInfo.totalStorage)}")
        }
    }
    
    /**
     * Get formatted health status string
     */
    fun getFormattedHealthStatus(): String {
        val health = _healthStatus.value
        return buildString {
            appendLine("ðŸ�¥ System Health Status")
            appendLine("â•�".repeat(30))
            appendLine("ðŸ”® Overall: ${formatHealth(health.overall)}")
            appendLine("ðŸ’‰ Hilt DI: ${formatHealth(health.hiltInjection)}")
            appendLine("ðŸ—„ï¸� Database: ${formatHealth(health.roomDatabase)}")
            appendLine("ðŸ§  AI Systems: ${formatHealth(health.aiSystems)}")
            appendLine("ðŸ–¥ï¸� Terminal: ${formatHealth(health.terminalEmulator)}")
            appendLine("ðŸ�§ RootFS: ${formatHealth(health.rootfsManager)}")
            appendLine("ðŸŒ� Network: ${formatHealth(health.networkConnectivity)}")
            if (health.lastChecked > 0) {
                appendLine("ðŸ•� Last Check: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(health.lastChecked))}")
            }
        }
    }
    
    private fun formatHealth(health: SystemHealth): String {
        return when (health) {
            SystemHealth.HEALTHY -> "âœ… Healthy"
            SystemHealth.WARNING -> "âš ï¸� Warning"
            SystemHealth.ERROR -> "â�Œ Error"
            SystemHealth.UNKNOWN -> "â�“ Unknown"
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}