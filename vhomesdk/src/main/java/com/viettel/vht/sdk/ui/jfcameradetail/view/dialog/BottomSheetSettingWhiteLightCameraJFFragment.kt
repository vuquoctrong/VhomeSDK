package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import androidx.fragment.app.activityViewModels
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingWhiteLightCameraJfBinding
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel

class BottomSheetSettingWhiteLightCameraJFFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSettingWhiteLightCameraJfBinding>() {

    private val viewModel: SettingCameraJFViewModel by activityViewModels()

    override val layoutId: Int = R.layout.fragment_bottom_sheet_setting_white_light_camera_jf

    override fun initView() {
        when (viewModel.getWhiteLightState.value) {
            WhiteLight.CLOSE -> {
                binding.cbClose.isChecked = true
                binding.cbAuto.isChecked = false
                binding.cbIntelligent.isChecked = false
                binding.cbOff.isChecked = false
            }

            WhiteLight.AUTO -> {
                binding.cbClose.isChecked = false
                binding.cbAuto.isChecked = true
                binding.cbIntelligent.isChecked = false
                binding.cbOff.isChecked = false
            }

            WhiteLight.INTELLIGENT -> {
                binding.cbClose.isChecked = false
                binding.cbAuto.isChecked = false
                binding.cbIntelligent.isChecked = true
                binding.cbOff.isChecked = false
            }

            WhiteLight.OFF -> {
                binding.cbClose.isChecked = false
                binding.cbAuto.isChecked = false
                binding.cbIntelligent.isChecked = false
                binding.cbOff.isChecked = true
            }

            else -> {}
        }
    }

    override fun initControl() {
        binding.apply {
            viewClickClose.setOnClickListener {
                if (!binding.cbClose.isChecked) {
                    viewModel.setWhiteLightState(WhiteLight.CLOSE)
                }
                dismissDialog()
            }
            viewClickAuto.setOnClickListener {
                if (!binding.cbAuto.isChecked) {
                    viewModel.setWhiteLightState(WhiteLight.AUTO)
                }
                dismissDialog()
            }
            viewClickIntelligent.setOnClickListener {
                if (!binding.cbIntelligent.isChecked) {
                    viewModel.setWhiteLightState(WhiteLight.INTELLIGENT)
                }
                dismissDialog()
            }
            viewClickOff.setOnClickListener {
                if (!binding.cbOff.isChecked) {
                    viewModel.setWhiteLightState(WhiteLight.OFF)
                }
                dismissDialog()
            }
        }
    }

    companion object {
        const val TAG = "BottomSheetSettingWhiteLightCameraJFFragment"
        fun newInstance() = BottomSheetSettingWhiteLightCameraJFFragment()
    }
}

enum class WhiteLight(val value: String) {
    CLOSE(value = "Close"),
    AUTO(value = "Auto"),
    INTELLIGENT(value = "Intelligent"),
    OFF(value = "Off")
}
