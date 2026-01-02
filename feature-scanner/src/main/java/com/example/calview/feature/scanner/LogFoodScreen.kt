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
    onCreateMeal: (name: String, calories: Int, protein: Int, carbs: Int, fats: Int) -> Unit,
    onDeleteMeal: (Long) -> Unit
) {
    var showCreateMealDialog by remember { mutableStateOf(false) }
    var mealToDelete by remember { mutableStateOf<MealEntity?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceBg)
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
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "My Meals",
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
                        text = "Quick Actions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Scan Food Button
                        QuickActionCard(
                            icon = Icons.Filled.CameraAlt,
                            title = "Scan Food",
                            subtitle = "Take a photo",
                            color = GradientStart,
                            onClick = onScanFood,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Create Meal Button
                        QuickActionCard(
                            icon = Icons.Filled.Add,
                            title = "Create Meal",
                            subtitle = "Add manually",
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
                            text = "Your Meals",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText
                        )
                        Text(
                            text = "${meals.size} meals",
                            fontSize = 13.sp,
                            color = MutedText
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
                title = { Text("Delete Meal?") },
                text = { Text("Are you sure you want to delete \"${meal.name}\"? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteMeal(meal.id)
                            mealToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mealToDelete = null }) {
                        Text("Cancel")
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
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(meal.timestamp))
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Image or Placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GradientStart.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (meal.imagePath != null && File(meal.imagePath).exists()) {
                    AsyncImage(
                        model = File(meal.imagePath),
                        contentDescription = meal.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = "🍽️", fontSize = 28.sp)
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
                            color = DarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Status badge for analyzing meals
                        if (meal.analysisStatus == AnalysisStatus.ANALYZING) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = GradientStart
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Analyzing...",
                                    fontSize = 12.sp,
                                    color = GradientStart
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Calories and date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 ${meal.calories} cal",
                        fontSize = 13.sp,
                        color = MutedText
                    )
                    Text(
                        text = " • $dateString",
                        fontSize = 12.sp,
                        color = MutedText.copy(alpha = 0.7f)
                    )
                }
                
                // Macros
                if (meal.protein > 0 || meal.carbs > 0 || meal.fats > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroChip("P", "${meal.protein}g", Color(0xFFEF4444))
                        MacroChip("C", "${meal.carbs}g", Color(0xFFF59E0B))
                        MacroChip("F", "${meal.fats}g", Color(0xFF3B82F6))
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = value,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
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
            text = "No meals yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkText
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Scan your food or create a custom meal",
            fontSize = 14.sp,
            color = MutedText
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
                "Create Custom Meal",
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
                    label = { Text("Meal Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text("Calories *") },
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
                        label = { Text("Protein") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        suffix = { Text("g") }
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { c -> c.isDigit() } },
                        label = { Text("Carbs") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        suffix = { Text("g") }
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it.filter { c -> c.isDigit() } },
                        label = { Text("Fats") },
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
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
