package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingTemplate
import com.example.calview.core.ui.components.CalorieRing

@Composable
fun CalorieRolloverScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    OnboardingTemplate(
        title = "Rollover extra calories to the next day?",
        progress = 0.95f,
        onBack = onBack,
        showBottomBar = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                SuggestionChip(
                    onClick = { },
                    label = { Text("Rollover up to 200 cals", color = Color(0xFF6A8FB3)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFEBF2F9)),
                    border = null,
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    RolloverCard(
                        title = "Yesterday",
                        calories = 350,
                        total = 500,
                        left = 150,
                        modifier = Modifier.weight(1f),
                        headerColor = Color(0xFFFFF0F0)
                    )
                    
                    RolloverCard(
                        title = "Today",
                        calories = 350,
                        total = 650,
                        left = 300,
                        extra = 150,
                        modifier = Modifier.weight(1.1f),
                        headerColor = Color(0xFFF9F9F9)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        viewModel.onRolloverExtraCaloriesChanged(false)
                        onNext()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("No", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Button(
                    onClick = { 
                        viewModel.onRolloverExtraCaloriesChanged(true)
                        onNext()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Yes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RolloverCard(
    title: String,
    calories: Int,
    total: Int,
    left: Int,
    modifier: Modifier = Modifier,
    extra: Int? = null,
    headerColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = if (title == "Yesterday") Color(0xFFD64D50) else Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (title == "Yesterday") Color(0xFFD64D50) else Color.Black)
                }
            }
            
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(calories.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("/$total", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                }
                
                if (extra != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFF6A8FB3), modifier = Modifier.size(12.dp))
                        Text("+$extra", fontSize = 12.sp, color = Color(0xFF6A8FB3), fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    CalorieRing(
                        consumed = calories.toFloat(),
                        total = total.toFloat(),
                        strokeWidth = 8.dp
                    )
                    Column(
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cals left", fontSize = 8.sp, color = Color.White)
                        Text(if (extra != null) "$calories + $extra" else left.toString(), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
