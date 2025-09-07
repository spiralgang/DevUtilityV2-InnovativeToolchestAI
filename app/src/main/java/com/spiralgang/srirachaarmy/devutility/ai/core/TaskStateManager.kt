// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.ai.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskStateManager - Core component of AIGuideNet
 * 
 * Manages hierarchical task state, decomposition, and execution tracking.
 * This is the Short-Term Context Memory that maintains current task status
 * and intermediate results for the Executive Planner.
 * 
 * Part of the Autonomous Internal Guidance & Routing Network (AIGuideNet)
 */
@Singleton
class TaskStateManager @Inject constructor() {
    
    private val _currentTask = MutableStateFlow<TaskNode?>(null)
    val currentTask: StateFlow<TaskNode?> = _currentTask.asStateFlow()
    
    private val _taskHistory = MutableStateFlow<List<TaskNode>>(emptyList())
    val taskHistory: StateFlow<List<TaskNode>> = _taskHistory.asStateFlow()
    
    private val activeTasks = mutableMapOf<String, TaskNode>()
    private val completedTasks = mutableMapOf<String, TaskNode>()
    
    data class TaskNode(
        val id: String = UUID.randomUUID().toString(),
        val parentId: String? = null,
        val title: String,
        val description: String,
        val type: TaskType,
        val status: TaskStatus,
        val priority: TaskPriority,
        val dependencies: List<String> = emptyList(),
        val subTasks: MutableList<TaskNode> = mutableListOf(),
        val context: Map<String, Any> = emptyMap(),
        val inputs: Map<String, Any> = emptyMap(),
        val outputs: MutableMap<String, Any> = mutableMapOf(),
        val errors: MutableList<TaskError> = mutableListOf(),
        val createdAt: Long = System.currentTimeMillis(),
        val startedAt: Long? = null,
        val completedAt: Long? = null,
        val estimatedDuration: Long? = null,
        val actualDuration: Long? = null,
        val toolsUsed: MutableList<String> = mutableListOf(),
        val metadata: MutableMap<String, Any> = mutableMapOf()
    ) {
        fun isCompleted(): Boolean = status == TaskStatus.COMPLETED
        fun isFailed(): Boolean = status == TaskStatus.FAILED
        fun isBlocked(): Boolean = status == TaskStatus.BLOCKED
        fun isInProgress(): Boolean = status == TaskStatus.IN_PROGRESS
        fun isPending(): Boolean = status == TaskStatus.PENDING
        
        fun getExecutionTime(): Long? {
            return if (startedAt != null && completedAt != null) {
                completedAt - startedAt
            } else null
        }
        
        fun addSubTask(subTask: TaskNode): TaskNode {
            subTasks.add(subTask.copy(parentId = this.id))
            return this
        }
        
        fun updateStatus(newStatus: TaskStatus): TaskNode {
            val updatedTask = this.copy(status = newStatus)
            return when (newStatus) {
                TaskStatus.IN_PROGRESS -> updatedTask.copy(startedAt = System.currentTimeMillis())
                TaskStatus.COMPLETED, TaskStatus.FAILED -> updatedTask.copy(completedAt = System.currentTimeMillis())
                else -> updatedTask
            }
        }
    }
    
    data class TaskError(
        val id: String = UUID.randomUUID().toString(),
        val type: ErrorType,
        val message: String,
        val details: String? = null,
        val timestamp: Long = System.currentTimeMillis(),
        val recoverable: Boolean = true,
        val retryCount: Int = 0,
        val context: Map<String, Any> = emptyMap()
    )
    
    enum class TaskType {
        USER_REQUEST,
        GOAL_DECOMPOSITION,
        TOOL_EXECUTION,
        DATA_PROCESSING,
        VALIDATION,
        LEARNING,
        SYSTEM_OPERATION,
        REFLECTION
    }
    
    enum class TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        BLOCKED,
        CANCELLED,
        PAUSED
    }
    
    enum class TaskPriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
    
    enum class ErrorType {
        TOOL_ERROR,
        VALIDATION_ERROR,
        DEPENDENCY_ERROR,
        TIMEOUT_ERROR,
        SYSTEM_ERROR,
        USER_ERROR,
        DATA_ERROR
    }
    
    /**
     * Create a new task from user input or internal goal
     */
    suspend fun createTask(
        title: String,
        description: String,
        type: TaskType = TaskType.USER_REQUEST,
        priority: TaskPriority = TaskPriority.NORMAL,
        context: Map<String, Any> = emptyMap(),
        inputs: Map<String, Any> = emptyMap()
    ): TaskNode = withContext(Dispatchers.IO) {
        val task = TaskNode(
            title = title,
            description = description,
            type = type,
            status = TaskStatus.PENDING,
            priority = priority,
            context = context,
            inputs = inputs
        )
        
        activeTasks[task.id] = task
        _currentTask.value = task
        
        Timber.d("Created new task: ${task.title} (${task.id})")
        return@withContext task
    }
    
    /**
     * Decompose a task into subtasks
     */
    suspend fun decomposeTask(
        taskId: String,
        subtasks: List<TaskNode>
    ): TaskNode? = withContext(Dispatchers.IO) {
        val task = activeTasks[taskId] ?: return@withContext null
        
        subtasks.forEach { subtask ->
            task.addSubTask(subtask.copy(parentId = taskId))
            activeTasks[subtask.id] = subtask
        }
        
        val decomposedTask = task.copy(
            type = TaskType.GOAL_DECOMPOSITION,
            metadata = task.metadata.apply { 
                put("decomposed", true)
                put("subtask_count", subtasks.size)
            }
        )
        
        activeTasks[taskId] = decomposedTask
        _currentTask.value = decomposedTask
        
        Timber.d("Decomposed task ${task.title} into ${subtasks.size} subtasks")
        return@withContext decomposedTask
    }
    
    /**
     * Update task status and manage state transitions
     */
    suspend fun updateTaskStatus(
        taskId: String,
        status: TaskStatus,
        outputs: Map<String, Any> = emptyMap(),
        error: TaskError? = null
    ): TaskNode? = withContext(Dispatchers.IO) {
        val task = activeTasks[taskId] ?: return@withContext null
        
        val updatedTask = task.updateStatus(status).copy(
            outputs = task.outputs.apply { putAll(outputs) }
        )
        
        error?.let { updatedTask.errors.add(it) }
        
        when (status) {
            TaskStatus.COMPLETED, TaskStatus.FAILED, TaskStatus.CANCELLED -> {
                activeTasks.remove(taskId)
                completedTasks[taskId] = updatedTask
                updateTaskHistory(updatedTask)
                
                // Check if parent task can be updated
                updatedTask.parentId?.let { parentId ->
                    checkParentTaskCompletion(parentId)
                }
            }
            else -> {
                activeTasks[taskId] = updatedTask
            }
        }
        
        _currentTask.value = updatedTask
        
        Timber.d("Updated task ${task.title} status to $status")
        return@withContext updatedTask
    }
    
    /**
     * Add tool execution result to task
     */
    suspend fun addToolExecution(
        taskId: String,
        toolName: String,
        inputs: Map<String, Any>,
        outputs: Map<String, Any>,
        success: Boolean,
        executionTime: Long? = null
    ): TaskNode? = withContext(Dispatchers.IO) {
        val task = activeTasks[taskId] ?: return@withContext null
        
        task.toolsUsed.add(toolName)
        task.outputs.putAll(outputs)
        task.metadata["tool_executions"] = (task.metadata["tool_executions"] as? List<Map<String, Any>> ?: emptyList()) + 
            mapOf(
                "tool" to toolName,
                "inputs" to inputs,
                "outputs" to outputs,
                "success" to success,
                "execution_time" to executionTime,
                "timestamp" to System.currentTimeMillis()
            )
        
        activeTasks[taskId] = task
        
        Timber.d("Added tool execution $toolName to task ${task.title}")
        return@withContext task
    }
    
    /**
     * Get next pending task based on priority and dependencies
     */
    suspend fun getNextPendingTask(): TaskNode? = withContext(Dispatchers.IO) {
        val pendingTasks = activeTasks.values
            .filter { it.isPending() }
            .filter { task -> 
                // Check if all dependencies are completed
                task.dependencies.all { depId -> 
                    completedTasks[depId]?.isCompleted() == true 
                }
            }
            .sortedWith(
                compareByDescending<TaskNode> { it.priority.ordinal }
                    .thenBy { it.createdAt }
            )
        
        return@withContext pendingTasks.firstOrNull()
    }
    
    /**
     * Get task execution statistics
     */
    suspend fun getTaskStatistics(): Map<String, Any> = withContext(Dispatchers.IO) {
        val allTasks = (activeTasks.values + completedTasks.values).toList()
        
        val stats = mutableMapOf<String, Any>()
        
        stats["total_tasks"] = allTasks.size
        stats["active_tasks"] = activeTasks.size
        stats["completed_tasks"] = completedTasks.values.count { it.isCompleted() }
        stats["failed_tasks"] = completedTasks.values.count { it.isFailed() }
        stats["blocked_tasks"] = activeTasks.values.count { it.isBlocked() }
        
        // Task type distribution
        val typeDistribution = allTasks
            .groupBy { it.type }
            .mapValues { it.value.size }
        stats["task_type_distribution"] = typeDistribution
        
        // Priority distribution
        val priorityDistribution = allTasks
            .groupBy { it.priority }
            .mapValues { it.value.size }
        stats["priority_distribution"] = priorityDistribution
        
        // Average execution times
        val completedWithTimes = completedTasks.values.filter { it.getExecutionTime() != null }
        if (completedWithTimes.isNotEmpty()) {
            val avgExecutionTime = completedWithTimes.mapNotNull { it.getExecutionTime() }.average()
            stats["average_execution_time_ms"] = avgExecutionTime
        }
        
        // Most used tools
        val toolUsage = allTasks
            .flatMap { it.toolsUsed }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        stats["most_used_tools"] = toolUsage
        
        // Success rate
        val totalCompleted = completedTasks.size
        if (totalCompleted > 0) {
            val successful = completedTasks.values.count { it.isCompleted() }
            stats["success_rate"] = successful.toFloat() / totalCompleted.toFloat()
        }
        
        return@withContext stats
    }
    
    /**
     * Get current task context for other AI components
     */
    suspend fun getCurrentTaskContext(): Map<String, Any> = withContext(Dispatchers.IO) {
        val currentTask = _currentTask.value ?: return@withContext emptyMap()
        
        return@withContext mapOf(
            "current_task_id" to currentTask.id,
            "current_task_title" to currentTask.title,
            "current_task_type" to currentTask.type,
            "current_task_status" to currentTask.status,
            "current_task_context" to currentTask.context,
            "current_task_inputs" to currentTask.inputs,
            "current_task_outputs" to currentTask.outputs,
            "active_subtasks" to currentTask.subTasks.filter { it.status == TaskStatus.IN_PROGRESS }.size,
            "pending_subtasks" to currentTask.subTasks.filter { it.status == TaskStatus.PENDING }.size,
            "completed_subtasks" to currentTask.subTasks.filter { it.status == TaskStatus.COMPLETED }.size,
            "tools_used" to currentTask.toolsUsed,
            "error_count" to currentTask.errors.size
        )
    }
    
    /**
     * Search tasks by criteria
     */
    suspend fun searchTasks(
        query: String? = null,
        type: TaskType? = null,
        status: TaskStatus? = null,
        priority: TaskPriority? = null
    ): List<TaskNode> = withContext(Dispatchers.IO) {
        val allTasks = (activeTasks.values + completedTasks.values).toList()
        
        return@withContext allTasks.filter { task ->
            (query == null || task.title.contains(query, ignoreCase = true) || 
             task.description.contains(query, ignoreCase = true)) &&
            (type == null || task.type == type) &&
            (status == null || task.status == status) &&
            (priority == null || task.priority == priority)
        }.sortedByDescending { it.createdAt }
    }
    
    // Private helper methods
    
    private suspend fun checkParentTaskCompletion(parentId: String) = withContext(Dispatchers.IO) {
        val parentTask = activeTasks[parentId] ?: return@withContext
        
        val allSubtasksCompleted = parentTask.subTasks.all { subtask ->
            val currentSubtask = activeTasks[subtask.id] ?: completedTasks[subtask.id]
            currentSubtask?.isCompleted() == true
        }
        
        if (allSubtasksCompleted) {
            updateTaskStatus(
                parentId, 
                TaskStatus.COMPLETED,
                mapOf("completion_reason" to "all_subtasks_completed")
            )
        }
    }
    
    private suspend fun updateTaskHistory(task: TaskNode) = withContext(Dispatchers.IO) {
        val currentHistory = _taskHistory.value.toMutableList()
        currentHistory.add(task)
        
        // Keep only last 100 tasks in memory
        if (currentHistory.size > 100) {
            currentHistory.removeAt(0)
        }
        
        _taskHistory.value = currentHistory
    }
    
    /**
     * Clear completed tasks older than specified time
     */
    suspend fun cleanupOldTasks(olderThanMillis: Long = 24 * 60 * 60 * 1000) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - olderThanMillis
        val tasksToRemove = completedTasks.values
            .filter { it.completedAt != null && it.completedAt!! < cutoffTime }
            .map { it.id }
        
        tasksToRemove.forEach { taskId ->
            completedTasks.remove(taskId)
        }
        
        Timber.d("Cleaned up ${tasksToRemove.size} old completed tasks")
    }
}