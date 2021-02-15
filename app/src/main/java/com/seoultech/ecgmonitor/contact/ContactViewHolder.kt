package com.seoultech.ecgmonitor.contact

import android.view.ViewGroup
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.base.BaseViewHolder
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.databinding.ItemContactListBinding

class ContactViewHolder(parent: ViewGroup, val listener: ((Contact) -> Unit)?)
    : BaseViewHolder<Contact, ItemContactListBinding>(R.layout.item_contact_list, parent) {

    override fun bind(data: Contact) {
        binding.run {
            viewModel = data
            listener?.let {
                root.setOnClickListener { it(data) }
            }
        }
    }
}