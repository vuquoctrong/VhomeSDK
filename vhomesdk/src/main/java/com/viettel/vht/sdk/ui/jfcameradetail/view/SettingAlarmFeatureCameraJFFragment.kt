package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.lib.FunSDK
import com.lib.sdk.bean.HumanDetectionBean
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.visible
import com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period.ListAlarmVoicePeriodJFViewModel
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentSettingAlarmFeatureCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.SettingAlarmFeatureCameraJFViewModel
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view.BottomSheetSecurityZoneFragment
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view.JFSmartAlertSetFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetSettingAlarmPushIntervalJFFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetSettingVoiceTipsFragment
import com.viettel.vht.sdk.utils.AlarmVoicePeriodType
import com.viettel.vht.sdk.utils.BottomSheetSettingAlarmVoicePeriodJFFragment
import com.viettel.vht.sdk.utils.Config
import com.xm.ui.dialog.XMPromptDlg
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingAlarmFeatureCameraJFFragment :
    BaseFragment<FragmentSettingAlarmFeatureCameraJfBinding, SettingAlarmFeatureCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: SettingAlarmFeatureCameraJFViewModel by viewModels()

    private val listAlarmVoicePeriodJFViewModel: ListAlarmVoicePeriodJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_setting_alarm_feature_camera_jf

    override fun getVM() = viewModel

    override fun initView() {
        super.initView()
        initData()
    }

    private fun initData() {
        arguments.takeIf { it?.containsKey(Define.BUNDLE_KEY.PARAM_ID) == true }?.let {
            viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
        }
        viewModel.getConfig()
        listAlarmVoicePeriodJFViewModel.getConfig()
        binding.tvSetupZone.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.btSoundScheduleSetting.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.clSoundSetting.isVisible = viewModel.isSupportAlarmVoiceTips()
        if (listAlarmVoicePeriodJFViewModel.isSupportAlarmVoicePeriod()) {
            binding.groupSoundScheduleSetting.visible()
            binding.btSoundScheduleSetting.isVisible =
                listAlarmVoicePeriodJFViewModel.typeAlarmVoicePeriod.value == AlarmVoicePeriodType.SCHEDULE
        } else {
            binding.groupSoundScheduleSetting.gone()
        }
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.swHumanDetection.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed) {
                if (checked) {
                    if (viewModel.isDetectTrackEnable() && !viewModel.isMotionHumanDetection()) {
                        XMPromptDlg.onShow(
                            context,
                            FunSDK.TS("TR_Detect_Human_And_Detect_Track_To_Open_Human_Tip"),
                            {
                                viewModel.setHumanDetectEnable(true)
                            },
                            null
                        )
                    } else {
                        viewModel.setHumanDetectEnable(true)
                    }
                } else {
                    viewModel.setHumanDetectEnable(false)
                }
            }
        }
        binding.swMarkHumanDetection.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed) {
                if (viewModel.isTrackSupport()) {
                    viewModel.setAlarmTrackEnable(checked)
                } else {
                    showCustomToast(title = "Tính năng này không được hỗ trợ", onFinish = {})
                }
            }
        }
        binding.itemLowMotionSensitivity.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed) {
                if (checked) {
                    binding.itemMediumMotionSensitivity.isChecked = false
                    binding.itemHighMotionSensitivity.isChecked = false
                    viewModel.setAlarmSensitivityLevel(1)
                } else {
                    binding.itemLowMotionSensitivity.isChecked = true
                }
            }
        }
        binding.itemMediumMotionSensitivity.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed) {
                if (checked) {
                    binding.itemLowMotionSensitivity.isChecked = false
                    binding.itemHighMotionSensitivity.isChecked = false
                    viewModel.setAlarmSensitivityLevel(3)
                } else {
                    binding.itemMediumMotionSensitivity.isChecked = true
                }
            }
        }
        binding.itemHighMotionSensitivity.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed) {
                if (checked) {
                    binding.itemLowMotionSensitivity.isChecked = false
                    binding.itemMediumMotionSensitivity.isChecked = false
                    viewModel.setAlarmSensitivityLevel(6)
                } else {
                    binding.itemHighMotionSensitivity.isChecked = true
                }
            }
        }
        binding.tvValueSound.setOnClickListener {
            if (viewModel.voiceTipList.isEmpty()) return@setOnClickListener
            BottomSheetSettingVoiceTipsFragment.newInstance().show(
                childFragmentManager,
                BottomSheetSettingVoiceTipsFragment.TAG
            )
        }
        binding.tvValueSoundScheduleSetting.setOnClickListener {
            if (listAlarmVoicePeriodJFViewModel.alarmVoicePeriodResponse == null) return@setOnClickListener
            BottomSheetSettingAlarmVoicePeriodJFFragment.newInstance().show(
                childFragmentManager,
                BottomSheetSettingAlarmVoicePeriodJFFragment.TAG
            )
        }
        binding.btSoundScheduleSetting.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
            appNavigation.openListAlarmVoicePeriodJFCamera(bundle)
        }
        binding.clAlarmPushInterval.setOnClickListener {
            if (viewModel.alarmPushInterval.value == -1) return@setOnClickListener
            BottomSheetSettingAlarmPushIntervalJFFragment.newInstance().show(
                childFragmentManager,
                BottomSheetSettingAlarmPushIntervalJFFragment.TAG
            )
        }
        binding.tvTypeSettingZone.setOnClickListener {
            if (viewModel.getHumanDetection.value != null) {
                BottomSheetSecurityZoneFragment.newInstance().show(
                    childFragmentManager,
                    BottomSheetSecurityZoneFragment.TAG
                )
            }
        }
        binding.tvSetupZone.setOnClickListener {
            when (viewModel.getRuleType()) {
                Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE -> {
                    val bundle = Bundle().apply {
                        putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
                        putInt("RuleType", com.manager.db.Define.ALERT_lINE_TYPE)
                        putSerializable("HumanDetection", viewModel.getHumanDetection.value)
                        putSerializable(
                            "ChannelHumanRuleLimit",
                            viewModel.channelHumanRuleLimitBean
                        )
                    }
                    appNavigation.openSmartAlertSetJFCamera(bundle)
                }

                Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA -> {
                    val bundle = Bundle().apply {
                        putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
                        putInt("RuleType", com.manager.db.Define.ALERT_AREA_TYPE)
                        putSerializable("HumanDetection", viewModel.getHumanDetection.value)
                        putSerializable(
                            "ChannelHumanRuleLimit",
                            viewModel.channelHumanRuleLimitBean
                        )
                    }
                    appNavigation.openSmartAlertSetJFCamera(bundle)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindingStateView() {
        super.bindingStateView()

        viewModel.getHumanDetection.observe(viewLifecycleOwner) {
            binding.swHumanDetection.isChecked = it?.isEnable ?: false
            binding.viewDivider.isVisible = it?.isEnable ?: false
            binding.clMarkHumanDetection.isVisible = it?.isEnable ?: false
            binding.swMarkHumanDetection.isChecked =
                (it?.isShowTrack ?: false) && viewModel.isTrackSupport()

            when (viewModel.getRuleType()) {
                Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE -> {
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_security_fence)
                    binding.tvSetupZone.text = "Thiết lập hàng rào an ninh"
                    binding.tvSetupZone.visible()
                }

                Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA -> {
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_security_zone)
                    binding.tvSetupZone.text = "Thiết lập vùng an ninh"
                    binding.tvSetupZone.visible()
                }

                else -> {
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_default_zone_ipc)
                    binding.tvSetupZone.gone()
                }
            }
        }

        viewModel.setHumanDetection.observe(viewLifecycleOwner) {
            if (!it.second) {
                showCustomToast(title = "Cập nhật thất bại", onFinish = {})
            }
            binding.swHumanDetection.isChecked = it.first?.isEnable ?: false
            binding.viewDivider.isVisible = it.first?.isEnable ?: false
            binding.clMarkHumanDetection.isVisible = it.first?.isEnable ?: false
            binding.swMarkHumanDetection.isChecked =
                (it.first?.isShowTrack ?: false) && viewModel.isTrackSupport()

            when (viewModel.getRuleType()) {
                Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE -> {
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_security_fence)
                    binding.tvSetupZone.text = "Thiết lập hàng rào an ninh"
                    binding.tvSetupZone.visible()
                }

                Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA -> {
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_security_zone)
                    binding.tvSetupZone.text = "Thiết lập vùng an ninh"
                    binding.tvSetupZone.visible()
                }

                else -> {
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_default_zone_ipc)
                    binding.tvSetupZone.gone()
                }
            }
        }

        viewModel.getAlarmInfo.observe(viewLifecycleOwner) {
            setSensitivityLevel(it?.Level ?: 0)
        }

        viewModel.updateAlarmInfoState.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(title = "Cập nhật thất bại", onFinish = {})
            }
        }

        viewModel.voiceTip.observe(viewLifecycleOwner) {
            binding.tvValueSound.text = it?.voiceText ?: ""
            if (it?.voiceEnum == viewModel.voiceSilent.voiceEnum) {
                binding.groupSoundScheduleSetting.gone()
            } else {
                binding.groupSoundScheduleSetting.visible()
                binding.btSoundScheduleSetting.isVisible =
                    listAlarmVoicePeriodJFViewModel.typeAlarmVoicePeriod.value == AlarmVoicePeriodType.SCHEDULE
            }
        }

        doOnFragmentResult<HumanDetectionBean>(JFSmartAlertSetFragment.RESULT_ALERT_SET) {
            viewModel.securityFence(it)
        }

        viewModel.alarmPushInterval.observe(viewLifecycleOwner) {
            binding.tvValueAlarmPushInterval.text =
                BottomSheetSettingAlarmPushIntervalJFFragment.listAlarmPushInterval.find { item ->
                    item.interval == it
                }?.value ?: ""
        }

        viewModel.updateAlarmPushIntervalState.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(title = "Cập nhật thất bại", onFinish = {})
            }
        }

        listAlarmVoicePeriodJFViewModel.isLoading.observe(viewLifecycleOwner) {
            showHideLoading(it)
        }

        listAlarmVoicePeriodJFViewModel.typeAlarmVoicePeriod.observe(viewLifecycleOwner) {
            binding.tvValueSoundScheduleSetting.text = it?.content ?: ""
            if (binding.groupSoundScheduleSetting.isVisible) {
                binding.btSoundScheduleSetting.isVisible = it == AlarmVoicePeriodType.SCHEDULE
            }
        }
    }

    private fun setSensitivityLevel(level: Int) {
        binding.itemLowMotionSensitivity.isChecked = false
        binding.itemMediumMotionSensitivity.isChecked = false
        binding.itemHighMotionSensitivity.isChecked = false
        when (level) {
            in Int.MIN_VALUE..2 -> binding.itemLowMotionSensitivity.isChecked = true
            in 3..4 -> binding.itemMediumMotionSensitivity.isChecked = true
            in 5..Int.MAX_VALUE -> binding.itemHighMotionSensitivity.isChecked = true
        }
    }
}
