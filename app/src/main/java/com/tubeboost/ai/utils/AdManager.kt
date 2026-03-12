package com.tubeboost.ai.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Centralized ad manager for all AdMob ad types
 * Uses TEST ad unit IDs - Replace with real IDs before publishing
 */
object AdManager {

    private const val TAG = "AdManager"

    // ─── TEST Ad Unit IDs (replace with real IDs before publishing) ─────────
    private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    // ─────────────────────────────────────────────────────────────────────────

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var generationCount = 0
    private val INTERSTITIAL_FREQUENCY = 3 // Show every 3 generations

    /**
     * Pre-load ads after SDK initialization
     */
    fun preloadAds(context: Context) {
        loadInterstitialAd(context)
        loadRewardedAd(context)
    }

    // ─── BANNER AD ────────────────────────────────────────────────────────────

    /**
     * Creates and loads a banner ad into the provided container
     */
    fun loadBannerAd(activity: Activity, container: ViewGroup) {
        try {
            val adView = AdView(activity)
            adView.setAdSize(AdSize.BANNER)
            adView.adUnitId = BANNER_AD_UNIT_ID

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Banner ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Banner ad failed to load: ${error.message}")
                    // Remove failed ad view to avoid empty space
                    container.removeAllViews()
                }

                override fun onAdOpened() {
                    Log.d(TAG, "Banner ad opened")
                }
            }

            container.removeAllViews()
            container.addView(adView)

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad: ${e.message}")
        }
    }

    // ─── INTERSTITIAL AD ──────────────────────────────────────────────────────

    /**
     * Loads an interstitial ad in background
     */
    fun loadInterstitialAd(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        Log.d(TAG, "Interstitial ad loaded")

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                interstitialAd = null
                                // Preload next interstitial
                                loadInterstitialAd(context)
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                interstitialAd = null
                                Log.e(TAG, "Interstitial failed to show: ${error.message}")
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Interstitial ad showed")
                            }
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        Log.e(TAG, "Interstitial failed to load: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading interstitial: ${e.message}")
        }
    }

    /**
     * Shows interstitial ad based on frequency counter
     * Returns true if ad was shown, false otherwise
     */
    fun showInterstitialIfReady(activity: Activity): Boolean {
        generationCount++
        return if (generationCount % INTERSTITIAL_FREQUENCY == 0 && interstitialAd != null) {
            try {
                interstitialAd?.show(activity)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error showing interstitial: ${e.message}")
                false
            }
        } else {
            false
        }
    }

    /**
     * Force show interstitial if available (ignores frequency counter)
     */
    fun showInterstitialNow(activity: Activity): Boolean {
        return if (interstitialAd != null) {
            try {
                interstitialAd?.show(activity)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error showing interstitial: ${e.message}")
                false
            }
        } else {
            false
        }
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null

    // ─── REWARDED AD ──────────────────────────────────────────────────────────

    /**
     * Loads a rewarded ad in background
     */
    fun loadRewardedAd(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        Log.d(TAG, "Rewarded ad loaded")

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                rewardedAd = null
                                // Preload next rewarded ad
                                loadRewardedAd(context)
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                rewardedAd = null
                                Log.e(TAG, "Rewarded ad failed to show: ${error.message}")
                            }
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading rewarded ad: ${e.message}")
        }
    }

    /**
     * Shows rewarded ad and calls the reward callback when user earns reward
     * @param onRewarded called with (rewardType, rewardAmount) when user completes viewing
     * @param onNotAvailable called when no ad is loaded
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (String, Int) -> Unit,
        onNotAvailable: () -> Unit = {}
    ) {
        if (rewardedAd == null) {
            onNotAvailable()
            // Preload for next time
            loadRewardedAd(activity)
            return
        }

        try {
            rewardedAd?.show(activity) { rewardItem ->
                val rewardType = rewardItem.type
                val rewardAmount = rewardItem.amount
                Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
                onRewarded(rewardType, rewardAmount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing rewarded ad: ${e.message}")
            onNotAvailable()
        }
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    /**
     * Reset counter (useful for testing)
     */
    fun resetGenerationCount() {
        generationCount = 0
    }
}
