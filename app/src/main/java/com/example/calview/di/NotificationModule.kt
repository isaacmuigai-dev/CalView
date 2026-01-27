package com.example.calview.di

import com.example.calview.core.data.notification.NotificationHandler
import com.example.calview.notification.CalViewNotificationManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationHandler(
        notificationManager: CalViewNotificationManager
    ): NotificationHandler
}
