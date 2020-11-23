package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothDevice
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private var isConnected = false
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_monitor)
        monitorViewModel = obtainViewModel().apply {
            isConnected.observe(this@MonitorActivity, {
                if (it) {
                    this@MonitorActivity.isConnected = true
                    Toast.makeText(this@MonitorActivity, "Connected", Toast.LENGTH_SHORT).show()
                    changeScreenMode()
                } else {
                    if (this@MonitorActivity.isConnected) {
                        this@MonitorActivity.isConnected = false
                        Toast.makeText(this@MonitorActivity, "Disconnected", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }
            })
            receivedValue.observe(this@MonitorActivity) {
                binding.ecggraphMonitor.addValue(it.toDouble())
            }
            isFailure.observe(this@MonitorActivity) {
                if (it) {
                    Toast.makeText(this@MonitorActivity, "Fail", Toast.LENGTH_SHORT).show()
                    finish()
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

    override fun onResume() {
        super.onResume()
        changeScreenMode()
    }

    private fun changeScreenMode() {
        if (isFullScreen) {
            return
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.run {
                hide(WindowInsets.Type.statusBars())
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
        isFullScreen = true
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorViewModel.disconnect()
    }

    private fun obtainViewModel() = obtainViewModel(MonitorViewModel::class.java)
}