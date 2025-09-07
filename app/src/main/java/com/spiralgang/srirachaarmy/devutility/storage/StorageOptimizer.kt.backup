package com.spiralgang.srirachaarmy.devutility.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Storage optimizer with ZRAM integration and compression
 * Part of DevUtility V2.5 storage optimization system
 */
@Singleton
class StorageOptimizer @Inject constructor(
    private val customCompressor: CustomCompressor,
    private val zramManager: ZRAMManager
) {
    
    private var compressionEnabled = true
    private var zramEnabled = false
    private val optimizationStats = OptimizationStats()
    
    data class OptimizationResult(
        val originalSize: Long,
        val optimizedSize: Long,
        val compressionRatio: Float,
        val timeTaken: Long,
        val method: String
    )
    
    data class OptimizationStats(
        var totalFilesOptimized: Long = 0,
        var totalBytesOriginal: Long = 0,
        var totalBytesOptimized: Long = 0,
        var totalTimeSaved: Long = 0,
        var averageCompressionRatio: Float = 0.0f
    )
    
    /**
     * Initialize storage optimization system
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Initializing storage optimization system")
            
            // Initialize ZRAM manager
            zramManager.initialize()
            
            // Check if ZRAM is supported and enable if possible
            if (zramManager.isZRAMSupported()) {
                zramEnabled = zramManager.enableZRAM()
                Timber.d("ZRAM enabled: $zramEnabled")
            } else {
                Timber.d("ZRAM not supported, using standard compression")
            }
            
            Timber.d("Storage optimization system initialized")
            
        } catch (e: Exception) {
            Timber.e(e, "Storage optimization initialization failed")
        }
    }
    
    /**
     * Optimize storage for file data
     */
    suspend fun optimizeStorage(
        fileData: ByteArray, 
        useZRAM: Boolean = zramEnabled,
        fileName: String = "unknown"
    ): OptimizationResult = withContext(Dispatchers.IO) {
        
        val startTime = System.currentTimeMillis()
        val originalSize = fileData.size.toLong()
        
        try {
            Timber.d("Optimizing storage for $fileName (${originalSize} bytes) - ZRAM: $useZRAM")
            
            val optimizedData = when {
                useZRAM && zramEnabled -> {
                    zramManager.compressWithZRAM(fileData)
                }
                compressionEnabled -> {
                    customCompressor.compress(fileData)
                }
                else -> {
                    fileData // No optimization
                }
            }
            
            val optimizedSize = optimizedData.size.toLong()
            val timeTaken = System.currentTimeMillis() - startTime
            val compressionRatio = if (optimizedSize > 0) originalSize.toFloat() / optimizedSize.toFloat() else 1.0f
            
            val method = when {
                useZRAM && zramEnabled -> "ZRAM"
                compressionEnabled -> "DEFLATE"
                else -> "NONE"
            }
            
            // Update statistics
            updateOptimizationStats(originalSize, optimizedSize, timeTaken, compressionRatio)
            
            val result = OptimizationResult(
                originalSize = originalSize,
                optimizedSize = optimizedSize,
                compressionRatio = compressionRatio,
                timeTaken = timeTaken,
                method = method
            )
            
            Timber.d("Optimization completed - Ratio: ${String.format("%.2f", compressionRatio)}, Method: $method, Time: ${timeTaken}ms")
            
            return@withContext result
            
        } catch (e: Exception) {
            Timber.e(e, "Storage optimization failed for $fileName")
            
            return@withContext OptimizationResult(
                originalSize = originalSize,
                optimizedSize = originalSize,
                compressionRatio = 1.0f,
                timeTaken = System.currentTimeMillis() - startTime,
                method = "FAILED"
            )
        }
    }
    
    /**
     * Restore optimized data
     */
    suspend fun restoreOptimizedData(
        compressedData: ByteArray,
        useZRAM: Boolean = zramEnabled,
        fileName: String = "unknown"
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            Timber.d("Restoring optimized data for $fileName - ZRAM: $useZRAM")
            
            val restoredData = when {
                useZRAM && zramEnabled -> {
                    zramManager.decompressFromZRAM(compressedData)
                }
                compressionEnabled -> {
                    customCompressor.decompress(compressedData)
                }
                else -> {
                    compressedData // No decompression needed
                }
            }
            
            Timber.d("Data restoration completed for $fileName")
            return@withContext restoredData
            
        } catch (e: Exception) {
            Timber.e(e, "Data restoration failed for $fileName")
            return compressedData // Return original on failure
        }
    }
    
    /**
     * Enable or disable compression
     */
    fun setCompressionEnabled(enabled: Boolean) {
        compressionEnabled = enabled
        Timber.d("Compression ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Enable or disable ZRAM usage
     */
    suspend fun setZramEnabled(enabled: Boolean) {
        if (enabled && zramManager.isZRAMSupported()) {
            zramEnabled = zramManager.enableZRAM()
            Timber.d("ZRAM enablement attempted: $zramEnabled")
        } else if (!enabled && zramEnabled) {
            val disabled = zramManager.disableZRAM()
            if (disabled) {
                zramEnabled = false
                Timber.d("ZRAM disabled")
            }
        } else if (enabled && !zramManager.isZRAMSupported()) {
            Timber.w("ZRAM not supported on this device")
            zramEnabled = false
        }
    }
    
    /**
     * Get current optimization statistics
     */
    fun getOptimizationStats(): OptimizationStats {
        return optimizationStats.copy()
    }
    
    /**
     * Get ZRAM statistics if available
     */
    suspend fun getZRAMStats(): ZRAMManager.ZRAMStats? = withContext(Dispatchers.IO) {
        return@withContext if (zramEnabled) {
            zramManager.getZRAMStats()
        } else {
            null
        }
    }
    
    /**
     * Optimize multiple files in batch
     */
    suspend fun batchOptimize(
        files: List<Pair<String, ByteArray>>,
        useZRAM: Boolean = zramEnabled
    ): List<Pair<String, OptimizationResult>> = withContext(Dispatchers.IO) {
        
        val results = mutableListOf<Pair<String, OptimizationResult>>()
        
        try {
            Timber.d("Starting batch optimization of ${files.size} files")
            
            for ((fileName, fileData) in files) {
                val result = optimizeStorage(fileData, useZRAM, fileName)
                results.add(fileName to result)
                
                // Small delay to prevent overwhelming the system
                kotlinx.coroutines.delay(10)
            }
            
            val totalOriginal = results.sumOf { it.second.originalSize }
            val totalOptimized = results.sumOf { it.second.optimizedSize }
            val overallRatio = if (totalOptimized > 0) totalOriginal.toFloat() / totalOptimized.toFloat() else 1.0f
            
            Timber.d("Batch optimization completed - Overall ratio: ${String.format("%.2f", overallRatio)}")
            
        } catch (e: Exception) {
            Timber.e(e, "Batch optimization failed")
        }
        
        return@withContext results
    }
    
    /**
     * Analyze file for optimization potential
     */
    suspend fun analyzeOptimizationPotential(
        fileData: ByteArray,
        fileName: String = "unknown"
    ): OptimizationAnalysis = withContext(Dispatchers.IO) {
        try {
            val fileSize = fileData.size.toLong()
            val fileType = detectFileType(fileName, fileData)
            
            // Test compression to estimate potential
            val testCompressed = customCompressor.compress(fileData)
            val estimatedRatio = fileSize.toFloat() / testCompressed.size.toFloat()
            
            val recommendation = when {
                estimatedRatio >= 3.0 -> "HIGHLY_RECOMMENDED"
                estimatedRatio >= 2.0 -> "RECOMMENDED"
                estimatedRatio >= 1.5 -> "MODERATE_BENEFIT"
                else -> "MINIMAL_BENEFIT"
            }
            
            OptimizationAnalysis(
                fileName = fileName,
                fileSize = fileSize,
                fileType = fileType,
                estimatedCompressionRatio = estimatedRatio,
                recommendation = recommendation,
                estimatedSavings = fileSize - testCompressed.size,
                analysisTime = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Optimization analysis failed for $fileName")
            OptimizationAnalysis(
                fileName = fileName,
                fileSize = fileData.size.toLong(),
                fileType = "UNKNOWN",
                estimatedCompressionRatio = 1.0f,
                recommendation = "ANALYSIS_FAILED",
                estimatedSavings = 0,
                analysisTime = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Clean up optimization resources
     */
    suspend fun cleanup() = withContext(Dispatchers.IO) {
        try {
            Timber.d("Cleaning up storage optimization resources")
            
            if (zramEnabled) {
                zramManager.disableZRAM()
            }
            
            // Reset statistics
            optimizationStats.apply {
                totalFilesOptimized = 0
                totalBytesOriginal = 0
                totalBytesOptimized = 0
                totalTimeSaved = 0
                averageCompressionRatio = 0.0f
            }
            
            Timber.d("Storage optimization cleanup completed")
            
        } catch (e: Exception) {
            Timber.e(e, "Storage optimization cleanup failed")
        }
    }
    
    // Private helper methods
    private fun updateOptimizationStats(
        originalSize: Long,
        optimizedSize: Long,
        timeTaken: Long,
        compressionRatio: Float
    ) {
        optimizationStats.apply {
            totalFilesOptimized++
            totalBytesOriginal += originalSize
            totalBytesOptimized += optimizedSize
            totalTimeSaved += timeTaken
            
            // Update average compression ratio
            averageCompressionRatio = if (totalFilesOptimized > 0) {
                (averageCompressionRatio * (totalFilesOptimized - 1) + compressionRatio) / totalFilesOptimized
            } else {
                compressionRatio
            }
        }
    }
    
    private fun detectFileType(fileName: String, fileData: ByteArray): String {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        
        return when (extension) {
            "kt", "java", "py", "js", "ts", "cpp", "c", "h" -> "SOURCE_CODE"
            "txt", "md", "log" -> "TEXT"
            "json", "xml", "yaml", "yml" -> "DATA_FORMAT"
            "png", "jpg", "jpeg", "gif", "bmp" -> "IMAGE"
            "mp4", "avi", "mkv", "mov" -> "VIDEO"
            "mp3", "wav", "ogg", "flac" -> "AUDIO"
            "zip", "rar", "7z", "tar", "gz" -> "ARCHIVE"
            "pdf", "doc", "docx", "ppt", "pptx" -> "DOCUMENT"
            else -> {
                // Try to detect by content
                when {
                    fileData.size >= 4 && fileData.sliceArray(0..3).contentEquals(byteArrayOf(0x89, 0x50, 0x4E, 0x47)) -> "PNG_IMAGE"
                    fileData.size >= 3 && fileData.sliceArray(0..2).contentEquals(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) -> "JPEG_IMAGE"
                    fileName.contains("gradle") || fileName.contains("build") -> "BUILD_FILE"
                    else -> "UNKNOWN"
                }
            }
        }
    }
    
    data class OptimizationAnalysis(
        val fileName: String,
        val fileSize: Long,
        val fileType: String,
        val estimatedCompressionRatio: Float,
        val recommendation: String,
        val estimatedSavings: Long,
        val analysisTime: Long
    )
}