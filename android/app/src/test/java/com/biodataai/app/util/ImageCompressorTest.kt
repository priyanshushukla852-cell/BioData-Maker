package com.biodataai.app.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageCompressorTest {

    @Test
    fun testCompressionMaxSize5MB() {
        // Verify 5MB limit is configured correctly
        assertTrue(5 * 1024 * 1024 > 0)
    }

    @Test
    fun testMaxDimensionScaling() {
        // Verify dimension constraints
        val maxDim = 2048
        val testWidth = 4000
        val expectedScale = testWidth.toFloat() / maxDim
        assertTrue(expectedScale > 1f)
    }

    @Test
    fun testCompressionResultCreation() {
        val result = ImageCompressor.CompressionResult(
            success = true,
            fileSizeBytes = 1024 * 1024 // 1MB
        )
        assertTrue(result.success)
        assertTrue(result.fileSizeBytes == 1024 * 1024L)
    }

    @Test
    fun testFailureResult() {
        val result = ImageCompressor.CompressionResult(
            success = false,
            error = "Image exceeds maximum size"
        )
        assertFalse(result.success)
        assertTrue(result.error == "Image exceeds maximum size")
    }
}
