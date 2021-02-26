package com.seoultech.ecgmonitor.service

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class ServiceModule {

    @Provides
    fun provideNotificationGenerator(@ApplicationContext context: Context): NotificationGenerator = ECGNotification(context)
}