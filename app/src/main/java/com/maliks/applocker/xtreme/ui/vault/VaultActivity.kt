package com.maliks.applocker.xtreme.ui.vault

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.*
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType
import com.maliks.applocker.xtreme.databinding.ActivityVaultBinding
import com.maliks.applocker.xtreme.ui.BaseActivity
import com.maliks.applocker.xtreme.ui.rateus.RateUsDialog
import com.maliks.applocker.xtreme.ui.vault.addingvaultdialog.AddToVaultDialog
import com.maliks.applocker.xtreme.ui.vault.analytics.VaultAdAnalytics
import com.maliks.applocker.xtreme.ui.vault.intent.VaultSelectorIntentHelper
import com.maliks.applocker.xtreme.util.ads.AdTestDevices

class VaultActivity : BaseActivity<VaultViewModel>() {

    private lateinit var binding: ActivityVaultBinding

    override fun getViewModel(): Class<VaultViewModel> = VaultViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vault)

        binding.viewPagerVault.adapter = VaultPagerAdapter(this, supportFragmentManager)
        binding.tabLayoutVault.setupWithViewPager(binding.viewPagerVault)
        binding.imageViewBack.setOnClickListener { finish() }
        binding.buttonAddToVault.setOnClickListener { addToVaultClicked() }

        showBannerAd()
    }

    // Register activity result launcher for selecting images/videos
    private val selectFilesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when (currentSelectionType) {
                VaultMediaType.TYPE_IMAGE -> handleSelectedFiles(data, VaultMediaType.TYPE_IMAGE)
                VaultMediaType.TYPE_VIDEO -> handleSelectedFiles(data, VaultMediaType.TYPE_VIDEO)
            }
        }
    }

    private var currentSelectionType = VaultMediaType.TYPE_IMAGE

    private fun addToVaultClicked() {
        proceedToAddFiles()
    }

    private fun proceedToAddFiles() {
        when (binding.viewPagerVault.currentItem) {
            0 -> {
                currentSelectionType = VaultMediaType.TYPE_IMAGE
                selectFilesLauncher.launch(VaultSelectorIntentHelper.selectMultipleImageIntent())
            }
            1 -> {
                currentSelectionType = VaultMediaType.TYPE_VIDEO
                selectFilesLauncher.launch(VaultSelectorIntentHelper.selectMultipleVideoIntent())
            }
            else -> {
                currentSelectionType = VaultMediaType.TYPE_IMAGE
                selectFilesLauncher.launch(VaultSelectorIntentHelper.selectMultipleImageIntent())
            }
        }
    }

    private fun handleSelectedFiles(data: Intent?, mediaType: VaultMediaType) {
        val uriList = arrayListOf<Uri>()

        if (data?.data != null) {
            val uri = data.data!!
            uriList.add(uri)
            // Take persistable URI permission
            contentResolver.takePersistableUriPermission(
                uri,
                data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            )
        } else if (data?.clipData != null) {
            val clipData = data.clipData!!
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                uriList.add(uri)
                // Take persistable URI permission
                contentResolver.takePersistableUriPermission(
                    uri,
                    data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                )
            }
        }

        if (uriList.isNotEmpty()) {
            AddToVaultDialog
                .newInstance(uriList, mediaType)
                .apply { onDismissListener = { showRateUsDialog() } }
                .also { it.show(supportFragmentManager, "") }
        } else {
            Toast.makeText(this, "No files selected.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRateUsDialog() {
        if (viewModel.shouldShowRateUs()) {
            RateUsDialog.newInstance().show(supportFragmentManager, "")
            viewModel.setRateUsAsked()
        }
    }

    private fun showBannerAd() {
        MobileAds.initialize(this) { initializationStatus ->
            // Optional: Handle initialization status
        }

        // Set test device IDs
        val testDeviceIds = AdTestDevices.DEVICES
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDeviceIds)
            .build()
        MobileAds.setRequestConfiguration(configuration)

        val mAdView = AdView(this).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = getString(R.string.banner_ad_unit_id)
            adListener = object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    VaultAdAnalytics.bannerAdClicked(this@VaultActivity)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    VaultAdAnalytics.bannerAdFailed(this@VaultActivity)
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    VaultAdAnalytics.bannerAdLoaded(this@VaultActivity)
                }
            }
        }

        binding.adContainer.addView(mAdView)

        // Build the ad request
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, VaultActivity::class.java)
        }
    }
}
