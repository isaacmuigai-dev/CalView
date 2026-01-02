package com.example.calview.core.data.repository

import com.example.calview.core.data.local.MealDao
import com.example.calview.core.data.local.MealEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao
) : MealRepository {

    override fun getAllMeals(): Flow<List<MealEntity>> = mealDao.getAllMeals()

    override fun getMealsForToday(): Flow<List<MealEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis
        
        return mealDao.getMealsForDate(startOfDay, endOfDay)
    }
    
    override fun getRecentUploads(): Flow<List<MealEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.timeInMillis
        return mealDao.getRecentUploads(startOfDay)
    }
    
    override suspend fun getMealById(id: Long): MealEntity? {
        return mealDao.getMealById(id)
    }

    override suspend fun logMeal(meal: MealEntity): Long {
        return mealDao.insertMeal(meal)
    }
    
    override suspend fun updateMeal(meal: MealEntity) {
        mealDao.updateMeal(meal)
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        mealDao.deleteMeal(meal)
    }
    
    override suspend fun deleteMealById(id: Long) {
        mealDao.getMealById(id)?.let { meal ->
            mealDao.deleteMeal(meal)
        }
    }
}
