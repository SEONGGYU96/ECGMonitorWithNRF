package com.seoultech.ecgmonitor.contact

import com.seoultech.ecgmonitor.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@InstallIn(ApplicationComponent::class)
@Module
class ContactModule {

    @Provides
    fun provideContactLocalDataSource(appDatabase: AppDatabase) : ContactDataSource =
        ContactLocalDataSource(appDatabase.contactDao())
}