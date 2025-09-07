package com.spiralgang.srirachaarmy.devutility.ai.core

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Integration test for AIGuideNet components
 * 
 * This test validates the basic functionality of the AIGuideNet framework
 * including task state management, guidance system, and environment awareness.
 */
class AIGuideNetIntegrationTest {
    
    private lateinit var taskStateManager: TaskStateManager
    private lateinit var aiGuidanceSystem: AIGuidanceSystem
    
    @Before
    fun setup() {
        taskStateManager = TaskStateManager()
        aiGuidanceSystem = AIGuidanceSystem()
    }
    
    @Test
    fun testTaskStateManagerBasicFunctionality() = runBlocking {
        // Test task creation
        val task = taskStateManager.createTask(
            title = "Test Task",
            description = "A simple test task",
            type = TaskStateManager.TaskType.USER_REQUEST,
            priority = TaskStateManager.TaskPriority.NORMAL
        )
        
        assertNotNull(task)
        assertEquals("Test Task", task.title)
        assertEquals(TaskStateManager.TaskStatus.PENDING, task.status)
        
        // Test task status update
        val updatedTask = taskStateManager.updateTaskStatus(
            taskId = task.id,
            status = TaskStateManager.TaskStatus.COMPLETED,
            outputs = mapOf("result" to "success")
        )
        
        assertNotNull(updatedTask)
        assertEquals(TaskStateManager.TaskStatus.COMPLETED, updatedTask!!.status)
        assertTrue(updatedTask.outputs.containsKey("result"))
        
        // Test task statistics
        val stats = taskStateManager.getTaskStatistics()
        assertNotNull(stats)
        assertTrue(stats.containsKey("total_tasks"))
        assertTrue(stats.containsKey("completed_tasks"))
    }
    
    @Test
    fun testAIGuidanceSystemBasicFunctionality() = runBlocking {
        // Initialize guidance system
        aiGuidanceSystem.initialize()
        
        // Test knowledge addition
        val knowledgeEntry = aiGuidanceSystem.addKnowledge(
            category = AIGuidanceSystem.KnowledgeCategory.BEST_PRACTICES,
            topic = "Test Knowledge",
            content = "This is test knowledge content",
            confidence = 0.9f
        )
        
        assertNotNull(knowledgeEntry)
        assertEquals("Test Knowledge", knowledgeEntry.topic)
        assertEquals(0.9f, knowledgeEntry.confidence, 0.01f)
        
        // Test knowledge query
        val queryResults = aiGuidanceSystem.queryKnowledge("Test")
        assertNotNull(queryResults)
        assertTrue(queryResults.isNotEmpty())
        assertEquals("Test Knowledge", queryResults[0].topic)
        
        // Test policy addition
        val policy = aiGuidanceSystem.addPolicy(
            name = "Test Policy",
            description = "A test policy",
            category = AIGuidanceSystem.PolicyCategory.WORKFLOW_OPTIMIZATION,
            condition = "test_condition",
            action = "test_action"
        )
        
        assertNotNull(policy)
        assertEquals("Test Policy", policy.name)
        
        // Test getting applicable policies
        val applicablePolicies = aiGuidanceSystem.getApplicablePolicies(
            mapOf("test" to "value")
        )
        assertNotNull(applicablePolicies)
        
        // Test guidance statistics
        val stats = aiGuidanceSystem.getGuidanceStatistics()
        assertNotNull(stats)
        assertTrue(stats.containsKey("knowledge_entries"))
        assertTrue(stats.containsKey("active_policies"))
    }
    
    @Test
    fun testTaskDecomposition() = runBlocking {
        val mainTask = taskStateManager.createTask(
            title = "Complex Task",
            description = "A task that needs decomposition",
            type = TaskStateManager.TaskType.DATA_PROCESSING
        )
        
        val subtasks = listOf(
            TaskStateManager.TaskNode(
                title = "Subtask 1",
                description = "First subtask",
                type = TaskStateManager.TaskType.VALIDATION,
                status = TaskStateManager.TaskStatus.PENDING,
                priority = TaskStateManager.TaskPriority.HIGH
            ),
            TaskStateManager.TaskNode(
                title = "Subtask 2", 
                description = "Second subtask",
                type = TaskStateManager.TaskType.DATA_PROCESSING,
                status = TaskStateManager.TaskStatus.PENDING,
                priority = TaskStateManager.TaskPriority.NORMAL
            )
        )
        
        val decomposedTask = taskStateManager.decomposeTask(mainTask.id, subtasks)
        
        assertNotNull(decomposedTask)
        assertEquals(2, decomposedTask!!.subTasks.size)
        assertEquals(TaskStateManager.TaskType.GOAL_DECOMPOSITION, decomposedTask.type)
        assertTrue(decomposedTask.metadata.containsKey("decomposed"))
        assertTrue(decomposedTask.metadata["decomposed"] as Boolean)
    }
    
    @Test
    fun testUserPreferenceLearning() = runBlocking {
        aiGuidanceSystem.initialize()
        
        val preference = aiGuidanceSystem.learnUserPreference(
            userId = "test_user",
            category = AIGuidanceSystem.PreferenceCategory.CODING_STYLE,
            key = "indentation",
            value = "spaces",
            confidence = 0.8f,
            learnedFrom = "code_analysis"
        )
        
        assertNotNull(preference)
        assertEquals("test_user", preference.userId)
        assertEquals("indentation", preference.key)
        assertEquals("spaces", preference.value)
        assertEquals(0.8f, preference.confidence, 0.01f)
        
        // Test preference retrieval
        val userPreferences = aiGuidanceSystem.getUserPreferences(
            userId = "test_user",
            category = AIGuidanceSystem.PreferenceCategory.CODING_STYLE
        )
        
        assertNotNull(userPreferences)
        assertTrue(userPreferences.isNotEmpty())
        assertEquals("indentation", userPreferences[0].key)
    }
    
    @Test
    fun testWorkflowPatternRecording() = runBlocking {
        aiGuidanceSystem.initialize()
        
        val workflowSteps = listOf(
            AIGuidanceSystem.WorkflowStep(
                stepNumber = 1,
                toolName = "ValidationService",
                parameters = mapOf("input" to "test_data"),
                expectedOutputs = listOf("validation_result")
            ),
            AIGuidanceSystem.WorkflowStep(
                stepNumber = 2,
                toolName = "DataProcessor",
                parameters = mapOf("data" to "validated_data"),
                expectedOutputs = listOf("processed_data")
            )
        )
        
        val pattern = aiGuidanceSystem.recordWorkflowPattern(
            name = "test_workflow",
            description = "A test workflow pattern",
            steps = workflowSteps,
            executionTime = 5000L
        )
        
        assertNotNull(pattern)
        assertEquals("test_workflow", pattern.name)
        assertEquals(2, pattern.steps.size)
        assertEquals(5000L, pattern.averageExecutionTime)
        assertEquals(1, pattern.usageCount)
    }
    
    @Test
    fun testSystemIntegration() = runBlocking {
        // Test that components can work together
        aiGuidanceSystem.initialize()
        
        // Create a task
        val task = taskStateManager.createTask(
            title = "Integration Test Task",
            description = "Testing system integration",
            type = TaskStateManager.TaskType.SYSTEM_OPERATION
        )
        
        // Add relevant knowledge
        aiGuidanceSystem.addKnowledge(
            category = AIGuidanceSystem.KnowledgeCategory.WORKFLOW_PATTERNS,
            topic = "System Operations",
            content = "Best practices for system operations include validation and monitoring",
            confidence = 0.9f
        )
        
        // Add a policy
        aiGuidanceSystem.addPolicy(
            name = "System Safety",
            description = "Ensure system safety during operations",
            category = AIGuidanceSystem.PolicyCategory.SYSTEM_CONSTRAINTS,
            condition = "system_operation",
            action = "validate_before_execution"
        )
        
        // Get contextual recommendations
        val recommendations = aiGuidanceSystem.getContextualRecommendations(
            mapOf("task_type" to "system_operation")
        )
        
        assertNotNull(recommendations)
        assertTrue(recommendations.isNotEmpty())
        
        // Update task with tool execution
        taskStateManager.addToolExecution(
            taskId = task.id,
            toolName = "SystemController",
            inputs = mapOf("operation" to "test"),
            outputs = mapOf("result" to "success"),
            success = true,
            executionTime = 2000L
        )
        
        // Complete the task
        taskStateManager.updateTaskStatus(
            taskId = task.id,
            status = TaskStateManager.TaskStatus.COMPLETED,
            outputs = mapOf("final_result" to "integration_test_success")
        )
        
        // Verify integration worked
        val taskStats = taskStateManager.getTaskStatistics()
        val guidanceStats = aiGuidanceSystem.getGuidanceStatistics()
        
        assertTrue((taskStats["completed_tasks"] as Int) > 0)
        assertTrue((guidanceStats["knowledge_entries"] as Int) > 0)
        assertTrue((guidanceStats["active_policies"] as Int) > 0)
    }
    
    @Test
    fun testErrorHandling() = runBlocking {
        // Test that system handles errors gracefully
        
        // Try to update non-existent task
        val result = taskStateManager.updateTaskStatus(
            taskId = "non_existent_id",
            status = TaskStateManager.TaskStatus.COMPLETED
        )
        assertNull(result)
        
        // Try to query empty knowledge base
        val queryResults = aiGuidanceSystem.queryKnowledge("non_existent_topic")
        assertNotNull(queryResults)
        assertTrue(queryResults.isEmpty())
        
        // Test with invalid parameters should not crash
        try {
            aiGuidanceSystem.getApplicablePolicies(emptyMap())
            // Should complete without exception
            assertTrue(true)
        } catch (e: Exception) {
            fail("Should not throw exception for empty context")
        }
    }
}