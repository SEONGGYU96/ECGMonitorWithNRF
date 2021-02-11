package com.seoultech.ecgmonitor.protocol

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.utils.PermissionUtil

class SMSSender(private val context: Context) {

    companion object {
        private const val TAG = "SMSSender"
    }

    fun send(type: AbnormalProtocol.AbnormalType, bpm: Int) {
        if (!PermissionUtil.isSMSPermissionsGranted(context)) {
            Log.d(TAG, "send(): SMS permission is not granted")
            return
        }
        val smsManager = SmsManager.getDefault()
        val userName = getUserName()
        val message = String.format(getContentText(type), userName, bpm)
        val numbers = getRegisteredNumbers()
        for (number in numbers) {
            smsManager.sendTextMessage(
                number, null, message, null, null)
            Log.d(TAG, "send(): $number")
        }
    }

    private fun getContentText(type: AbnormalProtocol.AbnormalType): String {
        return when (type) {
            AbnormalProtocol.AbnormalType.Tachycardia -> {
                context.getString(R.string.abnormal_sms_tachycardia)
            }
            AbnormalProtocol.AbnormalType.Bradycardia -> {
                context.getString(R.string.abnormal_sms_bradycardia)
            }
            else -> {
                context.getString(R.string.abnormal_sms_arrhythmia)
            }
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