package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.bluetooth.BluetoothStateLiveData
import com.seoultech.ecgmonitor.bluetooth.BluetoothStateReceiver
import com.seoultech.ecgmonitor.bluetooth.scan.ScanActivity
import com.seoultech.ecgmonitor.databinding.FragmentMonitorBinding
import com.sergivonavi.materialbanner.Banner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MonitorFragment : Fragment() {

    companion object {
        private const val TAG = "MonitorActivity"
    }

    private lateinit var binding: FragmentMonitorBinding
    private val monitorViewModel: MonitorViewModel by viewModels()
    private var isConnected = false
    private var isFullScreen = false
    private var isBounded = false
    private var bluetoothBannerIsShowing = false

    private val bluetoothStateBroadcastReceiver: BroadcastReceiver by lazy { BluetoothStateReceiver() }

    @Inject
    lateinit var bluetoothStateLiveData: BluetoothStateLiveData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMonitorBinding.inflate(inflater, container, false)

        isBounded = monitorViewModel.checkBoundedDevice()

        if (isBounded) {
            subscribeUi(binding)
        } else {
            showNoDeviceBanner()
            enableUi()
        }

        return binding.root
    }

    private fun registerBluetoothStateBroadcastReceiver() {
        requireActivity().registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    private fun enableUi() {
        //Todo: Ui들 회색으로 변경하기, 메뉴 버튼 제거
    }

    private fun showNoDeviceBanner() {
        Banner.Builder(requireContext()).setParent(binding.linearlayoutMonitorBanner)
            .setMessage(getString(R.string.banner_no_device))
            .setRightButton(getString(R.string.banner_find_device)) {
                //Todo: Fragment 전환
                Log.d(TAG, "Banner : Go to next Fragment!")
                it.dismiss()
            }
            .create()
            .show()
    }

    private fun subscribeUi(binding: FragmentMonitorBinding) {
        monitorViewModel.gattLiveData.run {
            isConnected.observe(viewLifecycleOwner, {
                if (it) { //connected
                    this@MonitorFragment.isConnected = true
                    //Toast.makeText(this@MonitorFragment, "Connected", Toast.LENGTH_SHORT).show()

                } else { //disconnected
                    if (this@MonitorFragment.isConnected) {
                        this@MonitorFragment.isConnected = false
                        //Toast.makeText(this@MonitorFragment, "Disconnected", Toast.LENGTH_SHORT)
                        //    .show()
                        //finish()
                    }
                }

            })

            //Observing heart rate value
            receivedValue.observe(viewLifecycleOwner, {
                binding.ecggraphMonitor.addValue(it)
            })

            //Observing failure of connection state
            isFailure.observe(viewLifecycleOwner, {
                if (it) {
                    //Toast.makeText(this@MonitorFragment, "Fail", Toast.LENGTH_SHORT).show()
                    //finish()
                }
            })
        }

        bluetoothStateLiveData.observe(viewLifecycleOwner, {
            if (it) {
                if (bluetoothBannerIsShowing) {
                    binding.bannerMonitorBluetooth.dismiss()
                    bluetoothBannerIsShowing = false
                }
            } else {
                if (!bluetoothBannerIsShowing) {
                    binding.bannerMonitorBluetooth.show()
                    bluetoothBannerIsShowing = true
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        //Stop drawing graph when this application is gone to background
        if (isBounded) {
            binding.ecggraphMonitor.stop()
            try {
                requireActivity().unregisterReceiver(bluetoothStateBroadcastReceiver)
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "onCleared() : Receiver not registered")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //change screen to landscape
        //changeScreenMode()
        //start graph
        if (isBounded) {
            binding.ecggraphMonitor.start()
            registerBluetoothStateBroadcastReceiver()
        }
    }

    private fun changeScreenMode() {
//        if (isFullScreen) {
//            return
//        }
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            window.decorView.windowInsetsController?.run {
//                hide(WindowInsets.Type.statusBars())
//                //hide(WindowInsets.Type.navigationBars())
//                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
//            }
//        } else {
//            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
//        }
//        isFullScreen = true
    }
}