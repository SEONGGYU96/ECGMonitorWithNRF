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

    fun getMinuteDiff(time1: Long, time2: Long): Long {
        return abs(time1 - time2)
    }

    fun getDayDiff(baseDate: GregorianCalendar, targetDate: GregorianCalendar): Int {
        val base = GregorianCalendar().apply { timeInMillis = baseDate.timeInMillis }
        val target = GregorianCalendar().apply { timeInMillis = targetDate.timeInMillis }
        initGregorianCalendarBelowDay(base)
        initGregorianCalendarBelowDay(target)

        val baseTimeInMillis = base.timeInMillis
        val targetTimeInMillis = target.timeInMillis
        val diff = (baseTimeInMillis - targetTimeInMillis) / (24 * 60 * 60 * 1000)

        return abs(diff).toInt()
    }

    private fun initGregorianCalendarBelowDay(calendar: GregorianCalendar) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}