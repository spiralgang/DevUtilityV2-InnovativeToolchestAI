package com.spiralgang.srirachaarmy.devutility.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * AIInstanceManager - Enforces single AI operation constraint
 * 
 * This manager ensures only ONE AI/Copilot operation runs at a time, 
 * addressing the user's requirement to "make the goddamn copilot limited to one at a time maximum"
 */
object AIInstanceManager {
    
    private val mutex = Mutex()
    private val isAIOperationActive = AtomicBoolean(false)
    private val currentActiveOperation = AtomicReference<String?>(null)
    
    enum class AIOperationType {
        THINK_MODULE,
        DEEPSEEK_ENGINE,
        WEB_NET_CASTE,
        LEARNING_BOT,
        SRIRACHA_GUIDANCE,
        CODE_REVIEW,
        COORDINATION,
        FLOAT_WINDOW
    }
    
    /**
     * Execute AI operation with single-instance constraint
     * Only allows one AI operation at a time across the entire system
     */
    suspend fun <T> executeAIOperation(
        operationType: AIOperationType,
        operationName: String,
        operation: suspend () -> T
    ): T {
        return mutex.withLock {
            if (isAIOperationActive.get()) {
                val activeOp = currentActiveOperation.get()
                Timber.w("⚠️ AI Operation blocked: $operationName ($operationType). Currently active: $activeOp")
                throw AIOperationBlockedException("AI operation already in progress: $activeOp. Please wait.")
            }
            
            try {
                isAIOperationActive.set(true)
                currentActiveOperation.set("$operationType: $operationName")
                
                Timber.d("🤖 Starting AI operation: $operationName ($operationType)")
                val result = operation()
                Timber.d("✅ Completed AI operation: $operationName ($operationType)")
                
                result
            } finally {
                isAIOperationActive.set(false)
                currentActiveOperation.set(null)
            }
        }
    }
    
    /**
     * Check if any AI operation is currently active
     */
    fun isAIOperationActive(): Boolean = isAIOperationActive.get()
    
    /**
     * Get currently active operation details
     */
    fun getCurrentActiveOperation(): String? = currentActiveOperation.get()
    
    /**
     * Force release AI lock (emergency use only)
     */
    fun forceReleaseAILock() {
        Timber.w("🚨 Force releasing AI lock - this should only be used in emergency situations")
        isAIOperationActive.set(false)
        currentActiveOperation.set(null)
    }
}

/**
 * Exception thrown when an AI operation is blocked due to another operation in progress
 */
class AIOperationBlockedException(message: String) : Exception(message)