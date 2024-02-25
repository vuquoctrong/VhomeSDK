package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingCameraBinding
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible

class BottomSheetSettingCameraJFFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSettingCameraBinding>() {

    private val viewModel: SettingCameraJFViewModel by activityViewModels()
    private val updateFirmwareViewModel: UpdateFirmwareCameraJFViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_setting_camera

    override fun initView() {
        binding.llResetDefault.gone()
        binding.llFactoryReset.gone()

        if ((viewModel.errorSDKId.value ?: 0) < 0) {
            binding.llUpdateCamera.gone()
            binding.llEditCamera.gone()
            binding.llRebootDevice.gone()
        } else {
            binding.llUpdateCamera.visible()
            binding.llEditCamera.visible()
            binding.llRebootDevice.visible()
        }

        binding.llUpdateCamera.setOnClickListener {
            viewModel.openSettingLiveEvent.value = RxEvent(Config.EventKey.EVENT_UPDATE_FW_CAMERA)
            dismiss()
        }
        binding.llInformationCamera.setOnClickListener {
            viewModel.openSettingLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_SETTING_CAMERA_OPEN_INFORMATION_CAMERA)
            dismiss()
        }

        binding.llEditCamera.setOnClickListener {
            viewModel.openSettingLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_SETTING_CAMERA_SETTING_EDIT_NAME)
            dismiss()
        }
        binding.llRebootDevice.setOnClickListener {
            viewModel.openSettingLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_SETTING_CAMERA_REBOOT_DEVICE)
            dismiss()
        }
        binding.llResetDefault.setOnClickListener {
            viewModel.openSettingLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_SETTING_CAMERA_RESET_DEFAULT)
            dismiss()

        }
        binding.llFactoryReset.setOnClickListener {
            viewModel.openSettingLiveEvent.value =
                RxEvent(Config.EventKey.EVENT_SETTING_CAMERA_FACTORY_RESET)
            dismiss()
        }

        binding.hasNewFirmWareNotification.isVisible =
            updateFirmwareViewModel.isCanUpdateFirmware.value ?: false
    }


    override fun initControl() {
        // TODO
    }


}