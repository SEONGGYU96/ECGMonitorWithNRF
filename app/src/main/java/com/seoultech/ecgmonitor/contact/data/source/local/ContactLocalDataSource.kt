package com.seoultech.ecgmonitor.contact.data.source.local

import android.content.Context
import android.provider.ContactsContract
import androidx.loader.content.CursorLoader
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.contact.data.source.ContactDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContactLocalDataSource(
    private val context: Context,
    private val contactDao: ContactDao
) : ContactDataSource {

    override fun getContacts(callback: (List<Contact>) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            callback(contactDao.getContacts())
        }
    }

    override fun getContactsFromDevice(): List<Contact> {
       return getDeviceContacts()
    }

    override fun getSearchedContactsFromDevice(name: String): List<Contact> {
        return getSearchedDeviceContacts(name)
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

    private fun getSearchedDeviceContacts(name: String): List<Contact> {
        val selectedContacts = mutableListOf<Contact>()
        val contacts = getDeviceContacts()

        for (contact in contacts) {
            if (contact.name.contains(name)) {
                selectedContacts.add(contact)
            }
        }

        return selectedContacts
    }

    private fun getDeviceContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursorLoader = CursorLoader(
            context, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null,
            null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ")ASC"
        )

        val c = cursorLoader.loadInBackground()

        c?.let {
            if (it.moveToFirst()) {
                val number = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val name = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                do {
                    contacts.add(Contact(it.getString(name), it.getString(number)))
                } while (it.moveToNext())
                it.close()
            }
        }
        return contacts
    }
}