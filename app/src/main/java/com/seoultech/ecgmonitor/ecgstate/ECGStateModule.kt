package com.seoultech.ecgmonitor.ecgstate

import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable
import com.seoultech.ecgmonitor.heartrate.HeartRateSnapshotLiveData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class ECGStateModule {

    @Provides
    @Singleton
    fun providesECGStateLiveData(
        heartRateSnapshotLiveData: HeartRateSnapshotLiveData,
        gattContainable: GattContainable
    ): ECGStateLiveData = ECGStateLiveData(heartRateSnapshotLiveData, gattContainable)
}