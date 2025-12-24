package com.example.calview.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.calview.core.ui.components.CalAIButton
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingTemplate

@Composable
fun CaloriesBurnedSettingScreen(
    viewModel: OnboardingViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    OnboardingTemplate(
        title = "Add calories burned back to your daily goal?",
        progress = 0.85f,
        onBack = onBack,
        showBottomBar = false // Custom bottom bar below
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.LightGray)
            ) {
                // Background Image Placeholder
                Icon(
                    Icons.Default.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(200.dp).align(Alignment.Center).graphicsLayer(alpha = 0.1f),
                    tint = Color.Black
                )
                
                // Goal Card Overlay
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .width(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Today's goal", fontSize = 14.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("500 Cals", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color(0xFFF3F3F3), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Running", fontSize = 12.sp, color = Color.Gray)
                                Text("+100 cals", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6A9B7E))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { 
                        viewModel.onAddCaloriesBackChanged(false)
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
                        viewModel.onAddCaloriesBackChanged(true)
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
