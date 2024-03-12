package com.viettel.vht.sdk.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateHelper {
    companion object {
        fun toDayMonth(cal: Calendar): String {
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            return sdf.format(cal.time)
        }

        fun dateToString(cal: Calendar): String {
            val currentDay = Calendar.getInstance()

            if (cal.get(Calendar.YEAR) == currentDay.get(Calendar.YEAR)
                && cal.get(Calendar.MONTH) == currentDay.get(Calendar.MONTH)
                && cal.get(Calendar.DAY_OF_MONTH) == currentDay.get(Calendar.DAY_OF_MONTH)
            ) {
                return "Hôm nay"
            }
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            return sdf.format(cal.time)
        }

        fun toHourMinute(cal: Calendar): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(cal.time)
        }

        fun toHourMinute(date: Date): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(date)
        }

        fun toHMS(cal: Calendar): String {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(cal.time)
        }

        fun toMS(date: Date): String {
            val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
            return sdf.format(date)
        }

        fun toStartH(date: Date): String {
            val sdf = SimpleDateFormat("HH", Locale.getDefault())
            return sdf.format(date) + ":00"
        }

        fun formatDate(cal: Calendar): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(cal.time)
        }

        fun formatPlaypackDate(cal: Calendar): String {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            return sdf.format(cal.time)
        }

        fun formatPlaypackToString(calText: String): Calendar {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.parse(calText)?.let {
                cal.time = it
            }
            return cal
        }

        fun getDurationFromMs(ms: Long): String {
            val seconds = (ms / 1000).toInt()
            return if (seconds < 60) {
                String.format("$seconds\"")
            } else {
                String.format("${seconds / 60}'${seconds % 60}\"")
            }
        }

        fun formatTimeStampToString(timestamp: String?): String {
            return if (timestamp.isNullOrEmpty()) {
                ""
            } else {
                try {
                    val calendar = Calendar.getInstance(Locale.ENGLISH)
                    calendar.timeInMillis = timestamp.toLong()
                    DateFormat.format("HH:mm:ss dd-MM-yyyy", calendar).toString()
                } catch (exception: Exception) {
                    ""
                }

            }
        }
    }
}
