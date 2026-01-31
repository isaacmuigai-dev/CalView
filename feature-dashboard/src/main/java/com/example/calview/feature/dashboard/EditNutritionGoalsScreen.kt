package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource

/**
 * Edit Nutrition Goals screen - allows users to edit calorie and macro goals.
 * Shows progress rings for each nutrient with expandable micronutrients section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNutritionGoalsScreen(
    currentCalories: Int = 1730,
    currentProtein: Int = 116,
    currentCarbs: Int = 207,
    currentFats: Int = 48,
    currentFiber: Int = 38,
    currentSugar: Int = 65,
    currentSodium: Int = 2300,
    onBack: () -> Unit,
    onSave: (calories: Int, protein: Int, carbs: Int, fats: Int, fiber: Int, sugar: Int, sodium: Int) -> Unit,
    onAutoGenerate: () -> Unit = {}
) {
    var calories by remember { mutableIntStateOf(currentCalories) }
    var protein by remember { mutableIntStateOf(currentProtein) }
    var carbs by remember { mutableIntStateOf(currentCarbs) }
    var fats by remember { mutableIntStateOf(currentFats) }
    var fiber by remember { mutableIntStateOf(currentFiber) }
    var sugar by remember { mutableIntStateOf(currentSugar) }
    var sodium by remember { mutableIntStateOf(currentSodium) }
    

    var editingField by remember { mutableStateOf<String?>(null) }
    var editValue by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.edit_nutrition_goals_title),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Calorie goal - with slider
            NutrientGoalCard(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = MaterialTheme.colorScheme.onBackground,
                progressColor = MaterialTheme.colorScheme.onBackground,
                label = stringResource(R.string.calorie_goal),
                value = calories,
                unit = "",
                progress = 1f,
                showSlider = true,
                minValue = 1000,
                maxValue = 4000,
                onValueChange = { calories = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Protein goal
            NutrientGoalCard(
                icon = Icons.Default.Favorite,
                iconTint = Color(0xFFE57373),
                progressColor = Color(0xFFE57373),
                label = stringResource(R.string.protein_goal),
                value = protein,
                unit = "g",
                progress = 0.7f,
                onValueChange = { protein = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Carb goal
            NutrientGoalCard(
                icon = Icons.Default.Grass,
                iconTint = Color(0xFFFFB74D),
                progressColor = Color(0xFFFFB74D),
                label = stringResource(R.string.carb_goal),
                value = carbs,
                unit = "g",
                progress = 0.8f,
                onValueChange = { carbs = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fat goal
            NutrientGoalCard(
                icon = Icons.Default.Opacity,
                iconTint = Color(0xFF64B5F6),
                progressColor = Color(0xFF64B5F6),
                label = stringResource(R.string.fat_goal),
                value = fats,
                unit = "g",
                progress = 0.6f,
                onValueChange = { fats = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            

            
            Spacer(modifier = Modifier.weight(1f))
            
            // Auto Generate Goals button
            OutlinedButton(
                onClick = onAutoGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = stringResource(R.string.auto_generate_goals),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Save Changes button
            Button(
                onClick = { onSave(calories, protein, carbs, fats, fiber, sugar, sodium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = stringResource(R.string.save_changes_button),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NutrientGoalCard(
    icon: ImageVector,
    iconTint: Color,
    progressColor: Color,
    label: String,
    value: Int,
    unit: String,
    progress: Float,
    showSlider: Boolean = false,
    minValue: Int = 0,
    maxValue: Int = 500,
    onValueChange: (Int) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(value.toString()) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress ring with icon
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColor,
                        strokeWidth = 4.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Label and value
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isEditing) {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    editText = newValue
                                    newValue.toIntOrNull()?.let { onValueChange(it) }
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(100.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.02).sp
                            )
                        )
                    } else {
                        Text(
                            text = "$value$unit",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { 
                                isEditing = true
                                editText = value.toString()
                            }
                        )
                    }
                }
                
                // Edit indicator
                if (!isEditing) {
                    IconButton(onClick = { 
                        isEditing = true
                        editText = value.toString()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_desc),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(onClick = { 
                        isEditing = false
                        editText.toIntOrNull()?.let { onValueChange(it) }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.check_desc),
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Slider for calorie goal
            if (showSlider) {
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = value.toFloat(),
                    onValueChange = { onValueChange(it.toInt()) },
                    valueRange = minValue.toFloat()..maxValue.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}
