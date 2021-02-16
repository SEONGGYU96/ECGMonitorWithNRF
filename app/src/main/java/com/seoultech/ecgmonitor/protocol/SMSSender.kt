package com.seoultech.ecgmonitor.protocol

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.preference.PreferenceManager
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.contact.data.source.ContactDataSource
import com.seoultech.ecgmonitor.setting.SettingPreferenceFragment
import com.seoultech.ecgmonitor.utils.PermissionUtil

class SMSSender(
    private val context: Context,
    private val contactDataSource: ContactDataSource
) {

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
        contactDataSource.getContacts { numbers ->
            for (number in numbers) {
                smsManager.sendTextMessage(
                    number.number, null, message, null, null
                )
                Log.d(TAG, "send(): $number")
            }
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
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                SettingPreferenceFragment.USER_NAME_PREFERENCE_KEY,
                context.getString(R.string.setting_name_default)
            )!!
    }
}