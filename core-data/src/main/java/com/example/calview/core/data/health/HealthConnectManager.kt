package com.example.calview.core.data.health

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
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
import javax.inject.Inject
import javax.inject.Singleton

data class HealthData(
    val steps: Long = 0,
    val caloriesBurned: Double = 0.0,
    val exerciseMinutes: Long = 0,
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
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )
    
    /**
     * Check if Health Connect is available on this device
     */
    fun isAvailable(): Boolean {
        // Health Connect requires API 26+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return false
        }
        
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)
        return availabilityStatus == HealthConnectClient.SDK_AVAILABLE
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
            val today = LocalDate.now()
            val startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            
            val timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            
            // Read steps
            val stepsResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )
            val totalSteps = stepsResponse.records.sumOf { it.count }
            
            // Read calories
            val caloriesResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )
            val totalCalories = caloriesResponse.records.sumOf { it.energy.inKilocalories }
            
            // Read exercise
            val exerciseResponse = client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = timeRangeFilter
                )
            )
            val exerciseMinutes = exerciseResponse.records.sumOf { record ->
                java.time.Duration.between(record.startTime, record.endTime).toMinutes()
            }
            
            _healthData.value = HealthData(
                steps = totalSteps,
                caloriesBurned = totalCalories,
                exerciseMinutes = exerciseMinutes,
                isConnected = true,
                isAvailable = true
            )
        } catch (e: Exception) {
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
