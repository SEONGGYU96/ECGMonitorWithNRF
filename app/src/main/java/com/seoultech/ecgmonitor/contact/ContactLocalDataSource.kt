package com.seoultech.ecgmonitor.contact

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactLocalDataSource(private val contactDao: ContactDao): ContactDataSource {

    override fun getContacts(callback: (List<Contact>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            callback(contactDao.getContacts())
        }
    }

    override fun deleteContact(number: String) {
       GlobalScope.launch(Dispatchers.IO) {
           contactDao.deleteContact(number)
       }
    }

    override fun insertContact(contact: Contact) {
        GlobalScope.launch(Dispatchers.IO) {
            contactDao.insertContact(contact)
        }
    }
}