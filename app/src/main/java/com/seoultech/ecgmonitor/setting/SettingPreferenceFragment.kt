package com.seoultech.ecgmonitor.setting

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.viewModels
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.contact.Contact
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingPreferenceFragment : PreferenceFragmentCompat() {

    private val settingViewModel: SettingViewModel by viewModels()

    private lateinit var contactsCategory: PreferenceCategory

    private val contactInsertPreferenceButton: Preference by lazy(this::getContactInsertButton)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        initNameSetting()
        initContactCategory()
    }

    private fun initNameSetting() {
        val namePreference = findPreference<EditTextPreference>("user_name")
        namePreference?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                if (TextUtils.isEmpty(text)) {
                    getString(R.string.setting_name_default)
                } else {
                    text
                }
            }
    }

    private fun initContactCategory() {
        contactsCategory = PreferenceCategory(context).apply {
            key = "contacts"
            title = getString(R.string.setting_title_add_contact)
        }
        preferenceScreen.addPreference(contactsCategory)
        settingViewModel.getContacts(this::initContacts)
    }

    private fun initContacts(contacts: List<Contact>) {
        contactsCategory.addPreference(contactInsertPreferenceButton)
        for (contact in contacts) {
            addContactToCategory(contact)
        }
    }

    private fun getContactInsertButton(): Preference {
        return Preference(context).apply {
            key = "insert_button"
            title = getString(R.string.setting_add_contact_button)
            setOnPreferenceClickListener {
                showAddContactDialog()
                true
            }
        }
    }

    private fun showAddContactDialog() {
        val items = arrayOf(
            getString(R.string.setting_dialog_select1),
            getString(R.string.setting_dialog_select2)
        )

        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.setting_dialog_title))
                .setItems(items) { _, which ->
                    if (which == 0) {
                        //Todo: 주소록에서 추가
                    } else {
                        showAddDirectContactDialog()
                    }
                }
                .show()
        }
    }

    private fun showAddDirectContactDialog() {
        val enterLayout = layoutInflater
            .inflate(R.layout.dialog_add_contact, null, false)
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.setting_dialog_select2))
                .setView(enterLayout)
                .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> }
                .setPositiveButton(getString(R.string.dialog_add)) { _, _ ->
                    val name = enterLayout
                        .findViewById<TextInputLayout>(R.id.textField_addcontact_name)
                        .editText?.text.toString()
                    val number = enterLayout
                        .findViewById<TextInputLayout>(R.id.textField_addcontact_number)
                        .editText?.text.toString()
                    if (name.isNotBlank() && number.isNotBlank()) {
                        val contact = Contact(name, number)
                        addContactToCategory(contact)
                        settingViewModel.insertContact(contact)
                    }
                }
                .show()
        }
    }

    private fun addContactToCategory(contact: Contact) {
        val contactElement = Preference(context).apply {
            key = contact.number
            title = contact.name
            summary = contact.number
        }
        contactsCategory.addPreference(contactElement)
    }
}