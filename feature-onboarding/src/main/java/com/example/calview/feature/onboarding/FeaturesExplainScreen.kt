package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.walkthrough.FeatureIntroCarousel
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

@Composable
fun FeaturesExplainScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = 8,
        totalSteps = 10,
        title = "Welcome to CalView AI",
        subtitle = "Master your nutrition with the power of artificial intelligence.",
        onBack = onBack,
        onContinue = onContinue
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Carousel of features
        FeatureIntroCarousel(
            modifier = Modifier.weight(1f)
        )
    }
}
