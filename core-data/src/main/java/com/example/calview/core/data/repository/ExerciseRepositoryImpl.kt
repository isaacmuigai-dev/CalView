package com.example.calview.core.data.repository

import com.example.calview.core.data.local.ExerciseDao
import com.example.calview.core.data.local.ExerciseEntity
import com.example.calview.core.data.local.ExerciseType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val dailyLogRepository: DailyLogRepository
) : ExerciseRepository {
    
    // Scope for background sync operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun getAllExercises(): Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    override fun getExercisesForToday(): Flow<List<ExerciseEntity>> {
        val (startOfDay, endOfDay) = getTodayTimestamps()
        return exerciseDao.getExercisesForDate(startOfDay, endOfDay)
    }
    
    override fun getExercisesForDate(dateString: String): Flow<List<ExerciseEntity>> {
        val (startOfDay, endOfDay) = getDateTimestamps(dateString)
        return exerciseDao.getExercisesForDate(startOfDay, endOfDay)
    }
    
    override fun getTotalCaloriesBurnedForDate(dateString: String): Flow<Int> {
        val (startOfDay, endOfDay) = getDateTimestamps(dateString)
        return exerciseDao.getTotalCaloriesBurnedForDate(startOfDay, endOfDay)
    }

    override fun getLastSevenDaysCalories(): Flow<List<Double>> {
        val today = Calendar.getInstance()
        val flows = (0..6).map { daysAgo ->
            val date = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }
            val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(date.time)
            getTotalCaloriesBurnedForDate(dateString).map { it.toDouble() }
        }
        return combine(flows) { it.toList() }
    }
    
    override fun getRecentExercises(limit: Int): Flow<List<ExerciseEntity>> {
        return exerciseDao.getRecentExercises(limit)
    }
    
    override fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByType(type)
    }
    
    override suspend fun getExerciseById(id: Long): ExerciseEntity? {
        return exerciseDao.getExerciseById(id)
    }

    override suspend fun logExercise(exercise: ExerciseEntity): Long {
        val id = exerciseDao.insertExercise(exercise)
        
        // Sync to Firestore if user is signed in
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.saveExercise(userId, exercise)
                } catch (e: Exception) {
                    android.util.Log.e("ExerciseRepository", "Error syncing exercise to Firestore", e)
                }
            }
        }
        
        // Update DailyLog with burned calories
        scope.launch {
            syncCaloriesBurnedForDate(
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(exercise.timestamp)
            )
        }
        
        return id
    }
    
    override suspend fun updateExercise(exercise: ExerciseEntity) {
        exerciseDao.updateExercise(exercise)
        
        // Sync to Firestore
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.saveExercise(userId, exercise)
                } catch (e: Exception) {
                    android.util.Log.e("ExerciseRepository", "Error syncing exercise update", e)
                }
            }
        }

        // Update DailyLog
        scope.launch {
            syncCaloriesBurnedForDate(
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(exercise.timestamp)
            )
        }
    }

    override suspend fun deleteExercise(exercise: ExerciseEntity) {
        exerciseDao.deleteExercise(exercise)
        
        // Sync to Firestore
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.deleteExercise(userId, exercise.firestoreId)
                } catch (e: Exception) {
                    android.util.Log.e("ExerciseRepository", "Error deleting exercise from Firestore", e)
                }
            }
        }

        // Update DailyLog
        scope.launch {
            syncCaloriesBurnedForDate(
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(exercise.timestamp)
            )
        }
    }
    
    override suspend fun deleteExerciseById(id: Long) {
        exerciseDao.getExerciseById(id)?.let { exercise ->
            deleteExercise(exercise)
        }
    }
    
    override suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val cloudExercises = firestoreRepository.getExercises(userId)
            if (cloudExercises.isNotEmpty()) {
                cloudExercises.forEach { exercise ->
                    // Check if exercise already exists locally
                    val existing = exerciseDao.getExerciseByFirestoreId(exercise.firestoreId)
                    if (existing == null) {
                        exerciseDao.insertExercise(exercise)
                    }
                }
                android.util.Log.d("ExerciseRestore", "Restored ${cloudExercises.size} exercises from cloud")
                true
            } else {
                android.util.Log.d("ExerciseRestore", "No exercises found in cloud")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ExerciseRestore", "Error restoring exercises", e)
            false
        }
    }
    
    override suspend fun clearAllExercises() {
        try {
            exerciseDao.deleteAllExercises()
            android.util.Log.d("ExerciseRepository", "Cleared all local exercises")
        } catch (e: Exception) {
            android.util.Log.e("ExerciseRepository", "Error clearing exercises", e)
        }
    }
    
    override suspend fun hasAnyExercises(): Boolean {
        return try {
            val exercises = getAllExercises().firstOrNull() ?: emptyList()
            exercises.isNotEmpty()
        } catch (e: Exception) {
            android.util.Log.e("ExerciseRepository", "Error checking hasAnyExercises", e)
            false
        }
    }
    
    override fun calculateCaloriesBurned(metValue: Double, weightKg: Double, durationMinutes: Int): Int {
        // MET formula: Calories = MET × weight(kg) × duration(hours)
        return (metValue * weightKg * (durationMinutes / 60.0)).toInt()
    }

    private suspend fun syncCaloriesBurnedForDate(dateString: String) {
        try {
            val (startOfDay, endOfDay) = getDateTimestamps(dateString)
            val totalBurned = exerciseDao.getTotalCaloriesBurnedForDateSync(startOfDay, endOfDay)
            
            val log = dailyLogRepository.getLogForDateSync(dateString) 
                ?: com.example.calview.core.data.local.DailyLogEntity(date = dateString)
            
            dailyLogRepository.saveLog(log.copy(
                caloriesBurned = totalBurned
            ))
        } catch (e: Exception) {
            android.util.Log.e("ExerciseRepository", "Error syncing calories burned for $dateString", e)
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
    
    private fun getDateTimestamps(dateString: String): Pair<Long, Long> {
        val parts = dateString.split("-")
        if (parts.size != 3) return Pair(0, 0)
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toIntOrNull() ?: 2024)
            set(Calendar.MONTH, (parts[1].toIntOrNull() ?: 1) - 1)
            set(Calendar.DAY_OF_MONTH, parts[2].toIntOrNull() ?: 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        return Pair(startOfDay, endOfDay)
    }
}
