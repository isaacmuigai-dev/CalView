package com.example.calview

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.calview.worker.CoachNotificationWorker
import com.example.calview.worker.DailySyncWorker
import com.example.calview.worker.WaterReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Application class for CalView.
 * Initializes Hilt and schedules daily background sync.
 */
@HiltAndroidApp
class CalViewApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory


    override fun onCreate() {
        super.onCreate()

        // Force load BeginSignInRequest to prevent ClassNotFoundException during unmarshalling
        // This ensures the class is available early to all processes (including Firebase Analytics)
        try {
            Class.forName("com.google.android.gms.auth.api.identity.BeginSignInRequest")
            Log.d("CalViewApp", "✅ Successfully pre-loaded BeginSignInRequest")
        } catch (e: Exception) {
            Log.e("CalViewApp", "⚠️ Failed to pre-load BeginSignInRequest", e)
        }

        // Schedule daily sync
        scheduleDailySync()
        
        // Schedule premium feature workers
        scheduleCoachNotifications()
        scheduleWaterReminders()
    }

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        androidx.multidex.MultiDex.install(this)
    }
    
    /**
     * Configure WorkManager to use Hilt for dependency injection
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
    
    /**
     * Schedule the daily sync worker to run once every 24 hours.
     * Uses constraints to only sync when network is available.
     */
    private fun scheduleDailySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val dailySyncRequest = PeriodicWorkRequestBuilder<DailySyncWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // Start after 1 hour of app install
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailySyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            dailySyncRequest
        )
        
        Log.d("CalViewApp", "📅 Scheduled daily sync worker")
    }
    
    /**
     * Schedule coach notification workers for morning, midday, and evening.
     * Premium check happens within the worker itself.
     */
    private fun scheduleCoachNotifications() {
        val workManager = WorkManager.getInstance(this)
        
        // Morning notification (8 AM)
        scheduleCoachNotificationAt(workManager, 8, CoachNotificationWorker.TYPE_MORNING, "coach_morning")
        
        // Midday notification (1 PM)
        scheduleCoachNotificationAt(workManager, 13, CoachNotificationWorker.TYPE_MIDDAY, "coach_midday")
        
        // Evening notification (7 PM)
        scheduleCoachNotificationAt(workManager, 19, CoachNotificationWorker.TYPE_EVENING, "coach_evening")
        
        Log.d("CalViewApp", "💬 Scheduled coach notification workers")
    }
    
    private fun scheduleCoachNotificationAt(
        workManager: WorkManager,
        targetHour: Int,
        notificationType: String,
        workName: String
    ) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val coachRequest = PeriodicWorkRequestBuilder<CoachNotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("type" to notificationType))
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            coachRequest
        )
    }
    
    /**
     * Schedule water reminder worker to run every 2 hours during active hours.
     * Premium and enabled checks happen within the worker itself.
     */
    private fun scheduleWaterReminders() {
        val waterReminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            2, TimeUnit.HOURS
        )
            .setInitialDelay(30, TimeUnit.MINUTES) // Start 30 mins after app open
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WaterReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            waterReminderRequest
        )
        
        Log.d("CalViewApp", "💧 Scheduled water reminder worker")
    }
}
