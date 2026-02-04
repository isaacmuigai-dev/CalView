package com.example.calview.feature.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.data.billing.BillingManager
import com.example.calview.core.ui.theme.CalViewTheme
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import androidx.compose.ui.res.stringResource
import com.example.calview.feature.subscription.R

// Premium paywall color palette - Light theme matching colors
private val PaywallGradientStart = Color(0xFFFFF0EB)   // Soft coral tint (matches LightGradientMid)
private val PaywallGradientMid = Color(0xFFF5EEF8)     // Soft purple tint (matches LightGradientEnd)
private val PaywallGradientEnd = Color(0xFFFFFFFF)     // Pure white
private val PaywallPrimary = Color(0xFF4A7C59)         // SageGreen - primary accent
private val PaywallPrimaryLight = Color(0xFF6B9B7A)    // SageGreenLight
private val PaywallAccent = Color(0xFFE8927C)          // WarmCoral for highlights
private val PaywallCardSelected = Color(0xFFF0FAF3)    // Very light sage tint
private val PaywallCardBorder = Color(0xFF4A7C59)      // SageGreen border

@Composable
fun PaywallScreen(
    billingManager: BillingManager,
    onClose: () -> Unit,
    onSubscriptionSuccess: () -> Unit = {},
    viewModel: PaywallViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val uiState by viewModel.uiState.collectAsState()

    // Auto-navigate if premium
    LaunchedEffect(uiState.isPremium) {
        if (uiState.isPremium) {
            onSubscriptionSuccess()
        }
    }

    // Adaptive layout
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)

    // Subtle pulse animation for CTA
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Vibrant gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PaywallGradientStart,
                                    PaywallGradientMid,
                                    PaywallGradientEnd.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
                
                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(PaywallPrimary.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close),
                        tint = Color.White
                    )
                }
                
                // PRO Badge in center
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // PRO text with SageGreen styling - Premium typography
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PaywallPrimary
                    ) {
                        Text(
                            text = "PRO",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.02).sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Title & Subtitle
            Text(
                text = stringResource(R.string.unlock_full_potential),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Features Checklist with premium styling
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PremiumFeatureItem(stringResource(R.string.feature_unlimited_scans))
                PremiumFeatureItem(stringResource(R.string.feature_advanced_macros))
                PremiumFeatureItem(stringResource(R.string.feature_trends_insights))
                PremiumFeatureItem(stringResource(R.string.feature_voice_logging))
                PremiumFeatureItem(stringResource(R.string.feature_ai_coach))
                PremiumFeatureItem(stringResource(R.string.feature_streak_freeze))
                PremiumFeatureItem(stringResource(R.string.feature_water_reminders))
                PremiumFeatureItem(stringResource(R.string.feature_social_challenges))
                PremiumFeatureItem(stringResource(R.string.feature_no_ads))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Subscription Options
            if (uiState.isLoading && uiState.productDetailsList.isEmpty()) {
                val loadingDesc = stringResource(R.string.cd_loading_plans)
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .semantics { contentDescription = loadingDesc },
                    color = PaywallPrimary
                )
                Text(stringResource(R.string.loading_plans), style = MaterialTheme.typography.bodyMedium)
            } else {
                uiState.productDetailsList.forEach { product ->
                    val offer = product.subscriptionOfferDetails?.firstOrNull()
                    val price = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "..."
                    val isYearly = product.productId == BillingManager.SUBSCRIPTION_ID_YEARLY
                    
                    val isSelected = uiState.selectedProduct == product
                    
                    PremiumSubscriptionCard(
                        title = if (isYearly) stringResource(R.string.plan_yearly_best_value) else stringResource(R.string.plan_monthly),
                        price = price,
                        subtitle = if (isYearly) {
                            val monthly = (price.filter { it.isDigit() || it == '.' }.toFloatOrNull() ?: 0f) / 12
                            stringResource(R.string.price_per_month, String.format("%.2f", monthly))
                        } else stringResource(R.string.cancel_anytime),
                        isSelected = isSelected,
                        isBestValue = isYearly,
                        onSelect = {
                            viewModel.selectProduct(product, offer?.offerToken ?: "")
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium CTA Button with gradient
            Button(
                onClick = {
                    if (activity != null) {
                        viewModel.launchBillingFlow(activity)
                    }
                },
                enabled = uiState.selectedProduct != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(58.dp)
                    .scale(if (uiState.selectedProduct != null) pulseScale else 1f),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (uiState.selectedProduct != null) {
                                Brush.horizontalGradient(
                                    colors = listOf(PaywallPrimary, PaywallPrimaryLight)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.start_premium_now),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.selectedProduct != null) Color.White 
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            TextButton(onClick = { viewModel.restorePurchases() }) {
                Text(
                    stringResource(R.string.restore_purchases), 
                    color = PaywallPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.terms_of_service),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { openUrl(context, "https://calview.ai/terms") }
                        .semantics { role = Role.Button }
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.privacy_policy),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { openUrl(context, "https://calview.ai/privacy") }
                        .semantics { role = Role.Button }
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(text: String) {
    val featureDescription = stringResource(R.string.cd_feature_prefix, text)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = featureDescription
        }
    ) {
        // SageGreen checkmark circle
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(PaywallPrimary, PaywallPrimaryLight)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontFamily = InterFontFamily,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PremiumSubscriptionCard(
    title: String,
    price: String,
    subtitle: String,
    isSelected: Boolean,
    isBestValue: Boolean = false,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) PaywallCardBorder else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) PaywallCardSelected else MaterialTheme.colorScheme.surface
    
    val selectedDescription = if (isSelected) 
        stringResource(R.string.cd_subscription_option_selected, title, price, subtitle) 
    else 
        stringResource(R.string.cd_subscription_option_not_selected, title, price, subtitle)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.RadioButton
                    contentDescription = selectedDescription
                }
                .clickable(onClick = onSelect)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(PaywallPrimary, PaywallPrimaryLight)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Modifier.border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontFamily = InterFontFamily,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) PaywallPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isBestValue) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PaywallAccent
                            ) {
                                Text(
                                    text = "SAVE 50%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                // Price with premium Space Grotesk
                Text(
                    text = price,
                    fontFamily = SpaceGroteskFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.02).sp,
                    color = if (isSelected) PaywallPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Preview for individual components
@Preview(showBackground = true)
@Composable
private fun PremiumFeatureItemPreview() {
    CalViewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumFeatureItem("Unlimited AI Food Scans")
            PremiumFeatureItem("Advanced Macronutrient Breakdown")
            PremiumFeatureItem("No Ads")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PremiumSubscriptionCardPreview() {
    CalViewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PremiumSubscriptionCard(
                title = "Yearly",
                price = "$99.99",
                subtitle = "Just $8.33 / month",
                isSelected = true,
                isBestValue = true,
                onSelect = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            PremiumSubscriptionCard(
                title = "Monthly",
                price = "$9.99",
                subtitle = "Cancel anytime",
                isSelected = false,
                isBestValue = false,
                onSelect = {}
            )
        }
    }
}
