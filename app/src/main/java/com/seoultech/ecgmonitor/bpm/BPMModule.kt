package com.seoultech.ecgmonitor.bpm

import android.content.Context
import com.seoultech.ecgmonitor.bpm.calculate.BPMCalculator
import com.seoultech.ecgmonitor.bpm.calculate.BPMCalculatorImpl
import com.seoultech.ecgmonitor.bpm.data.BPMLiveData
import com.seoultech.ecgmonitor.bpm.data.HeartBeatSampleLiveData
import com.seoultech.ecgmonitor.database.AppDatabase
import com.seoultech.ecgmonitor.bpm.data.source.BPMDataSource
import com.seoultech.ecgmonitor.bpm.data.source.local.BPMLocalDataSource
import com.seoultech.ecgmonitor.bpm.detect.AbnormalBPMDetector
import com.seoultech.ecgmonitor.bpm.detect.AbnormalBPMDetectorImpl
import com.seoultech.ecgmonitor.protocol.AbnormalProtocol
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class BPMModule {

    @Provides
    @Singleton
    fun provideHeartRateLiveData() : BPMLiveData = BPMLiveData()

    @Provides
    @Singleton
    fun provideHeartRateSnapshotLiveData() : HeartBeatSampleLiveData = HeartBeatSampleLiveData()

    @Provides
    fun provideHeartRateCalculator() : BPMCalculator =
        BPMCalculatorImpl()

    @Provides
    fun provideBPMLocalDataSource(appDatabase: AppDatabase) : BPMDataSource =
        BPMLocalDataSource(appDatabase.bpmDao())

    @Provides
    fun provideAbnormalBPMDetector(bpmDataSource: BPMDataSource) : AbnormalBPMDetector =
        AbnormalBPMDetectorImpl(bpmDataSource)

    @Provides
    fun provideSampleStorageManager(@ApplicationContext context: Context) : SampleStorageManager =
        SampleStorageManagerImpl(context)

    @Provides
    fun provideBPMManager(
        bpmCalculator: BPMCalculator,
        abnormalBPMDetector: AbnormalBPMDetector,
        abnormalProtocol: AbnormalProtocol
    ) : BPMManager =
        BPMManager(bpmCalculator, abnormalBPMDetector, abnormalProtocol)
}