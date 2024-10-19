package com.maliks.applocker.xtreme.ui.main

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.ActivityMainBinding
import com.maliks.applocker.xtreme.ui.BaseActivity
import com.maliks.applocker.xtreme.ui.PermissionWizardActivity
import com.maliks.applocker.xtreme.ui.main.analytics.MainActivityAnalytics
import com.maliks.applocker.xtreme.ui.newpattern.CreateNewPatternActivity
import com.maliks.applocker.xtreme.ui.overlay.activity.OverlayValidationActivity
import com.maliks.applocker.xtreme.ui.permissions.PermissionChecker
import com.maliks.applocker.xtreme.ui.permissions.PermissionsActivity
import com.maliks.applocker.xtreme.ui.policydialog.PrivacyPolicyDialog
import com.maliks.applocker.xtreme.ui.rateus.RateUsDialog
import com.maliks.applocker.xtreme.util.AdManager
import com.maliks.applocker.xtreme.util.helper.NavigationIntentHelper

class MainActivity : BaseActivity<MainViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewPager.adapter = MainPagerAdapter(this, supportFragmentManager)
        binding.tablayout.setupWithViewPager(binding.viewPager)
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> MainActivityAnalytics.onSecurityTabSelected(this@MainActivity)
                    1 -> MainActivityAnalytics.onSettingsTabSelected(this@MainActivity)
                }
            }
        })

        binding.imageViewMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            AdManager.getInstance(this, getString(R.string.dashboard_ad_inter)).showInterstitialAd(this);
        }

        binding.navView.setNavigationItemSelectedListener(this)

        viewModel.getPatternCreationNeedLiveData().observe(this, Observer { isPatternCreateNeed ->
            when {
                isPatternCreateNeed -> {
                    startActivityForResult(
                        CreateNewPatternActivity.newIntent(this),
                        RC_CREATE_PATTERN
                    )
                }
                viewModel.isAppLaunchValidated().not() -> {
                    startActivityForResult(
                        OverlayValidationActivity.newIntent(this, this.packageName),
                        RC_VALIDATE_PATTERN
                    )
                }
            }
        })

        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isAutoStartPermissionRequested = sharedPreferences.getBoolean("auto_start_requested", false)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)
         if (isFirstLaunch) {
                    // Launch PermissionWizardActivity
                    val intent = Intent(this, PermissionWizardActivity::class.java)
                    startActivity(intent)

                    // Mark that the app has been launched
                    markAppAsLaunched()
        }


        if (!isAutoStartPermissionRequested) {
            requestAutoStartPermission(this)

            // Save the fact that we've requested auto-start permission
            with(sharedPreferences.edit()) {
                putBoolean("auto_start_requested", true)
                apply()
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
        AdManager.getInstance(this, getString(R.string.dashboard_ad_inter)).showInterstitialAd(this);

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_share -> startActivity(NavigationIntentHelper.getShareAppIntent())
            R.id.nav_rate_us -> startActivity(NavigationIntentHelper.getRateAppIntent())
            R.id.nav_feedback -> startActivity(NavigationIntentHelper.getFeedbackIntent())
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_CREATE_PATTERN -> {
                viewModel.onAppLaunchValidated()
                showPrivacyPolicyIfNeeded()
                if (resultCode != Activity.RESULT_OK) {
                    finish()
                }
            }
            RC_VALIDATE_PATTERN -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.onAppLaunchValidated()
                    showPrivacyPolicyIfNeeded()
                } else {
                    finish()
                }
            }
        }
    }

    private fun showPrivacyPolicyIfNeeded() {
        if (viewModel.isPrivacyPolicyAccepted().not()) {
            PrivacyPolicyDialog.newInstance().show(supportFragmentManager, "")
        }
        AdManager.getInstance(this, getString(R.string.dashboard_ad_inter)).showInterstitialAd(this);

    }
        private fun requestAutoStartPermission(context: Context) {
        try {
            val intent = Intent()
            val manufacturer = android.os.Build.MANUFACTURER.lowercase()
            when (manufacturer) {
                "xiaomi" -> intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                "huawei" -> intent.component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
                "oppo" -> intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
                "vivo" -> intent.component = ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
                "letv" -> intent.component = ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
                "asus" -> intent.component = ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.entry.FunctionActivity"
                )
                else -> Log.d("AutoStart", "Auto-start permission page not available for this manufacturer")
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AutoStart", "Error opening auto-start permission page: ${e.message}")
        }
    }


private fun markAppAsLaunched() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstLaunch", false)
        editor.apply()
    }
    companion object {
        private const val RC_CREATE_PATTERN = 2002
        private const val RC_VALIDATE_PATTERN = 2003
    }
}
