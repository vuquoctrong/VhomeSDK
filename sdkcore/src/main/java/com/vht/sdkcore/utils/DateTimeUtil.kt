package com.vht.sdkcore.utils

import android.os.Build
import java.text.Format
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class DateTimeUtil {
    companion object {
        var DATE_FORMAT_1 = "yyyy-MM-dd HH:mm:ss"
        var DATE_FORMAT_2 = "dd/MM/yyyy"
        var DATE_FORMAT_3 = "HH:mm:ss"
        var DATE_FORMAT_4 = "HH:mm"
        var DATE_FORMAT_5 = "dd-MM-yyyy HH:mm:ss"


        fun convertLongToDate(milliSeconds: Long, format: String): String {
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }

        fun convertTime(time: Long, format: String): String? {
            val date = Date(time)
            val format: Format = SimpleDateFormat(format)
            return format.format(date)
        }

        fun convertNanoUnixToDateTime(milliSeconds: Long, format: String): String {
            val dateTime =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Instant.ofEpochSecond(milliSeconds).atZone(ZoneId.systemDefault())
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            val formatter: DateTimeFormatter =
                DateTimeFormatter.ofPattern(format, Locale.ENGLISH)
            return dateTime.format(formatter)
        }

        fun validateHourAndMinute(hour: Int, minute: Int): String {
            var hourValue = ""
            var minuteValue = ""
            hourValue = if (hour < 10) "0$hour" else hour.toString()
            minuteValue = if (minute < 10) "0$minute" else minute.toString()
            return "$hourValue:$minuteValue"
        }

        fun getCurrentDate(): String {
            var str: String = ""
            str = try {
                val sdf = SimpleDateFormat(DATE_FORMAT_2)
                return sdf.format(Date())
            } catch (e: java.lang.Exception) {
                ""
            }
            return str
        }

    }
}