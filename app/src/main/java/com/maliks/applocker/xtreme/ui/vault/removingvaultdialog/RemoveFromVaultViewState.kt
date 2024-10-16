package com.maliks.applocker.xtreme.ui.vault.removingvaultdialog

import android.content.Context
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.ui.vault.addingvaultdialog.ProcessState

data class RemoveFromVaultViewState(val progress: Int, val processState: ProcessState) {

    fun getPercentText(context: Context): String {
        return context.getString(R.string.dialog_action_process, progress)
    }
}