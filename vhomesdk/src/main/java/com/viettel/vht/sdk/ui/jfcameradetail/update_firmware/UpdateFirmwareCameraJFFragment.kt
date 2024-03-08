package com.viettel.vht.sdk.ui.jfcameradetail.update_firmware

import android.annotation.SuppressLint
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentUpdateFirmwareCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel.Companion.UPDATE_FAILED
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel.Companion.UPDATE_STATE_COMPLETED
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel.Companion.UPDATE_STATE_DOWNLOAD
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel.Companion.UPDATE_STATE_START
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel.Companion.UPDATE_STATE_UPGRADE
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class UpdateFirmwareCameraJFFragment :
    BaseFragment<FragmentUpdateFirmwareCameraJfBinding, UpdateFirmwareCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: UpdateFirmwareCameraJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_update_firmware_camera_jf

    override fun getVM() = viewModel
    override fun initView() {
        super.initView()
        viewModel.getConfig()
    }

    override fun setOnClick() {
        super.setOnClick()
        val onBack = {
            if (appNavigation.navController?.previousBackStackEntry?.destination?.label == "PairSuccessFragment") {
                requireActivity().finish()
            } else {
                appNavigation.navigateUp()
            }
        }
        binding.toolbar.setOnLeftClickListener {
            onBack()
        }
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        binding.btnUpdate.setOnClickListener {
            if (binding.btnUpdate.isEnabled) {
                viewModel.updateFirmware()
            }
        }
        binding.swStatusAutoUpdateFirmware.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (viewModel.autoUpdateFirmwareState.value == null || viewModel.autoUpdateFirmwareState.value == isChecked) {
                    binding.swStatusAutoUpdateFirmware.isChecked = !isChecked
                    return@setOnCheckedChangeListener
                }
                if (isChecked) {
                    viewModel.setStateAutoUpdateFirmware(true)
                } else {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.CONFIRM)
                        .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.auto_upgrade_firmware_confirm))
                        .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.text_confirm).uppercase())
                        .setOnPositivePressed { dialog ->
                            dialog.dismiss()
                            viewModel.setStateAutoUpdateFirmware(false)
                        }
                        .setTextNegativeButtonWithString(getString(com.vht.sdkcore.R.string.cancel_text).uppercase())
                        .setOnNegativePressed { dialog ->
                            dialog.dismiss()
                            binding.swStatusAutoUpdateFirmware.isChecked = true
                        }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.currentVersion.observe(viewLifecycleOwner) {
            binding.valueDeviceVersionCurrent.text = it
        }
        viewModel.latestVersion.observe(viewLifecycleOwner) {
            binding.valueDeviceVersionLatest.text = it
        }
        viewModel.isCanUpdateFirmware.observe(viewLifecycleOwner) {
            binding.tvInLatestVersion.isVisible = !it
            setStateBtnUpdate(it)
            binding.groupVersionLatest.isVisible = it
        }
        viewModel.updateState.observe(viewLifecycleOwner) {
            when (it.first) {
                UPDATE_STATE_START -> {
                    binding.ivProgressBar.clearAnimation()
                    binding.ivProgressBar.startAnimation(
                        RotateAnimation(
                            0f, 360f,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f
                        ).apply {
                            duration = 1000
                            repeatCount = Animation.INFINITE
                        }
                    )
                    binding.tvProgress.text = "${it.second}%"
                    setStateBtnUpdate(false)
                    binding.groupProcessUpdate.visible()
                    binding.clUpdateFirmwareAuto.gone()
                    binding.groupVersionCurrent.gone()
                    binding.groupVersionLatest.gone()
                    binding.tvInLatestVersion.gone()
                    binding.btnUpdate.gone()
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.SYNC)
                        .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.upgrading_firmware_description))
                        .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.ok).uppercase())
                        .setOnPositivePressed { dialog -> dialog.dismiss() }
                        .hideOptionButton()
                        .showPositiveButton()
                }

                UPDATE_STATE_DOWNLOAD, UPDATE_STATE_UPGRADE -> {
                    binding.tvProgress.text = "${it.second}%"
                }

                UPDATE_STATE_COMPLETED -> {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.SUCCESS)
                        .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.upgrade_firmware_success))
                        .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.text_close).uppercase())
                        .setOnPositivePressed { dialog ->
                            dialog.dismiss()
                            requireActivity().finish()
                        }
                        .hideOptionButton()
                        .showPositiveButton()
                }

                UPDATE_FAILED -> {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.ERROR)
                        .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.upgrade_firmware_failed))
                        .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.text_close).uppercase())
                        .setOnPositivePressed { dialog ->
                            dialog.dismiss()
                            if (appNavigation.navController?.previousBackStackEntry?.destination?.label == "PairSuccessFragment") {
                                requireActivity().finish()
                            } else {
                                appNavigation.navigateUp()
                            }
                        }
                        .hideOptionButton()
                        .showPositiveButton()
                }
            }
        }
        viewModel.autoUpdateFirmwareState.observe(viewLifecycleOwner) {
            binding.swStatusAutoUpdateFirmware.isChecked = it == true
        }
        viewModel.isUpdateAutoUpdateFirmwareSuccess.observe(viewLifecycleOwner) {
            if (it) {
                CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                    .showDialog()
                    .showCenterImage(DialogType.SUCCESS)
                    .setDialogTitleWithString(
                        if (viewModel.autoUpdateFirmwareState.value == true) getString(com.vht.sdkcore.R.string.auto_upgrade_firmware_on_success)
                        else getString(com.vht.sdkcore.R.string.auto_upgrade_firmware_off_success)
                    )
                    .hideOptionButton()
                lifecycleScope.launchWhenStarted {
                    delay(3000)
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .dismiss()
                }
            } else {
                CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                    .showDialog()
                    .showCenterImage(DialogType.ERROR)
                    .setDialogTitleWithString(
                        if (viewModel.autoUpdateFirmwareState.value == true) getString(com.vht.sdkcore.R.string.auto_upgrade_firmware_off_failed)
                        else getString(com.vht.sdkcore.R.string.auto_upgrade_firmware_on_failed)
                    )
                    .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.ok).uppercase())
                    .setOnPositivePressed { dialog -> dialog.dismiss() }
                    .hideOptionButton()
                    .showPositiveButton()
            }
        }
    }

    private fun setStateBtnUpdate(enable: Boolean) {
        if (enable) {
            binding.btnUpdate.setBackgroundResource(R.drawable.bg_button_setting_submit)
            binding.btnUpdate.isEnabled = true
        } else {
            binding.btnUpdate.setBackgroundResource(R.drawable.bg_button_setting_disable)
            binding.btnUpdate.isEnabled = false
        }
    }
}
