package com.sgneuronlabs.devutilityandroidv2.storage

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

class CustomCompressor {
    /**
     * Compresses the input data using the standard DEFLATE algorithm (via Java's Deflater).
     * @param input The data to compress.
     * @return The compressed data.
     */
    fun compress(input: ByteArray): ByteArray {
        return try {
            val deflater = Deflater(Deflater.BEST_COMPRESSION)
            val outputStream = ByteArrayOutputStream()
            deflater.setInput(input)
            deflater.finish()

            val buffer = ByteArray(1024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            outputStream.close()
            val compressedData = outputStream.toByteArray()
            compressedData
        } catch (e: Exception) {
            input // Return original data on error
        }
    }

    /**
     * Decompresses the input data using a custom algorithm inspired by DEFLATE.
     * @param input The compressed data.
     * @return The decompressed data.
     */
    fun decompress(input: ByteArray): ByteArray {
        return try {
            val inflater = Inflater()
            val outputStream = ByteArrayOutputStream()
            inflater.setInput(input)

            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                outputStream.write(buffer, 0, count)
            }
            outputStream.close()
            val decompressedData = outputStream.toByteArray()
            decompressedData
        } catch (e: Exception) {
            input // Return original data on error
        }
    }
}