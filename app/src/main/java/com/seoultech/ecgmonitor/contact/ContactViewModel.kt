package com.seoultech.ecgmonitor.contact

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.contact.data.source.ContactDataSource

class ContactViewModel @ViewModelInject constructor(
    private val contactDataSource: ContactDataSource
) : ViewModel() {

    val searchText = MutableLiveData<String>(null)

    private val _contacts = Transformations.map(searchText) {
        searchContact(it)
    }

    val contacts: LiveData<List<Contact>>
        get() = _contacts

    val contactCount: LiveData<Int> = Transformations.map(contacts) {
        it?.size ?: 0
    }

    private fun searchContact(name: String?): List<Contact> {
        return if (name.isNullOrEmpty()) {
            getContactsAll()
        } else {
            getContactsAll(name)
        }
    }

    fun insertContact(contact: Contact) {
        contactDataSource.insertContact(contact)
    }

    private fun getContactsAll(): List<Contact> {
        return contactDataSource.getContactsFromDevice()
    }

    private fun getContactsAll(name: String): List<Contact> {
        return contactDataSource.getSearchedContactsFromDevice(name)
    }
}