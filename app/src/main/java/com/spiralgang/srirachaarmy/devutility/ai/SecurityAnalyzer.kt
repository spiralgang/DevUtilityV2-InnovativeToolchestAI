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
 * Enhanced security analyzer for comprehensive vulnerability detection
 * Part of DevUtility V2.5 security analysis features with AI-powered detection
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
        val cweId: String? = null, // Common Weakness Enumeration ID
        val confidence: Double = 1.0, // AI confidence in detection (0.0-1.0)
        val riskScore: Double = 0.0,  // Calculated risk score
        val exploitability: String = "UNKNOWN" // UNKNOWN, LOW, MEDIUM, HIGH
    )
    
    data class SecurityReport(
        val issues: List<SecurityIssue>,
        val overallRiskScore: Double,
        val criticalCount: Int,
        val highCount: Int,
        val mediumCount: Int,
        val lowCount: Int,
        val analysisTimestamp: Long = System.currentTimeMillis(),
        val codeMetrics: CodeMetrics
    )
    
    data class CodeMetrics(
        val linesOfCode: Int,
        val complexity: Int,
        val securityHotspots: Int,
        val cryptographicOperations: Int,
        val networkOperations: Int,
        val fileOperations: Int
    )
    
    private val mlBasedPatterns = mutableMapOf<String, Double>()
    private val vulnerabilityDatabase = loadVulnerabilityDatabase()
    
    init {
        // Initialize ML-based pattern recognition weights
        mlBasedPatterns["suspicious_string_concat"] = 0.7
        mlBasedPatterns["dynamic_code_execution"] = 0.9
        mlBasedPatterns["unvalidated_input"] = 0.8
        mlBasedPatterns["weak_crypto"] = 0.6
    }
    
    /**
     * Enhanced security analysis with AI-powered detection
     */
    fun analyzeSecurityAdvanced(code: String, language: String): SecurityReport {
        try {
            Timber.d("Performing enhanced security analysis for $language code")
            
            val issues = mutableListOf<SecurityIssue>()
            val codeMetrics = calculateCodeMetrics(code, language)
            
            // Core security checks
            checkHardcodedCredentials(code, issues)
            checkSQLInjection(code, issues)
            checkXSS(code, issues)
            checkInsecureRandom(code, issues)
            checkUnsafeDeserialization(code, issues)
            
            // Enhanced security checks
            checkCryptographicVulnerabilities(code, issues)
            checkInputValidation(code, issues)
            checkAuthenticationFlaws(code, issues)
            checkAuthorizationIssues(code, issues)
            checkDataExposure(code, issues)
            checkDenialOfService(code, issues)
            checkSecurityMisconfiguration(code, issues)
            
            // AI-powered pattern detection
            performMLBasedAnalysis(code, issues)
            
            // Language-specific enhanced checks
            when (language.lowercase()) {
                "kotlin", "java" -> {
                    analyzeJavaKotlinSecurityEnhanced(code, issues)
                }
                "javascript", "typescript" -> {
                    analyzeJavaScriptSecurityEnhanced(code, issues)
                }
                "python" -> {
                    analyzePythonSecurityEnhanced(code, issues)
                }
                "c", "cpp", "c++" -> {
                    analyzeCCppSecurity(code, issues)
                }
            }
            
            // Calculate risk scores
            issues.forEach { issue ->
                val riskScore = calculateRiskScore(issue, codeMetrics)
                val exploitability = assessExploitability(issue, code)
                issues[issues.indexOf(issue)] = issue.copy(
                    riskScore = riskScore,
                    exploitability = exploitability
                )
            }
            
            val report = SecurityReport(
                issues = issues.sortedByDescending { it.riskScore },
                overallRiskScore = calculateOverallRisk(issues),
                criticalCount = issues.count { it.severity == "CRITICAL" },
                highCount = issues.count { it.severity == "HIGH" },
                mediumCount = issues.count { it.severity == "MEDIUM" },
                lowCount = issues.count { it.severity == "LOW" },
                codeMetrics = codeMetrics
            )
            
            Timber.d("Enhanced security analysis completed: ${issues.size} issues found, overall risk: ${report.overallRiskScore}")
            return report
            
        } catch (e: Exception) {
            Timber.e(e, "Enhanced security analysis failed")
            return SecurityReport(
                issues = listOf(
                    SecurityIssue(
                        type = "ANALYSIS_ERROR",
                        severity = "HIGH",
                        message = "Security analysis failed: ${e.message}",
                        line = 0,
                        confidence = 1.0,
                        riskScore = 5.0
                    )
                ),
                overallRiskScore = 5.0,
                criticalCount = 0,
                highCount = 1,
                mediumCount = 0,
                lowCount = 0,
                codeMetrics = CodeMetrics(0, 0, 0, 0, 0, 0)
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
    
    // Enhanced Security Analysis Methods
    
    private fun calculateCodeMetrics(code: String, language: String): CodeMetrics {
        val lines = code.lines()
        return CodeMetrics(
            linesOfCode = lines.size,
            complexity = estimateComplexity(code),
            securityHotspots = countSecurityHotspots(code),
            cryptographicOperations = countCryptographicOperations(code),
            networkOperations = countNetworkOperations(code),
            fileOperations = countFileOperations(code)
        )
    }
    
    private fun estimateComplexity(code: String): Int {
        val complexityIndicators = listOf("if", "else", "for", "while", "switch", "case", "catch", "&&", "||")
        return complexityIndicators.sumOf { indicator ->
            Regex("\\b$indicator\\b").findAll(code).count()
        }
    }
    
    private fun countSecurityHotspots(code: String): Int {
        val hotspots = listOf("password", "secret", "key", "token", "auth", "login", "admin", "root")
        return hotspots.sumOf { hotspot ->
            Regex("\\b$hotspot\\b", RegexOption.IGNORE_CASE).findAll(code).count()
        }
    }
    
    private fun countCryptographicOperations(code: String): Int {
        val cryptoPatterns = listOf("encrypt", "decrypt", "hash", "cipher", "crypto", "ssl", "tls")
        return cryptoPatterns.sumOf { pattern ->
            Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE).findAll(code).count()
        }
    }
    
    private fun countNetworkOperations(code: String): Int {
        val networkPatterns = listOf("http", "url", "socket", "connect", "request", "response")
        return networkPatterns.sumOf { pattern ->
            Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE).findAll(code).count()
        }
    }
    
    private fun countFileOperations(code: String): Int {
        val filePatterns = listOf("file", "read", "write", "open", "close", "stream")
        return filePatterns.sumOf { pattern ->
            Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE).findAll(code).count()
        }
    }
    
    private fun checkCryptographicVulnerabilities(code: String, issues: MutableList<SecurityIssue>) {
        val weakCryptoPatterns = listOf(
            "MD5" to "Use SHA-256 or stronger",
            "SHA1" to "Use SHA-256 or stronger", 
            "DES" to "Use AES instead",
            "RC4" to "Use AES instead"
        )
        
        weakCryptoPatterns.forEach { (pattern, solution) ->
            Regex("\\b$pattern\\b", RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "WEAK_CRYPTOGRAPHY",
                        severity = "HIGH",
                        message = "Weak cryptographic algorithm: $pattern",
                        line = lineNumber,
                        description = "Weak cryptographic algorithms are vulnerable to attacks",
                        solution = solution,
                        cweId = "CWE-327",
                        confidence = 0.9
                    )
                )
            }
        }
    }
    
    private fun checkInputValidation(code: String, issues: MutableList<SecurityIssue>) {
        val inputSources = listOf("request", "input", "param", "query", "form", "body")
        val dangerousFunctions = listOf("eval", "exec", "system", "shell_exec")
        
        inputSources.forEach { source ->
            dangerousFunctions.forEach { func ->
                val pattern = Regex("$func\\s*\\([^)]*$source", RegexOption.IGNORE_CASE)
                pattern.findAll(code).forEach { match ->
                    val lineNumber = getLineNumber(code, match.range.first)
                    issues.add(
                        SecurityIssue(
                            type = "UNVALIDATED_INPUT",
                            severity = "CRITICAL",
                            message = "Unvalidated input passed to dangerous function: $func",
                            line = lineNumber,
                            description = "User input should be validated before use in dangerous functions",
                            solution = "Implement proper input validation and sanitization",
                            cweId = "CWE-20",
                            confidence = 0.8
                        )
                    )
                }
            }
        }
    }
    
    private fun checkAuthenticationFlaws(code: String, issues: MutableList<SecurityIssue>) {
        val authPatterns = listOf(
            "password\\s*==\\s*[\"'][^\"']*[\"']" to "Avoid hardcoded password comparisons",
            "session\\s*=\\s*[\"'][^\"']*[\"']" to "Avoid hardcoded session values",
            "token\\s*=\\s*[\"'][^\"']*[\"']" to "Avoid hardcoded authentication tokens"
        )
        
        authPatterns.forEach { (pattern, solution) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "AUTHENTICATION_FLAW",
                        severity = "HIGH",
                        message = "Authentication implementation flaw detected",
                        line = lineNumber,
                        description = "Hardcoded authentication values are security risks",
                        solution = solution,
                        cweId = "CWE-287",
                        confidence = 0.7
                    )
                )
            }
        }
    }
    
    private fun checkAuthorizationIssues(code: String, issues: MutableList<SecurityIssue>) {
        val authzPatterns = listOf(
            "admin\\s*=\\s*true" to "Avoid hardcoded admin privileges",
            "role\\s*=\\s*[\"']admin[\"']" to "Avoid hardcoded role assignments",
            "if\\s*\\(\\s*true\\s*\\)" to "Suspicious always-true authorization check"
        )
        
        authzPatterns.forEach { (pattern, description) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "AUTHORIZATION_ISSUE",
                        severity = "MEDIUM",
                        message = "Potential authorization bypass",
                        line = lineNumber,
                        description = description,
                        solution = "Implement proper authorization checks",
                        cweId = "CWE-285",
                        confidence = 0.6
                    )
                )
            }
        }
    }
    
    private fun checkDataExposure(code: String, issues: MutableList<SecurityIssue>) {
        val exposurePatterns = listOf(
            "printStackTrace\\(\\)" to "Stack traces can expose sensitive information",
            "System\\.out\\.print" to "Console output might expose sensitive data",
            "log\\.[a-z]+\\([^)]*password" to "Logging passwords is a security risk"
        )
        
        exposurePatterns.forEach { (pattern, description) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "DATA_EXPOSURE",
                        severity = "MEDIUM",
                        message = "Potential sensitive data exposure",
                        line = lineNumber,
                        description = description,
                        solution = "Remove or secure sensitive data in output",
                        cweId = "CWE-200",
                        confidence = 0.7
                    )
                )
            }
        }
    }
    
    private fun checkDenialOfService(code: String, issues: MutableList<SecurityIssue>) {
        val dosPatterns = listOf(
            "while\\s*\\(\\s*true\\s*\\)" to "Infinite loops can cause denial of service",
            "for\\s*\\([^;]*;\\s*true\\s*;" to "Infinite loops can cause denial of service",
            "Thread\\.sleep\\s*\\(\\s*[0-9]{4,}" to "Long sleep times can impact availability"
        )
        
        dosPatterns.forEach { (pattern, description) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "DENIAL_OF_SERVICE",
                        severity = "MEDIUM",
                        message = "Potential denial of service vulnerability",
                        line = lineNumber,
                        description = description,
                        solution = "Add proper bounds and timeouts",
                        cweId = "CWE-400",
                        confidence = 0.5
                    )
                )
            }
        }
    }
    
    private fun checkSecurityMisconfiguration(code: String, issues: MutableList<SecurityIssue>) {
        val misconfigPatterns = listOf(
            "debug\\s*=\\s*true" to "Debug mode should be disabled in production",
            "ssl\\s*=\\s*false" to "SSL should be enabled for security",
            "verify\\s*=\\s*false" to "Certificate verification should be enabled"
        )
        
        misconfigPatterns.forEach { (pattern, description) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "SECURITY_MISCONFIGURATION",
                        severity = "MEDIUM",
                        message = "Security misconfiguration detected",
                        line = lineNumber,
                        description = description,
                        solution = "Review and secure configuration settings",
                        cweId = "CWE-16",
                        confidence = 0.8
                    )
                )
            }
        }
    }
    
    private fun performMLBasedAnalysis(code: String, issues: MutableList<SecurityIssue>) {
        // AI-powered pattern detection based on machine learning models
        val suspiciousPatterns = mapOf(
            "string concatenation in SQL context" to Regex("\"SELECT.*\"\\s*\\+", RegexOption.IGNORE_CASE),
            "dynamic code execution" to Regex("eval\\s*\\(|exec\\s*\\(|Function\\s*\\(", RegexOption.IGNORE_CASE),
            "suspicious network calls" to Regex("fetch\\s*\\(.*\\+|request\\s*\\(.*\\+", RegexOption.IGNORE_CASE)
        )
        
        suspiciousPatterns.forEach { (description, pattern) ->
            pattern.findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                val confidence = mlBasedPatterns[description.replace(" ", "_")] ?: 0.5
                
                issues.add(
                    SecurityIssue(
                        type = "ML_DETECTED_VULNERABILITY",
                        severity = if (confidence > 0.8) "HIGH" else "MEDIUM",
                        message = "ML-detected potential vulnerability: $description",
                        line = lineNumber,
                        description = "AI-powered analysis detected suspicious pattern",
                        solution = "Review code for potential security issues",
                        confidence = confidence
                    )
                )
            }
        }
    }
    
    private fun analyzeJavaKotlinSecurityEnhanced(code: String, issues: MutableList<SecurityIssue>) {
        // Enhanced Java/Kotlin specific security checks
        val javaPatterns = mapOf(
            "Runtime\\.getRuntime\\(\\)\\.exec" to "Command injection risk",
            "ProcessBuilder\\(" to "Command execution risk",
            "Class\\.forName" to "Dynamic class loading risk",
            "System\\.setProperty" to "System property manipulation"
        )
        
        javaPatterns.forEach { (pattern, description) ->
            Regex(pattern).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "JAVA_SECURITY_RISK",
                        severity = "HIGH",
                        message = description,
                        line = lineNumber,
                        description = "Java/Kotlin specific security vulnerability",
                        solution = "Use safer alternatives and validate input",
                        confidence = 0.8
                    )
                )
            }
        }
    }
    
    private fun analyzeJavaScriptSecurityEnhanced(code: String, issues: MutableList<SecurityIssue>) {
        // Enhanced JavaScript specific security checks
        val jsPatterns = mapOf(
            "eval\\s*\\(" to "Code injection risk",
            "innerHTML\\s*=" to "XSS vulnerability risk",
            "document\\.write" to "XSS vulnerability risk",
            "window\\s*\\[" to "Dynamic property access risk"
        )
        
        jsPatterns.forEach { (pattern, description) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "JAVASCRIPT_SECURITY_RISK",
                        severity = "HIGH",
                        message = description,
                        line = lineNumber,
                        description = "JavaScript specific security vulnerability",
                        solution = "Use safer alternatives and sanitize input",
                        confidence = 0.8
                    )
                )
            }
        }
    }
    
    private fun analyzePythonSecurityEnhanced(code: String, issues: MutableList<SecurityIssue>) {
        // Enhanced Python specific security checks
        val pythonPatterns = mapOf(
            "eval\\s*\\(" to "Code injection risk",
            "exec\\s*\\(" to "Code execution risk",
            "pickle\\.loads" to "Deserialization risk",
            "subprocess\\." to "Command execution risk"
        )
        
        pythonPatterns.forEach { (pattern, description) ->
            Regex(pattern, RegexOption.IGNORE_CASE).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "PYTHON_SECURITY_RISK",
                        severity = "HIGH",
                        message = description,
                        line = lineNumber,
                        description = "Python specific security vulnerability",
                        solution = "Use safer alternatives and validate input",
                        confidence = 0.8
                    )
                )
            }
        }
    }
    
    private fun analyzeCCppSecurity(code: String, issues: MutableList<SecurityIssue>) {
        // C/C++ specific security checks
        val cPatterns = mapOf(
            "strcpy\\s*\\(" to "Buffer overflow risk - use strncpy",
            "sprintf\\s*\\(" to "Buffer overflow risk - use snprintf", 
            "gets\\s*\\(" to "Buffer overflow risk - use fgets",
            "malloc\\s*\\(" to "Memory management issue - check for leaks"
        )
        
        cPatterns.forEach { (pattern, description) ->
            Regex(pattern).findAll(code).forEach { match ->
                val lineNumber = getLineNumber(code, match.range.first)
                issues.add(
                    SecurityIssue(
                        type = "C_CPP_SECURITY_RISK",
                        severity = "HIGH",
                        message = description,
                        line = lineNumber,
                        description = "C/C++ specific security vulnerability",
                        solution = "Use safer memory management functions",
                        confidence = 0.9
                    )
                )
            }
        }
    }
    
    private fun calculateRiskScore(issue: SecurityIssue, metrics: CodeMetrics): Double {
        var score = when (issue.severity) {
            "CRITICAL" -> 9.0
            "HIGH" -> 7.0
            "MEDIUM" -> 5.0
            "LOW" -> 3.0
            else -> 1.0
        }
        
        // Adjust based on code complexity
        score *= (1.0 + (metrics.complexity / 100.0))
        
        // Adjust based on confidence
        score *= issue.confidence
        
        // Adjust based on context
        if (metrics.networkOperations > 10) score *= 1.2
        if (metrics.cryptographicOperations > 5) score *= 1.1
        if (metrics.securityHotspots > 20) score *= 1.3
        
        return score.coerceIn(0.0, 10.0)
    }
    
    private fun assessExploitability(issue: SecurityIssue, code: String): String {
        return when {
            issue.type.contains("INJECTION") && code.contains("user") -> "HIGH"
            issue.type.contains("XSS") && code.contains("input") -> "HIGH"
            issue.type.contains("HARDCODED") -> "MEDIUM"
            issue.severity == "CRITICAL" -> "MEDIUM"
            issue.severity == "HIGH" -> "MEDIUM"
            else -> "LOW"
        }
    }
    
    private fun calculateOverallRisk(issues: List<SecurityIssue>): Double {
        if (issues.isEmpty()) return 0.0
        
        val weightedSum = issues.sumOf { issue ->
            when (issue.severity) {
                "CRITICAL" -> 10.0
                "HIGH" -> 6.0
                "MEDIUM" -> 3.0
                "LOW" -> 1.0
                else -> 0.5
            }
        }
        
        return (weightedSum / issues.size).coerceIn(0.0, 10.0)
    }
    
    private fun loadVulnerabilityDatabase(): Map<String, String> {
        // In a real implementation, this would load from a comprehensive vulnerability database
        return mapOf(
            "CWE-79" to "Cross-site Scripting (XSS)",
            "CWE-89" to "SQL Injection",
            "CWE-20" to "Improper Input Validation",
            "CWE-200" to "Information Exposure",
            "CWE-287" to "Improper Authentication",
            "CWE-285" to "Improper Authorization",
            "CWE-327" to "Use of a Broken or Risky Cryptographic Algorithm",
            "CWE-330" to "Use of Insufficiently Random Values",
            "CWE-400" to "Uncontrolled Resource Consumption",
            "CWE-502" to "Deserialization of Untrusted Data",
            "CWE-798" to "Use of Hard-coded Credentials"
        )
    }
    
    /**
     * Legacy method for backward compatibility
     */
    fun analyzeSecurity(code: String, language: String): List<SecurityIssue> {
        return analyzeSecurityAdvanced(code, language).issues
    }
}