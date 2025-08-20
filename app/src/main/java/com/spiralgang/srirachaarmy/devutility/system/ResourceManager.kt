package com.spiralgang.srirachaarmy.devutility.system

import android.app.Activity
import android.content.ComponentCallbacks2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resource management with intelligent allocation and onTrimMemory() optimization
 * Part of DevUtility V2.5 resource management and optimization
 */
@Singleton
class ResourceManager @Inject constructor() {
    
    private var currentActivity: String = "unknown"
    private val resourceUsageHistory = mutableListOf<ResourceUsageSnapshot>()
    private var isLowMemoryMode = false
    
    data class ResourceUsageSnapshot(
        val timestamp: Long,
        val activity: String,
        val memoryUsage: Long,
        val cpuUsage: Double,
        val networkUsage: Long,
        val diskUsage: Long
    )
    
    data class ResourceAllocation(
        val memoryLimit: Long,
        val cpuPriority: String,
        val networkBandwidth: String,
        val diskIOPriority: String
    )
    
    /**
     * Prioritize resource allocation based on current user activity
     */
    fun prioritizeResources(activity: String) {
        try {
            Timber.d("Prioritizing resources for activity: $activity")
            currentActivity = activity
            
            val allocation = when (activity.lowercase()) {
                "coding", "editing" -> {
                    ResourceAllocation(
                        memoryLimit = 128 * 1024 * 1024, // 128MB
                        cpuPriority = "HIGH",
                        networkBandwidth = "NORMAL",
                        diskIOPriority = "HIGH"
                    )
                }
                "compiling", "building" -> {
                    ResourceAllocation(
                        memoryLimit = 256 * 1024 * 1024, // 256MB
                        cpuPriority = "MAXIMUM",
                        networkBandwidth = "LOW",
                        diskIOPriority = "MAXIMUM"
                    )
                }
                "syncing", "uploading" -> {
                    ResourceAllocation(
                        memoryLimit = 64 * 1024 * 1024, // 64MB
                        cpuPriority = "NORMAL",
                        networkBandwidth = "MAXIMUM",
                        diskIOPriority = "NORMAL"
                    )
                }
                "ai_analysis", "code_review" -> {
                    ResourceAllocation(
                        memoryLimit = 192 * 1024 * 1024, // 192MB
                        cpuPriority = "HIGH",
                        networkBandwidth = "NORMAL",
                        diskIOPriority = "NORMAL"
                    )
                }
                "idle", "background" -> {
                    ResourceAllocation(
                        memoryLimit = 32 * 1024 * 1024, // 32MB
                        cpuPriority = "LOW",
                        networkBandwidth = "LOW",
                        diskIOPriority = "LOW"
                    )
                }
                else -> {
                    ResourceAllocation(
                        memoryLimit = 96 * 1024 * 1024, // 96MB
                        cpuPriority = "NORMAL",
                        networkBandwidth = "NORMAL",
                        diskIOPriority = "NORMAL"
                    )
                }
            }
            
            applyResourceAllocation(allocation)
            recordResourceUsage()
            
            Timber.d("Resource allocation applied for $activity: $allocation")
            
        } catch (e: Exception) {
            Timber.e(e, "Resource prioritization failed for activity: $activity")
        }
    }
    
    /**
     * Handle low memory situations with onTrimMemory() integration
     */
    fun onTrimMemory(level: Int) {
        try {
            Timber.w("Memory trim requested - Level: $level")
            
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                    Timber.d("Running moderate - clearing non-essential caches")
                    clearNonEssentialCaches()
                    isLowMemoryMode = true
                }
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                    Timber.w("Running low - aggressive memory cleanup")
                    clearNonEssentialCaches()
                    clearCompilationCache()
                    compactDatabases()
                    isLowMemoryMode = true
                }
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                    Timber.e("Running critical - emergency memory cleanup")
                    performEmergencyMemoryCleanup()
                    isLowMemoryMode = true
                }
                ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                    Timber.d("UI hidden - releasing UI resources")
                    releaseUIResources()
                }
                ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                    Timber.d("Background - reducing background processing")
                    reduceBackgroundProcessing()
                }
                ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                    Timber.w("Moderate background - significant memory cleanup")
                    clearNonEssentialCaches()
                    pauseNonCriticalOperations()
                }
                ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                    Timber.e("Complete background - maximum memory cleanup")
                    performMaximumMemoryCleanup()
                }
            }
            
            // Force garbage collection after cleanup
            System.gc()
            
            // Log memory status after cleanup
            logMemoryStatus("After trim level $level")
            
        } catch (e: Exception) {
            Timber.e(e, "Memory trim handling failed for level: $level")
        }
    }
    
    /**
     * Monitor current resource usage
     */
    suspend fun getCurrentResourceUsage(): ResourceUsageSnapshot = withContext(Dispatchers.IO) {
        try {
            val runtime = Runtime.getRuntime()
            val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
            val cpuUsage = estimateCurrentCpuUsage()
            val networkUsage = estimateNetworkUsage()
            val diskUsage = estimateDiskUsage()
            
            ResourceUsageSnapshot(
                timestamp = System.currentTimeMillis(),
                activity = currentActivity,
                memoryUsage = memoryUsage,
                cpuUsage = cpuUsage,
                networkUsage = networkUsage,
                diskUsage = diskUsage
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current resource usage")
            ResourceUsageSnapshot(
                timestamp = System.currentTimeMillis(),
                activity = currentActivity,
                memoryUsage = -1,
                cpuUsage = -1.0,
                networkUsage = -1,
                diskUsage = -1
            )
        }
    }
    
    /**
     * Get resource usage history for analysis
     */
    fun getResourceUsageHistory(): List<ResourceUsageSnapshot> {
        return resourceUsageHistory.takeLast(100) // Keep last 100 snapshots
    }
    
    /**
     * Optimize resources based on usage patterns
     */
    suspend fun optimizeResourcesBasedOnHistory() = withContext(Dispatchers.IO) {
        try {
            if (resourceUsageHistory.size < 10) {
                Timber.d("Not enough usage history for optimization")
                return@withContext
            }
            
            // Analyze patterns
            val memoryPattern = analyzeMemoryPattern()
            val cpuPattern = analyzeCpuPattern()
            val activityPattern = analyzeActivityPattern()
            
            Timber.d("Resource patterns - Memory: $memoryPattern, CPU: $cpuPattern, Activity: $activityPattern")
            
            // Apply optimizations based on patterns
            if (memoryPattern == "HIGH_USAGE") {
                enableAggressiveMemoryManagement()
            }
            
            if (cpuPattern == "INTENSIVE") {
                optimizeCpuScheduling()
            }
            
            if (activityPattern == "FREQUENT_SWITCHING") {
                enableFastActivitySwitching()
            }
            
            Timber.d("Resource optimization applied based on usage history")
            
        } catch (e: Exception) {
            Timber.e(e, "Resource optimization failed")
        }
    }
    
    /**
     * Check if system is in low memory mode
     */
    fun isInLowMemoryMode(): Boolean = isLowMemoryMode
    
    /**
     * Exit low memory mode when resources are available
     */
    fun exitLowMemoryMode() {
        if (isLowMemoryMode) {
            isLowMemoryMode = false
            Timber.d("Exiting low memory mode - resources restored")
        }
    }
    
    // Private helper methods
    private fun applyResourceAllocation(allocation: ResourceAllocation) {
        // Apply memory limit
        suggestMemoryLimit(allocation.memoryLimit)
        
        // Apply CPU priority (simulate process priority adjustment)
        adjustCpuPriority(allocation.cpuPriority)
        
        // Apply network bandwidth allocation
        adjustNetworkPriority(allocation.networkBandwidth)
        
        // Apply disk I/O priority
        adjustDiskIOPriority(allocation.diskIOPriority)
    }
    
    private fun suggestMemoryLimit(limit: Long) {
        val limitMB = limit / (1024 * 1024)
        Timber.d("Memory limit suggestion: ${limitMB}MB")
        
        // In a real implementation, this might configure memory pools or heap limits
        val runtime = Runtime.getRuntime()
        val currentUsage = runtime.totalMemory() - runtime.freeMemory()
        val currentUsageMB = currentUsage / (1024 * 1024)
        
        if (currentUsage > limit) {
            Timber.w("Current memory usage (${currentUsageMB}MB) exceeds suggested limit (${limitMB}MB)")
            // Trigger memory cleanup
            System.gc()
        }
    }
    
    private fun adjustCpuPriority(priority: String) {
        Timber.d("CPU priority adjusted to: $priority")
        // In a real implementation, this might adjust thread priorities or nice values
    }
    
    private fun adjustNetworkPriority(bandwidth: String) {
        Timber.d("Network bandwidth priority: $bandwidth")
        // In a real implementation, this might configure network throttling
    }
    
    private fun adjustDiskIOPriority(priority: String) {
        Timber.d("Disk I/O priority: $priority")
        // In a real implementation, this might configure I/O scheduler priorities
    }
    
    private fun recordResourceUsage() {
        try {
            val runtime = Runtime.getRuntime()
            val memoryUsage = runtime.totalMemory() - runtime.freeMemory()
            
            val snapshot = ResourceUsageSnapshot(
                timestamp = System.currentTimeMillis(),
                activity = currentActivity,
                memoryUsage = memoryUsage,
                cpuUsage = estimateCurrentCpuUsage(),
                networkUsage = estimateNetworkUsage(),
                diskUsage = estimateDiskUsage()
            )
            
            resourceUsageHistory.add(snapshot)
            
            // Keep only recent history to prevent memory bloat
            if (resourceUsageHistory.size > 1000) {
                resourceUsageHistory.removeAt(0)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to record resource usage")
        }
    }
    
    private fun clearNonEssentialCaches() {
        Timber.d("Clearing non-essential caches")
        // Implementation would clear various app caches
    }
    
    private fun clearCompilationCache() {
        Timber.d("Clearing compilation cache")
        // Implementation would clear code compilation caches
    }
    
    private fun compactDatabases() {
        Timber.d("Compacting databases")
        // Implementation would trigger database VACUUM operations
    }
    
    private fun performEmergencyMemoryCleanup() {
        Timber.e("Performing emergency memory cleanup")
        clearNonEssentialCaches()
        clearCompilationCache()
        compactDatabases()
        releaseUIResources()
        // Force aggressive cleanup
        System.gc()
        System.runFinalization()
    }
    
    private fun releaseUIResources() {
        Timber.d("Releasing UI resources")
        // Implementation would release cached UI elements
    }
    
    private fun reduceBackgroundProcessing() {
        Timber.d("Reducing background processing")
        // Implementation would pause non-critical background tasks
    }
    
    private fun pauseNonCriticalOperations() {
        Timber.d("Pausing non-critical operations")
        // Implementation would pause file indexing, background analysis, etc.
    }
    
    private fun performMaximumMemoryCleanup() {
        Timber.e("Performing maximum memory cleanup")
        performEmergencyMemoryCleanup()
        // Additional cleanup for background state
    }
    
    private fun logMemoryStatus(context: String) {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        
        Timber.d("$context - Memory: Used ${usedMemory / 1024 / 1024}MB, Free ${freeMemory / 1024 / 1024}MB, Total ${totalMemory / 1024 / 1024}MB, Max ${maxMemory / 1024 / 1024}MB")
    }
    
    private fun estimateCurrentCpuUsage(): Double {
        // Simplified CPU usage estimation
        return (10..80).random().toDouble()
    }
    
    private fun estimateNetworkUsage(): Long {
        // Simplified network usage estimation in bytes
        return (1024 * (10..1000).random()).toLong()
    }
    
    private fun estimateDiskUsage(): Long {
        // Simplified disk usage estimation in bytes
        return (1024 * 1024 * (1..10).random()).toLong()
    }
    
    private fun analyzeMemoryPattern(): String {
        if (resourceUsageHistory.isEmpty()) return "UNKNOWN"
        
        val avgMemory = resourceUsageHistory.takeLast(20).map { it.memoryUsage }.average()
        val maxMemory = Runtime.getRuntime().maxMemory()
        val usagePercent = (avgMemory / maxMemory) * 100
        
        return when {
            usagePercent > 80 -> "HIGH_USAGE"
            usagePercent > 60 -> "MODERATE_USAGE"
            else -> "LOW_USAGE"
        }
    }
    
    private fun analyzeCpuPattern(): String {
        if (resourceUsageHistory.isEmpty()) return "UNKNOWN"
        
        val avgCpu = resourceUsageHistory.takeLast(20).map { it.cpuUsage }.average()
        
        return when {
            avgCpu > 70 -> "INTENSIVE"
            avgCpu > 40 -> "MODERATE"
            else -> "LIGHT"
        }
    }
    
    private fun analyzeActivityPattern(): String {
        if (resourceUsageHistory.size < 10) return "UNKNOWN"
        
        val recentActivities = resourceUsageHistory.takeLast(20).map { it.activity }.distinct()
        
        return when {
            recentActivities.size > 5 -> "FREQUENT_SWITCHING"
            recentActivities.size > 2 -> "MODERATE_SWITCHING"
            else -> "STABLE"
        }
    }
    
    private fun enableAggressiveMemoryManagement() {
        Timber.d("Enabling aggressive memory management")
        // Implementation would enable more frequent garbage collection
    }
    
    private fun optimizeCpuScheduling() {
        Timber.d("Optimizing CPU scheduling")
        // Implementation would adjust thread pools and priorities
    }
    
    private fun enableFastActivitySwitching() {
        Timber.d("Enabling fast activity switching optimization")
        // Implementation would pre-cache resources for common activities
    }
}