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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.DateRange
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
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import kotlinx.coroutines.delay
import java.time.LocalDate
import androidx.compose.ui.graphics.PathEffect
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.geometry.Rect
import java.time.format.DateTimeFormatter
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt

import com.example.calview.core.ui.walkthrough.WalkthroughOverlay
import com.example.calview.core.ui.walkthrough.WalkthroughStep
import com.example.calview.core.ui.walkthrough.onPositionedRect

// Modern color palette
private val GradientCyan = Color(0xFF059669) // Darker Emerald Green
private val GradientPurple = Color(0xFF7C3AED)
private val GradientPink = Color(0xFFEC4899)
private val GradientBlue = Color(0xFF3B82F6)
private val GradientOrange = Color(0xFFF59E0B)
private val CardBackground = Color(0xFFFAFAFA)
private val DarkText = Color(0xFF1F2937)
private val MutedText = Color(0xFF6B7280)

// BMI Colors
private val BMIUnderweight = Color(0xFF3B82F6)
private val BMIHealthy = Color(0xFF059669) // Darker Emerald Green
private val BMIOverweight = Color(0xFFF59E0B)
private val BMIObese = Color(0xFFEF4444)

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    ProgressContent(
        uiState = uiState,
        onRefresh = { viewModel.refreshData() },
        onUseStreakFreeze = { viewModel.useStreakFreeze() },
        onDismissWalkthrough = { viewModel.setHasSeenWalkthrough(true) },
        onWeekSelected = { viewModel.setWeekOffset(it) }
    )
}

@Composable
fun ProgressContent(
    uiState: ProgressUiState,
    onRefresh: () -> Unit = {},
    onUseStreakFreeze: () -> Unit = {},
    onDismissWalkthrough: () -> Unit = {},
    onWeekSelected: (Int) -> Unit = {}
) {
    var animationTriggered by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animationTriggered = true
    }
    
    // Walkthrough state
    var streakRect by remember { mutableStateOf<Rect?>(null) }
    var goalRect by remember { mutableStateOf<Rect?>(null) }
    var chartRect by remember { mutableStateOf<Rect?>(null) }
    var macroRect by remember { mutableStateOf<Rect?>(null) }
    var activityRect by remember { mutableStateOf<Rect?>(null) }
    var bmiRect by remember { mutableStateOf<Rect?>(null) }
    
    var currentStepIndex by remember(uiState.hasSeenWalkthrough) { 
        mutableIntStateOf(if (!uiState.hasSeenWalkthrough) 0 else -1) 
    }
    
    val walkthroughSteps = listOf(
        WalkthroughStep(
            id = "streak",
            title = stringResource(R.string.walkthrough_streak_title),
            description = stringResource(R.string.walkthrough_streak_desc),
            targetRect = streakRect
        ),
        WalkthroughStep(
            id = "goal",
            title = stringResource(R.string.walkthrough_goal_title),
            description = stringResource(R.string.walkthrough_goal_desc),
            targetRect = goalRect
        ),
        WalkthroughStep(
            id = "nutrition",
            title = stringResource(R.string.walkthrough_nutrition_title),
            description = stringResource(R.string.walkthrough_nutrition_desc),
            targetRect = chartRect
        ),
        WalkthroughStep(
            id = "macro",
            title = stringResource(R.string.walkthrough_macro_title),
            description = stringResource(R.string.walkthrough_macro_desc),
            targetRect = macroRect
        ),
        WalkthroughStep(
            id = "activity",
            title = stringResource(R.string.activity_title),
            description = stringResource(R.string.walkthrough_activity_desc),
            targetRect = activityRect
        ),
        WalkthroughStep(
            id = "bmi",
            title = stringResource(R.string.walkthrough_bmi_title),
            description = stringResource(R.string.walkthrough_bmi_desc),
            targetRect = bmiRect
        )
    )
    
    // Captured top position of the scrollable container
    var containerTop by remember { mutableStateOf(0f) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    // Get adaptive layout values based on screen size
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
    
    val scrollState = rememberScrollState()
    
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp.value

    // Auto-scroll logic for walkthrough
    LaunchedEffect(currentStepIndex) {
        if (currentStepIndex >= 0 && currentStepIndex < walkthroughSteps.size) {
            val step = walkthroughSteps[currentStepIndex]
            
            // Wait for layout and rects
            delay(200) 
            
            step.targetRect?.let { rect ->
                // Calculate position relative to container and center it
                // targetOffset = (absoluteItemTop - absoluteContainerTop) + currentScroll - (halfViewportHeight)
                val viewportHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                val itemTopInContainer = rect.top - containerTop + scrollState.value
                val itemHeight = rect.height
                val scrollTarget = itemTopInContainer - (viewportHeightPx / 2) + (itemHeight / 2)
                
                scrollState.animateScrollTo(
                    scrollTarget.toInt().coerceIn(0, scrollState.maxValue),
                    animationSpec = tween(700, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.calview.core.ui.theme.CalViewTheme.gradient),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .statusBarsPadding() // Handle edge-to-edge for status bar
                .onGloballyPositioned { containerTop = it.positionInWindow().y }
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // Header - simplified without refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.progress_title),
                    fontFamily = InterFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.track_health_journey),
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        
        val isTablet = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        
        // 0. Mission Control (Combined Summary)
        MissionControlCard(
            level = uiState.userLevel,
            xp = uiState.userXp,
            xpRequired = uiState.xpRequired,
            checklistItems = uiState.checklistItems
        )
        
        
        // 1.5. & 1.6. Streak Cards (Adaptive)
        if (isTablet) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    DayStreakCard(
                        modifier = Modifier.onPositionedRect { streakRect = it },
                        streak = uiState.dayStreak,
                        completedDays = uiState.completedDays,
                        animationTriggered = animationTriggered
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StreakFreezeCard(
                        remainingFreezes = uiState.remainingFreezes,
                        maxFreezes = uiState.maxFreezes,
                        canUseFreeze = uiState.remainingFreezes > 0, 
                        yesterdayMissed = uiState.yesterdayMissed,
                        currentStreak = uiState.dayStreak,
                        onUseFreeze = onUseStreakFreeze
                    )
                }
            }
        } else {
            DayStreakCard(
                modifier = Modifier.onPositionedRect { streakRect = it },
                streak = uiState.dayStreak,
                completedDays = uiState.completedDays,
                animationTriggered = animationTriggered
            )
            
            StreakFreezeCard(
                remainingFreezes = uiState.remainingFreezes,
                maxFreezes = uiState.maxFreezes,
                canUseFreeze = uiState.remainingFreezes > 0, 
                yesterdayMissed = uiState.yesterdayMissed,
                currentStreak = uiState.dayStreak,
                onUseFreeze = onUseStreakFreeze
            )
        }
        
        // 1.7. Goal Journey Card
        if (uiState.showGoalJourney) {
            GoalJourneyCard(
                modifier = Modifier.onPositionedRect { goalRect = it },
                goal = uiState.userGoal,
                currentWeight = uiState.currentWeight,
                targetWeight = uiState.goalWeight,
                weeklyPace = uiState.weeklyPace,
                weeksToGoal = uiState.weeksToGoal,
                weightDiff = uiState.weightDiff,
                estimatedGoalDate = uiState.estimatedGoalDate
            )
        }
        
        
        // 2. Weight Progress Card
        WeightProgressCard(
            currentWeight = uiState.currentWeight,
            goalWeight = uiState.goalWeight,
            progress = uiState.weightProgress,
            animationTriggered = animationTriggered
        )
        
        // 2.6. Weight History Graph with time filters
        if (uiState.weightHistory.isNotEmpty()) {
            WeightHistoryGraphCard(
                weightHistory = uiState.weightHistory,
                goalWeight = uiState.goalWeight,
                animationTriggered = animationTriggered
            )
        }
        
        // 3. Weekly Calories Chart
        WeeklyCaloriesChart(
            modifier = Modifier.onPositionedRect { chartRect = it },
            weeklyData = uiState.weeklyCalories,
            calorieGoal = uiState.calorieGoal,
            animationTriggered = animationTriggered,
            selectedWeekOffset = uiState.selectedWeekOffset,
            onWeekSelected = onWeekSelected
        )
        
        
        // 5. Combined Activity Overview Card
        Box(modifier = Modifier.fillMaxWidth().onPositionedRect { activityRect = it }) {
            ActivityOverviewCard(
                todaySteps = uiState.todaySteps,
                stepsGoal = uiState.stepsGoal,
                caloriesBurned = uiState.caloriesBurned,
                manualExerciseCalories = uiState.manualExerciseCalories,
                weeklySteps = uiState.weeklySteps,
                weeklyCaloriesBurned = (uiState.weeklyCaloriesBurned + uiState.weeklyExerciseCalories).toInt(),
                caloriesRecord = uiState.caloriesBurnedRecord.toInt(),
                animationTriggered = animationTriggered
            )
        }

        // 6. BMI Card - Last with height/weight
        CompactBMICard(
            modifier = Modifier.onPositionedRect { bmiRect = it },
            bmi = uiState.bmi,
            bmiCategory = stringResource(uiState.bmiCategory),
            height = uiState.height.toFloat(),
            weight = uiState.currentWeight,
            animationTriggered = animationTriggered
        )
        
        Spacer(modifier = Modifier.height(80.dp))
        } // Close Column

        // Walkthrough Overlay
        WalkthroughOverlay(
            steps = walkthroughSteps,
            currentStepIndex = currentStepIndex,
            onNext = {
                if (currentStepIndex < walkthroughSteps.size - 1) {
                    currentStepIndex++
                } else {
                    currentStepIndex = -1
                    onDismissWalkthrough()
                }
            },
            onSkip = {
                currentStepIndex = -1
                onDismissWalkthrough()
            }
        )
    } // Close Box
}

// ============ COMPACT BMI CARD ============
@Composable
fun CompactBMICard(
    modifier: Modifier = Modifier,
    bmi: Float,
    bmiCategory: String,
    height: Float = 0f,  // in cm
    weight: Float = 0f,  // in kg
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
    
    val bmiDescription = stringResource(R.string.bmi_content_desc, bmi, bmiCategory)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = bmiDescription },
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
                    text = stringResource(R.string.bmi_title),
                    fontFamily = InterFontFamily,
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
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = animatedColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = bmiCategory,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = animatedColor
                        )
                    }
                }
            }
            
            // Height and Weight display
            if (height > 0 || weight > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (height > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Height,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.unit_cm_format, height.toInt()),
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.02).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (weight > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Scale,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.unit_kg_format, weight),
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.02).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                Text(stringResource(R.string.bmi_underweight), fontFamily = InterFontFamily, fontSize = 10.sp, color = BMIUnderweight)
                Text(stringResource(R.string.bmi_healthy), fontFamily = InterFontFamily, fontSize = 10.sp, color = BMIHealthy)
                Text(stringResource(R.string.bmi_overweight), fontFamily = InterFontFamily, fontSize = 10.sp, color = BMIOverweight)
                Text(stringResource(R.string.bmi_obese), fontFamily = InterFontFamily, fontSize = 10.sp, color = BMIObese)
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
    showAsInt: Boolean = false,
    subLabel: String? = null  // Optional sub-label for breakdown (e.g., "+50 from exercise")
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
        color = MaterialTheme.colorScheme.surface,
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
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = trackColor,
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
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (unit.isNotEmpty()) {
                            Text(
                                text = " $unit",
                                fontFamily = InterFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = InterFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        // Show sub-label if provided (e.g., exercise calories)
                        if (subLabel != null) {
                            Text(
                                text = subLabel,
                                fontFamily = InterFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weight_goal_title),
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.percent_complete, (animatedProgress * 100).toInt()),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = GradientCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val fillBrush = if (isDark) Brush.horizontalGradient(listOf(Color.White, Color.White.copy(alpha = 0.8f)))
                           else Brush.horizontalGradient(listOf(Color.Black, Color.Black.copy(alpha = 0.8f)))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            fillBrush,
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
                        text = stringResource(R.string.current_label),
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currentWeight.toInt()} kg",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.goal_label),
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${goalWeight.toInt()} kg",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
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
    modifier: Modifier = Modifier,
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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.todays_macros_title),
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.total_label),
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MacroLegendRow(
                        color = proteinColor,
                        label = stringResource(R.string.macro_protein),
                        value = protein,
                        goal = proteinGoal
                    )
                    MacroLegendRow(
                        color = carbsColor,
                        label = stringResource(R.string.macro_carbs),
                        value = carbs,
                        goal = carbsGoal
                    )
                    MacroLegendRow(
                        color = fatsColor,
                        label = stringResource(R.string.macro_fats),
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
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${value.toInt()}g / ${goal}g",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.02).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============ WEEKLY CALORIES CHART WITH GRID ============
@Composable
fun WeeklyCaloriesChart(
    modifier: Modifier = Modifier,
    weeklyData: List<DailyCalories>,
    calorieGoal: Int,
    animationTriggered: Boolean,
    selectedWeekOffset: Int = 0,
    onWeekSelected: (Int) -> Unit = {}
) {
    // Week selection options
    val weekOptions = listOf(
        0 to stringResource(R.string.week_this),
        1 to stringResource(R.string.week_last),
        2 to stringResource(R.string.week_2_ago),
        3 to stringResource(R.string.week_3_ago)
    )

    // Round max to nearest 500 for cleaner grid
    val rawMax = maxOf(weeklyData.maxOfOrNull { it.calories } ?: calorieGoal, calorieGoal)
    val maxCalories = ((rawMax / 500) + 1) * 500
    val gridLines = 4 // Number of horizontal grid lines

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_calories_title),
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GradientCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = stringResource(R.string.goal_value_format, calorieGoal),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = GradientCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Week selection buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekOptions.forEach { (offset, label) ->
                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    val highlightColor = if (isDark) Color.White else Color.Black
                    val isSelected = selectedWeekOffset == offset
                    Surface(
                        onClick = { onWeekSelected(offset) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) highlightColor else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) (if (isDark) Color.Black else Color.White) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chart area with grid
            val chartDesc = stringResource(R.string.desc_weekly_calories_graph)
            Row(modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = chartDesc }
            ) {
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
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animatedHeight.coerceIn(0f, 1f))
                                            .background(
                                                if (isOverGoal) {
                                                    Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.6f)))
                                                } else {
                                                    if (isDark) Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.6f)))
                                                    else Brush.verticalGradient(listOf(Color.Black, Color.Black.copy(alpha = 0.6f)))
                                                },
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
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Text(" " + stringResource(R.string.legend_under), fontFamily = InterFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(GradientPink, CircleShape)
                )
                Text(" " + stringResource(R.string.legend_over), fontFamily = InterFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(2.dp)
                        .background(GradientOrange)
                )
                Text(" " + stringResource(R.string.legend_goal), fontFamily = InterFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ============ ENHANCED DAY STREAK CARD ============
@Composable
fun DayStreakCard(
    modifier: Modifier = Modifier,
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
        7 to stringResource(R.string.milestone_week_warrior),
        14 to stringResource(R.string.milestone_two_week_champion),
        30 to stringResource(R.string.milestone_monthly_master),
        60 to stringResource(R.string.milestone_stellar_streak),
        90 to stringResource(R.string.milestone_ultimate_champion)
    )
    val unlockedMilestones = milestones.filter { it.first <= bestStreak }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
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
                            contentDescription = stringResource(R.string.current_streak_desc),
                            modifier = Modifier.size(64.dp),
                            tint = GradientOrange
                        )
                        Text(
                            text = streak.toString(),
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = Color.White,
                            modifier = Modifier.offset(y = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.streak_current),
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = pluralStringResource(R.plurals.days_count, streak, streak),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
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
                            contentDescription = stringResource(R.string.best_streak_desc),
                            modifier = Modifier.size(48.dp),
                            tint = GradientPurple
                        )
                    }
                    Text(
                        text = stringResource(R.string.streak_best),
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = pluralStringResource(R.plurals.days_count, bestStreak, bestStreak),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
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
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isCompleted) {
                                        if (isDark) Brush.linearGradient(listOf(Color.White, Color.White))
                                        else Brush.linearGradient(listOf(Color.Black, Color.Black))
                                    } else {
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)))
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = stringResource(R.string.day_completed_desc),
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isDark) Color.Black else Color.White
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
                    text = stringResource(R.string.milestones_unlocked),
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                fontFamily = InterFontFamily,
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
    val quotes = stringArrayResource(R.array.motivational_quotes).toList()
    
    // Context-aware message based on progress
    val contextMessage = when {
        progress >= 0.9f -> stringResource(R.string.msg_almost_there)
        progress >= 0.5f -> stringResource(R.string.msg_halfway_there)
        streak >= 7 -> stringResource(R.string.msg_incredible_streak)
        streak >= 3 -> stringResource(R.string.msg_great_consistency)
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
                        contentDescription = stringResource(R.string.motivation_access_desc),
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.daily_motivation),
                        fontFamily = InterFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// ============ MICRONUTRIENT STATS ROW ============
@Composable
fun MicronutrientStatsRow(
    fiber: Float,
    sugar: Float,
    sodium: Float,
    fiberGoal: Float,
    sugarGoal: Float,
    sodiumGoal: Float,
    animationTriggered: Boolean
) {
    val fiberColor = Color(0xFF66BB6A)
    val sugarColor = Color(0xFFFF7043)
    val sodiumColor = Color(0xFF9575CD)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.micronutrients_title),
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MicronutrientItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.micro_fiber),
                    value = fiber,
                    goal = fiberGoal,
                    unit = "g",
                    color = fiberColor,
                    icon = Icons.Filled.Grass,
                    animationTriggered = animationTriggered
                )
                MicronutrientItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.micro_sugar),
                    value = sugar,
                    goal = sugarGoal,
                    unit = "g",
                    color = sugarColor,
                    icon = Icons.Filled.Cookie,
                    animationTriggered = animationTriggered
                )
                MicronutrientItem(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.micro_sodium),
                    value = sodium,
                    goal = sodiumGoal,
                    unit = "mg",
                    color = sodiumColor,
                    icon = Icons.Filled.Science,
                    animationTriggered = animationTriggered
                )
            }
        }
    }
}

@Composable
private fun MicronutrientItem(
    modifier: Modifier = Modifier,
    label: String,
    value: Float,
    goal: Float,
    unit: String,
    color: Color,
    icon: ImageVector,
    animationTriggered: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) (value / goal).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (unit == "mg") "${value.toInt()}" else String.format("%.1f", value),
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$unit / ${goal.toInt()}$unit",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            bmiCategory = R.string.bmi_healthy,
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

// ============ WEEKLY ACTIVITY CARD ============
@Composable
fun WeeklyActivityCard(
    weeklySteps: Long,
    weeklyCalories: Double,
    caloriesRecord: Double,  // Highest daily calories burned in 7 days
    animationTriggered: Boolean,
    exerciseCalories: Int = 0  // Weekly exercise calories
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Weekly Steps
            WeeklyStatItem(
                icon = Icons.Outlined.DirectionsWalk,
                value = weeklySteps.toFloat(),
                label = stringResource(R.string.weekly_steps_label),
                color = GradientPurple,
                animationTriggered = animationTriggered,
                showAsInt = true
            )
            
            // Weekly Calories (with exercise indicator)
            WeeklyStatItem(
                icon = Icons.Outlined.LocalFireDepartment,
                value = weeklyCalories.toFloat(),
                label = stringResource(R.string.weekly_burn_label),
                color = GradientOrange,
                animationTriggered = animationTriggered,
                showAsInt = true,
                subLabel = if (exerciseCalories > 0) stringResource(R.string.exercise_sublabel_format, exerciseCalories) else null
            )
            
            // Best Record (Max calories burned in a day for past 7 days)
            WeeklyStatItem(
                icon = Icons.Outlined.EmojiEvents,
                value = caloriesRecord.toFloat(),
                label = stringResource(R.string.record_label),
                color = GradientPink,
                animationTriggered = animationTriggered,
                showAsInt = true
            )
        }
    }
}

@Composable
private fun WeeklyStatItem(
    icon: ImageVector,
    value: Float,
    label: String,
    color: Color,
    animationTriggered: Boolean,
    showAsInt: Boolean = false,
    subLabel: String? = null
) {
    val animatedValue by animateFloatAsState(
        targetValue = if (animationTriggered) value else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "value"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = if (showAsInt) animatedValue.toInt().toString() else String.format("%.1f", animatedValue),
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.02).sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Show sub-label if provided (e.g., exercise calories)
        if (subLabel != null) {
            Text(
                text = subLabel,
                fontFamily = InterFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun StreakFreezeCard(
    modifier: Modifier = Modifier,
    remainingFreezes: Int,
    maxFreezes: Int,
    canUseFreeze: Boolean,
    yesterdayMissed: Boolean,
    currentStreak: Int,
    onUseFreeze: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(GradientBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Icecream, // Use Icecream as a fun "freeze" icon or snowflake if available
                            contentDescription = null,
                            tint = GradientBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.streak_freeze_title),
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = stringResource(R.string.available_freezes_format, remainingFreezes, maxFreezes),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (yesterdayMissed && canUseFreeze) {
               Text(
                   text = stringResource(R.string.missed_day_msg, currentStreak),
                   fontFamily = InterFontFamily,
                   fontSize = 14.sp,
                   fontWeight = FontWeight.Normal,
                   color = MaterialTheme.colorScheme.onSurface,
                   lineHeight = 20.sp
               )
               Spacer(modifier = Modifier.height(16.dp))
               Button(
                   onClick = onUseFreeze, 
                   modifier = Modifier.fillMaxWidth(),
                   shape = RoundedCornerShape(12.dp)
               ) {
                   Text(stringResource(R.string.use_freeze_action), fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold)
               }
            } else {
               Text(
                    text = if (remainingFreezes >0) stringResource(R.string.streak_safe_msg) else "No freezes remaining for this month.",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ============ COMBINED ACTIVITY OVERVIEW CARD ============
@Composable
fun ActivityOverviewCard(
    todaySteps: Int,
    stepsGoal: Int,
    caloriesBurned: Int,
    manualExerciseCalories: Int,
    weeklySteps: Long,
    weeklyCaloriesBurned: Int,
    caloriesRecord: Int,
    animationTriggered: Boolean
) {
    val stepsProgress by animateFloatAsState(
        targetValue = if (animationTriggered) (todaySteps.toFloat() / stepsGoal).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "stepsProgress"
    )
    val burnProgress by animateFloatAsState(
        targetValue = if (animationTriggered) (caloriesBurned / kotlin.math.max(caloriesRecord.toFloat(), 3000f)).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "burnProgress"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.activity_title),
                    fontFamily = InterFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GradientCyan.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.today_label),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GradientCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Today's Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Steps
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                            drawArc(brush = Brush.sweepGradient(listOf(GradientPurple, GradientPink)), startAngle = -90f, sweepAngle = 360f * stepsProgress, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Icon(Icons.Outlined.DirectionsWalk, contentDescription = null, modifier = Modifier.size(28.dp), tint = GradientPurple)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = todaySteps.toString(), fontFamily = SpaceGroteskFontFamily, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = stringResource(R.string.steps_label), fontFamily = InterFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Burned
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                            drawArc(brush = Brush.sweepGradient(listOf(GradientPink, GradientOrange)), startAngle = -90f, sweepAngle = 360f * burnProgress, useCenter = false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                        }
                        Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(28.dp), tint = GradientOrange)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = caloriesBurned.toString(), fontFamily = SpaceGroteskFontFamily, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = stringResource(R.string.burned_label), fontFamily = InterFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (manualExerciseCalories > 0) {
                            Text(text = stringResource(R.string.plus_value_format, manualExerciseCalories), fontFamily = InterFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            
            // 7-Day Summary Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = stringResource(R.string.weekly_steps_label), fontFamily = InterFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = weeklySteps.toString(), fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text(text = stringResource(R.string.weekly_burn_label), fontFamily = InterFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$weeklyCaloriesBurned ${stringResource(R.string.cal_unit)}", fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text(text = stringResource(R.string.record_label), fontFamily = InterFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(14.dp), tint = GradientOrange)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$caloriesRecord", fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

// ============ WEIGHT HISTORY GRAPH CARD ============
@Composable
fun WeightHistoryGraphCard(
    weightHistory: List<WeightEntry>,
    goalWeight: Float,
    animationTriggered: Boolean
) {
    var selectedRange by remember { mutableStateOf(0) } // 0=3m, 1=6m, 2=1y, 3=All
    val ranges = listOf(
        stringResource(R.string.range_3m),
        stringResource(R.string.range_6m),
        stringResource(R.string.range_1y),
        stringResource(R.string.range_all)
    )

    val rangeDays = listOf(90L, 180L, 365L, Long.MAX_VALUE)
    
    val filteredHistory = remember(weightHistory, selectedRange) {
        val cutoffDate = if (selectedRange < 3) {
            java.time.LocalDate.now().minusDays(rangeDays[selectedRange])
        } else {
            java.time.LocalDate.MIN
        }
        weightHistory.filter { it.date >= cutoffDate }.sortedBy { it.date }
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "graphProgress"
    )
    
    var selectedPointIndex by remember { mutableIntStateOf(-1) }
    var touchX by remember { mutableFloatStateOf(0f) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with title and filter buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weight_history_title),
                    fontFamily = InterFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ranges.forEachIndexed { index, label ->
                        val isSelected = selectedRange == index
                        Surface(
                            onClick = { selectedRange = index },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Weight change summary for the period
            if (filteredHistory.size >= 2) {
                val firstWeight = filteredHistory.first().weight
                val lastWeight = filteredHistory.last().weight
                val weightChange = lastWeight - firstWeight
                val trendUp = weightChange > 0
                val trendColor = when {
                    weightChange > 0.5f -> Color(0xFFE57373) // Red for gain
                    weightChange < -0.5f -> Color(0xFF81C784) // Green for loss
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (trendUp) "↑" else if (weightChange < 0) "↓" else "→",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f kg", kotlin.math.abs(weightChange)),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = trendColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (trendUp) "gained" else if (weightChange < 0) "lost" else "maintained",
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Graph
            if (filteredHistory.size >= 2) {
                val minWeight = (filteredHistory.minOf { it.weight } - 2f).coerceAtLeast(0f)
                val maxWeight = filteredHistory.maxOf { it.weight } + 2f
                val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)
                
                val graphDesc = stringResource(R.string.desc_weight_history_graph)
                
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    // Y-Axis Labels
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(32.dp)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 0..4) {
                            val labelWeight = maxWeight - (i * weightRange / 4)
                            Text(
                                text = labelWeight.toInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 36.dp)
                            .semantics { contentDescription = graphDesc }
                            .pointerInput(filteredHistory) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        touchX = offset.x
                                        val xStep = size.width / (filteredHistory.size - 1).coerceAtLeast(1)
                                        selectedPointIndex = (touchX / xStep).roundToInt().coerceIn(0, filteredHistory.size - 1)
                                    },
                                    onDrag = { change, _ ->
                                        touchX = change.position.x
                                        val xStep = size.width / (filteredHistory.size - 1).coerceAtLeast(1)
                                        selectedPointIndex = (touchX / xStep).roundToInt().coerceIn(0, filteredHistory.size - 1)
                                    },
                                    onDragEnd = { selectedPointIndex = -1 },
                                    onDragCancel = { selectedPointIndex = -1 }
                                )
                            }
                    ) {
                        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                        val curveColor = if (isDark) Color.White else Color.Black
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val xStep = width / (filteredHistory.size - 1).coerceAtLeast(1)
                            
                            val path = Path()
                            val filledPath = Path()
                            
                            filteredHistory.forEachIndexed { index, entry ->
                                val x = index * xStep * animatedProgress
                                val y = height - ((entry.weight - minWeight) / weightRange * height * animatedProgress)
                                if (index == 0) {
                                    path.moveTo(x, y)
                                    filledPath.moveTo(x, height)
                                    filledPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    filledPath.lineTo(x, y)
                                }
                                if (index == filteredHistory.size - 1) {
                                    filledPath.lineTo(x, height)
                                    filledPath.close()
                                }
                            }
                            
                            // Draw gradient fill
                            drawPath(
                                filledPath, 
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        curveColor.copy(alpha = 0.3f * animatedProgress),
                                        curveColor.copy(alpha = 0.05f * animatedProgress)
                                    )
                                )
                            )
                            
                            drawPath(path, curveColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                            
                            // Draw selection indicator
                            if (selectedPointIndex != -1) {
                                val x = selectedPointIndex * xStep
                                val entry = filteredHistory[selectedPointIndex]
                                val y = height - ((entry.weight - minWeight) / weightRange * height)
                                
                                drawLine(
                                    color = Color(0xFF6366F1).copy(alpha = 0.3f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 1.dp.toPx()
                                )
                                
                                drawCircle(
                                    color = Color(0xFF6366F1),
                                    radius = 6.dp.toPx(),
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            } else {
                                filteredHistory.forEachIndexed { index, entry ->
                                    val x = index * xStep * animatedProgress
                                    val y = height - ((entry.weight - minWeight) / weightRange * height * animatedProgress)
                                    drawCircle(Color(0xFF6366F1), 4.dp.toPx(), Offset(x, y))
                                    drawCircle(Color.White, 2.dp.toPx(), Offset(x, y))
                                }
                            }
                        }
                        
                        // Tooltip overlay
                        if (selectedPointIndex != -1) {
                            val entry = filteredHistory[selectedPointIndex]
                            val xStep = 1f / (filteredHistory.size - 1).coerceAtLeast(1)
                            val xPos = selectedPointIndex * xStep
                            
                            Surface(
                                modifier = Modifier
                                    .align(if (xPos > 0.5f) Alignment.TopStart else Alignment.TopEnd)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 4.dp
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text(
                                        text = "${entry.weight} kg",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGroteskFontFamily
                                    )
                                    Text(
                                        text = entry.date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                        fontSize = 10.sp,
                                        fontFamily = InterFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Date labels
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelCount = if (filteredHistory.size > 5) 5 else filteredHistory.size
                    val indices = if (filteredHistory.size > 1) {
                        List(labelCount) { i -> (i * (filteredHistory.size - 1) / (labelCount - 1)) }
                    } else listOf(0)
                    
                    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM d")
                    
                    indices.forEach { index ->
                        val entry = filteredHistory[index]
                        Text(
                            text = entry.date.format(dateFormatter),
                            fontFamily = InterFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.insufficient_data),
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
