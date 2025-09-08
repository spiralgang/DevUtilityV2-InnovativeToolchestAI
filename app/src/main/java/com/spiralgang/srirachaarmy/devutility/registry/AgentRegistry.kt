package com.spiralgang.srirachaarmy.devutility.registry

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Central Registry for managing agents and their bindings
 * Integrates with frontend agentRegistry.js
 */
object AgentRegistry {
    private val _agents = MutableStateFlow<Map<String, AgentConfig>>(emptyMap())
    val agents: StateFlow<Map<String, AgentConfig>> = _agents

    private val _activeAgents = MutableStateFlow<Set<String>>(emptySet())
    val activeAgents: StateFlow<Set<String>> = _activeAgents

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        initializeDefaultAgents()
    }

    fun registerAgent(name: String, config: AgentConfig) {
        val currentAgents = _agents.value.toMutableMap()
        currentAgents[name] = config.copy(status = "registered")
        _agents.value = currentAgents
        Timber.d("[Registry] Agent $name registered")
    }

    fun activateAgent(name: String): Boolean {
        val agent = _agents.value[name] ?: return false
        
        val currentActive = _activeAgents.value.toMutableSet()
        currentActive.add(name)
        _activeAgents.value = currentActive

        val currentAgents = _agents.value.toMutableMap()
        currentAgents[name] = agent.copy(status = "active", lastActivity = System.currentTimeMillis())
        _agents.value = currentAgents

        Timber.d("[Registry] Agent $name activated")
        return true
    }

    fun deactivateAgent(name: String): Boolean {
        val currentActive = _activeAgents.value.toMutableSet()
        val wasActive = currentActive.remove(name)
        _activeAgents.value = currentActive

        if (wasActive) {
            val agent = _agents.value[name]
            if (agent != null) {
                val currentAgents = _agents.value.toMutableMap()
                currentAgents[name] = agent.copy(status = "inactive")
                _agents.value = currentAgents
            }
            Timber.d("[Registry] Agent $name deactivated")
        }
        return wasActive
    }

    fun getActiveAgents(): List<AgentConfig> {
        return _activeAgents.value.mapNotNull { name -> _agents.value[name] }
    }

    suspend fun sendToBackend(agentName: String, data: String): AgentResponse {
        Timber.d("[Registry] Sending to backend agent $agentName: $data")
        // Simulate backend processing
        return AgentResponse(
            status = "success", 
            response = "Agent $agentName processed request",
            timestamp = System.currentTimeMillis()
        )
    }

    private fun initializeDefaultAgents() {
        coroutineScope.launch {
            registerAgent("Integration Agent", AgentConfig(
                name = "Integration Agent",
                type = "phi2",
                endpoint = "/api/agents/integration",
                capabilities = listOf("file-mapping", "integration-points"),
                status = "registered"
            ))

            registerAgent("Glue-Code Agent", AgentConfig(
                name = "Glue-Code Agent", 
                type = "qwen",
                endpoint = "/api/agents/glue-code",
                capabilities = listOf("missing-logic", "code-injection"),
                status = "registered"
            ))

            registerAgent("Code Review Agent", AgentConfig(
                name = "Code Review Agent",
                type = "deepseek", 
                endpoint = "/api/agents/code-review",
                capabilities = listOf("security-scan", "best-practices"),
                status = "registered"
            ))

            registerAgent("Optimization Agent", AgentConfig(
                name = "Optimization Agent",
                type = "ollama",
                endpoint = "/api/agents/optimization", 
                capabilities = listOf("performance", "resource-optimization"),
                status = "registered"
            ))
        }
    }
}

@Serializable
data class AgentConfig(
    val name: String,
    val type: String,
    val endpoint: String,
    val capabilities: List<String>,
    val status: String = "registered",
    val lastActivity: Long? = null,
    val bindTargets: List<String> = emptyList()
)

@Serializable
data class AgentResponse(
    val status: String,
    val response: String,
    val timestamp: Long
)# agent-bind:agentAPI->Registry
# agent-bind:agentRegistry->Registry
