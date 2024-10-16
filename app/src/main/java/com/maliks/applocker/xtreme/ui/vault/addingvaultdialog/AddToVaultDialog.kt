package com.maliks.applocker.xtreme.ui.vault.addingvaultdialog

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType.TYPE_IMAGE
import com.maliks.applocker.xtreme.data.database.vault.VaultMediaType.TYPE_VIDEO
import com.maliks.applocker.xtreme.databinding.DialogAddToVaultBinding
import com.maliks.applocker.xtreme.ui.BaseBottomSheetDialog
import com.maliks.applocker.xtreme.ui.vault.analytics.VaultAnalytics
import com.maliks.applocker.xtreme.util.delegate.inflate

class AddToVaultDialog : BaseBottomSheetDialog<AddToVaultViewModel>() {

    var onDismissListener: (() -> Unit)? = null

    private val binding: DialogAddToVaultBinding by inflate(R.layout.dialog_add_to_vault)

    override fun getViewModel(): Class<AddToVaultViewModel> = AddToVaultViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val uriList = arguments?.getParcelableArrayList<Uri>(KEY_URI_LIST)
        val mediaType = arguments?.getSerializable(KEY_MEDIA_TYPE) as VaultMediaType

        viewModel.getAddToVaultViewStateLiveData().observe(viewLifecycleOwner, Observer { viewState ->
            binding.viewState = viewState
            binding.executePendingBindings()

            if (viewState.processState == ProcessState.COMPLETE) {
                onDismissListener?.invoke()
                dismiss()

                when (mediaType) {
                    TYPE_IMAGE -> activity?.let { activity ->
                        uriList?.let { list ->
                            VaultAnalytics.addedImageVault(activity, list.size)
                        }
                    }
                    TYPE_VIDEO -> activity?.let { activity ->
                        uriList?.let { list ->
                            VaultAnalytics.addedVideoVault(activity, list.size)
                        }
                    }
                }
            }
        })

        uriList?.let { viewModel.setSelectedUriList(it, mediaType) }
    }

    companion object {

        private const val KEY_URI_LIST = "KEY_URI_LIST"
        private const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"

        fun newInstance(selectedUriList: ArrayList<Uri>, mediaType: VaultMediaType): AddToVaultDialog {
            return AddToVaultDialog().apply {
                isCancelable = false
                arguments = Bundle().apply {
                    putParcelableArrayList(KEY_URI_LIST, selectedUriList)
                    putSerializable(KEY_MEDIA_TYPE, mediaType)
                }
            }
        }
    }
}
