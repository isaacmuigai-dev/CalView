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
    
    var showMicronutrients by remember { mutableStateOf(false) }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Title
            Text(
                text = "Edit nutrition goals",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Calorie goal - with slider
            NutrientGoalCard(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFF1C1C1E),
                progressColor = Color(0xFF1C1C1E),
                label = "Calorie goal",
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
                label = "Protein goal",
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
                label = "Carb goal",
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
                label = "Fat goal",
                value = fats,
                unit = "g",
                progress = 0.6f,
                onValueChange = { fats = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View micronutrients toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMicronutrients = !showMicronutrients }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View micronutrients",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (showMicronutrients) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Micronutrients section (expandable)
            if (showMicronutrients) {
                Spacer(modifier = Modifier.height(8.dp))
                
                NutrientGoalCard(
                    icon = Icons.Default.Grain,
                    iconTint = Color(0xFF9575CD),
                    progressColor = Color(0xFF9575CD),
                    label = "Fiber goal",
                    value = fiber,
                    unit = "g",
                    progress = 0.5f,
                    onValueChange = { fiber = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                NutrientGoalCard(
                    icon = Icons.Default.Cake,
                    iconTint = Color(0xFFEC407A),
                    progressColor = Color(0xFFEC407A),
                    label = "Sugar goal",
                    value = sugar,
                    unit = "g",
                    progress = 0.4f,
                    onValueChange = { sugar = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                NutrientGoalCard(
                    icon = Icons.Default.WaterDrop,
                    iconTint = Color(0xFFFFB74D),
                    progressColor = Color(0xFFFFB74D),
                    label = "Sodium goal",
                    value = sodium,
                    unit = "mg",
                    progress = 0.3f,
                    onValueChange = { sodium = it }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Auto Generate Goals button
            OutlinedButton(
                onClick = onAutoGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Auto Generate Goals",
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
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E5E5))
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
                        trackColor = Color(0xFFF5F5F5)
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
                        fontFamily = Inter,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color.Gray
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
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    } else {
                        Text(
                            text = "$value$unit",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black,
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
                            contentDescription = "Edit",
                            tint = Color.Gray,
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
                            contentDescription = "Done",
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
                        thumbColor = Color(0xFF1C1C1E),
                        activeTrackColor = Color(0xFF1C1C1E),
                        inactiveTrackColor = Color(0xFFE5E5E5)
                    )
                )
            }
        }
    }
}
