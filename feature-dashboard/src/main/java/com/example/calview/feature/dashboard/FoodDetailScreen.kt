package com.example.calview.feature.dashboard

import android.content.ContentValues
import java.text.SimpleDateFormat
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.graphics.Typeface
import android.graphics.RectF
import android.graphics.BlurMaskFilter
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
import java.util.*
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

// NOTE: Use MaterialTheme.colorScheme for all colors - no hardcoded values

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
    
    // Satiety Index (1-10)
    val satietyIndex = remember(currentMeal) {
        val proteinWeight = 10
        val fiberWeight = 20
        val fatsWeight = 5
        val sugarWeight = 8
        val rawScore = 3.5f + (currentMeal.protein * proteinWeight + currentMeal.fiber * fiberWeight - currentMeal.fats * fatsWeight - currentMeal.sugar * sugarWeight) / 100f
        rawScore.coerceIn(1f, 10f).toInt()
    }
    
    // Permission launcher for saving to gallery on older Android versions
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                saveImageToGallery(context, currentMeal, servingCount)
            }
        } else {
            Toast.makeText(context, "Storage permission required to save image", Toast.LENGTH_SHORT).show()
        }
    }
    
    val onSaveWithPermissionCheck = {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            scope.launch {
                saveImageToGallery(context, currentMeal, servingCount)
            }
        }
        Unit
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
                        text = stringResource(R.string.food_details_title),
                        fontFamily = InterFontFamily,
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
                                    text = { Text(stringResource(R.string.report_food)) },
                                    leadingIcon = { Icon(Icons.Outlined.Flag, null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        Toast.makeText(context, context.getString(R.string.report_submitted), Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.save_image)) },
                                    leadingIcon = { Icon(Icons.Outlined.Download, null) },
                                    onClick = {
                                        showOptionsMenu = false
                                        onSaveWithPermissionCheck()
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_food), color = Color(0xFFEF4444)) },
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
                    .background(surfaceColor)
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
                            tint = onSurfaceVariantColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatMealTimeDetail(meal.timestamp),
                            fontSize = 14.sp,
                            color = onSurfaceVariantColor
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        color = onSurfaceColor,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Serving counter
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = surfaceColor
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
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.02).sp,
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
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    color = surfaceColor
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.LocalFireDepartment,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = onSurfaceColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = stringResource(R.string.calories_label),
                                                fontFamily = InterFontFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = onSurfaceVariantColor
                                            )
                                            Text(
                                                text = "${meal.calories * servingCount}",
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = (-0.02).sp,
                                                color = onSurfaceColor
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
                                        label = stringResource(R.string.protein_label),
                                        value = "${meal.protein * servingCount}g",
                                        icon = Icons.Filled.Favorite,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailCard(
                                        label = stringResource(R.string.carbs_label),
                                        value = "${meal.carbs * servingCount}g",
                                        icon = Icons.Filled.Eco,
                                        color = Color(0xFFF59E0B),
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroDetailCard(
                                        label = stringResource(R.string.fats_label),
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
                                        label = stringResource(R.string.fiber_label),
                                        value = "${meal.fiber * servingCount}g",
                                        emoji = "🌾",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MicroDetailCard(
                                        label = stringResource(R.string.sugar_label),
                                        value = "${meal.sugar * servingCount}g",
                                        emoji = "🍬",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MicroDetailCard(
                                        label = stringResource(R.string.sodium_label),
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
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    color = surfaceColor
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
                                                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
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
                                                    text = stringResource(R.string.health_score),
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = onSurfaceColor
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
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = (-0.02).sp,
                                            color = onSurfaceColor
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Satiety Index Card
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    color = surfaceColor
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
                                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "🥣",
                                                    fontSize = 24.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Satiety Index",
                                                    fontFamily = InterFontFamily,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = onSurfaceColor
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
                                                            .fillMaxWidth(satietyIndex / 10f)
                                                            .fillMaxHeight()
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                                                                ),
                                                                RoundedCornerShape(3.dp)
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "$satietyIndex/10",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = (-0.02).sp,
                                            color = onSurfaceColor
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
                                    if (pagerState.currentPage == index) onSurfaceColor else Color(0xFFD1D5DB),
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
                                        text = stringResource(R.string.what_detected),
                                        fontFamily = InterFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
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
                                                "${meal.confidenceScore.toInt()}% ${stringResource(R.string.confident_suffix)}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = (-0.02).sp,
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
                                            val prefix = stringResource(R.string.i_see_prefix)
                                            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = onSurfaceColor)) {
                                                append(prefix)
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
                                            val prefix = stringResource(R.string.i_suspect_prefix)
                                            withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = Color(0xFFFF9800))) {
                                                append(prefix)
                                            }
                                            lowConfidenceItems.forEachIndexed { index, item ->
                                                withStyle(SpanStyle(color = onSurfaceColor)) {
                                                    append(item.name)
                                                }
                                                if (item.detection_note != null) {
                                                    withStyle(SpanStyle(
                                                        color = onSurfaceVariantColor,
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
                        text = stringResource(R.string.ingredients_title),
                        fontFamily = InterFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor
                    )
                    TextButton(onClick = { showAddIngredientDialog = true }) {
                        Text(
                            text = stringResource(R.string.add_more_action),
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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // AI Swap suggestion
                meal.healthSwap?.let { swap ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "⚡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Swap Suggestion",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9A3412)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = swap,
                                    fontSize = 14.sp,
                                    color = Color(0xFF9A3412),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Nutrient Synergy
                meal.nutrientSynergy?.let { synergy ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "🧪", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Nutrient Synergy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = synergy,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF),
                                    lineHeight = 20.sp
                                )
                            }
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
                onDismiss = { showShareSheet = false },
                onSaveToGallery = onSaveWithPermissionCheck
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
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
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).sp,
                color = MaterialTheme.colorScheme.onSurface
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).sp,
                color = MaterialTheme.colorScheme.onSurface
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
    onDismiss: () -> Unit,
    onSaveToGallery: () -> Unit = {}
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
                    
                    // Top Macro Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.85f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 ${meal.calories * servingCount} kcal",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "P ${meal.protein * servingCount}g",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp,
                                    color = Color(0xFFEF5350)
                                )
                                Text(
                                    text = "C ${meal.carbs * servingCount}g",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp,
                                    color = Color(0xFFFFA726)
                                )
                                Text(
                                    text = "F ${meal.fats * servingCount}g",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp,
                                    color = Color(0xFF42A5F5)
                                )
                            }
                        }
                    }

                    // Bottom Branding Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        // Frosted glass background
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Color.White.copy(alpha = 0.85f)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // App Logo - 100dp
                                Image(
                                    painter = androidx.compose.ui.res.painterResource(
                                        id = com.example.calview.feature.dashboard.R.drawable.ic_calview_logo
                                    ),
                                    contentDescription = "CalViewAI Logo",
                                    modifier = Modifier
                                        .size(100.dp),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Food name and app branding
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = meal.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "CalViewAI",
                                        fontSize = 10.sp,
                                        color = Color.Black.copy(alpha = 0.5f)
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
                        icon = Icons.Filled.Download,
                        label = "Save",
                        onClick = onSaveToGallery
                    )
                    ShareOptionButton(
                        icon = Icons.Filled.ContentCopy,
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
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            
            // Check if meal has an image
            if (meal.imagePath == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No image to save", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            
            // Check if source image exists
            val sourceFile = File(meal.imagePath)
            if (!sourceFile.exists()) {
                android.util.Log.e("ShareFood", "Source image file not found: ${meal.imagePath}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Source image not found", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }
            // Short filename - max 10 chars from meal name
            val shortName = meal.name.take(10).replace(" ", "_").replace("/", "_").replace(".", "")
            val fileName = "${shortName}_${System.currentTimeMillis() % 10000}.jpg"
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
                // For Android 9 and below - save to app cache then copy to gallery
                try {
                    // First save to app cache (always writable)
                    val cacheDir = File(context.cacheDir, "share_images")
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs()
                    }
                    val cacheFile = File(cacheDir, fileName)
                    
                    FileOutputStream(cacheFile).use { out ->
                        brandedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    
                    // Try to copy to Pictures directory
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val calviewDir = File(picturesDir, "CalViewAI")
                    
                    try {
                        if (!calviewDir.exists()) {
                            val created = calviewDir.mkdirs()
                            android.util.Log.d("ShareFood", "Created directory: $created at ${calviewDir.absolutePath}")
                        }
                        val destFile = File(calviewDir, fileName)
                        cacheFile.copyTo(destFile, overwrite = true)
                        
                        // Notify media scanner
                        android.media.MediaScannerConnection.scanFile(context, arrayOf(destFile.absolutePath), null, { path, uri ->
                             android.util.Log.d("ShareFood", "Scanned $path: uri=$uri")
                        })
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "✓ Saved to Pictures/CalViewAI", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        // Fallback - keep in cache and show different message
                        android.util.Log.e("ShareFood", "Failed to copy to Pictures: ${e.message}", e)
                        
                        // If it's a permission issue or something else, at least tell the user where it is
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Saved to app cache (Gallery access failed)", Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    // Clean up cache file if copy succeeded
                    val verifyFile = File(calviewDir, fileName)
                    if (verifyFile.exists() && verifyFile.length() > 0) {
                        cacheFile.delete()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ShareFood", "Save to cache failed: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
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
            var shouldShareText = true
            
            // Check if meal has an image and it exists
            if (meal.imagePath != null) {
                val sourceFile = File(meal.imagePath)
                if (sourceFile.exists()) {
                    // Generate branded image
                    val brandedBitmap = generateBrandedImage(context, meal, servingCount)
                    
                    if (brandedBitmap != null) {
                        // Save to cache and share - explicitly create directory
                        val cacheDir = File(context.cacheDir, "share_images")
                        if (!cacheDir.exists()) {
                            val created = cacheDir.mkdirs()
                            android.util.Log.d("ShareFood", "Created cache dir: $created at ${cacheDir.absolutePath}")
                        }
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
                        shouldShareText = false
                    }
                } else {
                    android.util.Log.e("ShareFood", "Source image not found: ${meal.imagePath}")
                }
            }
            
            // Fallback to text share if image sharing failed
            if (shouldShareText) {
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
            android.util.Log.e("ShareFood", "Share failed: ${e.message}", e)
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
        
        // Margins and padding
        val horizontalMargin = width * 0.03f
        val topMargin = height * 0.03f
        val bottomMargin = height * 0.03f
        val padding = width * 0.025f
        
        // Content and paints
        val foodNamePaint = TextPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = width * 0.03f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        val appBrandPaint = TextPaint().apply {
            color = android.graphics.Color.argb(128, 0, 0, 0)
            textSize = width * 0.022f
            isAntiAlias = true
        }

        // --- DRAW TOP MACRO CARD ---
        val macroTextSize = width * 0.025f
        val macroBoldPaint = TextPaint().apply {
            color = android.graphics.Color.WHITE
            textSize = macroTextSize
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val proteinPaint = TextPaint().apply {
            color = android.graphics.Color.rgb(239, 83, 80) // Red
            textSize = macroTextSize * 0.95f
            isAntiAlias = true
        }
        val carbsPaint = TextPaint().apply {
            color = android.graphics.Color.rgb(255, 167, 38) // Orange
            textSize = macroTextSize * 0.95f
            isAntiAlias = true
        }
        val fatsPaint = TextPaint().apply {
            color = android.graphics.Color.rgb(66, 165, 245) // Blue
            textSize = macroTextSize * 0.95f
            isAntiAlias = true
        }

        val macroText = "🔥 ${meal.calories * servingCount} kcal   P ${meal.protein * servingCount}g   C ${meal.carbs * servingCount}g   F ${meal.fats * servingCount}g"
        val macroPillWidth = macroBoldPaint.measureText("🔥 ${meal.calories * servingCount} kcal") + 
                          proteinPaint.measureText("   P ${meal.protein * servingCount}g") + 
                          carbsPaint.measureText("   C ${meal.carbs * servingCount}g") + 
                          fatsPaint.measureText("   F ${meal.fats * servingCount}g") + padding * 2

        val macroPillHeight = macroTextSize * 2.2f
        val macroPillRadius = macroPillHeight / 2
        val macroPillLeft = (width - macroPillWidth) / 2
        val macroPillRect = RectF(macroPillLeft, topMargin, macroPillLeft + macroPillWidth, topMargin + macroPillHeight)
        
        val macroBgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(217, 0, 0, 0) // 85% Black
            isAntiAlias = true
        }
        canvas.drawRoundRect(macroPillRect, macroPillRadius, macroPillRadius, macroBgPaint)
        
        var currentMacroX = macroPillRect.left + padding
        val macroBaseline = macroPillRect.centerY() + (macroTextSize * 0.35f)
        
        canvas.drawText("🔥 ${meal.calories * servingCount} kcal", currentMacroX, macroBaseline, macroBoldPaint)
        currentMacroX += macroBoldPaint.measureText("🔥 ${meal.calories * servingCount} kcal")
        canvas.drawText("   P ${meal.protein * servingCount}g", currentMacroX, macroBaseline, proteinPaint)
        currentMacroX += proteinPaint.measureText("   P ${meal.protein * servingCount}g")
        canvas.drawText("   C ${meal.carbs * servingCount}g", currentMacroX, macroBaseline, carbsPaint)
        currentMacroX += carbsPaint.measureText("   C ${meal.carbs * servingCount}g")
        canvas.drawText("   F ${meal.fats * servingCount}g", currentMacroX, macroBaseline, fatsPaint)

        // --- DRAW BOTTOM BRANDING CARD ---
        val density = context.resources.displayMetrics.density
        // App logo size: 20% of width up to 120dp
        val maxLogoSizePx = (120 * density).toInt()
        val logoSize = (width * 0.20f).coerceAtMost(maxLogoSizePx.toFloat()).toInt()
        
        // Available width for text (full width - margins - padding - logo)
        val textLeft = horizontalMargin + padding + logoSize + padding * 1.5f
        val textRight = width - horizontalMargin - padding
        val availableTextWidth = (textRight - textLeft).toInt().coerceAtLeast(100)
        
        val builder = StaticLayout.Builder.obtain(meal.name, 0, meal.name.length, foodNamePaint, availableTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
        val staticLayout = builder.build()
        
        val textHeight = staticLayout.height.toFloat()
        val brandHeight = appBrandPaint.textSize * 1.2f
        
        val minCardHeight = height * 0.14f
        val cardHeight = (padding * 2 + textHeight + brandHeight).coerceAtLeast(minCardHeight)
        val cardRadius = width * 0.025f
        
        val cardTop = height - bottomMargin - cardHeight
        val cardRect = RectF(horizontalMargin, cardTop, width - horizontalMargin, height - bottomMargin)
        
        val glassPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(217, 255, 255, 255) // 85% opacity White
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, glassPaint)
        
        // Draw Logo - implement ContentScale.Crop behavior in a square area
        val logoBitmap = try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, com.example.calview.feature.dashboard.R.drawable.ic_calview_logo)
            if (drawable != null) {
                // Get intrinsic dimensions
                val intrinsicWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: logoSize
                val intrinsicHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: logoSize
                val aspectRatio = intrinsicWidth.toFloat() / intrinsicHeight
                
                // ContentScale.Crop logic: fill the logoSize x logoSize square
                val drawWidth: Int
                val drawHeight: Int
                if (aspectRatio > 1f) {
                    // Wider than tall: match height, crop width
                    drawHeight = logoSize
                    drawWidth = (logoSize * aspectRatio).toInt()
                } else {
                    // Taller than wide: match width, crop height
                    drawWidth = logoSize
                    drawHeight = (logoSize / aspectRatio).toInt()
                }
                
                val b = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                
                // Center the drawable to achieve crop behavior
                val left = (logoSize - drawWidth) / 2
                val top = (logoSize - drawHeight) / 2
                drawable.setBounds(left, top, left + drawWidth, top + drawHeight)
                drawable.draw(c)
                b
            } else null
        } catch (e: Exception) { null }
        
        if (logoBitmap != null) {
            val logoY = cardTop + (cardHeight - logoBitmap.height) / 2
            val logoX = horizontalMargin + padding
            canvas.drawBitmap(logoBitmap, logoX, logoY, null)
            logoBitmap.recycle()
        }
        
        // Draw Wrapped Text
        canvas.save()
        canvas.translate(textLeft, cardTop + padding)
        staticLayout.draw(canvas)
        canvas.restore()
        
        // Draw branding
        canvas.drawText("CalViewAI", textLeft, cardTop + padding + textHeight + appBrandPaint.textSize, appBrandPaint)
        
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

