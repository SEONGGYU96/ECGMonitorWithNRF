package com.seoultech.ecgmonitor

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.button.MaterialButton
import com.seoultech.ecgmonitor.databinding.ActivityScanBinding
import com.seoultech.ecgmonitor.extension.obtainViewModel
import com.seoultech.ecgmonitor.scan.ScanStateLiveData
import com.seoultech.ecgmonitor.utils.PermissionUtil
import com.seoultech.ecgmonitor.viewmodel.ScanViewModel

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
            getScanState().observe(this@ScanActivity, this@ScanActivity::startScan)
        }

        //init ActionBar
        setSupportActionBar(binding.toolbarMain)
        supportActionBar?.setTitle(R.string.app_name)

        //init OnClickListener
        binding.run {
            includeMainNopermission
                .findViewById<MaterialButton>(R.id.button_nopermission_grant)
                .setOnClickListener(this@ScanActivity)
            includeMainBluetoothoff
                .findViewById<MaterialButton>(R.id.button_bluetoothoff_on)
                .setOnClickListener(this@ScanActivity)
        }
    }

    override fun onStop() {
        super.onStop()
        stopScan()
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_nopermission_grant -> {
                PermissionUtil.requestLocationPermission(
                    this, REQUEST_ACCESS_FINE_LOCATION)
            }
            R.id.button_bluetoothoff_on -> {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun obtainViewModel() = obtainViewModel(ScanViewModel::class.java)
}