package com.biodataai.app.ui.viewmodel

import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkConnectivityViewModelTest {

    @Test
    fun testNetworkConnectivityUiStateInitial() {
        val state = NetworkConnectivityUiState()
        assertTrue(state.isOnline)
    }

    @Test
    fun testNetworkConnectivityUiStateOffline() {
        val state = NetworkConnectivityUiState(isOnline = false)
        assertTrue(!state.isOnline)
    }

    @Test
    fun testNetworkConnectivityUiStateOnline() {
        val state = NetworkConnectivityUiState(isOnline = true)
        assertTrue(state.isOnline)
    }
}
