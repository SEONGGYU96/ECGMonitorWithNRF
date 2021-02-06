package com.seoultech.ecgmonitor.ecgstate

import com.seoultech.ecgmonitor.bluetooth.gatt.GattContainable
import com.seoultech.ecgmonitor.bpm.data.HeartBeatSampleLiveData
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
        heartBeatSampleLiveData: HeartBeatSampleLiveData,
        gattContainable: GattContainable
    ): ECGStateLiveData = ECGStateLiveData(heartBeatSampleLiveData, gattContainable)
}