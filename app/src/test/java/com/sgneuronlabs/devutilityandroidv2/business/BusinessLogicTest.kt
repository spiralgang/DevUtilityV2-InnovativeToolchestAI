package com.sgneuronlabs.devutilityandroidv2.business

import org.junit.Test
import org.junit.Assert.*

class AICodingLogicTest {
    private val aiCodingLogic = AICodingLogic()

    @Test
    fun generateCodeSuggestion_test() {
        val input = "create a function"
        val language = "Kotlin"
        val result = aiCodingLogic.generateCodeSuggestion(input, language)
        
        assertTrue(result.contains(input))
        assertTrue(result.contains(language))
    }

    @Test
    fun analyzeCode_test() {
        val code = "fun test() {}"
        val result = aiCodingLogic.analyzeCode(code)
        
        assertTrue(result.isNotEmpty())
    }
}

class StorageLogicTest {
    private val storageLogic = StorageLogic()

    @Test
    fun optimizeStorage_test() {
        val data = "test data".toByteArray()
        val optimized = storageLogic.optimizeStorage(data, false)
        
        assertNotNull(optimized)
    }

    @Test
    fun restoreData_test() {
        val data = "test data".toByteArray()
        val compressed = storageLogic.optimizeStorage(data, false)
        val restored = storageLogic.restoreData(compressed, false)
        
        assertArrayEquals(data, restored)
    }
}