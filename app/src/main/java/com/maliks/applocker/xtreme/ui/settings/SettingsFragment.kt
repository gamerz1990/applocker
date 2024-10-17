package com.maliks.applocker.xtreme.ui.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.FragmentSettingsBinding
import com.maliks.applocker.xtreme.ui.BaseFragment
import com.maliks.applocker.xtreme.ui.intruders.IntrudersPhotosActivity
import com.maliks.applocker.xtreme.ui.newpattern.CreateNewPatternActivity
import com.maliks.applocker.xtreme.ui.permissiondialog.UsageAccessPermissionDialog
import com.maliks.applocker.xtreme.ui.permissions.PermissionChecker
import com.maliks.applocker.xtreme.ui.settings.analytics.SettingsAnalytics
import com.maliks.applocker.xtreme.util.delegate.inflate
import com.maliks.applocker.xtreme.util.extensions.toast

class SettingsFragment : BaseFragment<SettingsViewModel>() {

    private val binding: FragmentSettingsBinding by inflate(R.layout.fragment_settings)

    override fun getViewModel(): Class<SettingsViewModel> = SettingsViewModel::class.java

    // Define the permission launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setEnableIntrudersCatchers(true)
        } else {
            // Inform the user that the permission is necessary
            activity?.let {
                Toast.makeText(
                    it,
                    "Camera permission is required for Intruder Catcher.",
                    Toast.LENGTH_LONG
                ).show()
            }
            binding.switchEnableIntrudersCatcher.isChecked = false
            viewModel.setEnableIntrudersCatchers(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set lifecycleOwner and viewModel on the binding
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        // Set up listeners
        binding.imageViewLockAll.setOnClickListener {
            activity?.let { activity ->
                if (!PermissionChecker.checkUsageAccessPermission(activity)) {
                    UsageAccessPermissionDialog.newInstance()
                        .show(activity.supportFragmentManager, "")
                } else {
                    viewModel.onLockAllAppsClicked()
                }
            }
        }

        binding.layoutChangePattern.setOnClickListener {
            activity?.let {
                startActivityForResult(CreateNewPatternActivity.newIntent(it), RC_CHANGE_PATTERN)
            }
        }

        binding.switchStealth.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHiddenDrawingMode(isChecked)
        }

        binding.switchFingerPrint.setOnCheckedChangeListener { _, isChecked ->
            activity?.let { SettingsAnalytics.fingerPrintEnabled(it) }
            viewModel.setEnableFingerPrint(isChecked)
        }

        binding.switchEnableIntrudersCatcher.setOnCheckedChangeListener { _, isChecked ->
            activity?.let { SettingsAnalytics.intrudersEnabled(it) }
            enableIntrudersCatcher(isChecked)
        }

        binding.layoutIntrudersFolder.setOnClickListener {
            activity?.let {
                if (!viewModel.isIntrudersCatcherEnabled()) {
                    SettingsAnalytics.intrudersEnabled(it)
                    enableIntrudersCatcher(true)
                } else {
                    SettingsAnalytics.intrudersFolderClicked(it)
                    startActivity(IntrudersPhotosActivity.newIntent(it))
                }
            }
        }

        return binding.root
    }

    // Removed manual observers; data binding will handle LiveData observation

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_CHANGE_PATTERN -> {
                if (resultCode == Activity.RESULT_OK) {
                    activity?.let { it.toast(R.string.message_pattern_changed) }
                }
            }
        }
    }

    private fun enableIntrudersCatcher(isChecked: Boolean) {
        if (isChecked) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showPermissionsRationale()
            } else {
                // Request the CAMERA permission
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            viewModel.setEnableIntrudersCatchers(false)
        }
    }

    private fun showPermissionsRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera permission is needed to capture photos of intruders.")
            .setPositiveButton("Allow") { _, _ ->
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                binding.switchEnableIntrudersCatcher.isChecked = false
            }
            .create()
            .show()
    }

    companion object {
        private const val RC_CHANGE_PATTERN = 101
        fun newInstance() = SettingsFragment()
    }
}
