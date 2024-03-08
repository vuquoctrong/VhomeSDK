package com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model

import java.util.UUID

data class TimeItem(
    val id: String = UUID.randomUUID().toString(),
    var position: Int,
    var time: String = ALL_DAY_TIME,
    var dayOfWeek: MutableList<Boolean> =
        mutableListOf(false, false, false, false, false, false, false),
    var isOpen: Boolean = false,
    var isSelectDelete: Boolean = false,
    var stateDelete: Boolean = false
) {

    companion object {
        const val ALL_DAY_TIME = "00:00-24:00"
        private val dayOfWeekStr = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")

        const val SUNDAY_POSITION = 0
        const val MONDAY_POSITION = 1
        const val TUESDAY_POSITION = 2
        const val WEDNESDAY_POSITION = 3
        const val THURSDAY_POSITION = 4
        const val FRIDAY_POSITION = 5
        const val SATURDAY_POSITION = 6

        const val SCHEDULE_ALARM_TIME_FORMAT = "%s:%s:%s"
        const val SCHEDULE_ALARM_OPEN_STATE = 1
        const val SCHEDULE_ALARM_CLOSE_STATE = 0

        const val TOTAL_SCHEDULE_ALARM = 5

        fun getWeeks(dayOfWeek: List<Boolean>): String {
            var isAllDay = true
            var isWeekend = true
            val textDay = StringBuilder()
            val funcAdd = { position: Int, checkWeekend: Boolean ->
                if (dayOfWeek[position]) {
                    textDay.append(dayOfWeekStr[position])
                    textDay.append(", ")
                    if (checkWeekend) {
                        isWeekend = false
                    }
                } else {
                    if (!checkWeekend) {
                        isWeekend = false
                    }
                    isAllDay = false
                }
            }
            funcAdd(MONDAY_POSITION, true)
            funcAdd(TUESDAY_POSITION, true)
            funcAdd(WEDNESDAY_POSITION, true)
            funcAdd(THURSDAY_POSITION, true)
            funcAdd(FRIDAY_POSITION, true)
            funcAdd(SATURDAY_POSITION, false)
            funcAdd(SUNDAY_POSITION, false)
            return if (isAllDay) {
                "Mỗi ngày"
            } else if (isWeekend) {
                "Cuối tuần"
            } else {
                if (textDay.length > 2) {
                    textDay.substring(0, textDay.length - 2).toString()
                } else {
                    textDay.toString()
                }
            }
        }
    }
}
