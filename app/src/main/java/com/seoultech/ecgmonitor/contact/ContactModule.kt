package com.seoultech.ecgmonitor.contact

import android.content.Context
import com.seoultech.ecgmonitor.contact.data.source.ContactDataSource
import com.seoultech.ecgmonitor.contact.data.source.local.ContactLocalDataSource
import com.seoultech.ecgmonitor.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class ContactModule {

    @Provides
    fun provideContactLocalDataSource(
        @ApplicationContext context: Context, appDatabase: AppDatabase) : ContactDataSource =
        ContactLocalDataSource(context, appDatabase.contactDao())
}