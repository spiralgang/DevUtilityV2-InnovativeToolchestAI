package com.spiralgang.srirachaarmy.devutility.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ModuleUi component for agent dashboard integration
 */
@Composable
fun AgentDashboardModuleUi(
    modifier: Modifier = Modifier,
    onAgentToggle: (String, Boolean) -> Unit = { _, _ -> }
) {
    var agents by remember { mutableStateOf(listOf<AgentInfo>()) }
    
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DevUtility Agent System",
            style = MaterialTheme.typography.headlineMedium
        )
        
        agents.forEach { agent ->
            AgentCard(
                agent = agent,
                onToggle = { onAgentToggle(agent.name, !agent.active) }
            )
        }
    }
}

@Composable
private fun AgentCard(
    agent: AgentInfo,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = agent.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Status: ${agent.status}",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agent.active) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (agent.active) "Stop" else "Start")
            }
        }
    }
}

data class AgentInfo(
    val name: String,
    val status: String,
    val active: Boolean
)# agent-bind:AgentDashboard->ModuleUi
