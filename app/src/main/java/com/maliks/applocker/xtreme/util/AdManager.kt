// AdManager.kt
package com.maliks.applocker.xtreme.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager private constructor(private val context: Context, private val adId: String) {

    companion object {
        private const val TAG = "AdManager"
        private var INSTANCE: AdManager? = null
        private var lastAdShownTime: Long = 0
        private const val AD_SHOW_INTERVAL = 40000L // 20 seconds

        // Singleton instance accessor
        fun getInstance(context: Context, adId: String): AdManager {
            if (INSTANCE == null) {
                INSTANCE = AdManager(context.applicationContext, adId)
            }

            return INSTANCE!!
        }

        fun getCreated(): AdManager? {
            return INSTANCE
        }
    }

    // Preloaded ad instance
    private var preloadedAd: InterstitialAd? = null

    init {
        preloadAdInBackground()
    }

    /**
     * Preload an interstitial ad in the background.
     */
    private fun preloadAdInBackground() {
        if (preloadedAd != null) {
            Log.d(TAG, "An ad is already preloaded.")
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    preloadedAd = ad
                    Log.d(TAG, "Preloaded interstitial ad.")
                    setAdCallbacks()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    preloadedAd = null
                    Log.e(TAG, "Failed to preload interstitial ad: ${loadAdError.message}")
                    // Optionally, retry loading after a delay
                }
            },
        )
    }

    /**
     * Set callbacks for the preloaded ad.
     */
    private fun setAdCallbacks() {
        preloadedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Preloaded ad was dismissed.")
                preloadedAd = null
                preloadAdInBackground() // Preload next ad
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Preloaded ad failed to show: ${adError.message}")
                preloadedAd = null
                preloadAdInBackground() // Preload next ad
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Preloaded ad showed fullscreen content.")
            }
        }
    }

    /**
     * Show the interstitial ad if it is preloaded and 40 seconds have passed since the last ad was shown.
     * Updated to make it accessible from a fragment without requiring an Activity context.
     */
    fun showInterstitialAd(activity: Activity?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAdShownTime >= AD_SHOW_INTERVAL) {
            if (preloadedAd != null) {
                preloadedAd?.show(activity ?: return)
                lastAdShownTime = currentTime
            } else {
                Log.d(TAG, "Interstitial ad is not ready yet.")
            }
        } else {
            Log.d(TAG, "Ad cannot be shown yet. Please wait for the interval to pass.")
        }
    }

    /**
     * Retrieve the preloaded ad.
     * Returns the preloaded ad if available, else null.
     */
    fun getPreloadedAd(): InterstitialAd? {
        return preloadedAd
    }

    fun getAdId(): String {
        return adId
    }
}
