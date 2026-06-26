package com.biodataai.app.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biodataai.app.util.AdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdsUiState(
    val isAdLoading: Boolean = false,
    val adError: String? = null,
    val isAdShown: Boolean = false
)

class AdsViewModel(context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(AdsUiState())
    val uiState: StateFlow<AdsUiState> = _uiState.asStateFlow()

    private val applicationContext = context.applicationContext

    init {
        // Initialize Mobile Ads SDK once at startup
        AdManager.initialize(applicationContext)
    }

    /**
     * Load interstitial ad for PDF export
     */
    fun loadAdForPdfExport() {
        _uiState.value = _uiState.value.copy(isAdLoading = true, adError = null)
        viewModelScope.launch {
            AdManager.loadInterstitialAd(
                applicationContext,
                onAdLoaded = {
                    _uiState.value = _uiState.value.copy(isAdLoading = false)
                },
                onAdFailedToLoad = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAdLoading = false,
                        adError = error
                    )
                }
            )
        }
    }

    /**
     * Show interstitial ad if loaded
     */
    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val adWasShown = AdManager.showInterstitialAd(activity) {
            _uiState.value = _uiState.value.copy(isAdShown = false)
            onAdDismissed()
        }

        if (adWasShown) {
            _uiState.value = _uiState.value.copy(isAdShown = true)
        }
    }

    /**
     * Check if ad is ready to show
     */
    fun isAdReady(): Boolean {
        return AdManager.isAdReady()
    }

    /**
     * Clear any ad errors
     */
    fun clearAdError() {
        _uiState.value = _uiState.value.copy(adError = null)
    }
}
