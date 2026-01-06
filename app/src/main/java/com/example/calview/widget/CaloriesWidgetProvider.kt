package com.example.calview.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.room.Room
import com.example.calview.MainActivity
import com.example.calview.R
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.util.Calendar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * AppWidgetProvider for the Calories Widget that displays remaining calories, macros,
 * streak, steps, and water on the user's home screen.
 */
class CaloriesWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is added
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Create RemoteViews FIRST - outside try block so widget always has valid views
        val views = RemoteViews(context.packageName, R.layout.widget_calories)
        
        // Set click intent to open app (always needed)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        scope.launch {
            try {
                // Read goals from DataStore
                val preferences = context.dataStore.data.first()
                
                val goalCalories = preferences[intPreferencesKey("recommended_calories")] ?: 2000
                val goalProtein = preferences[intPreferencesKey("recommended_protein")] ?: 117
                val goalCarbs = preferences[intPreferencesKey("recommended_carbs")] ?: 203
                val goalFats = preferences[intPreferencesKey("recommended_fats")] ?: 47
                
                // Get today's meals from Room database
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calview_db"  // Must match DatabaseModule.kt
                ).fallbackToDestructiveMigration().build()
                
                val (startOfDay, endOfDay) = getTodayTimestamps()
                val todayMeals = database.mealDao().getMealsForDate(startOfDay, endOfDay).first()
                
                // Calculate consumed totals (only from completed analyses)
                val completedMeals = todayMeals.filter { it.analysisStatus == AnalysisStatus.COMPLETED }
                val consumedCalories = completedMeals.sumOf { it.calories }
                val consumedProtein = completedMeals.sumOf { it.protein }
                val consumedCarbs = completedMeals.sumOf { it.carbs }
                val consumedFats = completedMeals.sumOf { it.fats }
                
                // Calculate remaining
                val caloriesRemaining = (goalCalories - consumedCalories).coerceAtLeast(0)
                val proteinRemaining = (goalProtein - consumedProtein).coerceAtLeast(0)
                val carbsRemaining = (goalCarbs - consumedCarbs).coerceAtLeast(0)
                val fatsRemaining = (goalFats - consumedFats).coerceAtLeast(0)
                
                // Calculate progress percentage
                val progressPercent = if (goalCalories > 0) {
                    ((goalCalories - caloriesRemaining) * 100 / goalCalories).coerceIn(0, 100)
                } else 0
                
                // Calculate streak (simple: consecutive days with logged meals)
                val streak = calculateStreak(database)
                
                // Get steps from DataStore (cached by DashboardViewModel)
                val steps = preferences[intPreferencesKey("last_known_steps")] ?: 0
                val stepsGoal = preferences[intPreferencesKey("daily_steps_goal")] ?: 10000
                val stepsPercent = if (stepsGoal > 0) ((steps * 100) / stepsGoal).coerceIn(0, 100) else 0
                
                // Get water from DataStore
                val rawWater = preferences[intPreferencesKey("water_consumed")] ?: 0
                val waterDate = preferences[longPreferencesKey("water_date")] ?: 0L
                
                // Verify water is from today
                val (tStart, tEnd) = getTodayTimestamps()
                val waterConsumed = if (waterDate in tStart..tEnd) rawWater else 0
                
                val waterGoal = 8 // Hardcoded goal for now, matching UI default
                val waterPercent = if (waterGoal > 0) ((waterConsumed * 100) / waterGoal).coerceIn(0, 100) else 0
                
                // Update calorie display
                views.setTextViewText(
                    R.id.widget_calories_remaining,
                    formatNumber(caloriesRemaining)
                )
                views.setTextViewText(
                    R.id.widget_calories_goal,
                    "of ${formatNumber(goalCalories)}"
                )
                views.setTextViewText(
                    R.id.widget_progress_percent,
                    "($progressPercent%)"
                )
                
                // Update macro display
                views.setTextViewText(R.id.widget_protein, "${proteinRemaining}g")
                views.setTextViewText(R.id.widget_carbs, "${carbsRemaining}g")
                views.setTextViewText(R.id.widget_fats, "${fatsRemaining}g")
                
                // Update streak
                views.setTextViewText(R.id.widget_streak, streak.toString())
                
                // Update steps
                if (steps > 0) {
                    views.setTextViewText(R.id.widget_steps, "${formatNumber(steps)} / ${formatNumber(stepsGoal)}")
                    views.setProgressBar(R.id.widget_steps_progress, 100, stepsPercent, false)
                    views.setTextViewText(R.id.widget_steps_percent, "$stepsPercent%")
                } else {
                    views.setTextViewText(R.id.widget_steps, "—")
                    views.setProgressBar(R.id.widget_steps_progress, 100, 0, false)
                    views.setTextViewText(R.id.widget_steps_percent, "—")
                }
                
                // Update water
                if (waterConsumed >= 0) {
                    views.setTextViewText(R.id.widget_water, "$waterConsumed / $waterGoal")
                    views.setProgressBar(R.id.widget_water_progress, 100, waterPercent, false)
                    views.setTextViewText(R.id.widget_water_percent, "$waterPercent%")
                } else {
                    views.setTextViewText(R.id.widget_water, "—")
                    views.setProgressBar(R.id.widget_water_progress, 100, 0, false)
                    views.setTextViewText(R.id.widget_water_percent, "—")
                }
                
                // Close database
                database.close()
                
            } catch (e: Exception) {
                android.util.Log.e("CaloriesWidget", "Error updating widget: ${e.message}", e)
                // Set fallback values on error
                views.setTextViewText(R.id.widget_calories_remaining, "—")
                views.setTextViewText(R.id.widget_calories_goal, "Open app")
                views.setTextViewText(R.id.widget_progress_percent, "")
                views.setTextViewText(R.id.widget_protein, "—")
                views.setTextViewText(R.id.widget_carbs, "—")
                views.setTextViewText(R.id.widget_fats, "—")
                views.setTextViewText(R.id.widget_streak, "0")
                views.setTextViewText(R.id.widget_steps, "—")
                views.setTextViewText(R.id.widget_water, "—")
            }
            
            // Always update widget - even on error we have valid fallback views
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    
    /**
     * Calculate streak: consecutive days with at least one logged meal
     */
    private suspend fun calculateStreak(database: AppDatabase): Int {
        try {
            val calendar = Calendar.getInstance()
            var streak = 0
            
            // Check backwards from today
            for (i in 0 until 365) { // Max 365 days
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis
                
                val mealsOnDay = database.mealDao().getMealsForDate(startOfDay, endOfDay).first()
                
                if (mealsOnDay.isNotEmpty()) {
                    streak++
                    // Move to previous day
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                } else {
                    // Streak broken
                    break
                }
            }
            
            return streak.coerceAtLeast(0)
        } catch (e: Exception) {
            return 0
        }
    }
    
    private fun getTodayTimestamps(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        return Pair(startOfDay, endOfDay)
    }

    private fun formatNumber(number: Int): String {
        return if (number >= 1000) {
            String.format("%,d", number)
        } else {
            number.toString()
        }
    }

    companion object {
        /**
         * Request an update for all widget instances.
         * Call this when meal data changes.
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, CaloriesWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
