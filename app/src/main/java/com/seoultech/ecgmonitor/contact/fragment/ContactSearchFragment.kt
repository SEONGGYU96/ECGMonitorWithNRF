package com.seoultech.ecgmonitor.contact.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seoultech.ecgmonitor.contact.ContactListAdapter
import com.seoultech.ecgmonitor.contact.ContactViewModel
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.databinding.FragmentContactsSearchBinding
import com.seoultech.ecgmonitor.findNavController

class ContactSearchFragment: Fragment() {

    private lateinit var binding: FragmentContactsSearchBinding

    private val contactViewModel: ContactViewModel by activityViewModels()

    private val contactListAdapter = ContactListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsSearchBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = contactViewModel
            recyclerviewSearchcontact.adapter = contactListAdapter
        }

        setOnClickListener()

        return binding.root
    }

    private fun setOnClickListener() {
        contactListAdapter.setOnContactClickListener(this::saveContactAndExit)
        binding.run {
            imagebuttonSearchcontactBack.setOnClickListener { findNavController().navigateUp() }
            imagebuttonSearchcontactClear.setOnClickListener { edittextSearchcontactSearch.text = null }
        }
    }

    private fun saveContactAndExit(contact: Contact) {
        contactViewModel.insertContact(contact)
        requireActivity().finish()
    }
}