package com.viettel.vht.sdk.ui.listvideocloud

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.vht.sdkcore.base.BaseBottomSheetFragment
import com.vht.sdkcore.utils.AppLog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetDatePickerBinding
import java.text.SimpleDateFormat
import java.util.*


class BottomSheetDatePickerListVideoJFFragment:
    BaseBottomSheetFragment<FragmentBottomSheetDatePickerBinding, ListVideoCloudViewModel>() {
    private val viewmodel by viewModels<ListVideoCloudViewModel>({ requireParentFragment() })

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_date_picker

    override fun initView() {
        binding.lifecycleOwner = viewLifecycleOwner
        val dateTime = viewmodel.date.value ?: Date().time
        binding.calendar.setDate(Date(dateTime))
        binding.calendar.setMaximumDate(Calendar.getInstance())
        val minCalendar = Calendar.getInstance()
        minCalendar.add(Calendar.DATE, -30)
        binding.calendar.setMinimumDate(minCalendar)
        binding.calendar.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val date = eventDay?.calendar?.time ?: Date()
                val string = SimpleDateFormat("dd-M-yyyy").format(date)
                val timeLong = SimpleDateFormat("dd-M-yyyy").parse(string).time
                viewmodel.date.value = timeLong
                viewmodel.sendLogForCloudPackage(
                    screenID = AppLog.ScreenID.CLOUD_LIST,
                    actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                )
                dismiss()
            }
        })
        viewmodel.listDateCloudDayAlso.value?.let {
            setEventDay()
            setHighLightedDay()
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setEventDay() {
        val events: MutableList<EventDay> = ArrayList()
        for (date in viewmodel.listDateCloudDayAlso.value!!) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            events.add(EventDay(calendar, R.drawable.ic_ellipse_cloud_day))
        }

        binding.calendar.setEvents(events)
    }

    private fun setHighLightedDay() {
        val listTime = mutableListOf<Calendar>()
        for (date in viewmodel.listDateCloudDayAlso.value!!) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            listTime.add(calendar)
        }
        binding.calendar.setHighlightedDays(listTime)
    }

    override fun getVM(): ListVideoCloudViewModel = viewmodel
}