package com.spiralgang.srirachaarmy.devutility.core

import com.spiralgang.srirachaarmy.devutility.ai.*
import com.spiralgang.srirachaarmy.devutility.execution.FloatWindowWatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central orchestrator integrating all SrirachaArmy bots throughout the DevUtility ecosystem
 * Rationale: 126kB file analysis reveals distributed bot integration requiring unified coordination
 */
@Singleton
class SrirachaArmyOrchestrator @Inject constructor(
    private val srirachaAgentBehaviorPrompts: SrirachaAgentBehaviorPrompts,
    private val webNetCasteAI: WebNetCasteAI,
    private val aiThinkModule: AIThinkModule,
    private val learningBot: LearningBot,
    private val srirachaGuidanceSystem: SrirachaGuidanceSystem,
    private val floatWindowWatcher: FloatWindowWatcher
) {
    
    private val _orchestratorState = MutableStateFlow(OrchestratorState.Idle)
    val orchestratorState: StateFlow<OrchestratorState> = _orchestratorState
    
    private val _activeBots = MutableStateFlow<Set<BotType>>(emptySet())
    val activeBots: StateFlow<Set<BotType>> = _activeBots
    
    private val _contextualIntelligence = MutableStateFlow<ContextualIntelligence?>(null)
    val contextualIntelligence: StateFlow<ContextualIntelligence?> = _contextualIntelligence

    /**
     * Initialize the complete SrirachaArmy ecosystem
     * Coordinates bot activation and inter-bot communication
     */
    suspend fun initializeSrirachaArmy() {
        _orchestratorState.value = OrchestratorState.Initializing
        
        try {
            Timber.d("Initializing SrirachaArmy bot ecosystem...")
            
            // Initialize core AI modules
            aiThinkModule.initialize()
            srirachaGuidanceSystem.initialize()
            
            // Initialize learning and adaptation systems
            learningBot.initialize()
            webNetCasteAI.initialize()
            
            // Initialize behavioral prompt system
            srirachaAgentBehaviorPrompts.loadBehaviorProfiles()
            
            // Initialize UI and interaction systems
            floatWindowWatcher.initialize()
            
            // Establish inter-bot communication channels
            establishBotCommunication()
            
            _orchestratorState.value = OrchestratorState.Ready
            _activeBots.value = setOf(
                BotType.AI_THINK_MODULE,
                BotType.SRIRACHA_GUIDANCE_SYSTEM,
                BotType.LEARNING_BOT,
                BotType.WEB_NET_CASTE_AI,
                BotType.FLOAT_WINDOW_WATCHER
            )
            
            Timber.d("SrirachaArmy ecosystem initialized successfully")
            
        } catch (e: Exception) {
            _orchestratorState.value = OrchestratorState.Error(e.message ?: "Initialization failed")
            Timber.e(e, "SrirachaArmy initialization failed")
        }
    }

    /**
     * Coordinate contextual intelligence across all bots
     * Enables seamless information sharing and collaborative decision-making
     * NOW LIMITED TO ONE AI OPERATION AT A TIME
     */
    suspend fun processContextualIntelligence(context: IntelligenceContext): IntelligenceResult {
        if (_orchestratorState.value !is OrchestratorState.Ready) {
            return IntelligenceResult.Error("SrirachaArmy not ready")
        }
        
        return try {
            AIInstanceManager.executeAIOperation(
                AIInstanceManager.AIOperationType.COORDINATION,
                "processContextualIntelligence"
            ) {
                // Sequential processing instead of parallel - ONE AT A TIME
                val thinkModuleResult = aiThinkModule.processContext(context)
                val guidanceResult = srirachaGuidanceSystem.provideGuidance(context) 
                val learningResult = learningBot.adaptFromContext(context)
                val webCasteResult = webNetCasteAI.analyzeWebContext(context)
                
                // Synthesize intelligence from all sources
                val synthesizedIntelligence = synthesizeIntelligence(
                    thinkModuleResult,
                    guidanceResult,
                    learningResult,
                    webCasteResult
                )
                
                _contextualIntelligence.value = synthesizedIntelligence
                
                IntelligenceResult.Success(synthesizedIntelligence)
            }
            
        } catch (e: AIOperationBlockedException) {
            Timber.w(e, "Contextual intelligence processing blocked - another AI operation in progress")
            IntelligenceResult.Error("AI operation in progress: ${e.message}")
        } catch (e: Exception) {
            Timber.e(e, "Contextual intelligence processing failed")
            IntelligenceResult.Error("Intelligence processing failed: ${e.message}")
        }
    }

    /**
     * Orchestrate bot collaboration for complex tasks
     * NOW ENFORCES SINGLE AI OPERATION CONSTRAINT
     */
    suspend fun orchestrateBotCollaboration(
        task: CollaborativeTask
    ): CollaborationResult {
        val participatingBots = determineBotParticipation(task)
        
        return try {
            AIInstanceManager.executeAIOperation(
                AIInstanceManager.AIOperationType.COORDINATION,
                "orchestrateBotCollaboration: ${task.description}"
            ) {
                val collaborationPlan = createCollaborationPlan(task, participatingBots)
                val executionResults = executeCollaborativePlan(collaborationPlan)
                
                CollaborationResult.Success(
                    task = task,
                    participatingBots = participatingBots,
                    results = executionResults
                )
            }
            
        } catch (e: AIOperationBlockedException) {
            Timber.w(e, "Bot collaboration blocked - another AI operation in progress")
            CollaborationResult.Error("AI operation in progress: ${e.message}")
        } catch (e: Exception) {
            CollaborationResult.Error("Collaboration failed: ${e.message}")
        }
    }

    private suspend fun establishBotCommunication() {
        // Set up message passing between bots
        aiThinkModule.setMessageHandler { message ->
            broadcastToRelevantBots(message, BotType.AI_THINK_MODULE)
        }
        
        srirachaGuidanceSystem.setMessageHandler { message ->
            broadcastToRelevantBots(message, BotType.SRIRACHA_GUIDANCE_SYSTEM)
        }
        
        learningBot.setMessageHandler { message ->
            broadcastToRelevantBots(message, BotType.LEARNING_BOT)
        }
        
        Timber.d("Inter-bot communication channels established")
    }

    private suspend fun broadcastToRelevantBots(message: BotMessage, sender: BotType) {
        val relevantBots = determineRelevantBots(message, sender)
        
        relevantBots.forEach { botType ->
            when (botType) {
                BotType.AI_THINK_MODULE -> aiThinkModule.receiveMessage(message)
                BotType.SRIRACHA_GUIDANCE_SYSTEM -> srirachaGuidanceSystem.receiveMessage(message)
                BotType.LEARNING_BOT -> learningBot.receiveMessage(message)
                BotType.WEB_NET_CASTE_AI -> webNetCasteAI.receiveMessage(message)
                BotType.FLOAT_WINDOW_WATCHER -> floatWindowWatcher.receiveMessage(message)
            }
        }
    }

    private fun determineRelevantBots(message: BotMessage, sender: BotType): Set<BotType> {
        return when (message.type) {
            MessageType.CODE_ANALYSIS -> setOf(BotType.AI_THINK_MODULE, BotType.SRIRACHA_GUIDANCE_SYSTEM)
            MessageType.LEARNING_UPDATE -> setOf(BotType.LEARNING_BOT, BotType.SRIRACHA_GUIDANCE_SYSTEM)
            MessageType.UI_INTERACTION -> setOf(BotType.FLOAT_WINDOW_WATCHER)
            MessageType.WEB_ANALYSIS -> setOf(BotType.WEB_NET_CASTE_AI, BotType.AI_THINK_MODULE)
            else -> emptySet()
        }
    }

    private fun synthesizeIntelligence(
        thinkResult: Any,
        guidanceResult: Any,
        learningResult: Any,
        webCasteResult: Any
    ): ContextualIntelligence {
        return ContextualIntelligence(
            analyticalInsight = thinkResult.toString(),
            guidanceRecommendation = guidanceResult.toString(),
            learningAdaptation = learningResult.toString(),
            webContextAnalysis = webCasteResult.toString(),
            synthesizedRecommendation = "Integrated intelligence from all SrirachaArmy bots",
            confidence = 0.85f
        )
    }

    private fun determineBotParticipation(task: CollaborativeTask): Set<BotType> {
        return when (task.type) {
            TaskType.CODE_DEVELOPMENT -> setOf(
                BotType.AI_THINK_MODULE,
                BotType.SRIRACHA_GUIDANCE_SYSTEM,
                BotType.LEARNING_BOT
            )
            TaskType.WEB_RESEARCH -> setOf(
                BotType.WEB_NET_CASTE_AI,
                BotType.AI_THINK_MODULE
            )
            TaskType.UI_AUTOMATION -> setOf(
                BotType.FLOAT_WINDOW_WATCHER,
                BotType.SRIRACHA_GUIDANCE_SYSTEM
            )
            else -> _activeBots.value
        }
    }

    private suspend fun createCollaborationPlan(
        task: CollaborativeTask,
        bots: Set<BotType>
    ): CollaborationPlan {
        return CollaborationPlan(
            task = task,
            participatingBots = bots,
            executionSteps = generateExecutionSteps(task, bots),
            timeline = estimateTimeline(task, bots)
        )
    }

    private suspend fun executeCollaborativePlan(plan: CollaborationPlan): Map<BotType, Any> {
        val results = mutableMapOf<BotType, Any>()
        
        plan.executionSteps.forEach { step ->
            when (step.botType) {
                BotType.AI_THINK_MODULE -> {
                    results[step.botType] = aiThinkModule.executeStep(step)
                }
                BotType.SRIRACHA_GUIDANCE_SYSTEM -> {
                    results[step.botType] = srirachaGuidanceSystem.executeStep(step)
                }
                BotType.LEARNING_BOT -> {
                    results[step.botType] = learningBot.executeStep(step)
                }
                BotType.WEB_NET_CASTE_AI -> {
                    results[step.botType] = webNetCasteAI.executeStep(step)
                }
                BotType.FLOAT_WINDOW_WATCHER -> {
                    results[step.botType] = floatWindowWatcher.executeStep(step)
                }
            }
        }
        
        return results
    }

    private fun generateExecutionSteps(task: CollaborativeTask, bots: Set<BotType>): List<ExecutionStep> {
        // Generate execution steps based on task and participating bots
        return bots.mapIndexed { index, bot ->
            ExecutionStep(
                stepId = index,
                botType = bot,
                action = "Execute ${task.description} for ${bot.name}",
                dependencies = if (index > 0) listOf(index - 1) else emptyList()
            )
        }
    }

    private fun estimateTimeline(task: CollaborativeTask, bots: Set<BotType>): Long {
        // Estimate execution timeline based on task complexity and bot count
        return task.estimatedDuration * bots.size
    }

    sealed class OrchestratorState {
        object Idle : OrchestratorState()
        object Initializing : OrchestratorState()
        object Ready : OrchestratorState()
        data class Processing(val task: String) : OrchestratorState()
        data class Error(val message: String) : OrchestratorState()
    }

    enum class BotType {
        AI_THINK_MODULE,
        SRIRACHA_GUIDANCE_SYSTEM,
        LEARNING_BOT,
        WEB_NET_CASTE_AI,
        FLOAT_WINDOW_WATCHER
    }
}

// Supporting data classes
data class IntelligenceContext(
    val type: String,
    val data: Map<String, Any>,
    val priority: Priority = Priority.NORMAL
)

data class ContextualIntelligence(
    val analyticalInsight: String,
    val guidanceRecommendation: String,
    val learningAdaptation: String,
    val webContextAnalysis: String,
    val synthesizedRecommendation: String,
    val confidence: Float
)

data class CollaborativeTask(
    val type: TaskType,
    val description: String,
    val estimatedDuration: Long,
    val priority: Priority
)

data class CollaborationPlan(
    val task: CollaborativeTask,
    val participatingBots: Set<SrirachaArmyOrchestrator.BotType>,
    val executionSteps: List<ExecutionStep>,
    val timeline: Long
)

data class ExecutionStep(
    val stepId: Int,
    val botType: SrirachaArmyOrchestrator.BotType,
    val action: String,
    val dependencies: List<Int>
)

data class BotMessage(
    val type: MessageType,
    val content: String,
    val metadata: Map<String, Any> = emptyMap()
)

sealed class IntelligenceResult {
    data class Success(val intelligence: ContextualIntelligence) : IntelligenceResult()
    data class Error(val message: String) : IntelligenceResult()
}

sealed class CollaborationResult {
    data class Success(
        val task: CollaborativeTask,
        val participatingBots: Set<SrirachaArmyOrchestrator.BotType>,
        val results: Map<SrirachaArmyOrchestrator.BotType, Any>
    ) : CollaborationResult()
    data class Error(val message: String) : CollaborationResult()
}

enum class TaskType {
    CODE_DEVELOPMENT,
    WEB_RESEARCH,
    UI_AUTOMATION,
    DATA_ANALYSIS,
    LEARNING_ADAPTATION
}

enum class MessageType {
    CODE_ANALYSIS,
    LEARNING_UPDATE,
    UI_INTERACTION,
    WEB_ANALYSIS,
    GUIDANCE_REQUEST,
    STATUS_UPDATE
}

enum class Priority {
    LOW, NORMAL, HIGH, CRITICAL
}