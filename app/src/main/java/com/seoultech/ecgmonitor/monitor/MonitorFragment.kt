package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.FragmentMonitorBinding
import com.seoultech.ecgmonitor.ecgstate.ECGStateCallback
import com.seoultech.ecgmonitor.ecgstate.ECGStateLiveData
import com.seoultech.ecgmonitor.ecgstate.ECGStateObserver
import com.seoultech.ecgmonitor.findNavController
import com.seoultech.ecgmonitor.heartrate.HeartRateLiveData
import com.seoultech.ecgmonitor.heartrate.HeartRateSnapshotLiveData
import com.sergivonavi.materialbanner.Banner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MonitorFragment : Fragment(), ECGStateCallback {

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
    lateinit var heartRateLiveData: HeartRateLiveData

    @Inject
    lateinit var heartRateSnapshotLiveData: HeartRateSnapshotLiveData

    @Inject
    lateinit var ecgStateLiveData: ECGStateLiveData

    private val ecgStateObserver = ECGStateObserver(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonitorBinding.inflate(inflater, container, false).apply {
            bannerMonitorBluetooth.setRightButtonListener {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarMonitor)
        setHasOptionsMenu(true)

        subscribeUi()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.monitor_app_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_monitor_disconnect -> {
                showDisconnectDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        //Stop drawing graph when this application is gone to background
        binding.ecggraphMonitor.stop()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun beforeBounded() {
        isConnected = false
        disableUi()
        showNoDeviceBanner()
    }

    override fun onBluetoothDisabled() {
        if (!bluetoothBannerIsShowing) {
            showBluetoothDisabledBanner()
            disableUi()
        }
    }

    override fun onBluetoothEnabled() {
        if (bluetoothBannerIsShowing) {
            dismissBluetoothDisabledBanner()
            enableUi()
        }
    }

    override fun onConnected() {
        if (!isConnected) {
            startPlot()
        }
    }

    override fun onDisconnected() {
        if (isConnected) {
            stopPlot()
        }
    }

    override fun onFailure() {
        Snackbar.make(
            binding.root, getString(R.string.monitor_snackbar_fail),
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }

    //연결된 기기가 없을 때 배너 띄우기
    private fun showNoDeviceBanner() {
        if (noDeviceBanner != null) {
            return
        }
        val banner = Banner.Builder(requireContext()).setParent(binding.linearlayoutMonitorBanner)
            .setMessage(getString(R.string.banner_no_device))
            .setRightButton(getString(R.string.banner_find_device)) {
                it.dismiss()
                noDeviceBanner = null
                val direction = MonitorFragmentDirections.actionMonitorFragmentToScanFragment()
                findNavController().navigate(direction)
            }
            .create()
        banner.show()
        noDeviceBanner = banner
    }

    //연결 상태 및 블루투스 연결 상태 구독
    private fun subscribeUi() {
        subscribeHeartRateValue()
        ecgStateLiveData.observe(viewLifecycleOwner, ecgStateObserver)
    }

    private fun showBluetoothDisabledBanner() {
        binding.bannerMonitorBluetooth.show()
        bluetoothBannerIsShowing = true
    }

    private fun dismissBluetoothDisabledBanner() {
        binding.bannerMonitorBluetooth.dismiss()
        bluetoothBannerIsShowing = false
    }

    private fun subscribeHeartRateValue() {
        //Observing heart rate value
        heartRateSnapshotLiveData.observe(viewLifecycleOwner, {
            binding.ecggraphMonitor.addValue(it)
        })
    }

    private fun stopPlot() {
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
        if (ecgStateLiveData.isBounded()) {
            if (ecgStateLiveData.isBluetoothEnabled()) {
                if (ecgStateLiveData.isConnected()) {
                    startPlot()
                } else {
                    stopPlot()
                    isConnected = false
                }
            }
        } else {
            beforeBounded()
        }
    }

    private fun showDisconnectDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.monitor_disconnect_dialog_title))
            .setMessage(getString(R.string.monitor_disconnect_dialog_message))
            .setPositiveButton(getString(R.string.monitor_disconnect_dialog_possitive_button)) { dialog, _ ->
                dialog.dismiss()
                monitorViewModel.disconnect()
            }
            .setNegativeButton(getString(R.string.monitor_disconnect_dialog_negative_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
//
//private fun changeScreenMode() {
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
//}