package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivityMonitorBinding
import com.seoultech.ecgmonitor.extension.obtainViewModel

class MonitorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MonitorActivity"
        const val EXTRA_DEVICE = "device"
    }

    private lateinit var binding: ActivityMonitorBinding
    private lateinit var monitorViewModel: MonitorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_monitor)
        monitorViewModel = obtainViewModel()
        val device = intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
        if (device == null) {
            Log.e(TAG, "device is null")
            finish()
        }
        monitorViewModel.connect(device!!)
    }

    private fun obtainViewModel() = obtainViewModel(MonitorViewModel::class.java)
}