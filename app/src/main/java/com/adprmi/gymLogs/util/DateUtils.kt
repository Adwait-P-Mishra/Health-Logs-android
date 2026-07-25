package com.adprmi.gymLogs.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
    fun formatDate(date: Date, pattern: String): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(date)
    }

    fun getWeekDays(selectedDate: Date): List<Date> {
        val cal = Calendar.getInstance().apply {
            time = selectedDate
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val days = mutableListOf<Date>()
        (0 until 7).forEach { _ ->
            days.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }

    fun getMonthDays(displayedMonth: Date): List<Date> {
        val cal = Calendar.getInstance().apply {
            time = displayedMonth
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val firstWeekday = cal.get(Calendar.DAY_OF_WEEK)
//        val month = cal.get(Calendar.MONTH)

        // Backtrack to the start of the week for alignment
        cal.add(Calendar.DAY_OF_YEAR, -(firstWeekday - Calendar.SUNDAY))

        val days = mutableListOf<Date>()
        // We want to show 5 or 6 weeks depending on the month
        // Usually, 42 days (6 weeks) is standard to fit any month
        (0 until 42).forEach { _ ->
            days.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }
}
