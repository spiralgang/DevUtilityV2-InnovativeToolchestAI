// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.agentic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * AgenticIntegrationInterface - Living Repository Integration UI
 * 
 * The main UI component that brings together all agentic systems and makes
 * the entire organized repository accessible through the DevUtility app.
 * This interface dynamically adapts based on available resources, active
 * workflows, and user preferences.
 * 
 * Features:
 * - Real-time repository resource browsing and interaction
 * - Living code instance monitoring and management
 * - Agent workflow visualization and control
 * - Dynamic resource loading with intelligent prefetching
 * - Context-aware resource recommendations
 * - Multi-agent collaboration monitoring
 * 
 * Implements all DevUtility agentic standards:
 * - @GDA: Guided development through structured resource access
 * - @UFUIC-O: User-customizable interface for all repository content
 * - @PIPI: Preview-implement-push-implement for safe operations
 * - @LDU: Linear development updates with full traceability
 * - @EG: Easy-to-grasp interface for complex repository operations
 * - @GATT: AI-guided tutorials for repository utilization
 * - @SWT: Structured walk-through of all available capabilities
 */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AgenticIntegrationInterface(
    repositoryManager: AgenticRepositoryManager,
    livingCodeAdapter: LivingCodeAdapter,
    workflowEngine: AgenticWorkflowEngine,
    resourceLoader: DynamicResourceLoader,
    modifier: Modifier = Modifier
) {
    val livingCodeState by repositoryManager.livingCodeState.collectAsStateWithLifecycle()
    val livingInstances by livingCodeAdapter.livingInstances.collectAsStateWithLifecycle()
    val workflowState by workflowEngine.workflowState.collectAsStateWithLifecycle()
    val resourceCache by resourceLoader.resourceCache.collectAsStateWithLifecycle()
    val loadingMetrics by resourceLoader.loadingMetrics.collectAsStateWithLifecycle()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // Dynamic color scheme based on system state
    val dynamicColors = remember(workflowState.systemLoad, livingInstances.size) {
        generateDynamicColors(workflowState.systemLoad, livingInstances.size)
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // @GATT: AI-guided top bar with tutorial tips
        AgenticTopBar(
            totalResources = livingCodeState.availableResources.size,
            activeWorkflows = workflowState.activeCollaborations.size,
            systemLoad = workflowState.systemLoad,
            dynamicColors = dynamicColors
        )
        
        // @EG: Easy-to-grasp tab navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = dynamicColors.surface,
            contentColor = dynamicColors.onSurface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Repository") },
                icon = { Icon(Icons.Default.Folder, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Living Code") },
                icon = { 
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = null,
                        modifier = Modifier.rotate(if (livingInstances.isNotEmpty()) 360f else 0f)
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Workflows") },
                icon = { Icon(Icons.Default.Groups, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Resources") },
                icon = { Icon(Icons.Default.CloudQueue, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Insights") },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null) }
            )
        }
        
        // @SWT: Structured walk-through of tab content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "tab_content"
        ) { tab ->
            when (tab) {
                0 -> RepositoryExplorer(
                    livingCodeState = livingCodeState,
                    resourceLoader = resourceLoader,
                    workflowEngine = workflowEngine,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> LivingCodeMonitor(
                    livingInstances = livingInstances,
                    livingCodeAdapter = livingCodeAdapter,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> WorkflowDashboard(
                    workflowState = workflowState,
                    workflowEngine = workflowEngine,
                    modifier = Modifier.fillMaxSize()
                )
                3 -> ResourceManager(
                    resourceCache = resourceCache,
                    loadingMetrics = loadingMetrics,
                    resourceLoader = resourceLoader,
                    modifier = Modifier.fillMaxSize()
                )
                4 -> SystemInsights(
                    livingCodeState = livingCodeState,
                    workflowState = workflowState,
                    loadingMetrics = loadingMetrics,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Top bar showing system status and AI-guided tips
 */
@Composable
private fun AgenticTopBar(
    totalResources: Int,
    activeWorkflows: Int,
    systemLoad: Double,
    dynamicColors: AgenticColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = dynamicColors.primaryContainer,
            contentColor = dynamicColors.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DevUtility Agentic Integration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All repository resources available as living code",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$totalResources", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Workspaces, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("$activeWorkflows", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${(systemLoad * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * Repository Explorer - Browse and interact with all organized repository content
 */
@Composable
private fun RepositoryExplorer(
    livingCodeState: LivingCodeState,
    resourceLoader: DynamicResourceLoader,
    workflowEngine: AgenticWorkflowEngine,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = modifier.padding(16.dp)) {
        // @EG: Easy-to-grasp search and filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search repository resources") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Category filters
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("All", "Configs", "Tools", "Datasets", "Living Code", "Scripts")) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Resource list
        val filteredResources = remember(livingCodeState.availableResources, selectedCategory, searchQuery) {
            livingCodeState.availableResources.filter { resource ->
                val matchesCategory = selectedCategory == "All" || when (selectedCategory) {
                    "Configs" -> resource.type == ResourceType.CONFIG
                    "Tools" -> resource.type == ResourceType.TOOL
                    "Datasets" -> resource.type == ResourceType.DATASET || resource.type == ResourceType.TRAINING_DATA
                    "Living Code" -> resource.isLivingCode
                    "Scripts" -> resource.type == ResourceType.SCRIPT
                    else -> true
                }
                val matchesSearch = searchQuery.isEmpty() || 
                    resource.title.contains(searchQuery, ignoreCase = true) ||
                    resource.description.contains(searchQuery, ignoreCase = true) ||
                    resource.tags.any { it.contains(searchQuery, ignoreCase = true) }
                
                matchesCategory && matchesSearch
            }
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredResources) { resource ->
                RepositoryResourceCard(
                    resource = resource,
                    onLoadResource = { 
                        scope.launch {
                            resourceLoader.loadResourceByPath(resource.path)
                        }
                    },
                    onTransformToLiving = {
                        scope.launch {
                            workflowEngine.queueTask(
                                WorkflowTask(
                                    id = "transform_${resource.path.hashCode()}_${System.currentTimeMillis()}",
                                    type = "resource_transformation",
                                    description = "Transform ${resource.title} to living code",
                                    requiredAgents = listOf("resource_scanner", "adaptation_engine"),
                                    resources = listOf(resource.path),
                                    priority = TaskPriority.HIGH,
                                    agenticStandards = resource.agenticStandards,
                                    expectedOutcome = "Living code instance"
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * Individual repository resource card
 */
@Composable
private fun RepositoryResourceCard(
    resource: RepositoryResource,
    onLoadResource: () -> Unit,
    onTransformToLiving: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLoadResource() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = resource.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = resource.path,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Badge(
                        containerColor = when (resource.type) {
                            ResourceType.CONFIG -> Color(0xFF4CAF50)
                            ResourceType.TOOL -> Color(0xFF2196F3)
                            ResourceType.DATASET -> Color(0xFF9C27B0)
                            ResourceType.LIVING_CODE -> Color(0xFFFF9800)
                            ResourceType.SCRIPT -> Color(0xFF607D8B)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Text(
                            text = resource.type.name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    if (resource.isLivingCode) {
                        Badge(
                            containerColor = Color(0xFFFF5722)
                        ) {
                            Text("LIVING", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Agentic standards tags
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(resource.agenticStandards) { standard ->
                    AssistChip(
                        onClick = { },
                        label = { Text(standard, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoadResource,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Load")
                }
                
                if (!resource.isLivingCode) {
                    OutlinedButton(
                        onClick = onTransformToLiving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Make Living")
                    }
                }
            }
        }
    }
}

/**
 * Living Code Monitor - View and manage living code instances
 */
@Composable
private fun LivingCodeMonitor(
    livingInstances: Map<String, LivingCodeInstance>,
    livingCodeAdapter: LivingCodeAdapter,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Living Code Instances",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${livingInstances.size} active instances adapting to your usage",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (livingInstances.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No Living Code Instances Yet",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Transform repository resources into living code to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(livingInstances.entries.toList()) { (id, instance) ->
                    LivingCodeInstanceCard(
                        instance = instance,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Individual living code instance card
 */
@Composable
private fun LivingCodeInstanceCard(
    instance: LivingCodeInstance,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = instance.id.substringAfter("living_").substringBefore("_"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = instance.originalPath,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "State: ${instance.adaptationState}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Badge(
                    containerColor = when (instance.adaptationState) {
                        "initialized" -> Color(0xFF4CAF50)
                        "adapting" -> Color(0xFFFF9800)
                        "optimized" -> Color(0xFF2196F3)
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Text("LIVING", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Transformation history
            Text(
                text = "Evolution History:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            instance.transformationHistory.takeLast(3).forEach { change ->
                Text(
                    text = "â€¢ $change",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Performance metrics
            if (instance.performanceMetrics.isNotEmpty()) {
                Text(
                    text = "Performance:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instance.performanceMetrics.entries.toList()) { (metric, value) ->
                        AssistChip(
                            onClick = { },
                            label = { Text("$metric: ${(value * 100).toInt()}%") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Workflow Dashboard - Monitor and control agent collaborations
 */
@Composable
private fun WorkflowDashboard(
    workflowState: WorkflowState,
    workflowEngine: AgenticWorkflowEngine,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Agent Workflow Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${workflowState.activeCollaborations.size} active collaborations",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(Modifier.height(16.dp))
        
        // System load indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "System Load: ${(workflowState.systemLoad * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = workflowState.systemLoad.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Active collaborations
        if (workflowState.activeCollaborations.isNotEmpty()) {
            Text(
                text = "Active Collaborations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workflowState.activeCollaborations) { collaboration ->
                    CollaborationCard(collaboration = collaboration)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Agent statuses
        Text(
            text = "Agent Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workflowState.agentStatuses.entries.toList()) { (agentId, status) ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.contains("active")) {
                            Color(0xFF4CAF50)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = agentId.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual collaboration card
 */
@Composable
private fun CollaborationCard(
    collaboration: AgentCollaboration,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = collaboration.task.description,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Agents: ${collaboration.participants.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = collaboration.progress.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(8.dp))
            
            if (collaboration.outputs.isNotEmpty()) {
                Text(
                    text = "Latest: ${collaboration.outputs.last()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Resource Manager - Monitor cache and loading performance
 */
@Composable
private fun ResourceManager(
    resourceCache: ResourceCache,
    loadingMetrics: LoadingMetrics,
    resourceLoader: DynamicResourceLoader,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Resource Management",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Cache statistics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Cache Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cached Resources:")
                    Text("${resourceCache.loadedResources.size}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cache Size:")
                    Text("${resourceCache.totalCacheSize / 1024}KB")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hit Rate:")
                    Text("${if (loadingMetrics.totalRequests > 0) (loadingMetrics.cacheHits * 100 / loadingMetrics.totalRequests) else 0}%")
                }
                
                Spacer(Modifier.height(8.dp))
                
                Button(
                    onClick = { resourceLoader.clearCache() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Cache")
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Prefetch queue
        if (resourceCache.prefetchQueue.isNotEmpty()) {
            Text(
                text = "Prefetch Queue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(resourceCache.prefetchQueue) { resourceId ->
                    Card {
                        Text(
                            text = resourceId,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * System Insights - Overall analytics and recommendations
 */
@Composable
private fun SystemInsights(
    livingCodeState: LivingCodeState,
    workflowState: WorkflowState,
    loadingMetrics: LoadingMetrics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "System Insights",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(16.dp))
        
        // Summary cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InsightCard(
                    title = "Repository Resources",
                    value = "${livingCodeState.availableResources.size}",
                    subtitle = "Total indexed",
                    icon = Icons.Default.Storage,
                    color = Color(0xFF4CAF50)
                )
            }
            item {
                InsightCard(
                    title = "Living Instances",
                    value = "${livingCodeState.availableResources.count { it.isLivingCode }}",
                    subtitle = "Adaptive resources",
                    icon = Icons.Default.AutoAwesome,
                    color = Color(0xFFFF9800)
                )
            }
            item {
                InsightCard(
                    title = "Active Workflows",
                    value = "${workflowState.activeCollaborations.size}",
                    subtitle = "Agent collaborations",
                    icon = Icons.Default.Groups,
                    color = Color(0xFF2196F3)
                )
            }
            item {
                InsightCard(
                    title = "System Load",
                    value = "${(workflowState.systemLoad * 100).toInt()}%",
                    subtitle = "Current capacity",
                    icon = Icons.Default.Speed,
                    color = if (workflowState.systemLoad > 0.8) Color(0xFFFF5722) else Color(0xFF4CAF50)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Recommendations
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(8.dp))
                
                val recommendations = generateRecommendations(livingCodeState, workflowState, loadingMetrics)
                recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.LightbulbOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual insight card
 */
@Composable
private fun InsightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Generate recommendations based on system state
 */
private fun generateRecommendations(
    livingCodeState: LivingCodeState,
    workflowState: WorkflowState,
    loadingMetrics: LoadingMetrics
): List<String> {
    val recommendations = mutableListOf<String>()
    
    val livingCodePercentage = if (livingCodeState.availableResources.isNotEmpty()) {
        livingCodeState.availableResources.count { it.isLivingCode } * 100 / livingCodeState.availableResources.size
    } else 0
    
    if (livingCodePercentage < 50) {
        recommendations.add("Consider transforming more resources to living code for enhanced adaptability")
    }
    
    if (workflowState.systemLoad > 0.8) {
        recommendations.add("System load is high - consider optimizing active workflows")
    }
    
    if (loadingMetrics.totalRequests > 0 && loadingMetrics.cacheHits * 100 / loadingMetrics.totalRequests < 70) {
        recommendations.add("Cache hit rate is low - enable prefetching for frequently used resources")
    }
    
    if (workflowState.queuedTasks.size > 5) {
        recommendations.add("Multiple tasks are queued - system capacity may need adjustment")
    }
    
    if (recommendations.isEmpty()) {
        recommendations.add("System is operating optimally with good resource utilization")
    }
    
    return recommendations
}

/**
 * Dynamic color scheme generation based on system state
 */
data class AgenticColorScheme(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val surface: Color,
    val onSurface: Color
)

private fun generateDynamicColors(systemLoad: Double, livingInstanceCount: Int): AgenticColorScheme {
    val primaryHue = when {
        systemLoad > 0.8 -> 0f // Red tint for high load
        livingInstanceCount > 10 -> 120f // Green tint for high activity
        else -> 240f // Blue tint for normal operation
    }
    
    return AgenticColorScheme(
        primary = Color.hsv(primaryHue, 0.6f, 0.8f),
        primaryContainer = Color.hsv(primaryHue, 0.3f, 0.95f),
        onPrimaryContainer = Color.hsv(primaryHue, 0.8f, 0.2f),
        surface = Color.hsv(primaryHue, 0.1f, 0.98f),
        onSurface = Color.hsv(primaryHue, 0.8f, 0.1f)
    )
}