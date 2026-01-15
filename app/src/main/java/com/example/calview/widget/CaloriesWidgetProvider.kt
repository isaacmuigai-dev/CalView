package com.example.calview.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import java.util.Calendar

/**
 * AppWidgetProvider for the Calories Widget that displays remaining calories, macros,
 * streak, steps, and water on the user's home screen.
 * 
 * NOTE: Uses SharedPreferences instead of DataStore to avoid conflicts with app's DataStore singleton.
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
    
    /**
     * Get SharedPreferences for widget data (synced from DataStore by app)
     */
    private fun getWidgetPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Wrap EVERYTHING in try-catch to prevent "problem loading widget"
        try {
            // Create RemoteViews FIRST - outside inner try block so widget always has valid views
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
            
            // Set click intent for Scan Food button
            val scanFoodIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "scanner")
                putExtra("scan_mode", "scan_food")
            }
            val scanFoodPendingIntent = PendingIntent.getActivity(
                context,
                1, // Different request code
                scanFoodIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_scan_food_button, scanFoodPendingIntent)
            
            // Set click intent for Barcode button
            val barcodeIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "scanner")
                putExtra("scan_mode", "barcode")
            }
            val barcodePendingIntent = PendingIntent.getActivity(
                context,
                2, // Different request code
                barcodeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_barcode_button, barcodePendingIntent)

            // Fix potential "Problem loading widget" due to VectorDrawable compatibility
            // by programmatically converting vectors to bitmaps
            drawableToBitmap(context, R.drawable.ic_scan_food)?.let {
                views.setImageViewBitmap(R.id.widget_scan_icon, it)
            }
            drawableToBitmap(context, R.drawable.ic_barcode)?.let {
                views.setImageViewBitmap(R.id.widget_barcode_icon, it)
            }
        
        // Set initial fallback values IMMEDIATELY to prevent "problem loading widget"
        views.setTextViewText(R.id.widget_calories_remaining, "—")
        views.setTextViewText(R.id.widget_protein, "—")
        views.setTextViewText(R.id.widget_carbs, "—")
        views.setTextViewText(R.id.widget_fats, "—")
        views.setTextViewText(R.id.widget_protein_status, "Loading...")
        views.setTextViewText(R.id.widget_carbs_status, "")
        views.setTextViewText(R.id.widget_fats_status, "")
        
        // Update widget immediately with loading state
        appWidgetManager.updateAppWidget(appWidgetId, views)
        
        scope.launch {
            try {
                // Read goals from SharedPreferences (synced from DataStore by app)
                val prefs = getWidgetPrefs(context)
                
                val goalCalories = prefs.getInt("recommended_calories", 2000)
                val goalProtein = prefs.getInt("recommended_protein", 117)
                val goalCarbs = prefs.getInt("recommended_carbs", 203)
                val goalFats = prefs.getInt("recommended_fats", 47)
                
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
                
                // Get steps from SharedPreferences (synced by app)
                val steps = prefs.getInt("last_known_steps", 0)
                val stepsGoal = prefs.getInt("daily_steps_goal", 10000)
                val stepsPercent = if (stepsGoal > 0) ((steps * 100) / stepsGoal).coerceIn(0, 100) else 0
                
                // Get water from SharedPreferences (synced by app)
                val rawWater = prefs.getInt("water_consumed", 0)
                val waterDate = prefs.getLong("water_date", 0L)
                
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
                
                // Update macro display with over/under status
                val proteinOver = consumedProtein > goalProtein
                val carbsOver = consumedCarbs > goalCarbs
                val fatsOver = consumedFats > goalFats
                
                val proteinDiff = if (proteinOver) consumedProtein - goalProtein else goalProtein - consumedProtein
                val carbsDiff = if (carbsOver) consumedCarbs - goalCarbs else goalCarbs - consumedCarbs
                val fatsDiff = if (fatsOver) consumedFats - goalFats else goalFats - consumedFats
                
                views.setTextViewText(R.id.widget_protein, "${proteinDiff}g")
                views.setTextViewText(R.id.widget_protein_status, "Protein ${if (proteinOver) "over" else "left"}")
                
                views.setTextViewText(R.id.widget_carbs, "${carbsDiff}g")
                views.setTextViewText(R.id.widget_carbs_status, "Carbs ${if (carbsOver) "over" else "left"}")
                
                views.setTextViewText(R.id.widget_fats, "${fatsDiff}g")
                views.setTextViewText(R.id.widget_fats_status, "Fats ${if (fatsOver) "over" else "left"}")
                
                // Close database
                database.close()
                
                // Update widget with actual data
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                android.util.Log.e("CaloriesWidget", "Error updating widget: ${e.message}", e)
                // Leave fallback values since we already set them
            }
        }
        } catch (t: Throwable) {
            // Outermost catch - prevent ANY crash from causing "problem loading widget"
            android.util.Log.e("CaloriesWidget", "Critical widget error: ${t.message}", t)
            try {
                // Create minimal fallback views
                val fallbackViews = RemoteViews(context.packageName, R.layout.widget_calories)
                fallbackViews.setTextViewText(R.id.widget_calories_remaining, "Tap to open")
                appWidgetManager.updateAppWidget(appWidgetId, fallbackViews)
            } catch (e2: Throwable) {
                android.util.Log.e("CaloriesWidget", "Failed to create fallback views", e2)
            }
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

    /**
     * Convert vector drawable to bitmap for RemoteViews compatibility
     */
    /**
     * Convert vector drawable to bitmap for RemoteViews compatibility.
     * Limits size to avoid TransactionTooLargeException.
     */
    private fun drawableToBitmap(context: Context, drawableId: Int): android.graphics.Bitmap? {
        try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableId) ?: return null
            
            // Limit max size to ~48dp (approx 144px at xxhdpi) to be safe for widget IPC
            val maxSizePx = 144
            
            var width = drawable.intrinsicWidth
            var height = drawable.intrinsicHeight
            
            if (width <= 0 || height <= 0) {
                width = maxSizePx
                height = maxSizePx
            } else if (width > maxSizePx || height > maxSizePx) {
                val ratio = width.toFloat() / height.toFloat()
                if (width > height) {
                    width = maxSizePx
                    height = (maxSizePx / ratio).toInt()
                } else {
                    height = maxSizePx
                    width = (maxSizePx * ratio).toInt()
                }
            }
            
            val bitmap = android.graphics.Bitmap.createBitmap(
                width,
                height,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: Exception) {
            android.util.Log.e("CaloriesWidget", "Error converting drawable to bitmap", e)
            return null
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
