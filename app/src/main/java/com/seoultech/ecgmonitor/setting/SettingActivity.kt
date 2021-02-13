package com.seoultech.ecgmonitor.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivitySettingBinding>(this, R.layout.activity_setting)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<SettingPreferenceFragment>(R.id.fragmentcontainer_setting_preference)
        }
    }
}