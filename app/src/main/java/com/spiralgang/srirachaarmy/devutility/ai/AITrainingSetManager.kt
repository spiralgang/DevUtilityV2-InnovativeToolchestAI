package com.spiralgang.srirachaarmy.devutility.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Training Set Manager with system prompts
 * Part of DevUtility V2.5 AI enhancement features
 */
@Singleton
class AITrainingSetManager @Inject constructor() {
    
    private val systemPrompts = mutableMapOf<String, SystemPrompt>()
    private val trainingData = mutableMapOf<String, TrainingDataset>()
    private var isInitialized = false
    
    data class SystemPrompt(
        val id: String,
        val title: String,
        val content: String,
        val category: String,
        val priority: Int,
        val isActive: Boolean = true,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val usage_count: Int = 0
    )
    
    data class TrainingDataset(
        val id: String,
        val name: String,
        val language: String,
        val category: String,
        val examples: List<TrainingExample>,
        val version: String,
        val isActive: Boolean = true,
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    data class TrainingExample(
        val input: String,
        val expectedOutput: String,
        val context: String,
        val difficulty: String, // EASY, MEDIUM, HARD
        val tags: List<String>
    )
    
    /**
     * Initialize AI training sets and system prompts
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) {
                Timber.d("AI Training Set Manager already initialized")
                return@withContext
            }
            
            Timber.d("Initializing AI Training Set Manager")
            
            // Load default system prompts
            loadDefaultSystemPrompts()
            
            // Load default training datasets
            loadDefaultTrainingDatasets()
            
            isInitialized = true
            
            Timber.d("AI Training Set Manager initialized with ${systemPrompts.size} prompts and ${trainingData.size} datasets")
            
        } catch (e: Exception) {
            Timber.e(e, "AI Training Set Manager initialization failed")
        }
    }
    
    /**
     * Add or update system prompt
     */
    fun addSystemPrompt(prompt: SystemPrompt) {
        systemPrompts[prompt.id] = prompt
        Timber.d("System prompt added: ${prompt.title}")
    }
    
    /**
     * Get system prompt by ID
     */
    fun getSystemPrompt(id: String): SystemPrompt? {
        return systemPrompts[id]
    }
    
    /**
     * Get all active system prompts for a category
     */
    fun getSystemPromptsForCategory(category: String): List<SystemPrompt> {
        return systemPrompts.values
            .filter { it.category == category && it.isActive }
            .sortedByDescending { it.priority }
    }
    
    /**
     * Get training dataset for specific language and context
     */
    fun getTrainingDataset(language: String, category: String): TrainingDataset? {
        return trainingData.values
            .firstOrNull { it.language == language && it.category == category && it.isActive }
    }
    
    /**
     * Add training example to dataset
     */
    fun addTrainingExample(datasetId: String, example: TrainingExample): Boolean {
        val dataset = trainingData[datasetId]
        return if (dataset != null) {
            val updatedExamples = dataset.examples + example
            val updatedDataset = dataset.copy(
                examples = updatedExamples,
                lastUpdated = System.currentTimeMillis()
            )
            trainingData[datasetId] = updatedDataset
            Timber.d("Training example added to dataset: $datasetId")
            true
        } else {
            Timber.w("Dataset not found: $datasetId")
            false
        }
    }
    
    /**
     * Generate contextual system prompt for AI interaction
     */
    suspend fun generateContextualPrompt(
        language: String,
        context: String,
        userLevel: String = "intermediate"
    ): String = withContext(Dispatchers.IO) {
        try {
            val basePrompts = getSystemPromptsForCategory("base")
            val languagePrompts = getSystemPromptsForCategory("language_$language")
            val contextPrompts = getSystemPromptsForCategory("context_$context")
            val levelPrompts = getSystemPromptsForCategory("level_$userLevel")
            
            val promptBuilder = StringBuilder()
            
            // Add base system behavior
            basePrompts.take(1).forEach { prompt ->
                promptBuilder.append(prompt.content).append("\n\n")
            }
            
            // Add language-specific guidance
            languagePrompts.take(1).forEach { prompt ->
                promptBuilder.append(prompt.content).append("\n\n")
            }
            
            // Add context-specific instructions
            contextPrompts.take(1).forEach { prompt ->
                promptBuilder.append(prompt.content).append("\n\n")
            }
            
            // Add user level adjustments
            levelPrompts.take(1).forEach { prompt ->
                promptBuilder.append(prompt.content).append("\n\n")
            }
            
            val finalPrompt = promptBuilder.toString().trim()
            Timber.d("Generated contextual prompt for $language/$context/$userLevel")
            
            return@withContext finalPrompt
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate contextual prompt")
            return@withContext getDefaultPrompt()
        }
    }
    
    /**
     * Update training data based on user feedback
     */
    suspend fun updateBasedOnFeedback(
        language: String,
        context: String,
        input: String,
        output: String,
        feedback: String,
        rating: Int // 1-5
    ) = withContext(Dispatchers.IO) {
        try {
            if (rating >= 4) {
                // Good feedback - add as positive example
                val example = TrainingExample(
                    input = input,
                    expectedOutput = output,
                    context = context,
                    difficulty = inferDifficulty(input),
                    tags = extractTags(input, context)
                )
                
                val datasetId = "${language}_${context}"
                addTrainingExample(datasetId, example)
                
                Timber.d("Positive feedback incorporated into training data")
            } else {
                // Poor feedback - log for improvement
                Timber.w("Poor feedback received - Input: $input, Feedback: $feedback, Rating: $rating")
                // In a real implementation, this would be used to update the model
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to update training data based on feedback")
        }
    }
    
    /**
     * Get training statistics
     */
    fun getTrainingStatistics(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        stats["total_prompts"] = systemPrompts.size
        stats["active_prompts"] = systemPrompts.values.count { it.isActive }
        stats["total_datasets"] = trainingData.size
        stats["active_datasets"] = trainingData.values.count { it.isActive }
        stats["total_examples"] = trainingData.values.sumOf { it.examples.size }
        
        val languageDistribution = trainingData.values
            .groupBy { it.language }
            .mapValues { it.value.size }
        stats["language_distribution"] = languageDistribution
        
        val categoryDistribution = trainingData.values
            .groupBy { it.category }
            .mapValues { it.value.size }
        stats["category_distribution"] = categoryDistribution
        
        return stats
    }
    
    // Private helper methods
    private fun loadDefaultSystemPrompts() {
        // Base AI behavior prompts
        addSystemPrompt(SystemPrompt(
            id = "base_assistant",
            title = "Base AI Assistant",
            content = """You are an expert programming assistant for DevUtility IDE. You provide helpful, accurate, and context-aware coding assistance. Always:
- Explain your reasoning clearly
- Provide working code examples when possible
- Consider security and best practices
- Adapt your response to the user's skill level
- Be concise but comprehensive""",
            category = "base",
            priority = 100
        ))
        
        // Language-specific prompts
        addSystemPrompt(SystemPrompt(
            id = "kotlin_expert",
            title = "Kotlin Expert",
            content = """You are a Kotlin expert. Focus on:
- Idiomatic Kotlin patterns and best practices
- Null safety and type safety
- Coroutines for asynchronous programming
- Android-specific Kotlin features
- Functional programming concepts
- Performance optimizations""",
            category = "language_kotlin",
            priority = 90
        ))
        
        addSystemPrompt(SystemPrompt(
            id = "java_expert",
            title = "Java Expert", 
            content = """You are a Java expert. Focus on:
- Object-oriented design principles
- SOLID principles and design patterns
- Modern Java features (8+)
- Memory management and garbage collection
- Concurrency and threading
- Enterprise Java patterns""",
            category = "language_java",
            priority = 90
        ))
        
        // Context-specific prompts
        addSystemPrompt(SystemPrompt(
            id = "code_review_context",
            title = "Code Review Context",
            content = """When reviewing code, focus on:
- Code quality and readability
- Security vulnerabilities
- Performance implications
- Maintainability and scalability
- Adherence to coding standards
- Potential bugs and edge cases""",
            category = "context_code_review",
            priority = 85
        ))
        
        addSystemPrompt(SystemPrompt(
            id = "debugging_context",
            title = "Debugging Context",
            content = """When helping with debugging:
- Ask for stack traces and error messages
- Suggest systematic debugging approaches
- Recommend logging and monitoring strategies
- Help identify root causes, not just symptoms
- Provide prevention strategies""",
            category = "context_debugging",
            priority = 85
        ))
        
        // User level prompts
        addSystemPrompt(SystemPrompt(
            id = "beginner_level",
            title = "Beginner Level",
            content = """For beginner users:
- Explain concepts step by step
- Provide more detailed explanations
- Include learning resources and references
- Use simpler vocabulary
- Show multiple examples""",
            category = "level_beginner",
            priority = 70
        ))
        
        addSystemPrompt(SystemPrompt(
            id = "advanced_level",
            title = "Advanced Level",
            content = """For advanced users:
- Be more concise and technical
- Focus on edge cases and optimization
- Discuss architectural implications
- Reference advanced concepts and patterns
- Provide alternative approaches""",
            category = "level_advanced",
            priority = 70
        ))
    }
    
    private fun loadDefaultTrainingDatasets() {
        // Kotlin training dataset
        val kotlinDataset = TrainingDataset(
            id = "kotlin_general",
            name = "Kotlin General Programming",
            language = "kotlin",
            category = "general",
            examples = listOf(
                TrainingExample(
                    input = "How do I create a nullable variable in Kotlin?",
                    expectedOutput = "In Kotlin, you can create a nullable variable by adding a ? after the type:\n```kotlin\nvar name: String? = null\nval age: Int? = null\n```",
                    context = "basic_syntax",
                    difficulty = "EASY",
                    tags = listOf("nullable", "variables", "syntax")
                ),
                TrainingExample(
                    input = "Show me how to use coroutines for network calls",
                    expectedOutput = "Here's how to use coroutines for network calls:\n```kotlin\nclass NetworkService {\n    suspend fun fetchData(): String = withContext(Dispatchers.IO) {\n        // Network call here\n        \"data\"\n    }\n}\n```",
                    context = "async_programming",
                    difficulty = "MEDIUM",
                    tags = listOf("coroutines", "network", "async")
                )
            ),
            version = "1.0"
        )
        trainingData["kotlin_general"] = kotlinDataset
        
        // Java training dataset
        val javaDataset = TrainingDataset(
            id = "java_general",
            name = "Java General Programming",
            language = "java",
            category = "general",
            examples = listOf(
                TrainingExample(
                    input = "How do I create a singleton class in Java?",
                    expectedOutput = "Here's a thread-safe singleton pattern in Java:\n```java\npublic class Singleton {\n    private static volatile Singleton instance;\n    \n    private Singleton() {}\n    \n    public static Singleton getInstance() {\n        if (instance == null) {\n            synchronized (Singleton.class) {\n                if (instance == null) {\n                    instance = new Singleton();\n                }\n            }\n        }\n        return instance;\n    }\n}\n```",
                    context = "design_patterns",
                    difficulty = "MEDIUM",
                    tags = listOf("singleton", "thread-safety", "patterns")
                )
            ),
            version = "1.0"
        )
        trainingData["java_general"] = javaDataset
    }
    
    private fun getDefaultPrompt(): String {
        return "You are a helpful programming assistant. Provide clear, accurate, and helpful responses to coding questions."
    }
    
    private fun inferDifficulty(input: String): String {
        val complexKeywords = listOf("architecture", "optimization", "performance", "concurrent", "async", "advanced")
        val basicKeywords = listOf("how to", "what is", "example", "simple", "basic")
        
        val inputLower = input.lowercase()
        
        return when {
            complexKeywords.any { inputLower.contains(it) } -> "HARD"
            basicKeywords.any { inputLower.contains(it) } -> "EASY"
            else -> "MEDIUM"
        }
    }
    
    private fun extractTags(input: String, context: String): List<String> {
        val tags = mutableListOf<String>()
        
        // Add context as a tag
        tags.add(context)
        
        // Extract programming language mentions
        val languages = listOf("kotlin", "java", "python", "javascript")
        languages.forEach { lang ->
            if (input.lowercase().contains(lang)) {
                tags.add(lang)
            }
        }
        
        // Extract concept mentions
        val concepts = listOf("function", "class", "variable", "loop", "array", "list", "map")
        concepts.forEach { concept ->
            if (input.lowercase().contains(concept)) {
                tags.add(concept)
            }
        }
        
        return tags.distinct()
    }
}