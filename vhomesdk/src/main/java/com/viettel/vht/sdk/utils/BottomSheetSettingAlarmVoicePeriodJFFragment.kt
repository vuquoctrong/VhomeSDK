package com.viettel.vht.sdk.utils

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period.ListAlarmVoicePeriodJFViewModel
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingAlarmVoicePeriodJfBinding


class BottomSheetSettingAlarmVoicePeriodJFFragment :
    BaseBottomSheetFragmentNotModel<FragmentBottomSheetSettingAlarmVoicePeriodJfBinding>() {

    private val viewModel: ListAlarmVoicePeriodJFViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_setting_alarm_voice_period_jf

    override fun initView() {
        binding.cbAlways.isVisible =
            viewModel.typeAlarmVoicePeriod.value == AlarmVoicePeriodType.ALWAYS

        binding.cbSchedule.isVisible =
            viewModel.typeAlarmVoicePeriod.value == AlarmVoicePeriodType.SCHEDULE
    }


    override fun initControl() {
        binding.tvAlways.setOnClickListener {
            viewModel.setTypeAlarmVoicePeriod(AlarmVoicePeriodType.ALWAYS)
            dismiss()
        }

        binding.tvSchedule.setOnClickListener {
            viewModel.setTypeAlarmVoicePeriod(AlarmVoicePeriodType.SCHEDULE)
            dismiss()
        }
    }

    companion object {
        const val TAG = "BottomSheetSettingAlarmVoicePeriodJFFragment"

        fun newInstance() = BottomSheetSettingAlarmVoicePeriodJFFragment()
    }
}

enum class AlarmVoicePeriodType(val content: String) {
    ALWAYS("Luôn bật"),
    SCHEDULE("Lịch trình"),
}
