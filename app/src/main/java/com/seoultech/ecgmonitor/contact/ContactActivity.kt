package com.seoultech.ecgmonitor.contact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.*
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ActivityContactBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView<ActivityContactBinding>(this, R.layout.activity_contact)
    }
}