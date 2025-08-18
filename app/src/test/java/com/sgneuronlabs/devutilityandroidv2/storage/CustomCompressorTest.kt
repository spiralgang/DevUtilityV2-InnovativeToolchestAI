package com.sgneuronlabs.devutilityandroidv2.storage

import org.junit.Test
import org.junit.Assert.*

class CustomCompressorTest {
    private val customCompressor = CustomCompressor()

    @Test
    fun compress_decompress_test() {
        val originalData = "Hello, World! This is a test string for compression.".toByteArray()
        
        val compressed = customCompressor.compress(originalData)
        val decompressed = customCompressor.decompress(compressed)
        
        assertArrayEquals(originalData, decompressed)
    }

    @Test
    fun compress_reduces_size() {
        val largeData = "A".repeat(1000).toByteArray()
        val compressed = customCompressor.compress(largeData)
        
        assertTrue("Compressed data should be smaller", compressed.size < largeData.size)
    }
}