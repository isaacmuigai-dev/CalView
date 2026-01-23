package com.example.calview.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
 * AppWidgetProvider for the CalViewAI Widget - Redesigned with modern styling
 * 
 * Displays:
 * - Nested progress rings (Calories outer, Steps inner)
 * - Compact macro progress bars (Protein, Carbs, Fats)
 * - Activity stats (Steps, Burn, 7d Burn, Record)
 * - Streak counter
 * - Quick action buttons (Scan Food, Barcode)
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
        // Use goAsync to keep the BroadcastReceiver alive during async work
        val pendingResult = goAsync()
        
        scope.launch {
            try {
                // Update each widget instance
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } finally {
                // Determine completion
                pendingResult.finish()
            }
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

    private suspend fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Wrap EVERYTHING in try-catch to prevent "problem loading widget"
        try {
            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_calories)
            
            // Read theme preference (defaults to light)
            val prefs = getWidgetPrefs(context)
            val isDarkTheme = prefs.getBoolean("widget_dark_theme", false)
            
            // Apply theme-appropriate colors
            val textColorPrimary = if (isDarkTheme) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            val textColorSecondary = if (isDarkTheme) 0xB3FFFFFF.toInt() else 0xB3000000.toInt()
            val textColorTertiary = if (isDarkTheme) 0x99FFFFFF.toInt() else 0x99000000.toInt()
            
            // Set background based on theme
            if (isDarkTheme) {
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background_gradient_dark)
            } else {
                views.setInt(R.id.widget_container, "setBackgroundResource", R.drawable.widget_background_gradient)
            }
            
            // Set click intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Setup Calendar Display
            val calendar = Calendar.getInstance()
            val dayName = if (isToday(calendar)) "TODAY" else String.format("%tA", calendar).uppercase()
            val fullDate = String.format("%tb %te", calendar, calendar)
            
            views.setTextViewText(R.id.widget_day_name, dayName)
            views.setTextViewText(R.id.widget_full_date, fullDate)
            
            // Apply theme colors to header texts
            views.setTextColor(R.id.widget_day_name, textColorSecondary)
            views.setTextColor(R.id.widget_full_date, textColorPrimary)

            // Set Initial State
            views.setTextViewText(R.id.widget_calories_remaining, "—")
            views.setProgressBar(R.id.widget_calorie_ring, 100, 0, false)
            views.setProgressBar(R.id.widget_steps_ring, 100, 0, false)
            
            // Macros init
            views.setTextViewText(R.id.widget_protein, "0g")
            views.setTextViewText(R.id.widget_carbs, "0g")
            views.setTextViewText(R.id.widget_fats, "0g")
            views.setProgressBar(R.id.widget_protein_progress, 100, 0, false)
            views.setProgressBar(R.id.widget_carbs_progress, 100, 0, false)
            views.setProgressBar(R.id.widget_fats_progress, 100, 0, false)
            
            // Stats init
            views.setTextViewText(R.id.widget_steps, "--")
            views.setTextViewText(R.id.widget_burn, "--")
            views.setTextViewText(R.id.widget_weekly_burn, "--")
            views.setTextViewText(R.id.widget_record, "--")
            views.setTextViewText(R.id.widget_streak_header, "0")
            views.setViewVisibility(R.id.widget_rollover_text_container, android.view.View.GONE)

            // BMI Init
            views.setTextViewText(R.id.widget_weight_value, "-- kg")
            views.setTextViewText(R.id.widget_height_value, "-- cm")
            views.setTextViewText(R.id.widget_bmi_category_text, "Unknown")
            views.setTextViewText(R.id.widget_bmi_title, "BMI --")

            // Draw Gradient Bitmap for BMI Bar
            val gradientBitmap = createGradientBitmap(context)
            views.setImageViewBitmap(R.id.widget_bmi_bar, gradientBitmap)

            // Update with initial loading state first (optional, but good for responsiveness)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            
            // --- Async Data Fetching ---
            try {
                val prefs = getWidgetPrefs(context)
                
                // 1. Prefs Data (Safe)
                var goalCalories = 2000
                var goalProtein = 117
                var goalCarbs = 203
                var goalFats = 47
                var caloriesBurned = 0
                var steps = 0
                var stepsGoal = 10000
                var dailyStepsGoal = 10000
                var weight = 0f
                var height = 0
                var rolloverEnabled = false
                var rolloverAmount = 0
                var addCaloriesBack = false
                
                try {
                    goalCalories = prefs.getInt("recommended_calories", 2000)
                    goalProtein = prefs.getInt("recommended_protein", 117)
                    goalCarbs = prefs.getInt("recommended_carbs", 203)
                    goalFats = prefs.getInt("recommended_fats", 47)
                    
                    weight = prefs.getFloat("weight", 0f)
                    height = prefs.getInt("height", 0)
                    
                    rolloverEnabled = prefs.getBoolean("rollover_enabled", false)
                    rolloverAmount = prefs.getInt("rollover_amount", 0)
                    addCaloriesBack = prefs.getBoolean("add_calories_back", false)
                    
                    caloriesBurned = prefs.getInt("calories_burned", 0)
                    val weeklyBurn = prefs.getInt("weekly_burn", 0)
                    val recordBurn = prefs.getInt("record_burn", 0)
                    
                    steps = prefs.getInt("last_known_steps", 0)
                    stepsGoal = prefs.getInt("daily_steps_goal", 10000)
                    dailyStepsGoal = stepsGoal

                    // UI Updates from Prefs
                    views.setTextViewText(R.id.widget_burn, formatCompactNumber(caloriesBurned))
                    views.setTextViewText(R.id.widget_weekly_burn, formatCompactNumber(weeklyBurn))
                    views.setTextViewText(R.id.widget_record, formatCompactNumber(recordBurn))
                    
                    // BMI Display safely
                    updateBmiDisplay(context, views, weight, height)
                    
                    // Rollover Display
                    // Rollover Display (Center Indicator)
                    if (rolloverEnabled && rolloverAmount > 0) {
                        views.setViewVisibility(R.id.widget_indicator_rollover, android.view.View.VISIBLE)
                        views.setTextViewText(R.id.widget_indicator_rollover, "+$rolloverAmount")
                    } else {
                        views.setViewVisibility(R.id.widget_indicator_rollover, android.view.View.GONE)
                    }
                    
                    // Active Calories Display (Center Indicator)
                    if (addCaloriesBack && caloriesBurned > 0) {
                        views.setViewVisibility(R.id.widget_indicator_active, android.view.View.VISIBLE)
                        views.setTextViewText(R.id.widget_indicator_active, "+$caloriesBurned")
                    } else {
                        views.setViewVisibility(R.id.widget_indicator_active, android.view.View.GONE)
                    }
                    
                    // Hide legacy bottom container (if present in XML)
                    views.setViewVisibility(R.id.widget_rollover_text_container, android.view.View.GONE)

                } catch (e: Exception) {
                    android.util.Log.e("CaloriesWidget", "Error reading prefs", e)
                    views.setTextViewText(R.id.widget_calories_remaining, "PrefErr")
                }

                // 2. Database access for meals (Risky)
                var consumedCalories = 0
                var consumedProtein = 0
                var consumedCarbs = 0
                var consumedFats = 0
                var streak = 0
                
                try {
                    val database = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "calview_db"
                    ).fallbackToDestructiveMigration().build()
                    
                    try {
                        val (startOfDay, endOfDay) = getTodayTimestamps()
                        // Use firstOrNull to be safe if flow is empty (though Flow usually emits empty list)
                        val todayMeals = database.mealDao().getMealsForDate(startOfDay, endOfDay).first()
                        
                        // Consumed
                        val completedMeals = todayMeals.filter { it.analysisStatus == AnalysisStatus.COMPLETED }
                        consumedCalories = completedMeals.sumOf { it.calories }
                        consumedProtein = completedMeals.sumOf { it.protein }
                        consumedCarbs = completedMeals.sumOf { it.carbs }
                        consumedFats = completedMeals.sumOf { it.fats }
                        
                        // Streak
                        streak = calculateStreak(database)
                        views.setTextViewText(R.id.widget_streak_header, streak.toString())
                        
                    } catch (e: Exception) {
                        android.util.Log.e("CaloriesWidget", "Error reading DB", e)
                         // Debug indicator
                        views.setTextViewText(R.id.widget_streak_header, "ERR")
                    } finally {
                        database.close()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CaloriesWidget", "Error creating DB", e)
                    views.setTextViewText(R.id.widget_streak_header, "DB")
                }
                
                // 3. Final Calculations & UI Update
                try {
                    val effectiveRollover = if (rolloverEnabled) rolloverAmount else 0
                    val effectiveBurned = if (addCaloriesBack) caloriesBurned else 0
                    val effectiveGoal = goalCalories + effectiveRollover + effectiveBurned
                    val remaining = (effectiveGoal - consumedCalories).coerceAtLeast(0)
                    
                    // Progress
                    val calProgress = if (effectiveGoal > 0) ((consumedCalories * 100) / effectiveGoal).coerceIn(0, 100) else 0
                    val proProgress = if (goalProtein > 0) ((consumedProtein * 100) / goalProtein).coerceIn(0, 100) else 0
                    val carProgress = if (goalCarbs > 0) ((consumedCarbs * 100) / goalCarbs).coerceIn(0, 100) else 0
                    val fatProgress = if (goalFats > 0) ((consumedFats * 100) / goalFats).coerceIn(0, 100) else 0
                    
                    // Steps Progress
                    val stepsProgress = if (stepsGoal > 0) ((steps * 100) / stepsGoal).coerceIn(0, 100) else 0
                    
                    // UI Updates
                    views.setTextViewText(R.id.widget_calories_remaining, formatNumber(remaining))
                    views.setProgressBar(R.id.widget_calorie_ring, 100, calProgress, false)
                    
                    views.setProgressBar(R.id.widget_steps_ring, 100, stepsProgress, false)
                    views.setTextViewText(R.id.widget_steps, formatCompactNumber(steps))
                    
                    // Show REMAINING macros (Goal - Consumed)
                    val remainingProtein = (goalProtein - consumedProtein).coerceAtLeast(0)
                    val remainingCarbs = (goalCarbs - consumedCarbs).coerceAtLeast(0)
                    val remainingFats = (goalFats - consumedFats).coerceAtLeast(0)
                    
                    views.setTextViewText(R.id.widget_protein, "${remainingProtein}g")
                    views.setProgressBar(R.id.widget_protein_progress, 100, proProgress, false)
                    
                    views.setTextViewText(R.id.widget_carbs, "${remainingCarbs}g")
                    views.setProgressBar(R.id.widget_carbs_progress, 100, carProgress, false)
                    
                    views.setTextViewText(R.id.widget_fats, "${remainingFats}g")
                    views.setProgressBar(R.id.widget_fats_progress, 100, fatProgress, false)
                
                } catch (e: Exception) {
                     android.util.Log.e("CaloriesWidget", "Error calculating UI", e)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                android.util.Log.e("CaloriesWidget", "Error updating widget: ${e.message}", e)
            }
        } catch (t: Throwable) {
            android.util.Log.e("CaloriesWidget", "Critical widget error: ${t.message}", t)
        }
    }
    
    private fun updateBmiDisplay(context: Context, views: RemoteViews, weight: Float, height: Int) {
        val bmi = if (weight > 0 && height > 0) {
            val heightM = height / 100f
            weight / (heightM * heightM)
        } else 0f
        
        if (bmi > 0) {
            val bmiFormatted = String.format("%.1f", bmi)
            // Update Footer Title
            views.setTextViewText(R.id.widget_bmi_title, "BMI $bmiFormatted")
            // Update Header Value (Next to streak)
            views.setTextViewText(R.id.widget_bmi_value, bmiFormatted)
            
            views.setTextViewText(R.id.widget_weight_value, "${weight.toInt()} kg")
            views.setTextViewText(R.id.widget_height_value, "$height cm")
            
            // Category
            val (categoryText, categoryColor) = getBmiCategory(bmi)
            views.setTextViewText(R.id.widget_bmi_category_text, categoryText)
            views.setTextColor(R.id.widget_bmi_category_text, categoryColor)
            
            // Marker Position
            val compositeBitmap = createCompositeBmiBitmap(context, bmi)
            views.setImageViewBitmap(R.id.widget_bmi_bar, compositeBitmap)
            views.setViewVisibility(R.id.widget_bmi_marker, android.view.View.GONE)
            
        } else {
            // Empty state
            views.setTextViewText(R.id.widget_bmi_title, "BMI --")
            views.setTextViewText(R.id.widget_bmi_value, "--") // Header
            views.setTextViewText(R.id.widget_weight_value, "-- kg")
            views.setTextViewText(R.id.widget_height_value, "-- cm")
            views.setTextViewText(R.id.widget_bmi_category_text, "Start logging to see BMI")
            views.setTextColor(R.id.widget_bmi_category_text, android.graphics.Color.GRAY)
            views.setViewVisibility(R.id.widget_bmi_marker, android.view.View.GONE)
        }
    }
    

    
    // --- Helper for BMI Gradient ---
    private fun createGradientBitmap(context: Context): android.graphics.Bitmap {
        val width = 300 // Fixed internal width, scale later or good enough
        val height = 24 // Height of bar in px
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Colors
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#3B82F6"), // Underweight
            android.graphics.Color.parseColor("#10B981"), // Healthy
            android.graphics.Color.parseColor("#F59E0B"), // Overweight
            android.graphics.Color.parseColor("#EF4444")  // Obese
        )
        // Positions (approximate based on BMI ranges: <18.5, 18.5-25, 25-30, >30)
        // Range 15 to 40 = 25 units.
        // 18.5 is (3.5/25) = 14%
        // 25 is (10/25) = 40%
        // 30 is (15/25) = 60%
        val positions = floatArrayOf(0f, 0.4f, 0.6f, 1f) // Start, Healthy End/Over Start, Over End/Obese Start, End
        // Distributed linear gradient for smooth transition
        
        val paint = android.graphics.Paint()
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            colors, null, // Auto distribute for now, or refine
            android.graphics.Shader.TileMode.CLAMP
        )
        // Draw rounded rect
        val rect = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
        val cornerRadius = 12f // rounded
        paint.isAntiAlias = true
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        
        return bitmap
    }

    // Composite Bitmap: Gradient Bar + Marker at correct position
    private fun createCompositeBmiBitmap(context: Context, bmi: Float): android.graphics.Bitmap {
        val width = 600 // Higher res for smoothness
        val height = 40 // Taller to accommodate marker
        val barHeight = 20
        val barTop = (height - barHeight) / 2f
        
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // 1. Draw Gradient Bar
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#3B82F6"), // Blue (Under)
            android.graphics.Color.parseColor("#10B981"), // Green (Healthy)
            android.graphics.Color.parseColor("#F59E0B"), // Orange (Over)
            android.graphics.Color.parseColor("#EF4444")  // Red (Obese)
        )
        // Adjusted distribution to match 15->40 scale roughly
        // 0.0 -> Blue
        // 0.33 -> Green
        // 0.66 -> Orange
        // 1.0 -> Red
        
        val paint = android.graphics.Paint()
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            colors, null, 
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.isAntiAlias = true
        
        val cornerRadius = barHeight / 2f
        val barRect = android.graphics.RectF(0f, barTop, width.toFloat(), barTop + barHeight)
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, paint)
        
        // 2. Calculate Marker Position
        // Range 15 to 40
        val clampedBmi = bmi.coerceIn(15f, 40f)
        val progress = (clampedBmi - 15f) / 25f // 0 to 1
        
        // Marker X center
        val markerX = progress * width
        
        // 3. Draw Marker (White circle with colored border)
        val markerRadius = 14f // Slightly larger than bar height/2
        val markerPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }
        // White fill
        canvas.drawCircle(markerX, height / 2f, markerRadius, markerPaint)
        
        // Colored border (Tint based on category color)
        val (_, categoryColor) = getBmiCategory(bmi)
        markerPaint.style = android.graphics.Paint.Style.STROKE
        markerPaint.strokeWidth = 4f
        markerPaint.color = categoryColor
        canvas.drawCircle(markerX, height / 2f, markerRadius - 2f, markerPaint)
        
        return bitmap
    }
    
    private fun getBmiCategory(bmi: Float): Pair<String, Int> {
        return when {
            bmi < 18.5f -> "Underweight" to android.graphics.Color.parseColor("#3B82F6")
            bmi < 25f -> "Healthy Weight" to android.graphics.Color.parseColor("#10B981")
            bmi < 30f -> "Overweight" to android.graphics.Color.parseColor("#F59E0B")
            else -> "Obese" to android.graphics.Color.parseColor("#EF4444")
        }
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
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
    
    private fun formatCompactNumber(number: Int): String {
        return when {
            number >= 10000 -> String.format("%.0fk", number / 1000.0)
            number >= 1000 -> String.format("%.1fk", number / 1000.0)
            else -> number.toString()
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
                
                // We must collect flow to get list, first() gets the current emission
                val mealsOnDay = database.mealDao().getMealsForDate(startOfDay, endOfDay).first()
                
                // Count any meal as activity (or check if calories > 0)
                if (mealsOnDay.isNotEmpty()) {
                    streak++
                    // Move to previous day
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                } else {
                    // Streak broken (unless it's today and we haven't logged yet? 
                    // Usually streak allows missing current day until end of day, but simpler logic breaks immediately)
                    // For now, simple break if empty.
                    
                    // Allow skipping "today" if empty and checking yesterday? 
                    // If i==0 (today) and empty, don't break, just continue to yesterday? 
                    // No, standard streak usually implies *current* streak. If today is empty, streak might be 0 or previous.
                    // Let's stick to simple strict consecutive logic for now.
                    if (i == 0 && mealsOnDay.isEmpty()) {
                        // If today is empty, check yesterday without breaking immediately contextually?
                        // But strictly, streak is consecutive days.
                        // Let's just break for consistency with previous behavior.
                    }
                    break
                }
            }
            
            return streak.coerceAtLeast(0)
        } catch (e: Exception) {
            return 0
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
