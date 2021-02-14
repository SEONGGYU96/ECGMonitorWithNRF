package com.seoultech.ecgmonitor.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seoultech.ecgmonitor.bpm.data.BPM
import com.seoultech.ecgmonitor.bpm.data.source.local.BPMDao
import com.seoultech.ecgmonitor.contact.Contact
import com.seoultech.ecgmonitor.contact.ContactDao

@Database(entities = [BPM::class, Contact::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun bpmDao(): BPMDao
    abstract fun contactDao(): ContactDao
}