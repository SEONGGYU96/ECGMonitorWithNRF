package com.seoultech.ecgmonitor.bpm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bpm")
data class BPM(
    @PrimaryKey val time: Long,
    val bpm: Int
)
