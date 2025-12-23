package com.example.calview.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.ui.components.CalAICard
import com.example.calview.ui.components.CalorieRing
import com.example.calview.ui.screens.components.OnboardingTemplate
import com.example.calview.ui.viewmodels.OnboardingViewModel

@Composable
fun CustomPlanReadyScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    OnboardingTemplate(
        title = "",
        showBottomBar = true,
        onContinue = onContinue,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF1C1C1E),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Congratulations\nyour custom plan is ready!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "You should Maintain:", fontSize = 18.sp, color = Color.Black, fontFamily = FontFamily.SansSerif)

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
            ) {
                Text(
                    text = "${uiState.weight} lbs",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            CalAICard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Recommendation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "You can edit this any time",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.SansSerif
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        RecommendationRing(
                            label = "Calories",
                            value = "${uiState.recommendedCalories}",
                            icon = Icons.Filled.LocalFireDepartment,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        RecommendationRing(
                            label = "Carbs",
                            value = "${uiState.recommendedCarbs}g",
                            icon = Icons.Filled.Grass,
                            color = Color(0xFFE5A87B),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        RecommendationRing(
                            label = "Protein",
                            value = "${uiState.recommendedProtein}g",
                            icon = Icons.Filled.Egg,
                            color = Color(0xFFD64D50),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        RecommendationRing(
                            label = "Fats",
                            value = "${uiState.recommendedFats}g",
                            icon = Icons.Filled.Eco,
                            color = Color(0xFF6A8FB3),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationRing(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.SansSerif)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CalorieRing(
                    consumed = 1,
                    goal = 1,
                )
                Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.SansSerif)

                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp),
                    tint = Color.Black
                )
            }
        }
    }
}
