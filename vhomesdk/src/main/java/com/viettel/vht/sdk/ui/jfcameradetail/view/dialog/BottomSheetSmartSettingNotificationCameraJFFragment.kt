package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSmartNotificationCameraJfBinding
import com.viettel.vht.sdk.ui.jfcameradetail.view.SettingCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetFragmentNotModel

class BottomSheetSmartSettingNotificationCameraJFFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSmartNotificationCameraJfBinding>() {

    private val viewModel: SettingCameraJFViewModel by activityViewModels()

    override val layoutId: Int = R.layout.fragment_bottom_sheet_smart_notification_camera_jf

    override fun initView() {
        binding.cbNotification24hValue.isVisible =
            viewModel.notificationAlarm.value == SmartSettingNotificationCameraJF.ALL_DAY_24H
        binding.cbNotificationScheduleValue.isVisible =
            viewModel.notificationAlarm.value == SmartSettingNotificationCameraJF.SCHEDULE
        binding.cbTurnOffNotificationValue.isVisible =
            viewModel.notificationAlarm.value == SmartSettingNotificationCameraJF.OFF
    }

    override fun initControl() {
        binding.cbNotification24h.setOnClickListener {
            if (!binding.cbNotification24hValue.isVisible) {
                viewModel.setNotificationAlarmEnable(SmartSettingNotificationCameraJF.ALL_DAY_24H)
                dismissDialog()
            } else {
                dismissDialog()
            }
        }

        binding.cbNotificationSchedule.setOnClickListener {
            if (!binding.cbNotificationScheduleValue.isVisible) {
                viewModel.setNotificationAlarmEnable(SmartSettingNotificationCameraJF.SCHEDULE)
                dismissDialog()
            } else {
                dismissDialog()
            }
        }

        binding.cbTurnOffNotification.setOnClickListener {
            if (!binding.cbTurnOffNotificationValue.isVisible) {
                viewModel.setNotificationAlarmEnable(SmartSettingNotificationCameraJF.OFF)
                dismissDialog()
            } else {
                dismissDialog()
            }
        }

    }

    companion object {
        const val TAG = "BottomSheetSmartSettingNotificationCameraJFFragment"
        fun newInstance() = BottomSheetSmartSettingNotificationCameraJFFragment()
    }
}

enum class SmartSettingNotificationCameraJF {
    ALL_DAY_24H,
    SCHEDULE,
    OFF
}
