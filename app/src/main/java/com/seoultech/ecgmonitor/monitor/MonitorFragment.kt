package com.seoultech.ecgmonitor.monitor

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.seoultech.ecgmonitor.MainActivity
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.FragmentMonitorBinding
import com.seoultech.ecgmonitor.ecgstate.ECGStateCallback
import com.seoultech.ecgmonitor.ecgstate.ECGStateLiveData
import com.seoultech.ecgmonitor.ecgstate.ECGStateObserver
import com.seoultech.ecgmonitor.bpm.data.BPMLiveData
import com.seoultech.ecgmonitor.bpm.data.HeartBeatSampleLiveData
import com.seoultech.ecgmonitor.setting.SettingActivity
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
    private var isLandscapeMode = true
    private var userChangeRotate = false
    private var bluetoothBannerIsShowing = false
    private var noDeviceBanner: Banner? = null
    private var disconnectMenuVisibility = false

    @Inject
    lateinit var bpmLiveData: BPMLiveData

    @Inject
    lateinit var heartBeatSampleLiveData: HeartBeatSampleLiveData

    @Inject
    lateinit var ecgStateLiveData: ECGStateLiveData

    private val ecgStateObserver = ECGStateObserver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonitorBinding.inflate(inflater, container, false)

        subscribeUi()
        setOnClickListener()

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //화면 방향을 변경하면 모두 초기화되므로 onConnect에서 화면을 다시 눕혀버림
        //따라서 사용자가 화면 방향을 변경하고 있는지는 기억하고 있어야함
        outState.putBoolean("userChangeRotate", userChangeRotate)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        userChangeRotate = savedInstanceState?.getBoolean("userChangeRotate", false)?: false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.monitor_app_bar, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.menu_monitor_disconnect).isVisible = disconnectMenuVisibility
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_monitor_disconnect -> {
                showDisconnectDialog()
                true
            }
            R.id.menu_monitor_setting -> {
                navigateSettingFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        startMonitoring(false)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun beforeBounded() {
        isConnected = false
        userChangeRotate = false
        changePortrait()
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
            isConnected = true
            if (!userChangeRotate) {
                changeLandscape()
            }
            startPlot()
        }
    }

    override fun onDisconnected() {
        if (isConnected) {
            isConnected = false
            if (!userChangeRotate) {
                changePortrait()
            }
            stopPlot()
        }
    }

    override fun onFailure() {
        Snackbar.make(
            binding.root, getString(R.string.monitor_snackbar_fail),
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }

    private fun refresh() {
        ecgStateLiveData.refresh()
    }

    private fun setDisconnectMenuVisibility(isVisible: Boolean) {
        disconnectMenuVisibility = isVisible
        (requireActivity() as AppCompatActivity).invalidateOptionsMenu()
    }

    private fun initToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.monitor_title)
    }

    private fun setOnClickListener() {
        binding.run {
            imagebuttonMonitorRotate.setOnClickListener {
                userChangeRotate = true
                changeScreenMode()
            }
            bannerMonitorBluetooth.setRightButtonListener {
                startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun showNoDeviceBanner() {
        if (noDeviceBanner != null) {
            return
        }
        val banner = Banner.Builder(requireContext()).setParent(binding.linearlayoutMonitorBanner)
            .setMessage(getString(R.string.banner_no_device))
            .setRightButton(getString(R.string.scan_title)) {
                it.dismiss()
                noDeviceBanner = null
//                val direction = MonitorFragmentDirections.actionMonitorFragmentToScanFragment()
//                findNavController().navigate(direction)
                (requireActivity() as MainActivity).navigateScanFragment()
            }
            .create()
        banner.show()
        noDeviceBanner = banner
    }

    private fun navigateSettingFragment() {
        requireActivity().run {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    private fun subscribeUi() {
        heartBeatSampleLiveData.observe(viewLifecycleOwner, {
            binding.ecggraphMonitor.addValue(it.value, it.time)
        })

        ecgStateLiveData.observe(viewLifecycleOwner, ecgStateObserver)

        bpmLiveData.observe(viewLifecycleOwner) {
            binding.textviewMonitorHeartrate.text = it.toString()
        }
    }

    private fun showBluetoothDisabledBanner() {
        binding.bannerMonitorBluetooth.show()
        bluetoothBannerIsShowing = true
    }

    private fun dismissBluetoothDisabledBanner() {
        binding.bannerMonitorBluetooth.dismiss()
        bluetoothBannerIsShowing = false
    }

    private fun stopPlot() {
        disableUi()
        Snackbar.make(
            binding.root,
            getString(R.string.monitor_snackbar_disconnected),
            Snackbar.LENGTH_INDEFINITE
        ).show()
    }

    private fun startPlot() {
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
        startMonitoring(true)
        setRotateButtonVisibility(true)
        changeBPMViewColor(R.color.red)
        setDisconnectMenuVisibility(true)
    }

    private fun startMonitoring(isStart: Boolean) {
        if (isStart) {
            binding.ecggraphMonitor.start()
        } else {
            binding.ecggraphMonitor.stop()
        }
    }

    //UI 비활성화
    private fun disableUi() {
        initBPMTextView()
        startMonitoring(false)
        setRotateButtonVisibility(false)
        changeBPMViewColor(R.color.colorGray)
    }

    private fun initBPMTextView() {
        binding.textviewMonitorHeartrate.text = getString(R.string.monitor_no_heart_rate)
    }

    private fun setRotateButtonVisibility(isVisible: Boolean) {
        binding.imagebuttonMonitorRotate.visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun changeBPMViewColor(@ColorRes colorRes: Int) {
        binding.run {
            imageviewMonitorHeart.imageTintList =
                ContextCompat.getColorStateList(requireContext(), colorRes)
            textviewMonitorHeartrate.setTextColor(
                ContextCompat.getColor(requireContext(), colorRes)
            )
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

    private fun changeScreenMode() {
        isLandscapeMode = if (isLandscapeMode) {
            changePortrait()
            false
        } else {
            changeLandscape()
            true
        }
    }

    private fun changePortrait() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun changeLandscape() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

