package com.spiralgang.srirachaarmy.devutility.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spiralgang.srirachaarmy.devutility.ai.LivingAINativeInterface
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * LivingDynamicInterface - The UI that breathes and adapts with AI
 * 
 * This composable creates the intimate AI&i experience where the interface
 * dynamically morphs based on AI understanding, user patterns, and context.
 * It's not just a UI - it's a living, breathing extension of the AI mind.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LivingDynamicInterface(
    livingInterface: LivingAINativeInterface,
    modifier: Modifier = Modifier
) {
    val livingState by livingInterface.livingState.collectAsStateWithLifecycle()
    val conversationFlow by livingInterface.conversationFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val contextAwareness by livingInterface.contextAwareness.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    
    // Dynamic colors that change based on AI state
    val dynamicColors = remember(livingState.aiPersonalityActive, livingState.contextualUnderstanding) {
        generateDynamicColorScheme(livingState)
    }
    
    // Breathing animation for the living interface
    val breathingScale by animateFloatAsState(
        targetValue = 1.0f + (livingState.energyLevel * 0.02f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    // Dynamic input field that adapts to context
    var inputText by remember { mutableStateOf("") }
    var inputMode by remember { mutableStateOf(InputMode.HYBRID) }
    
    // AI suggestions that appear contextually
    var aiSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    
    LaunchedEffect(livingState.proactiveReadiness) {
        if (livingState.proactiveReadiness) {
            aiSuggestions = livingInterface.getProactiveSuggestions()
        }
    }
    
    MaterialTheme(colorScheme = dynamicColors) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .scale(breathingScale)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            dynamicColors.background,
                            dynamicColors.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dynamic header that shows AI state
                LivingHeader(
                    livingState = livingState,
                    contextAwareness = contextAwareness,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Main conversation area - this IS the terminal AND AI chat
                LivingConversationArea(
                    messages = conversationFlow,
                    aiSuggestions = aiSuggestions,
                    onSuggestionClick = { suggestion ->
                        scope.launch {
                            val response = livingInterface.processAINativeInput(suggestion)
                            Timber.d("Processed suggestion: ${response.aiResponse}")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dynamic input area that morphs based on AI understanding
                LivingInputArea(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    inputMode = inputMode,
                    onModeChange = { inputMode = it },
                    livingState = livingState,
                    onSubmit = { input ->
                        scope.launch {
                            val response = livingInterface.processAINativeInput(input)
                            inputText = ""
                            Timber.d("Processed input: ${response.aiResponse}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Floating AI personality indicator
            FloatingAIPersonality(
                personality = livingState.aiPersonalityActive,
                understanding = livingState.contextualUnderstanding,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
            
            // Adaptive context hints that appear based on AI understanding
            if (livingState.contextualUnderstanding > 0.3f) {
                AdaptiveContextHints(
                    context = contextAwareness,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun LivingHeader(
    livingState: LivingAINativeInterface.LivingInterfaceState,
    contextAwareness: LivingAINativeInterface.DynamicContext,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.3f + (livingState.contextualUnderstanding * 0.4f)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "üå∂Ô∏è SrirachaArmy Living Interface",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Dynamic energy indicator
                EnergyIndicator(energy = livingState.energyLevel)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Contextual status that shows what the AI understands
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ContextualChip(
                    label = "Mode: ${livingState.currentMode.name.replace("_", " ")}",
                    color = MaterialTheme.colorScheme.secondary
                )
                ContextualChip(
                    label = "Focus: ${contextAwareness.developmentPhase}",
                    color = MaterialTheme.colorScheme.tertiary
                )
                ContextualChip(
                    label = "Understanding: ${(livingState.contextualUnderstanding * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LivingConversationArea(
    messages: List<LivingAINativeInterface.AINativeMessage>,
    aiSuggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = false
    ) {
        items(messages) { message ->
            MessageBubble(
                message = message,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Show AI suggestions as interactive elements
        if (aiSuggestions.isNotEmpty()) {
            item {
                AISuggestionsRow(
                    suggestions = aiSuggestions,
                    onSuggestionClick = onSuggestionClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: LivingAINativeInterface.AINativeMessage,
    modifier: Modifier = Modifier
) {
    val isAI = message.interpretedAs != LivingAINativeInterface.MessageType.SHELL_COMMAND
    
    Row(
        modifier = modifier,
        horizontalArrangement = if (isAI) Arrangement.Start else Arrangement.End
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAI) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isAI) 4.dp else 16.dp,
                bottomEnd = if (isAI) 16.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message type indicator
                Text(
                    text = getMessageTypeEmoji(message.interpretedAs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // User input
                if (message.input.isNotBlank()) {
                    Text(
                        text = message.input,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // AI response
                if (message.aiResponse.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.aiResponse,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Shell output if present
                message.shellOutput?.let { output ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "‚ö° $output",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Suggested actions
                if (message.suggestedActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        message.suggestedActions.forEach { action ->
                            SuggestionChip(
                                onClick = { /* Handle action */ },
                                label = { Text(action, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AISuggestionsRow(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "üí° AI Suggestions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            suggestions.forEach { suggestion ->
                TextButton(
                    onClick = { onSuggestionClick(suggestion) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivingInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    inputMode: InputMode,
    onModeChange: (InputMode) -> Unit,
    livingState: LivingAINativeInterface.LivingInterfaceState,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Dynamic input mode selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InputMode.values().forEach { mode ->
                FilterChip(
                    onClick = { onModeChange(mode) },
                    label = { Text(mode.displayName) },
                    selected = inputMode == mode,
                    leadingIcon = { Text(mode.emoji) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // The main input field that adapts to AI understanding
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(getAdaptivePlaceholder(inputMode, livingState))
            },
            leadingIcon = { Text(inputMode.emoji) },
            trailingIcon = {
                IconButton(
                    onClick = { if (inputText.isNotBlank()) onSubmit(inputText) }
                ) {
                    Text("‚ö°", fontSize = 18.sp)
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = { if (inputText.isNotBlank()) onSubmit(inputText) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.5f + (livingState.contextualUnderstanding * 0.5f)
                )
            )
        )
    }
}

@Composable
fun FloatingAIPersonality(
    personality: String,
    understanding: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.9f)
        ),
        shape = CircleShape
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ü§ñ",
                fontSize = 24.sp
            )
            Text(
                text = personality,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                textAlign = TextAlign.Center
            )
            LinearProgressIndicator(
                progress = understanding,
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.inversePrimary,
                trackColor = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun AdaptiveContextHints(
    context: LivingAINativeInterface.DynamicContext,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Text(
            text = "üéØ ${context.developmentPhase} | üî• ${context.userMood.name.lowercase()}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun EnergyIndicator(
    energy: Float,
    modifier: Modifier = Modifier
) {
    val energyColor = lerp(
        Color.Gray,
        Color.Green,
        energy
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .background(energyColor, CircleShape)
    ) {
        Text(
            text = "‚ö°",
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun ContextualChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color
        )
    }
}

enum class InputMode(val displayName: String, val emoji: String) {
    SHELL("Shell", "üíª"),
    AI("Chat", "ü§ñ"),
    HYBRID("Both", "üî•"),
    LEARN("Learn", "üß†")
}

fun getMessageTypeEmoji(type: LivingAINativeInterface.MessageType): String {
    return when (type) {
        LivingAINativeInterface.MessageType.SHELL_COMMAND -> "üíª"
        LivingAINativeInterface.MessageType.AI_QUESTION -> "ü§ñ"
        LivingAINativeInterface.MessageType.HYBRID_REQUEST -> "üî•"
        LivingAINativeInterface.MessageType.AMBIENT_THOUGHT -> "üí≠"
        LivingAINativeInterface.MessageType.LEARNING_MOMENT -> "üå±"
    }
}

fun getAdaptivePlaceholder(mode: InputMode, state: LivingAINativeInterface.LivingInterfaceState): String {
    return when (mode) {
        InputMode.SHELL -> "Execute command..."
        InputMode.AI -> "Ask me anything..."
        InputMode.HYBRID -> when {
            state.contextualUnderstanding > 0.7f -> "I'm ready for complex requests..."
            state.contextualUnderstanding > 0.3f -> "Command or question?"
            else -> "Tell me what you need..."
        }
        InputMode.LEARN -> "What did you learn or notice?"
    }
}

fun generateDynamicColorScheme(state: LivingAINativeInterface.LivingInterfaceState): ColorScheme {
    val baseHue = when (state.aiPersonalityActive) {
        "CodeReaver" -> 280f // Purple
        "WebNetCaste" -> 200f // Blue
        "LearningBot" -> 120f // Green
        else -> 15f // Orange (SrirachaArmy default)
    }
    
    val saturation = 0.3f + (state.energyLevel * 0.4f)
    val lightness = 0.2f + (state.contextualUnderstanding * 0.3f)
    
    return lightColorScheme(
        primary = Color.hsl(baseHue, saturation, lightness + 0.3f),
        secondary = Color.hsl(baseHue + 30f, saturation * 0.8f, lightness + 0.4f),
        tertiary = Color.hsl(baseHue + 60f, saturation * 0.6f, lightness + 0.5f)
    )
}