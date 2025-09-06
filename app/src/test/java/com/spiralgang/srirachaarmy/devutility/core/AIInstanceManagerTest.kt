package com.spiralgang.srirachaarmy.devutility.core

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Test AI Instance Manager constraint enforcement
 * Validates that only one AI operation can run at a time
 */
class AIInstanceManagerTest {

    @Test
    fun `test single AI operation constraint`() = runBlocking {
        // Reset state
        AIInstanceManager.forceReleaseAILock()
        
        // Test that initially no operation is active
        assertFalse(AIInstanceManager.isAIOperationActive())
        assertNull(AIInstanceManager.getCurrentActiveOperation())
        
        var firstOperationCompleted = false
        var secondOperationBlocked = false
        
        coroutineScope {
            // Start first AI operation
            val firstOperation = async {
                AIInstanceManager.executeAIOperation(
                    AIInstanceManager.AIOperationType.THINK_MODULE,
                    "First operation"
                ) {
                    delay(1000) // Simulate AI work
                    firstOperationCompleted = true
                    "First result"
                }
            }
            
            // Wait a bit then try second operation
            delay(100)
            
            val secondOperation = async {
                try {
                    AIInstanceManager.executeAIOperation(
                        AIInstanceManager.AIOperationType.CODE_REVIEW,
                        "Second operation"
                    ) {
                        "Second result"
                    }
                } catch (e: AIOperationBlockedException) {
                    secondOperationBlocked = true
                    "Blocked as expected"
                }
            }
            
            // Wait for both to complete
            firstOperation.await()
            secondOperation.await()
        }
        
        // Verify constraint was enforced
        assertTrue("First operation should have completed", firstOperationCompleted)
        assertTrue("Second operation should have been blocked", secondOperationBlocked)
        
        // Verify state is clean after operations
        assertFalse(AIInstanceManager.isAIOperationActive())
        assertNull(AIInstanceManager.getCurrentActiveOperation())
    }
    
    @Test
    fun `test sequential AI operations work`() = runBlocking {
        // Reset state
        AIInstanceManager.forceReleaseAILock()
        
        // Execute first operation
        val result1 = AIInstanceManager.executeAIOperation(
            AIInstanceManager.AIOperationType.DEEPSEEK_ENGINE,
            "Sequential operation 1"
        ) {
            "Result 1"
        }
        
        // Execute second operation (should work since first is done)
        val result2 = AIInstanceManager.executeAIOperation(
            AIInstanceManager.AIOperationType.WEB_NET_CASTE,
            "Sequential operation 2"  
        ) {
            "Result 2"
        }
        
        assertEquals("Result 1", result1)
        assertEquals("Result 2", result2)
        
        // Verify final state
        assertFalse(AIInstanceManager.isAIOperationActive())
    }
}