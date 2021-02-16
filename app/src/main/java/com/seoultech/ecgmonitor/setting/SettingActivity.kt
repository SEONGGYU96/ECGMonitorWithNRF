package com.seoultech.ecgmonitor.setting

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivitySettingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_setting)

        setSupportActionBar(binding.toolbarSetting)

        replaceSettingFragment()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SettingPreferenceFragment.REQUEST_SMS_PERMISSION_CODE -> {
                getSettingPreferenceFragment()
                    .setSMSSwitch(grantResults.isNotEmpty() && grantResults[0]
                                == PackageManager.PERMISSION_GRANTED)
            }
            SettingPreferenceFragment.REQUEST_READ_CONTACT_PERMISSION_CODE -> {
                getSettingPreferenceFragment().startContactActivity()
            }
        }
    }

    private fun replaceSettingFragment() {
        supportFragmentManager.commit {
            replace<SettingPreferenceFragment>(R.id.fragmentcontainer_setting_preference)
        }
    }

    private fun getSettingPreferenceFragment(): SettingPreferenceFragment =
        supportFragmentManager.fragments[0] as SettingPreferenceFragment
}