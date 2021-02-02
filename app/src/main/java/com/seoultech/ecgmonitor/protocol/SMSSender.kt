package com.seoultech.ecgmonitor.protocol

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.seoultech.ecgmonitor.R

class SMSSender(private val context: Context) {

    companion object {
        private const val TAG = "SMSSender"
    }

    fun send(bpm: Int) {
        val smsManager = SmsManager.getDefault()
        val userName = getUserName()
        val message = String.format(context.getString(R.string.abnormal_sms), userName, bpm)
        val numbers = getRegisteredNumbers()
        for (number in numbers) {
            smsManager.sendTextMessage(
                number, null, message, null, null)
            Log.d(TAG, "send(): $number")
        }
    }

    private fun getUserName(): String {
        //Todo: Preference에서 사용자 이름 가져오기
        return "아무개"
    }

    private fun getRegisteredNumbers(): List<String> {
        //Todo: DB에서 등록된 전화번호 가져오기
        return mutableListOf("01098855658")
    }
}