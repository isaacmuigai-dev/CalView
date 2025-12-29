package com.example.calview.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Referral source screen matching the design.
 * Scrollable list of options: TikTok, YouTube, Google, Play Store, Facebook, 
 * Friend or family, TV, Instagram, X, Other
 */
@Composable
fun ReferralSourceScreen(
    currentStep: Int = 3,
    totalSteps: Int = 3,
    selectedSource: String = "",
    onSourceSelected: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "Where did you hear about us?",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = selectedSource.isNotEmpty()
    ) {
        // Scrollable list of referral sources
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReferralOption(
                source = "TikTok",
                icon = { TikTokIcon() },
                isSelected = selectedSource == "TikTok",
                onClick = { onSourceSelected("TikTok") }
            )
            
            ReferralOption(
                source = "YouTube",
                icon = { YouTubeIcon() },
                isSelected = selectedSource == "YouTube",
                onClick = { onSourceSelected("YouTube") }
            )
            
            ReferralOption(
                source = "Google",
                icon = { GoogleIcon() },
                isSelected = selectedSource == "Google",
                onClick = { onSourceSelected("Google") }
            )
            
            ReferralOption(
                source = "Play Store",
                icon = { PlayStoreIcon() },
                isSelected = selectedSource == "Play Store",
                onClick = { onSourceSelected("Play Store") }
            )
            
            ReferralOption(
                source = "Facebook",
                icon = { FacebookIcon() },
                isSelected = selectedSource == "Facebook",
                onClick = { onSourceSelected("Facebook") }
            )
            
            ReferralOption(
                source = "Friend or family",
                icon = { FriendIcon(selected = selectedSource == "Friend or family") },
                isSelected = selectedSource == "Friend or family",
                onClick = { onSourceSelected("Friend or family") }
            )
            
            ReferralOption(
                source = "TV",
                icon = { TVIcon(selected = selectedSource == "TV") },
                isSelected = selectedSource == "TV",
                onClick = { onSourceSelected("TV") }
            )
            
            ReferralOption(
                source = "Instagram",
                icon = { InstagramIcon() },
                isSelected = selectedSource == "Instagram",
                onClick = { onSourceSelected("Instagram") }
            )
            
            ReferralOption(
                source = "X",
                icon = { XIcon(selected = selectedSource == "X") },
                isSelected = selectedSource == "X",
                onClick = { onSourceSelected("X") }
            )
            
            ReferralOption(
                source = "Other",
                icon = { OtherIcon(selected = selectedSource == "Other") },
                isSelected = selectedSource == "Other",
                onClick = { onSourceSelected("Other") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReferralOption(
    source: String,
    icon: @Composable () -> Unit,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1C1C1E) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = source,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

// Brand Icons
@Composable
private fun TikTokIcon() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black,
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("♪", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun YouTubeIcon() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFF0000),
                modifier = Modifier.size(28.dp, 20.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("▶", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun GoogleIcon() {
    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
        Text("G", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
    }
}

@Composable
private fun PlayStoreIcon() {
    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
        Text("▶", fontSize = 20.sp, color = Color(0xFF00C853))
    }
}

@Composable
private fun FacebookIcon() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1877F2),
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("f", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FriendIcon(selected: Boolean) {
    Icon(
        imageVector = Icons.Filled.Groups,
        contentDescription = "Friend or family",
        tint = if (selected) Color.White else Color.Black,
        modifier = Modifier.size(28.dp)
    )
}

@Composable
private fun TVIcon(selected: Boolean) {
    Icon(
        imageVector = Icons.Filled.Tv,
        contentDescription = "TV",
        tint = if (selected) Color.White else Color.Black,
        modifier = Modifier.size(26.dp)
    )
}

@Composable
private fun InstagramIcon() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF58529),
                            Color(0xFFDD2A7B),
                            Color(0xFF8134AF),
                            Color(0xFF515BD4)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Transparent,
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun XIcon(selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (selected) Color.White else Color.Black,
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                "𝕏",
                color = if (selected) Color.Black else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OtherIcon(selected: Boolean) {
    Icon(
        imageVector = Icons.Filled.MoreHoriz,
        contentDescription = "Other",
        tint = if (selected) Color.White else Color.Black,
        modifier = Modifier.size(26.dp)
    )
}
