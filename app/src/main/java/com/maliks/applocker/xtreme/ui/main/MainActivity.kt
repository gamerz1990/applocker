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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.maliks.applocker.xtreme.ui.policydialog.PrivacyPolicyDialog
import com.maliks.applocker.xtreme.util.AdManager
import com.maliks.applocker.xtreme.util.helper.NavigationIntentHelper
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity<MainViewModel>(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    // Activity Result Launchers
    private lateinit var createPatternLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayValidationLauncher: ActivityResultLauncher<Intent>

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        setupActivityResultLaunchers()
        setupViewPager()
        setupNavigationDrawer()
        handleFirstLaunch()
        handleAutoStartPermission()
    }

    private fun setupActivityResultLaunchers() {
        createPatternLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.onAppLaunchValidated()
                showPrivacyPolicyIfNeeded()
            } else {
                finish()
            }
        }

        overlayValidationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.onAppLaunchValidated()
            } else {
                finish()
            }
        }
    }

    private fun setupViewPager() {
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
    }

    private fun setupNavigationDrawer() {
        binding.imageViewMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            AdManager.getInstance(this, getString(R.string.dashboard_ad_inter)).showInterstitialAd(this)
        }
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun handleFirstLaunch() {
        val isFirstLaunch = sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            val intent = Intent(this, PermissionWizardActivity::class.java)
            startActivity(intent)
            markAppAsLaunched()
        }
    }

    private fun handleAutoStartPermission() {
        val isAutoStartPermissionRequested = sharedPreferences.getBoolean(KEY_AUTO_START_REQUESTED, false)
        if (!isAutoStartPermissionRequested) {
            requestAutoStartPermission(this)
            sharedPreferences.edit().putBoolean(KEY_AUTO_START_REQUESTED, true).apply()
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
        AdManager.getInstance(this, getString(R.string.dashboard_ad_inter)).showInterstitialAd(this)
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

    private fun showPrivacyPolicyIfNeeded() {
        if (!viewModel.isPrivacyPolicyAccepted()) {
            PrivacyPolicyDialog.newInstance().show(supportFragmentManager, "")
        }
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
                else -> {
                    Snackbar.make(binding.root, R.string.auto_start_not_supported, Snackbar.LENGTH_LONG).show()
                    return
                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AutoStart", "Error opening auto-start permission page: ${e.message}")
            Snackbar.make(binding.root, R.string.auto_start_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun markAppAsLaunched() {
        sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply()
    }

    companion object {
        private const val APP_PREFERENCES = "app_preferences"
        private const val KEY_AUTO_START_REQUESTED = "auto_start_requested"
        private const val KEY_IS_FIRST_LAUNCH = "isFirstLaunch"
        private const val RC_CREATE_PATTERN = 2002
        private const val RC_VALIDATE_PATTERN = 2003
    }
}
