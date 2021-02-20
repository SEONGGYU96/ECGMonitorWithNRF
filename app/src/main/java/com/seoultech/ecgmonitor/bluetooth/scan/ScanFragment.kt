package com.seoultech.ecgmonitor.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.seoultech.ecgmonitor.MainActivity
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.bluetooth.state.BluetoothStateLiveData
import com.seoultech.ecgmonitor.bluetooth.state.BluetoothStateReceiver
import com.seoultech.ecgmonitor.databinding.FragmentScanBinding
import com.seoultech.ecgmonitor.findNavController
import com.seoultech.ecgmonitor.service.GattConnectionMaintenanceService
import com.seoultech.ecgmonitor.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class ScanFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val TAG = "ScanActivity"
        private const val REQUEST_ACCESS_FINE_LOCATION = 100
    }

    lateinit var binding: FragmentScanBinding

    private val scanViewModel: ScanViewModel by viewModels()

    @Inject
    @Named("bluetoothStateReceiver")
    lateinit var bluetoothStateReceiver: BluetoothStateReceiver

    @Inject
    lateinit var bluetoothStateLiveData: BluetoothStateLiveData

    private var discovered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //init view
        binding = FragmentScanBinding.inflate(inflater, container, false)

        //init view model
        scanViewModel.scanStateLiveData.observe(requireActivity(), this@ScanFragment::startScan)

        bluetoothStateLiveData.observe(requireActivity(), {
            scanViewModel.setBluetoothEnabled(it)
        })

        binding.run {
            //init OnClickListener
            includeScanNopermission
                .findViewById<MaterialButton>(R.id.button_nopermission_grant)
                .setOnClickListener(this@ScanFragment)
            includeScanBluetoothoff
                .findViewById<MaterialButton>(R.id.button_bluetoothoff_on)
                .setOnClickListener(this@ScanFragment)
        }
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        //When Activity is gone to background, Scanning must stop. And don't get broadcast from bluetooth state
        stopScan()
        try {
            requireActivity().unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "onCleared() : Receiver not registered")
        }
    }

    override fun onResume() {
        super.onResume()
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
                    requireActivity(), REQUEST_ACCESS_FINE_LOCATION
                )
            }
            R.id.button_bluetoothoff_on -> {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun initToolbar() {
        setHasOptionsMenu(false)
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.scan_title)
    }

    //Let's start scanning
    private fun startScan(state: ScanStateLiveData) {
        if (discovered) {
            return
        }

        binding.run {
            //check location permission
            if (PermissionUtil.isLocationPermissionsGranted(requireContext())) {
                includeScanNopermission.visibility = View.GONE

                //check bluetooth
                if (state.isBluetoothEnabled()) {
                    includeScanBluetoothoff.visibility = View.GONE
                    scanViewModel.startScan()

                    //check has device
                    if (!state.hasRecords()) {
                        includeScanScanning.visibility = View.VISIBLE
                    } else {
                        includeScanScanning.visibility = View.GONE
                        val device = state.getDiscoveredDevice()
                        if (device != null) {
                            discovered = true
                            startConnectionService(device)
                            startMonitorActivity()
                        }
                    }
                } else {
                    Log.d(TAG, "startScan() : Bluetooth is not enabled")
                    includeScanBluetoothoff.visibility = View.VISIBLE
                    includeScanScanning.visibility = View.GONE
                }
            } else {
                Log.d(TAG, "startScan() : Location permission required")
                includeScanNopermission.visibility = View.VISIBLE
                includeScanBluetoothoff.visibility = View.GONE
                includeScanScanning.visibility = View.GONE
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
            requireContext(),
            GattConnectionMaintenanceService::class.java
        ).apply {
            //You must hand over the device that attempts to connect
            putExtra(GattConnectionMaintenanceService.EXTRA_DISCOVERED_DEVICE, device)
        }
        requireActivity().startService(intent)
    }

    //Start next activity for connection
    private fun startMonitorActivity() {
        parentFragmentManager.popBackStack()
    }

    //register BroadcastReceiver to know whether bluetooth is off
    private fun registerBluetoothStateBroadcastReceiver() {
        requireActivity().registerReceiver(
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }
}