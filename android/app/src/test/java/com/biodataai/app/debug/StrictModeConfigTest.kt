package com.biodataai.app.debug

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrictModeConfigTest {

    @Test
    fun testStrictModeConfigInitialization() {
        assertNotNull(StrictModeConfig)
    }

    @Test
    fun testEnableStrictModeWithDebugFalse() {
        // When isDebug=false, StrictMode is not enabled (no Android context needed)
        try {
            StrictModeConfig.enableStrictMode(isDebug = false)
            assertTrue(true)
        } catch (e: Exception) {
            assertTrue(false)
        }
    }

    @Test
    fun testDebugConfigExists() {
        val config = StrictModeConfig
        assertNotNull(config)
        assertTrue(config::class.simpleName == "StrictModeConfig")
    }
}
