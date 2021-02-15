package com.seoultech.ecgmonitor.contact

import android.view.ViewGroup
import com.seoultech.ecgmonitor.base.BaseAdapter
import com.seoultech.ecgmonitor.base.BaseViewHolder
import com.seoultech.ecgmonitor.contact.data.Contact

class ContactListAdapter : BaseAdapter<Contact>() {
    private var listener: ((Contact) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Contact, *> {
        return ContactViewHolder(parent, listener)
    }

    fun setOnContactClickListener(listener: (Contact) -> Unit) {
        this.listener = listener
    }
}