package com.example.calview.feature.onboarding

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
import com.example.calview.core.ui.theme.Inter
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
        delay(2500L) // 2.5 seconds
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
                // New Vector Icon - theme aware
                val iconRes = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                    com.example.calview.core.ui.R.drawable.ic_vector_icon_white
                } else {
                    com.example.calview.core.ui.R.drawable.ic_vector_icon_black
                }
                
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "CalViewAI Icon",
                    modifier = Modifier.fillMaxWidth(0.2f)
                        .size(100.dp),
                    contentScale = ContentScale.Crop
                )
                
                // App Name
                Text(
                    text = "CalViewAI",
                    fontFamily = Inter,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
