package com.seoultech.ecgmonitor.bpm.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.FragmentBpmHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class BPMHistoryFragment : Fragment() {

    private lateinit var binding: FragmentBpmHistoryBinding

    private val bpmHistoryViewModel: BPMHistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBpmHistoryBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        setDateText(GregorianCalendar())

        subscribeUI()

        binding.bpmdaypickerBpmhistory.setDayClickListener {
            bpmHistoryViewModel.getBPMDataOnDate(it)
            setDateText(it)
        }

        return binding.root
    }

    private fun setDateText(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        binding.textviewBpmhistorySeleteddate.text =
            String.format(getString(R.string.history_selected_date_format), year, month, day)
    }

    override fun onResume() {
        super.onResume()
        bpmHistoryViewModel.refresh()
    }

    private fun subscribeUI() {
        bpmHistoryViewModel.run {
            bpmData.observe(viewLifecycleOwner) {
                if (it.isEmpty()) {
                    enableUI(false)
                } else {
                    enableUI(true)
                    binding.bpmhistoryviewerBpmhistory.setBPMData(it)
                }
            }
            firstDate.observe(viewLifecycleOwner) {
                if (it == -1L) {
                    enableEntireUI(false)
                } else if (it != null) {
                    enableEntireUI(true)
                    binding.bpmdaypickerBpmhistory.setStartTimeInMillis(it)
                }
            }
        }
    }

    private fun enableEntireUI(isEnable: Boolean) {
        binding.bpmdaypickerBpmhistory.visibility = getVisibility(isEnable)
        enableUI(isEnable)
    }

    private fun enableUI(isEnable: Boolean) {
        binding.run {
            scrollviewBpmhistory.visibility = getVisibility(isEnable)
            layoutBpmhistoryNodata.visibility = getVisibility(!isEnable)
        }
    }

    private fun getVisibility(isEnable: Boolean): Int {
        return if (isEnable) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}