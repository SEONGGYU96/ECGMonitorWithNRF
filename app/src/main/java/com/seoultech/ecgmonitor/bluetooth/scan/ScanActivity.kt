package com.seoultech.ecgmonitor.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.button.MaterialButton
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivityScanBinding
import com.seoultech.ecgmonitor.device.DeviceAdapter
import com.seoultech.ecgmonitor.monitor.MonitorFragment
import com.seoultech.ecgmonitor.service.GattConnectionMaintenanceService
import com.seoultech.ecgmonitor.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScanActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "ScanActivity"
        private const val REQUEST_ACCESS_FINE_LOCATION = 100
    }

    lateinit var binding: ActivityScanBinding

    private val scanViewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init view
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan)

        //init view model
        scanViewModel.scanStateLiveData.observe(this@ScanActivity, this@ScanActivity::startScan)

        //init ActionBar
        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.setTitle(R.string.app_name)

        binding.run {
            //init OnClickListener
            includeMainNopermission
                .findViewById<MaterialButton>(R.id.button_nopermission_grant)
                .setOnClickListener(this@ScanActivity)
            includeMainBluetoothoff
                .findViewById<MaterialButton>(R.id.button_bluetoothoff_on)
                .setOnClickListener(this@ScanActivity)

            //init RecyclerView
            recyclerviewMainDevice.run {
                //Add divider
                addItemDecoration(DividerItemDecoration(this@ScanActivity, DividerItemDecoration.VERTICAL))
                //Set Adapter
                adapter = DeviceAdapter(this@ScanActivity, scanViewModel.deviceLiveData)
                    .apply {
                        listener = {
                            startConnectionService(it)
                            startMonitorActivity()
                        }
                    }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //When Activity is gone to background, Scanning must stop. And don't get broadcast from bluetooth state
        stopScan()
        try {
            application.unregisterReceiver(bluetoothStateBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "onCleared() : Receiver not registered")
        }
    }

    override fun onRestart() {
        super.onRestart()
        //When Activity is back to foreground, previous results of scanning can be invalid. So clear these.
        scanViewModel.clearDevices()
        //register BroadcastReceiver to know whether bluetooth is off
        registerBluetoothStateBroadcastReceiver()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            //When come back from process of requiring location permission
            REQUEST_ACCESS_FINE_LOCATION -> scanViewModel.refresh()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_nopermission_grant -> {
                PermissionUtil.requestLocationPermission(
                    this, REQUEST_ACCESS_FINE_LOCATION
                )
            }
            R.id.button_bluetoothoff_on -> {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    //Let's start scanning
    private fun startScan(state: ScanStateLiveData) {
        binding.run {
            //check location permission
            if (PermissionUtil.isLocationPermissionsGranted(this@ScanActivity)) {
                includeMainNopermission.visibility = View.GONE

                //check bluetooth
                if (state.isBluetoothEnabled()) {
                    includeMainBluetoothoff.visibility = View.GONE
                    scanViewModel.startScan()
                    progressbarMain.visibility = View.VISIBLE

                    //check has device
                    if (!state.hasRecords()) {
                        includeMainNodevice.visibility = View.VISIBLE
                    } else {
                        includeMainNodevice.visibility = View.GONE
                    }
                } else {
                    Log.d(TAG, "startScan() : Bluetooth is not enabled")
                    includeMainBluetoothoff.visibility = View.VISIBLE
                    progressbarMain.visibility = View.INVISIBLE
                    includeMainNodevice.visibility = View.GONE
                }
            } else {
                Log.d(TAG, "startScan() : Location permission required")
                includeMainNopermission.visibility = View.VISIBLE
                includeMainBluetoothoff.visibility = View.GONE
                includeMainNodevice.visibility = View.GONE
                progressbarMain.visibility = View.INVISIBLE
            }
        }
    }

    //Stop scanning
    private fun stopScan() {
        scanViewModel.stopScan()
    }

    //Start service for connecting and maintaining it
    private fun startConnectionService(device: BluetoothDevice) {
        val intent = Intent(
            this,
            GattConnectionMaintenanceService::class.java
        ).apply {
            //You must hand over the device that attempts to connect
            putExtra(GattConnectionMaintenanceService.EXTRA_DISCOVERED_DEVICE, device)
        }
        startService(intent)
    }

    //Start next activity for connection
    private fun startMonitorActivity() {
        //Before start next activity, stop scanning
        stopScan()

        startActivity(Intent(this, MonitorFragment::class.java))
    }

    //register BroadcastReceiver to know whether bluetooth is off
    private fun registerBluetoothStateBroadcastReceiver() {
        application.registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    private val bluetoothStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) {
                return
            }

            val state = intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF
            )
            val previousState = intent.getIntExtra(
                BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF
            )

            when (state) {
                BluetoothAdapter.STATE_ON -> {
                    scanViewModel.setBluetoothEnabled(true)
                }
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan()
                        scanViewModel.setBluetoothEnabled(false)
                    }
                }
            }
        }
    }
}