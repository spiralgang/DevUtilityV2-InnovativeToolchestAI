package com.sgneuronlabs.devutilityandroidv2.ai

import org.junit.Test
import org.junit.Assert.*

class SecurityAnalyzerTest {
    private val securityAnalyzer = SecurityAnalyzer()

    @Test
    fun analyzeCodeSecurity_basicTest() {
        val code = "println(\"Hello World\")"
        val result = securityAnalyzer.analyzeCodeSecurity(code)
        assertEquals("No security issues detected", result)
    }
}

class CodeSummarizerTest {
    private val codeSummarizer = CodeSummarizer()

    @Test
    fun summarizeCode_basicTest() {
        val code = "fun main() { println(\"Hello\") }"
        val result = codeSummarizer.summarizeCode(code)
        assertTrue(result.contains("Basic functionality"))
    }
}

class AIThinkModuleTest {
    private val aiThinkModule = AIThinkModule()

    @Test
    fun learn_and_think_test() {
        val input = "test input"
        val context = "test context"
        aiThinkModule.learn(input, context)
        
        val result = aiThinkModule.think(input)
        assertEquals(context, result)
    }
}

class LearningBotTest {
    private val learningBot = LearningBot()

    @Test
    fun monitorUser_test() {
        // Test that monitoring doesn't crash
        learningBot.monitorUser("user1", "code_write")
        val guidance = learningBot.provideGuidance("user1")
        assertTrue(guidance.isNotEmpty())
    }
}