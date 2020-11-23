package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivityMonitorBinding
import com.seoultech.ecgmonitor.extension.obtainViewModel
import kotlinx.android.synthetic.main.activity_monitor.*

class MonitorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MonitorActivity"
        const val EXTRA_DEVICE = "device"
    }

    private lateinit var binding: ActivityMonitorBinding
    private lateinit var monitorViewModel: MonitorViewModel
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_monitor)
        monitorViewModel = obtainViewModel().apply {
            isConnected.observe(this@MonitorActivity, {
                if (it) {
                    this@MonitorActivity.isConnected = true
                    Toast.makeText(this@MonitorActivity, "Connected", Toast.LENGTH_SHORT).show()
                } else {
                    if (this@MonitorActivity.isConnected) {
                        this@MonitorActivity.isConnected = false
                        Toast.makeText(this@MonitorActivity, "Disconnected", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            receivedValue.observe(this@MonitorActivity) {
                binding.ecggraphMonitor.addValue(it.toDouble())
            }
            isFailure.observe(this@MonitorActivity) {
                if (it) {
                    Toast.makeText(this@MonitorActivity, "Fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val device = intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
        if (device == null) {
            Log.e(TAG, "device is null")
            finish()
        }
        monitorViewModel.connect(device!!)
    }

    private fun obtainViewModel() = obtainViewModel(MonitorViewModel::class.java)
}