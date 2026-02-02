package com.example.calview.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getMealsForDate(startOfDay: Long, endOfDay: Long): Flow<List<MealEntity>>
    
    // Get recent uploads (analyzing or recently completed meals from today)
    @Query("SELECT * FROM meals WHERE timestamp >= :startOfDay ORDER BY timestamp DESC LIMIT 5")
    fun getRecentUploads(startOfDay: Long): Flow<List<MealEntity>>
    
    // Get meal by ID
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: Long): MealEntity?
    
    @Query("SELECT * FROM meals WHERE id = :id")
    fun getMealByIdFlow(id: Long): Flow<MealEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long
    
    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)
    
    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()
}
