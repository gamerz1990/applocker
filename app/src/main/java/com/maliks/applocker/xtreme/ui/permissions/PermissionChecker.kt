package com.maliks.applocker.xtreme.ui.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build
import android.provider.Settings

object PermissionChecker {

    fun checkUsageAccessPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return true
        }

        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid, applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED

        } catch (e: NameNotFoundException) {
            false
        }
    }

    fun checkOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            if (context is android.app.Activity) {
                context.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION_CODE)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    fun isAllPermissionChecked(context: Context) =
        checkUsageAccessPermission(context) && checkOverlayPermission(context)

    const val REQUEST_OVERLAY_PERMISSION_CODE = 101
}
