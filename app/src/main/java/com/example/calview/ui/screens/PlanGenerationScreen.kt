package com.example.calview.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.ui.screens.components.OnboardingTemplate

@Composable
fun PlanGenerationScreen(
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    OnboardingTemplate(
        title = "", // Empty title as per screenshot
        progress = 1.0f,
        onBack = onBack,
        onContinue = onContinue
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustration Container
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE0E0FF).copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Heart Finger Icon (simplified placeholder)
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFD64D50)
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFE5A87B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "All done!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Time to generate your\ncustom plan!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                color = Color.Black,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanGenerationScreenPreview() {
    PlanGenerationScreen(
        onContinue = {},
        onBack = {}
    )
}
