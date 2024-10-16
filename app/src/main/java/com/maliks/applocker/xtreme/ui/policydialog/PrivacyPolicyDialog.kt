package com.maliks.applocker.xtreme.ui.policydialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.DialogPrivacyPolicyBinding
import com.maliks.applocker.xtreme.ui.BaseBottomSheetDialog
import com.maliks.applocker.xtreme.ui.permissions.IntentHelper
import com.maliks.applocker.xtreme.ui.policydialog.analytics.PrivacyPolicyAnalytics
import com.maliks.applocker.xtreme.util.delegate.inflate

class PrivacyPolicyDialog : BaseBottomSheetDialog<PrivacyPolicyViewModel>() {

    private val binding: DialogPrivacyPolicyBinding by inflate(R.layout.dialog_privacy_policy)

    override fun getViewModel(): Class<PrivacyPolicyViewModel> = PrivacyPolicyViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.buttonAccept.setOnClickListener {
            activity?.let { PrivacyPolicyAnalytics.sendPrivacyPolicyAccept(it) }
            viewModel.acceptPrivacyPolicy()
            dismiss()
        }

        binding.textViewPrivacyPolicy.setOnClickListener {
            startActivity(IntentHelper.privacyPolicyWebIntent())
        }

        return binding.root
    }

    companion object {
        fun newInstance(): AppCompatDialogFragment {
            val dialog = PrivacyPolicyDialog()
            dialog.isCancelable = false
            return dialog
        }
    }

}