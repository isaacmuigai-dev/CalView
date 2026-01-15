package com.example.calview.core.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Billing via native Google Play Billing Library.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        
        // Product IDs (Subscription IDs from Play Console)
        const val SUBSCRIPTION_ID_MONTHLY = "calview_pro_monthly"
        const val SUBSCRIPTION_ID_YEARLY = "calview_pro_yearly"
        
        private val PRODUCT_IDS = listOf(
            SUBSCRIPTION_ID_MONTHLY,
            SUBSCRIPTION_ID_YEARLY
        )

        // Test emails that get automatic premium access
        private val TEST_EMAILS = listOf(
            "calviewai.reviewer@gmail.com",
        )
    }

    private val _billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()
    
    // Connection retry logic
    private var connectionRetryCount = 0

    // Current user email for test bypass
    private var currentUserEmail: String? = null

    init {
        startConnection()
    }
    
    /**
     * Set the current user's email to check for test account bypass
     */
    fun setUserEmail(email: String?) {
        currentUserEmail = email
        checkTestAccount()
    }
    
    private fun checkTestAccount() {
        if (currentUserEmail != null && TEST_EMAILS.any { it.equals(currentUserEmail, ignoreCase = true) }) {
            Log.d(TAG, "Test account detected - granting premium access")
            _isPremium.value = true
        }
    }

    /**
     * Start connection to Billing Service
     */
    private fun startConnection() {
        _billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing Service Connected")
                    connectionRetryCount = 0
                    queryProductDetails()
                    queryPurchases()
                } else {
                    Log.e(TAG, "Billing Service Connection Failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e(TAG, "Billing Service Disconnected")
                retryConnection()
            }
        })
    }
    
    private fun retryConnection() {
        val maxRetries = 3
        if (connectionRetryCount < maxRetries) {
            connectionRetryCount++
            scope.launch {
                delay(2000L * connectionRetryCount) // Exponential backoffish
                startConnection()
            }
        }
    }

    /**
     * Query product details from Google Play
     */
    private fun queryProductDetails() {
        val productList = PRODUCT_IDS.map { 
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        _billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Product Details Fetched: ${productDetailsList.size}")
                _productDetails.value = productDetailsList
            } else {
                Log.e(TAG, "Error querying product details: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Launch Billing Flow for purchasing a product
     */
    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails, offerToken: String) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
            
        val billingResult = _billingClient.launchBillingFlow(activity, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
        }
    }

    /**
     * Handle updates from the Billing Client (purchases)
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User cancelled the purchase")
        } else {
            Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
        }
    }

    /**
     * Acknowledge purchase and unlock content
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                    
                _billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")
                        _isPremium.value = true
                    } else {
                        Log.e(TAG, "Purchase acknowledgement failed: ${billingResult.debugMessage}")
                    }
                }
            } else {
                // Already acknowledged
                _isPremium.value = true
            }
        }
    }

    /**
     * Query existing purchases to restore state
     */
    fun queryPurchases() {
        if (!_billingClient.isReady) {
            startConnection()
            return
        }
        
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        _billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var hasActiveSubscription = false
                if (purchases.isNotEmpty()) {
                    for (purchase in purchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            hasActiveSubscription = true
                            // Ensure it's acknowledged if it's not already
                            if (!purchase.isAcknowledged) {
                                handlePurchase(purchase)
                            }
                        }
                    }
                }
                
                // If not found in SUBS, check INAPP if you support that
                // otherwise set value
                if (hasActiveSubscription) {
                    _isPremium.value = true
                } else {
                    // Only flip to false if we haven't manually granted access via test email
                    checkTestAccount()
                }
            } else {
                Log.e(TAG, "Error querying purchases: ${billingResult.debugMessage}")
            }
        }
    }
    
    fun restorePurchases() {
        queryPurchases()
    }
}
