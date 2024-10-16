package com.maliks.applocker.xtreme.ui.rateus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.DialogRateUsBinding
import com.maliks.applocker.xtreme.ui.BaseBottomSheetDialog
import com.maliks.applocker.xtreme.util.delegate.inflate

class RateUsDialog : BaseBottomSheetDialog<RateUsViewModel>() {

    private val binding: DialogRateUsBinding by inflate(R.layout.dialog_rate_us)

    override fun getViewModel(): Class<RateUsViewModel> = RateUsViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.buttonRate.setOnClickListener {
            onRatePositiveClicked()
        }
        binding.buttonLater.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    private fun onRatePositiveClicked() {
        viewModel.setUserRateUs()
        activity?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.maliks.applocker.xtreme"))) }
        dismiss()
    }

    companion object {

        fun newInstance(): AppCompatDialogFragment = RateUsDialog()
    }

}