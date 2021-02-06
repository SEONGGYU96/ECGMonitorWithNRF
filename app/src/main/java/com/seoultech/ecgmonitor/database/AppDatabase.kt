package com.seoultech.ecgmonitor.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.bpm.data.source.local.BPMDao

@Database(entities = [BPM::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun bpmDao(): BPMDao
}