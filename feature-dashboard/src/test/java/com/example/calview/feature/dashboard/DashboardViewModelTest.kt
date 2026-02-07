package com.example.calview.feature.dashboard

import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.health.HealthData
import com.example.calview.core.ai.FoodAnalysisService
import android.content.Context
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val mealRepository: MealRepository = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val healthConnectManager: HealthConnectManager = mockk(relaxed = true)
    private val foodAnalysisService: FoodAnalysisService = mockk(relaxed = true)
    private val coachMessageGenerator: com.example.calview.core.data.coach.CoachMessageGenerator = mockk(relaxed = true)
    private val selectedDateHolder: com.example.calview.core.data.state.SelectedDateHolder = mockk(relaxed = true)
    private val waterReminderRepository: com.example.calview.core.data.repository.WaterReminderRepository = mockk(relaxed = true)
    private val streakFreezeRepository: com.example.calview.core.data.repository.StreakFreezeRepository = mockk(relaxed = true)
    private val dailyLogRepository: com.example.calview.core.data.repository.DailyLogRepository = mockk(relaxed = true)
    private val exerciseRepository: com.example.calview.core.data.repository.ExerciseRepository = mockk(relaxed = true)
    private val smartCoachService: com.example.calview.core.data.coach.SmartCoachService = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    
    // Use UnconfinedTestDispatcher for immediate execution
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flows - meals
        coEvery { mealRepository.getMealsForToday() } returns flowOf(emptyList())
        coEvery { mealRepository.getRecentUploads() } returns flowOf(emptyList())
        coEvery { mealRepository.getAllMeals() } returns flowOf(emptyList())
        coEvery { mealRepository.getMealsForDate(any()) } returns flowOf(emptyList())
        
        // Selected Date
        every { selectedDateHolder.selectedDate } returns MutableStateFlow(java.time.LocalDate.now())
        
        // Daily Log
        coEvery { dailyLogRepository.getLogForDate(any()) } returns flowOf(null)
        coEvery { dailyLogRepository.getLogForDateSync(any()) } returns null
        
        // Streak Freeze
        coEvery { streakFreezeRepository.getStreakData(any()) } returns flowOf(0)
        
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
        coEvery { userPreferencesRepository.recordBurn } returns flowOf(0)
        coEvery { userPreferencesRepository.lastKnownSteps } returns flowOf(0)
        coEvery { userPreferencesRepository.lastActivityTimestamp } returns flowOf(0L)
        coEvery { userPreferencesRepository.lastRolloverDate } returns flowOf(0L)
        coEvery { userPreferencesRepository.waterServingSize } returns flowOf(250)
        coEvery { userPreferencesRepository.hasSeenDashboardWalkthrough } returns flowOf(true)
        coEvery { userPreferencesRepository.setHasSeenDashboardWalkthrough(any()) } returns Unit
        
        // Water consumption preferences
        coEvery { userPreferencesRepository.waterConsumed } returns flowOf(0)
        coEvery { userPreferencesRepository.waterDate } returns flowOf(System.currentTimeMillis())
        coEvery { userPreferencesRepository.setWaterConsumed(any(), any()) } returns Unit
        coEvery { userPreferencesRepository.syncWidgetData() } returns Unit
        coEvery { userPreferencesRepository.setLastKnownSteps(any()) } returns Unit
        coEvery { userPreferencesRepository.setLastActivityTimestamp(any()) } returns Unit
        coEvery { userPreferencesRepository.setActivityStats(any(), any(), any()) } returns Unit
        coEvery { userPreferencesRepository.setRolloverCaloriesAmount(any()) } returns Unit
        coEvery { userPreferencesRepository.setLastRolloverDate(any()) } returns Unit
        
        // Water Reminder
        val defaultWaterSettings = com.example.calview.core.data.local.WaterReminderSettingsEntity(
            id = 1, enabled = false, intervalHours = 2, startHour = 8, endHour = 22, dailyGoalMl = 2000
        )
        coEvery { waterReminderRepository.observeSettings() } returns flowOf(defaultWaterSettings)
        
        // Exercise
        coEvery { exerciseRepository.getLastSevenDaysCalories() } returns flowOf(emptyList())
        coEvery { exerciseRepository.getTotalCaloriesBurnedForDate(any()) } returns flowOf(0)
        coEvery { exerciseRepository.getExercisesForDate(any()) } returns flowOf(emptyList())
        
        // Health Connect
        every { healthConnectManager.healthData } returns MutableStateFlow(HealthData())
        coEvery { healthConnectManager.isAvailable() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct default goal and zero consumption`() = runBlocking {
        viewModel = DashboardViewModel(
            mealRepository, 
            userPreferencesRepository, 
            healthConnectManager, 
            foodAnalysisService, 
            coachMessageGenerator,
            smartCoachService,
            selectedDateHolder,
            waterReminderRepository,
            streakFreezeRepository,
            dailyLogRepository,
            exerciseRepository,
            context
        )
        
        // Get first state where goalCalories is set
        val state = viewModel.dashboardState.first { it.goalCalories == 2000 }
        
        assertEquals(0, state.consumedCalories)
        assertEquals(2000, state.goalCalories)
        assertEquals(2000, state.remainingCalories)
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `state updates correctly when meals are emitted`() = runBlocking {
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
        viewModel = DashboardViewModel(
            mealRepository, 
            userPreferencesRepository, 
            healthConnectManager, 
            foodAnalysisService, 
            coachMessageGenerator,
            smartCoachService,
            selectedDateHolder,
            waterReminderRepository,
            streakFreezeRepository,
            dailyLogRepository,
            exerciseRepository,
            context
        )
        
        // Wait for state with meals data
        val state = viewModel.dashboardState.first { it.consumedCalories > 0 }

        // Assert
        assertEquals(800, state.consumedCalories) // 300 + 500
        assertEquals(1200, state.remainingCalories) // 2000 - 800
        assertEquals(50, state.proteinG) // 10 + 40
        assertEquals(50, state.carbsG) // 50 + 0
        assertEquals(15, state.fatsG) // 5 + 10
        viewModel.viewModelScope.cancel()
    }
    
    @Test
    fun `only COMPLETED meals count toward calorie totals`() = runBlocking {
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
        viewModel = DashboardViewModel(
            mealRepository, 
            userPreferencesRepository, 
            healthConnectManager, 
            foodAnalysisService, 
            coachMessageGenerator,
            smartCoachService,
            selectedDateHolder,
            waterReminderRepository,
            streakFreezeRepository,
            dailyLogRepository,
            exerciseRepository,
            context
        )
        
        // Wait for state with consumed calories
        val state = viewModel.dashboardState.first { it.consumedCalories > 0 }

        // Assert - only the completed meal's calories should be counted
        assertEquals(500, state.consumedCalories) // Only completed meal
        assertEquals(1500, state.remainingCalories) // 2000 - 500
        assertEquals(20, state.proteinG)
        assertEquals(30, state.carbsG)
        assertEquals(10, state.fatsG)
        viewModel.viewModelScope.cancel()
    }
    
    @Test
    fun `recentUploads are included in dashboard state`() = runBlocking {
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
        viewModel = DashboardViewModel(
            mealRepository, 
            userPreferencesRepository, 
            healthConnectManager, 
            foodAnalysisService, 
            coachMessageGenerator,
            smartCoachService,
            selectedDateHolder,
            waterReminderRepository,
            streakFreezeRepository,
            dailyLogRepository,
            exerciseRepository,
            context
        )
        
        // Wait for state with recent uploads
        val state = viewModel.dashboardState.first { it.recentUploads.isNotEmpty() }

        // Assert
        assertEquals(1, state.recentUploads.size)
        assertEquals("Recent Upload", state.recentUploads.first().name)
        viewModel.viewModelScope.cancel()
    }
    
    @Test
    fun `addWater increases water consumed`() = runBlocking {
        // Arrange
        viewModel = DashboardViewModel(
            mealRepository, 
            userPreferencesRepository, 
            healthConnectManager, 
            foodAnalysisService, 
            coachMessageGenerator,
            smartCoachService,
            selectedDateHolder,
            waterReminderRepository,
            streakFreezeRepository,
            dailyLogRepository,
            exerciseRepository,
            context
        )
        
        // Wait for initial state
        viewModel.dashboardState.first { it.goalCalories == 2000 }
        
        // Act
        viewModel.addWater(8)
        
        // Wait for state with water
        val state = viewModel.dashboardState.first { it.waterConsumed > 0 }

        // Assert
        assertEquals(8, state.waterConsumed)
        viewModel.viewModelScope.cancel()
    }
    
    @Test
    fun `removeWater decreases water consumed but not below zero`() = runBlocking {
        // Arrange
        viewModel = DashboardViewModel(
            mealRepository, 
            userPreferencesRepository, 
            healthConnectManager, 
            foodAnalysisService, 
            coachMessageGenerator,
            smartCoachService,
            selectedDateHolder,
            waterReminderRepository,
            streakFreezeRepository,
            dailyLogRepository,
            exerciseRepository,
            context
        )
        
        // Wait for initial state
        viewModel.dashboardState.first { it.goalCalories == 2000 }
        
        // Act - try to remove water when at 0
        viewModel.removeWater(8)
        
        // Get current state
        val state = viewModel.dashboardState.first { it.goalCalories == 2000 }

        // Assert - should stay at 0
        assertEquals(0, state.waterConsumed)
        viewModel.viewModelScope.cancel()
    }
}
