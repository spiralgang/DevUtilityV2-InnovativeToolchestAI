package com.spiralgang.srirachaarmy.devutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spiralgang.srirachaarmy.devutility.ai.LivingAINativeInterface
import com.spiralgang.srirachaarmy.devutility.ui.LivingDynamicInterface
import com.spiralgang.srirachaarmy.devutility.ui.DevUtilityViewModelV2
import com.spiralgang.srirachaarmy.devutility.ui.theme.SrirachaArmyTheme
import com.spiralgang.srirachaarmy.devutility.agentic.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlinx.coroutines.launch

/**
 * MainActivity for SrirachaArmy DevUtility - Living AI-Native Experience
 * 
 * This is the entry point for the intimate UI 'AI&i' AI-APK smart personal code space.
 * The entire interface IS the AI - every interaction is simultaneously:
 * - A shell command
 * - An AI conversation  
 * - A development action
 * - A learning moment
 * 
 * The Shell & Terminal IS DevUtility && SrirachaAI && AI frameworks interface.
 * This creates a truly living-code agentic agency personal dynamic interface.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: DevUtilityViewModelV2 by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("MainActivity starting - SrirachaArmy DevUtility Living AI Interface")
        
        enableEdgeToEdge()
        setContent {
            SrirachaArmyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Use the ViewModel-managed UI state
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    
                    // Show the enhanced DevUtility interface
                    DevUtilityMainInterface(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        Timber.d("Living AI-Native Interface initialized successfully")
    }
}

/**
 * The main living AI experience - this IS the DevUtility interface
 * Every pixel adapts to AI understanding and user patterns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivingAIMainExperience() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Create a mock living interface for now (until dependency injection is set up)
    val livingInterface = remember { createMockLivingInterface(context) }
    
    // Initialize the living interface
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                livingInterface.initialize()
                Timber.d("Living AI Interface initialized successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize Living AI Interface")
            }
        }
    }
    
    // The entire app is now the living, dynamic AI interface
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LivingDynamicInterface(
            livingInterface = livingInterface,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Creates a mock living interface for demonstration
 * TODO: Replace with proper dependency injection once Hilt is configured
 */
private fun createMockLivingInterface(context: android.content.Context): LivingAINativeInterface {
    // This is a simplified mock - in the real implementation this would be injected
    return MockLivingAINativeInterface(context)
}

/**
 * Mock implementation for demonstration purposes
 * Shows how the living interface would work once fully integrated
 */
private class MockLivingAINativeInterface(private val context: android.content.Context) : LivingAINativeInterface {
    
    private val _livingState = kotlinx.coroutines.flow.MutableStateFlow(
        LivingInterfaceState(
            currentMode = AgenticMode.CONVERSATIONAL_SHELL,
            aiPersonalityActive = "CodeReaver",
            contextualUnderstanding = 0.4f,
            personalAdaptation = 0.3f,
            proactiveReadiness = true,
            livingMemory = listOf(
                "🌱 Learning your development patterns...",
                "🔥 SrirachaArmy personality system activated",
                "🤖 AI-native interface ready for intimate collaboration"
            ),
            currentFocus = DevelopmentFocus.EXPLORATION,
            energyLevel = 0.8f
        )
    )
    override val livingState = _livingState.asStateFlow()
    
    private val _conversationFlow = kotlinx.coroutines.flow.MutableSharedFlow<AINativeMessage>()
    override val conversationFlow = _conversationFlow.asSharedFlow()
    
    private val _contextAwareness = kotlinx.coroutines.flow.MutableStateFlow(
        DynamicContext(
            currentProject = "DevUtilityV2.5",
            activeLanguage = "Kotlin",
            developmentPhase = "enhancement", 
            userMood = UserMood.FOCUSED,
            environmentState = mapOf(
                "terminal_ready" to true,
                "ai_online" to true,
                "sriracha_heat" to "medium"
            ),
            recentPatterns = listOf(
                "UI enhancement focus",
                "AI integration work", 
                "Living interface development"
            )
        )
    )
    override val contextAwareness = _contextAwareness.asStateFlow()
    
    override suspend fun initialize() {
        Timber.d("Mock Living AI Interface initialized")
        
        // Simulate initialization progress
        kotlinx.coroutines.delay(1000)
        
        // Add welcome message
        _conversationFlow.emit(
            AINativeMessage(
                input = "",
                interpretedAs = MessageType.AMBIENT_THOUGHT,
                aiResponse = "🌶️ Welcome to the living AI-native DevUtility experience!\n\n🤖 I'm CodeReaver, your intimate development partner. Every interaction here blends shell commands, AI conversation, and development actions into one seamless experience.\n\n💡 Try typing anything - commands, questions, or thoughts. I'll understand the context and respond appropriately.",
                suggestedActions = listOf(
                    "👋 Ask me about the codebase",
                    "💻 Execute a shell command", 
                    "🔥 Show me SrirachaArmy capabilities",
                    "🧠 Let's brainstorm improvements"
                )
            )
        )
        
        _livingState.value = _livingState.value.copy(
            contextualUnderstanding = 0.6f,
            personalAdaptation = 0.4f
        )
    }
    
    override suspend fun processAINativeInput(input: String): AINativeMessage {
        Timber.d("Processing AI-native input: $input")
        
        val messageType = when {
            input.startsWith("/") || input.contains("ls") || input.contains("cd") -> MessageType.SHELL_COMMAND
            input.contains("?") || input.contains("how") || input.contains("what") -> MessageType.AI_QUESTION
            input.contains("sriracha") || input.contains("bot") -> MessageType.AI_QUESTION
            else -> MessageType.HYBRID_REQUEST
        }
        
        val response = when (messageType) {
            MessageType.SHELL_COMMAND -> AINativeMessage(
                input = input,
                interpretedAs = messageType,
                aiResponse = "⚡ Executing: `$input`\n\n🤖 I see you're working with shell commands. The output shows your current directory structure. Based on this, I can help you navigate your development workflow more efficiently.",
                shellOutput = "Command executed successfully",
                suggestedActions = listOf(
                    "🔍 Analyze this directory",
                    "📁 Show me project structure", 
                    "⚡ Run build commands"
                )
            )
            MessageType.AI_QUESTION -> AINativeMessage(
                input = input,
                interpretedAs = messageType,
                aiResponse = "🧠 Great question! Based on our current context (working on DevUtility enhancement), let me provide a thoughtful response...\n\n${generateContextualAIResponse(input)}\n\n💡 This ties into your development patterns I've been observing.",
                suggestedActions = listOf(
                    "🔥 Deep dive into this topic",
                    "💻 Show practical examples",
                    "🌱 Learn more patterns"
                )
            )
            else -> AINativeMessage(
                input = input,
                interpretedAs = messageType,
                aiResponse = "🔥 I understand you want both action and insight. Let me handle this comprehensively...\n\n${generateHybridResponse(input)}",
                suggestedActions = listOf(
                    "⚡ Execute the action",
                    "🧠 Explain the reasoning", 
                    "🎯 Optimize this pattern"
                )
            )
        }
        
        _conversationFlow.emit(response)
        
        // Update understanding
        _livingState.value = _livingState.value.copy(
            contextualUnderstanding = (_livingState.value.contextualUnderstanding + 0.05f).coerceAtMost(1.0f),
            personalAdaptation = (_livingState.value.personalAdaptation + 0.03f).coerceAtMost(1.0f)
        )
        
        return response
    }
    
    override suspend fun getProactiveSuggestions(): List<String> {
        return listOf(
            "🌶️ Ready to add more SrirachaArmy personality to the interface?",
            "🔧 I can help optimize your development workflow",
            "🎨 Want to enhance the living UI animations?",
            "🧠 Let's explore AI-driven code suggestions"
        )
    }
    
    private fun generateContextualAIResponse(input: String): String {
        return when {
            input.contains("sriracha") -> "SrirachaArmy represents the spicy, intelligent development experience. Each bot has a unique personality and specialization, working together to create an intimate AI development partnership."
            input.contains("living") || input.contains("dynamic") -> "The living interface adapts to your patterns, learns from interactions, and evolves its personality. It's not just a tool - it's a development partner that grows with you."
            input.contains("ai") -> "Our AI integration blends multiple personalities and specialized capabilities. Every interaction teaches the system more about your development style and preferences."
            else -> "Based on your development patterns and current context, I can provide targeted insights that adapt to your specific workflow and goals."
        }
    }
    
    private fun generateHybridResponse(input: String): String {
        return "Analyzing your request in the context of the living DevUtility environment. I'll provide both immediate action and strategic insight to enhance your development experience."
    }
    
    // Stub implementations for missing base class methods
    data class LivingInterfaceState(
        val currentMode: AgenticMode = AgenticMode.CONVERSATIONAL_SHELL,
        val aiPersonalityActive: String = "CodeReaver",
        val contextualUnderstanding: Float = 0.0f,
        val personalAdaptation: Float = 0.0f,
        val proactiveReadiness: Boolean = false,
        val livingMemory: List<String> = emptyList(),
        val currentFocus: DevelopmentFocus = DevelopmentFocus.EXPLORATION,
        val energyLevel: Float = 1.0f
    )
    
    data class AINativeMessage(
        val input: String,
        val interpretedAs: MessageType,
        val aiResponse: String,
        val shellOutput: String? = null,
        val suggestedActions: List<String> = emptyList(),
        val learningInsight: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class DynamicContext(
        val currentProject: String? = null,
        val activeLanguage: String = "multi",
        val developmentPhase: String = "exploration",
        val userMood: UserMood = UserMood.FOCUSED,
        val environmentState: Map<String, Any> = emptyMap(),
        val recentPatterns: List<String> = emptyList()
    )
    
    enum class AgenticMode {
        CONVERSATIONAL_SHELL,
        PROACTIVE_ASSISTANT,
        LEARNING_OBSERVER,
        DEEP_COLLABORATION,
        INTUITIVE_AUTOMATION
    }
    
    enum class DevelopmentFocus {
        EXPLORATION,
        IMPLEMENTATION,
        DEBUGGING,
        OPTIMIZATION,
        CREATION
    }
    
    enum class UserMood {
        FOCUSED,
        EXPLORATORY,
        PROBLEM_SOLVING,
        CREATIVE,
        FRUSTRATED,
        CONFIDENT
    }
    
    enum class MessageType {
        SHELL_COMMAND,
        AI_QUESTION,
        HYBRID_REQUEST,
        AMBIENT_THOUGHT,
        LEARNING_MOMENT
    }
}

// Base interface for the living AI system
interface LivingAINativeInterface {
    val livingState: kotlinx.coroutines.flow.StateFlow<*>
    val conversationFlow: kotlinx.coroutines.flow.SharedFlow<*>
    val contextAwareness: kotlinx.coroutines.flow.StateFlow<*>
    
    suspend fun initialize()
    suspend fun processAINativeInput(input: String): Any
    suspend fun getProactiveSuggestions(): List<String>
}

/**
 * Main DevUtility interface using the ViewModel
 */
@Composable
fun DevUtilityMainInterface(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    // Simple workspace interface for now
    DevUtilityWorkspace(
        uiState = uiState,
        viewModel = viewModel,
        modifier = modifier
    )
}

/**
 * Main workspace interface
 */
@Composable
fun DevUtilityWorkspace(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    // Main workspace with tabs showing different components
    Column(modifier = modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 SrirachaArmy DevUtility v${BuildConfig.SRIRACHA_ARMY_VERSION}",
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Status indicators
                    Text(
                        text = "🤖 ${uiState.activeBotCount} bots",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    if (uiState.isAIOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🟢 AI Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Main content tabs
        var selectedTab by remember { mutableStateOf(0) }
        val tabTitles = listOf("🏠 Workspace", "⚡ Agentic", "🐧 Terminal", "🔧 Settings")
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Color(0xFFFF5722)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            0 -> WorkspacePanel(uiState, viewModel, Modifier.fillMaxSize())
            1 -> AgenticPanel(Modifier.fillMaxSize())
            2 -> TerminalPanel(uiState, viewModel, Modifier.fillMaxSize())
            3 -> SettingsPanel(uiState, Modifier.fillMaxSize())
        }
    }
}

/**
 * Main workspace panel
 */
@Composable
fun WorkspacePanel(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔥 SrirachaArmy Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Heat Level: ${uiState.currentHeatLevel}")
                    Text("Active Bots: ${uiState.activeBotCount}")
                    Text("Current Language: ${uiState.selectedLanguage}")
                }
            }
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📁 Development Environment",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Terminal Ready: ${if (uiState.terminalReady) "✅" else "❌"}")
                    Text("RootFS Ready: ${if (uiState.rootfsReady) "✅" else "❌"}")
                    Text("Editor Ready: ${if (uiState.editorReady) "✅" else "❌"}")
                    Text("Container Engine: ${if (uiState.containerEngineReady) "✅" else "❌"}")
                }
            }
        }
        
        if (uiState.openFiles.isNotEmpty()) {
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📝 Open Files",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.openFiles.forEach { file ->
                            Text(
                                text = "• $file",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Agentic systems panel
 */
@Composable
fun AgenticPanel(modifier: Modifier = Modifier) {
    val repositoryManager = remember { AgenticRepositoryManager(LocalContext.current) }
    val livingCodeAdapter = remember { LivingCodeAdapter(LocalContext.current, repositoryManager) }
    val workflowEngine = remember { AgenticWorkflowEngine(repositoryManager, livingCodeAdapter) }
    val resourceLoader = remember { DynamicResourceLoader(LocalContext.current, repositoryManager) }
    
    AgenticIntegrationInterface(
        repositoryManager = repositoryManager,
        livingCodeAdapter = livingCodeAdapter,
        workflowEngine = workflowEngine,
        resourceLoader = resourceLoader,
        modifier = modifier
    )
}

/**
 * Terminal panel
 */
@Composable
fun TerminalPanel(
    uiState: DevUtilityViewModelV2.UIState,
    viewModel: DevUtilityViewModelV2,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "🐧 Terminal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f)
            )
        ) {
            LazyColumn(
                modifier = Modifier.padding(12.dp)
            ) {
                item {
                    Text(
                        text = "Working Directory: ${uiState.currentWorkingDirectory}",
                        color = Color.Green,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                items(uiState.terminalOutput) { line ->
                    Text(
                        text = line,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                
                if (uiState.terminalOutput.isEmpty()) {
                    item {
                        Text(
                            text = "Terminal ready - Enter commands above or use agentic interface",
                            color = Color.Gray,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * Settings panel
 */
@Composable
fun SettingsPanel(
    uiState: DevUtilityViewModelV2.UIState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "⚙️ Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🤖 AI Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Current Model: ${uiState.aiModel}")
                    Text("Status: ${if (uiState.isAIOnline) "Online" else "Offline"}")
                }
            }
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🌶️ SrirachaArmy Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Heat Level: ${uiState.currentHeatLevel}")
                    Text("Coordination Pattern: ${uiState.coordinationPattern.ifEmpty { "None" }}")
                }
            }
        }
        
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔧 System Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version: ${BuildConfig.SRIRACHA_ARMY_VERSION}")
                    Text("Build: ${BuildConfig.BUILD_TIMESTAMP}")
                    Text("Available Languages: ${uiState.availableLanguages.size}")
                }
            }
        }
    }
}