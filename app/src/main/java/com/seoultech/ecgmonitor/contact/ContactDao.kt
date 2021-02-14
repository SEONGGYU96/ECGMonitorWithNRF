package com.seoultech.ecgmonitor.contact

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact")
    suspend fun getContacts(): List<Contact>

    @Insert
    suspend fun insertContact(contact: Contact)

    @Query("DELETE FROM contact WHERE number = :number")
    suspend fun deleteContact(number: String)
}