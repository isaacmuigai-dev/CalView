package com.example.calview.feature.onboarding

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash Screen - Displays app logo and name for 2-3 seconds
 * before automatically navigating to the welcome screen.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "fade"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        android.util.Log.d("SplashScreen", "Splash started, waiting 2.5s...")
        delay(2500L) // 2.5 seconds
        android.util.Log.d("SplashScreen", "Splash timeout reached, calling onTimeout")
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
            .semantics { contentDescription = "CalViewAI splash screen. Loading app." },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            // Row with Icon and App Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Theme-aware logo without tinting
                // Theme-aware logo without tinting
                // Use background luminance to determine actual theme (user pref or system)
                val backgroundColor = MaterialTheme.colorScheme.background
                val isDarkTheme = backgroundColor.luminance() < 0.5f
                
                // Light Mode -> Black Logo, Dark Mode -> White Logo
                val iconRes = if (isDarkTheme) {
                    com.example.calview.core.ui.R.drawable.app_logo_white
                } else {
                    com.example.calview.core.ui.R.drawable.app_logo_black
                }
                
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "CalViewAI Icon",
                    modifier = Modifier.fillMaxWidth(0.16f)
                        .size(90.dp),
                    contentScale = ContentScale.Crop
                )
                
                // App Name
                Text(
                    text = "CalViewAI",
                    fontFamily = com.example.calview.core.ui.theme.BrandingFontFamily,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp,
                    color = if (isDarkTheme) Color.White else Color(0xFF000000)
                )
            }
        }
    }
}
