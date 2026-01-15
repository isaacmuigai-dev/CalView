package com.example.calview.feature.scanner

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.calview.core.data.local.AnalysisStatus
import com.example.calview.core.data.local.MealEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.calview.feature.scanner.R
import androidx.compose.ui.res.stringResource

// Color palette
private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val AccentCyan = Color(0xFF00D4AA)
private val DarkText = Color(0xFF1F2937)
private val MutedText = Color(0xFF6B7280)
private val CardBg = Color(0xFFF9FAFB)
private val SurfaceBg = Color.White

/**
 * My Meals Screen - Simple, minimalistic design
 * Features: View saved meals, create custom meals, quick scan access
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMealsScreen(
    meals: List<MealEntity>,
    onBack: () -> Unit,
    onScanFood: () -> Unit,
    onMealClick: (MealEntity) -> Unit = {},
    onCreateMeal: (name: String, calories: Int, protein: Int, carbs: Int, fats: Int) -> Unit,
    onDeleteMeal: (Long) -> Unit
) {
    var showCreateMealDialog by remember { mutableStateOf(false) }
    var mealToDelete by remember { mutableStateOf<MealEntity?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxSize()
                .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = stringResource(R.string.my_meals_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions
                item {
                    Text(
                        text = stringResource(R.string.quick_actions_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Scan Food Button
                        QuickActionCard(
                            icon = Icons.Filled.CameraAlt,
                            title = stringResource(R.string.scan_food_title),
                            subtitle = stringResource(R.string.scan_food_subtitle),
                            color = GradientStart,
                            onClick = onScanFood,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Create Meal Button
                        QuickActionCard(
                            icon = Icons.Filled.Add,
                            title = stringResource(R.string.create_meal_title),
                            subtitle = stringResource(R.string.create_meal_subtitle),
                            color = AccentCyan,
                            onClick = { showCreateMealDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Saved Meals Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.your_meals_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${meals.size} ${stringResource(R.string.meals_count_suffix)}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Meals List or Empty State
                if (meals.isEmpty()) {
                    item {
                        EmptyMealsState()
                    }
                } else {
                    items(
                        items = meals.sortedByDescending { it.timestamp },
                        key = { it.id }
                    ) { meal ->
                        MealCard(
                            meal = meal,
                            onClick = { onMealClick(meal) },
                            onDelete = { mealToDelete = meal }
                        )
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
        
        // Create Meal Dialog
        if (showCreateMealDialog) {
            CreateMealDialog(
                onDismiss = { showCreateMealDialog = false },
                onCreate = { name, calories, protein, carbs, fats ->
                    onCreateMeal(name, calories, protein, carbs, fats)
                    showCreateMealDialog = false
                }
            )
        }
        
        // Delete Confirmation Dialog
        mealToDelete?.let { meal ->
            AlertDialog(
                onDismissRequest = { mealToDelete = null },
                title = { Text(stringResource(R.string.delete_meal_title)) },
                text = { Text(stringResource(R.string.delete_meal_confirm, meal.name)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteMeal(meal.id)
                            mealToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text(stringResource(R.string.delete_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mealToDelete = null }) {
                        Text(stringResource(R.string.cancel_action))
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MealCard(
    meal: MealEntity,
    onClick: () -> Unit = {},
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(meal.timestamp))
    
    val isAnalyzing = meal.analysisStatus == AnalysisStatus.ANALYZING ||
                      meal.analysisStatus == AnalysisStatus.PENDING
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAnalyzing) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Image - Larger to match RecentMealCard (100dp)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (meal.imagePath != null && File(meal.imagePath).exists()) {
                    AsyncImage(
                        model = File(meal.imagePath ?: ""),
                        contentDescription = meal.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(GradientStart, GradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = "Food placeholder",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Analyzing overlay
                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Meal Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = meal.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Status or date
                        if (isAnalyzing) {
                            Text(
                                text = stringResource(R.string.analyzing_status),
                                fontSize = 12.sp,
                                color = GradientStart,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = dateString,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Delete Icon Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_meal_content_desc),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calories row with fire icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFF6B35)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${meal.calories} cal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Macros row
                if (meal.protein > 0 || meal.carbs > 0 || meal.fats > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MacroItem("P", "${meal.protein}g", Color(0xFFEF4444))
                        MacroItem("C", "${meal.carbs}g", Color(0xFFF59E0B))
                        MacroItem("F", "${meal.fats}g", Color(0xFF3B82F6))
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroItem(
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label $value",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyMealsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🍽️", fontSize = 64.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.no_meals_yet),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.no_meals_desc),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateMealDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, calories: Int, protein: Int, carbs: Int, fats: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    
    val isValid = name.isNotBlank() && calories.isNotBlank() && (calories.toIntOrNull() ?: 0) > 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(R.string.create_custom_meal_title),
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.meal_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.calories_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Text("🔥") }
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.protein_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        suffix = { Text("g") }
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.carbs_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        suffix = { Text("g") }
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.fats_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        suffix = { Text("g") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        name,
                        calories.toIntOrNull() ?: 0,
                        protein.toIntOrNull() ?: 0,
                        carbs.toIntOrNull() ?: 0,
                        fats.toIntOrNull() ?: 0
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text(stringResource(R.string.create_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    )
}

// Keep the old function signature for backwards compatibility during transition
@Deprecated("Use MyMealsScreen instead", ReplaceWith("MyMealsScreen"))
@Composable
fun LogFoodScreen(
    onBack: () -> Unit,
    onScanFood: () -> Unit = {},
    onFoodAdded: (Any) -> Unit = {}
) {
    // Redirect to empty state since we don't have the new parameters
    MyMealsScreen(
        meals = emptyList(),
        onBack = onBack,
        onScanFood = onScanFood,
        onCreateMeal = { _, _, _, _, _ -> },
        onDeleteMeal = {}
    )
}
