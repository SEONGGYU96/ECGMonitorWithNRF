package com.seoultech.ecgmonitor.heartrate

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class HeartRateModule {

    @Provides
    @Singleton
    fun provideHeartRateLiveData() : HeartRateLiveData = HeartRateLiveData()

    @Provides
    @Singleton
    fun provideHeartRateSnapshotLiveData() : HeartRateSnapshotLiveData = HeartRateSnapshotLiveData()

    @Provides
    fun provideHeartRateCalculator(heartRateLiveData: HeartRateLiveData) : HeartRateCalculable =
        HeartRateCalculator(heartRateLiveData)
}