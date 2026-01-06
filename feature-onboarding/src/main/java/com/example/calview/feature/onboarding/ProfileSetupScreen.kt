package com.example.calview.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
 * Consolidated Profile Setup Screen
 * Collects: Gender, Birthdate, Height, Weight, Activity Level
 */
@Composable
fun ProfileSetupScreen(
    currentStep: Int,
    totalSteps: Int,
    // Gender
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    // Birthdate
    birthMonth: String,
    birthDay: Int,
    birthYear: Int,
    onMonthChanged: (String) -> Unit,
    onDayChanged: (Int) -> Unit,
    onYearChanged: (Int) -> Unit,
    // Height & Weight (Metric only - kg/cm)
    heightCm: Int,
    weightKg: Int,
    onHeightCmChanged: (Int) -> Unit,
    onWeightKgChanged: (Int) -> Unit,
    // Activity Level
    selectedWorkouts: String,
    onWorkoutsSelected: (String) -> Unit,
    // Navigation
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isComplete = selectedGender.isNotEmpty() && selectedWorkouts.isNotEmpty()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()  // Ensure buttons don't overlap navigation bar
    ) {
        // Top bar with back button and progress
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = "Set up your profile",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Tell us about yourself to personalize your experience",
                fontFamily = Inter,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // ===================== GENDER SECTION =====================
            SectionTitle(title = "Gender", emoji = "👤")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GenderChip(
                    label = "Male",
                    emoji = "👨",
                    isSelected = selectedGender == "Male",
                    onClick = { onGenderSelected("Male") },
                    modifier = Modifier.weight(1f)
                )
                GenderChip(
                    label = "Female",
                    emoji = "👩",
                    isSelected = selectedGender == "Female",
                    onClick = { onGenderSelected("Female") },
                    modifier = Modifier.weight(1f)
                )
                GenderChip(
                    label = "Other",
                    emoji = "🧑",
                    isSelected = selectedGender == "Other",
                    onClick = { onGenderSelected("Other") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== BIRTHDATE SECTION =====================
            SectionTitle(title = "Birthdate", emoji = "🎂")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Month dropdown
                DropdownSelector(
                    label = "Month",
                    value = birthMonth,
                    options = listOf("January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"),
                    onValueSelected = onMonthChanged,
                    modifier = Modifier.weight(1.5f)
                )
                
                // Day dropdown
                DropdownSelector(
                    label = "Day",
                    value = birthDay.toString(),
                    options = (1..31).map { it.toString() },
                    onValueSelected = { onDayChanged(it.toInt()) },
                    modifier = Modifier.weight(1f)
                )
                
                // Year dropdown
                DropdownSelector(
                    label = "Year",
                    value = birthYear.toString(),
                    options = (1940..2010).map { it.toString() },
                    onValueSelected = { onYearChanged(it.toInt()) },
                    modifier = Modifier.weight(1.2f)  // Wider for 4-digit year
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== HEIGHT & WEIGHT SECTION =====================
            SectionTitle(title = "Height & Weight", emoji = "📏")
            
            // Metric inputs - Dropdown selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DropdownSelector(
                    label = "Height",
                    value = "$heightCm cm",
                    options = (100..220).map { "$it cm" },
                    onValueSelected = { onHeightCmChanged(it.replace(" cm", "").toInt()) },
                    modifier = Modifier.weight(1f)
                )
                DropdownSelector(
                    label = "Weight",
                    value = "$weightKg kg",
                    options = (30..200).map { "$it kg" },
                    onValueSelected = { onWeightKgChanged(it.replace(" kg", "").toInt()) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // ===================== ACTIVITY LEVEL SECTION =====================
            SectionTitle(title = "Activity Level", emoji = "🏃")
            
            Text(
                text = "How many workouts do you do per week?",
                fontFamily = Inter,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivityOption(
                    label = "0-2 workouts",
                    description = "Little to no exercise",
                    isSelected = selectedWorkouts == "0-2",
                    onClick = { onWorkoutsSelected("0-2") }
                )
                ActivityOption(
                    label = "3-5 workouts",
                    description = "Moderate exercise",
                    isSelected = selectedWorkouts == "3-5",
                    onClick = { onWorkoutsSelected("3-5") }
                )
                ActivityOption(
                    label = "6+ workouts",
                    description = "Very active",
                    isSelected = selectedWorkouts == "6+",
                    onClick = { onWorkoutsSelected("6+") }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Continue button
        Button(
            onClick = onContinue,
            enabled = isComplete,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = "Continue",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String, emoji: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun GenderChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            ),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitToggleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun NumberInputField(
    label: String,
    value: Int,
    suffix: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = Inter,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        OutlinedTextField(
            value = value.toString(),
            onValueChange = { newValue ->
                newValue.toIntOrNull()?.let { onValueChange(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            suffix = { Text(suffix, color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1C1C1E),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            singleLine = true
        )
    }
}

@Composable
private fun ActivityOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            ),
        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
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
                    text = label,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
