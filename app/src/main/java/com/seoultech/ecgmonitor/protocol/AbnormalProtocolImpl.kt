package com.seoultech.ecgmonitor.protocol

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.contact.data.source.ContactDataSource
import com.seoultech.ecgmonitor.setting.SettingPreferenceFragment

class AbnormalProtocolImpl(
    private val context: Context,
    private val contactDataSource: ContactDataSource
) : AbnormalProtocol {

    companion object {
        private const val TAG = "AbnormalProtocol"
    }

    override fun startAbnormalProtocol(averageBpm: Int, type: AbnormalProtocol.AbnormalType) {
        Log.d(TAG, "startAbnormalProtocol() : bpm $averageBpm")
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        val smsAllowed = preference.getBoolean(
                SettingPreferenceFragment.SMS_PREFERENCE_KEY, false)
        val notificationAllowed = preference.getBoolean(
            SettingPreferenceFragment.PUSH_ALERT_PREFERENCE_KEY, false
        )
        if (smsAllowed) {
            SMSSender(context, contactDataSource).send(type, averageBpm)
        }
        if (notificationAllowed) {
            AbnormalNotification(context).showNotification(type, averageBpm)
        }
    }
}