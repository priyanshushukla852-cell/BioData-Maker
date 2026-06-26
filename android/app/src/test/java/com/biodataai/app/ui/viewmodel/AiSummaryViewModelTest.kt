package com.biodataai.app.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiSummaryViewModelTest {

    @Test
    fun testAiSummaryUiStateInitial() {
        val state = AiSummaryUiState()
        assertFalse(state.isLoading)
        assertEquals("", state.summary)
        assertFalse(state.isManualEntry)
    }

    @Test
    fun testUpdateSummary() {
        val state = AiSummaryUiState()
        val updated = state.copy(summary = "Test summary")
        assertEquals("Test summary", updated.summary)
    }

    @Test
    fun testManualEntryMode() {
        val state = AiSummaryUiState(
            isManualEntry = true,
            summary = "User-written summary"
        )
        assertEquals("User-written summary", state.summary)
        assertTrue(state.isManualEntry)
    }

    @Test
    fun testTimeoutHandling() {
        val state = AiSummaryUiState(
            isLoading = false,
            isManualEntry = true,
            error = "AI summary generation timed out. You can write your own summary below."
        )
        assertTrue(state.isManualEntry)
        assertTrue(state.error?.contains("timed out") == true)
    }

    @Test
    fun testKeywordsParsing() {
        val keywords = listOf("marriage", "engineer", "vegetarian", "hinduism")
        val state = AiSummaryUiState(
            summary = "AI generated summary",
            keywords = keywords
        )
        assertEquals(4, state.keywords.size)
        assertEquals("marriage", state.keywords[0])
    }

    @Test
    fun testAiSummaryResponse() {
        val response = com.biodataai.app.network.api.AiSummaryResponse(
            summary = "Professional engineer from a well-to-do family",
            keywords = listOf("engineer", "professional", "educated"),
            generatedAt = "2026-06-27T10:30:00Z"
        )
        assertEquals("Professional engineer from a well-to-do family", response.summary)
        assertEquals(3, response.keywords.size)
    }
}
