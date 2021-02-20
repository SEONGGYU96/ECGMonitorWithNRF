package com.seoultech.ecgmonitor.bpm.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.seoultech.ecgmonitor.databinding.FragmentBpmHistoryBinding

class BPMHistoryFragment: Fragment() {

    private lateinit var binding : FragmentBpmHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBpmHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }
}