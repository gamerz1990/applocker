package com.maliks.applocker.xtreme.data.database.callblocker.addtoblacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.DialogCallBlockerAddToBlacklistBinding
import com.maliks.applocker.xtreme.ui.BaseBottomSheetDialog
import com.maliks.applocker.xtreme.util.delegate.inflate

class AddToBlackListDialog : BaseBottomSheetDialog<AddToBlackListViewModel>() {

    private val binding: DialogCallBlockerAddToBlacklistBinding by inflate(R.layout.dialog_call_blocker_add_to_blacklist)

    override fun getViewModel(): Class<AddToBlackListViewModel> = AddToBlackListViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding.buttonBlock.setOnClickListener {
            if (validateInputFields()) {
                viewModel.blockNumber(
                    binding.editTextName.text.toString(),
                    binding.editTextPhoneNumber.text.toString()
                )
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getViewStateLiveData()
            .observe(viewLifecycleOwner, Observer {
                dismiss()
            })
    }

    private fun validateInputFields(): Boolean {
        return !binding.editTextPhoneNumber.text.isNullOrEmpty()
    }

    companion object {
        fun newInstance(): AppCompatDialogFragment = AddToBlackListDialog()
    }
}
