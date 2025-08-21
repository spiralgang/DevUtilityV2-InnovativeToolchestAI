package com.spiralgang.srirachaarmy.devutility.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Code summarization service using AI
 * Part of DevUtility V2.5 AI enhancement features
 */
@Singleton
class CodeSummarizer @Inject constructor() {
    
    /**
     * Generate AI-powered code summary
     */
    suspend fun generateSummary(code: String, language: String): String = withContext(Dispatchers.IO) {
        try {
            Timber.d("Generating summary for $language code")
            
            val summary = StringBuilder()
            
            // Analyze code structure
            val structure = analyzeCodeStructure(code, language)
            summary.append("Code Structure: $structure\n")
            
            // Analyze functions/methods
            val functions = extractFunctions(code, language)
            if (functions.isNotEmpty()) {
                summary.append("Functions/Methods:\n")
                functions.take(5).forEach { function ->
                    summary.append("  - $function\n")
                }
                if (functions.size > 5) {
                    summary.append("  ... and ${functions.size - 5} more\n")
                }
            }
            
            // Analyze complexity
            val complexity = analyzeComplexity(code)
            summary.append("Complexity: $complexity\n")
            
            // Analyze dependencies
            val dependencies = extractDependencies(code, language)
            if (dependencies.isNotEmpty()) {
                summary.append("Dependencies: ${dependencies.joinToString(", ")}\n")
            }
            
            // Generate overall purpose
            val purpose = inferPurpose(code, language)
            summary.append("Purpose: $purpose")
            
            summary.toString()
            
        } catch (e: Exception) {
            Timber.e(e, "Code summarization failed")
            "Summary generation failed: ${e.message}"
        }
    }
    
    private fun analyzeCodeStructure(code: String, language: String): String {
        val lines = code.split("\n").filter { it.trim().isNotEmpty() }
        val totalLines = lines.size
        
        return when (language.lowercase()) {
            "kotlin", "java" -> {
                val classes = code.split(Regex("""class\s+\w+""")).size - 1
                val methods = code.split(Regex("""fun\s+\w+|void\s+\w+|public\s+\w+""")).size - 1
                "Object-oriented ($classes classes, $methods methods, $totalLines lines)"
            }
            "python" -> {
                val classes = code.split(Regex("""class\s+\w+""")).size - 1
                val functions = code.split(Regex("""def\s+\w+""")).size - 1
                "Python script ($classes classes, $functions functions, $totalLines lines)"
            }
            "javascript" -> {
                val functions = code.split(Regex("""function\s+\w+|=>\s*{""")).size - 1
                "JavaScript ($functions functions, $totalLines lines)"
            }
            else -> "Generic structure ($totalLines lines)"
        }
    }
    
    private fun extractFunctions(code: String, language: String): List<String> {
        val functions = mutableListOf<String>()
        
        when (language.lowercase()) {
            "kotlin", "java" -> {
                val functionPattern = Regex("""fun\s+(\w+)\s*\([^)]*\)|void\s+(\w+)\s*\([^)]*\)|public\s+\w+\s+(\w+)\s*\([^)]*\)""")
                functionPattern.findAll(code).forEach { match ->
                    val functionName = match.groupValues.find { it.isNotBlank() && it != match.value }
                    functionName?.let { functions.add(it) }
                }
            }
            "python" -> {
                val functionPattern = Regex("""def\s+(\w+)\s*\([^)]*\):""")
                functionPattern.findAll(code).forEach { match ->
                    functions.add(match.groupValues[1])
                }
            }
            "javascript" -> {
                val functionPattern = Regex("""function\s+(\w+)\s*\([^)]*\)|const\s+(\w+)\s*=\s*\([^)]*\)\s*=>|let\s+(\w+)\s*=\s*\([^)]*\)\s*=>""")
                functionPattern.findAll(code).forEach { match ->
                    val functionName = match.groupValues.find { it.isNotBlank() && it != match.value }
                    functionName?.let { functions.add(it) }
                }
            }
        }
        
        return functions
    }
    
    private fun analyzeComplexity(code: String): String {
        val lines = code.split("\n").filter { it.trim().isNotEmpty() }
        val cyclomaticComplexity = calculateCyclomaticComplexity(code)
        
        return when {
            lines.size < 20 && cyclomaticComplexity <= 5 -> "Low (Simple)"
            lines.size < 50 && cyclomaticComplexity <= 10 -> "Moderate"
            lines.size < 100 && cyclomaticComplexity <= 20 -> "Medium-High"
            else -> "High (Complex)"
        }
    }
    
    private fun calculateCyclomaticComplexity(code: String): Int {
        val decisionPoints = listOf("if", "while", "for", "case", "catch", "&&", "||", "?")
        var complexity = 1
        
        decisionPoints.forEach { point ->
            complexity += code.split(point).size - 1
        }
        
        return complexity
    }
    
    private fun extractDependencies(code: String, language: String): List<String> {
        val dependencies = mutableListOf<String>()
        
        when (language.lowercase()) {
            "kotlin", "java" -> {
                val importPattern = Regex("""import\s+([\w.]+)""")
                importPattern.findAll(code).forEach { match ->
                    val import = match.groupValues[1]
                    val packageName = import.substringBeforeLast(".", import)
                    if (packageName.contains(".")) {
                        dependencies.add(packageName.substringBefore("."))
                    }
                }
            }
            "python" -> {
                val importPattern = Regex("""import\s+(\w+)|from\s+(\w+)\s+import""")
                importPattern.findAll(code).forEach { match ->
                    val module = match.groupValues[1].ifEmpty { match.groupValues[2] }
                    if (module.isNotBlank()) {
                        dependencies.add(module)
                    }
                }
            }
            "javascript" -> {
                val importPattern = Regex("""import.*from\s+['"]([^'"]+)['"]|require\(['"]([^'"]+)['"]\)""")
                importPattern.findAll(code).forEach { match ->
                    val module = match.groupValues[1].ifEmpty { match.groupValues[2] }
                    if (module.isNotBlank() && !module.startsWith(".")) {
                        dependencies.add(module)
                    }
                }
            }
        }
        
        return dependencies.distinct().take(10) // Limit to 10 main dependencies
    }
    
    private fun inferPurpose(code: String, language: String): String {
        val codeContent = code.lowercase()
        
        return when {
            codeContent.contains("activity") && codeContent.contains("android") -> 
                "Android application activity"
            codeContent.contains("fragment") && codeContent.contains("android") -> 
                "Android fragment component"
            codeContent.contains("service") && codeContent.contains("android") -> 
                "Android background service"
            codeContent.contains("viewmodel") || codeContent.contains("livedata") -> 
                "MVVM architecture component"
            codeContent.contains("repository") || codeContent.contains("dao") -> 
                "Data access/repository layer"
            codeContent.contains("api") || codeContent.contains("retrofit") || codeContent.contains("http") -> 
                "API/Network communication"
            codeContent.contains("database") || codeContent.contains("room") || codeContent.contains("sql") -> 
                "Database operations"
            codeContent.contains("test") || codeContent.contains("junit") || codeContent.contains("assert") -> 
                "Unit testing code"
            codeContent.contains("ui") || codeContent.contains("compose") || codeContent.contains("layout") -> 
                "User interface component"
            codeContent.contains("util") || codeContent.contains("helper") -> 
                "Utility/Helper functions"
            codeContent.contains("main") || codeContent.contains("app") -> 
                "Main application entry point"
            else -> "General programming logic"
        }
    }
}