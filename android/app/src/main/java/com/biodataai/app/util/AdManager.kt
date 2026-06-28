package com.biodataai.app.util

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions

object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoaded = false

    private var rewardedAd: RewardedAd? = null

    /**
     * Initialize Mobile Ads SDK (call once at app startup)
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context)
    }

    /**
     * Load interstitial ad for PDF export (called after successful export)
     * Uses Google's test Ad Unit ID for development/testing.
     * For production, replace with your actual AdMob Unit ID from Google AdMob console.
     */
    fun loadInterstitialAd(
        context: Context,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (error: String) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()

        // Google's test Interstitial Ad Unit ID (safe for development/testing)
        // Production: replace with real Ad Unit ID from https://admob.google.com
        val adUnitId = "ca-app-pub-3940256099942544/1033173712"

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

    // --- Rewarded ad: "watch an ad to unlock one more AI summary" after the daily cap ---
    //
    // The grant is recorded server-side via AdMob Server-Side Verification (SSV): we set the
    // user's Firebase uid as SSV custom data, AdMob calls the backend's /api/admob/ssv with it,
    // and the backend (after verifying Google's signature) credits +1 generation. The app does
    // not grant anything itself — it just shows the ad and then re-checks/retries the request.

    /**
     * Pre-load a rewarded ad. Uses Google's test Rewarded Ad Unit ID for development.
     * Production: replace with the real Rewarded Ad Unit ID from https://admob.google.com and
     * set ADMOB_REWARDED_AD_UNIT_ID on the backend so SSV callbacks are accepted.
     */
    fun loadRewardedAd(
        context: Context,
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (error: String) -> Unit = {}
    ) {
        val adRequest = AdRequest.Builder().build()
        // Google's test Rewarded Ad Unit ID (safe for development/testing).
        val adUnitId = "ca-app-pub-3940256099942544/5224354917"

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: com.google.android.gms.ads.LoadAdError) {
                    rewardedAd = null
                    onAdFailedToLoad("Rewarded ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    /**
     * Show the rewarded ad, tagging it with the user's Firebase uid for SSV so the backend can
     * credit the right user. [onRewardEarned] fires when the user completes the ad; the caller
     * should then re-check the AI quota / retry the summary (the backend grant lands via SSV).
     *
     * @return true if an ad was shown, false if none was loaded
     */
    fun showRewardedAd(
        activity: android.app.Activity,
        firebaseUid: String,
        onRewardEarned: () -> Unit = {},
        onAdDismissed: () -> Unit = {}
    ): Boolean {
        val ad = rewardedAd ?: return false

        ad.setServerSideVerificationOptions(
            ServerSideVerificationOptions.Builder()
                .setCustomData(firebaseUid)
                .build()
        )
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                rewardedAd = null
                onAdDismissed()
            }
        }
        ad.show(activity) { onRewardEarned() }
        return true
    }
}
