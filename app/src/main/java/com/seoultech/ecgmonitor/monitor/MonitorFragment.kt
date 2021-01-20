package com.seoultech.ecgmonitor.monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.bluetooth.BluetoothStateLiveData
import com.seoultech.ecgmonitor.databinding.FragmentMonitorBinding
import com.seoultech.ecgmonitor.findNavController
import com.seoultech.ecgmonitor.heartrate.HeartRateLiveData
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
    private var bluetoothBannerIsShowing = false
    private var noDeviceBanner: Banner? = null

    @Inject
    lateinit var bluetoothStateLiveData: BluetoothStateLiveData

    @Inject
    lateinit var heartRateLiveData: HeartRateLiveData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonitorBinding.inflate(inflater, container, false)

        subscribeUi()

        if (!monitorViewModel.checkBoundedDevice()) {
            showNoDeviceBanner()
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        //Stop drawing graph when this application is gone to background
        binding.ecggraphMonitor.stop()
    }

    override fun onResume() {
        super.onResume()
        refresh()
        if (isConnected) {
            binding.ecggraphMonitor.start()
        }
    }

    //연결된 기기가 없을 때 배너 띄우기
    private fun showNoDeviceBanner() {
        val banner = Banner.Builder(requireContext()).setParent(binding.linearlayoutMonitorBanner)
            .setMessage(getString(R.string.banner_no_device))
            .setRightButton(getString(R.string.banner_find_device)) {
                it.dismiss()
                val direction = MonitorFragmentDirections.actionMonitorFragmentToScanFragment()
                findNavController().navigate(direction)
            }
            .create()
        banner.show()
        noDeviceBanner = banner
    }

    //연결 상태 및 블루투스 연결 상태 구독
    private fun subscribeUi() {
        subscribeConnectionState()
        subscribeHeartRateValue()
        subscribeConnectionIfFail()
        subscribeBluetoothState()
    }

    private fun subscribeBluetoothState() {
        //블루투스 연결 상태 구독
        bluetoothStateLiveData.observe(viewLifecycleOwner, { isEnabled ->
            if (isEnabled) {
                if (bluetoothBannerIsShowing) {
                    dismissBluetoothDisabledBanner()
                    enableUi()
                }
            } else {
                if (!bluetoothBannerIsShowing) {
                    showBluetoothDisabledBanner()
                    disableUi()
                }
            }
        })
    }

    private fun showBluetoothDisabledBanner() {
        binding.bannerMonitorBluetooth.show()
        bluetoothBannerIsShowing = true
    }

    private fun dismissBluetoothDisabledBanner() {
        binding.bannerMonitorBluetooth.dismiss()
        bluetoothBannerIsShowing = false
    }

    private fun subscribeConnectionIfFail() {
        //Observing failure of connection state
        monitorViewModel.gattLiveData.isFailure.observe(viewLifecycleOwner, {
            if (!monitorViewModel.checkBoundedDevice()) {
                if (it) {
                    Snackbar.make(
                        binding.root, getString(R.string.monitor_snackbar_fail),
                        Snackbar.LENGTH_INDEFINITE
                    ).show()
                }
            }
        })
    }

    private fun subscribeHeartRateValue() {
        //Observing heart rate value
        monitorViewModel.gattLiveData.receivedValue.observe(viewLifecycleOwner, {
            binding.ecggraphMonitor.addValue(it)
        })
    }

    private fun subscribeConnectionState() {
        monitorViewModel.gattLiveData.isConnected.observe(viewLifecycleOwner, {
            if (monitorViewModel.checkBoundedDevice()) {
                if (it) { //connected
                    if (!this@MonitorFragment.isConnected) {
                        startPlot()
                    }

                } else { //disconnected
                    if (this@MonitorFragment.isConnected) {
                        stopPloat()
                    }
                }
            }
        })
    }

    private fun stopPloat() {
        this@MonitorFragment.isConnected = false
        disableUi()
        Snackbar.make(
            binding.root,
            getString(R.string.monitor_snackbar_disconnected),
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }

    private fun startPlot() {
        this@MonitorFragment.isConnected = true
        enableUi()
        dismissNoDeviceBannerIfItShowing()
        Snackbar.make(
            binding.root,
            getString(R.string.monitor_snackbar_connected),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun dismissNoDeviceBannerIfItShowing() {
        noDeviceBanner?.dismiss()
        noDeviceBanner = null
    }

    //UI 활성화
    private fun enableUi() {
        binding.run {
            imageviewMonitorHeart.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            textviewMonitorHeartrate.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red)
            )
            toolbarMonitor.menu.getItem(0).isVisible = true
            ecggraphMonitor.start()
        }
    }

    //UI 비활성화
    private fun disableUi() {
        binding.run {
            imageviewMonitorHeart.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.colorDisabledUi)
            textviewMonitorHeartrate.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorDisabledUi)
            )
            toolbarMonitor.menu.getItem(0).isVisible = false
            ecggraphMonitor.stop()
        }
    }

    private fun refresh() {
        monitorViewModel.gattLiveData.isConnected.value?.let {
            if (it) {
                startPlot()
            } else {
                stopPloat()
            }
        } ?: run {
            isConnected = false
            disableUi()
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