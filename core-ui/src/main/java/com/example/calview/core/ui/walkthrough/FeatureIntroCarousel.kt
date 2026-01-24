package com.example.calview.core.ui.walkthrough

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FeatureItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val bgColor: Color,
    val accentColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeatureIntroCarousel(
    modifier: Modifier = Modifier
) {
    val features = listOf(
        FeatureItem(
            title = "Instant Photo Analysis",
            description = "Just snap a photo of your food. Our advanced AI identifies ingredients and estimates nutrition in seconds.",
            icon = Icons.Filled.CameraAlt,
            bgColor = Color(0xFFE8F5E9),
            accentColor = Color(0xFF2E7D32)
        ),
        FeatureItem(
            title = "Personalized AI Coach",
            description = "Get real-time advice on your macros, meal suggestions, and encouragement to stay on track with your goals.",
            icon = Icons.Filled.AutoAwesome,
            bgColor = Color(0xFFE3F2FD),
            accentColor = Color(0xFF1565C0)
        ),
        FeatureItem(
            title = "Smart Streak Tracking",
            description = "Build a healthy habit. Use Streak Freezes on busy days to keep your momentum going without stress.",
            icon = Icons.Filled.Whatshot,
            bgColor = Color(0xFFFFF3E0),
            accentColor = Color(0xFFEF6C00)
        )
    )

    val pagerState = rememberPagerState(pageCount = { features.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) { page ->
            val feature = features[page]
            FeatureSlide(feature)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pager Indicators
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(features.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureSlide(feature: FeatureItem) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = feature.bgColor.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = feature.bgColor,
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = feature.accentColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = feature.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = feature.accentColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.description,
                fontSize = 15.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
