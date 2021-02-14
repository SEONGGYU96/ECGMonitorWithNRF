package com.seoultech.ecgmonitor.setting

import android.content.Context
import androidx.preference.PreferenceCategory

class TouchableCategory(context: Context?) : PreferenceCategory(context) {

    override fun isEnabled(): Boolean {
        return true
    }

    override fun isSelectable(): Boolean {
        return true
    }
}