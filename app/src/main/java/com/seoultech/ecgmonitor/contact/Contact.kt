package com.seoultech.ecgmonitor.contact

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact")
data class Contact(
    val name: String,
    @PrimaryKey
    val number: String
)
