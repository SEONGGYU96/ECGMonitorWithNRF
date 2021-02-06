package com.seoultech.ecgmonitor.utils

import java.util.*

object TimeUtil {

    fun getMillisecondBefore(minuteAgo: Int): Long {
        return GregorianCalendar().apply {
            add(Calendar.MINUTE, minuteAgo * -1)
            set(Calendar.SECOND, 0)
        }.timeInMillis
    }
}