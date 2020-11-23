package com.seoultech.ecgmonitor.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.button.MaterialButton
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivityScanBinding
import com.seoultech.ecgmonitor.device.DeviceAdapter
import com.seoultech.ecgmonitor.extension.obtainViewModel
import com.seoultech.ecgmonitor.monitor.MonitorActivity
import com.seoultech.ecgmonitor.utils.BluetoothUtil
import com.seoultech.ecgmonitor.utils.PermissionUtil

class ScanActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private const val TAG = "ScanActivity"
        private const val REQUEST_ACCESS_FINE_LOCATION = 100
    }

    lateinit var binding: ActivityScanBinding

    private lateinit var scanViewModel: ScanViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init view
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan)

        //init view model
        scanViewModel = obtainViewModel().apply {
            scanStateLiveData.observe(this@ScanActivity, this@ScanActivity::startScan)
        }

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

            recyclerviewMainDevice.run {
                addItemDecoration(DividerItemDecoration(this@ScanActivity, DividerItemDecoration.VERTICAL))
                adapter = DeviceAdapter(this@ScanActivity, scanViewModel.deviceLiveData)
                    .apply {
                        listener = {
                            startConnectionActivity(it)
                        }
                    }
                }
            }
        }

    override fun onStop() {
        super.onStop()
        stopScan()
    }

    override fun onRestart() {
        super.onRestart()
        scanViewModel.run {
            deviceLiveData.clear()
            scanStateLiveData.clearRecords()
        }
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

    private fun stopScan() {
        scanViewModel.stopScan()
    }

    private fun startConnectionActivity(device: BluetoothDevice) {
        stopScan()

        val intent = Intent(this, MonitorActivity::class.java).apply {
            putExtra("device", device)
        }
        startActivity(intent)
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

    private fun obtainViewModel() = obtainViewModel(ScanViewModel::class.java)
}