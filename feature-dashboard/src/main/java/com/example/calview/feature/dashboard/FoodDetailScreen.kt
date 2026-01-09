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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
                
                // Detected Items Section with Confidence Scores
                if (meal.detectedItemsJson != null) {
                    val detectedItems = remember(meal.detectedItemsJson) {
                        try {
                            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                .decodeFromString<List<com.example.calview.core.ai.model.FoodItem>>(meal.detectedItemsJson!!)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (!detectedItems.isNullOrEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = surfaceColor,
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "What I Detected",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurfaceColor
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    // Overall confidence badge
                                    if (meal.confidenceScore > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when {
                                                meal.confidenceScore >= 80f -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                meal.confidenceScore >= 60f -> Color(0xFFFFC107).copy(alpha = 0.15f)
                                                else -> Color(0xFFFF5722).copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                "${meal.confidenceScore.toInt()}% confident",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = when {
                                                    meal.confidenceScore >= 80f -> Color(0xFF4CAF50)
                                                    meal.confidenceScore >= 60f -> Color(0xFFFFC107)
                                                    else -> Color(0xFFFF5722)
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                // Build the "I see" text
                                val highConfidenceItems = detectedItems.filter { it.confidence >= 0.7 }
                                val lowConfidenceItems = detectedItems.filter { it.confidence < 0.7 }
                                
                                if (highConfidenceItems.isNotEmpty()) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = onSurfaceColor)) {
                                                append("I see: ")
                                            }
                                            highConfidenceItems.forEachIndexed { index, item ->
                                                withStyle(SpanStyle(color = onSurfaceColor)) {
                                                    append(item.name)
                                                }
                                                withStyle(SpanStyle(
                                                    color = when {
                                                        item.confidence >= 0.85 -> Color(0xFF4CAF50)
                                                        item.confidence >= 0.7 -> Color(0xFFFFC107)
                                                        else -> Color(0xFFFF5722)
                                                    },
                                                    fontSize = 12.sp
                                                )) {
                                                    append(" (${(item.confidence * 100).toInt()}%)")
                                                }
                                                if (index < highConfidenceItems.size - 1) {
                                                    append(", ")
                                                }
                                            }
                                        },
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                                
                                if (lowConfidenceItems.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = Color(0xFFFF9800))) {
                                                append("I suspect: ")
                                            }
                                            lowConfidenceItems.forEachIndexed { index, item ->
                                                withStyle(SpanStyle(color = onSurfaceColor)) {
                                                    append(item.name)
                                                }
                                                if (item.detection_note != null) {
                                                    withStyle(SpanStyle(
                                                        color = MutedText,
                                                        fontSize = 12.sp,
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                    )) {
                                                        append(" (${item.detection_note})")
                                                    }
                                                } else {
                                                    withStyle(SpanStyle(
                                                        color = Color(0xFFFF5722),
                                                        fontSize = 12.sp
                                                    )) {
                                                        append(" (${(item.confidence * 100).toInt()}%)")
                                                    }
                                                }
                                                if (index < lowConfidenceItems.size - 1) {
                                                    append(", ")
                                                }
                                            }
                                        },
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
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
                    
                    // Branding Overlay at TOP - Floating WHITE transparent card
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter) // TOP position
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.85f), // More opaque for readability
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                // Top row: Logo + App name + Food name
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // App Logo - Larger
                                    Image(
                                        painter = androidx.compose.ui.res.painterResource(
                                            id = com.example.calview.feature.dashboard.R.drawable.app_logo
                                        ),
                                        contentDescription = "CalViewAI Logo",
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "CalViewAI",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = meal.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Bottom row: Macros
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Calories
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "🔥", fontSize = 18.sp)
                                        Text(
                                            text = "${meal.calories * servingCount}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "cal",
                                            fontSize = 10.sp,
                                            color = Color.Black.copy(alpha = 0.6f)
                                        )
                                    }
                                    // Protein
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "💪", fontSize = 18.sp)
                                        Text(
                                            text = "${meal.protein * servingCount}g",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "protein",
                                            fontSize = 10.sp,
                                            color = Color.Black.copy(alpha = 0.6f)
                                        )
                                    }
                                    // Carbs
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "🌾", fontSize = 18.sp)
                                        Text(
                                            text = "${meal.carbs * servingCount}g",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "carbs",
                                            fontSize = 10.sp,
                                            color = Color.Black.copy(alpha = 0.6f)
                                        )
                                    }
                                    // Fats
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "💧", fontSize = 18.sp)
                                        Text(
                                            text = "${meal.fats * servingCount}g",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "fats",
                                            fontSize = 10.sp,
                                            color = Color.Black.copy(alpha = 0.6f)
                                        )
                                    }
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
                .size(36.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ShareMacroItemWhite(
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconColor
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.Black.copy(alpha = 0.6f)
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
            android.util.Log.d("ShareFood", "Starting save to gallery for: ${meal.name}")
            
            val fileName = "${meal.name.replace(" ", "_")}_CalViewAI_${System.currentTimeMillis()}.jpg"
            android.util.Log.d("ShareFood", "File name: $fileName")
            
            // Generate branded image first
            val brandedBitmap = generateBrandedImage(context, meal, servingCount)
            android.util.Log.d("ShareFood", "Branded bitmap: ${brandedBitmap != null}")
            
            if (brandedBitmap == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to create image", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CalViewAI")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                android.util.Log.d("ShareFood", "MediaStore URI: $uri")
                
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val success = brandedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        android.util.Log.d("ShareFood", "Compress success: $success")
                    }
                    
                    // Clear pending flag
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✓ Saved to Pictures/CalViewAI", Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to create file in gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // For Android 9 and below
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val calviewDir = File(picturesDir, "CalViewAI")
                calviewDir.mkdirs()
                val file = File(calviewDir, fileName)
                
                FileOutputStream(file).use { out ->
                    brandedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                
                // Notify media scanner
                android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✓ Saved to Pictures/CalViewAI", Toast.LENGTH_LONG).show()
                }
            }
            
            brandedBitmap.recycle()
            
        } catch (e: Exception) {
            android.util.Log.e("ShareFood", "Save failed: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
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
        android.util.Log.d("ShareFood", "Generating branded image for: ${meal.name}")
        
        // Load food image
        val foodBitmap = meal.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(path)
            } else {
                android.util.Log.e("ShareFood", "Image file not found: $path")
                null
            }
        } ?: return null
        
        android.util.Log.d("ShareFood", "Food bitmap size: ${foodBitmap.width}x${foodBitmap.height}")
        
        // Create output bitmap (matching original aspect ratio)
        val outputBitmap = Bitmap.createBitmap(foodBitmap.width, foodBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        
        // Draw food image
        canvas.drawBitmap(foodBitmap, 0f, 0f, null)
        
        val width = foodBitmap.width.toFloat()
        val height = foodBitmap.height.toFloat()
        val margin = width * 0.03f
        
        // Card at TOP - LARGER floating WHITE card with more opacity
        val cardWidth = width - (margin * 2)
        val cardHeight = height * 0.15f // Taller card for macros
        val cardTop = margin
        val cardRadius = width * 0.03f
        
        // Draw semi-transparent WHITE rounded card (85% opacity)
        val cardPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(217, 255, 255, 255) // 85% opacity WHITE
            isAntiAlias = true
        }
        val cardRect = android.graphics.RectF(margin, cardTop, margin + cardWidth, cardTop + cardHeight)
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardPaint)
        
        // Load app logo - LARGER
        val logoSize = (cardHeight * 0.4f).toInt().coerceAtLeast(48)
        val logoBitmap = try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.calview.feature.dashboard.R.drawable.app_logo)
            if (drawable != null) {
                val bitmap = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
                val logoCanvas = Canvas(bitmap)
                drawable.setBounds(0, 0, logoSize, logoSize)
                drawable.draw(logoCanvas)
                bitmap
            } else null
        } catch (e: Exception) { null }
        
        // Text paints - BLACK text for white card
        val appNamePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = width * 0.04f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
        
        val foodNamePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(180, 0, 0, 0)
            textSize = width * 0.03f
            isAntiAlias = true
        }
        
        val macroValuePaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = width * 0.035f
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val macroLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(150, 0, 0, 0)
            textSize = width * 0.025f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val emojiPaint = android.graphics.Paint().apply {
            textSize = width * 0.04f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        // Content layout
        val contentPadding = width * 0.025f
        val contentLeft = margin + contentPadding
        
        // Top row: Logo + App name + Food name
        val topRowCenterY = cardTop + (cardHeight * 0.3f)
        
        if (logoBitmap != null) {
            val logoY = topRowCenterY - (logoSize / 2f)
            canvas.drawBitmap(logoBitmap, contentLeft, logoY, null)
            
            val textLeft = contentLeft + logoSize + contentPadding
            canvas.drawText("CalViewAI", textLeft, topRowCenterY - 2, appNamePaint)
            canvas.drawText(meal.name.take(30), textLeft, topRowCenterY + foodNamePaint.textSize + 2, foodNamePaint)
            
            logoBitmap.recycle()
        } else {
            canvas.drawText("CalViewAI", contentLeft, topRowCenterY, appNamePaint)
        }
        
        // Bottom row: Macros with emojis
        val macroRowY = cardTop + (cardHeight * 0.75f)
        val macroSpacing = cardWidth / 4f
        val macroStartX = margin + (macroSpacing / 2f)
        
        val calories = meal.calories * servingCount
        val protein = meal.protein * servingCount
        val carbs = meal.carbs * servingCount
        val fats = meal.fats * servingCount
        
        // Draw each macro: emoji, value, label
        val macros = listOf(
            Triple("🔥", "$calories", "cal"),
            Triple("💪", "${protein}g", "protein"),
            Triple("🌾", "${carbs}g", "carbs"),
            Triple("💧", "${fats}g", "fats")
        )
        
        macros.forEachIndexed { index, (emoji, value, label) ->
            val x = macroStartX + (index * macroSpacing)
            canvas.drawText(emoji, x, macroRowY - macroValuePaint.textSize * 0.8f, emojiPaint)
            canvas.drawText(value, x, macroRowY + 4, macroValuePaint)
            canvas.drawText(label, x, macroRowY + macroLabelPaint.textSize + 6, macroLabelPaint)
        }
        
        foodBitmap.recycle()
        android.util.Log.d("ShareFood", "Branded image generated successfully")
        outputBitmap
    } catch (e: Exception) {
        android.util.Log.e("FoodDetailScreen", "Failed to generate branded image", e)
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

