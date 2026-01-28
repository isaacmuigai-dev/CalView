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
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.StreakFreezeRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Worker that sends evening reminders to protect user's streak.
 * Runs at 8 PM and 9 PM to remind users who haven't logged meals.
 * Available for all users (not premium-gated).
 */
@HiltWorker
class StreakReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val mealRepository: MealRepository,
    private val streakFreezeRepository: StreakFreezeRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "streak_reminder_work"
        const val CHANNEL_ID = "streak_reminders"
        const val NOTIFICATION_ID = 7001
        
        // SharedPrefs key for tracking last notification date
        const val PREF_LAST_STREAK_REMINDER_DATE = "last_streak_reminder_date"
    }
    
    override suspend fun doWork(): Result {
        try {
            // Check if we already sent a reminder today
            val prefs = applicationContext.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
            val lastReminderDate = prefs.getString(PREF_LAST_STREAK_REMINDER_DATE, "")
            val today = LocalDate.now().toString()
            
            if (lastReminderDate == today) {
                return Result.success() // Already reminded today
            }
            
            // Get today's meals
            val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val todayMeals = mealRepository.getMealsForDate(todayFormatted).first()
                .filter { it.analysisStatus == AnalysisStatus.COMPLETED }
            
            // If user has logged meals today, no need for reminder
            if (todayMeals.isNotEmpty()) {
                return Result.success()
            }
            
            // Get current streak info
            val allMeals = mealRepository.getAllMeals().first()
                .filter { it.analysisStatus == AnalysisStatus.COMPLETED }
            val mealDates = allMeals.map { meal ->
                java.time.Instant.ofEpochMilli(meal.timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.distinct()
            
            val currentStreak = streakFreezeRepository.getStreakData(mealDates).first()
            
            // Only remind if user has an active streak to protect
            if (currentStreak >= 2) {
                val userName = userPreferencesRepository.userName.first().ifEmpty { "there" }
                    .split(" ").firstOrNull() ?: "there"
                
                showStreakReminderNotification(userName, currentStreak)
                
                // Mark that we sent a reminder today
                prefs.edit().putString(PREF_LAST_STREAK_REMINDER_DATE, today).apply()
            }
            
        } catch (e: Exception) {
            android.util.Log.e("StreakReminderWorker", "Error checking streak", e)
        }
        
        return Result.success()
    }
    
    private fun showStreakReminderNotification(userName: String, currentStreak: Int) {
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
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🔥 Protect Your $currentStreak-Day Streak!")
            .setContentText("Hey $userName, you haven't logged any meals today!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Hey $userName, your $currentStreak-day streak is at risk! " +
                        "Log a meal before midnight to keep it going. " +
                        "Even a quick photo counts!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Streak Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to maintain your logging streak"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
