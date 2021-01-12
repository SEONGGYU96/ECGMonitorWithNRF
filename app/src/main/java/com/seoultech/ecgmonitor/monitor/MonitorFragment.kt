package com.seoultech.ecgmonitor.monitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.seoultech.ecgmonitor.databinding.FragmentMonitorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonitorFragment : Fragment() {

    companion object {
        private const val TAG = "MonitorActivity"
    }

    private lateinit var binding: FragmentMonitorBinding
    private val monitorViewModel: MonitorViewModel by viewModels()
    private var isConnected = false
    private var isFullScreen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMonitorBinding.inflate(inflater, container, false)

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
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        //Stop drawing graph when this application is gone to background
        binding.ecggraphMonitor.stop()
    }

    override fun onResume() {
        super.onResume()
        //change screen to landscape
        //changeScreenMode()
        //start graph
        binding.ecggraphMonitor.start()
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