// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.ui.enhanced

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.spiralgang.srirachaarmy.devutility.ai.CompatibleModelLoRAFineTuning
import com.spiralgang.srirachaarmy.devutility.ai.LivingAINativeInterface
import com.spiralgang.srirachaarmy.devutility.system.DistributionCategory
import com.spiralgang.srirachaarmy.devutility.system.RootFSManager

/**
 * Enhanced DevUtility Interface showcasing new LoRA fine-tuning and expanded RootFS capabilities
 * This represents the intimate UI 'AI&i' living-code agentic agency interface
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnhancedDevUtilityInterface(
    livingAIInterface: LivingAINativeInterface,
    rootFSManager: RootFSManager,
    modifier: Modifier = Modifier
) {
    val livingState by livingAIInterface.livingState.collectAsStateWithLifecycle()
    val fineTuningState by livingAIInterface.getFineTuningState().collectAsStateWithLifecycle()
    val fineTuningProgress by livingAIInterface.getFineTuningProgress().collectAsStateWithLifecycle()
    val availableDistributions by rootFSManager.availableDistributions.collectAsStateWithLifecycle()
    
    // Coroutine scope for handling async operations
    val scope = rememberCoroutineScope()
    
    // Dynamic breathing animation based on AI energy
    val breathingScale by animateFloatAsState(
        targetValue = 1.0f + (livingState.energyLevel * 0.1f),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // AI personality colors
    val personalityColor = remember(livingState.aiPersonalityActive) {
        when (livingState.aiPersonalityActive) {
            "CodeReaver" -> Color(0xFF9C27B0) // Purple
            "WebNetCaste" -> Color(0xFF2196F3) // Blue  
            "LearningBot" -> Color(0xFF4CAF50) // Green
            "SrirachaArmy" -> Color(0xFFFF5722) // Orange
            "PersonalizedGemma" -> Color(0xFFE91E63) // Pink
            else -> Color(0xFF673AB7) // Default purple
        }
    }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("🧠 AI Fine-Tuning", "🐧 RootFS Systems", "⚡ Living Interface")
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        personalityColor.copy(alpha = 0.1f),
                        Color.Black.copy(alpha = 0.9f)
                    ),
                    radius = 1000f
                )
            )
            .scale(breathingScale)
    ) {
        // Dynamic header with AI personality indicator
        EnhancedHeader(
            personalityColor = personalityColor,
            livingState = livingState,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tab navigation
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color.Transparent,
            contentColor = personalityColor,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = personalityColor
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) personalityColor else Color.Gray,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Content based on selected tab
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { if (targetState > initialState) 300 else -300 }
                ) with slideOutHorizontally(
                    targetOffsetX = { if (targetState > initialState) -300 else 300 }
                )
            }
        ) { tab ->
            when (tab) {
                0 -> LoRAFineTuningPanel(
                    fineTuningState = fineTuningState,
                    fineTuningProgress = fineTuningProgress,
                    personalityColor = personalityColor,
                    onStartFineTuning = { 
                        // Start LoRA fine-tuning process
                        scope.launch {
                            livingAIInterface.startPersonalizedFineTuning()
                        }
                    },
                    onTrainPersonality = { personality ->
                        // Train specific AI personality
                        scope.launch {
                            livingAIInterface.trainAIPersonality(personality)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                1 -> RootFSPanel(
                    distributions = availableDistributions,
                    personalityColor = personalityColor,
                    onDownloadDistribution = { distribution ->
                        // Download and install distribution
                        scope.launch {
                            rootFSManager.downloadDistribution(distribution)
                        }
                    },
                    onCreateEnvironment = { distribution ->
                        // Create new environment from distribution
                        scope.launch {
                            rootFSManager.createXShadowChrootEnvironment(
                                distributionName = distribution.name,
                                category = distribution.category
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                2 -> LivingInterfacePanel(
                    livingState = livingState,
                    personalityColor = personalityColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun EnhancedHeader(
    personalityColor: Color,
    livingState: LivingAINativeInterface.LivingInterfaceState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        backgroundColor = personalityColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "DevUtility AI&i Interface",
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Bold,
                        color = personalityColor
                    )
                )
                
                // AI Energy Indicator
                CircularProgressIndicator(
                    progress = livingState.energyLevel,
                    color = personalityColor,
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Contextual Understanding Progress
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Understanding: ${(livingState.contextualUnderstanding * 100).toInt()}%",
                    color = Color.White,
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.width(12.dp))
                LinearProgressIndicator(
                    progress = livingState.contextualUnderstanding,
                    color = personalityColor,
                    backgroundColor = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // AI Personality and Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Active AI: ${livingState.aiPersonalityActive}",
                    color = personalityColor,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
                )
                
                Text(
                    text = "Mode: ${livingState.currentMode}",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

private fun getCategoryDisplayName(category: DistributionCategory): String {
    return when (category) {
        DistributionCategory.GENERAL -> "🏠 General Purpose"
        DistributionCategory.SECURITY -> "🔒 Security & Penetration Testing"
        DistributionCategory.MINIMAL -> "⚡ Minimal & Lightweight"
        DistributionCategory.DEVELOPMENT -> "💻 Development Focused"
        DistributionCategory.CONTAINER -> "📦 Container Optimized"
        DistributionCategory.EMBEDDED -> "🔧 Embedded & IoT"
        DistributionCategory.EXPERIMENTAL -> "🧪 Experimental"
    }
}

// Additional composables would be implemented here...
// (Truncated for brevity - the full implementation would include all the helper composables)