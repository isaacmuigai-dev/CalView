package com.example.calview.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.calview.MainActivity
import com.example.calview.R
import com.example.calview.core.data.billing.BillingManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * WorkManager worker for badge and achievement notifications.
 * Checks for new badges earned or milestones reached.
 * Premium feature only.
 */
@HiltWorker
class BadgeNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billingManager: BillingManager
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "badge_notification_work"
        const val CHANNEL_ID = "badge_notifications"
        const val NOTIFICATION_ID_BASE = 6000
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        // Similar to SocialChallengeNotificationWorker, this would check for new badges
        // For now, we'll implement the infrastructure to show the notification
        
        return Result.success()
    }
    
    fun showNotification(badgeName: String, description: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                this.description = "Updates about earned badges and achievements"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "profile") // Assuming badges are in profile
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use appropriate badge icon
            .setContentTitle("New Badge Earned!")
            .setContentText("You earned the $badgeName badge!")
            .setStyle(NotificationCompat.BigTextStyle().bigText("You earned the $badgeName badge! $description"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + Random.nextInt(1000), notification)
    }
}
