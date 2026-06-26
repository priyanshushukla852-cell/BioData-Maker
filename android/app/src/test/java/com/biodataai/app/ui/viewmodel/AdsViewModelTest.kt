package com.biodataai.app.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdsViewModelTest {

    @Test
    fun testAdsUiStateInitial() {
        val state = AdsUiState()
        assertFalse(state.isAdLoading)
        assertFalse(state.isAdShown)
        assertNull(state.adError)
    }

    @Test
    fun testAdsUiStateLoading() {
        val state = AdsUiState(isAdLoading = true)
        assertTrue(state.isAdLoading)
        assertFalse(state.isAdShown)
    }

    @Test
    fun testAdsUiStateAdShown() {
        val state = AdsUiState(isAdShown = true)
        assertTrue(state.isAdShown)
        assertFalse(state.isAdLoading)
    }

    @Test
    fun testAdsUiStateError() {
        val state = AdsUiState(adError = "Ad load failed")
        assertEquals("Ad load failed", state.adError)
        assertFalse(state.isAdLoading)
    }

    @Test
    fun testAdsUiStateTransition() {
        // Loading -> Loaded
        val loadingState = AdsUiState(isAdLoading = true)
        val loadedState = loadingState.copy(isAdLoading = false)
        assertFalse(loadedState.isAdLoading)

        // Loaded -> Showing
        val showingState = loadedState.copy(isAdShown = true)
        assertTrue(showingState.isAdShown)
    }
}
