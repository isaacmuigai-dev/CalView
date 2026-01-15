package com.example.calview.feature.subscription

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.example.calview.core.data.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PaywallUiState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val productDetailsList: List<ProductDetails> = emptyList(),
    val selectedProduct: ProductDetails? = null,
    val selectedOfferToken: String = ""
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    // Internal mutable state for selection
    private val _selectionState = MutableStateFlow<Pair<ProductDetails?, String>>(null to "")

    val uiState: StateFlow<PaywallUiState> = combine(
        billingManager.isPremium,
        billingManager.productDetails,
        _selectionState
    ) { isPremium, products, selection ->
        
        // Auto-select logic if nothing is selected yet and products are available
        var (currentProduct, currentToken) = selection
        if (currentProduct == null && products.isNotEmpty()) {
            val yearly = products.find { it.productId == BillingManager.SUBSCRIPTION_ID_YEARLY }
            if (yearly != null) {
                currentProduct = yearly
                currentToken = yearly.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
            } else {
                currentProduct = products.first()
                currentToken = products.first().subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
            }
            // Update internal state to reflect auto-selection
            _selectionState.update { currentProduct to currentToken }
        }

        PaywallUiState(
            isLoading = products.isEmpty(),
            isPremium = isPremium,
            productDetailsList = products,
            selectedProduct = currentProduct,
            selectedOfferToken = currentToken
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PaywallUiState()
    )

    fun selectProduct(product: ProductDetails, offerToken: String) {
        _selectionState.update { product to offerToken }
    }

    fun launchBillingFlow(activity: Activity) {
        val state = uiState.value
        if (state.selectedProduct != null) {
            billingManager.launchBillingFlow(activity, state.selectedProduct, state.selectedOfferToken)
        }
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }
}
