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
import com.example.calview.core.data.local.DailyLogDao
import com.example.calview.core.data.repository.WaterReminderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * WorkManager worker for water reminders.
 * Sends periodic hydration reminders during active hours.
 * Premium feature only.
 */
@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val billingManager: BillingManager,
    private val waterReminderRepo: WaterReminderRepository,
    private val dailyLogDao: DailyLogDao
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "water_reminder_work"
        const val CHANNEL_ID = "water_reminders"
        const val NOTIFICATION_ID = 3001
        
        // Action for quick water logging
        const val ACTION_LOG_WATER = "com.example.calview.ACTION_LOG_WATER"
        const val EXTRA_AMOUNT_ML = "amount_ml"
    }
    
    override suspend fun doWork(): Result {
        // Premium check
        if (!billingManager.isPremium.first()) {
            return Result.success() // Silently skip for non-premium
        }
        
        // Check if reminders are enabled
        val settings = waterReminderRepo.getSettings()
        if (!settings.enabled) {
            return Result.success()
        }
        
        // Check if within active hours
        val currentHour = LocalTime.now().hour
        if (currentHour < settings.startHour || currentHour >= settings.endHour) {
            return Result.success() // Outside active hours
        }
        
        // Get today's water intake
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val log = dailyLogDao.getLogForDateSync(today)
        val currentIntake = log?.waterIntake ?: 0
        val goal = settings.dailyGoalMl
        val remaining = goal - currentIntake
        
        // Generate and show notification
        val message = generateMessage(currentIntake, goal, remaining)
        showNotification(message, remaining > 0)
        
        return Result.success()
    }
    
    private fun generateMessage(current: Int, goal: Int, remaining: Int): String {
        val percent = ((current.toFloat() / goal) * 100).toInt()
        
        return when {
            remaining <= 0 -> "🎉 You've hit your water goal! ${current}ml of ${goal}ml. Amazing!"
            percent >= 75 -> "💧 Almost there! ${current}ml / ${goal}ml. Just ${remaining}ml to go!"
            percent >= 50 -> "💦 Halfway! You're at ${current}ml. Keep sipping!"
            percent >= 25 -> "🚰 Time to hydrate! ${current}ml logged. ${remaining}ml remaining."
            else -> "💧 Stay hydrated! Tap to log 250ml of water."
        }
    }
    
    private fun showNotification(message: String, showAction: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Hydration reminders throughout the day"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent to open app
        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "water")
        }
        val openPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("💧 Water Reminder")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
        
        // Add quick action button to log 250ml
        if (showAction) {
            val logWaterIntent = Intent(applicationContext, WaterLogReceiver::class.java).apply {
                action = ACTION_LOG_WATER
                putExtra(EXTRA_AMOUNT_ML, 250)
            }
            val logWaterPendingIntent = PendingIntent.getBroadcast(
                applicationContext, 1, logWaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            builder.addAction(
                R.drawable.ic_water_drop,
                "+250ml",
                logWaterPendingIntent
            )
        }
        
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
