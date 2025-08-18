package com.sgneuronlabs.devutilityandroidv2.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.script.ScriptEngineManager

class CompilationException(message: String) : Exception(message)
class RuntimeException(message: String) : Exception(message)

data class SettingsData(var theme: String = "light", var language: String = "kotlin", var fontSize: Int = 14)

class DevUtilityViewModelV2(application: Application) : AndroidViewModel(application) {
    private val _codeInput = MutableStateFlow("")
    val codeInput: StateFlow<String> = _codeInput

    private val _suggestedCode = MutableStateFlow("")
    val suggestedCode: StateFlow<String> = _suggestedCode

    private val _executionOutput = MutableStateFlow("")
    val executionOutput: StateFlow<String> = _executionOutput

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val settings = MutableStateFlow(SettingsData())

    private val sandboxDir = application.getExternalFilesDir(null) ?: throw IllegalStateException("Sandbox directory unavailable")
    private val aiService by lazy { AIService(getApiKey()) }
    private val codeSandbox = CodeSandbox(application)
    private val prefs = application.getSharedPreferences("DevUtilityPrefs", Context.MODE_PRIVATE)

    fun suggestCode(input: String) {
        viewModelScope.launch {
            try {
                val apiKey = getApiKey()
                if (apiKey.isEmpty()) {
                    _error.value = "Please set your OpenAI API key first"
                    return@launch
                }
                val aiResponse = aiService.generateCodeSuggestion(input, settings.value.language)
                _suggestedCode.value = aiResponse
                saveToSandbox(aiResponse, "suggestedCode.kt")
            } catch (e: Exception) {
                _error.value = "AI suggestion failed: ${e.message}"
            }
        }
    }

    fun executeCode(code: String) {
        viewModelScope.launch {
            try {
                if (code.length > 10000) {
                    _error.value = "Code exceeds maximum length"
                    return@launch
                }
                val result = codeSandbox.execute(code)
                _executionOutput.value = result
                saveToSandbox(result, "executionOutput.txt")
            } catch (compileError: CompilationException) {
                _executionOutput.value = "Compilation Error: ${compileError.message}"
            } catch (runtimeError: RuntimeException) {
                _executionOutput.value = "Runtime Error: ${runtimeError.message}"
            } catch (e: Exception) {
                _error.value = "Execution failed: ${e.message}"
            }
        }
    }

    private fun saveToSandbox(content: String, fileName: String) {
        val file = File(sandboxDir, fileName)
        try {
            file.writeText(content)
        } catch (e: Exception) {
            _error.value = "Failed to save file: ${e.message}"
        }
    }

    fun loadSandboxFile(fileName: String): String? {
        val file = File(sandboxDir, fileName)
        return try {
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            _error.value = "Failed to load file: ${e.message}"
            null
        }
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("user_api_key", apiKey).apply()
    }

    private fun getApiKey(): String {
        return prefs.getString("user_api_key", "")!!
    }

    fun getAISuggestion(code: String): String {
        return when {
            code.isEmpty() -> "fun example() { println(\"Hello, Galaxy!\") }"
            code.contains("fun") -> "// Add error handling?"
            else -> "// Complete this logic"
        }
    }
}

class CodeSandbox(private val context: Context) {
    private val sandboxDir = context.getExternalFilesDir(null) ?: throw IllegalStateException("Sandbox directory unavailable")
    private val scriptEngine by lazy {
        ScriptEngineManager().getEngineByExtension("kts") ?: throw IllegalStateException("Kotlin script engine not found")
    }

    fun execute(code: String): String {
        val scriptFile = File(sandboxDir, "TempScript.kts")
        val outputFile = File(sandboxDir, "output.txt")
        try {
            scriptFile.writeText(code)
            val output = StringBuilder()
            val originalOut = System.out
            val printStream = java.io.PrintStream(object : java.io.OutputStream() {
                override fun write(b: Int) {
                    output.append(b.toChar())
                }
            })
            System.setOut(printStream)
            scriptEngine.eval(code)
            System.setOut(originalOut)
            val result = output.toString().ifEmpty { "No output" }
            outputFile.writeText(result)
            return result
        } catch (e: javax.script.ScriptException) {
            throw if (e.message?.contains("error") == true) {
                CompilationException("Compilation failed: ${e.message}")
            } else {
                RuntimeException("Execution failed: ${e.message}")
            }
        } catch (e: Exception) {
            throw Exception("Sandbox error: ${e.message}")
        } finally {
            scriptFile.delete()
            outputFile.delete()
        }
    }
}