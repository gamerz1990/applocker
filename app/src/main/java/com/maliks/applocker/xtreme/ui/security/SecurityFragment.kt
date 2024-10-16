package com.maliks.applocker.xtreme.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.gms.ads.*
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.FragmentSecurityBinding
import com.maliks.applocker.xtreme.ui.BaseFragment
import com.maliks.applocker.xtreme.ui.background.BackgroundsActivity
import com.maliks.applocker.xtreme.ui.browser.BrowserActivity
import com.maliks.applocker.xtreme.ui.callblocker.CallBlockerActivity
import com.maliks.applocker.xtreme.ui.permissiondialog.UsageAccessPermissionDialog
import com.maliks.applocker.xtreme.ui.permissions.PermissionChecker
import com.maliks.applocker.xtreme.ui.security.analytics.SecurityFragmentAnalytics
import com.maliks.applocker.xtreme.ui.vault.VaultActivity
import com.maliks.applocker.xtreme.ui.vault.analytics.VaultAdAnalytics
import com.maliks.applocker.xtreme.util.ads.AdTestDevices
import com.maliks.applocker.xtreme.util.delegate.inflate

class SecurityFragment : BaseFragment<SecurityViewModel>() {

    private val binding: FragmentSecurityBinding by inflate(R.layout.fragment_security)

    private val adapter: AppLockListAdapter = AppLockListAdapter()

    override fun getViewModel(): Class<SecurityViewModel> = SecurityViewModel::class.java

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding.recyclerViewAppLockList.adapter = adapter

        binding.recyclerViewAppLockList.addOnScrollListener(
            MainActionsShowHideScrollListener(
                binding.layoutMainActions.root,
                resources.getDimension(R.dimen.main_actions_size) + resources.getDimension(R.dimen.margin_16dp)
            )
        )

//        binding.layoutMainActions.layoutCallBlocker.setOnClickListener {
//            activity?.let {
//                startActivity(CallBlockerActivity.newIntent(it))
//                SecurityFragmentAnalytics.onCallBlockerClicked(it)
//            }
//        }
        binding.layoutMainActions.layoutTheme.setOnClickListener {
            activity?.let {
                startActivity(BackgroundsActivity.newIntent(it))
                SecurityFragmentAnalytics.onBackgroundClicked(it)
            }
        }

        binding.layoutMainActions.layoutBrowser.setOnClickListener {
            activity?.let {
                startActivity(BrowserActivity.newIntent(it))
                SecurityFragmentAnalytics.onBrowserClicked(it)
            }
        }

        binding.layoutMainActions.layoutVault.setOnClickListener {
            activity?.let {
                startActivity(VaultActivity.newIntent(it))
                SecurityFragmentAnalytics.onVaultClicked(it)
            }
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        showBannerAd()

        viewModel.getAppDataListLiveData().observe(viewLifecycleOwner, Observer {
            adapter.setAppDataList(it)
        })

        adapter.appItemClicked = this@SecurityFragment::onAppSelected
    }

    private fun onAppSelected(selectedApp: AppLockItemItemViewState) {
        activity?.let {
            if (PermissionChecker.checkUsageAccessPermission(it).not()) {
                UsageAccessPermissionDialog.newInstance().show(it.supportFragmentManager, "")
            } else {
                if (selectedApp.isLocked) {
                    SecurityFragmentAnalytics.onAppUnlocked(it)
                    viewModel.unlockApp(selectedApp)
                } else {
                    SecurityFragmentAnalytics.onAppLocked(it)
                    viewModel.lockApp(selectedApp)
                }
            }
        }
    }

    private fun showBannerAd() {
        activity?.let { activity ->
            MobileAds.initialize(activity)

            // Set test device IDs
            val testDeviceIds = AdTestDevices.DEVICES
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            val mAdView = AdView(activity).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = getString(R.string.dashboard_banner_ad_unit_id)
                adListener = object : AdListener() {
                    override fun onAdClicked() {
                        super.onAdClicked()
                        VaultAdAnalytics.bannerAdClicked(activity)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        VaultAdAnalytics.bannerAdFailed(activity)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        VaultAdAnalytics.bannerAdLoaded(activity)
                    }
                }
            }

            binding.adLayout.addView(mAdView)

            // Build the ad request
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
        }
    }

    companion object {
        fun newInstance() = SecurityFragment()
    }
}
