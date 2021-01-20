package com.seoultech.ecgmonitor.bluetooth

import android.content.Context
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnectible
import com.seoultech.ecgmonitor.bluetooth.connect.BluetoothGattConnector
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable
import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainer
import com.seoultech.ecgmonitor.bluetooth.scan.ScanFragment
import com.seoultech.ecgmonitor.ecgstate.ECGStateLiveData
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class BluetoothModule {

    @Provides
    @Singleton
    fun provideGattContainable() : GattContainable = GattContainer()

    @Provides
    @Singleton
    fun provideBluetoothGattConnector(
        @ApplicationContext context: Context,
        gattContainable: GattContainable
    ) : BluetoothGattConnectible = BluetoothGattConnector(context, gattContainable)

    @Provides
    fun provideBluetoothStateReceiver(ecgStateLiveData: ECGStateLiveData): BluetoothStateReceiver =
        BluetoothStateReceiver(ecgStateLiveData)

    @Provides
    @Singleton
    fun provideBluetoothStateLiveData(): BluetoothStateLiveData = BluetoothStateLiveData()

    @Provides
    @Named("bluetoothStateReceiver")
    fun provideBluetoothStateReceiverToScanComponent(bluetoothStateLiveData: BluetoothStateLiveData): BluetoothStateReceiver =
        BluetoothStateReceiver(bluetoothStateLiveData)
}