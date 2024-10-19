package com.maliks.applocker.xtreme.ui.overlay.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.andrognito.patternlockview.PatternLockView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.ViewPatternOverlayBinding
import com.maliks.applocker.xtreme.ui.newpattern.SimplePatternListener
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.maliks.applocker.xtreme.data.AppLockerPreferences
import com.maliks.applocker.xtreme.ui.background.GradientBackgroundDataProvider
import com.maliks.applocker.xtreme.ui.overlay.OverlayValidateType
import com.maliks.applocker.xtreme.ui.overlay.OverlayViewState
import com.maliks.applocker.xtreme.ui.vault.analytics.VaultAdAnalytics
import com.maliks.applocker.xtreme.util.ads.AdTestDevices

class PatternOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var onPatternCompleted: ((List<PatternLockView.Dot>) -> Unit)? = null

    private var appLockerPreferences = AppLockerPreferences(context.applicationContext)

    val binding: ViewPatternOverlayBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_pattern_overlay, this, true)

    init {
        binding.patternLockView.addPatternLockListener(object : SimplePatternListener() {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                super.onComplete(pattern)
                pattern?.let { onPatternCompleted?.invoke(it) }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showBannerAd();
        updateSelectedBackground()
        binding.patternLockView.clearPattern()
        binding.viewState = OverlayViewState()
    }

    fun observePattern(onPatternCompleted: (List<PatternLockView.Dot>) -> Unit) {
        this.onPatternCompleted = onPatternCompleted
    }

    fun notifyDrawnWrong() {
        binding.patternLockView.clearPattern()
        binding.viewState =
            OverlayViewState(
                overlayValidateType = OverlayValidateType.TYPE_PATTERN,
                isDrawnCorrect = false
            )
        YoYo.with(Techniques.Shake)
            .duration(700)
            .playOn(binding.textViewPrompt)
    }

    fun notifyDrawnCorrect() {
        binding.patternLockView.clearPattern()
        binding.viewState =
            OverlayViewState(
                overlayValidateType = OverlayValidateType.TYPE_PATTERN,
                isDrawnCorrect = true
            )
    }

    fun setHiddenDrawingMode(isHiddenDrawingMode: Boolean) {
        binding.patternLockView.isInStealthMode = isHiddenDrawingMode
    }

    fun setAppPackageName(appPackageName: String) {
        try {
            val icon = context.packageManager.getApplicationIcon(appPackageName)
            binding.avatarLock.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            binding.avatarLock.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_round_lock_24px))
            e.printStackTrace()
        }
    }

    private fun updateSelectedBackground() {
        val selectedBackgroundId = appLockerPreferences.getSelectedBackgroundId()
        GradientBackgroundDataProvider.gradientViewStateList.forEach {
            if (it.id == selectedBackgroundId) {
                binding.layoutOverlayMain.background = it.getGradiendDrawable(context)
            }
        }
    }

    private fun showBannerAd() {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(context) { initializationStatus ->
            // Optional: Handle initialization status if needed
        }

        // Set test device IDs
        val testDeviceIds = AdTestDevices.DEVICES
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        val mAdView = AdView(context).apply {
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            adUnitId = context.getString(R.string.overlay_banner_ad_unit_id)
            adListener = object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    VaultAdAnalytics.bannerAdClicked(context)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    VaultAdAnalytics.bannerAdFailed(context)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    VaultAdAnalytics.bannerAdLoaded(context)
                }
            }
        }

        binding.adView.addView(mAdView)

        // Build the ad request
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}