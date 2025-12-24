package com.example.calview.feature.dashboard

import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.data.repository.MealRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import com.example.calview.core.data.repository.UserPreferencesRepository

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val mealRepository: MealRepository = mockk()
    private val userPreferencesRepository: UserPreferencesRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flow for getMealsForToday (used by ViewModel)
        coEvery { mealRepository.getMealsForToday() } returns flowOf(emptyList())
        // Mock default flow for recommendedCalories
        coEvery { userPreferencesRepository.recommendedCalories } returns flowOf(2500)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has correct default goal and zero consumption`() = runTest {
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository)
        // Advance coroutines to ensure stateIn starts
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.dashboardState.value
        assertEquals(0, state.consumedCalories)
        assertEquals(2500, state.goalCalories)
        assertEquals(2500, state.remainingCalories)
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
            timestamp = 1000L
        )
        val meal2 = MealEntity(
            name = "Chicken", 
            calories = 500, 
            protein = 40, 
            carbs = 0, 
            fats = 10,
            timestamp = 2000L
        )
        
        coEvery { mealRepository.getMealsForToday() } returns flowOf(listOf(meal1, meal2))

        // Act
        viewModel = DashboardViewModel(mealRepository, userPreferencesRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.dashboardState.value
        assertEquals(800, state.consumedCalories) // 300 + 500
        assertEquals(1700, state.remainingCalories) // 2500 - 800
        assertEquals(50, state.proteinG) // 10 + 40
        assertEquals(50, state.carbsG) // 50 + 0
        assertEquals(15, state.fatsG) // 5 + 10
    }
}
