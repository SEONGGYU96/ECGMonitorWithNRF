package com.seoultech.ecgmonitor.setting

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.contact.data.source.ContactDataSource

class SettingViewModel @ViewModelInject constructor(
    private val contactDataSource: ContactDataSource
): ViewModel() {

    fun insertContact(contact: Contact) {
        contactDataSource.insertContact(contact)
    }

    fun deleteContact(number: String) {
        contactDataSource.deleteContact(number)
    }

    fun getContacts(callback: (List<Contact>) -> Unit) {
        contactDataSource.getContacts(callback)
    }
}