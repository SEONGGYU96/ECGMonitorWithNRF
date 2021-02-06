package com.seoultech.ecgmonitor.utils

import junit.framework.TestCase
import java.util.*

class TimeUtilTest : TestCase() {

    fun testGetMillisecondBefore() {
        val now = GregorianCalendar()
        val after = GregorianCalendar().apply { timeInMillis = TimeUtil.getMillisecondBefore(10) }
        println("now : ${now.get(Calendar.YEAR)}년 ${now.get(Calendar.MONTH + 1)}월 ${now.get(Calendar.DAY_OF_MONTH)}일 ${now.get(Calendar.HOUR_OF_DAY)} : ${now.get(Calendar.MINUTE)}")
        println("after : ${after.get(Calendar.YEAR)}년 ${after.get(Calendar.MONTH + 1)}월 ${after.get(Calendar.DAY_OF_MONTH)}일 ${after.get(Calendar.HOUR_OF_DAY)} : ${after.get(Calendar.MINUTE)}")
    }
}