package com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.dialog

import android.annotation.SuppressLint
import android.widget.Toast
import com.vht.sdkcore.utils.custom.WheelPicker
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetTimeScheduleSettingBinding
import com.viettel.vht.sdk.utils.BaseBottomSheetTimePickerFragment
import com.viettel.vht.sdk.utils.invisible
import com.viettel.vht.sdk.utils.visible

@SuppressLint("SetTextI18n")
class BottomSheetSettingTimeScheduleFragment :
    BaseBottomSheetTimePickerFragment<FragmentBottomSheetTimeScheduleSettingBinding>() {

    private var onBottomSheetTimeSchedule: OnBottomSheetTimeSchedule? = null

    private var time: MutableList<String> = mutableListOf()

    private var startOrEnd = true //true: start ; false: end

    override val layoutId: Int = R.layout.fragment_bottom_sheet_time_schedule_setting

    override fun listTimePicker() = listOf(binding.wheelPickerHour, binding.wheelPickerMinute)

    override fun initView() {
        if (time.size == 4) {
            startOrEnd = true
            selectTimeStart(true)
            selectTimeEnd(false)
            binding.tvValueTimeStart.text = "${time[0]}:${time[1]}"
            binding.tvValueTimeEnd.text = "${time[2]}:${time[3]}"
        } else {
            Toast.makeText(requireContext(), "Thời gian không hợp lệ", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun initControl() {
        super.initControl()
        binding.apply {
            viewTimeStart.setOnClickListener {
                startOrEnd = true
                selectTimeStart(true)
                selectTimeEnd(false)
            }
            viewTimeEnd.setOnClickListener {
                startOrEnd = false
                selectTimeStart(false)
                selectTimeEnd(true)
            }
            wheelPickerHour.setOnValueChangedListener(object : WheelPicker.OnValueChangeListener {
                override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
                    if (startOrEnd) {
                        setTime(hourStart = newVal)
                        tvValueTimeStart.text = "$newVal:${wheelPickerMinute.getCurrentItem()}"
                    } else {
                        setTime(hourEnd = newVal)
                        tvValueTimeEnd.text = "$newVal:${wheelPickerMinute.getCurrentItem()}"
                    }
                }
            })
            wheelPickerMinute.setOnValueChangedListener(object : WheelPicker.OnValueChangeListener {
                override fun onValueChange(picker: WheelPicker, oldVal: String, newVal: String) {
                    if (startOrEnd) {
                        setTime(minuteStart = newVal)
                        tvValueTimeStart.text = "${wheelPickerHour.getCurrentItem()}:$newVal"
                    } else {
                        setTime(minuteEnd = newVal)
                        tvValueTimeEnd.text = "${wheelPickerHour.getCurrentItem()}:$newVal"
                    }
                }
            })
            btCancel.setOnClickListener {
                onBottomSheetTimeSchedule?.onCancel(time)
                dismiss()
            }
            btSubmit.setOnClickListener {
                onBottomSheetTimeSchedule?.onSubmit(time)
                dismiss()
            }
        }
    }

    private fun selectTimeStart(state: Boolean) {
        binding.tvTitleTimeStart.isChecked = state
        binding.tvValueTimeStart.isChecked = state
        if (state) {
            binding.viewLineTimeStart.visible()
            binding.wheelPickerHour.scrollToValue(time[0])
            binding.wheelPickerMinute.scrollToValue(time[1])
        } else {
            binding.viewLineTimeStart.invisible()
        }
    }

    private fun selectTimeEnd(state: Boolean) {
        binding.tvTitleTimeEnd.isChecked = state
        binding.tvValueTimeEnd.isChecked = state
        if (state) {
            binding.viewLineTimeEnd.visible()
            binding.wheelPickerHour.scrollToValue(time[2])
            binding.wheelPickerMinute.scrollToValue(time[3])
        } else {
            binding.viewLineTimeEnd.invisible()
        }
    }

    private fun setTime(
        hourStart: String = "",
        minuteStart: String = "",
        hourEnd: String = "",
        minuteEnd: String = ""
    ) {
        try {
            hourStart.takeIf { it.isNotEmpty() }?.let { time[0] = it }
            minuteStart.takeIf { it.isNotEmpty() }?.let { time[1] = it }
            hourEnd.takeIf { it.isNotEmpty() }?.let { time[2] = it }
            minuteEnd.takeIf { it.isNotEmpty() }?.let { time[3] = it }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnBottomSheetTimeSchedule {
        fun onSubmit(time: List<String>)
        fun onCancel(time: List<String>)
    }

    companion object {
        const val TAG = "BottomSheetSettingTimeScheduleFragment"
        fun newInstance(
            onBottomSheetTimeSchedule: OnBottomSheetTimeSchedule,
            time: List<String> = emptyList()
        ): BottomSheetSettingTimeScheduleFragment =
            BottomSheetSettingTimeScheduleFragment().apply {
                this.onBottomSheetTimeSchedule = onBottomSheetTimeSchedule
                this.time = time.toMutableList()
            }
    }
}
