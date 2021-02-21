package com.seoultech.ecgmonitor.bpm.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.seoultech.ecgmonitor.databinding.FragmentBpmHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class BPMHistoryFragment: Fragment() {

    private lateinit var binding : FragmentBpmHistoryBinding

    private val bpmHistoryViewModel : BPMHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBpmHistoryBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        bpmHistoryViewModel.bpmData.observe(viewLifecycleOwner) {
            binding.bpmhistoryviewerBpmhistory.setBPMData(it)
        }

        binding.bpmdaypickerBpmhistory.run {
            setStartTimeInMillis(
                GregorianCalendar().apply { add(Calendar.MONTH, -1) }.timeInMillis)
            setDayClickListener {
                bpmHistoryViewModel.getBPMDataOnDate(it)
            }
        }


        return binding.root
    }
}