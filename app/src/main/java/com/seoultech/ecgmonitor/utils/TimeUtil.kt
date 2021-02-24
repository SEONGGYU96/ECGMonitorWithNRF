package com.seoultech.ecgmonitor.utils

import java.util.*
import kotlin.math.abs

object TimeUtil {

    fun getMillisecondBefore(minuteAgo: Int): Long {
        return GregorianCalendar().apply {
            add(Calendar.MINUTE, minuteAgo * -1)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }

    fun getCurrentTimeInMills(): Long {
        return GregorianCalendar().timeInMillis
    }

    fun getMinuteDiff(time1: Long, time2: Long): Int {
        return abs((time1 - time2) / (60 * 1000)).toInt()
    }

    fun getDayDiff(baseDate: GregorianCalendar, targetDate: GregorianCalendar): Int {
        val base = GregorianCalendar().apply { timeInMillis = baseDate.timeInMillis }
        val target = GregorianCalendar().apply { timeInMillis = targetDate.timeInMillis }
        initCalendarBelowDay(base)
        initCalendarBelowDay(target)

        val baseTimeInMillis = base.timeInMillis
        val targetTimeInMillis = target.timeInMillis
        val diff = (baseTimeInMillis - targetTimeInMillis) / (24 * 60 * 60 * 1000)

        return abs(diff).toInt()
    }

    fun initCalendarBelowMinute(calendar: Calendar) {
        calendar.apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun initCalendarBelowDay(calendar: Calendar) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        initCalendarBelowDay(calendar)
    }

    fun initCalendarBelowDayToFull(calendar: Calendar) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
}