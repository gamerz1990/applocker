package com.maliks.applocker.xtreme.ui.permissiondialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.DialogUsagePermissionBinding
import com.maliks.applocker.xtreme.ui.BaseBottomSheetDialog
import com.maliks.applocker.xtreme.ui.permissiondialog.analytics.PermissionDialogAnayltics
import com.maliks.applocker.xtreme.ui.permissions.IntentHelper
import com.maliks.applocker.xtreme.util.delegate.inflate

class UsageAccessPermissionDialog : BaseBottomSheetDialog<UsageAccessPermissionViewModel>() {

    private val binding: DialogUsagePermissionBinding by inflate(R.layout.dialog_usage_permission)

    override fun getViewModel(): Class<UsageAccessPermissionViewModel> = UsageAccessPermissionViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.buttonPermit.setOnClickListener {
            activity?.let { PermissionDialogAnayltics.usagePermissionPermitClicked(it) }
            onPermitClicked()
        }
        binding.buttonCancel.setOnClickListener {
            activity?.let { PermissionDialogAnayltics.usagePermissionCancelClicked(it) }
            dismiss()
        }
        return binding.root
    }

    private fun onPermitClicked() {
        startActivity(IntentHelper.usageAccessIntent())
        dismiss()
    }

    companion object {

        fun newInstance(): AppCompatDialogFragment = UsageAccessPermissionDialog()
    }

}