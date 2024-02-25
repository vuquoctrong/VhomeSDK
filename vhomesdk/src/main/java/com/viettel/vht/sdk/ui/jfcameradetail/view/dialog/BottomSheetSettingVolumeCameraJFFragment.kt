package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.lib.sdk.bean.DevVolumeBean
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingVolumeCameraBinding
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel

class BottomSheetSettingVolumeCameraJFFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSettingVolumeCameraBinding>() {

    private val viewModel: SettingCameraJFViewModel by activityViewModels()
    private var type = TYPE_VOLUME

    companion object {
        const val TAG = "BottomSheetSettingVolumeCameraJFFragment"
        const val TYPE_VOLUME = 0
        const val TYPE_MIC = 1
        fun newInstance(type: Int): BottomSheetSettingVolumeCameraJFFragment =
            BottomSheetSettingVolumeCameraJFFragment().apply {
                val bundle = bundleOf(
                    Define.BUNDLE_KEY.PARAM_TYPE to type
                )
                arguments = bundle
            }
    }

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_setting_volume_camera

    override fun initView() {
        arguments?.let {
            type = it.getInt(Define.BUNDLE_KEY.PARAM_TYPE)
        }
        binding.tvTitle.text = when (type) {
            TYPE_VOLUME -> getString(com.vht.sdkcore.R.string.string_setting_volume_value)
            TYPE_MIC -> getString(com.vht.sdkcore.R.string.string_setting_value_volume_mic)
            else -> getString(com.vht.sdkcore.R.string.string_setting_volume_value)
        }
        binding.seekbar.max = DevVolumeBean.VOLUME_MAX
        binding.seekbar.progress = getCurrentVolume()
        binding.tvValue.text = getCurrentVolume().toString()
    }

    override fun initControl() {
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.tvValue.text = (p0?.progress ?: getCurrentVolume()).toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (binding.seekbar.progress != getCurrentVolume()) {
            when (type) {
                TYPE_VOLUME -> viewModel.setVolume(binding.seekbar.progress)
                TYPE_MIC -> viewModel.setMicVolume(binding.seekbar.progress)
            }
        }
    }

    private fun getCurrentVolume() = when (type) {
        TYPE_VOLUME -> viewModel.currentVolumeBean?.leftVolume ?: 0
        TYPE_MIC -> viewModel.currentMicVolumeBean?.leftVolume ?: 0
        else -> 0
    }
}