package com.seoultech.ecgmonitor.contact.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seoultech.ecgmonitor.contact.ContactFragmentDirections
import com.seoultech.ecgmonitor.contact.ContactListAdapter
import com.seoultech.ecgmonitor.contact.ContactViewModel
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.databinding.FragmentContactsBinding
import com.seoultech.ecgmonitor.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : Fragment() {

    private lateinit var binding: FragmentContactsBinding

    private val contactViewModel: ContactViewModel by activityViewModels()

    private val contactListAdapter = ContactListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = contactViewModel
            recyclerviewContacts.adapter = contactListAdapter
        }

        contactListAdapter.setOnContactClickListener(this::saveContactAndExit)
        setOnClickListener()

        return binding.root
    }

    private fun saveContactAndExit(contact: Contact) {
        contactViewModel.insertContact(contact)
        requireActivity().finish()
    }

    private fun setOnClickListener() {
        binding.run {
            imagebuttonContactsSearch.setOnClickListener { navigateSearchFragment() }
        }
    }

    private fun navigateSearchFragment() {
        findNavController()
            .navigate(ContactFragmentDirections.actionContactFragment2ToContactSearchFragment())
    }

}