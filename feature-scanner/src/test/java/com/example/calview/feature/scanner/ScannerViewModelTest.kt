package com.example.calview.feature.scanner

import com.example.calview.core.ai.FoodAnalysisService
import com.example.calview.core.data.repository.MealRepository
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private lateinit var viewModel: ScannerViewModel
    private val foodAnalysisService: FoodAnalysisService = mockk()
    private val mealRepository: MealRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScannerViewModel(foodAnalysisService, mealRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        // Assertion logic will go here
        // assertEquals(ScannerUiState.Idle, viewModel.uiState.value)
    }
}
