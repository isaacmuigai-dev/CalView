package com.example.calview.feature.subscription

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.SubscriptionOfferDetails
import com.example.calview.core.data.billing.BillingManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private lateinit var viewModel: PaywallViewModel
    private val billingManager: BillingManager = mockk(relaxed = true)
    
    // Mock StateFlows for BillingManager
    private val isPremiumFlow = MutableStateFlow(false)
    private val productDetailsFlow = MutableStateFlow<List<ProductDetails>>(emptyList())
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { billingManager.isPremium } returns isPremiumFlow
        every { billingManager.productDetails } returns productDetailsFlow
        
        viewModel = PaywallViewModel(billingManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.productDetailsList.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun `auto-selects yearly plan when products loaded`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Arrange
        val yearlyProduct = mockProduct(BillingManager.SUBSCRIPTION_ID_YEARLY, "yearly_token")
        val monthlyProduct = mockProduct(BillingManager.SUBSCRIPTION_ID_MONTHLY, "monthly_token")
        
        // Act
        productDetailsFlow.emit(listOf(monthlyProduct, yearlyProduct))
        
        // Wait for state update
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        
        // Assert
        assertFalse(state.isLoading)
        assertEquals(2, state.productDetailsList.size)
        assertEquals(yearlyProduct, state.selectedProduct)
        assertEquals("yearly_token", state.selectedOfferToken)
        
        collectJob.cancel()
    }
    
    @Test
    fun `auto-selects first plan if yearly not available`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Arrange
        val monthlyProduct = mockProduct(BillingManager.SUBSCRIPTION_ID_MONTHLY, "monthly_token")
        
        // Act
        productDetailsFlow.emit(listOf(monthlyProduct))
        
        // Wait for state update
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        
        // Assert
        assertEquals(monthlyProduct, state.selectedProduct)
        assertEquals("monthly_token", state.selectedOfferToken)
        
        collectJob.cancel()
    }

    @Test
    fun `selectProduct updates state`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Arrange
        val yearlyProduct = mockProduct(BillingManager.SUBSCRIPTION_ID_YEARLY, "yearly_token")
        val monthlyProduct = mockProduct(BillingManager.SUBSCRIPTION_ID_MONTHLY, "monthly_token")
        productDetailsFlow.emit(listOf(monthlyProduct, yearlyProduct))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act - select monthly
        viewModel.selectProduct(monthlyProduct, "monthly_token")
        testDispatcher.scheduler.advanceUntilIdle() // Ensure flow update
        
        val state = viewModel.uiState.value
        
        // Assert
        assertEquals(monthlyProduct, state.selectedProduct)
        assertEquals("monthly_token", state.selectedOfferToken)
        
        collectJob.cancel()
    }

    @Test
    fun `launchBillingFlow uses selected product`() = runTest(testDispatcher) {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Arrange
        val activity: Activity = mockk()
        val product = mockProduct("test_id", "test_token")
        productDetailsFlow.emit(listOf(product))
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Act
        viewModel.launchBillingFlow(activity)
        
        // Assert
        verify { billingManager.launchBillingFlow(activity, product, "test_token") }
        
        collectJob.cancel()
    }
    
    @Test
    fun `restorePurchases delegates to billingManager`() = runTest(testDispatcher) {
        // Act
        viewModel.restorePurchases()
        
        // Assert
        verify { billingManager.restorePurchases() }
    }

    // Helper to generic mock ProductDetails since it's final and hard to mock deep hierarchy
    // We rely on simple mocks for this test logic
    private fun mockProduct(id: String, token: String): ProductDetails {
        val product = mockk<ProductDetails>(relaxed = true)
        every { product.productId } returns id
        
        val offerDetails = mockk<SubscriptionOfferDetails>(relaxed = true)
        every { offerDetails.offerToken } returns token
        every { product.subscriptionOfferDetails } returns listOf(offerDetails)
        
        return product
    }
}
