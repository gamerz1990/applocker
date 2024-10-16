package com.maliks.applocker.xtreme.ui

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.service.ServiceStarter
import com.maliks.applocker.xtreme.service.worker.WorkerStarter
import com.maliks.applocker.xtreme.ui.permissions.PermissionChecker

class PermissionWizardActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: MaterialButton
    private lateinit var finishButton: MaterialButton
    private val steps = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_wizard)

        initializeSteps()

        viewPager = findViewById(R.id.viewPager)
        nextButton = findViewById(R.id.nextButton)
        finishButton = findViewById(R.id.finishButton)

        val adapter = PermissionWizardAdapter(this, steps)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == adapter.itemCount - 1) {
                    nextButton.visibility = MaterialButton.GONE
                    finishButton.visibility = MaterialButton.VISIBLE
                } else {
                    nextButton.visibility = MaterialButton.VISIBLE
                    finishButton.visibility = MaterialButton.GONE
                }
            }
        })

        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            val currentStepLayout = steps[currentItem]

            when (currentStepLayout) {
                R.layout.wizard_step_two -> {
                    val manufacturer = android.os.Build.MANUFACTURER.lowercase()
                    if (manufacturer == "google") {
                        requestIgnoreBatteryOptimizations()
                    } else {
                        openAutostartSettings()
                    }
                }
                R.layout.wizard_step_three -> {
                    requestUsageAccessPermission()
                }
                R.layout.wizard_step_four -> {
                    requestOverlayPermission()
                }
                R.layout.wizard_step_five -> {
                    requestIgnoreBatteryOptimizations()
                }
                R.layout.wizard_step_huawei -> {
                    openHuaweiProtectedAppsSettings()
                }
            }

            if (currentItem < steps.size - 1) {
                viewPager.currentItem = currentItem + 1
            } else {
                finish()
            }
        }

        finishButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeSteps() {
        steps.add(R.layout.wizard_step_one) // Intro step

        steps.add(R.layout.wizard_step_two) // Step 2: Autostart/Battery Optimization

        if (!isUsageAccessGranted()) {
            steps.add(R.layout.wizard_step_three)
        }

        if (!isOverlayPermissionGranted()) {
            steps.add(R.layout.wizard_step_four)
        }

        if (!isBatteryOptimizationIgnored()) {
            steps.add(R.layout.wizard_step_five)
        }

        if (isHuaweiDevice()) {
            steps.add(R.layout.wizard_step_huawei)
        }
    }

    private fun isUsageAccessGranted(): Boolean {
        try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            return false
        }
    }

    private fun isOverlayPermissionGranted(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun isHuaweiDevice(): Boolean {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        return manufacturer == "huawei" || manufacturer == "honor"
    }

    private fun openAutostartSettings() {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        try {
            val intent = when (manufacturer) {
                "xiaomi" -> Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                // Other manufacturers...

                else -> {
                    // For devices without specific autostart settings, open app settings
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open Autostart settings", Toast.LENGTH_LONG).show()
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun requestUsageAccessPermission() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open Usage Access settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestOverlayPermission() {
        if (!PermissionChecker.checkOverlayPermission(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun openHuaweiProtectedAppsSettings() {
        try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to open Huawei Protected Apps settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun startForegroundNotificationService() {
        ServiceStarter.startService(this)
        WorkerStarter.startServiceCheckerWorker()
    }

    override fun onResume() {
        super.onResume()
        // Optionally, check permissions and update UI if necessary
    }
}
