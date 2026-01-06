package com.example.calview.feature.dashboard

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calview.core.data.local.MealEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Color palette
private val GradientCyan = Color(0xFF00D4AA)
private val GradientPurple = Color(0xFF7C3AED)
private val GradientPink = Color(0xFFEC4899)
private val GradientBlue = Color(0xFF3B82F6)
private val GradientOrange = Color(0xFFF59E0B)
private val DarkText = Color(0xFF1F2937)
private val MutedText = Color(0xFF6B7280)

/**
 * Food Detail Screen - Shows full food details with share/save functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    meal: MealEntity,
    onBack: () -> Unit,
    onDelete: (MealEntity) -> Unit = {},
    onUpdate: (MealEntity) -> Unit = {},
    onRecalibrate: suspend (MealEntity, List<String>, Int) -> MealEntity? = { _, _, _ -> null }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Dark theme support
    val isDarkTheme = !androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var servingCount by remember { mutableIntStateOf(1) }
    var isRecalibrating by remember { mutableStateOf(false) }
    
    // Track modifications
    var additionalIngredients by remember { mutableStateOf(listOf<String>()) }
    var currentMeal by remember { mutableStateOf(meal) }
    val hasChanges by remember(servingCount, additionalIngredients, currentMeal) {
        derivedStateOf { 
            servingCount != 1 || additionalIngredients.isNotEmpty() || currentMeal != meal 
        }
    }
    
    // Calculate health score (simplified - based on macro balance)
    val healthScore = remember(currentMeal) {
        val totalMacros = currentMeal.protein + currentMeal.carbs + currentMeal.fats
        if (totalMacros > 0) {
            val proteinRatio = currentMeal.protein.toFloat() / totalMacros
            val balance = minOf(proteinRatio * 10, 10f)
            (balance * 0.4f + 4f).toInt().coerceIn(1, 10)
        } else 4
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Full Image with overlay header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                // Food Image
                if (meal.imagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(meal.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = meal.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE8EAF6), Color(0xFFC5CAE9))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFF7986CB)
                        )
                    }
                }
                
                // Gradient overlay at top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
                
                // Header with back button, title, and actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "Food Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share button
                        IconButton(
                            onClick = { showShareSheet = true },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                        
                        // Options button
                        Box {
                            IconButton(
                                onClick = { showOptionsMenu = true },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White
                                )
                            }
                            
                            // Options dropdown
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Report Food") },
                                    leadingIcon = { Icon(Icons.Outlined.Flag, null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        Toast.makeText(context, "Report submitted", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Save Image") },
                                    leadingIcon = { Icon(Icons.Outlined.Download, null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        scope.launch {
                                            saveImageToGallery(context, meal)
                                        }
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Delete Food", color = Color(0xFFEF4444)) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444)) },
                                    onClick = {
                                        showOptionsMenu = false
                                        onDelete(meal)
                                        onBack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Scrollable content sheet
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-24).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Time and bookmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MutedText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatMealTimeDetail(meal.timestamp),
                            fontSize = 14.sp,
                            color = MutedText
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Food name and serving controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = meal.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Serving counter
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        color = Color.White
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = { if (servingCount > 1) servingCount-- },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = servingCount.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { servingCount++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Nutrition Cards Pager
                val pagerState = rememberPagerState(pageCount = { 2 })
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    when (page) {
                        0 -> {
                            // Page 1: Calories and Main Macros
                            Column {
                                // Calories Card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color(0xFFF3F4F6), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.LocalFireDepartment,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = DarkText
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Calories",
                                                fontSize = 14.sp,
                                                color = MutedText
                                            )
                                            Text(
                                                text = "${meal.calories * servingCount}",
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkText
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Macro Cards Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MacroDetailCard(
                                        label = "Protein",
                                        value = "${meal.protein * servingCount}g",
                                        icon = Icons.Filled.Favorite,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailCard(
                                        label = "Carbs",
                                        value = "${meal.carbs * servingCount}g",
                                        icon = Icons.Filled.Eco,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailCard(
                                        label = "Fats",
                                        value = "${meal.fats * servingCount}g",
                                        icon = Icons.Filled.WaterDrop,
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Page 2: Micronutrients and Health Score
                            Column {
                                // Micro Cards Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    MicroDetailCard(
                                        label = "Fiber",
                                        value = "${meal.fiber * servingCount}g",
                                        emoji = "🌾",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MicroDetailCard(
                                        label = "Sugar",
                                        value = "${meal.sugar * servingCount}g",
                                        emoji = "🍬",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MicroDetailCard(
                                        label = "Sodium",
                                        value = "${meal.sodium * servingCount}mg",
                                        emoji = "🧂",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Health Score Card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(Color(0xFFFEE2E2), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "❤️",
                                                    fontSize = 24.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Health score",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = DarkText
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                // Progress bar
                                                Box(
                                                    modifier = Modifier
                                                        .width(100.dp)
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(Color(0xFFE5E7EB))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(healthScore / 10f)
                                                            .fillMaxHeight()
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(Color(0xFF10B981), Color(0xFF34D399))
                                                                ),
                                                                RoundedCornerShape(3.dp)
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "$healthScore/10",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Page indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                .background(
                                    if (pagerState.currentPage == index) DarkText else Color(0xFFD1D5DB),
                                    CircleShape
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Ingredients Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    TextButton(onClick = { showAddIngredientDialog = true }) {
                        Text(
                            text = "+ Add more",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Original ingredient item
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currentMeal.name} • ${currentMeal.calories * servingCount} cal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceColor
                        )
                        Text(
                            text = "×$servingCount",
                            fontSize = 14.sp,
                            color = onSurfaceVariantColor
                        )
                    }
                }
                
                // Additional ingredients
                additionalIngredients.forEachIndexed { index, ingredient ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceColor
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ingredient,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceColor,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { 
                                    additionalIngredients = additionalIngredients.toMutableList().also { it.removeAt(index) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Health insight if available
                meal.healthInsight?.let { insight ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "💡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = insight,
                                fontSize = 14.sp,
                                color = Color(0xFF065F46),
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hasChanges && !isRecalibrating) {
                                scope.launch {
                                    isRecalibrating = true
                                    try {
                                        val recalibrated = onRecalibrate(currentMeal, additionalIngredients, servingCount)
                                        if (recalibrated != null) {
                                            currentMeal = recalibrated
                                            Toast.makeText(context, "Results updated!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to recalibrate", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isRecalibrating = false
                                    }
                                }
                            } else if (!hasChanges) {
                                Toast.makeText(context, "No changes to fix", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = hasChanges && !isRecalibrating,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.dp, if (hasChanges) MaterialTheme.colorScheme.primary else Color(0xFFE5E7EB))
                    ) {
                        if (isRecalibrating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecalibrating) "Fixing..." else "Fix Results",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Button(
                        onClick = {
                            // Update meal if changes were made
                            if (hasChanges) {
                                val updatedMeal = currentMeal.copy(
                                    calories = currentMeal.calories * servingCount,
                                    protein = currentMeal.protein * servingCount,
                                    carbs = currentMeal.carbs * servingCount,
                                    fats = currentMeal.fats * servingCount,
                                    fiber = currentMeal.fiber * servingCount,
                                    sugar = currentMeal.sugar * servingCount,
                                    sodium = currentMeal.sodium * servingCount
                                )
                                onUpdate(updatedMeal)
                            }
                            onBack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = onSurfaceColor
                        )
                    ) {
                        Text(
                            text = "Done",
                            fontWeight = FontWeight.SemiBold,
                            color = backgroundColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Share Sheet Dialog
        if (showShareSheet) {
            ShareFoodSheet(
                meal = currentMeal,
                servingCount = servingCount,
                onDismiss = { showShareSheet = false }
            )
        }
        
        // Add Ingredient Dialog
        if (showAddIngredientDialog) {
            AddIngredientDialog(
                onDismiss = { showAddIngredientDialog = false },
                onAdd = { ingredient ->
                    additionalIngredients = additionalIngredients + ingredient
                    showAddIngredientDialog = false
                }
            )
        }
    }
}

@Composable
private fun MacroDetailCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MutedText
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }
    }
}

@Composable
private fun MicroDetailCard(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MutedText
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
        }
    }
}

/**
 * Share Food Sheet - Shows shareable image with CalViewAI branding
 */
@Composable
private fun ShareFoodSheet(
    meal: MealEntity,
    servingCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Dark theme support
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = onSurfaceColor)
                    }
                    Text(
                        text = "Share",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.size(48.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Shareable Image Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    // Food Image
                    if (meal.imagePath != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(meal.imagePath)
                                .crossfade(true)
                                .build(),
                            contentDescription = meal.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    // Branding Overlay at bottom - More transparent
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.7f), // Semi-transparent for food visibility
                            shadowElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // App branding with logo
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // App Logo
                                    Image(
                                        painter = androidx.compose.ui.res.painterResource(
                                            id = com.example.calview.feature.dashboard.R.drawable.app_logo
                                        ),
                                        contentDescription = "CalViewAI Logo",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CalViewAI",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Food name
                                Text(
                                    text = meal.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Nutrition summary
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ShareMacroItem(
                                        icon = Icons.Filled.LocalFireDepartment,
                                        value = "${meal.calories * servingCount}",
                                        label = "Calories",
                                        color = Color.White
                                    )
                                    ShareMacroItem(
                                        icon = Icons.Filled.Favorite,
                                        value = "${meal.protein * servingCount}",
                                        label = "Protein",
                                        color = Color(0xFFEF4444)
                                    )
                                    ShareMacroItem(
                                        icon = Icons.Filled.Eco,
                                        value = "${meal.carbs * servingCount}",
                                        label = "Carbs",
                                        color = Color(0xFFF59E0B)
                                    )
                                    ShareMacroItem(
                                        icon = Icons.Filled.WaterDrop,
                                        value = "${meal.fats * servingCount}",
                                        label = "Fats",
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Share Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareOptionButton(
                        icon = Icons.Filled.Share,
                        label = "Share",
                        onClick = {
                            scope.launch {
                                shareFood(context, meal, servingCount)
                            }
                        }
                    )
                    ShareOptionButton(
                        icon = Icons.Outlined.Download,
                        label = "Save",
                        onClick = {
                            scope.launch {
                                saveImageToGallery(context, meal)
                            }
                        }
                    )
                    ShareOptionButton(
                        icon = Icons.Outlined.ContentCopy,
                        label = "Copy",
                        onClick = {
                            val text = "${meal.name}\n${meal.calories} cal | P: ${meal.protein}g | C: ${meal.carbs}g | F: ${meal.fats}g\n\nTracked with CalViewAI"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Food Info", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareMacroItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MutedText
        )
    }
}

@Composable
private fun ShareOptionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF3F4F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = DarkText
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MutedText
        )
    }
}

private fun formatMealTimeDetail(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private suspend fun saveImageToGallery(context: Context, meal: MealEntity, servingCount: Int = 1) {
    withContext(Dispatchers.IO) {
        try {
            val fileName = "${meal.name.replace(" ", "_")}_CalViewAI_${System.currentTimeMillis()}.jpg"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CalViewAI")
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                uri?.let { contentUri ->
                    // Generate branded image
                    val brandedBitmap = generateBrandedImage(context, meal, servingCount)
                    if (brandedBitmap != null) {
                        context.contentResolver.openOutputStream(contentUri)?.use { outputStream ->
                            brandedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        }
                        brandedBitmap.recycle()
                    } else {
                        // Fallback to original image
                        meal.imagePath?.let { path ->
                            val sourceFile = File(path)
                            if (sourceFile.exists()) {
                                context.contentResolver.openOutputStream(contentUri)?.use { outputStream ->
                                    sourceFile.inputStream().use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun shareFood(context: Context, meal: MealEntity, servingCount: Int) {
    withContext(Dispatchers.IO) {
        try {
            // Generate branded image
            val brandedBitmap = generateBrandedImage(context, meal, servingCount)
            
            if (brandedBitmap != null) {
                // Save to cache and share
                val cacheDir = File(context.cacheDir, "share_images")
                cacheDir.mkdirs()
                val shareFile = File(cacheDir, "calviewai_${System.currentTimeMillis()}.jpg")
                
                FileOutputStream(shareFile).use { output ->
                    brandedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
                }
                brandedBitmap.recycle()
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    shareFile
                )
                
                withContext(Dispatchers.Main) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share ${meal.name}"))
                }
            } else {
                // Fallback to text share
                withContext(Dispatchers.Main) {
                    val shareText = buildString {
                        appendLine("🍽️ ${meal.name}")
                        appendLine()
                        appendLine("📊 Nutrition Info:")
                        appendLine("🔥 ${meal.calories * servingCount} Calories")
                        appendLine("💪 ${meal.protein * servingCount}g Protein")
                        appendLine("🌾 ${meal.carbs * servingCount}g Carbs")
                        appendLine("💧 ${meal.fats * servingCount}g Fats")
                        appendLine()
                        appendLine("Tracked with CalViewAI 📱")
                    }
                    
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share ${meal.name}"))
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun generateBrandedImage(context: Context, meal: MealEntity, servingCount: Int): Bitmap? {
    return try {
        // Load food image
        val foodBitmap = meal.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(path)
            } else null
        } ?: return null
        
        // Create output bitmap (square for social media)
        val size = maxOf(foodBitmap.width, foodBitmap.height)
        val outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        
        // Draw food image centered
        val left = (size - foodBitmap.width) / 2f
        val top = (size - foodBitmap.height) / 2f
        canvas.drawBitmap(foodBitmap, left, top, null)
        
        // Draw semi-transparent overlay at bottom
        val overlayHeight = size * 0.35f
        val overlayPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, size - overlayHeight, size.toFloat(), size.toFloat(), overlayPaint)
        
        // Load app logo
        val logoBitmap = try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.calview.feature.dashboard.R.drawable.app_logo)
            if (drawable != null) {
                val logoSize = (size * 0.08f).toInt()
                val bitmap = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
                val logoCanvas = Canvas(bitmap)
                drawable.setBounds(0, 0, logoSize, logoSize)
                drawable.draw(logoCanvas)
                bitmap
            } else null
        } catch (e: Exception) { null }
        
        // Draw logo and app name
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = size * 0.04f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        
        val padding = size * 0.04f
        var yPos = size - overlayHeight + padding + textPaint.textSize
        
        if (logoBitmap != null) {
            canvas.drawBitmap(logoBitmap, padding, yPos - logoBitmap.height + 5, null)
            canvas.drawText("CalViewAI", padding + logoBitmap.width + 10, yPos, textPaint)
            logoBitmap.recycle()
        } else {
            canvas.drawText("CalViewAI", padding, yPos, textPaint)
        }
        
        // Draw food name
        yPos += textPaint.textSize * 1.8f
        textPaint.textSize = size * 0.05f
        canvas.drawText(meal.name, padding, yPos, textPaint)
        
        // Draw nutrition info
        yPos += textPaint.textSize * 1.8f
        textPaint.textSize = size * 0.035f
        textPaint.typeface = android.graphics.Typeface.DEFAULT
        
        val calories = meal.calories * servingCount
        val protein = meal.protein * servingCount
        val carbs = meal.carbs * servingCount
        val fats = meal.fats * servingCount
        
        val nutritionText = "🔥 $calories cal  •  💪 ${protein}g  •  🌾 ${carbs}g  •  💧 ${fats}g"
        canvas.drawText(nutritionText, padding, yPos, textPaint)
        
        foodBitmap.recycle()
        outputBitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Dialog for adding additional ingredients
 */
@Composable
private fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var ingredientText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Ingredient",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Enter an ingredient that the AI might have missed:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = ingredientText,
                    onValueChange = { ingredientText = it },
                    label = { Text("Ingredient name") },
                    placeholder = { Text("e.g., extra cheese, olive oil") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (ingredientText.isNotBlank()) {
                        onAdd(ingredientText.trim())
                    }
                },
                enabled = ingredientText.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

