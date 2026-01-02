package com.example.calview.feature.trends

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.cos
import kotlin.math.sin

// Modern color palette
private val GradientCyan = Color(0xFF00D4AA)
private val GradientPurple = Color(0xFF7C3AED)
private val GradientPink = Color(0xFFEC4899)
private val GradientBlue = Color(0xFF3B82F6)
private val GradientOrange = Color(0xFFF59E0B)
private val CardBackground = Color(0xFFFAFAFA)
private val DarkText = Color(0xFF1F2937)
private val MutedText = Color(0xFF6B7280)

// BMI Colors
private val BMIUnderweight = Color(0xFF3B82F6)
private val BMIHealthy = Color(0xFF10B981)
private val BMIOverweight = Color(0xFFF59E0B)
private val BMIObese = Color(0xFFEF4444)

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    ProgressContent(
        uiState = uiState,
        onRefresh = { viewModel.refreshData() }
    )
}

@Composable
fun ProgressContent(
    uiState: ProgressUiState,
    onRefresh: () -> Unit = {}
) {
    var animationTriggered by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animationTriggered = true
    }
    
    // Get adaptive layout values based on screen size
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFEFF6FF),
                        Color(0xFFF8FAFC)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Progress",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "Track your health journey",
                    fontSize = 14.sp,
                    color = MutedText
                )
            }
            
            // Refresh button
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Refresh",
                    tint = GradientPurple
                )
            }
        }
        
        // Hero BMI Card - using CompactBMICard since AnimatedBMICard is not defined
        // The CompactBMICard is placed at the bottom of the screen
        // (see line ~246 where CompactBMICard is called)
        
        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedStatCard(
                modifier = Modifier.weight(1f),
                value = uiState.currentWeight,
                label = "Weight",
                unit = "kg",
                icon = Icons.Outlined.Scale,
                progress = uiState.weightProgress,
                gradientColors = listOf(GradientCyan, GradientBlue),
                animationTriggered = animationTriggered
            )
            AnimatedStatCard(
                modifier = Modifier.weight(1f),
                value = uiState.dayStreak.toFloat(),
                label = "Streak",
                unit = "days",
                icon = Icons.Filled.Whatshot,
                progress = (uiState.dayStreak / 30f).coerceIn(0f, 1f),
                gradientColors = listOf(GradientOrange, GradientPink),
                animationTriggered = animationTriggered,
                isStreak = true
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedStatCard(
                modifier = Modifier.weight(1f),
                value = uiState.todaySteps.toFloat(),
                label = "Steps",
                unit = "",
                icon = Icons.Outlined.DirectionsWalk,
                progress = (uiState.todaySteps / uiState.stepsGoal.toFloat()).coerceIn(0f, 1f),
                gradientColors = listOf(GradientPurple, GradientPink),
                animationTriggered = animationTriggered,
                showAsInt = true
            )
            AnimatedStatCard(
                modifier = Modifier.weight(1f),
                value = uiState.caloriesBurned.toFloat(),
                label = "Burned",
                unit = "cal",
                icon = Icons.Outlined.LocalFireDepartment,
                progress = (uiState.caloriesBurned / 500f).coerceIn(0f, 1f),
                gradientColors = listOf(GradientPink, GradientOrange),
                animationTriggered = animationTriggered,
                showAsInt = true
            )
        }
        
        // Day Streak Visual - moved up for visibility
        DayStreakCard(
            streak = uiState.dayStreak,
            bestStreak = uiState.bestStreak,
            completedDays = uiState.completedDays,
            animationTriggered = animationTriggered
        )
        
        // Weight Progress Card
        WeightProgressCard(
            currentWeight = uiState.currentWeight,
            goalWeight = uiState.goalWeight,
            progress = uiState.weightProgress,
            animationTriggered = animationTriggered
        )
        
        // Weekly Calories Chart
        WeeklyCaloriesChart(
            weeklyData = uiState.weeklyCalories,
            calorieGoal = uiState.calorieGoal,
            animationTriggered = animationTriggered
        )
        
        // Weekly Macros Donut
        MacroDonutCard(
            protein = uiState.todayProtein,
            carbs = uiState.todayCarbs,
            fats = uiState.todayFats,
            proteinGoal = uiState.proteinGoal,
            carbsGoal = uiState.carbsGoal,
            fatsGoal = uiState.fatsGoal,
            animationTriggered = animationTriggered
        )
        
        // Motivational Banner
        MotivationalCard(
            progress = uiState.weightProgress,
            streak = uiState.dayStreak
        )
        
        // BMI Card - compact at the bottom
        CompactBMICard(
            bmi = uiState.bmi,
            bmiCategory = uiState.bmiCategory,
            animationTriggered = animationTriggered
        )
        
        Spacer(modifier = Modifier.height(80.dp))
        } // Close Column
    } // Close Box
}

// ============ COMPACT BMI CARD ============
@Composable
fun CompactBMICard(
    bmi: Float,
    bmiCategory: String,
    animationTriggered: Boolean
) {
    val animatedBMI by animateFloatAsState(
        targetValue = if (animationTriggered) bmi else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bmi"
    )
    
    val bmiColor = when {
        bmi < 18.5f -> BMIUnderweight
        bmi < 25f -> BMIHealthy
        bmi < 30f -> BMIOverweight
        else -> BMIObese
    }
    
    val animatedColor by animateColorAsState(
        targetValue = bmiColor,
        animationSpec = tween(800),
        label = "bmiColor"
    )
    
    // Calculate position on bar (0-1 range)
    val bmiProgress = ((animatedBMI.coerceIn(15f, 40f) - 15f) / 25f).coerceIn(0f, 1f)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "BMI: ${String.format("%.1f", bmi)}, $bmiCategory" },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BMI",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format("%.1f", animatedBMI),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = animatedColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = bmiCategory,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = animatedColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Horizontal BMI bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                // Background gradient bar
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(BMIUnderweight, BMIHealthy, BMIOverweight, BMIObese)
                    )
                    drawRoundRect(
                        brush = gradient,
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
                
                // Position indicator
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset(x = ((bmiProgress * (LocalConfiguration.current.screenWidthDp - 48)).coerceAtLeast(0f)).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, animatedColor, CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // BMI scale labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Under", fontSize = 10.sp, color = BMIUnderweight)
                Text("Healthy", fontSize = 10.sp, color = BMIHealthy)
                Text("Over", fontSize = 10.sp, color = BMIOverweight)
                Text("Obese", fontSize = 10.sp, color = BMIObese)
            }
        }
    }
}

// ============ ANIMATED STAT CARD ============
@Composable
fun AnimatedStatCard(
    modifier: Modifier = Modifier,
    value: Float,
    label: String,
    unit: String,
    icon: ImageVector,
    progress: Float,
    gradientColors: List<Color>,
    animationTriggered: Boolean,
    isStreak: Boolean = false,
    showAsInt: Boolean = false
) {
    val animatedValue by animateFloatAsState(
        targetValue = if (animationTriggered) value else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "value"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) progress else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )
    
    // Accessibility description
    val accessibilityDescription = if (showAsInt) {
        "$label: ${animatedValue.toInt()} $unit"
    } else {
        "$label: ${String.format("%.1f", animatedValue)} $unit"
    }
    
    Surface(
        modifier = modifier
            .scale(scale)
            .height(140.dp)
            .semantics { contentDescription = accessibilityDescription },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.horizontalGradient(gradientColors),
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(gradientColors),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    // Mini progress ring
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color(0xFFE5E7EB),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(gradientColors),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedText
                        )
                    }
                }
                
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (showAsInt) animatedValue.toInt().toString() 
                                   else String.format("%.1f", animatedValue),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = " $unit",
                                fontSize = 14.sp,
                                color = MutedText,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        color = MutedText
                    )
                }
            }
        }
    }
}

// ============ WEIGHT PROGRESS CARD ============
@Composable
fun WeightProgressCard(
    currentWeight: Float,
    goalWeight: Float,
    progress: Float,
    animationTriggered: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}% complete",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GradientCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(GradientCyan, GradientBlue)),
                            RoundedCornerShape(6.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                    Text(
                        text = "${currentWeight.toInt()} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Goal",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                    Text(
                        text = "${goalWeight.toInt()} kg",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GradientCyan
                    )
                }
            }
        }
    }
}

// ============ MACRO DONUT CARD ============
@Composable
fun MacroDonutCard(
    protein: Float,
    carbs: Float,
    fats: Float,
    proteinGoal: Int,
    carbsGoal: Int,
    fatsGoal: Int,
    animationTriggered: Boolean
) {
    val total = protein + carbs + fats
    val proteinPercent = if (total > 0) protein / total else 0.33f
    val carbsPercent = if (total > 0) carbs / total else 0.33f
    val fatsPercent = if (total > 0) fats / total else 0.33f
    
    val animatedProtein by animateFloatAsState(
        targetValue = if (animationTriggered) proteinPercent else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "protein"
    )
    val animatedCarbs by animateFloatAsState(
        targetValue = if (animationTriggered) carbsPercent else 0f,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "carbs"
    )
    val animatedFats by animateFloatAsState(
        targetValue = if (animationTriggered) fatsPercent else 0f,
        animationSpec = tween(1600, easing = FastOutSlowInEasing),
        label = "fats"
    )
    
    val proteinColor = Color(0xFFEF4444)
    val carbsColor = Color(0xFFF59E0B)
    val fatsColor = Color(0xFF3B82F6)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today's Macros",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut chart
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 20.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        var startAngle = -90f
                        
                        // Protein arc
                        drawArc(
                            color = proteinColor,
                            startAngle = startAngle,
                            sweepAngle = 360f * animatedProtein,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += 360f * animatedProtein
                        
                        // Carbs arc
                        drawArc(
                            color = carbsColor,
                            startAngle = startAngle,
                            sweepAngle = 360f * animatedCarbs,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += 360f * animatedCarbs
                        
                        // Fats arc
                        drawArc(
                            color = fatsColor,
                            startAngle = startAngle,
                            sweepAngle = 360f * animatedFats,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${total.toInt()}g",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                        Text(
                            text = "Total",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }
                
                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroLegendRow(
                        color = proteinColor,
                        label = "Protein",
                        value = protein,
                        goal = proteinGoal
                    )
                    MacroLegendRow(
                        color = carbsColor,
                        label = "Carbs",
                        value = carbs,
                        goal = carbsGoal
                    )
                    MacroLegendRow(
                        color = fatsColor,
                        label = "Fats",
                        value = fats,
                        goal = fatsGoal
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroLegendRow(
    color: Color,
    label: String,
    value: Float,
    goal: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = DarkText
            )
            Text(
                text = "${value.toInt()}g / ${goal}g",
                fontSize = 11.sp,
                color = MutedText
            )
        }
    }
}

// ============ WEEKLY CALORIES CHART WITH GRID ============
@Composable
fun WeeklyCaloriesChart(
    weeklyData: List<DailyCalories>,
    calorieGoal: Int,
    animationTriggered: Boolean
) {
    // Round max to nearest 500 for cleaner grid
    val rawMax = maxOf(weeklyData.maxOfOrNull { it.calories } ?: calorieGoal, calorieGoal)
    val maxCalories = ((rawMax / 500) + 1) * 500
    val gridLines = 4 // Number of horizontal grid lines
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Calories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GradientCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Goal: $calorieGoal",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GradientCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart area with grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .height(160.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in gridLines downTo 0) {
                        Text(
                            text = "${(maxCalories * i / gridLines)}",
                            fontSize = 9.sp,
                            color = MutedText
                        )
                    }
                }
                
                // Chart with grid lines
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                ) {
                    // Grid lines canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gridColor = Color(0xFFE5E7EB)
                        val goalLineY = size.height * (1 - calorieGoal.toFloat() / maxCalories)
                        
                        // Draw horizontal grid lines
                        for (i in 0..gridLines) {
                            val y = size.height * i / gridLines
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        
                        // Draw dashed goal line
                        val dashWidth = 8.dp.toPx()
                        val dashGap = 4.dp.toPx()
                        var currentX = 0f
                        while (currentX < size.width) {
                            drawLine(
                                color = GradientOrange,
                                start = Offset(currentX, goalLineY),
                                end = Offset(minOf(currentX + dashWidth, size.width), goalLineY),
                                strokeWidth = 2.dp.toPx()
                            )
                            currentX += dashWidth + dashGap
                        }
                    }
                    
                    // Bars
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyData.forEachIndexed { index, day ->
                            val animatedHeight by animateFloatAsState(
                                targetValue = if (animationTriggered && day.calories > 0) 
                                    (day.calories.toFloat() / maxCalories) else 0f,
                                animationSpec = tween(
                                    durationMillis = 800,
                                    delayMillis = index * 100,
                                    easing = FastOutSlowInEasing
                                ),
                                label = "bar$index"
                            )
                            
                            val isOverGoal = day.calories > calorieGoal
                            val barColor = if (isOverGoal) GradientPink else GradientCyan
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(24.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animatedHeight.coerceIn(0f, 1f))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(barColor, barColor.copy(alpha = 0.6f))
                                                ),
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // X-axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklyData.forEach { day ->
                    Text(
                        text = day.day.take(1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedText,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(GradientCyan, CircleShape)
                )
                Text(" Under", fontSize = 11.sp, color = MutedText)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(GradientPink, CircleShape)
                )
                Text(" Over", fontSize = 11.sp, color = MutedText)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(GradientOrange)
                )
                Text(" Goal", fontSize = 11.sp, color = MutedText)
            }
        }
    }
}

// ============ ENHANCED DAY STREAK CARD ============
@Composable
fun DayStreakCard(
    streak: Int,
    bestStreak: Int = streak, // Added best streak parameter
    completedDays: List<Boolean>,
    animationTriggered: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )
    
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    
    // Milestone badges
    val milestones = listOf(
        7 to "🔥 Week Warrior",
        14 to "💪 Two Week Champion",
        30 to "🏆 Monthly Master",
        60 to "⭐ Stellar Streak",
        90 to "👑 Ultimate Champion"
    )
    val unlockedMilestones = milestones.filter { it.first <= bestStreak }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current vs Best streak header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Current Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .scale(flameScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Whatshot,
                            contentDescription = "Current Streak",
                            modifier = Modifier.size(64.dp),
                            tint = GradientOrange
                        )
                        Text(
                            text = streak.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.offset(y = 4.dp)
                        )
                    }
                    Text(
                        text = "Current",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                    Text(
                        text = "$streak days",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .background(Color(0xFFE5E7EB))
                )
                
                // Best Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = "Best Streak",
                            modifier = Modifier.size(48.dp),
                            tint = GradientPurple
                        )
                    }
                    Text(
                        text = "Best",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                    Text(
                        text = "$bestStreak days",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GradientPurple
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Week indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekDays.forEachIndexed { index, day ->
                    val isCompleted = completedDays.getOrElse(index) { false }
                    
                    val animatedScale by animateFloatAsState(
                        targetValue = if (animationTriggered && isCompleted) 1f else 0.8f,
                        animationSpec = tween(400, delayMillis = index * 50),
                        label = "dayScale$index"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(animatedScale)
                    ) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) DarkText else MutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isCompleted) 
                                        Brush.linearGradient(listOf(GradientCyan, GradientBlue))
                                    else 
                                        Brush.linearGradient(listOf(Color(0xFFE5E7EB), Color(0xFFE5E7EB))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = "Day completed",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Milestone badges (if any unlocked)
            if (unlockedMilestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Milestones Unlocked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedText
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    unlockedMilestones.takeLast(3).forEach { (days, badge) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GradientPurple.copy(alpha = 0.1f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = badge.split(" ").first(), // Just the emoji
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============ ENHANCED MOTIVATIONAL CARD WITH ROTATING QUOTES ============
@Composable
fun MotivationalCard(
    progress: Float,
    streak: Int
) {
    // Inspirational quotes that rotate
    val quotes = listOf(
        "💫 Small steps lead to big changes",
        "🌟 Consistency beats perfection every time",
        "💪 Your body hears everything your mind says",
        "🎯 Progress, not perfection, is what matters",
        "🔥 Every healthy choice is a victory",
        "⭐ You're stronger than you think",
        "🚀 The only bad workout is the one that didn't happen",
        "🌈 Trust the process, embrace the journey"
    )
    
    // Context-aware message based on progress
    val contextMessage = when {
        progress >= 0.9f -> "Almost there! You're so close to your goal! 🎯"
        progress >= 0.5f -> "Halfway there! Keep pushing forward! 💪"
        streak >= 7 -> "Incredible streak! You're unstoppable! 🔥"
        streak >= 3 -> "Great consistency! Keep the momentum! ⚡"
        else -> null
    }
    
    var currentQuoteIndex by remember { mutableStateOf(0) }
    
    // Auto-rotate quotes every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentQuoteIndex = (currentQuoteIndex + 1) % quotes.size
        }
    }
    
    val displayMessage = contextMessage ?: quotes[currentQuoteIndex]
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(GradientCyan.copy(alpha = 0.12f), GradientPurple.copy(alpha = 0.12f))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Brush.linearGradient(listOf(GradientCyan, GradientPurple)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = "Motivation tip",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Motivation",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AnimatedContent(
                        targetState = displayMessage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith 
                            fadeOut(animationSpec = tween(500))
                        },
                        label = "quoteAnimation"
                    ) { message ->
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkText,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    ProgressContent(
        uiState = ProgressUiState(
            currentWeight = 70f,
            goalWeight = 65f,
            height = 175,
            bmi = 22.9f,
            bmiCategory = "Healthy",
            weightProgress = 0.6f,
            calorieGoal = 2000,
            todayCalories = 1450,
            todayProtein = 80f,
            todayCarbs = 120f,
            todayFats = 45f,
            dayStreak = 5,
            completedDays = listOf(true, true, true, true, true, false, false),
            weeklyCalories = listOf(
                DailyCalories("Sun", 1800, 90f, 150f, 60f),
                DailyCalories("Mon", 2100, 100f, 180f, 70f),
                DailyCalories("Tue", 1950, 95f, 160f, 65f),
                DailyCalories("Wed", 2200, 110f, 190f, 75f),
                DailyCalories("Thu", 1750, 85f, 140f, 55f),
                DailyCalories("Fri", 0, 0f, 0f, 0f),
                DailyCalories("Sat", 0, 0f, 0f, 0f)
            ),
            isLoading = false
        )
    )
}
