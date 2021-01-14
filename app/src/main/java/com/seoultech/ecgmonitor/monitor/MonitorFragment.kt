package com.seoultech.ecgmonitor.monitor

import android.content.BroadcastReceiver
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.bluetooth.BluetoothStateLiveData
import com.seoultech.ecgmonitor.bluetooth.BluetoothStateReceiver
import com.seoultech.ecgmonitor.databinding.FragmentMonitorBinding
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
    private var isBounded = false
    private var bluetoothBannerIsShowing = false

    @Inject
    lateinit var bluetoothStateLiveData: BluetoothStateLiveData

    @Inject
    lateinit var heartRateLiveData: HeartRateLiveData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMonitorBinding.inflate(inflater, container, false)

        //연결 이력 확인
        isBounded = monitorViewModel.checkBoundedDevice()

        if (isBounded) { //연결 이력 있으면 연결 상태 구독
            subscribeUi(binding)
        } else { //없으면 UI 비활성화
            showNoDeviceBanner()
            disableUi()
        }

        return binding.root
    }

    //연결된 기기가 없을 때 배너 띄우기
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

    //연결 상태 및 블루투스 연결 상태 구독
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

        //블루투스 연결 상태 구독
        bluetoothStateLiveData.observe(viewLifecycleOwner, {
            if (it) {
                if (bluetoothBannerIsShowing) {
                    binding.bannerMonitorBluetooth.dismiss()
                    bluetoothBannerIsShowing = false
                    enableUi()
                    binding.ecggraphMonitor.stop()
                }
            } else {
                if (!bluetoothBannerIsShowing) {
                    binding.bannerMonitorBluetooth.show()
                    bluetoothBannerIsShowing = true
                    disableUi()
                    binding.ecggraphMonitor.stop()
                }
            }
        })
    }

    //UI 활성화
    private fun enableUi() {
        binding.run {
            imageviewMonitorHeart.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.red)
            textviewMonitorHeartrate.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.red))
            toolbarMonitor.menu.getItem(0).isVisible = true
        }
    }

    //UI 비활성화
    private fun disableUi() {
        binding.run {
            imageviewMonitorHeart.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.colorDisabledUi)
            textviewMonitorHeartrate.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorDisabledUi))
            toolbarMonitor.menu.getItem(0).isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        //Stop drawing graph when this application is gone to background
        if (isBounded) {
            binding.ecggraphMonitor.stop()
        }
    }

    override fun onResume() {
        super.onResume()
        //change screen to landscape
        //changeScreenMode()
        //start graph
        if (isBounded) {
            binding.ecggraphMonitor.start()
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