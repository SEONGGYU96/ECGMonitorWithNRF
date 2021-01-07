package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivityMonitorBinding
import com.seoultech.ecgmonitor.extension.obtainViewModel
import com.seoultech.ecgmonitor.service.ConnectingService

class MonitorActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MonitorActivity"
        const val EXTRA_DEVICE = "device"
    }

    private lateinit var binding: ActivityMonitorBinding
    private lateinit var monitorViewModel: MonitorViewModel
    private var device: BluetoothDevice? = null
    private var isConnected = false
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_monitor)

        monitorViewModel = obtainViewModel().apply {
            //Observe the state of connection withe the device
            gattLiveData.run {
                isConnected.observe(this@MonitorActivity, {
                    if (it) { //connected
                        this@MonitorActivity.isConnected = true
                        Toast.makeText(this@MonitorActivity, "Connected", Toast.LENGTH_SHORT).show()

                    } else { //disconnected
                        if (this@MonitorActivity.isConnected) {
                            this@MonitorActivity.isConnected = false
                            Toast.makeText(this@MonitorActivity, "Disconnected", Toast.LENGTH_SHORT)
                                .show()
                            //finish()
                        }
                    }
                })

                //Observing heart rate value
                receivedValue.observe(this@MonitorActivity, {
                    binding.ecggraphMonitor.addValue(it)
                })

                //Observing failure of connection state
                isFailure.observe(this@MonitorActivity, {
                    if (it) {
                        Toast.makeText(this@MonitorActivity, "Fail", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                })
            }
        }

        //Get the device from ScanActivity.
        device = intent.getParcelableExtra(EXTRA_DEVICE)

        //The device of intent may be null when start this activity from SplashActivity
        if (device == null) {
            Log.e(TAG, "device is null. May be this is already connected.")
        } else {
            //Connect with this device for first time.
            monitorViewModel.connect(device!!)
        }
    }

    override fun onPause() {
        super.onPause()
        //Stop drawing graph when this application is gone to background
        binding.ecggraphMonitor.stop()

        //Start ForegroundService for prevent destroy application.
        //If this application is destroyed, Connection will be disconnected too.
        ContextCompat.startForegroundService(
            this, Intent(this, ConnectingService::class.java)
        )
    }

    override fun onResume() {
        super.onResume()
        //change screen to landscape
        changeScreenMode()
        //start graph
        binding.ecggraphMonitor.start()
        //Stop ForegroundService. Activity is replace the role of preventing destroy
        stopService(Intent(this, ConnectingService::class.java))
    }

    private fun changeScreenMode() {
        if (isFullScreen) {
            return
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController?.run {
                hide(WindowInsets.Type.statusBars())
                //hide(WindowInsets.Type.navigationBars())
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

    private fun obtainViewModel() = obtainViewModel(MonitorViewModel::class.java)
}