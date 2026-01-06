package com.example.calview.core.data.repository

import com.example.calview.core.data.local.MealDao
import com.example.calview.core.data.local.MealEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlinx.coroutines.launch
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao,
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : MealRepository {
    
    // Scope for background sync operations
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

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
    
    override fun getMealsForDate(dateString: String): Flow<List<MealEntity>> {
        // Parse date string (YYYY-MM-DD) to timestamps
        val parts = dateString.split("-")
        if (parts.size != 3) return mealDao.getMealsForDate(0, 0) // Return empty
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toIntOrNull() ?: 2024)
            set(Calendar.MONTH, (parts[1].toIntOrNull() ?: 1) - 1) // Month is 0-indexed
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
        val id = mealDao.insertMeal(meal)
        
        // Sync to Firestore if user is signed in
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    // Upload image to Firebase Storage if local path exists
                    var mealToSave = meal
                    if (!meal.imagePath.isNullOrEmpty()) {
                        val imageUrl = storageRepository.uploadMealImage(
                            localPath = meal.imagePath,
                            userId = userId,
                            firestoreId = meal.firestoreId
                        )
                        if (imageUrl != null) {
                            // Update local DB with image URL
                            mealToSave = meal.copy(imageUrl = imageUrl)
                            mealDao.updateMeal(mealToSave.copy(id = id))
                        }
                    }
                    // Save meal with imageUrl to Firestore
                    firestoreRepository.saveMeal(userId, mealToSave)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        return id
    }
    
    override suspend fun updateMeal(meal: MealEntity) {
        mealDao.updateMeal(meal)
        
        // Sync to Firestore
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.saveMeal(userId, meal)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun deleteMeal(meal: MealEntity) {
        mealDao.deleteMeal(meal)
        
        // Sync to Firestore
        val userId = authRepository.getUserId()
        if (userId.isNotEmpty()) {
            scope.launch {
                try {
                    firestoreRepository.deleteMeal(userId, meal.firestoreId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override suspend fun deleteMealById(id: Long) {
        mealDao.getMealById(id)?.let { meal ->
            deleteMeal(meal)
        }
    }
    
    override suspend fun restoreFromCloud(): Boolean {
        val userId = authRepository.getUserId()
        if (userId.isEmpty()) return false
        
        return try {
            val cloudMeals = firestoreRepository.getMeals(userId)
            if (cloudMeals.isNotEmpty()) {
                // Insert all meals from cloud to local DB
                cloudMeals.forEach { meal ->
                    var mealToInsert = meal
                    
                    // Download image if we have a cloud URL but no local file
                    if (!meal.imageUrl.isNullOrEmpty()) {
                        val localFile = if (!meal.imagePath.isNullOrEmpty()) {
                            java.io.File(meal.imagePath)
                        } else null
                        
                        // If local file doesn't exist, download from cloud
                        if (localFile == null || !localFile.exists()) {
                            val newLocalPath = android.os.Environment.getExternalStoragePublicDirectory(
                                android.os.Environment.DIRECTORY_PICTURES
                            ).absolutePath + "/CalView/meals/${meal.firestoreId}.jpg"
                            
                            val downloaded = storageRepository.downloadMealImage(meal.imageUrl, newLocalPath)
                            if (downloaded) {
                                mealToInsert = meal.copy(imagePath = newLocalPath)
                            }
                        }
                    }
                    
                    mealDao.insertMeal(mealToInsert)
                }
                android.util.Log.d("MealRestore", "Restored ${cloudMeals.size} meals from cloud")
                true
            } else {
                android.util.Log.d("MealRestore", "No meals found in cloud")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("MealRestore", "Error restoring meals", e)
            false
        }
    }
}
