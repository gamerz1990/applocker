package com.maliks.applocker.xtreme.ui.callblocker.blacklist.delete

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.DialogBlackListItemDeleteBinding
import com.maliks.applocker.xtreme.ui.BaseBottomSheetDialog
import com.maliks.applocker.xtreme.util.delegate.inflate

class BlackListItemDeleteDialog : BaseBottomSheetDialog<BlackListItemDeleteViewModel>() {

    private val binding: DialogBlackListItemDeleteBinding by inflate(R.layout.dialog_black_list_item_delete)

    override fun getViewModel(): Class<BlackListItemDeleteViewModel> = BlackListItemDeleteViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.buttonDelete.setOnClickListener {
            onDeleteClicked()
        }
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    private fun onDeleteClicked() {
        viewModel.deleteFromBlackList(arguments?.getInt(KEY_BLACK_LIST_ID, -1) ?: -1)
        dismiss()
    }

    companion object {

        private const val KEY_BLACK_LIST_ID = "KEY_BLACK_LIST_ID"

        fun newInstance(blackListId: Int): AppCompatDialogFragment {
            return BlackListItemDeleteDialog().apply {
                arguments = Bundle().apply {
                    putInt(KEY_BLACK_LIST_ID, blackListId)
                }
            }
        }
    }

}