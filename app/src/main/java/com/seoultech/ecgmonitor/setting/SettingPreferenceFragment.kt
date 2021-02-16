package com.seoultech.ecgmonitor.setting

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.contact.data.Contact
import com.seoultech.ecgmonitor.contact.ContactActivity
import com.seoultech.ecgmonitor.utils.PermissionUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingPreferenceFragment : PreferenceFragmentCompat() {

    companion object {
        private const val CONTACT_PREFERENCE_KEY = "contacts"
        private const val INSERT_BUTTON_PREFERENCE_KEY = "insert_button"
        const val USER_NAME_PREFERENCE_KEY = "user_name"
        const val SMS_PREFERENCE_KEY = "sms"

        const val REQUEST_SMS_PERMISSION_CODE = 1
        const val REQUEST_READ_CONTACT_PERMISSION_CODE = 2
    }

    private val settingViewModel: SettingViewModel by viewModels()

    private lateinit var contactsCategory: PreferenceCategory

    private val contactInsertPreferenceButton: Preference by lazy(this::getContactInsertButton)
    private lateinit var smsSwitchPreference: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        initSwitchPreferenceCallback()
        initNameSetting()
        initContactCategory()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.addPreference(contactsCategory)
        settingViewModel.getContacts(this::initContacts)
    }

    override fun onStop() {
        super.onStop()
        contactsCategory.removeAll()
    }

    private fun initSwitchPreferenceCallback() {
        smsSwitchPreference = findPreference<SwitchPreferenceCompat>(SMS_PREFERENCE_KEY)!!.apply {
            setOnPreferenceChangeListener(this@SettingPreferenceFragment::requestSMSPermissionIfNeed)
        }
    }

    private fun requestSMSPermissionIfNeed(preference: Preference, newValue: Any): Boolean {
        if (preference.key != SMS_PREFERENCE_KEY) {
            return false
        }
        if (newValue as Boolean) {
            context?.let {
                return if (!PermissionUtil.isSMSPermissionsGranted(it)) {
                    PermissionUtil.requestSMSPermission(requireActivity(), REQUEST_SMS_PERMISSION_CODE)
                    false
                } else {
                    true
                }
            }
        }
        return true
    }

    fun setSMSSwitch(isChecked: Boolean) {
        smsSwitchPreference.isChecked = isChecked
    }

    private fun initNameSetting() {
        val namePreference = findPreference<EditTextPreference>(USER_NAME_PREFERENCE_KEY)
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
            key = CONTACT_PREFERENCE_KEY
            title = getString(R.string.setting_title_add_contact)
        }
    }

    private fun initContacts(contacts: List<Contact>) {
        contactsCategory.addPreference(contactInsertPreferenceButton)
        for (contact in contacts) {
            addContactToCategory(contact)
        }
    }

    private fun getContactInsertButton(): Preference {
        return Preference(context).apply {
            key = INSERT_BUTTON_PREFERENCE_KEY
            title = getString(R.string.setting_add_contact_button)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_add_24)
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
                        if (PermissionUtil.isContactPermissionGranted(it)) {
                            startContactActivity()
                        } else {
                            PermissionUtil.requestContactPermission(
                                requireActivity(), REQUEST_READ_CONTACT_PERMISSION_CODE)
                        }
                    } else {
                        showAddDirectContactDialog()
                    }
                }
                .show()
        }
    }

    fun startContactActivity() {
        requireActivity().run {
            startActivity(Intent(this, ContactActivity::class.java))
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
            icon = ContextCompat.getDrawable(context, R.drawable.ic_people_24)
            setOnPreferenceClickListener(this@SettingPreferenceFragment::showRemoveDialog)
        }
        contactsCategory.addPreference(contactElement)
    }

    private fun showRemoveDialog(preference: Preference): Boolean {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(getString(R.string.setting_dialog_remove_title))
                .setMessage(getString(R.string.setting_dialog_remove_message))
                .setNegativeButton(getString(R.string.dialog_cancel)) { _, _ -> }
                .setPositiveButton(getString(R.string.dialog_remove)) { _, _ ->
                    removeContact(preference)
                }
                .show()
        }
        return true
    }

    private fun removeContact(preference: Preference) {
        settingViewModel.deleteContact(preference.summary.toString())
        contactsCategory.removePreference(preference)
    }
}