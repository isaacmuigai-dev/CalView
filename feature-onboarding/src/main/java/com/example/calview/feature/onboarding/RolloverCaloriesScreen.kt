package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter

/**
 * Rollover extra calories to the next day screen.
 * If user selects "Yes", unused calories (up to 200) from the previous day
 * will be added to the next day's calorie allowance in the dashboard.
 * 
 * Example: Daily goal is 500 cals. User only ate 350 cals yesterday.
 * Remaining 150 cals (up to max 200) rolls over to today.
 * Today's new goal: 500 + 150 = 650 cals
 */
@Composable
fun RolloverCaloriesScreen(
    currentStep: Int,
    totalSteps: Int,
    rolloverEnabled: Boolean = false,
    onChoice: (Boolean) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top bar with back button and progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF1C1C1E),
                trackColor = Color(0xFFE5E5E5)
            )
            
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = "Rollover extra calories to the next day?",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.Black,
            lineHeight = 36.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Text(
                text = "Rollover up to 200 cals",
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF5C6BC0),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Visualization cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Yesterday card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF0F0),
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Yesterday",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFFE57373)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Calories display
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "350",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "/500",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Calories left badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1C1C1E)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cals left\n150",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress ring placeholder
                    CircularProgressIndicator(
                        progress = { 0.7f },
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFF1C1C1E),
                        strokeWidth = 4.dp,
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
            
            // Today card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Calories display
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "350",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "/650",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Rollover indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⏰", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+150",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color(0xFF5C6BC0)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Calories left badge with rollover
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1C1C1E)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cals left\n150 + ",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = "150",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF5C6BC0)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Progress ring
                    CircularProgressIndicator(
                        progress = { 0.54f },
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFF1C1C1E),
                        strokeWidth = 4.dp,
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // No / Yes buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // No button
            Button(
                onClick = { 
                    onChoice(false)
                    onContinue()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Text(
                    text = "No",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            
            // Yes button
            Button(
                onClick = { 
                    onChoice(true)
                    onContinue()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E)
                )
            ) {
                Text(
                    text = "Yes",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
