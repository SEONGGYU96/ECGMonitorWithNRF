package com.seoultech.ecgmonitor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seoultech.ecgmonitor.monitor.MonitorFragment
import com.seoultech.ecgmonitor.setting.SettingPreferenceFragment

class ECGFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                MonitorFragment()
            }
            else -> {
                SettingPreferenceFragment()
            }
        }
    }
}