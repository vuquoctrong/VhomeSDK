package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view

import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseBottomSheetFragment
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSecurityZoneBinding
import com.viettel.vht.sdk.ui.jfcameradetail.SettingAlarmFeatureCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.gone

class BottomSheetSecurityZoneFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSecurityZoneBinding>() {

    private val viewModel: SettingAlarmFeatureCameraJFViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_security_zone

    override fun initView() {
        binding.clSecurityFence.gone()
//        binding.cbSecurityFence.isChecked =
//            viewModel.getRuleType() == Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE
        binding.cbSafeZone.isChecked =
            viewModel.getRuleType() == Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA
        binding.cbDefaultZone.isChecked =
            viewModel.getRuleType() == Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT
//        binding.clSecurityFence.setOnClickListener {
//            viewModel.setRuleType(Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE)
//            dismiss()
//        }
        binding.clSafeZone.setOnClickListener {
            viewModel.setRuleType(Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA)
            dismiss()
        }
        binding.clDefaultZone.setOnClickListener {
            viewModel.setRuleType(Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT)
            dismiss()
        }
    }

    override fun initControl() {}

    companion object {
        const val TAG = "BottomSheetSecurityZoneFragment"
        fun newInstance() = BottomSheetSecurityZoneFragment()
    }
}