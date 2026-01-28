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
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Worker that checks for user inactivity and sends re-engagement notifications.
 * Triggers after 3 days of no activity to bring users back.
 * Available for all users.
 */
@HiltWorker
class InactivityWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "inactivity_work"
        const val CHANNEL_ID = "reengagement"
        const val NOTIFICATION_ID = 9001
        
        // Days of inactivity before triggering
        const val INACTIVITY_THRESHOLD_DAYS = 3L
        
        // Max days before we stop bothering them
        const val MAX_INACTIVITY_DAYS = 14L
        
        // SharedPrefs keys
        const val PREF_LAST_INACTIVITY_NOTIFICATION = "last_inactivity_notification"
        const val PREF_INACTIVITY_NOTIFICATION_COUNT = "inactivity_notification_count"
    }
    
    override suspend fun doWork(): Result {
        try {
            val lastActivityTimestamp = userPreferencesRepository.lastActivityTimestamp.first()
            
            // If no activity recorded yet, skip
            if (lastActivityTimestamp == 0L) {
                return Result.success()
            }
            
            val daysSinceActivity = ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(lastActivityTimestamp),
                Instant.now()
            )
            
            // Only notify if inactive for 3-14 days
            if (daysSinceActivity < INACTIVITY_THRESHOLD_DAYS || daysSinceActivity > MAX_INACTIVITY_DAYS) {
                return Result.success()
            }
            
            // Check how many times we've already notified
            val prefs = applicationContext.getSharedPreferences("inactivity_prefs", Context.MODE_PRIVATE)
            val lastNotificationDate = prefs.getLong(PREF_LAST_INACTIVITY_NOTIFICATION, 0L)
            val notificationCount = prefs.getInt(PREF_INACTIVITY_NOTIFICATION_COUNT, 0)
            
            // Only send one notification per inactivity period (every 3 days, max 3 notifications)
            val daysSinceLastNotification = ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(lastNotificationDate),
                Instant.now()
            )
            
            if (daysSinceLastNotification < 3 || notificationCount >= 3) {
                return Result.success()
            }
            
            // Send re-engagement notification
            val userName = userPreferencesRepository.userName.first().ifEmpty { "there" }
                .split(" ").firstOrNull() ?: "there"
            
            showInactivityNotification(userName, daysSinceActivity.toInt(), notificationCount)
            
            // Update notification tracking
            prefs.edit()
                .putLong(PREF_LAST_INACTIVITY_NOTIFICATION, System.currentTimeMillis())
                .putInt(PREF_INACTIVITY_NOTIFICATION_COUNT, notificationCount + 1)
                .apply()
            
        } catch (e: Exception) {
            android.util.Log.e("InactivityWorker", "Error checking inactivity", e)
        }
        
        return Result.success()
    }
    
    private fun showInactivityNotification(userName: String, daysInactive: Int, notificationCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "main?tab=0&showScanMenu=true")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Vary the message based on notification count for freshness
        val (title, message) = when (notificationCount) {
            0 -> Pair(
                "👋 We miss you, $userName!",
                "It's been $daysInactive days since your last meal log. " +
                        "Just 30 seconds to snap a photo and get back on track. " +
                        "Your health journey continues with one small step!"
            )
            1 -> Pair(
                "🍽️ Ready to log your next meal?",
                "Hey $userName, tracking your meals helps you understand your nutrition. " +
                        "Even logging one meal today can restart your healthy habits. " +
                        "We're here when you're ready!"
            )
            else -> Pair(
                "💪 Small steps, big results",
                "$userName, consistency beats perfection. Even if life got busy, " +
                        "you can always start again. One photo, one meal, one step toward " +
                        "your health goals. We believe in you!"
            )
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("It's been $daysInactive days - time to get back on track!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Call this when user logs a meal to reset the inactivity counter
     */
    fun resetInactivityCounter(context: Context) {
        val prefs = context.getSharedPreferences("inactivity_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(PREF_INACTIVITY_NOTIFICATION_COUNT, 0)
            .putLong(PREF_LAST_INACTIVITY_NOTIFICATION, 0L)
            .apply()
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Re-engagement",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to keep tracking your nutrition"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
