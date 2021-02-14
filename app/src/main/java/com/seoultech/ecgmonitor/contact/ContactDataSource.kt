package com.seoultech.ecgmonitor.contact

interface ContactDataSource {

    fun getContacts(callback: (List<Contact>) -> Unit)

    fun deleteContact(number: String)

    fun insertContact(contact: Contact)
}