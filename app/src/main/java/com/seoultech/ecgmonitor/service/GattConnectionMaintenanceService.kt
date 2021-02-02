package com.seoultech.ecgmonitor.service

import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.seoultech.ecgmonitor.MainActivity
import com.seoultech.ecgmonitor.bluetooth.state.BluetoothStateReceiver
import com.seoultech.ecgmonitor.ecgstate.ECGStateCallback
import com.seoultech.ecgmonitor.ecgstate.ECGStateLiveData
import com.seoultech.ecgmonitor.ecgstate.ECGStateObserver
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnectible
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable
import com.seoultech.ecgmonitor.heartbeat.heartrate.HeartRateCalculator
import com.seoultech.ecgmonitor.heartbeat.HeartBeatSampleLiveData
import com.seoultech.ecgmonitor.heartbeat.heartrate.BPMManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * GATT 연결 및 유지 담당 서비스
 * 최초 연결 후 의도적인 연결 해제 외에는 종료되지 않아야 함
 */
@AndroidEntryPoint
class GattConnectionMaintenanceService : LifecycleService(), ECGStateCallback {

    companion object {
        private const val TAG = "ConnectionService"

        private const val NOTIFICATION_ID = 1
        const val EXTRA_DISCOVERED_DEVICE = "discoveredDevice"
    }

    @Inject
    lateinit var heartRateCalculator: HeartRateCalculator

    //GATT 객체 컨테이너 (싱글턴)
    @Inject
    lateinit var gattContainer: GattContainable

    //GATT 연결 모듈
    @Inject
    lateinit var gattConnector: BluetoothGattConnectible

    //GATT 연결 상태 라이브데이터 (싱글턴)
    @Inject
    lateinit var ecgStateLiveData: ECGStateLiveData

    //Notification 생성 모듈
    @Inject
    lateinit var notification: NotificationGenerator

    @Inject
    lateinit var bluetoothStateReceiver: BluetoothStateReceiver

    @Inject
    lateinit var heartBeatSampleLiveData: HeartBeatSampleLiveData

    private var boundedDevice: BluetoothDevice? = null

    private val ecgStateObserver = ECGStateObserver(this)

    private val heartRateSnapshotObserver = Observer<HeartBeatSampleLiveData> {
        heartRateCalculator.addValue(it.value)
    }

    //Todo: Injection
    private val bpmManager = BPMManager()

    //Notification 터치 시 동작할 PendingIntent
    private val pendingIntent: PendingIntent by lazy {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!gattContainer.hasGatt()) {//GattContainer 에 gatt 이 없으면 연결 전이거나 잘못된 접근
            Log.d(TAG, "onStartCommand : GattContainer doesn't have GATT")
            val discoveredDevice = intent?.getParcelableExtra<BluetoothDevice>(
                EXTRA_DISCOVERED_DEVICE
            )

            if (discoveredDevice == null) { //intent로 전달된 BluetoothDevice도 없으면 잘못된 접근
                Log.e(TAG, "Discovered device is null. Connection is not invalid.")
                stopSelf() //서비스 종료
                return super.onStartCommand(intent, flags, startId)
            }

            //intent로 전달된 BluetoothDevice가 있으면 해당 기기와 연결 실행 및 ForegroundService 시작
            connect(discoveredDevice) //BluetoothDevice 가 있으면 연결
            boundedDevice = discoveredDevice
            ecgStateLiveData.observeForever(ecgStateObserver)

            setNotification(State.CONNECTED)
            registerBluetoothStateBroadcastReceiver()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        //서비스 종료 시 Observer들을 수동으로 해제해주어야함
        ecgStateLiveData.removeObserver(ecgStateObserver)
        stopHeartRateCalculate()
        unRegisterBluetoothStateBroadcastReceiver()
    }

    override fun beforeBounded() {
        stopSelf()
    }

    override fun onBluetoothDisabled() {
        setNotification(State.BLUETOOTH_DISABLED)
        stopHeartRateCalculate()
    }

    override fun onBluetoothEnabled() {
        boundedDevice?.let {
            gattContainer.gatt?.disconnect()
            connect(it)
            startHeartRateCalculate()
        } ?: run {
            throw IllegalStateException()
        }
    }

    override fun onConnected() {
        setNotification(State.CONNECTED)
        startHeartRateCalculate()
    }

    override fun onDisconnected() {
        setNotification(State.DISCONNECTED)
        stopHeartRateCalculate()
    }

    override fun onFailure() {
        setNotification(State.FAILURE)
        stopHeartRateCalculate()
    }

    private fun connect(device: BluetoothDevice) {
        gattConnector.connect(device, ecgStateLiveData)
        Log.d(TAG, "connect(): Try connect with ${device.address}")
    }

    private fun setNotification(state: State) {
        when (state) {
            State.CONNECTED -> refreshNotification(
                notification.getConnectingNotification(pendingIntent)
            )

            State.DISCONNECTED -> refreshNotification(
                notification.getDisconnectedNotification(pendingIntent)
            )

            State.BLUETOOTH_DISABLED -> refreshNotification(
                notification.getBluetoothDisabledNotification(pendingIntent)
            )

            else -> refreshNotification(
                notification.getFailureNotification(pendingIntent)
            )
        }
    }

    private fun refreshNotification(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startHeartRateCalculate() {
        heartRateCalculator.setOnBPMCalculatedListener {
            gattConnector.sendAliveMessage()
            bpmManager.addBpm(it)
        }
        heartRateCalculator.startCalculating()
        heartBeatSampleLiveData.observeForever(heartRateSnapshotObserver)
    }

    private fun stopHeartRateCalculate() {
        heartRateCalculator.stopCalculating()
        heartBeatSampleLiveData.removeObserver(heartRateSnapshotObserver)
    }

    private fun unRegisterBluetoothStateBroadcastReceiver() {
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "Receiver not registered")
        }
    }

    private fun registerBluetoothStateBroadcastReceiver() {
        registerReceiver(
            bluetoothStateReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    enum class State {
        CONNECTED, DISCONNECTED, BLUETOOTH_DISABLED, FAILURE;
    }
}


