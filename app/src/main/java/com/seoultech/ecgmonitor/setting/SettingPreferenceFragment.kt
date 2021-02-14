package com.seoultech.ecgmonitor.setting

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.contact.Contact

class SettingPreferenceFragment: PreferenceFragmentCompat() {

    private lateinit var contactsCategory: PreferenceCategory

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

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

        contactsCategory = TouchableCategory(context).apply {
            key = "contacts"
            title = getString(R.string.setting_title_add_contact)
            isSelectable = true
            isEnabled = true
            setOnPreferenceClickListener {
                showAddContactDialog(it as PreferenceCategory)
                true
            }
        }

        preferenceScreen.addPreference(contactsCategory)
    }

    private fun showAddContactDialog(contactsCategory: PreferenceCategory) {
        val items = arrayOf(getString(R.string.setting_dialog_select1), getString(R.string.setting_dialog_select2))

        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.setting_dialog_title))
                .setItems(items) { _, which ->
                    if (which == 0) {
                        //Todo: 주소록에서 추가
                    } else {
                        showAddDirectContactDialog(contactsCategory)
                    }
                }
                .show()
        }
    }

    private fun showAddDirectContactDialog(contactsCategory: PreferenceCategory) {
        val enterLayout = layoutInflater.inflate(R.layout.dialog_add_contact, null, false)
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.setting_dialog_select2))
                .setView(enterLayout)
                .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                }
                .setPositiveButton(getString(R.string.dialog_add)) { dialog, _ ->
                    val name = enterLayout.findViewById<TextInputLayout>(R.id.textField_addcontact_name).editText?.text.toString()
                    val number = enterLayout.findViewById<TextInputLayout>(R.id.textField_addcontact_number).editText?.text.toString()
                    if (name.isNotBlank() && number.isNotBlank()) {
                        val contact = Contact(name, number)
                        addContactToCategory(contact, contactsCategory)
                        //Todo : DB에 저장
                    }
                }
                .show()
        }
    }

    private fun addContactToCategory(contact: Contact, contactCategory: PreferenceCategory) {
        val contactElement = Preference(context).apply {
            key = contact.number
            title = contact.name
            summary = contact.number
        }
        contactCategory.addPreference(contactElement)
    }
}