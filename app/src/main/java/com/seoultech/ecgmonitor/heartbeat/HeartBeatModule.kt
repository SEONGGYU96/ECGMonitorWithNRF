package com.seoultech.ecgmonitor.heartbeat

import com.seoultech.ecgmonitor.heartbeat.heartrate.HeartRateCalculator
import com.seoultech.ecgmonitor.heartbeat.heartrate.HeartRateCalculatorImpl
import com.seoultech.ecgmonitor.heartbeat.heartrate.HeartRateLiveData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class HeartBeatModule {

    @Provides
    @Singleton
    fun provideHeartRateLiveData() : HeartRateLiveData = HeartRateLiveData()

    @Provides
    @Singleton
    fun provideHeartRateSnapshotLiveData() : HeartBeatSampleLiveData = HeartBeatSampleLiveData()

    @Provides
    fun provideHeartRateCalculator(heartRateLiveData: HeartRateLiveData) : HeartRateCalculator =
        HeartRateCalculatorImpl(heartRateLiveData)
}