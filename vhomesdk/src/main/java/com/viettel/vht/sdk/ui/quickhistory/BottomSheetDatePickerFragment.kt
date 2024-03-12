package com.viettel.vht.sdk.ui.quickhistory

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


class BottomSheetDatePickerFragment :
    BaseBottomSheetFragment<FragmentBottomSheetDatePickerBinding, HistoryDetailLiveViewModel>() {
    private var eventDay: EventDay? = null
    private val viewmodel by viewModels<HistoryDetailLiveViewModel>({ requireParentFragment() })

    override val layoutId: Int
        get() = R.layout.fragment_bottom_sheet_date_picker

    override fun initView() {
        binding.lifecycleOwner = viewLifecycleOwner
        val dateTime = viewmodel.dateLiveData.value ?: Date().time
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
                viewmodel.dateLiveData.value = timeLong
                viewmodel.sendLogForCloudPackage(
                    screenID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.screenID,
                    actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                )
                dismiss()
            }
        })

        setEventDay()
        setHighLightedDay()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setEventDay() {
        val events: MutableList<EventDay> = ArrayList()
        for (date in viewmodel.listDateCloudDayAlso) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            events.add(EventDay(calendar, R.drawable.ic_ellipse_cloud_day))
        }

        binding.calendar.setEvents(events)
    }

    private fun setHighLightedDay() {
        val listTime = mutableListOf<Calendar>()
        for (date in viewmodel.listDateCloudDayAlso) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            listTime.add(calendar)
        }
        binding.calendar.setHighlightedDays(listTime)
    }

    override fun getVM(): HistoryDetailLiveViewModel = viewmodel
}