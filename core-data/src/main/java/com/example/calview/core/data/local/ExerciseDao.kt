package com.example.calview.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    
    @Query("SELECT * FROM exercises ORDER BY timestamp DESC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getExercisesForDate(startOfDay: Long, endOfDay: Long): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    suspend fun getExercisesForDateSync(startOfDay: Long, endOfDay: Long): List<ExerciseEntity>
    
    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM exercises WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getTotalCaloriesBurnedForDate(startOfDay: Long, endOfDay: Long): Flow<Int>
    
    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM exercises WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    suspend fun getTotalCaloriesBurnedForDateSync(startOfDay: Long, endOfDay: Long): Int
    
    @Query("SELECT * FROM exercises WHERE type = :type ORDER BY timestamp DESC")
    fun getExercisesByType(type: ExerciseType): Flow<List<ExerciseEntity>>
    
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    
    @Query("SELECT * FROM exercises WHERE firestoreId = :firestoreId")
    suspend fun getExerciseByFirestoreId(firestoreId: String): ExerciseEntity?
    
    @Query("SELECT * FROM exercises ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentExercises(limit: Int): Flow<List<ExerciseEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
    
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
    
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
    
    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: Long)
    
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()
}
