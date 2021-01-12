package com.seoultech.ecgmonitor.service

import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnectible
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable
import com.seoultech.ecgmonitor.bluetooth.gatt.GattLiveData
import com.seoultech.ecgmonitor.monitor.MonitorActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * GATT 연결 및 유지 담당 서비스
 * 최초 연결 후 의도적인 연결 해제 외에는 종료되지 않아야 함
 */
@AndroidEntryPoint
class GattConnectionMaintenanceService : LifecycleService() {

    companion object {
        private const val TAG = "ConnectionService"

        private const val NOTIFICATION_ID = 1
        const val FLAG_CONNECTED = "FLAG_CONNECTED"
        const val EXTRA_DISCOVERED_DEVICE = "discoveredDevice"
    }

    //GATT 객체 컨테이너 (싱글턴)
    @Inject
    lateinit var gattContainer: GattContainable

    //GATT 연결 모듈
    @Inject
    lateinit var gattConnector: BluetoothGattConnectible

    //GATT 연결 상태 라이브데이터 (싱글턴)
    @Inject
    lateinit var gattLiveData: GattLiveData

    //Notification 생성 모듈
    @Inject
    lateinit var notification: NotificationGenerator

    //Notification 터치 시 동작할 PendingIntent
    private val pendingIntent: PendingIntent by lazy {
        Intent(this, MonitorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private lateinit var connectionStateObserver: Observer<Boolean>

    override fun onCreate() {
        super.onCreate()

        //연결 상태에 따른 Observer
        connectionStateObserver = Observer {
            if (gattContainer.hasGatt()) { //GATT 객체가 없으면 초기 값 때문에 한 번 실행되는 것 방지
                if (it) {
                    Log.d(TAG, "observer : connected")
                    startForeground(
                        NOTIFICATION_ID,
                        notification.getConnectingNotification(pendingIntent)
                    )
                } else {
                    Log.d(TAG, "observer : disconnected")
                    startForeground(
                        NOTIFICATION_ID,
                        notification.getDisconnectedNotification(pendingIntent)
                    )
                }
            }
        }

        //최초 생성 시 GATT 연결 상태에 따른 동작 Observing
        gattLiveData.isConnected.observeForever(connectionStateObserver)

        //gattLiveData.receivedValue.observeForever {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!gattContainer.hasGatt()) {//GattContainer 에 gatt 이 없으면 연결 전이거나 잘못된 접근
            Log.d(TAG, "onStartCommand : GattContainer doesn't have GATT")
            val discoveredDevice = intent?.getParcelableExtra<BluetoothDevice>(
                EXTRA_DISCOVERED_DEVICE)

            if (discoveredDevice == null) { //intent로 전달된 BluetoothDevice도 없으면 잘못된 접근
                Log.e(TAG, "Discovered device is null. Connection is not invalid.")
                stopSelf() //서비스 종료
                return super.onStartCommand(intent, flags, startId)
            }

            //intent로 전달된 BluetoothDevice가 있으면 해당 기기와 연결 실행 및 ForegroundService 시작
            gattConnector.connect(discoveredDevice, gattLiveData) //BluetoothDevice 가 있으면 연결
            startForeground(
                NOTIFICATION_ID,
                notification.getConnectingNotification(pendingIntent)
            )
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        //서비스 종료 시 Observer들을 수동으로 해제해주어야함
        gattLiveData.run {
            isConnected.removeObserver(connectionStateObserver)
        }
    }
}