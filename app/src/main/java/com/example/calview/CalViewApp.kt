package com.example.calview

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.calview.worker.BadgeNotificationWorker
import com.example.calview.worker.CoachNotificationWorker
import com.example.calview.worker.DailyEngagementWorker
import com.example.calview.worker.DailySyncWorker
import com.example.calview.worker.FastingNotificationWorker
import com.example.calview.worker.InactivityWorker
import com.example.calview.worker.SocialChallengeNotificationWorker
import com.example.calview.worker.StreakReminderWorker
import com.example.calview.worker.WaterReminderWorker
import com.example.calview.worker.WeeklySummaryWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory


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

        // Initialize Firebase App Check
        // Use Debug provider for local builds, Play Integrity for release
        val appCheck = FirebaseAppCheck.getInstance()
        val useDebugProvider = BuildConfig.DEBUG
        Log.d("CalViewApp", "🔐 App Check Init: DEBUG=$useDebugProvider")
        
        if (useDebugProvider) {
            try {
                // Use reflection to load DebugAppCheckProviderFactory as it's only available in debug builds
                val debugProviderClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                val getInstanceMethod = debugProviderClass.getMethod("getInstance")
                val factory = getInstanceMethod.invoke(null) as? com.google.firebase.appcheck.AppCheckProviderFactory
                if (factory != null) {
                    Log.d("CalViewApp", "🛠️ Installing DebugAppCheckProviderFactory")
                    appCheck.installAppCheckProviderFactory(factory)
                }
            } catch (e: Exception) {
                Log.e("CalViewApp", "⚠️ Could not install DebugAppCheckProviderFactory", e)
            }
        } else {
            Log.d("CalViewApp", "🛡️ Installing PlayIntegrityAppCheckProviderFactory")
            appCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }

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
        scheduleFastingNotifications()
        
        // Schedule engagement notifications (available for all users)
        scheduleEngagementNotifications()
        
        // Schedule premium social/gamification workers
        scheduleSocialAndGamificationWorkers()
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
    
    /**
     * Schedule fasting notification worker to run hourly.
     * Premium check happens within the worker itself.
     */
    private fun scheduleFastingNotifications() {
        val fastingRequest = PeriodicWorkRequestBuilder<FastingNotificationWorker>(
            1, TimeUnit.HOURS
        )
            .setInitialDelay(15, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            FastingNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            fastingRequest
        )
        
        Log.d("CalViewApp", "⏱️ Scheduled fasting notification worker")
    }
    
    /**
     * Schedule engagement notification workers that are available for all users.
     * These help maintain user engagement and streak protection.
     */
    private fun scheduleEngagementNotifications() {
        val workManager = WorkManager.getInstance(this)
        
        // 1. Streak Reminder Worker - runs at 8 PM to remind users to log meals
        scheduleWorkerAtTime(
            workManager = workManager,
            workerClass = StreakReminderWorker::class.java,
            targetHour = 20, // 8 PM
            workName = StreakReminderWorker.WORK_NAME
        )
        
        // 2. Daily Engagement Worker - runs at 8 PM for users who haven't logged
        scheduleWorkerAtTime(
            workManager = workManager,
            workerClass = DailyEngagementWorker::class.java,
            targetHour = 20,
            workName = DailyEngagementWorker.WORK_NAME
        )
        
        // 3. Weekly Summary Worker - runs Sunday at 10 AM
        scheduleWeeklySummary(workManager)
        
        // 4. Inactivity Worker - runs daily to check for inactive users
        val inactivityRequest = PeriodicWorkRequestBuilder<InactivityWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(6, TimeUnit.HOURS) // Start 6 hours after install
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            InactivityWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            inactivityRequest
        )
        
        Log.d("CalViewApp", "📢 Scheduled engagement notification workers")
    }
    
    /**
     * Schedule premium social and gamification workers.
     * Premium checks happen within the workers themselves.
     */
    private fun scheduleSocialAndGamificationWorkers() {
        val workManager = WorkManager.getInstance(this)
        
        // 1. Badge Notification Worker - checks daily for new badges
        val badgeRequest = PeriodicWorkRequestBuilder<BadgeNotificationWorker>(
            12, TimeUnit.HOURS // Check twice daily
        )
            .setInitialDelay(2, TimeUnit.HOURS)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            BadgeNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            badgeRequest
        )
        
        // 2. Social Challenge Notification Worker - checks daily for challenge updates
        val socialRequest = PeriodicWorkRequestBuilder<SocialChallengeNotificationWorker>(
            12, TimeUnit.HOURS // Check twice daily
        )
            .setInitialDelay(3, TimeUnit.HOURS)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SocialChallengeNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            socialRequest
        )
        
        Log.d("CalViewApp", "🎮 Scheduled social and gamification workers")
    }
    
    /**
     * Helper to schedule a worker at a specific hour daily.
     */
    private fun <T : CoroutineWorker> scheduleWorkerAtTime(
        workManager: WorkManager,
        workerClass: Class<T>,
        targetHour: Int,
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
        
        val request = PeriodicWorkRequest.Builder(
            workerClass,
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
    
    /**
     * Schedule weekly summary for Sunday mornings.
     */
    private fun scheduleWeeklySummary(workManager: WorkManager) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 10) // 10 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(currentTime)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        
        val summaryRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(
            7, TimeUnit.DAYS // Once a week
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WeeklySummaryWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            summaryRequest
        )
    }
}
