package com.seoultech.ecgmonitor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.seoultech.ecgmonitor.bpm.history.BPMHistoryFragment
import com.seoultech.ecgmonitor.monitor.MonitorFragment
import com.seoultech.ecgmonitor.setting.SettingPreferenceFragment

class MainFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                MonitorFragment()
            }
            1-> {
                BPMHistoryFragment()
            }
            else -> {
                SettingPreferenceFragment()
            }
        }
    }
}