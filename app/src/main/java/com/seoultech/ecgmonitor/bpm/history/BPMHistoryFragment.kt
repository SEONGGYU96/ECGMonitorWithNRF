package com.seoultech.ecgmonitor.bpm.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.seoultech.ecgmonitor.databinding.FragmentBpmHistoryBinding
import java.util.*

class BPMHistoryFragment: Fragment() {

    private lateinit var binding : FragmentBpmHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBpmHistoryBinding.inflate(inflater, container, false)

        binding.bpmdaypickerBpmhistory.run {
            setStartTimeInMillis(
                GregorianCalendar().apply { add(Calendar.MONTH, -1) }.timeInMillis)
            setDayClickListener {
                //Todo: 선택한 날의 데이터 불러오기
                Log.d("TEST", it.toString())
            }
        }
        return binding.root
    }
}