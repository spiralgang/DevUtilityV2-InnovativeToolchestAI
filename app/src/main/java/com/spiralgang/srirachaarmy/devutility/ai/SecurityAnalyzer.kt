package com.spiralgang.srirachaarmy.devutility.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security analyzer for code vulnerability detection
 * Part of DevUtility V2.5 security analysis features
 */
@Singleton
class SecurityAnalyzer @Inject constructor() {
    
    data class SecurityIssue(
        val type: String,
        val severity: String, // LOW, MEDIUM, HIGH, CRITICAL
        val message: String,
        val line: Int,
        val description: String = "",
        val solution: String = "",
        val cweId: String? = null // Common Weakness Enumeration ID
    )
    
    /**
     * Analyze code for security vulnerabilities
     */
    fun analyzeSecurity(code: String, language: String): List<SecurityIssue> {
        try {
            Timber.d("Analyzing security for $language code")
            
            val issues = mutableListOf<SecurityIssue>()
            
            // Common security checks for all languages
            checkHardcodedCredentials(code, issues)
            checkSQLInjection(code, issues)
            checkXSS(code, issues)
            checkInsecureRandom(code, issues)
            checkUnsafeDeserialization(code, issues)
            
            // Language-specific security checks
            when (language.lowercase()) {
                "kotlin", "java" -> {
                    analyzeJavaKotlinSecurity(code, issues)
                }
                "javascript" -> {
                    analyzeJavaScriptSecurity(code, issues)
                }
                "python" -> {
                    analyzePythonSecurity(code, issues)
                }
            }
            
            Timber.d("Found ${issues.size} security issues")
            return issues
        } catch (e: Exception) {
            Timber.e(e, "Security analysis failed")
            return listOf(
                SecurityIssue(
                    type = "ANALYSIS_ERROR",
                    severity = "HIGH",
                    message = "Security analysis failed: ${e.message}",
                    line = 0
                )
            )
        }
    }
    
    private fun checkHardcodedCredentials(code: String, issues: MutableList<SecurityIssue>) {
        val credentialPatterns = listOf(
            Regex("""password\s*[=:]\s*["'][^"']+["']""", RegexOption.IGNORE_CASE),
            Regex("""api[_-]?key\s*[=:]\s*["'][^"']+["']""", RegexOption.IGNORE_CASE),
            Regex("""secret\s*[=:]\s*["'][^"']+["']""", RegexOption.IGNORE_CASE),
            Regex("""token\s*[=:]\s*["'][^"']+["']""", RegexOption.IGNORE_CASE)
        )
        credentialPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "HARDCODED_CREDENTIALS",
                        severity = "CRITICAL",
                        message = "Hardcoded credentials detected",
                        line = lineNumber,
                        description = "Hardcoded credentials in source code pose a severe security risk",
                        solution = "Use environment variables or secure configuration files",
                        cweId = "CWE-798"
                    )
                )
            }
        }
    }
    
    private fun checkSQLInjection(code: String, issues: MutableList<SecurityIssue>) {
        val sqlPatterns = listOf(
            Regex("""query\s*\+\s*["\']""", RegexOption.IGNORE_CASE),
            Regex("""SELECT\s+\*\s+FROM\s+\w+\s+WHERE\s+\w+\s*=\s*["\']?\s*\+""", RegexOption.IGNORE_CASE),
            Regex(""""SELECT.*"\s*\+\s*""", RegexOption.IGNORE_CASE)
        )
        
        sqlPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "SQL_INJECTION",
                        severity = "HIGH",
                        message = "Potential SQL injection vulnerability",
                        line = lineNumber,
                        description = "Dynamic SQL construction may lead to SQL injection attacks",
                        solution = "Use parameterized queries or prepared statements",
                        cweId = "CWE-89"
                    )
                )
            }
        }
    }
    
    private fun checkXSS(code: String, issues: MutableList<SecurityIssue>) {
        val xssPatterns = listOf(
            Regex("""innerHTML\s*=\s*.*\+""", RegexOption.IGNORE_CASE),
            Regex("""document\.write\s*\(.*\+""", RegexOption.IGNORE_CASE),
            Regex("""eval\s*\(""", RegexOption.IGNORE_CASE)
        )
        
        xssPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "XSS_VULNERABILITY",
                        severity = "HIGH",
                        message = "Potential cross-site scripting (XSS) vulnerability",
                        line = lineNumber,
                        description = "Dynamic content injection may lead to XSS attacks",
                        solution = "Sanitize user input and use safe DOM manipulation methods",
                        cweId = "CWE-79"
                    )
                )
            }
        }
    }
    
    private fun checkInsecureRandom(code: String, issues: MutableList<SecurityIssue>) {
        val insecureRandomPatterns = listOf(
            Regex("""Math\.random\(\)"""),
            Regex("""Random\(\)"""),
            Regex("""java\.util\.Random""")
        )
        
        insecureRandomPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "INSECURE_RANDOM",
                        severity = "MEDIUM",
                        message = "Use of cryptographically weak random number generator",
                        line = lineNumber,
                        description = "Weak random number generation can be predictable",
                        solution = "Use SecureRandom for cryptographic purposes",
                        cweId = "CWE-330"
                    )
                )
            }
        }
    }
    
    private fun checkUnsafeDeserialization(code: String, issues: MutableList<SecurityIssue>) {
        val deserializationPatterns = listOf(
            Regex("""ObjectInputStream"""),
            Regex("""readObject\s*\("""),
            Regex("""pickle\.loads"""),
            Regex("""JSON\.parse\([^)]*\)""")
        )
        
        deserializationPatterns.forEach { pattern ->
            pattern.findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "UNSAFE_DESERIALIZATION",
                        severity = "HIGH",
                        message = "Potential unsafe deserialization",
                        line = lineNumber,
                        description = "Deserializing untrusted data can lead to code execution",
                        solution = "Validate and sanitize input before deserialization",
                        cweId = "CWE-502"
                    )
                )
            }
        }
    }
    
    private fun analyzeJavaKotlinSecurity(code: String, issues: MutableList<SecurityIssue>) {
        // Check for System.exit calls
        Regex("""System\.exit\s*\(""").findAll(code).forEach { match ->
            val lineNumber = getLineNumber(code, match.range.first)
            issues.add(
                SecurityIssue(
                    type = "SYSTEM_EXIT",
                    severity = "MEDIUM",
                    message = "Use of System.exit() can terminate the application unexpectedly",
                    line = lineNumber,
                    description = "System.exit() calls can be used for denial of service",
                    solution = "Use proper exception handling instead of System.exit()"
                )
            )
        }
        
        // Check for Runtime.exec usage
        Regex("""Runtime\.getRuntime\(\)\.exec""").findAll(code).forEach { match ->
            val lineNumber = getLineNumber(code, match.range.first)
            issues.add(
                SecurityIssue(
                    type = "COMMAND_INJECTION",
                    severity = "HIGH",
                    message = "Potential command injection vulnerability",
                    line = lineNumber,
                    description = "Runtime.exec() with user input can lead to command injection",
                    solution = "Validate and sanitize command arguments, use ProcessBuilder",
                    cweId = "CWE-78"
                )
            )
        }
    }
    
    private fun analyzeJavaScriptSecurity(code: String, issues: MutableList<SecurityIssue>) {
        // Check for dangerous functions
        val dangerousFunctions = listOf("eval", "setTimeout", "setInterval", "Function")
        dangerousFunctions.forEach { func ->
            Regex("""$func\s*\(""").findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "DANGEROUS_FUNCTION",
                        severity = "MEDIUM",
                        message = "Use of potentially dangerous function: $func",
                        line = lineNumber,
                        description = "$func can execute arbitrary code and pose security risks",
                        solution = "Avoid using $func with untrusted input"
                    )
                )
            }
        }
    }
    
    private fun analyzePythonSecurity(code: String, issues: MutableList<SecurityIssue>) {
        // Check for dangerous imports
        val dangerousImports = listOf("os.system", "subprocess.call", "exec", "eval")
        dangerousImports.forEach { import ->
            if (code.contains(import)) {
                issues.add(
                    SecurityIssue(
                        type = "DANGEROUS_IMPORT",
                        severity = "HIGH",
                        message = "Use of potentially dangerous function: $import",
                        line = 1, // Simplified line detection
                        description = "$import can execute arbitrary code",
                        solution = "Use safer alternatives and validate input"
                    )
                )
            }
        }
    }
    
    private fun getLineNumber(code: String, charIndex: Int): Int {
        return code.substring(0, charIndex).count { it == '\n' } + 1
    }
}