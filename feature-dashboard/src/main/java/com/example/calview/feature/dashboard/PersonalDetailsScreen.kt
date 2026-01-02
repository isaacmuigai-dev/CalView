package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.ui.theme.Inter

/**
 * Personal Details screen showing editable user profile information.
 * Matches the design with Goal Weight card and list of editable items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsScreen(
    viewModel: PersonalDetailsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEditGoalWeight: () -> Unit,
    onEditHeightWeight: () -> Unit,
    onEditBirthday: () -> Unit,
    onEditGender: () -> Unit,
    onEditStepsGoal: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal details",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Goal Weight Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAFAFA),
                border = BorderStroke(1.dp, Color(0xFFE5E5E5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Goal Weight",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${uiState.goalWeight.toInt()} lbs",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    }
                    
                    Button(
                        onClick = onEditGoalWeight,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1C1C1E),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Change Goal",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Details List Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E5E5))
            ) {
                Column {
                    // Current Weight
                    PersonalDetailItem(
                        label = "Current Weight",
                        value = "${uiState.currentWeight.toInt()} lbs",
                        onClick = onEditHeightWeight
                    )
                    
                    HorizontalDivider(
                        color = Color(0xFFF3F3F3),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Height
                    PersonalDetailItem(
                        label = "Height",
                        value = uiState.formattedHeight.ifEmpty { "Not set" },
                        onClick = onEditHeightWeight
                    )
                    
                    HorizontalDivider(
                        color = Color(0xFFF3F3F3),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Date of birth
                    PersonalDetailItem(
                        label = "Date of birth",
                        value = uiState.formattedBirthDate,
                        onClick = onEditBirthday
                    )
                    
                    HorizontalDivider(
                        color = Color(0xFFF3F3F3),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Gender
                    PersonalDetailItem(
                        label = "Gender",
                        value = uiState.gender.ifEmpty { "Not set" },
                        onClick = onEditGender
                    )
                    
                    HorizontalDivider(
                        color = Color(0xFFF3F3F3),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    // Daily Step Goal
                    PersonalDetailItem(
                        label = "Daily Step Goal",
                        value = "${uiState.dailyStepsGoal} steps",
                        onClick = onEditStepsGoal
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalDetailItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = Color.Black
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                fontFamily = Inter,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $label",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
