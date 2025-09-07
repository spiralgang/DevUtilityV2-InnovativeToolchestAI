// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.srirachaarmy.devutility.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Code review service using AI analysis
 * Part of DevUtility V2.5 AI enhancement features
 */
@Singleton
class CodeReviewService @Inject constructor() {
    
    data class CodeReviewResult(
        val score: Int, // 0-100
        val issues: List<String>,
        val suggestions: List<String>,
        val securityIssues: List<SecurityAnalyzer.SecurityIssue>,
        val performanceIssues: List<String> = emptyList(),
        val codeQuality: String = "Unknown",
        val maintainabilityScore: Int = 0,
        val complexity: String = "Unknown"
    )
    
    /**
     * Perform comprehensive code review
     */
    suspend fun performReview(
        code: String,
        language: String,
        securityIssues: List<SecurityAnalyzer.SecurityIssue>,
        performanceAnalysis: Any?
    ): CodeReviewResult = withContext(Dispatchers.IO) {
        try {
            Timber.d("Performing code review for $language")
            
            val issues = mutableListOf<String>()
            val suggestions = mutableListOf<String>()
            
            // Analyze code structure
            analyzeCodeStructure(code, language, issues, suggestions)
            
            // Analyze naming conventions
            analyzeNamingConventions(code, language, issues, suggestions)
            
            // Analyze best practices
            analyzeBestPractices(code, language, issues, suggestions)
            
            // Calculate scores
            val score = calculateOverallScore(issues.size, securityIssues.size)
            val maintainabilityScore = calculateMaintainabilityScore(code, language)
            val complexity = analyzeComplexity(code, language)
            
            CodeReviewResult(
                score = score,
                issues = issues,
                suggestions = suggestions,
                securityIssues = securityIssues,
                codeQuality = determineCodeQuality(score),
                maintainabilityScore = maintainabilityScore,
                complexity = complexity
            )
        } catch (e: Exception) {
            Timber.e(e, "Code review analysis failed")
            CodeReviewResult(
                score = 0,
                issues = listOf("Analysis failed: ${e.message}"),
                suggestions = emptyList(),
                securityIssues = securityIssues
            )
        }
    }
    
    private fun analyzeCodeStructure(code: String, language: String, issues: MutableList<String>, suggestions: MutableList<String>) {
        when (language.lowercase()) {
            "kotlin", "java" -> {
                if (!code.contains("class") && !code.contains("fun ") && !code.contains("val ")) {
                    issues.add("Code structure seems incomplete or malformed")
                    suggestions.add("Ensure proper class/function structure")
                }
                
                // Check for proper indentation
                val lines = code.split("\n")
                var indentationInconsistent = false
                for (line in lines) {
                    if (line.trim().isNotEmpty() && !line.startsWith("    ") && !line.startsWith("\t") && line.startsWith(" ")) {
                        indentationInconsistent = true
                        break
                    }
                }
                if (indentationInconsistent) {
                    issues.add("Inconsistent indentation detected")
                    suggestions.add("Use consistent 4-space indentation")
                }
            }
            "python" -> {
                if (!code.contains("def ") && !code.contains("class ")) {
                    issues.add("No function or class definitions found")
                    suggestions.add("Structure code with proper functions or classes")
                }
            }
            "javascript" -> {
                if (!code.contains("function") && !code.contains("=>") && !code.contains("var ") && !code.contains("let ") && !code.contains("const ")) {
                    issues.add("No function or variable declarations found")
                    suggestions.add("Use proper variable declarations (let/const)")
                }
            }
        }
    }
    
    private fun analyzeNamingConventions(code: String, language: String, issues: MutableList<String>, suggestions: MutableList<String>) {
        when (language.lowercase()) {
            "kotlin", "java" -> {
                // Check for camelCase functions and variables
                val functionPattern = Regex("""fun\s+([A-Z][a-zA-Z0-9_]*)""")
                functionPattern.findAll(code).forEach {
                    issues.add("Function '${it.groupValues[1]}' should start with lowercase (camelCase)")
                    suggestions.add("Use camelCase for function names: ${it.groupValues[1].replaceFirstChar { char -> char.lowercase() }}")
                }
                
                // Check for PascalCase classes
                val classPattern = Regex("""class\s+([a-z][a-zA-Z0-9_]*)""")
                classPattern.findAll(code).forEach {
                    issues.add("Class '${it.groupValues[1]}' should start with uppercase (PascalCase)")
                    suggestions.add("Use PascalCase for class names: ${it.groupValues[1].replaceFirstChar { char -> char.uppercase() }}")
                }
            }
        }
    }
    
    private fun analyzeBestPractices(code: String, language: String, issues: MutableList<String>, suggestions: MutableList<String>) {
        // Check for hardcoded values
        val hardcodedStrings = Regex(""""[^"]{10,}"""").findAll(code)
        if (hardcodedStrings.count() > 0) {
            issues.add("Hardcoded strings detected")
            suggestions.add("Consider using constants or configuration files for long strings")
        }
        
        // Check for TODO/FIXME comments
        if (code.contains("TODO") || code.contains("FIXME")) {
            issues.add("Unresolved TODO/FIXME comments found")
            suggestions.add("Complete or remove TODO/FIXME items before production")
        }
        
        // Language-specific best practices
        when (language.lowercase()) {
            "kotlin" -> {
                if (code.contains("!!")) {
                    issues.add("Non-null assertion operator (!!) usage detected")
                    suggestions.add("Consider using safe calls (?.) or explicit null checks")
                }
            }
            "java" -> {
                if (code.contains("System.out.println")) {
                    issues.add("System.out.println usage detected")
                    suggestions.add("Use proper logging framework instead of System.out.println")
                }
            }
        }
    }
    
    private fun calculateOverallScore(issueCount: Int, securityIssueCount: Int): Int {
        val baseScore = 100
        val issueDeduction = issueCount * 5
        val securityDeduction = securityIssueCount * 10
        return maxOf(0, baseScore - issueDeduction - securityDeduction)
    }
    
    private fun calculateMaintainabilityScore(code: String, language: String): Int {
        val lines = code.split("\n").filter { it.trim().isNotEmpty() }
        val complexity = when {
            lines.size < 20 -> 100
            lines.size < 50 -> 80
            lines.size < 100 -> 60
            lines.size < 200 -> 40
            else -> 20
        }
        return complexity
    }
    
    private fun analyzeComplexity(code: String, language: String): String {
        val cyclomaticComplexity = calculateCyclomaticComplexity(code)
        return when {
            cyclomaticComplexity <= 10 -> "Low"
            cyclomaticComplexity <= 20 -> "Moderate"
            cyclomaticComplexity <= 50 -> "High"
            else -> "Very High"
        }
    }
    
    private fun calculateCyclomaticComplexity(code: String): Int {
        // Simplified cyclomatic complexity calculation
        val decisionPoints = listOf("if", "while", "for", "case", "catch", "&&", "||")
        var complexity = 1 // Base complexity
        
        decisionPoints.forEach { point ->
            complexity += code.split(point).size - 1
        }
        
        return complexity
    }
    
    private fun determineCodeQuality(score: Int): String {
        return when {
            score >= 90 -> "Excellent"
            score >= 80 -> "Good"
            score >= 70 -> "Average"
            score >= 60 -> "Below Average"
            else -> "Poor"
        }
    }
}