package com.example.calview.core.data.repository

import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealDao
import com.example.calview.core.data.local.MealEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealRepositoryImplTest {

    private lateinit var repository: MealRepositoryImpl
    private val mealDao: MealDao = mockk(relaxed = true)
    private val firestoreRepository: FirestoreRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val storageRepository: StorageRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        // Return empty userId to prevent Firestore sync during tests
        coEvery { authRepository.getUserId() } returns ""
        
        repository = MealRepositoryImpl(
            mealDao = mealDao,
            firestoreRepository = firestoreRepository,
            authRepository = authRepository,
            storageRepository = storageRepository
        )
    }

    @Test
    fun `getAllMeals returns flow from dao`() = runTest {
        // Arrange
        val meals = listOf(
            MealEntity(id = 1, name = "Food 1", calories = 100, protein = 10, carbs = 20, fats = 5),
            MealEntity(id = 2, name = "Food 2", calories = 200, protein = 20, carbs = 30, fats = 10)
        )
        coEvery { mealDao.getAllMeals() } returns flowOf(meals)

        // Act
        val result = repository.getAllMeals().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Food 1", result[0].name)
        assertEquals("Food 2", result[1].name)
    }
    
    @Test
    fun `logMeal inserts meal and returns id`() = runTest {
        // Arrange
        val meal = MealEntity(
            name = "Test Meal",
            calories = 300,
            protein = 15,
            carbs = 40,
            fats = 8
        )
        coEvery { mealDao.insertMeal(meal) } returns 42L

        // Act
        val result = repository.logMeal(meal)

        // Assert
        assertEquals(42L, result)
        coVerify { mealDao.insertMeal(meal) }
    }
    
    @Test
    fun `getMealById returns meal from dao`() = runTest {
        // Arrange
        val meal = MealEntity(
            id = 1,
            name = "Test Meal",
            calories = 300,
            protein = 15,
            carbs = 40,
            fats = 8,
            analysisStatus = AnalysisStatus.COMPLETED
        )
        coEvery { mealDao.getMealById(1L) } returns meal

        // Act
        val result = repository.getMealById(1L)

        // Assert
        assertNotNull(result)
        assertEquals("Test Meal", result?.name)
        assertEquals(AnalysisStatus.COMPLETED, result?.analysisStatus)
    }
    
    @Test
    fun `getMealById returns null when meal not found`() = runTest {
        // Arrange
        coEvery { mealDao.getMealById(999L) } returns null

        // Act
        val result = repository.getMealById(999L)

        // Assert
        assertNull(result)
    }
    
    @Test
    fun `updateMeal calls dao updateMeal`() = runTest {
        // Arrange
        val meal = MealEntity(
            id = 1,
            name = "Updated Meal",
            calories = 400,
            protein = 20,
            carbs = 50,
            fats = 15,
            analysisStatus = AnalysisStatus.COMPLETED,
            analysisProgress = 100f
        )

        // Act
        repository.updateMeal(meal)

        // Assert
        coVerify { mealDao.updateMeal(meal) }
    }
    
    @Test
    fun `deleteMeal calls dao deleteMeal`() = runTest {
        // Arrange
        val meal = MealEntity(
            id = 1,
            name = "Meal to Delete",
            calories = 100,
            protein = 5,
            carbs = 10,
            fats = 3
        )

        // Act
        repository.deleteMeal(meal)

        // Assert
        coVerify { mealDao.deleteMeal(meal) }
    }
}
