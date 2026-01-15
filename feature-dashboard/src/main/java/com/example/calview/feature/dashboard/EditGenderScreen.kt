package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource

/**
 * Set Gender screen with Male, Female, Other selection buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGenderScreen(
    currentGender: String = "",
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedGender by remember { mutableStateOf(currentGender) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.set_gender_title),
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            Spacer(modifier = Modifier.weight(0.3f))
            
            // Gender options
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GenderOptionButton(
                    label = stringResource(R.string.gender_male),
                    isSelected = selectedGender == "Male",
                    onClick = { selectedGender = "Male" }
                )
                
                GenderOptionButton(
                    label = stringResource(R.string.gender_female),
                    isSelected = selectedGender == "Female",
                    onClick = { selectedGender = "Female" }
                )
                
                GenderOptionButton(
                    label = stringResource(R.string.gender_other),
                    isSelected = selectedGender == "Other",
                    onClick = { selectedGender = "Other" }
                )
            }
            
            Spacer(modifier = Modifier.weight(0.7f))
            
            // Save button
            Button(
                onClick = { onSave(selectedGender) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = selectedGender.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE5E5E5),
                    disabledContentColor = Color.Gray
                )
            ) {
                Text(
                    text = stringResource(R.string.save_changes_button),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GenderOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1C1C1E) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}
