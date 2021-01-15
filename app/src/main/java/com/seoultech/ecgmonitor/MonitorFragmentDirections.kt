package com.seoultech.ecgmonitor

import android.os.Bundle
import androidx.navigation.NavDirections

class MonitorFragmentDirections private constructor() {
    private class ActionMonitorFragmentToScanFragment : NavDirections {
        override fun getActionId(): Int {
            return R.id.action_monitorFragment_to_scanFragment
        }

        override fun getArguments(): Bundle {
            return Bundle()
        }
    }

    companion object {
        fun actionMonitorFragmentToScanFragment(): NavDirections =
            ActionMonitorFragmentToScanFragment()
    }
}