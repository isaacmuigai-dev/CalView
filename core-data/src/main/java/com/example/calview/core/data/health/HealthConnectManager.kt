package com.example.calview.core.data.health

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class HealthData(
    val steps: Long = 0,
    val caloriesBurned: Double = 0.0,
    val exerciseMinutes: Long = 0,
    val weeklySteps: Long = 0,
    val weeklyCaloriesBurned: Double = 0.0,
    val maxCaloriesBurnedRecord: Double = 0.0,
    val maxStepsRecord: Long = 0,  // Best daily steps in past 7 days
    val lastSevenDaysCalories: List<Double> = emptyList(), // Daily activity calories for last 7 days
    val isConnected: Boolean = false,
    val isAvailable: Boolean = false
)

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _healthData = MutableStateFlow(HealthData())
    val healthData: StateFlow<HealthData> = _healthData.asStateFlow()
    
    private var healthConnectClient: HealthConnectClient? = null
    
    // Required permissions
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )
    
    /**
     * Check if Health Connect is available on this device
     */
    fun isAvailable(): Boolean {
        // Health Connect requires API 26+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            android.util.Log.d("HealthConnectManager", "SDK too old: ${Build.VERSION.SDK_INT}")
            return false
        }
        
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)
        android.util.Log.d("HealthConnectManager", "SDK Status: ${getSdkStatusName(availabilityStatus)}")
        
        // SDK_AVAILABLE = 3, SDK_UNAVAILABLE = 1, SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED = 2
        return when (availabilityStatus) {
            HealthConnectClient.SDK_AVAILABLE -> true
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                // Still try to connect - sometimes it works even with this status
                android.util.Log.d("HealthConnectManager", "Provider update may be required, but attempting connection")
                true
            }
            else -> false
        }
    }
    
    /**
     * Get human-readable SDK status name
     */
    private fun getSdkStatusName(status: Int): String {
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> "SDK_AVAILABLE"
            HealthConnectClient.SDK_UNAVAILABLE -> "SDK_UNAVAILABLE"
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED"
            else -> "UNKNOWN ($status)"
        }
    }
    
    /**
     * Check if we have all required permissions
     */
    suspend fun hasAllPermissions(): Boolean {
        if (!isAvailable()) return false
        
        val client = getOrCreateClient() ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return permissions.all { it in granted }
    }
    
    /**
     * Create permission request contract
     */
    fun createPermissionRequestContract() = PermissionController.createRequestPermissionResultContract()
    
    /**
     * Read today's health data
     */
    suspend fun readTodayData() {
        readDataForDate(LocalDate.now(ZoneId.systemDefault()))
    }
    
    /**
     * Read health data for a specific date
     */
    suspend fun readDataForDate(date: LocalDate) {
        if (!isAvailable()) {
            _healthData.value = HealthData(isAvailable = false)
            return
        }
        
        val client = getOrCreateClient() ?: return
        
        // Check permissions
        val granted = client.permissionController.getGrantedPermissions()
        val hasPermissions = permissions.all { it in granted }
        
        if (!hasPermissions) {
            _healthData.value = HealthData(isAvailable = true, isConnected = false)
            return
        }
        
        try {
            val zoneId = ZoneId.systemDefault()
            val today = LocalDate.now(zoneId)
            
            // Time ranges for the requested date
            val startOfRequestedDay = date.atStartOfDay(zoneId).toInstant()
            val endOfRequestedDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()
            
            // Weekly calculations relative to requested date
            val startOfWeek = date.minusDays(6).atStartOfDay(zoneId).toInstant()
            val startOfMonth = date.minusDays(29).atStartOfDay(zoneId).toInstant()
            
            // Request full month data to calculate records and weekly aggregates locally
            val monthRangeFilter = TimeRangeFilter.between(startOfMonth, endOfRequestedDay)
            
            // Read steps (30 days)
            val stepsResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = monthRangeFilter
                )
            )
            // Filter locally for specific time windows
            val totalStepsForDate = stepsResponse.records.filter { 
                it.startTime >= startOfRequestedDay && it.startTime < endOfRequestedDay 
            }.sumOf { it.count }
            val totalStepsWeek = stepsResponse.records.filter { it.startTime >= startOfWeek }.sumOf { it.count }
            
            // Read active calories (30 days)
            val activeCaloriesResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = ActiveCaloriesBurnedRecord::class,
                    timeRangeFilter = monthRangeFilter
                )
            )
            android.util.Log.d("HealthConnect", "=== ACTIVE CALORIES DEBUG ===")
            android.util.Log.d("HealthConnect", "Active calories records count: ${activeCaloriesResponse.records.size}")
            val activeCaloriesForDate = activeCaloriesResponse.records.filter { 
                it.startTime >= startOfRequestedDay && it.startTime < endOfRequestedDay 
            }.sumOf { it.energy.inKilocalories }
            val activeCaloriesWeek = activeCaloriesResponse.records.filter { it.startTime >= startOfWeek }.sumOf { it.energy.inKilocalories }
            android.util.Log.d("HealthConnect", "Active calories for date: $activeCaloriesForDate")
            android.util.Log.d("HealthConnect", "Active calories for week: $activeCaloriesWeek")
            
            // Read total calories as fallback (some apps only record total, not active)
            val totalCaloriesResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = monthRangeFilter
                )
            )
            android.util.Log.d("HealthConnect", "=== TOTAL CALORIES DEBUG ===")
            android.util.Log.d("HealthConnect", "Total calories records count: ${totalCaloriesResponse.records.size}")
            val totalCaloriesFromRecord = totalCaloriesResponse.records.filter { 
                it.startTime >= startOfRequestedDay && it.startTime < endOfRequestedDay 
            }.sumOf { it.energy.inKilocalories }
            val totalCaloriesWeekFromRecord = totalCaloriesResponse.records.filter { it.startTime >= startOfWeek }.sumOf { it.energy.inKilocalories }
            android.util.Log.d("HealthConnect", "Total calories for date: $totalCaloriesFromRecord")
            android.util.Log.d("HealthConnect", "Total calories for week: $totalCaloriesWeekFromRecord")
            
            // For daily "burned" display: Use ActiveCaloriesBurned (exercise calories)
            // For weekly stats: Use ActiveCaloriesBurned only (more meaningful for exercise tracking)
            // FALLBACK 1: If Active is 0 but Total exists, estimate activity by subtracting BMR
            // FALLBACK 2: If no calorie data at all, estimate from steps (avg 0.04 cal/step)
            val estimatedDailyBMR = 1500.0 // Average BMR per day
            val caloriesPerStep = 0.04 // Average calories burned per step
            
            // Calculate baseline estimate from steps (floor for calories burned)
            val estimatedFromSteps = totalStepsForDate * caloriesPerStep
            val estimatedFromWeeklySteps = totalStepsWeek * caloriesPerStep
            
            val caloriesBurnedForDate = if (activeCaloriesForDate > 0) {
                // Prefer actual active calories, but ensure it's at least as much as steps would imply
                maxOf(activeCaloriesForDate, estimatedFromSteps)
            } else if (totalCaloriesFromRecord > estimatedDailyBMR) {
                // Fallback 1: Total minus estimated BMR = approximate activity
                val estimatedFromTotal = (totalCaloriesFromRecord - estimatedDailyBMR).coerceAtLeast(0.0)
                maxOf(estimatedFromTotal, estimatedFromSteps)
            } else {
                // Fallback 2: Estimate calories from steps
                estimatedFromSteps
            }
            
            // Calculate Daily Record (Max ACTIVE calories burned in a single day within the last 7 days)
            // We must calculate the best value for EACH day individually using the same fallback logic as today
            val historicalDailyCalories = (0..6).map { daysAgo ->
                val targetDate = date.minusDays(daysAgo.toLong())
                
                // Active Calories for this specific day
                val dailyActive = activeCaloriesResponse.records
                    .filter { it.startTime.atZone(zoneId).toLocalDate() == targetDate }
                    .sumOf { it.energy.inKilocalories }
                    
                // Total Calories for this specific day
                val dailyTotal = totalCaloriesResponse.records
                    .filter { it.startTime.atZone(zoneId).toLocalDate() == targetDate }
                    .sumOf { it.energy.inKilocalories }
                    
                // Steps for this specific day
                val dailySteps = stepsResponse.records
                    .filter { it.startTime.atZone(zoneId).toLocalDate() == targetDate }
                    .sumOf { it.count }
                
                val dailyEstimatedFromSteps = dailySteps * caloriesPerStep
                
                if (dailyActive > 0) {
                     maxOf(dailyActive, dailyEstimatedFromSteps)
                } else if (dailyTotal > estimatedDailyBMR) {
                     val dailyEstimatedFromTotal = (dailyTotal - estimatedDailyBMR).coerceAtLeast(0.0)
                     maxOf(dailyEstimatedFromTotal, dailyEstimatedFromSteps)
                } else {
                     dailyEstimatedFromSteps
                }
            }

            val weeklyCaloriesBurned = historicalDailyCalories.sum()
            android.util.Log.d("HealthConnect", "=== FINAL CALORIES (with fallback) ===")

            android.util.Log.d("HealthConnect", "Activity calories for date: $caloriesBurnedForDate")
            android.util.Log.d("HealthConnect", "Activity calories for week: $weeklyCaloriesBurned")

            
            // Ensure the record includes TODAY's fully calculated value
            val historicalMaxCalories = historicalDailyCalories.maxOrNull() ?: 0.0
            val maxCaloriesRecord = maxOf(historicalMaxCalories, caloriesBurnedForDate)
            
            android.util.Log.d("HealthConnect", "Max calories record (activity only): $maxCaloriesRecord")
            android.util.Log.d("HealthConnect", "Historical days calculated: ${historicalDailyCalories.size}")
            
            // Calculate max steps record (best daily steps in past 7 days)
            val stepsByDay = stepsResponse.records
                .filter { it.startTime >= startOfWeek } // Only last 7 days
                .groupBy { it.startTime.atZone(zoneId).toLocalDate() }
                .mapValues { (_, records) -> records.sumOf { it.count } }
            
            // Ensure record includes TODAY
            val historicalMaxSteps = stepsByDay.values.maxOrNull() ?: 0L
            val maxStepsRecord = maxOf(historicalMaxSteps, totalStepsForDate)
            android.util.Log.d("HealthConnect", "Max steps record (7d): $maxStepsRecord")
            android.util.Log.d("HealthConnect", "Steps by day count: ${stepsByDay.size}")
            
            // Read exercise for the requested date
            val exerciseResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfRequestedDay, endOfRequestedDay)
                )
            )
            val exerciseMinutes = exerciseResponse.records.sumOf { record ->
                java.time.Duration.between(record.startTime, record.endTime).toMinutes()
            }
            android.util.Log.d("HealthConnect", "Exercise records: ${exerciseResponse.records.size}, minutes: $exerciseMinutes")
            
            android.util.Log.d("HealthConnect", "=== STEPS DEBUG ===")
            android.util.Log.d("HealthConnect", "Steps for date: $totalStepsForDate")
            android.util.Log.d("HealthConnect", "Steps for week: $totalStepsWeek")
            
            _healthData.value = HealthData(
                steps = totalStepsForDate,
                caloriesBurned = caloriesBurnedForDate,
                exerciseMinutes = exerciseMinutes,
                weeklySteps = totalStepsWeek,
                weeklyCaloriesBurned = weeklyCaloriesBurned,
                maxCaloriesBurnedRecord = maxCaloriesRecord,
                maxStepsRecord = maxStepsRecord,
                lastSevenDaysCalories = historicalDailyCalories, // Expose daily history
                isConnected = true,
                isAvailable = true
            )
            android.util.Log.d("HealthConnect", "=== HEALTH DATA UPDATED ===")
            android.util.Log.d("HealthConnect", "HealthData: steps=$totalStepsForDate, burned=$caloriesBurnedForDate, weeklyBurned=$weeklyCaloriesBurned, stepsRecord=$maxStepsRecord")
        } catch (e: Exception) {
            e.printStackTrace()
            _healthData.value = HealthData(isAvailable = true, isConnected = false)
        }
    }
    
    /**
     * Refresh data after permissions granted
     */
    suspend fun onPermissionsGranted() {
        readTodayData()
    }
    
    private fun getOrCreateClient(): HealthConnectClient? {
        if (healthConnectClient != null) return healthConnectClient
        
        return try {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
            healthConnectClient
        } catch (e: Exception) {
            null
        }
    }
}
