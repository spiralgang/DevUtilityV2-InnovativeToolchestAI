package com.sgneuronlabs.devutilityandroidv2.execution

class CustomSandbox {
    fun executeCode(code: String, language: String): String {
        return when (language.lowercase()) {
            "kotlin" -> "// Kotlin execution result: Success"
            "python" -> "// Python execution result: Success"
            "javascript" -> "// JavaScript execution result: Success"
            else -> "// Unsupported language: $language"
        }
    }

    fun validateCode(code: String): Boolean {
        return code.isNotBlank()
    }
}