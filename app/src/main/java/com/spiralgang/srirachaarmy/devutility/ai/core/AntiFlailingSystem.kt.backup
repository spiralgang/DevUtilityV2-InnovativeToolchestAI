package com.spiralgang.srirachaarmy.devutility.ai.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AntiFlailingSystem - Prevents agent flailing through structured decision-making
 * 
 * Based on analysis of real-world agent flailing case study where AI systems
 * reactively add capabilities (internet browsing, facial recognition) without
 * proper integration, leading to solution sprawl and inconsistent behavior.
 * 
 * This system implements specific anti-flailing patterns:
 * 1. Knowledge Gap Detection & Recovery Protocols
 * 2. Capability Assessment Before Tool Addition
 * 3. Structured Expansion Decision Framework
 * 4. Solution Coherence Validation
 * 
 * Prevents common flailing patterns:
 * - Reactive tool addition without integration planning
 * - Feature creep without architectural consideration
 * - Knowledge gap panic leading to unfocused capability expansion
 * - Solution sprawl without coherent strategy
 */
@Singleton
class AntiFlailingSystem @Inject constructor() {

    /**
     * Flailing detection states
     */
    enum class FlailingRisk {
        LOW,           // Structured decision-making in progress
        MODERATE,      // Some reactive patterns detected
        HIGH,          // Multiple reactive decisions without integration
        CRITICAL       // Uncontrolled capability expansion detected
    }

    /**
     * Knowledge gap analysis result
     */
    data class KnowledgeGapAnalysis(
        val gapType: GapType,
        val severity: Float,  // 0.0 to 1.0
        val existingCapabilities: List<String>,
        val requiredCapabilities: List<String>,
        val recoveryStrategies: List<RecoveryStrategy>,
        val preventiveActions: List<String>
    )

    enum class GapType {
        KNOWLEDGE_INSUFFICIENT,     // PRE-training knowledge gap
        TOOL_MISSING,              // Missing specific tool/capability
        INTEGRATION_INCOMPLETE,    // Tools exist but not properly integrated
        STRATEGY_UNCLEAR,          // Problem-solving approach undefined
        CONTEXT_MISSING           // Insufficient environmental context
    }

    data class RecoveryStrategy(
        val type: RecoveryType,
        val description: String,
        val requiredResources: List<String>,
        val integrationComplexity: Float,  // 0.0 to 1.0
        val riskLevel: Float,             // 0.0 to 1.0
        val expectedOutcome: String
    )

    enum class RecoveryType {
        USE_EXISTING_TOOLS,        // Leverage current capabilities better
        ENHANCE_INTEGRATION,       // Improve existing tool coordination
        STRUCTURED_EXPANSION,      // Add new capability with full integration
        KNOWLEDGE_SYNTHESIS,       // Combine existing knowledge differently
        CONTEXT_ENRICHMENT,        // Gather more environmental context
        ABORT_AND_REPLAN          // Current approach is fundamentally flawed
    }

    /**
     * Capability expansion decision framework
     */
    data class ExpansionDecision(
        val shouldExpand: Boolean,
        val expansionType: ExpansionType,
        val justification: String,
        val integrationPlan: List<String>,
        val riskMitigation: List<String>,
        val successMetrics: List<String>
    )

    enum class ExpansionType {
        NO_EXPANSION_NEEDED,       // Current capabilities sufficient
        TARGETED_ENHANCEMENT,      // Specific improvement to existing system
        CONTROLLED_ADDITION,       // New capability with full integration
        STRATEGIC_PIVOT,           // Fundamental approach change needed
        EMERGENCY_EXPANSION        // Critical gap requiring immediate action
    }

    private val _flailingRisk = MutableStateFlow(FlailingRisk.LOW)
    val flailingRisk: StateFlow<FlailingRisk> = _flailingRisk.asStateFlow()

    private val _recentDecisions = MutableStateFlow<List<String>>(emptyList())
    private val _capabilityAdditions = MutableStateFlow<List<String>>(emptyList())

    /**
     * Analyze knowledge gap and provide structured recovery options
     * 
     * Based on real-world flailing example where AI needed "internet browsing"
     * and "facial recognition" - this method provides structured analysis
     * to prevent reactive capability addition.
     */
    fun analyzeKnowledgeGap(
        currentCapabilities: List<String>,
        requiredTask: String,
        contextInfo: Map<String, Any>
    ): KnowledgeGapAnalysis {
        
        Timber.d("🔍 Analyzing knowledge gap for task: $requiredTask")
        
        // Analyze what's actually needed vs. what's available
        val missingCapabilities = analyzeMissingCapabilities(currentCapabilities, requiredTask)
        val gapSeverity = calculateGapSeverity(missingCapabilities, requiredTask)
        val gapType = determineGapType(missingCapabilities, currentCapabilities, contextInfo)
        
        // Generate recovery strategies based on gap analysis
        val recoveryStrategies = generateRecoveryStrategies(gapType, missingCapabilities, currentCapabilities)
        
        // Provide preventive actions for future similar situations
        val preventiveActions = generatePreventiveActions(gapType, requiredTask)
        
        val analysis = KnowledgeGapAnalysis(
            gapType = gapType,
            severity = gapSeverity,
            existingCapabilities = currentCapabilities,
            requiredCapabilities = missingCapabilities,
            recoveryStrategies = recoveryStrategies,
            preventiveActions = preventiveActions
        )
        
        Timber.d("📊 Knowledge gap analysis: ${gapType.name}, severity: $gapSeverity")
        return analysis
    }

    /**
     * Evaluate whether capability expansion is warranted
     * 
     * Prevents reactive additions like "add internet browsing" without
     * considering integration complexity and architectural impact.
     */
    fun evaluateCapabilityExpansion(
        proposedCapability: String,
        justification: String,
        currentSystem: Map<String, Any>
    ): ExpansionDecision {
        
        Timber.d("🤔 Evaluating capability expansion: $proposedCapability")
        
        // Check if expansion is actually needed
        val existingAlternatives = findExistingAlternatives(proposedCapability, currentSystem)
        if (existingAlternatives.isNotEmpty()) {
            return ExpansionDecision(
                shouldExpand = false,
                expansionType = ExpansionType.NO_EXPANSION_NEEDED,
                justification = "Existing capabilities can address need: ${existingAlternatives.joinToString()}",
                integrationPlan = listOf("Enhance coordination of existing tools"),
                riskMitigation = emptyList(),
                successMetrics = listOf("Task completion with existing tools")
            )
        }
        
        // Analyze integration complexity and architectural impact
        val integrationComplexity = assessIntegrationComplexity(proposedCapability, currentSystem)
        val architecturalImpact = assessArchitecturalImpact(proposedCapability, currentSystem)
        
        // Determine expansion type and create integration plan
        val expansionType = determineExpansionType(integrationComplexity, architecturalImpact, justification)
        val integrationPlan = createIntegrationPlan(proposedCapability, expansionType, currentSystem)
        val riskMitigation = identifyRiskMitigation(proposedCapability, expansionType)
        val successMetrics = defineSuccessMetrics(proposedCapability, expansionType)
        
        val decision = ExpansionDecision(
            shouldExpand = expansionType != ExpansionType.NO_EXPANSION_NEEDED,
            expansionType = expansionType,
            justification = justification,
            integrationPlan = integrationPlan,
            riskMitigation = riskMitigation,
            successMetrics = successMetrics
        )
        
        // Track decision for flailing risk assessment
        trackExpansionDecision(proposedCapability, decision)
        
        Timber.d("⚖️ Expansion decision: ${decision.expansionType.name}, should expand: ${decision.shouldExpand}")
        return decision
    }

    /**
     * Detect potential flailing patterns in recent decisions
     */
    fun assessFlailingRisk(recentDecisions: List<String>, systemMetrics: Map<String, Float>): FlailingRisk {
        val reactiveDecisions = countReactiveDecisions(recentDecisions)
        val capabilityAdditionRate = calculateCapabilityAdditionRate()
        val integrationCompleteness = systemMetrics["integration_completeness"] ?: 0.5f
        val decisionCoherence = assessDecisionCoherence(recentDecisions)
        
        val riskScore = calculateFlailingRiskScore(
            reactiveDecisions,
            capabilityAdditionRate, 
            integrationCompleteness,
            decisionCoherence
        )
        
        val risk = when {
            riskScore < 0.3f -> FlailingRisk.LOW
            riskScore < 0.6f -> FlailingRisk.MODERATE
            riskScore < 0.8f -> FlailingRisk.HIGH
            else -> FlailingRisk.CRITICAL
        }
        
        _flailingRisk.value = risk
        
        if (risk >= FlailingRisk.HIGH) {
            Timber.w("⚠️ High flailing risk detected: score=$riskScore, reactive_decisions=$reactiveDecisions")
        }
        
        return risk
    }

    /**
     * Generate anti-flailing recommendations
     */
    fun generateAntiFlailingRecommendations(
        currentRisk: FlailingRisk,
        gapAnalysis: KnowledgeGapAnalysis,
        systemState: Map<String, Any>
    ): List<String> {
        
        val recommendations = mutableListOf<String>()
        
        when (currentRisk) {
            FlailingRisk.LOW -> {
                recommendations.add("Continue structured decision-making approach")
                recommendations.add("Maintain integration-first mindset for new capabilities")
            }
            FlailingRisk.MODERATE -> {
                recommendations.add("Review recent decisions for reactive patterns")
                recommendations.add("Strengthen capability assessment before expansion")
                recommendations.add("Focus on integration completeness over feature addition")
            }
            FlailingRisk.HIGH -> {
                recommendations.add("PAUSE new capability additions until integration improves")
                recommendations.add("Conduct comprehensive system architecture review")
                recommendations.add("Implement mandatory integration planning for all expansions")
                recommendations.add("Focus on optimizing existing tool coordination")
            }
            FlailingRisk.CRITICAL -> {
                recommendations.add("IMMEDIATE HALT on all capability expansion")
                recommendations.add("Emergency system coherence audit required")
                recommendations.add("Revert recent reactive additions until proper integration")
                recommendations.add("Implement strict governance for future capability decisions")
            }
        }
        
        // Add specific recommendations based on gap analysis
        recommendations.addAll(gapAnalysis.preventiveActions)
        
        return recommendations
    }

    // Private helper methods
    
    private fun analyzeMissingCapabilities(current: List<String>, task: String): List<String> {
        // Analyze what capabilities are actually missing for the task
        // This is a simplified implementation - in practice would use NLP analysis
        val taskRequirements = extractTaskRequirements(task)
        return taskRequirements.filter { requirement ->
            !current.any { capability -> 
                capability.contains(requirement, ignoreCase = true) ||
                isCapabilityEquivalent(capability, requirement)
            }
        }
    }
    
    private fun extractTaskRequirements(task: String): List<String> {
        // Extract requirements from task description
        val requirements = mutableListOf<String>()
        
        when {
            task.contains("internet", ignoreCase = true) || task.contains("web", ignoreCase = true) -> {
                requirements.add("web_access")
                requirements.add("information_retrieval")
            }
            task.contains("facial", ignoreCase = true) || task.contains("recognition", ignoreCase = true) -> {
                requirements.add("image_analysis")
                requirements.add("pattern_recognition")
                requirements.add("identity_verification")
            }
            task.contains("security", ignoreCase = true) || task.contains("fraud", ignoreCase = true) -> {
                requirements.add("security_analysis")
                requirements.add("threat_detection")
            }
        }
        
        return requirements
    }
    
    private fun isCapabilityEquivalent(capability: String, requirement: String): Boolean {
        val equivalents = mapOf(
            "web_access" to listOf("web_search", "information_retrieval", "online_research"),
            "image_analysis" to listOf("computer_vision", "visual_processing", "pattern_recognition"),
            "security_analysis" to listOf("threat_detection", "vulnerability_assessment", "security_audit")
        )
        
        return equivalents[requirement]?.any { equivalent ->
            capability.contains(equivalent, ignoreCase = true)
        } ?: false
    }
    
    private fun calculateGapSeverity(missingCapabilities: List<String>, task: String): Float {
        if (missingCapabilities.isEmpty()) return 0.0f
        
        val criticalCapabilities = listOf("security_analysis", "threat_detection")
        val criticalMissing = missingCapabilities.intersect(criticalCapabilities).size
        
        return minOf(1.0f, (missingCapabilities.size * 0.3f) + (criticalMissing * 0.4f))
    }
    
    private fun determineGapType(
        missing: List<String>, 
        current: List<String>, 
        context: Map<String, Any>
    ): GapType {
        return when {
            missing.isEmpty() && current.isNotEmpty() -> GapType.INTEGRATION_INCOMPLETE
            missing.isNotEmpty() && current.isEmpty() -> GapType.KNOWLEDGE_INSUFFICIENT
            missing.size > current.size -> GapType.TOOL_MISSING
            context.isEmpty() -> GapType.CONTEXT_MISSING
            else -> GapType.STRATEGY_UNCLEAR
        }
    }
    
    private fun generateRecoveryStrategies(
        gapType: GapType,
        missing: List<String>,
        current: List<String>
    ): List<RecoveryStrategy> {
        
        val strategies = mutableListOf<RecoveryStrategy>()
        
        when (gapType) {
            GapType.TOOL_MISSING -> {
                strategies.add(RecoveryStrategy(
                    RecoveryType.USE_EXISTING_TOOLS,
                    "Leverage existing capabilities more effectively before adding new tools",
                    current,
                    0.2f,
                    0.1f,
                    "Task completion with enhanced existing tool coordination"
                ))
                strategies.add(RecoveryStrategy(
                    RecoveryType.STRUCTURED_EXPANSION,
                    "Add missing capability with full integration planning",
                    missing + listOf("integration_framework"),
                    0.8f,
                    0.6f,
                    "Robust new capability properly integrated into system"
                ))
            }
            GapType.INTEGRATION_INCOMPLETE -> {
                strategies.add(RecoveryStrategy(
                    RecoveryType.ENHANCE_INTEGRATION,
                    "Improve coordination between existing tools",
                    listOf("integration_framework", "coordination_layer"),
                    0.5f,
                    0.3f,
                    "Seamless tool coordination and improved effectiveness"
                ))
            }
            GapType.KNOWLEDGE_INSUFFICIENT -> {
                strategies.add(RecoveryStrategy(
                    RecoveryType.KNOWLEDGE_SYNTHESIS,
                    "Combine existing knowledge in new ways",
                    listOf("knowledge_base", "synthesis_engine"),
                    0.3f,
                    0.2f,
                    "Enhanced problem-solving with existing knowledge"
                ))
            }
            else -> {
                strategies.add(RecoveryStrategy(
                    RecoveryType.CONTEXT_ENRICHMENT,
                    "Gather more environmental and situational context",
                    listOf("context_analysis", "environment_awareness"),
                    0.4f,
                    0.2f,
                    "Better understanding enabling effective tool selection"
                ))
            }
        }
        
        return strategies
    }
    
    private fun generatePreventiveActions(gapType: GapType, task: String): List<String> {
        return when (gapType) {
            GapType.TOOL_MISSING -> listOf(
                "Implement capability assessment before task execution",
                "Create tool gap analysis framework",
                "Establish integration requirements for new tools"
            )
            GapType.INTEGRATION_INCOMPLETE -> listOf(
                "Strengthen inter-tool communication protocols",
                "Implement coordination effectiveness metrics",
                "Regular integration health checks"
            )
            GapType.KNOWLEDGE_INSUFFICIENT -> listOf(
                "Expand PRE-training knowledge base",
                "Implement knowledge synthesis capabilities",
                "Create learning feedback loops"
            )
            else -> listOf(
                "Enhance context gathering mechanisms",
                "Implement proactive environment analysis",
                "Create situational awareness frameworks"
            )
        }
    }
    
    private fun findExistingAlternatives(proposedCapability: String, currentSystem: Map<String, Any>): List<String> {
        val alternatives = mutableListOf<String>()
        
        @Suppress("UNCHECKED_CAST")
        val currentCapabilities = currentSystem["capabilities"] as? List<String> ?: emptyList()
        
        when (proposedCapability.lowercase()) {
            "internet_browsing", "web_access" -> {
                if (currentCapabilities.any { it.contains("web") || it.contains("search") }) {
                    alternatives.add("WebNetCasteAI for structured web analysis")
                }
                if (currentCapabilities.any { it.contains("knowledge") || it.contains("training") }) {
                    alternatives.add("PRE-training knowledge base for information needs")
                }
            }
            "facial_recognition" -> {
                if (currentCapabilities.any { it.contains("vision") || it.contains("image") }) {
                    alternatives.add("Existing computer vision capabilities")
                }
                if (currentCapabilities.any { it.contains("pattern") || it.contains("recognition") }) {
                    alternatives.add("Pattern recognition through LearningBot")
                }
            }
        }
        
        return alternatives
    }
    
    private fun assessIntegrationComplexity(capability: String, currentSystem: Map<String, Any>): Float {
        // Simple heuristic for integration complexity
        return when (capability.lowercase()) {
            "internet_browsing" -> 0.7f  // High - requires network access, security, rate limiting
            "facial_recognition" -> 0.8f // Very high - requires ML models, privacy considerations
            "web_search" -> 0.5f         // Medium - can build on existing capabilities
            else -> 0.4f
        }
    }
    
    private fun assessArchitecturalImpact(capability: String, currentSystem: Map<String, Any>): Float {
        // Assess how much the new capability would change the overall architecture
        return when (capability.lowercase()) {
            "internet_browsing" -> 0.6f  // Medium-high impact on security and data flow
            "facial_recognition" -> 0.9f // Very high - new ML pipeline, privacy framework
            else -> 0.3f
        }
    }
    
    private fun determineExpansionType(
        integrationComplexity: Float,
        architecturalImpact: Float,
        justification: String
    ): ExpansionType {
        val overallComplexity = (integrationComplexity + architecturalImpact) / 2f
        val urgencyKeywords = listOf("urgent", "critical", "emergency", "immediate")
        val isUrgent = urgencyKeywords.any { justification.contains(it, ignoreCase = true) }
        
        return when {
            overallComplexity < 0.3f -> ExpansionType.TARGETED_ENHANCEMENT
            overallComplexity < 0.6f -> ExpansionType.CONTROLLED_ADDITION
            overallComplexity < 0.8f && isUrgent -> ExpansionType.EMERGENCY_EXPANSION
            overallComplexity >= 0.8f -> ExpansionType.STRATEGIC_PIVOT
            else -> ExpansionType.CONTROLLED_ADDITION
        }
    }
    
    private fun createIntegrationPlan(
        capability: String,
        expansionType: ExpansionType,
        currentSystem: Map<String, Any>
    ): List<String> {
        
        val basePlan = listOf(
            "1. Define capability interface and contracts",
            "2. Assess security and privacy implications",
            "3. Create integration layer with existing systems",
            "4. Implement capability with comprehensive testing",
            "5. Monitor integration effectiveness and adjust"
        )
        
        return when (expansionType) {
            ExpansionType.CONTROLLED_ADDITION -> basePlan + listOf(
                "6. Gradual rollout with fallback mechanisms",
                "7. Performance impact assessment",
                "8. User experience validation"
            )
            ExpansionType.STRATEGIC_PIVOT -> listOf(
                "1. Comprehensive architecture review",
                "2. Impact analysis on all existing systems",
                "3. Phased implementation with milestone validation",
                "4. Extensive testing and validation framework",
                "5. Migration strategy for existing functionality"
            )
            ExpansionType.EMERGENCY_EXPANSION -> listOf(
                "1. Rapid prototype with minimal viable integration",
                "2. Essential security validation only",
                "3. Deploy with monitoring and quick rollback",
                "4. Post-deployment proper integration planning"
            )
            else -> basePlan
        }
    }
    
    private fun identifyRiskMitigation(capability: String, expansionType: ExpansionType): List<String> {
        val baseRisks = listOf(
            "Integration failure - implement comprehensive testing",
            "Performance degradation - establish performance baselines",
            "Security vulnerabilities - conduct security review"
        )
        
        return when (capability.lowercase()) {
            "internet_browsing" -> baseRisks + listOf(
                "Network security risks - implement request filtering and rate limiting",
                "Data privacy - ensure no sensitive data in requests",
                "Reliability issues - implement timeout and fallback mechanisms"
            )
            "facial_recognition" -> baseRisks + listOf(
                "Privacy violations - implement consent and data protection",
                "Bias and accuracy issues - validate on diverse datasets", 
                "Regulatory compliance - ensure GDPR/privacy law compliance"
            )
            else -> baseRisks
        }
    }
    
    private fun defineSuccessMetrics(capability: String, expansionType: ExpansionType): List<String> {
        return listOf(
            "Integration completeness: 100% of planned interfaces implemented",
            "Performance impact: <10% degradation in existing functionality",
            "Error rate: <5% for new capability operations",
            "User satisfaction: Positive feedback on new capability",
            "System stability: No increase in system errors post-deployment"
        )
    }
    
    private fun trackExpansionDecision(capability: String, decision: ExpansionDecision) {
        val recentDecisions = _recentDecisions.value.toMutableList()
        recentDecisions.add("$capability:${decision.expansionType.name}")
        
        // Keep only last 10 decisions
        if (recentDecisions.size > 10) {
            recentDecisions.removeAt(0)
        }
        
        _recentDecisions.value = recentDecisions
        
        if (decision.shouldExpand) {
            val additions = _capabilityAdditions.value.toMutableList()
            additions.add(capability)
            if (additions.size > 20) {
                additions.removeAt(0)
            }
            _capabilityAdditions.value = additions
        }
    }
    
    private fun countReactiveDecisions(recentDecisions: List<String>): Int {
        val reactivePatterns = listOf("EMERGENCY_", "IMMEDIATE_", "QUICK_", "RAPID_")
        return recentDecisions.count { decision ->
            reactivePatterns.any { pattern -> decision.contains(pattern) }
        }
    }
    
    private fun calculateCapabilityAdditionRate(): Float {
        val additions = _capabilityAdditions.value
        return if (additions.size >= 5) {
            // High addition rate indicates potential flailing
            minOf(1.0f, additions.size / 10.0f)
        } else {
            additions.size / 10.0f
        }
    }
    
    private fun assessDecisionCoherence(recentDecisions: List<String>): Float {
        if (recentDecisions.size < 3) return 1.0f
        
        // Simple coherence measure - decisions that build on each other vs. scattered
        var coherenceScore = 0f
        for (i in 1 until recentDecisions.size) {
            val current = recentDecisions[i]
            val previous = recentDecisions[i-1]
            
            // Check if decisions are related/building on each other
            if (areDecisionsRelated(current, previous)) {
                coherenceScore += 1f
            }
        }
        
        return coherenceScore / (recentDecisions.size - 1)
    }
    
    private fun areDecisionsRelated(decision1: String, decision2: String): Boolean {
        // Simple heuristic for decision relatedness
        val tokens1 = decision1.lowercase().split(":", "_", " ")
        val tokens2 = decision2.lowercase().split(":", "_", " ")
        
        return tokens1.intersect(tokens2.toSet()).isNotEmpty()
    }
    
    private fun calculateFlailingRiskScore(
        reactiveDecisions: Int,
        additionRate: Float,
        integrationCompleteness: Float,
        decisionCoherence: Float
    ): Float {
        val reactiveWeight = 0.3f
        val additionWeight = 0.3f  
        val integrationWeight = 0.2f
        val coherenceWeight = 0.2f
        
        val reactiveScore = minOf(1.0f, reactiveDecisions / 5.0f)
        val integrationScore = 1.0f - integrationCompleteness
        val coherenceScore = 1.0f - decisionCoherence
        
        return (reactiveScore * reactiveWeight) +
               (additionRate * additionWeight) +
               (integrationScore * integrationWeight) +
               (coherenceScore * coherenceWeight)
    }
}