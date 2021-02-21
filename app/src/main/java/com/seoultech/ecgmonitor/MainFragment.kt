package com.seoultech.ecgmonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.seoultech.ecgmonitor.databinding.FragmentMainBinding

class MainFragment: Fragment() {

    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.main_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.viewpagerMain.run {
            isUserInputEnabled = false
            adapter = MainFragmentStateAdapter(this@MainFragment)

            TabLayoutMediator(binding.tablayoutMain, this) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.monitor_title)
                    1-> getString(R.string.bpmhistory_title)
                    else -> getString(R.string.setting_title)
                }
            }.attach()
        }

        return binding.root
    }
}