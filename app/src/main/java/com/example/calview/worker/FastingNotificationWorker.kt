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
import com.example.calview.core.data.repository.FastingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random

/**
 * WorkManager worker for fasting notifications.
 * Sends notifications at key milestones during a fast.
 * Premium feature only.
 */
@HiltWorker
class FastingNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billingManager: BillingManager,
    private val fastingRepository: FastingRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "fasting_notification_work"
        const val CHANNEL_ID = "fasting_notifications"
        const val NOTIFICATION_ID = 4001
        
        // Notification types
        const val TYPE_STARTED = "started"
        const val TYPE_MILESTONE_25 = "milestone_25"
        const val TYPE_MILESTONE_50 = "milestone_50"
        const val TYPE_MILESTONE_75 = "milestone_75"
        const val TYPE_ALMOST_DONE = "almost_done"
        const val TYPE_COMPLETED = "completed"
        const val TYPE_ENCOURAGEMENT = "encouragement"
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        val activeFast = fastingRepository.activeFast.first() ?: return Result.success()
        
        val now = System.currentTimeMillis()
        val elapsedMinutes = ((now - activeFast.startTime) / 60000).toInt()
        val targetMinutes = activeFast.targetDurationMinutes
        val progress = (elapsedMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
        val remainingMinutes = (targetMinutes - elapsedMinutes).coerceAtLeast(0)
        val remainingHours = remainingMinutes / 60
        val remainingMins = remainingMinutes % 60
        
        val notificationType = inputData.getString("type") ?: determineNotificationType(progress)
        
        // Prevent duplicate milestones for the same fast
        val prefs = applicationContext.getSharedPreferences("fasting_prefs", Context.MODE_PRIVATE)
        val lastSentMilestone = prefs.getString("last_milestone_${activeFast.id}", "")
        
        val isMilestone = notificationType.startsWith("milestone") || notificationType == TYPE_ALMOST_DONE
        if (isMilestone && notificationType == lastSentMilestone) {
            return Result.success() // Already sent this milestone for this session
        }
        
        val (message, emoji) = generateMessage(notificationType, progress, remainingHours, remainingMins, activeFast.fastingType)
        
        if (message.isNotEmpty()) {
            showNotification("$emoji $message")
            // Record if it was a milestone
            if (isMilestone) {
                prefs.edit().putString("last_milestone_${activeFast.id}", notificationType).apply()
            }
        }
        
        return Result.success()
    }
    
    private fun determineNotificationType(progress: Float): String {
        return when {
            progress >= 0.90f -> TYPE_ALMOST_DONE
            progress >= 0.75f -> TYPE_MILESTONE_75
            progress >= 0.50f -> TYPE_MILESTONE_50
            progress >= 0.25f -> TYPE_MILESTONE_25
            else -> TYPE_ENCOURAGEMENT
        }
    }
    
    private fun generateMessage(type: String, progress: Float, hoursLeft: Int, minsLeft: Int, fastType: String): Pair<String, String> {
        val timeLeft = if (hoursLeft > 0) "${hoursLeft}h ${minsLeft}m" else "${minsLeft}m"
        val percentComplete = (progress * 100).toInt()
        
        return when (type) {
            TYPE_STARTED -> Pair(
                "Your $fastType fast has begun! Stay strong and hydrate well.",
                "🚀"
            )
            TYPE_MILESTONE_25 -> Pair(
                "You're 25% there! $timeLeft remaining. Keep it up!",
                "💪"
            )
            TYPE_MILESTONE_50 -> Pair(
                "Halfway done! $timeLeft to go. You've got this!",
                "🎯"
            )
            TYPE_MILESTONE_75 -> Pair(
                "75% complete! Only $timeLeft left. The finish line is in sight!",
                "🏃"
            )
            TYPE_ALMOST_DONE -> Pair(
                "Almost there! Just $timeLeft remaining. Don't give up now!",
                "⭐"
            )
            TYPE_COMPLETED -> Pair(
                "Congratulations! You completed your $fastType fast! Amazing discipline!",
                "🎉"
            )
            TYPE_ENCOURAGEMENT -> {
                val messages = listOf(
                    Pair("Stay focused! You're $percentComplete% through your fast.", "🧘"),
                    Pair("Drink some water! Hydration helps during fasting.", "💧"),
                    Pair("Your body is working hard. $timeLeft to go!", "🔥"),
                    Pair("You're doing amazing! Keep pushing through!", "💫")
                )
                messages.random()
            }
            else -> Pair("", "")
        }
    }
    
    private fun showNotification(message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fasting Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for your fasting progress"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "fasting")
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fasting Timer")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + Random.nextInt(1000), notification)
    }
}
