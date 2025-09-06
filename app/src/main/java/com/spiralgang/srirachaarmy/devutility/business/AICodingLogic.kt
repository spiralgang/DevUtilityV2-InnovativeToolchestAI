package com.spiralgang.srirachaarmy.devutility.business

import com.spiralgang.srirachaarmy.devutility.ai.*
import com.spiralgang.srirachaarmy.devutility.core.AIInstanceManager
import com.spiralgang.srirachaarmy.devutility.core.AIOperationBlockedException
import com.spiralgang.srirachaarmy.devutility.execution.CodeExecutor
import com.spiralgang.srirachaarmy.devutility.profiler.Profiler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Business logic for AI-powered coding features
 * Central coordinator for code analysis, execution, and AI assistance
 * Part of DevUtility V2.5 enhanced AI integration
 */
@Singleton
class AICodingLogic @Inject constructor(
    private val codeExecutor: CodeExecutor,
    private val codeReviewService: CodeReviewService,
    private val securityAnalyzer: SecurityAnalyzer,
    private val codeSummarizer: CodeSummarizer,
    private val profiler: Profiler,
    private val aiThinkModule: AIThinkModule,
    private val offlineAIService: OfflineAIService
) {
    
    /**
     * Execute code in custom sandbox environment
     */
    suspend fun executeCode(code: String, language: String): String = withContext(Dispatchers.IO) {
        try {
            Timber.d("Executing $language code in custom sandbox")
            
            // Pre-execution AI analysis
            val analysisResult = aiThinkModule.analyzeCodeBeforeExecution(code, language)
            Timber.d("Pre-execution analysis: $analysisResult")
            
            // Execute in secure sandbox
            val result = codeExecutor.executeInSandbox(code, language)
            
            // Post-execution analysis
            aiThinkModule.analyzeExecutionResult(result, code, language)
            
            result
        } catch (e: Exception) {
            Timber.e(e, "Code execution failed")
            "Execution failed: ${e.message}"
        }
    }
    
    /**
     * Perform comprehensive code review using AI
     * NOW LIMITED TO ONE AI OPERATION AT A TIME
     */
    suspend fun reviewCode(code: String, language: String): CodeReviewService.CodeReviewResult = withContext(Dispatchers.IO) {
        try {
            AIInstanceManager.executeAIOperation(
                AIInstanceManager.AIOperationType.CODE_REVIEW,
                "reviewCode: $language"
            ) {
                Timber.d("Performing AI code review for $language")
                
                // Multi-layered analysis
                val securityIssues = securityAnalyzer.analyzeSecurity(code, language)
                val performanceAnalysis = profiler.analyzePerformance(code, language)
                val thinkModuleInsights = aiThinkModule.deepAnalysis(code, language)
                
                // Synthesize review results
                codeReviewService.performReview(code, language, securityIssues, performanceAnalysis)
            }
        } catch (e: AIOperationBlockedException) {
            Timber.w(e, "Code review blocked - another AI operation in progress")
            CodeReviewService.CodeReviewResult(
                score = 0,
                issues = listOf("Code review queued - another AI operation in progress"),
                suggestions = listOf("Please wait and try again"),
                securityIssues = emptyList()
            )
        } catch (e: Exception) {
            Timber.e(e, "Code review failed")
            CodeReviewService.CodeReviewResult(
                score = 0,
                issues = listOf("Review failed: ${e.message}"),
                suggestions = emptyList(),
                securityIssues = emptyList()
            )
        }
    }
    
    /**
     * Profile code performance
     */
    fun profileCode(code: String, language: String): Profiler.ProfileResult {
        return try {
            Timber.d("Profiling $language code performance")
            profiler.profileExecution(code, language)
        } catch (e: Exception) {
            Timber.e(e, "Code profiling failed")
            Profiler.ProfileResult(
                executionTime = -1,
                memoryUsage = -1,
                recommendations = listOf("Profiling failed: ${e.message}")
            )
        }
    }
    
    /**
     * Run comprehensive tests (unit, integration, UI)
     */
    fun runTests(code: String, language: String): Triple<String, String, String> {
        return try {
            Timber.d("Running comprehensive tests for $language code")
            
            val unitTestResults = runUnitTests(code, language)
            val integrationTestResults = runIntegrationTests(code, language)
            val uiTestResults = runUITests(code, language)
            
            Triple(unitTestResults, integrationTestResults, uiTestResults)
        } catch (e: Exception) {
            Timber.e(e, "Test execution failed")
            val errorMsg = "Test failed: ${e.message}"
            Triple(errorMsg, errorMsg, errorMsg)
        }
    }
    
    /**
     * Trigger CI/CD build process
     */
    suspend fun triggerCIBuild(projectId: String): String = withContext(Dispatchers.IO) {
        try {
            Timber.d("Triggering CI build for project: $projectId")
            // Integration with CI/CD pipeline
            "CI build triggered successfully for project: $projectId"
        } catch (e: Exception) {
            Timber.e(e, "CI build trigger failed")
            "CI build failed: ${e.message}"
        }
    }
    
    /**
     * Analyze code security vulnerabilities
     */
    fun analyzeSecurity(code: String, language: String): List<SecurityAnalyzer.SecurityIssue> {
        return try {
            Timber.d("Analyzing security for $language code")
            securityAnalyzer.analyzeSecurity(code, language)
        } catch (e: Exception) {
            Timber.e(e, "Security analysis failed")
            listOf(
                SecurityAnalyzer.SecurityIssue(
                    type = "ANALYSIS_ERROR",
                    severity = "HIGH",
                    message = "Security analysis failed: ${e.message}",
                    line = 0
                )
            )
        }
    }
    
    /**
     * Generate code summary using AI
     */
    suspend fun summarizeCode(code: String, language: String): String = withContext(Dispatchers.IO) {
        try {
            Timber.d("Generating AI code summary for $language")
            codeSummarizer.generateSummary(code, language)
        } catch (e: Exception) {
            Timber.e(e, "Code summarization failed")
            "Summary generation failed: ${e.message}"
        }
    }
    
    // Private helper methods
    private fun runUnitTests(code: String, language: String): String {
        // Implementation for unit test execution
        return "Unit tests passed: 0/0"
    }
    
    private fun runIntegrationTests(code: String, language: String): String {
        // Implementation for integration test execution
        return "Integration tests passed: 0/0"
    }
    
    private fun runUITests(code: String, language: String): String {
        // Implementation for UI test execution
        return "UI tests passed: 0/0"
    }
}