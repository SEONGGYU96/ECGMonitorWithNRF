package com.seoultech.ecgmonitor.protocol

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class AbnormalProtocolModule {

    @Provides
    fun provideAbnormalProtocol(@ApplicationContext context: Context) : AbnormalProtocol =
        AbnormalProtocolImpl(context)
}