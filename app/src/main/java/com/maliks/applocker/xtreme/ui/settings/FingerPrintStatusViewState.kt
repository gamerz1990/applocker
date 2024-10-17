package com.maliks.applocker.xtreme.ui.settings

import android.content.Context
import com.maliks.applocker.xtreme.R

data class FingerPrintStatusViewState(
    val isFingerPrintSupported: Boolean,
    val isFingerPrintRegistered: Boolean
) {

    fun getFingerPrintSettingTitle(context: Context): String {
        return when {
            !isFingerPrintSupported -> context.getString(R.string.setting_fingerprint_not_supported_title)
            !isFingerPrintRegistered -> context.getString(R.string.setting_fingerprint_not_registered_title)
            else -> context.getString(R.string.setting_fingerprint_title)
        }
    }

    fun getFingerPrintSettingSubtitle(context: Context): String {
        return when {
            !isFingerPrintSupported -> context.getString(R.string.setting_fingerprint_not_supported_description)
            !isFingerPrintRegistered -> context.getString(R.string.setting_fingerprint_not_registered_description)
            else -> context.getString(R.string.setting_fingerprint_description)
        }
    }

    val fingerPrintCheckBoxEnabled: Boolean
        get() = isFingerPrintSupported && isFingerPrintRegistered
}
