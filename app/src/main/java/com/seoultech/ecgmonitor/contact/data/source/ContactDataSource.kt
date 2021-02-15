package com.seoultech.ecgmonitor.contact.data.source

import com.seoultech.ecgmonitor.contact.data.Contact

interface ContactDataSource {

    fun getContacts(callback: (List<Contact>) -> Unit)

    fun getContactsFromDevice(): List<Contact>

    fun getSearchedContactsFromDevice(name: String): List<Contact>

    fun deleteContact(number: String)

    fun insertContact(contact: Contact)
}