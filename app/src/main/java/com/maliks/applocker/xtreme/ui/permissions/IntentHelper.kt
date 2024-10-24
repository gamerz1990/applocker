package com.maliks.applocker.xtreme.ui.permissions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings


object IntentHelper {

    fun overlayIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    }

    fun usageAccessIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    fun privacyPolicyWebIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("https://sc.hailz.co/privacy.html"))
    }

    fun rateUsIntent(): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.maliks.applocker.xtreme"))
    }

    fun startStorePage(activity: Activity) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Malik.co")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Malik.co"))
            )
        }
    }
}