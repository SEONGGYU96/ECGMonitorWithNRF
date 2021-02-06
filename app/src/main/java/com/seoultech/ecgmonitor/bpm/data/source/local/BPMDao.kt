package com.seoultech.ecgmonitor.bpm.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.seoultech.ecgmonitor.bpm.data.BPM

@Dao
interface BPMDao {
    @Query("SELECT * FROM bpm WHERE time >= :baseTime")
    suspend fun getBpmsAbove(baseTime: Long): List<BPM>

    @Query("SELECT AVG(bpm) FROM bpm WHERE time >= :baseTime")
    suspend fun getAverageOfBPMAbove(baseTime: Long): Int

    @Insert
    suspend fun insertBpm(bpm: BPM)
}