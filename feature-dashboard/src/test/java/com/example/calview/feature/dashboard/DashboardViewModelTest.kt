package com.example.calview.feature.dashboard

import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.health.HealthData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val mealRepository: MealRepository = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val healthConnectManager: HealthConnectManager = mockk(relaxed = true)
    
    // Use UnconfinedTestDispatcher for immediate execution
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flows
        coEvery { mealRepository.getMealsForToday() } returns flowOf(emptyList())
        coEvery { mealRepository.getRecentUploads() } returns flowOf(emptyList())
        coEvery { mealRepository.getAllMeals() } returns flowOf(emptyList())
        
        // User preferences - calorie and macro goals
        coEvery { userPreferencesRepository.recommendedCalories } returns flowOf(2000)
        coEvery { userPreferencesRepository.recommendedProtein } returns flowOf(125)
        coEvery { userPreferencesRepository.recommendedCarbs } returns flowOf(250)
        coEvery { userPreferencesRepository.recommendedFats } returns flowOf(56)
        
        // User preferences - settings
        coEvery { userPreferencesRepository.dailyStepsGoal } returns flowOf(10000)
        coEvery { userPreferencesRepository.rolloverExtraCalories } returns flowOf(false)
        coEvery { userPreferencesRepository.rolloverCaloriesAmount } returns flowOf(0)
        coEvery { userPreferencesRepository.addCaloriesBack } returns flowOf(false)
        
        // Health Connect
        every { healthConnectManager.healthData } returns MutableStateFlow(HealthData())
        coEvery { healthConnectManager.isAvailable() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct default goal and zero consumption`() = runTest {
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager)
        
        // Get first state where goalCalories is set
        val state = viewModel.dashboardState.first { it.goalCalories == 2000 }
        
        assertEquals(0, state.consumedCalories)
        assertEquals(2000, state.goalCalories)
        assertEquals(2000, state.remainingCalories)
    }

    @Test
    fun `state updates correctly when meals are emitted`() = runTest {
        // Arrange
        val meal1 = MealEntity(
            name = "Oatmeal", 
            calories = 300, 
            protein = 10, 
            carbs = 50, 
            fats = 5,
            timestamp = 1000L,
            analysisStatus = AnalysisStatus.COMPLETED
        )
        val meal2 = MealEntity(
            name = "Chicken", 
            calories = 500, 
            protein = 40, 
            carbs = 0, 
            fats = 10,
            timestamp = 2000L,
            analysisStatus = AnalysisStatus.COMPLETED
        )
        
        coEvery { mealRepository.getMealsForToday() } returns flowOf(listOf(meal1, meal2))

        // Act
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager)
        
        // Wait for state with meals data
        val state = viewModel.dashboardState.first { it.consumedCalories > 0 }

        // Assert
        assertEquals(800, state.consumedCalories) // 300 + 500
        assertEquals(1200, state.remainingCalories) // 2000 - 800
        assertEquals(50, state.proteinG) // 10 + 40
        assertEquals(50, state.carbsG) // 50 + 0
        assertEquals(15, state.fatsG) // 5 + 10
    }
    
    @Test
    fun `only COMPLETED meals count toward calorie totals`() = runTest {
        // Arrange - mix of analyzing and completed meals
        val completedMeal = MealEntity(
            name = "Completed Meal", 
            calories = 500, 
            protein = 20, 
            carbs = 30, 
            fats = 10,
            timestamp = 1000L,
            analysisStatus = AnalysisStatus.COMPLETED
        )
        val analyzingMeal = MealEntity(
            name = "Analyzing...", 
            calories = 0, 
            protein = 0, 
            carbs = 0, 
            fats = 0,
            timestamp = 2000L,
            analysisStatus = AnalysisStatus.ANALYZING
        )
        val pendingMeal = MealEntity(
            name = "Pending...", 
            calories = 0, 
            protein = 0, 
            carbs = 0, 
            fats = 0,
            timestamp = 3000L,
            analysisStatus = AnalysisStatus.PENDING
        )
        
        coEvery { mealRepository.getMealsForToday() } returns flowOf(listOf(completedMeal, analyzingMeal, pendingMeal))

        // Act
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager)
        
        // Wait for state with consumed calories
        val state = viewModel.dashboardState.first { it.consumedCalories > 0 }

        // Assert - only the completed meal's calories should be counted
        assertEquals(500, state.consumedCalories) // Only completed meal
        assertEquals(1500, state.remainingCalories) // 2000 - 500
        assertEquals(20, state.proteinG)
        assertEquals(30, state.carbsG)
        assertEquals(10, state.fatsG)
    }
    
    @Test
    fun `recentUploads are included in dashboard state`() = runTest {
        // Arrange
        val recentMeal = MealEntity(
            name = "Recent Upload", 
            calories = 200, 
            protein = 10, 
            carbs = 25, 
            fats = 5,
            timestamp = System.currentTimeMillis(),
            analysisStatus = AnalysisStatus.COMPLETED,
            imagePath = "/path/to/image.jpg"
        )
        
        coEvery { mealRepository.getRecentUploads() } returns flowOf(listOf(recentMeal))

        // Act
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager)
        
        // Wait for state with recent uploads
        val state = viewModel.dashboardState.first { it.recentUploads.isNotEmpty() }

        // Assert
        assertEquals(1, state.recentUploads.size)
        assertEquals("Recent Upload", state.recentUploads.first().name)
    }
    
    @Test
    fun `addWater increases water consumed`() = runTest {
        // Arrange
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager)
        
        // Wait for initial state
        viewModel.dashboardState.first { it.goalCalories == 2000 }
        
        // Act
        viewModel.addWater(8)
        
        // Wait for state with water
        val state = viewModel.dashboardState.first { it.waterConsumed > 0 }

        // Assert
        assertEquals(8, state.waterConsumed)
    }
    
    @Test
    fun `removeWater decreases water consumed but not below zero`() = runTest {
        // Arrange
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository, healthConnectManager)
        
        // Wait for initial state
        viewModel.dashboardState.first { it.goalCalories == 2000 }
        
        // Act - try to remove water when at 0
        viewModel.removeWater(8)
        
        // Get current state
        val state = viewModel.dashboardState.first { it.goalCalories == 2000 }

        // Assert - should stay at 0
        assertEquals(0, state.waterConsumed)
    }
}
