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
}