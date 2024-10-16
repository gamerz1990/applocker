package com.maliks.applocker.xtreme.ui.vault.removalconfirmationdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaEntity
import com.maliks.applocker.xtreme.databinding.DialogRemovalConfirmationBinding
import com.maliks.applocker.xtreme.ui.vault.removingvaultdialog.RemoveFromVaultDialog
import com.maliks.applocker.xtreme.util.delegate.inflate

class RemovalConfirmationDialog : BottomSheetDialogFragment() {

    private val binding: DialogRemovalConfirmationBinding by inflate(R.layout.dialog_removal_confirmation)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.buttonAccept.setOnClickListener {
            arguments?.getParcelable<VaultMediaEntity>(KEY_VAULT_MEDIA_ENTITY)?.let { vaultMediaEntity ->
                activity?.let {
                    dismiss()
                    RemoveFromVaultDialog.newInstance(vaultMediaEntity).show(it.supportFragmentManager, "")
                }
            }
        }

        return binding.root
    }

    companion object {
        private const val KEY_VAULT_MEDIA_ENTITY = "KEY_VAULT_MEDIA_ENTITY"

        fun newInstance(vaultMediaEntity: VaultMediaEntity): DialogFragment {
            return RemovalConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_VAULT_MEDIA_ENTITY, vaultMediaEntity)
                }
            }
        }
    }
}