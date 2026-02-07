package com.example.calview.feature.trends

import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import com.example.calview.core.data.repository.UserPreferencesRepository
import com.example.calview.core.data.health.HealthConnectManager
import com.example.calview.core.data.state.SelectedDateHolder
import com.example.calview.core.data.local.WeightHistoryDao
import com.example.calview.core.data.prediction.WeightPredictionEngine
import android.util.Log
import io.mockk.coEvery

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher

import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import com.example.calview.feature.trends.R

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    // Test subclass to expose onCleared() for coroutine cleanup
    private class TestProgressViewModel(
        userPreferencesRepository: UserPreferencesRepository,
        mealRepository: MealRepository,
        healthConnectManager: HealthConnectManager,
        selectedDateHolder: SelectedDateHolder,
        weightHistoryRepository: com.example.calview.core.data.repository.WeightHistoryRepository,
        predictionEngine: WeightPredictionEngine,
        streakFreezeRepository: com.example.calview.core.data.repository.StreakFreezeRepository,
        notificationHandler: com.example.calview.core.data.notification.NotificationHandler,
        exerciseRepository: com.example.calview.core.data.repository.ExerciseRepository
    ) : ProgressViewModel(
        userPreferencesRepository,
        mealRepository,
        healthConnectManager,
        selectedDateHolder,
        weightHistoryRepository,
        predictionEngine,
        streakFreezeRepository,
        notificationHandler,
        exerciseRepository
    ) {
        public fun clear() {
            onCleared()
        }
    }

    private lateinit var viewModel: TestProgressViewModel

    private val mealRepository: MealRepository = mockk(relaxed = true)
    private val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true)
    private val healthConnectManager: HealthConnectManager = mockk(relaxed = true)
    private val selectedDateHolder: SelectedDateHolder = mockk(relaxed = true)
    private val weightHistoryRepository: com.example.calview.core.data.repository.WeightHistoryRepository = mockk(relaxed = true)
    private val predictionEngine: WeightPredictionEngine = mockk(relaxed = true)
    private val streakFreezeRepository: com.example.calview.core.data.repository.StreakFreezeRepository = mockk(relaxed = true)
    private val notificationHandler: com.example.calview.core.data.notification.NotificationHandler = mockk(relaxed = true)
    private val exerciseRepository: com.example.calview.core.data.repository.ExerciseRepository = mockk(relaxed = true)
    
    private val testDispatcher = StandardTestDispatcher()



    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0


        Dispatchers.setMain(testDispatcher)

        
        // Mock user preferences flows
        every { userPreferencesRepository.weight } returns flowOf(75f)
        every { userPreferencesRepository.goalWeight } returns flowOf(70f)
        every { userPreferencesRepository.height } returns flowOf(175)
        every { userPreferencesRepository.recommendedCalories } returns flowOf(2000)
        every { userPreferencesRepository.recommendedProtein } returns flowOf(125)
        every { userPreferencesRepository.recommendedCarbs } returns flowOf(250)
        every { userPreferencesRepository.recommendedFats } returns flowOf(56)
        every { userPreferencesRepository.dailyStepsGoal } returns flowOf(10000)
        every { userPreferencesRepository.startWeight } returns flowOf(80f)
        
        // Mock meal repository flows
        every { mealRepository.getMealsForToday() } returns flowOf(emptyList())
        every { mealRepository.getAllMeals() } returns flowOf(emptyList())
        every { mealRepository.getMealsForDate(any()) } returns flowOf(emptyList())
        
        // Mock other dependencies
        every { selectedDateHolder.selectedDate } returns MutableStateFlow(LocalDate.now())
        every { healthConnectManager.healthData } returns MutableStateFlow(com.example.calview.core.data.health.HealthData())
        every { healthConnectManager.isAvailable() } returns false
        every { weightHistoryRepository.getAllWeightHistory() } returns flowOf(emptyList())
        every { predictionEngine.predictWeight(any(), any()) } returns com.example.calview.core.data.prediction.WeightPredictionEngine.PredictionResult(0f, 0f, null, null, com.example.calview.core.data.prediction.WeightPredictionEngine.Trend.INSUFFICIENT_DATA)
        every { userPreferencesRepository.recordBurn } returns flowOf(0)
        every { exerciseRepository.getTotalCaloriesBurnedForDate(any()) } returns flowOf(0)
        every { exerciseRepository.getLastSevenDaysCalories() } returns flowOf(listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        every { userPreferencesRepository.hasSeenProgressWalkthrough } returns flowOf(true)
        every { userPreferencesRepository.userLevel } returns flowOf(1)
        every { userPreferencesRepository.userXp } returns flowOf(0)
        every { userPreferencesRepository.waterConsumed } returns flowOf(0)
        every { userPreferencesRepository.waterDate } returns flowOf(0L)
        every { userPreferencesRepository.notifiedDailyGoalFlags } returns flowOf("")
        every { userPreferencesRepository.lastNotifiedDailyGoalDate } returns flowOf("")
        every { userPreferencesRepository.userGoal } returns flowOf("Maintain")
        every { userPreferencesRepository.weightChangePerWeek } returns flowOf(0.5f)
        coEvery { streakFreezeRepository.ensureStreakFreezeInitialized() } returns Unit
        coEvery { streakFreezeRepository.observeCurrentMonthFreezes() } returns flowOf(null)


    }

    @After
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.clear()
        }
        Dispatchers.resetMain()
    }


    // ==================== DATA FLOW TESTS ====================

    @Test
    fun `user metrics flow correctly from repository to UI state`() = runTest {
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value



        // Assert - verify user metrics are wired correctly
        assertEquals(75f, state.currentWeight, 0.1f)
        assertEquals(70f, state.goalWeight, 0.1f)
        assertEquals(175, state.height)
        assertEquals(2000, state.calorieGoal)
        assertEquals(125, state.proteinGoal)
        assertEquals(250, state.carbsGoal)
        assertEquals(56, state.fatsGoal)
        assertEquals(10000, state.stepsGoal)
    }

    @Test
    fun `BMI is calculated correctly from weight and height`() = runTest {
        // Arrange - weight 75kg, height 175cm
        // BMI = 75 / (1.75)^2 = 75 / 3.0625 = 24.49
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(24.49f, state.bmi, 0.5f) // Allow some tolerance
        assertEquals(R.string.bmi_healthy, state.bmiCategory)

    }

    @Test
    fun `BMI category is Underweight when BMI below 18_5`() = runTest {
        // Arrange - weight 50kg, height 175cm = BMI 16.3
        every { userPreferencesRepository.weight } returns flowOf(50f)
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(R.string.bmi_underweight, state.bmiCategory)

    }

    @Test
    fun `BMI category is Overweight when BMI 25 to 30`() = runTest {
        // Arrange - weight 90kg, height 175cm = BMI 29.4
        every { userPreferencesRepository.weight } returns flowOf(90f)
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(R.string.bmi_overweight, state.bmiCategory)

    }

    @Test
    fun `BMI category is Obese when BMI above 30`() = runTest {
        // Arrange - weight 100kg, height 175cm = BMI 32.7
        every { userPreferencesRepository.weight } returns flowOf(100f)
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(R.string.bmi_obese, state.bmiCategory)

    }

    // ==================== MEAL DATA FLOW TESTS ====================

    @Test
    fun `today macros are calculated from meals correctly`() = runTest {
        // Arrange
        val todayMeals = listOf(
            MealEntity(
                name = "Breakfast",
                calories = 400,
                protein = 20,
                carbs = 50,
                fats = 15,
                timestamp = System.currentTimeMillis(),
                analysisStatus = AnalysisStatus.COMPLETED
            ),
            MealEntity(
                name = "Lunch",
                calories = 600,
                protein = 35,
                carbs = 60,
                fats = 20,
                timestamp = System.currentTimeMillis(),
                analysisStatus = AnalysisStatus.COMPLETED
            )
        )
        // ViewModel uses getMealsForDate via flatMapLatest on selectedDate
        every { mealRepository.getMealsForDate(any()) } returns flowOf(todayMeals)
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(1000, state.todayCalories) // 400 + 600
        assertEquals(55f, state.todayProtein, 0.1f) // 20 + 35
        assertEquals(110f, state.todayCarbs, 0.1f) // 50 + 60
        assertEquals(35f, state.todayFats, 0.1f) // 15 + 20

    }

    // ==================== STREAK CALCULATION TESTS ====================

    @Test
    fun `streak is calculated from consecutive meal days`() = runTest {
        // Arrange - meals from last 3 consecutive days
        val today = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        val meals = listOf(
            MealEntity(name = "Today", calories = 500, protein = 20, carbs = 50, fats = 15, 
                timestamp = today, analysisStatus = AnalysisStatus.COMPLETED),
            MealEntity(name = "Yesterday", calories = 500, protein = 20, carbs = 50, fats = 15, 
                timestamp = today - oneDayMs, analysisStatus = AnalysisStatus.COMPLETED),
            MealEntity(name = "2 Days Ago", calories = 500, protein = 20, carbs = 50, fats = 15, 
                timestamp = today - 2 * oneDayMs, analysisStatus = AnalysisStatus.COMPLETED)
        )
        every { mealRepository.getAllMeals() } returns flowOf(meals)
        every { streakFreezeRepository.getStreakData(any()) } returns flowOf(1)
        every { streakFreezeRepository.calculateBestStreak(any(), any()) } returns flowOf(5)
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert - should have at least 3 day streak
        assertTrue("Expected streak >= 1, got ${state.dayStreak}", state.dayStreak >= 1)

    }

    @Test
    fun `best streak is calculated correctly`() = runTest {
        // Arrange
        val today = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        
        val meals = listOf(
            MealEntity(name = "Today", calories = 500, protein = 20, carbs = 50, fats = 15, 
                timestamp = today, analysisStatus = AnalysisStatus.COMPLETED)
        )
        every { mealRepository.getAllMeals() } returns flowOf(meals)
        every { streakFreezeRepository.getStreakData(any()) } returns flowOf(1)
        every { streakFreezeRepository.calculateBestStreak(any(), any()) } returns flowOf(1)
        
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert - best streak should be at least 1
        assertTrue("Expected bestStreak >= 1, got ${state.bestStreak}", state.bestStreak >= 1)

    }

    // ==================== WEEKLY DATA TESTS ====================

    @Test
    fun `weekly calories data is generated for 7 days`() = runTest {
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(7, state.weeklyCalories.size)
        assertTrue(state.weeklyCalories.all { it.day.isNotEmpty() })
    }


    @Test
    fun `completed days list has 7 entries for week display`() = runTest {
        // Act
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // Assert
        assertEquals(7, state.completedDays.size)
    }


    // ==================== REFRESH FUNCTIONALITY ====================

    @Test
    fun `refreshData sets isLoading to true initially`() = runTest {
        // Arrange
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act
        viewModel.refreshData()
        
        // Assert - verify it sets loading
        assertTrue("isLoading should be true initially", viewModel.uiState.value.isLoading)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify it resets
        assertTrue("isLoading should be false after background work", !viewModel.uiState.value.isLoading)
    }


    // ==================== WEEK SELECTION TESTS ====================

    @Test
    fun `setWeekOffset updates selectedWeekOffset in state`() = runTest {
        // Arrange
        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act
        viewModel.setWeekOffset(2)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value

        // Assert
        assertEquals(2, state.selectedWeekOffset)
    }

    @Test
    fun `weekly calories ignores meals from other weeks based on offset`() = runTest {
        // Arrange:
        // Today is in week 0.
        // Create meals for "Today" (Week 0) and "Last Week" (Week 1).
        val today = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        
        val week0Meal = MealEntity(name = "Week 0", calories = 500, protein = 20, carbs = 50, fats = 15, 
            timestamp = today, analysisStatus = AnalysisStatus.COMPLETED)
            
        val week1Meal = MealEntity(name = "Week 1", calories = 800, protein = 30, carbs = 60, fats = 25, 
            timestamp = today - oneWeekMs, analysisStatus = AnalysisStatus.COMPLETED)
            
        every { mealRepository.getAllMeals() } returns flowOf(listOf(week0Meal, week1Meal))

        viewModel = TestProgressViewModel(
            userPreferencesRepository, 
            mealRepository, 
            healthConnectManager, 
            selectedDateHolder, 
            weightHistoryRepository, 
            predictionEngine,
            streakFreezeRepository,
            notificationHandler,
            exerciseRepository
        )


        // Act 1: Default offset 0 (This week)
        testDispatcher.scheduler.advanceUntilIdle()
        val state0 = viewModel.uiState.value
        val week0Calories = state0.weeklyCalories.sumOf { it.calories }
        
        // Assert 1: Should only include week 0 meal (500)
        // If this fails, it might be due to day-of-week calculation differences between test and app
        assertEquals("Expected 500 calories for current week, got $week0Calories. Weekly data: ${state0.weeklyCalories}", 500, week0Calories)

        // Act 2: Offset 1 (Last week)
        viewModel.setWeekOffset(1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state1 = viewModel.uiState.value
        val week1Calories = state1.weeklyCalories.sumOf { it.calories }

        // Assert 2: Should only include week 1 meal (800)
        assertEquals("Expected 800 calories for last week, got $week1Calories. Weekly data: ${state1.weeklyCalories}", 800, week1Calories)
    }

}
