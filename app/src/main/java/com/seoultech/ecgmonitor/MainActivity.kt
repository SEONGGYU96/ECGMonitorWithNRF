package com.seoultech.ecgmonitor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.seoultech.ecgmonitor.bluetooth.scan.ScanFragment
import com.seoultech.ecgmonitor.databinding.ActivityMainBinding
import com.seoultech.ecgmonitor.monitor.MonitorFragment
import com.seoultech.ecgmonitor.setting.SettingPreferenceFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbarMain)
    }
}