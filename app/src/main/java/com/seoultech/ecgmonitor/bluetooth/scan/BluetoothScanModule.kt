package com.seoultech.ecgmonitor.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.seoultech.ecgmonitor.device.DeviceLiveData
import com.seoultech.ecgmonitor.utils.PermissionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat

@InstallIn(ActivityComponent::class)
@Module
class BluetoothScanModule {

    @Provides
    fun provideBluetoothScanner(): BluetoothScanable = BluetoothScanner(
        BluetoothAdapter.getDefaultAdapter(),
        BluetoothLeScannerCompat.getScanner()
    )

    @Provides
    fun provideScanStateLiveData(
        @ApplicationContext context: Context,
        bluetoothScanner: BluetoothScanable
    ) = ScanStateLiveData(
        bluetoothScanner.isBluetoothEnabled(),
        PermissionUtil.isLocationPermissionsGranted(context)
    )

    @Provides
    fun provideDeviceLiveData(): DeviceLiveData = DeviceLiveData()
}