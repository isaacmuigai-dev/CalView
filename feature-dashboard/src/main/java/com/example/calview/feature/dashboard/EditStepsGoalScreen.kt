package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter

/**
 * Edit Step Goal screen matching the design with:
 * - Previous goal card with footprint icon
 * - Text input field for new step goal
 * - Revert and Done buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStepsGoalScreen(
    currentStepsGoal: Int = 10000,
    onBack: () -> Unit,
    onSave: (Int) -> Unit
) {
    var stepsInput by remember { mutableStateOf(currentStepsGoal.toString()) }
    val previousGoal = remember { currentStepsGoal }
    
    // Parse input to int, default to previous if invalid
    val parsedSteps = stepsInput.toIntOrNull() ?: 0
    val isValid = parsedSteps >= 1000 && parsedSteps <= 100000
    val hasChanged = parsedSteps != previousGoal
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = "Edit Step Goal",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Previous goal card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F8F8),
                border = BorderStroke(1.dp, Color(0xFFE5E5E5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Footprint icon with circular progress indicator
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Progress ring
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(56.dp),
                            color = Color(0xFF1C1C1E),
                            strokeWidth = 3.dp,
                            trackColor = Color(0xFFE5E5E5)
                        )
                        // Footprint icon
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = "Steps",
                            tint = Color(0xFF1C1C1E),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = parsedSteps.toString(),
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Previous goal $previousGoal steps",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Step Goal input field
            OutlinedTextField(
                value = stepsInput,
                onValueChange = { newValue ->
                    // Only allow numeric input
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        stepsInput = newValue
                    }
                },
                label = {
                    Text(
                        text = "Daily Step Goal",
                        fontFamily = Inter
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1C1C1E),
                    unfocusedBorderColor = Color(0xFFE5E5E5),
                    focusedLabelColor = Color(0xFF1C1C1E),
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Revert button
                OutlinedButton(
                    onClick = { stepsInput = previousGoal.toString() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Revert",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                
                // Done button
                Button(
                    onClick = { 
                        if (isValid) {
                            onSave(parsedSteps)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    enabled = isValid && hasChanged,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1C1C1E),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE5E5E5),
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text(
                        text = "Done",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
