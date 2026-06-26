package com.biodataai.app.util

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoaded = false

    /**
     * Initialize Mobile Ads SDK (call once at app startup)
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context)
    }

    /**
     * Load interstitial ad for PDF export (called after successful export)
     * Note: In production, replace with actual Google AdMob Unit ID
     */
    fun loadInterstitialAd(
        context: Context,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (error: String) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()

        // Test Ad Unit ID for development/testing
        // Replace with real Ad Unit ID from Google AdMob console in production
        val adUnitId = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoaded = true
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                    isAdLoaded = false
                    onAdFailedToLoad("Ad failed to load: ${adError.message}")
                }
            }
        )
    }

    /**
     * Show interstitial ad if loaded
     * Returns true if ad was shown, false otherwise
     */
    fun showInterstitialAd(
        activity: android.app.Activity,
        onAdDismissed: () -> Unit = {}
    ): Boolean {
        if (interstitialAd != null && isAdLoaded) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdDismissed()
                    interstitialAd = null
                    isAdLoaded = false
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    isAdLoaded = false
                    onAdDismissed()
                }
            }

            interstitialAd?.show(activity)
            return true
        }
        return false
    }

    /**
     * Check if ad is loaded and ready to show
     */
    fun isAdReady(): Boolean {
        return isAdLoaded && interstitialAd != null
    }

    /**
     * Reset ad state (useful for testing)
     */
    fun resetAd() {
        interstitialAd = null
        isAdLoaded = false
    }
}
