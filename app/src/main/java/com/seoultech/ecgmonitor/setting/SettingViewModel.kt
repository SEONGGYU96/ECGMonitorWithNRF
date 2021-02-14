package com.seoultech.ecgmonitor.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.contact.Contact
import com.seoultech.ecgmonitor.contact.ContactDataSource

class SettingViewModel @ViewModelInject constructor(
    private val contactDataSource: ContactDataSource): ViewModel() {

    fun insertContact(contact: Contact) {
        contactDataSource.insertContact(contact)
    }

    fun deleteContact(contact: Contact) {
        contactDataSource.deleteContact(contact.number)
    }

    fun getContacts(callback: (List<Contact>) -> Unit) {
        contactDataSource.getContacts(callback)
    }
}