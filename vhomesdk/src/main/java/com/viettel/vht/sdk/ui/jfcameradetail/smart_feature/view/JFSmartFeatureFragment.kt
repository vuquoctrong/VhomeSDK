package com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.lib.sdk.bean.HumanDetectionBean
import com.manager.db.Define.ALERT_AREA_TYPE
import com.manager.db.Define.ALERT_lINE_TYPE
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfSmartFeatureBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.IAlarmPushSetView
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.IHumanDetectView
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.viettel.vht.sdk.utils.Config
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class JFSmartFeatureFragment :
    BaseFragment<FragmentJfSmartFeatureBinding, SettingCameraJFViewModel>(), IHumanDetectView,
    IAlarmPushSetView {

    private val TAG = JFSmartFeatureFragment::class.simpleName

    @Inject
    lateinit var appNavigation: AppNavigation

    override val layoutId = R.layout.fragment_jf_smart_feature

    private val viewModel: SettingCameraJFViewModel by activityViewModels()

    override fun getVM() = viewModel

    private lateinit var devId: String


    override fun initView() {
        super.initView()
        viewModel.setIHumanDetectView(this)
        viewModel.setIAlarmPushSetView(this)
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                devId = it.getString(Define.BUNDLE_KEY.PARAM_ID, "")
            }
        }
        if (viewModel.getRuleType() == HumanDetectionBean.IA_TRIPWIRE) {
            binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_security_fence)
        } else if (viewModel.getRuleType() == HumanDetectionBean.IA_PERIMETER) {
            binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_safe_zone)
        }
        setupViewMotionDetect()
    }

    fun setupViewMotionDetect() {
        val level: Int = viewModel.getAlarmSensitivityLevel()
        if (level <= 2) {
            binding.rbLow.isChecked = true
        } else if (level <= 4) { //3 4
            binding.rbMedium.isChecked = true
        } else if (level <= 6) { // 5 6
            binding.rbHigh.isChecked = true
        }
        binding.rbLow.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                showHideLoading(true)
                viewModel.setAlarmSensitivityLevel(1)
            }
        }
        binding.rbMedium.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                showHideLoading(true)
                viewModel.setAlarmSensitivityLevel(3)
            }
        }
        binding.rbHigh.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                showHideLoading(true)
                viewModel.setAlarmSensitivityLevel(6)
            }
        }
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.toolBar.setOnRightClickListener {
            showHideLoading(viewModel.saveHumanDetect())
        }
        binding.tvSetupZone.setOnClickListener {
            BottomSheetSecurityZoneFragment().show(
                childFragmentManager,
                "BottomSheetSecurityZoneFragment"
            )
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.openSecurityFenceEvent.observe(viewLifecycleOwner, observer = Observer {
            when (it.keyId) {
                Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE -> {
                    Log.d(TAG, "EVENT_SETTING_CAMERA_SECURITY_FENCE")
                    val bundle = Bundle().apply {
                        putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
                        putInt("RuleType", ALERT_lINE_TYPE)
                        putSerializable("HumanDetection", viewModel.currentHumanDetectionBean)
                        putSerializable(
                            "ChannelHumanRuleLimit",
                            viewModel.channelHumanRuleLimitBean
                        )

                    }
                    viewModel.setRuleType(HumanDetectionBean.IA_TRIPWIRE)
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_security_fence)
                    appNavigation.openSmartAlertSetJFCamera(bundle)
                }
                Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA -> {
                    Log.d(TAG, "EVENT_SETTING_CAMERA_SAFE_AREA")
                    val bundle = Bundle().apply {
                        putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
                        putInt("RuleType", ALERT_AREA_TYPE)
                        putSerializable("HumanDetection", viewModel.currentHumanDetectionBean)
                        putSerializable(
                            "ChannelHumanRuleLimit",
                            viewModel.channelHumanRuleLimitBean
                        )
                    }
                    viewModel.setRuleType(HumanDetectionBean.IA_PERIMETER)
                    binding.tvTypeSettingZone.text = getString(com.vht.sdkcore.R.string.string_safe_zone)
                    appNavigation.openSmartAlertSetJFCamera(bundle)
                }
            }
        })
    }

    override fun bindingAction() {
        super.bindingAction()
        doOnFragmentResult<HumanDetectionBean>(JFSmartAlertSetFragment.RESULT_ALERT_SET) {
            viewModel.currentHumanDetectionBean = it
        }
    }

    override fun updateHumanDetectResult(isSuccess: Boolean) {
        Log.d(TAG, "updateHumanDetectResult")
        showHideLoading(false)
    }

    override fun saveHumanDetectResult(isSuccess: Boolean) {
        Log.d(TAG, "saveHumanDetectResult")
        showHideLoading(false)
    }

    override fun onGetMotionDetectResult(isSuccess: Boolean) {
        Log.d(TAG, "onGetMotionDetectResult")
        showHideLoading(false)
    }

    override fun onSaveConfigResult(isSuccess: Boolean) {
        Log.d(TAG, "onSaveConfigResult")
        showHideLoading(false)
    }

    override fun onGetConfigFailed() {
        Log.d(TAG, "onGetConfigFailed")
        showHideLoading(false)
    }
}