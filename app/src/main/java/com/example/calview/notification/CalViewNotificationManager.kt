package com.example.calview.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.calview.MainActivity
import com.example.calview.R
import com.example.calview.core.data.notification.NotificationHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalViewNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationHandler {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    NotificationHandler.CHANNEL_SYSTEM,
                    "System Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "General app updates and system messages" },
                NotificationChannel(
                    NotificationHandler.CHANNEL_ENGAGEMENT,
                    "Goal & Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Celebrations for hitting goals and habit reminders" },
                NotificationChannel(
                    NotificationHandler.CHANNEL_SOCIAL,
                    "Social activity",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Updates from your friends and challenges" },
                NotificationChannel(
                    NotificationHandler.CHANNEL_PREMIUM,
                    "Premium features",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Updates regarding your subscription" }
            )
            notificationManager.createNotificationChannels(channels)
        }
    }

    override fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        message: String,
        autoCancel: Boolean,
        navigateTo: String?
    ) {
        val iconRes = R.drawable.ic_launcher_foreground // Fixed default icon

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (navigateTo != null) {
                putExtra("navigate_to", navigateTo)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(autoCancel)

        notificationManager.notify(id, builder.build())
    }
}
