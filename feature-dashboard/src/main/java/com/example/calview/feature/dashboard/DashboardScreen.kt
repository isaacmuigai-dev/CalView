package com.example.calview.feature.dashboard

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import com.example.calview.core.data.coach.CoachMessageGenerator
import com.example.calview.feature.dashboard.CoachTipCard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import com.example.calview.core.data.local.MealEntity
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import com.example.calview.feature.dashboard.components.CalorieRing
import com.example.calview.feature.dashboard.components.MacroStatsRow
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import com.example.calview.core.ui.walkthrough.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import com.example.calview.core.ui.util.rememberHapticsManager
import com.example.calview.core.ui.theme.CalViewTheme
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily

// Modern color palette for Activity Card
private val GradientCyan = Color(0xFF059669) // Darker Emerald Green
private val GradientPurple = Color(0xFF7C3AED)
private val GradientPink = Color(0xFFEC4899)
private val GradientOrange = Color(0xFFF59E0B)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    scrollToRecentUploads: Boolean = false,
    scrollToMealId: Long = -1L,
    onScrollHandled: () -> Unit = {},
    onSuggestionsClick: () -> Unit = {},
    onFastingClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {}
) {
    val state by viewModel.dashboardState.collectAsState()
    val context = LocalContext.current
    
    // Auto-scroll to Recent Uploads when coming from scanner
    // Item index 7 is approximately where "Recently uploaded" header starts
    val recentUploadsIndex = 7
    LaunchedEffect(Unit) {
        // Refresh health data whenever Dashboard is shown
        viewModel.refreshHealthData()
    }
    
    LaunchedEffect(scrollToRecentUploads) {
        if (scrollToRecentUploads) {
            // Small delay to ensure LazyColumn is rendered
            kotlinx.coroutines.delay(300)
            lazyListState.animateScrollToItem(recentUploadsIndex)
            onScrollHandled()
        }
    }
    
    LaunchedEffect(scrollToMealId, state.recentUploads) {
        if (scrollToMealId != -1L) {
             val index = state.recentUploads.indexOfFirst { it.id == scrollToMealId }
             if (index != -1) {
                 kotlinx.coroutines.delay(500) // Slightly longer delay to ensure list is populated
                 // Calculate base index dynamically based on visibility of items
                 var baseIndex = 0
                 baseIndex++ // HeaderSection
                 if (state.coachTip != null) baseIndex++ // CoachTipCard
                 // DateSelector is part of first item { ... } block with Header?
                 // Let's check block structure.
                 // Item 1: HeaderSection + CoachTip + DateSelector (Lines 304-319) - This is ONE item block.
                 
                 // Item 2: NutritionOverviewCard (Lines 321-344)
                 
                 // Item 3: Premium Features Row (Lines 347-398)
                 
                 // Item 4: HealthScoreCardPremium (Lines 401-406)
                 
                 // Item 5 (Conditional): Health Connect Button (Lines 409-480)
                 
                 // Item 6: UnifiedActivityCard (Lines 483-490)
                 
                 // Item 7: WaterCardPremium + Dialog (Lines 493-512)
                 
                 // Item 8: "Recently Uploaded" Text (Lines 514-522)
                 
                 // So items start at index 8 (if Health Connect shown = 9 items before list?)
                 // Let's count indices (0-based):
                 // 0: Header + Date
                 // 1: Nutrition
                 // 2: Premium Buttons
                 // 3: Health Score
                 // (If Health Connect): 4
                 // Next is Activity. If HC: 5. Else: 4.
                 // Next is Water. If HC: 6. Else: 5.
                 // Next is Text ("Recently Uploaded"). If HC: 7. Else: 6.
                 // First meal is at: If HC: 8. Else: 7.
                 
                 var currentCount = 3 // 0, 1, 2 are fixed (Header, Nutrition, HealthScore)
                 if (!state.isHealthConnected) currentCount++
                 currentCount++ // UnifiedActivity
                 currentCount++ // Water
                 currentCount++ // "Recently Uploaded" text
                 
                 val targetIndex = currentCount + index
                 lazyListState.animateScrollToItem(targetIndex)
                 onScrollHandled()
             }
        }
    }
    
    // Snackbar for burned calories notification
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State for showing Health Connect onboarding
    var showHealthOnboarding by rememberSaveable { mutableStateOf(false) }
    
    // State for showing streak lost dialog
    var showStreakLostDialog by rememberSaveable { mutableStateOf(false) }
    var hasShownStreakLostDialog by rememberSaveable { mutableStateOf(false) }
    
    // State for showing food detail screen
    var selectedMeal by remember { mutableStateOf<com.example.calview.core.data.local.MealEntity?>(null) }
    
    // NOTE: Removed annoying burned calories notification (snackbar)
    // The notification was triggered too frequently and displayed misleading calorie values
    
    // Check if streak lost and show dialog once per session
    // Specifically targeting the first time it's true after load
    // Check if streak lost and show dialog once per session
    LaunchedEffect(state.streakLost) {
        if (state.streakLost && !hasShownStreakLostDialog) {
            showStreakLostDialog = true
            hasShownStreakLostDialog = true
        }
    }
    
    // Health Connect permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = viewModel.healthConnectManager.createPermissionRequestContract()
    ) { granted ->
        android.util.Log.d("HealthConnect", "Permission result received!")
        android.util.Log.d("HealthConnect", "Granted permissions: $granted")
        android.util.Log.d("HealthConnect", "Required permissions: ${viewModel.healthConnectManager.permissions}")
        if (granted.containsAll(viewModel.healthConnectManager.permissions)) {
            android.util.Log.d("HealthConnect", "All permissions granted! Calling onHealthPermissionsGranted()")
            viewModel.onHealthPermissionsGranted()
        } else {
            android.util.Log.d("HealthConnect", "NOT all permissions granted")
        }
        showHealthOnboarding = false
    }
    
    // Launch permissions after onboarding
    val onLaunchPermissions: () -> Unit = {
        android.util.Log.d("HealthConnect", "onLaunchPermissions CALLED")
        val available = viewModel.healthConnectManager.isAvailable()
        android.util.Log.d("HealthConnect", "isAvailable returned: $available")
        if (available) {
            try {
                val perms = viewModel.healthConnectManager.permissions
                android.util.Log.d("HealthConnect", "About to launch with ${perms.size} permissions")
                permissionLauncher.launch(perms)
                android.util.Log.d("HealthConnect", "permissionLauncher.launch() COMPLETED")
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "EXCEPTION: ${e.message}", e)
                android.widget.Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } else {
            android.util.Log.d("HealthConnect", "Not available, showing toast")
            android.widget.Toast.makeText(
                context,
                "Health Connect is not available. Please install from Play Store.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Show onboarding screen when button is clicked
    val onConnectHealth: () -> Unit = {
        showHealthOnboarding = true
    }
    
    // Show Streak Lost Dialog
    if (showStreakLostDialog) {
        StreakLostDialog(
            currentStreak = state.currentStreak,
            completedDays = state.completedDays,
            onDismiss = { showStreakLostDialog = false }
        )
    }
    
    // Show Food Detail Screen if meal selected
    selectedMeal?.let { meal ->
        FoodDetailScreen(
            meal = meal,
            onBack = { selectedMeal = null },
            onDelete = { mealToDelete ->
                viewModel.deleteMeal(mealToDelete)
                selectedMeal = null
            },
            onUpdate = { updatedMeal ->
                viewModel.updateMeal(updatedMeal)
            },
            onRecalibrate = { mealToRecalibrate, additionalIngredients, servingCount ->
                viewModel.recalibrateMeal(mealToRecalibrate, additionalIngredients, servingCount)
            }
        )
        return
    }
    
    // Show Health Connect Onboarding or Dashboard
    if (showHealthOnboarding) {
        HealthConnectOnboardingScreen(
            onGoBack = { showHealthOnboarding = false },
            onGetStarted = onLaunchPermissions
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
        ) {
            DashboardContent(
                state = state,
                onDateSelected = { viewModel.selectDate(it) },
                onAddWater = { amount -> viewModel.addWater(amount) },
                onRemoveWater = { amount -> viewModel.removeWater(amount) },
                onConnectHealth = onConnectHealth,
                onMealClick = { selectedMeal = it },
                onFastingClick = onFastingClick,
                onChallengesClick = onChallengesClick,

                lazyListState = lazyListState,
                // Water Reminder Callbacks
                onWaterReminderEnabledChange = { viewModel.setWaterReminderEnabled(it) },
                onWaterReminderIntervalChange = { viewModel.setWaterReminderInterval(it) },
                onWaterReminderDailyGoalChange = { viewModel.setWaterReminderDailyGoal(it) },
                onWaterServingSizeChange = { viewModel.setWaterServingSize(it) },
                onUpgradeClick = { /* Navigate to Paywall - handled via top level nav if needed, or callback */ },
                onWalkthroughComplete = { viewModel.setHasSeenWalkthrough(true) }
            )
            
            // Snackbar host at bottom
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun DashboardContent(
    state: DashboardState,
    onDateSelected: (Calendar) -> Unit,
    onAddWater: (Int) -> Unit,
    onRemoveWater: (Int) -> Unit,
    onConnectHealth: () -> Unit = {},
    onMealClick: (com.example.calview.core.data.local.MealEntity) -> Unit = {},
    onFastingClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {},
    scrollToMealId: Long = -1L,
    // Scroll state for position memory
    lazyListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    // Water Reminder Callbacks
    onWaterReminderEnabledChange: (Boolean) -> Unit = {},
    onWaterReminderIntervalChange: (Int) -> Unit = {},
    onWaterReminderDailyGoalChange: (Int) -> Unit = {},
    onWaterServingSizeChange: (Int) -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onWalkthroughComplete: () -> Unit = {}
) {
    // Get adaptive layout values based on screen size
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
    
    // Walkthrough State
    var nutritionRect by remember { mutableStateOf<Rect?>(null) }
    var healthScoreRect by remember { mutableStateOf<Rect?>(null) }
    var waterRect by remember { mutableStateOf<Rect?>(null) }
    var calendarRect by remember { mutableStateOf<Rect?>(null) }
    var healthConnectRect by remember { mutableStateOf<Rect?>(null) }
    var recentMealsRect by remember { mutableStateOf<Rect?>(null) }
    var aiCoachRect by remember { mutableStateOf<Rect?>(null) }
    var weeklyActivityRect by remember { mutableStateOf<Rect?>(null) }
    
    var currentStepIndex by rememberSaveable(state.hasSeenWalkthrough) { 
        mutableIntStateOf(if (!state.hasSeenWalkthrough) 0 else -1) 
    }
    
    val walkthroughSteps = listOf(
        WalkthroughStep(
            id = "nutrition",
            title = stringResource(R.string.walkthrough_nutrition_title),
            description = stringResource(R.string.walkthrough_nutrition_desc),
            targetRect = nutritionRect
        ),
        WalkthroughStep(
            id = "calendar",
            title = stringResource(R.string.walkthrough_calendar_title),
            description = stringResource(R.string.walkthrough_calendar_desc),
            targetRect = calendarRect
        ),
        WalkthroughStep(
            id = "score",
            title = stringResource(R.string.walkthrough_score_title),
            description = stringResource(R.string.walkthrough_score_desc),
            targetRect = aiCoachRect
        ),
        WalkthroughStep(
            id = "activity",
            title = stringResource(R.string.activity_title), // "Activity"
            description = stringResource(R.string.walkthrough_activity_desc), // New string needed
            targetRect = weeklyActivityRect
        ),
        WalkthroughStep(
            id = "water",
            title = stringResource(R.string.walkthrough_water_title),
            description = stringResource(R.string.walkthrough_water_desc),
            targetRect = waterRect
        ),
        WalkthroughStep(
            id = "meal",
            title = stringResource(R.string.walkthrough_meal_title),
            description = stringResource(R.string.walkthrough_meal_desc),
            targetRect = recentMealsRect
        )
    )

    // Pager State (Hoisted for access in LaunchedEffect)
    val pagerState = rememberPagerState(pageCount = { 4 })

    // Auto-scroll logic for walkthrough steps to ensure target items are visible
    LaunchedEffect(currentStepIndex) {
        if (currentStepIndex != -1) {
            // Give layout a moment to settle and for rects to be captured
            kotlinx.coroutines.delay(150)
            
            // Skip Heath Connect step if already connected
            if (walkthroughSteps.getOrNull(currentStepIndex)?.id == "health" && state.isHealthConnected) {
                if (currentStepIndex < walkthroughSteps.lastIndex) {
                    currentStepIndex++
                }
                return@LaunchedEffect
            }
            
            // Scroll Pager to relevant page
            try {
                when (walkthroughSteps.getOrNull(currentStepIndex)?.id) {
                    "nutrition" -> pagerState.animateScrollToPage(0)
                    "score" -> { /* No pager scroll, it's below */ }
                    "health", "activity" -> pagerState.animateScrollToPage(2)
                    "water" -> pagerState.animateScrollToPage(1)
                }
            } catch (e: Exception) {
                // Ignore pager scroll errors
            }

            // Scroll LazyColumn to item
            val targetScrollIndex = when (walkthroughSteps.getOrNull(currentStepIndex)?.id) {
                "nutrition" -> 1 // Pager
                "calendar" -> 0 // Header
                "score" -> 2 // AI Coach (Item index 2)
                "health" -> 1 // Pager (Health Connect/Activity on Page 2)
                "water" -> 1 // Pager (Water on Page 1)
                "activity" -> 1 // Pager (Activity on Page 2)
                "meal" -> 4 // Recently Uploaded Header (Item index 4 based on view_file structure)
                else -> -1
            }
            
            if (targetScrollIndex != -1) {
                try {
                    lazyListState.animateScrollToItem(targetScrollIndex)
                } catch (e: Exception) {
                    android.util.Log.e("DashboardScreen", "Error scrolling to walkthrough item", e)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.calview.core.ui.theme.CalViewTheme.gradient),
        contentAlignment = Alignment.TopCenter
    ) {
        // Walkthrough Overlay
        if (currentStepIndex != -1) {
            WalkthroughOverlay(
                steps = walkthroughSteps,
                currentStepIndex = currentStepIndex,
                onNext = {
                    if (currentStepIndex < walkthroughSteps.size - 1) {
                        currentStepIndex++
                    } else {
                        currentStepIndex = -1
                        onWalkthroughComplete()
                    }
                },
                onSkip = {
                    currentStepIndex = -1
                    onWalkthroughComplete()
                }
            )
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .statusBarsPadding() // Handle edge-to-edge for status bar
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        item {
            DateSelector(
                modifier = Modifier.onPositionedRect { calendarRect = it },
                selectedDate = state.selectedDate, 
                onDateSelected = onDateSelected,
                allMealDates = state.allMealDates,
                streakDays = state.currentStreak
            )
        }

        item {
            // Pager State for horizontal scrollable cards
            
            // Shared state for eaten/left toggle across all nutrition cards
            var showEaten by rememberSaveable { mutableStateOf(true) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onPositionedRect { nutritionRect = it },
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    pageSpacing = 0.dp,
                    contentPadding = PaddingValues(0.dp)
                ) { page ->
                    when (page) {
                        // Page 1: Calories + Macros (Protein, Carbs, Fats)
                        0 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(265.dp), // Increased from 320.dp
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Calories Card
                                CaloriesCardRedesigned(
                                    remainingCalories = state.remainingCalories,
                                    isVisible = pagerState.currentPage == 0,
                                    consumedCalories = state.consumedCalories,
                                    goalCalories = state.goalCalories,
                                    rolloverCaloriesEnabled = state.rolloverCaloriesEnabled,
                                    rolloverCaloriesAmount = state.rolloverCaloriesAmount,
                                    burnedCalories = state.burnedCaloriesAdded,
                                    addCaloriesBackEnabled = state.addCaloriesBackEnabled,
                                    showEaten = showEaten,
                                    onToggle = { showEaten = !showEaten }
                                )
                                
                                // Macros Row (Protein, Carbs, Fats)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MacroCardRedesigned(
                                        label = "Protein",
                                        isVisible = pagerState.currentPage == 0,
                                        consumed = state.proteinG,
                                        goal = state.proteinGoal,
                                        unit = "g",
                                        icon = Icons.Filled.Favorite,
                                        iconTint = Color(0xFFE57373),
                                        showEaten = showEaten,
                                        onToggle = { showEaten = !showEaten },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroCardRedesigned(
                                        label = "Carbs",
                                        isVisible = pagerState.currentPage == 0,
                                        consumed = state.carbsG,
                                        goal = state.carbsGoal,
                                        unit = "g",
                                        icon = Icons.Filled.LocalFlorist,
                                        iconTint = Color(0xFFE5A87B),
                                        showEaten = showEaten,
                                        onToggle = { showEaten = !showEaten },
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroCardRedesigned(
                                        label = "Fats",
                                        isVisible = pagerState.currentPage == 0,
                                        consumed = state.fatsG,
                                        goal = state.fatsGoal,
                                        unit = "g",
                                        icon = Icons.Filled.WaterDrop,
                                        iconTint = Color(0xFF64B5F6),
                                        showEaten = showEaten,
                                        onToggle = { showEaten = !showEaten },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        
                        // Page 2: Micros (Fiber, Sugar, Sodium) + Water
                        1 -> {
                            var showWaterSettings by remember { mutableStateOf(false) }
                            var waterServingSize by remember(state.waterServingSize) { 
                                mutableIntStateOf(state.waterServingSize) 
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(265.dp), // Increased from 320.dp
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Micros Row (Fiber, Sugar, Sodium) - uniform height
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MacroCardRedesigned(
                                        label = "Fiber",
                                        isVisible = pagerState.currentPage == 1,
                                        consumed = state.fiberG,
                                        goal = state.fiberGoal,
                                        unit = "g",
                                        icon = Icons.Filled.Spa,
                                        iconTint = Color(0xFF9575CD),
                                        showEaten = showEaten,
                                        onToggle = { showEaten = !showEaten },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    MacroCardRedesigned(
                                        label = "Sugar",
                                        isVisible = pagerState.currentPage == 1,
                                        consumed = state.sugarG,
                                        goal = state.sugarGoal,
                                        unit = "g",
                                        icon = Icons.Filled.Cookie,
                                        iconTint = Color(0xFFF06292),
                                        showEaten = showEaten,
                                        onToggle = { showEaten = !showEaten },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    MacroCardRedesigned(
                                        label = "Sodium",
                                        isVisible = pagerState.currentPage == 1,
                                        consumed = state.sodiumG,
                                        goal = state.sodiumGoal,
                                        unit = "mg",
                                        icon = Icons.Filled.Grain,
                                        iconTint = Color(0xFFFFB74D),
                                        showEaten = showEaten,
                                        onToggle = { showEaten = !showEaten },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                                
                                // Water Card (full width)
                                WaterCardRedesigned(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .onPositionedRect { waterRect = it },
                                    isVisible = pagerState.currentPage == 1,
                                    consumed = state.waterConsumed,
                                    servingSize = waterServingSize,
                                    onAdd = { onAddWater(waterServingSize) },
                                    onRemove = { onRemoveWater(waterServingSize) },
                                    onSettingsClick = { showWaterSettings = true }
                                )
                            }
                            
                            // Water Settings Dialog
                            WaterSettingsDialog(
                                showDialog = showWaterSettings,
                                currentServingSize = waterServingSize,
                                onDismiss = { showWaterSettings = false },
                                onServingSizeChange = { waterServingSize = it },
                                isPremium = true,
                                waterReminderEnabled = state.waterReminderEnabled,
                                waterReminderInterval = state.waterReminderIntervalHours,
                                waterReminderStartHour = state.waterReminderStartHour,
                                waterReminderEndHour = state.waterReminderEndHour,
                                waterReminderDailyGoal = state.waterReminderDailyGoalMl,
                                onWaterReminderEnabledChange = onWaterReminderEnabledChange,
                                onWaterReminderIntervalChange = onWaterReminderIntervalChange,
                                onWaterReminderDailyGoalChange = onWaterReminderDailyGoalChange,
                                onUpgradeClick = onUpgradeClick
                            )
                        }
                        
                        // Page 3: Steps + Calories Burned + Weekly Activity Summary
                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(265.dp) // Increased from 320.dp
                                    .onPositionedRect { healthConnectRect = it },
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Steps + Calories Burned Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Steps Card (smaller - 45% width)
                                    StepsTodayCardRedesigned(
                                        steps = state.steps.toInt(),
                                        isVisible = pagerState.currentPage == 2,
                                        goal = state.stepsGoal,
                                        isConnected = state.isHealthConnected,
                                        onConnectClick = onConnectHealth,
                                        modifier = Modifier.weight(0.9f)
                                    )
                                    
                                    // Calories Burned Card (larger - 55% width)
                                    CaloriesBurnedCardRedesigned(
                                        calories = state.caloriesBurned,
                                        isVisible = pagerState.currentPage == 2,
                                        stepsCalories = state.steps.toInt() / 20, // Approx calories from steps
                                        exerciseCalories = state.manualExerciseCalories, // From logged exercises
                                        modifier = Modifier.weight(1.1f)
                                    )
                                }
                                
                                // Weekly Activity Summary Card (full width)
                                WeeklyActivitySummaryCard(
                                    weeklySteps = state.weeklySteps,
                                    weeklyCaloriesBurned = state.weeklyCaloriesBurned,
                                    caloriesRecord = state.caloriesBurnedRecord,
                                    exerciseCalories = state.manualExerciseCalories,
                                    isVisible = pagerState.currentPage == 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .onPositionedRect { weeklyActivityRect = it }
                                )
                            }
                        }
                        
                        // Page 4: Today's Exercises
                        3 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(265.dp), // Increased from 320.dp
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (state.exercises.isNotEmpty()) {
                                    ExerciseSummaryCard(
                                        exercises = state.exercises,
                                        totalCalories = state.manualExerciseCalories
                                    )
                                } else {
                                    // Empty state when no exercises logged
                                    CalAICard(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.FitnessCenter,
                                                contentDescription = null,
                                                modifier = Modifier.size(40.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "No exercises logged today",
                                                fontFamily = InterFontFamily,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Log your workouts to track calories burned",
                                                fontFamily = InterFontFamily,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }
                
                // Page Indicators (4 dots) - Theme-aware colors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .background(
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
        
        // AI Coach Card (standalone, above Weekly Review)
        item {
            Box(modifier = Modifier
                .fillMaxWidth()
                .onPositionedRect { aiCoachRect = it }) {
                AICoachCard(
                    coachTip = state.coachTip
                )
            }
        }

        item {
            val today = Calendar.getInstance()
            val isToday = state.selectedDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                         state.selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            
            Text(
                text = if (isToday) stringResource(R.string.recently_uploaded) 
                       else "Meals for ${SimpleDateFormat("MMM d", Locale.getDefault()).format(state.selectedDate.time)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                modifier = Modifier.onPositionedRect { recentMealsRect = it }
            )
        }

        items(state.meals) { meal ->
            RecentMealCard(
                meal = meal,
                onClick = { onMealClick(meal) }
            )
        }

        item {
            // Empty State if no meals
            if (state.meals.isEmpty()) {
                CalAICard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(RecentMealIcon, null, modifier = Modifier.size(60.dp), tint = Color(0xFFE8F5E9))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.empty_meals_message), color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        } // Close LazyColumn
    } // Close Box
}

@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, widthDp = 375, heightDp = 812)
@Composable
fun DashboardScreenPreview() {
    val windowSizeClass = androidx.compose.material3.windowsizeclass.WindowSizeClass.calculateFromSize(
        androidx.compose.ui.unit.DpSize(375.dp, 812.dp)
    )
    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
        MaterialTheme {
            DashboardContent(
                state = DashboardState(),
                onDateSelected = {},
                onAddWater = {},
                onRemoveWater = {}
            )
        }
    }
}

@Composable
fun MicroStatsRow(
    fiber: Int, 
    sugar: Int, 
    sodium: Int,
    fiberConsumed: Int = 0,
    sugarConsumed: Int = 0,
    sodiumConsumed: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MicroCard(
            label = stringResource(R.string.fiber_left), 
            value = "${fiber}g", 
            icon = Icons.Filled.Grass, 
            iconTint = Color(0xFF66BB6A), 
            progress = if (fiber + fiberConsumed > 0) fiberConsumed.toFloat() / (fiber + fiberConsumed) else 0f,
            modifier = Modifier.weight(1f)
        )
        MicroCard(
            label = stringResource(R.string.sugar_left), 
            value = "${sugar}g", 
            icon = Icons.Filled.Cake, 
            iconTint = Color(0xFFEC407A), 
            progress = if (sugar + sugarConsumed > 0) sugarConsumed.toFloat() / (sugar + sugarConsumed) else 0f,
            modifier = Modifier.weight(1f)
        )
        MicroCard(
            label = stringResource(R.string.sodium_left), 
            value = "${sodium}mg", 
            icon = Icons.Filled.WaterDrop, 
            iconTint = Color(0xFFFFA726), 
            progress = if (sodium + sodiumConsumed > 0) sodiumConsumed.toFloat() / (sodium + sodiumConsumed) else 0f,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MicroCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    iconTint: Color, 
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    // Use a more visible track color
    val trackColor = Color(0xFFE0E0E0) // Light gray track that's visible on white cards
    
    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "microProgress"
    )
    
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 180.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value, 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label, 
                fontSize = 11.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular progress ring with icon - larger size
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.CenterHorizontally)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "$label progress: ${(progress * 100).toInt()}%"
                    }
            ) {
                // Draw ring using Canvas for proper visibility
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track (background circle)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    // Progress arc
                    drawArc(
                        color = iconTint,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                // Icon in center - larger
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(28.dp), 
                    tint = iconTint
                )
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: Int) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.health_score), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$score/10", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { score / 10f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape),
                color = Color(0xFFE8F5E9),
                trackColor = Color(0xFFF3F3F3)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(R.string.health_score_desc),
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}


val RecentMealIcon: ImageVector
    @Composable
    get() = Icons.Filled.Restaurant // Fallback


@Composable
fun DateSelector(
    modifier: Modifier = Modifier,
    selectedDate: Calendar, 
    onDateSelected: (Calendar) -> Unit,
    allMealDates: List<Long> = emptyList(),
    streakDays: Int = 0
) {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    
    // Calculate the start of the current week (Monday)
    fun getWeekStart(date: Calendar): Calendar {
        return (date.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }
    
    // Generate weeks: 4 weeks before, current week, 4 weeks after (9 total weeks)
    val currentWeekStart = getWeekStart(today)
    val weeks = (-4..4).map { weekOffset ->
        val weekStart = (currentWeekStart.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
        // Generate 7 days for this week (Monday to Sunday)
        (0..6).map { dayOffset ->
            (weekStart.clone() as Calendar).apply { add(Calendar.DATE, dayOffset) }
        }
    }
    
    // Find current week index (should be 4, the middle)
    val currentWeekIndex = 4
    val pagerState = rememberPagerState(
        initialPage = currentWeekIndex,
        pageCount = { weeks.size }
    )
    
    // Helper to check if a date has meals logged
    val calendarForCalc = Calendar.getInstance()
    fun hasMealsOnDate(date: Calendar): Boolean {
        if (allMealDates.isEmpty()) return false
        
        return allMealDates.any { timestamp ->
            calendarForCalc.timeInMillis = timestamp
            calendarForCalc.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
            calendarForCalc.get(Calendar.YEAR) == date.get(Calendar.YEAR)
        }
    }
    
    // Helper to check if date is in the future
    fun isFutureDate(date: Calendar): Boolean {
        val todayStart = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateStart = (date.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return dateStart.after(todayStart)
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Month and Year Header with Logo and Streak
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            // Date (Left)
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = dateFormat.format(selectedDate.time),
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "month_animation"
                ) { dateText ->
                    Text(
                        text = dateText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            // Logo (Center)
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val iconRes = if (isDarkTheme) {
                com.example.calview.core.ui.R.drawable.app_logo_white
            } else {
                com.example.calview.core.ui.R.drawable.app_logo_black
            }

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "CalViewAI Icon",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
            
            // Streak badge (Right)
            if (streakDays > 0) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.CenterEnd).semantics(mergeDescendants = true) {
                        contentDescription = "$streakDays day streak"
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$streakDays",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Day name headers (Mon - Sun) - fixed at top, matching reference image
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayName ->
                Text(
                    text = dayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.width(44.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Week Pager - swipe left/right to change weeks
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeks[weekIndex].forEach { day ->
                    val isToday = day.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && 
                                  day.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    val isSelected = day.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR) && 
                                     day.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                    val hasMeals = hasMealsOnDate(day)
                    val isFuture = isFutureDate(day)
                    
                    DateItemCompact(
                        day = day,
                        isToday = isToday,
                        isSelected = isSelected,
                        hasMeals = hasMeals,
                        isFuture = isFuture,
                        onClick = { onDateSelected(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateItemCompact(
    day: Calendar,
    isToday: Boolean,
    isSelected: Boolean,
    hasMeals: Boolean,
    isFuture: Boolean = false,
    onClick: () -> Unit
) {
    val haptics = rememberHapticsManager()
    // Colors matching reference image exactly
    val coralColor = Color(0xFFE58B8B) // Coral/salmon for meals logged (from reference)
    val grayDashedColor = Color(0xFFC4C4C4) // Light gray for dashed borders
    val todayBorderColor = Color(0xFF9CA3AF) // Gray solid for today
    val futureBorderColor = Color(0xFFD1D5DB) // Very light gray for future dates
    val selectedBgColor = Color.White // White background for selected
    val darkTextColor = Color(0xFF4B5563) // Dark gray text
    val lightTextColor = Color(0xFF9CA3AF) // Light gray text for future dates
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(
                when {
                    isSelected -> {
                        // Selected: white rounded rectangle background with shadow + gray circle border
                        Modifier
                            .shadow(4.dp, RoundedCornerShape(14.dp))
                            .background(selectedBgColor, RoundedCornerShape(14.dp))
                            .drawBehind {
                                // Draw gray circle border inside the white background
                                drawCircle(
                                    color = todayBorderColor,
                                    radius = size.minDimension / 2 - 4.dp.toPx(),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                    }

                    isFuture -> {
                        // Future dates: very light gray solid circle border
                        Modifier.drawBehind {
                            drawCircle(
                                color = futureBorderColor,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }

                    hasMeals -> {
                        // Meals logged: solid coral/salmon circle border
                        Modifier.drawBehind {
                            drawCircle(
                                color = coralColor,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    isToday && !hasMeals -> {
                        // Today (no meals): solid gray circle border
                        Modifier.drawBehind {
                            drawCircle(
                                color = todayBorderColor,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    else -> {
                        // Past dates with no meals: dashed gray circle border
                        Modifier.drawBehind {
                            drawCircle(
                                color = grayDashedColor,
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 10f),
                                        0f
                                    )
                                )
                            )
                        }
                    }
                }
            )
            .clickable {
                haptics.tick()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.get(Calendar.DAY_OF_MONTH).toString(),
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = when {
                isFuture -> lightTextColor
                isSelected -> darkTextColor
                hasMeals -> coralColor
                else -> darkTextColor
            }
        )
    }
}

// Unified card combining CaloriesCard and MacroStatsRow with shared toggle
@Composable
fun NutritionOverviewCard(
    remainingCalories: Int,
    consumedCalories: Int,
    goalCalories: Int,
    protein: Int,
    carbs: Int,
    fats: Int,
    proteinConsumed: Int = 0,
    carbsConsumed: Int = 0,
    fatsConsumed: Int = 0,
    fiber: Int = 38,
    sugar: Int = 64,
    sodium: Int = 2300,
    fiberConsumed: Int = 0,
    sugarConsumed: Int = 0,
    sodiumConsumed: Int = 0,
    rolloverCaloriesEnabled: Boolean = false,
    rolloverCaloriesAmount: Int = 0,
    burnedCalories: Int = 0,
    addCaloriesBackEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Shared toggle state for all cards
    var showEaten by remember { mutableStateOf(true) }
    val haptics = rememberHapticsManager()
    
    // Animate calorie numbers
    val animatedConsumed by animateIntAsState(
        targetValue = consumedCalories,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "consumedCalories"
    )
    val animatedRemaining by animateIntAsState(
        targetValue = remainingCalories,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "remainingCalories"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Calories Card Section
        CalAICard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { 
                haptics.click()
                showEaten = !showEaten 
            }
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = showEaten,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                        },
                        label = "calories_animation"
                    ) { isEaten ->
                        // Premium typography for hero calorie numbers
                        val typography = CalViewTheme.typography
                        
                        Column {
                            if (isEaten) {
                                    // Hero calorie number with Space Grotesk - tight tracking
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 44.sp, // Increased from 42.sp
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.02).sp
                                        )) {
                                            append(animatedConsumed.toString())
                                        }
                                        withStyle(SpanStyle(
                                            fontFamily = InterFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )) {
                                            append(" /$goalCalories")
                                        }
                                    }
                                )
                                // Label with Inter
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(
                                            fontFamily = InterFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )) {
                                            append("Calories ")
                                        }
                                        withStyle(SpanStyle(
                                            fontFamily = InterFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )) {
                                            append("eaten")
                                        }
                                    },
                                    style = typography.secondaryLabel.copy(fontSize = 12.sp) // Increased size
                                )
                            } else {
                                // Hero "remaining" number
                                Text(
                                    text = animatedRemaining.toString(),
                                    style = typography.heroNumber.copy(
                                        fontSize = 18.sp, // Increased from 16.sp
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = (-0.02).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Label with Inter
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(
                                                    fontFamily = InterFontFamily,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp // Increased from 10.sp
                                                )) {
                                                    append("Calories ")
                                                }
                                                withStyle(SpanStyle(
                                                    fontFamily = InterFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 12.sp // Increased from 10.sp
                                                )) {
                                                    append("left")
                                                }
                                            },
                                        )

                                        // Indicators (Rollover + Active) in same row
                                        if (rolloverCaloriesEnabled && rolloverCaloriesAmount > 0) {
                                            Surface(
                                                color = Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Redo,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(7.dp), // Reduced from 8.dp
                                                        tint = Color(0xFF2E7D32)
                                                    )
                                                    Text(
                                                        text = "+$rolloverCaloriesAmount",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), // Reduced from 9.sp
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        
                                        if (addCaloriesBackEnabled && burnedCalories > 0) {
                                            Surface(
                                                color = Color(0xFFE8F5E9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocalFireDepartment, 
                                                        contentDescription = null,
                                                        modifier = Modifier.size(7.dp), // Reduced from 8.dp
                                                        tint = Color(0xFF2E7D32)
                                                    )
                                                    Text(
                                                        text = "+$burnedCalories",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), // Reduced from 9.sp
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
                
                // Calorie ring - Reduced size
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                    CalorieRing(
                        consumed = consumedCalories.toFloat(), 
                        goal = goalCalories.toFloat(),
                    )
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        // Macro Stats Row Section (Protein, Carbs, Fats)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroCardUnified(
                label = "Protein",
                goalValue = protein,
                consumedValue = proteinConsumed,
                showEaten = showEaten,
                color = Color(0xFFE57373),
                trackColor = Color(0xFFE8E8E8),
                icon = Icons.Filled.Favorite,
                onClick = { showEaten = !showEaten },
                modifier = Modifier.weight(1f)
            )
            MacroCardUnified(
                label = "Carbs",
                goalValue = carbs,
                consumedValue = carbsConsumed,
                showEaten = showEaten,
                color = Color(0xFFE5A87B),
                trackColor = Color(0xFFE8E8E8),
                icon = Icons.Filled.LocalFlorist,
                onClick = { showEaten = !showEaten },
                modifier = Modifier.weight(1f)
            )
            MacroCardUnified(
                label = "Fats",
                goalValue = fats,
                consumedValue = fatsConsumed,
                showEaten = showEaten,
                color = Color(0xFF64B5F6),
                trackColor = Color(0xFFE8E8E8),
                icon = Icons.Filled.WaterDrop,
                onClick = { showEaten = !showEaten },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Micronutrients Row Section (Fiber, Sugar, Sodium) - Premium design
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MicroCardUnified(
                label = "Fiber",
                goalValue = fiber,
                consumedValue = fiberConsumed,
                showEaten = showEaten,
                unit = "g",
                color = Color(0xFF9575CD), // Purple
                trackColor = Color(0xFFF0ECF5),
                icon = Icons.Filled.Spa,
                onClick = { showEaten = !showEaten },
                modifier = Modifier.weight(1f)
            )
            MicroCardUnified(
                label = "Sugar",
                goalValue = sugar,
                consumedValue = sugarConsumed,
                showEaten = showEaten,
                unit = "g",
                color = Color(0xFFF06292), // Pink
                trackColor = Color(0xFFFCE4EC),
                icon = Icons.Filled.Cookie,
                onClick = { showEaten = !showEaten },
                modifier = Modifier.weight(1f)
            )
            MicroCardUnified(
                label = "Sodium",
                goalValue = sodium,
                consumedValue = sodiumConsumed,
                showEaten = showEaten,
                unit = "mg",
                color = Color(0xFFFFB74D), // Orange
                trackColor = Color(0xFFFFF3E0),
                icon = Icons.Filled.Grain,
                onClick = { showEaten = !showEaten },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Micronutrient card with matching design to MacroCardUnified
@Composable
fun MicroCardUnified(
    label: String,
    goalValue: Int,
    consumedValue: Int,
    showEaten: Boolean,
    unit: String,
    color: Color,
    trackColor: Color,
    icon: ImageVector,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val remaining = maxOf(0, goalValue - consumedValue)
    
    // Helper to format large numbers (e.g. 2300 -> 2.3K)
    fun formatValue(value: Int): String {
        return if (value >= 1000) {
            val k = value / 1000f
            if (k % 1f == 0f) "${k.toInt()}K" else String.format(java.util.Locale.US, "%.1fK", k)
        } else {
            value.toString()
        }
    }
    
    // Calculate progress
    val progress = if (goalValue > 0) (consumedValue.toFloat() / goalValue).coerceIn(0f, 1f) else 0f
    
    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "microProgress"
    )
    
    CalAICard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AnimatedContent(
                targetState = showEaten,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                },
                label = "micro_animation"
            ) { isEaten ->
                Column {
                    if (isEaten) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontSize = 13.sp, // Reduced from 16.sp
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(formatValue(consumedValue))
                                }
                                withStyle(SpanStyle(
                                    fontSize = 8.sp, // Reduced from 10.sp for goal
                                    color = Color.Gray
                                )) {
                                    append(" /${formatValue(goalValue)}$unit")
                                }
                            }
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color.Gray)) {
                                    append("$label ")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("eaten")
                                }
                            },
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "${formatValue(remaining)}$unit",
                            fontSize = 13.sp, // Reduced from 16.sp
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color.Gray)) {
                                    append("$label ")
                                }
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("left")
                                }
                            },
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Ring with icon - compact size
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp), 
                    tint = color
                )
            }
        }
    }
}

// Premium Health Score Card with gradient, animation, and AI coach tip
@Composable
fun HealthScoreCardPremium(
    score: Int,
    recommendation: String = "Track your meals to get personalized health insights.",
    coachTip: CoachMessageGenerator.CoachTip? = null,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score / 10f,
        animationSpec = tween(800),
        label = "health_score"
    )
    
    // Dynamic color based on score
    val scoreColor = when {
        score >= 7 -> Color(0xFF4CAF50) // Green
        score >= 4 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    CalAICard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health score",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$score/10",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = scoreColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Premium progress bar with gradient effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedScore)
                        .fillMaxHeight()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = when {
                                    score >= 7 -> listOf(Color(0xFF81C784), Color(0xFF4CAF50))
                                    score >= 4 -> listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
                                    else -> listOf(Color(0xFFEF5350), Color(0xFFF44336))
                                }
                            )
                        )
                )
            }
            
            // AI Coach Check-in section (if tip available)
            coachTip?.let { tip ->
                Spacer(modifier = Modifier.height(16.dp))
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emoji Container
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tip.emoji,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI Coach",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tip.category.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = tip.message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            } ?: run {
                // Show recommendation only if no coach tip
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = recommendation,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ============ HEALTH SCORE CARD (without AI Coach) ============
@Composable
fun HealthScoreCard(
    score: Int,
    recommendation: String = "Track your meals to get personalized health insights.",
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = if (isVisible) score / 10f else 0f,
        animationSpec = tween(800),
        label = "health_score"
    )
    
    val animatedScoreInt by animateIntAsState(
        targetValue = if (isVisible) score else 0,
        animationSpec = tween(800),
        label = "health_score_int"
    )
    
    // Dynamic color based on score
    val scoreColor = when {
        score >= 7 -> Color(0xFF4CAF50) // Green
        score >= 4 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
    
    CalAICard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health score",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$animatedScoreInt/10",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = scoreColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar with gradient effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedScore)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = when {
                                    score >= 7 -> listOf(Color(0xFF81C784), Color(0xFF4CAF50))
                                    score >= 4 -> listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
                                    else -> listOf(Color(0xFFEF5350), Color(0xFFF44336))
                                }
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation,
                fontSize = 10.sp,
                color = Color.Gray,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// ============ AI COACH CARD (standalone) ============
@Composable
fun AICoachCard(
    coachTip: CoachMessageGenerator.CoachTip?,
    modifier: Modifier = Modifier
) {
    coachTip?.let { tip ->
        CalAICard(modifier = modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emoji Container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tip.emoji,
                            fontSize = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI Coach",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tip.category.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = tip.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            maxLines = 10,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// Steps Today Card with Health Connect integration
// Unified Activity Card - Steps and Calories in one card
@Composable
fun UnifiedActivityCard(
    steps: Int,
    stepsGoal: Int,
    calories: Int,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    exerciseCalories: Int = 0  // Calories from logged exercises
) {
    CalAICard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Steps Section
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress ring with icon
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = if (stepsGoal > 0) (steps.toFloat() / stepsGoal).coerceIn(0f, 1f) else 0f
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Track
                        drawArc(
                            color = Color(0xFFE8E8E8),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        // Progress
                        if (progress > 0) {
                            drawArc(
                                color = Color(0xFF424242),
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Steps",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF424242)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Premium typography for data values
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp
                        )) {
                            append(steps.toString())
                        }
                        withStyle(SpanStyle(
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )) {
                            append(" /$stepsGoal")
                        }
                    }
                )
                Text(
                    text = "Steps Today",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(100.dp)
                    .background(Color(0xFFE8E8E8))
            )
            
            // Calories Section
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fire icon with ring
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFFE8E8E8),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                        // Progress based on calories (arbitrary goal of 500)
                        val calProgress = (calories / 3000f).coerceIn(0f, 1f)
                        if (calProgress > 0) {
                            drawArc(
                                color = Color(0xFFFF9800),
                                startAngle = -90f,
                                sweepAngle = 360f * calProgress,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Calories",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFFFF9800)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Premium typography for calories burned
                Text(
                    text = calories.toString(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.02).sp
                )
                Text(
                    text = stringResource(R.string.burned_label),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show exercise breakdown if there are logged exercises
                if (exerciseCalories > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "+$exerciseCalories",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StepsTodayCard(
    steps: Int,
    goal: Int,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: value/goal on top, "Steps Today" below
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )) {
                        append(steps.toString())
                    }
                    withStyle(SpanStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )) {
                        append(" /$goal")
                    }
                }
            )
            Text(
                text = "Steps Today",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isConnected) {
                // Connected state: Show progress ring with footprints
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring (track)
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(100.dp)
                    ) {
                        drawCircle(
                            color = Color(0xFFE8E8E8),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                    // Inner ring
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(60.dp)
                    ) {
                        drawCircle(
                            color = Color(0xFFF0F0F0),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                    // Footprints icon
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Steps",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF424242)
                    )
                }
            } else {
                // Not connected: Show rings + connection prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring (track)
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(100.dp)
                    ) {
                        drawCircle(
                            color = Color(0xFFE8E8E8),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                    // Inner ring
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.size(60.dp)
                    ) {
                        drawCircle(
                            color = Color(0xFFF0F0F0),
                            style = Stroke(width = 6.dp.toPx())
                        )
                    }
                    // Footprints icon
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Steps",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF424242)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Connection prompt card - clickable
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            android.util.Log.d("StepsTodayCard", "Health Connect button CLICKED!")
                            onConnectClick()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Google Health icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FavoriteBorder,
                                contentDescription = null,
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Text(
                            text = "Connect Google Health to track your steps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF424242),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// Calories Burned Card
@Composable
fun CaloriesBurnedCard(
    calories: Int,
    stepsCalories: Int,
    modifier: Modifier = Modifier
) {
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: fire icon + value on top, "Calories burned" below
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFF424242),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = calories.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Calories burned",
                fontSize = 10.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Steps calories row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sneaker icon in dark circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1C1C1E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "Steps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+$stepsCalories",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// Premium Water Card
@Composable
fun WaterCardPremium(
    consumed: Int,
    servingSize: Int = 250,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cups = consumed / servingSize
    
    CalAICard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Water glass icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFF0F4FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF42A5F5),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Water info
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)) {
                Text(
                    text = "Water",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ml_suffix, consumed),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "($cups cups)",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onSettingsClick() }
                    )
                }
            }
            
            // +/- buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Minus button - outline (theme-aware)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Plus button - filled (theme-aware)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// REDESIGNED DASHBOARD CARD COMPOSABLES FOR HORIZONTAL PAGER
// ============================================================================

// Redesigned Calories Card - matches design image Page 1
@Composable
fun CaloriesCardRedesigned(
    remainingCalories: Int,
    consumedCalories: Int,
    goalCalories: Int,
    rolloverCaloriesEnabled: Boolean = false,
    rolloverCaloriesAmount: Int = 0,
    burnedCalories: Int = 0,
    addCaloriesBackEnabled: Boolean = false,
    showEaten: Boolean = true,
    onToggle: () -> Unit = {},
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Animate number changes
    val animatedCalories by animateIntAsState(
        targetValue = if (isVisible) (if (showEaten) consumedCalories else remainingCalories) else 0,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "calories_animation"
    )
    
    CalAICard(modifier = modifier.fillMaxWidth(), onClick = onToggle) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = showEaten,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "calories_content"
                ) { isEaten ->
                    Column {
                        if (isEaten) {
                            // Show consumed/goal format when showing "eaten"
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.02).sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )) {
                                        append(animatedCalories.toString())
                                    }
                                    withStyle(SpanStyle(
                                        fontFamily = InterFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )) {
                                        append("/$goalCalories")
                                    }
                                }
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("Calories ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append("eaten")
                                    }
                                },
                                fontFamily = InterFontFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // Show remaining/goal format when showing "left"
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.02).sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )) {
                                        append(animatedCalories.toString())
                                    }
                                    withStyle(SpanStyle(
                                        fontFamily = InterFontFamily,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )) {
                                        append("/$goalCalories")
                                    }
                                }
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("Calories ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append("left")
                                    }
                                },
                                fontFamily = InterFontFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
               
            }
            // Show extra info badges
            Column {
            if (rolloverCaloriesEnabled && rolloverCaloriesAmount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = " +$rolloverCaloriesAmount ",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            if (addCaloriesBackEnabled && burnedCalories > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = " +$burnedCalories ",
                        fontSize = 10.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            }
                // Calorie ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                CalorieRing(
                    consumed = if (isVisible) consumedCalories.toFloat() else 0f,
                    goal = goalCalories.toFloat(),
                    strokeWidth = 4.dp
                )
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

// Redesigned Macro Card - compact design for horizontal row
@Composable
fun MacroCardRedesigned(
    label: String,
    consumed: Int,
    goal: Int,
    unit: String,
    icon: ImageVector,
    iconTint: Color,
    showEaten: Boolean = true,
    onToggle: () -> Unit = {},
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val remaining = maxOf(0, goal - consumed)
    val progress = if (goal > 0) (consumed.toFloat() / goal).coerceIn(0f, 1f) else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) progress else 0f,
        animationSpec = tween(500),
        label = "macroProgress"
    )
    
    val displayValue = if (showEaten) consumed else remaining
    val animatedDisplayValue by animateIntAsState(
        targetValue = if (isVisible) displayValue else 0,
        animationSpec = tween(500),
        label = "macroValue"
    )
    val displayLabel = if (showEaten) "$label eaten" else "$label left"
    
    CalAICard(modifier = modifier, onClick = onToggle) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Value with unit and goal
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )) {
                        append(animatedDisplayValue.toString())
                    }
                    withStyle(SpanStyle(
                        fontFamily = InterFontFamily,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )) {
                        append("/$goal$unit")
                    }
                }
            )
            
            // Label (eaten/left)
            Text(
                text = displayLabel,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress ring with icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawArc(
                        color = iconTint.copy(alpha = 0.2f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    // Progress
                    drawArc(
                        color = iconTint,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = iconTint
                )
            }
        }
    }
}

// Redesigned Steps Card with integrated Health Connect CTA
@Composable
fun StepsTodayCardRedesigned(
    steps: Int,
    goal: Int,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) progress else 0f,
        animationSpec = tween(800),
        label = "stepsProgress"
    )
    
    val animatedSteps by animateIntAsState(
        targetValue = if (isVisible) steps else 0,
        animationSpec = tween(800),
        label = "stepsValue"
    )
    
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header: Steps value / goal
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFontFamily
                    )) {
                        append(animatedSteps.toString())
                    }
                    withStyle(SpanStyle(
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = InterFontFamily
                    )) {
                        append(" /$goal")
                    }
                }
            )
            
            Text(
                text = stringResource(R.string.steps_today),
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(1.dp))
            
            if (isConnected) {
                // Linear Progress Bar with Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    // Custom Linear Progress Indicator
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            } else {
                // Health Connect CTA button inside card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            android.util.Log.d("StepsTodayCard", "Health Connect CTA clicked!")
                            onConnectClick()
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Google Fit logo
                        Image(
                            painter = painterResource(id = R.drawable.google_fit_logo),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Text(
                            text = stringResource(R.string.connect_health_to_track_steps),
                            fontFamily = InterFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 10.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

// Redesigned Calories Burned Card
@Composable
fun CaloriesBurnedCardRedesigned(
    calories: Int,
    stepsCalories: Int,
    exerciseCalories: Int = 0,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedBurned by animateIntAsState(
        targetValue = if (isVisible) calories else 0,
        animationSpec = tween(800),
        label = "burnedValue"
    )
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header: fire icon + value on top
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = animatedBurned.toString(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = stringResource(R.string.calories_burned),
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Steps and Exercise in one Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Steps calories
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Sneaker/Steps icon in dark circle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF1C1C1E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    Text(
                        text = "+$stepsCalories kcal",
                        fontFamily = InterFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
                
                // Exercise calories (only show if exerciseCalories > 0)
                if (exerciseCalories > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Dumbbell icon in orange circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFFF9800), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FitnessCenter,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = "+$exerciseCalories kcal",
                            fontFamily = InterFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}


// Redesigned Water Card with glass icon
@Composable
fun WaterCardRedesigned(
    consumed: Int,
    servingSize: Int = 250,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onSettingsClick: () -> Unit = {},
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedConsumed by animateIntAsState(
        targetValue = if (isVisible) consumed else 0,
        animationSpec = tween(800),
        label = "waterValue"
    )
    val cups = consumed / servingSize
    
    CalAICard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Water glass icon (blue)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Custom glass-like representation using LocalDrink icon
                Icon(
                    imageVector = Icons.Filled.LocalDrink,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.water_label),
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ml_suffix, animatedConsumed),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "($cups cups)",
                        fontFamily = InterFontFamily,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSettingsClick() }
                    )
                }
            }
            
            // +/- buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Minus button - outline
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Plus button - filled
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Redesigned Weekly Activity Summary Card for Page 3
@Composable
fun WeeklyActivitySummaryCard(
    weeklySteps: Long,
    weeklyCaloriesBurned: Int,
    caloriesRecord: Int,
    exerciseCalories: Int = 0,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedSteps by animateIntAsState(
        targetValue = if (isVisible) weeklySteps.toInt() else 0,
        animationSpec = tween(800),
        label = "weeklySteps"
    )
    val animatedCalories by animateIntAsState(
        targetValue = if (isVisible) weeklyCaloriesBurned else 0,
        animationSpec = tween(800),
        label = "weeklyCalories"
    )
    val animatedRecord by animateIntAsState(
        targetValue = if (isVisible) caloriesRecord else 0,
        animationSpec = tween(800),
        label = "caloriesRecord"
    )
    val animatedExercise by animateIntAsState(
        targetValue = if (isVisible) exerciseCalories else 0,
        animationSpec = tween(800),
        label = "exerciseCalories"
    )
    
    CalAICard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Title
            Text(
                text = "Weekly Activity",
                fontFamily = InterFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 7d Steps
                WeeklyStatItemText(
                    value = animatedSteps,
                    label = stringResource(R.string.weekly_steps_label),
                    color = Color(0xFF9575CD)
                )
                
                // 7d Burn
                WeeklyStatItemText(
                    value = animatedCalories,
                    label = stringResource(R.string.weekly_burn_label),
                    color = Color(0xFFFF9800),
                    suffix = " kcal"
                )
                
                // Record
                WeeklyStatItemText(
                    value = animatedRecord,
                    label = stringResource(R.string.record_label),
                    color = Color(0xFFE91E63),
                    icon = Icons.Filled.EmojiEvents,
                    suffix = " kcal"
                )
                
                // Exercise (Workouts)
                WeeklyStatItemText(
                    value = animatedExercise,
                    label = "Workout",
                    color = Color(0xFF4CAF50),
                    suffix = " kcal"
                )
            }
        }
    }
}

@Composable
private fun WeeklyStatItemText(
    value: Int,
    label: String,
    color: Color,
    icon: ImageVector? = null,
    suffix: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = (if (value >= 10000 && suffix.isEmpty()) "${value / 1000}k" else value.toString()) + suffix,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

// ============================================================================
// END REDESIGNED DASHBOARD CARD COMPOSABLES
// ============================================================================

@Composable
fun MacroCardUnified(
    label: String,
    goalValue: Int,
    consumedValue: Int,
    showEaten: Boolean,
    color: Color,
    trackColor: Color,
    icon: ImageVector,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val remaining = maxOf(0, goalValue - consumedValue)
    
    // Calculate progress
    val progress = if (goalValue > 0) (consumedValue.toFloat() / goalValue).coerceIn(0f, 1f) else 0f
    
    // Animate progress changes
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "macroProgress"
    )
    
    // Premium typography for macro numbers
    val typography = CalViewTheme.typography
    
    CalAICard(modifier = modifier, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            AnimatedContent(
                targetState = showEaten,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn(animationSpec = tween(300)))
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(300)))
                },
                label = "macro_animation"
            ) { isEaten ->
                Column {
                    if (isEaten) {
                        // Macro number with Space Grotesk - premium feel
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.02).sp // Tight tracking
                                )) {
                                    append(consumedValue.toString())
                                }
                                withStyle(SpanStyle(
                                    fontFamily = InterFontFamily,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )) {
                                    append(" /${goalValue}g")
                                }
                            }
                        )
                        // Label with Inter - lighter feel
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontFamily = InterFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )) {
                                    append("$label ")
                                }
                                withStyle(SpanStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )) {
                                    append("eaten")
                                }
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Macro number with Space Grotesk
                        Text(
                            text = "${remaining}g",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Label with Inter
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontFamily = InterFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )) {
                                    append("$label ")
                                }
                                withStyle(SpanStyle(
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )) {
                                    append("left")
                                }
                            },
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Ring with icon - compact size
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp), 
                    tint = color
                )
            }
        }
    }
}




@Composable
fun RecentMealCard(
    meal: MealEntity,
    onClick: () -> Unit = {}
) {
    val isAnalyzing = meal.analysisStatus == com.example.calview.core.data.local.AnalysisStatus.ANALYZING ||
                      meal.analysisStatus == com.example.calview.core.data.local.AnalysisStatus.PENDING
    
    // Animation for progress
    val animatedProgress by animateFloatAsState(
        targetValue = meal.analysisProgress / 100f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "progressAnimation"
    )
    
    // Pulse animation for analyzing state
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // Shimmer animation offset
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    // Rotating animation for the progress circle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )
    
    CalAICard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { if (!isAnalyzing) onClick() }
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Image with progress overlay for analyzing state
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                ) {
                // Load food image from path using Coil AsyncImage
                val path = meal.imagePath
                if (path != null) {
                    var imageLoadError by remember { mutableStateOf(false) }
                    
                    if (!imageLoadError) {
                        val model = if (path.startsWith("http")) {
                            path
                        } else {
                            File(path)
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Food image for ${meal.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = { error ->
                                android.util.Log.e("MealCardImage", "Failed to load image: $path")
                                android.util.Log.e("MealCardImage", "Error: ${error.result.throwable.message}")
                                imageLoadError = true
                            },
                            onSuccess = {
                                android.util.Log.d("MealCardImage", "Successfully loaded image: $path")
                            }
                        )
                    }
                    
                    // Show fallback icon if image fails to load or while loading
                    if (imageLoadError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Restaurant,
                                contentDescription = "Food placeholder",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Icon(
                        Icons.Filled.Restaurant,
                        contentDescription = "No food image available",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Animated progress overlay for analyzing state
                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer rotating ring
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(60.dp)
                                .graphicsLayer { rotationZ = rotationAngle },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            strokeWidth = 3.dp
                        )
                        
                        // Animated inner progress
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(50.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            strokeWidth = 4.dp
                        )
                        
                        // Percentage text with pulse
                        Text(
                            "${meal.analysisProgress.toInt()}%",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = pulseAlpha),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (isAnalyzing) {
                    // Analyzing state UI with animations
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Animated analyzing icon
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    rotationZ = rotationAngle * 0.5f
                                    alpha = pulseAlpha
                                },
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            getAnalyzingStepText(meal.analysisProgress),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = pulseAlpha)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Animated progress bars with shimmer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnimatedProgressSegment(
                            active = animatedProgress >= 0.25f,
                            shimmerOffset = shimmerOffset,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedProgressSegment(
                            active = animatedProgress >= 0.50f,
                            shimmerOffset = shimmerOffset,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedProgressSegment(
                            active = animatedProgress >= 0.75f,
                            shimmerOffset = shimmerOffset,
                            modifier = Modifier.weight(1f)
                        )
                        AnimatedProgressSegment(
                            active = animatedProgress >= 1.0f,
                            shimmerOffset = shimmerOffset,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "We'll notify you when done!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Completed state UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            meal.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Confidence badge
                            if (meal.confidenceScore > 0) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = when {
                                                meal.confidenceScore >= 80f -> Color(0xFF4CAF50).copy(
                                                    alpha = 0.15f
                                                )

                                                meal.confidenceScore >= 60f -> Color(0xFFFFC107).copy(
                                                    alpha = 0.15f
                                                )

                                                else -> Color(0xFFFF5722).copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${meal.confidenceScore.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = when {
                                            meal.confidenceScore >= 80f -> Color(0xFF4CAF50)
                                            meal.confidenceScore >= 60f -> Color(0xFFFFC107)
                                            else -> Color(0xFFFF5722)
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Text(
                                formatMealTime(meal.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${meal.calories} calories",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MacroInfo(text = "${meal.protein}g", icon = Icons.Filled.Favorite, Color(0xFFFF5252))
                        MacroInfo(text = "${meal.carbs}g", icon = Icons.Filled.Eco, Color(0xFFFFAB40))
                        MacroInfo(text = "${meal.fats}g", icon = Icons.Filled.WaterDrop, Color(0xFF448AFF))
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MacroInfo(text = "${meal.fiber}g", icon = Icons.Filled.Grass, Color(0xFF66BB6A))
                        MacroInfo(text = "${meal.sugar}g", icon = Icons.Filled.Cookie, Color(0xFFFF7043))
                        MacroInfo(text = "${meal.sodium}mg", icon = Icons.Filled.Science, Color(0xFF9575CD))
                    }
                }
            }
        }
    }
}

/**
 * Get contextual text based on analysis progress
 */
private fun getAnalyzingStepText(progress: Float): String {
    return when {
        progress < 20f -> "Analysing..."
        progress < 40f -> "Identifying food items..."
        progress < 60f -> "Breaking down components..."
        progress < 80f -> "Calculating nutrition..."
        progress < 95f -> "Finalizing..."
        else -> "Almost done!"
    }
}

@Composable
fun AnimatedProgressSegment(
    active: Boolean,
    shimmerOffset: Float,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    val backgroundColor = if (active) activeColor else inactiveColor
    
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor)
            .then(
                if (active) {
                    Modifier.drawWithContent {
                        drawContent()
                        // Shimmer effect on active segments
                        val shimmerWidth = size.width * 0.5f
                        val shimmerX = (shimmerOffset * (size.width + shimmerWidth)) - shimmerWidth
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                start = Offset(shimmerX, 0f),
                                end = Offset(shimmerX + shimmerWidth, 0f)
                            )
                        )
                    }
                } else Modifier
            )
    )
}

@Composable
fun ProgressSegment(active: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (active) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
    )
}

fun formatMealTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun MacroInfo(text: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterSettingsDialog(
    showDialog: Boolean,
    currentServingSize: Int,
    onDismiss: () -> Unit,
    onServingSizeChange: (Int) -> Unit,
    // Water Reminder Props
    isPremium: Boolean = true,
    waterReminderEnabled: Boolean = false,
    waterReminderInterval: Int = 2,
    waterReminderStartHour: Int = 8,
    waterReminderEndHour: Int = 22,
    waterReminderDailyGoal: Int = 2500,
    onWaterReminderEnabledChange: (Boolean) -> Unit = {},
    onWaterReminderIntervalChange: (Int) -> Unit = {},
    onWaterReminderDailyGoalChange: (Int) -> Unit = {},
    onUpgradeClick: () -> Unit = {}
) {
    if (showDialog) {
        var selectedSize by remember { mutableStateOf(currentServingSize) }
        var showPicker by remember { mutableStateOf(false) }
        
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() },
                        tint = Color.Gray
                    )
                    Text(
                        text = "Water settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.size(24.dp)) // Balance the row
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Serving Size Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPicker = !showPicker },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Serving Size",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.ml_suffix, selectedSize) + " (${"%.1f".format(selectedSize / 250f)} cups)",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }
                }
                
                // Serving size picker
                AnimatedVisibility(visible = showPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Simple number picker
                        val sizes = listOf(250, 500, 750, 1000)
                        sizes.forEach { size ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (size == selectedSize) Color(0xFFF5F5F5) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        if (size == selectedSize) 1.dp else 0.dp,
                                        if (size == selectedSize) Color(0xFFE0E0E0) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedSize = size
                                        onServingSizeChange(size)
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size.toString(),
                                    fontSize = if (size == selectedSize) 18.sp else 14.sp,
                                    fontWeight = if (size == selectedSize) FontWeight.Bold else FontWeight.Normal,
                                    color = if (size == selectedSize) Color.Black else Color.Gray
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(24.dp))
                
                // Water Reminder Settings Card
                WaterReminderSettingsCard(
                    isPremium = isPremium,
                    enabled = waterReminderEnabled,
                    intervalHours = waterReminderInterval,
                    startHour = waterReminderStartHour,
                    endHour = waterReminderEndHour,
                    dailyGoalMl = waterReminderDailyGoal,
                    servingSize = currentServingSize,
                    onEnabledChange = onWaterReminderEnabledChange,
                    onIntervalChange = onWaterReminderIntervalChange,
                    onStartHourChange = { /* TODO: Implement time picker logic here or in ViewModel */ },
                    onEndHourChange = { /* TODO */ },
                    onDailyGoalChange = onWaterReminderDailyGoalChange,
                    onServingSizeChange = onServingSizeChange,
                    onUpgradeClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Info section
                Text(
                    text = "How much water do you need to stay hydrated?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Everyone's needs are slightly different, but we recommended aiming for at least 2500 ml of water each day",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


// Health Connect Onboarding Screen - Shows before requesting permissions
@Composable
fun HealthConnectOnboardingScreen(
    onGoBack: () -> Unit,
    onGetStarted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp)
    ) {
        // Title
        Text(
            text = "Get started with\nHealth Connect",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subtitle
        Text(
            text = "CalViewAI stores your health and fitness data, giving you a simple way to sync the different apps on your phone",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Illustration - App icons connecting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column - Apps
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Fitness app
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFFE4D6), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = Color(0xFFFF7043),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    // Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        if (it < 2) Color(0xFFFFB74D) else Color(0xFFE8E8E8),
                                        CircleShape
                                    )
                            )
                        }
                    }
                    // Steps app
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFFE0B2), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚶", fontSize = 24.sp)
                    }
                }
                
                // Center - Health Connect icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Health Connect",
                        tint = Color(0xFF00BCD4),
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Right column - Apps
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Heart app
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFFFE0EC), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonitorHeart,
                            contentDescription = null,
                            tint = Color(0xFFEC407A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    // Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        if (it >= 2) Color(0xFF81C784) else Color(0xFFE8E8E8),
                                        CircleShape
                                    )
                            )
                        }
                    }
                    // Sleep app
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFF42A5F5),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Info section 1
        Column {
            Text(
                text = "Share data with your apps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose the data that each app can read or write to Health Connect",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Info section 2
        Column {
            Text(
                text = "Manage your settings and privacy",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Change app permissions and manage your data at any time",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Go back button
            TextButton(onClick = onGoBack) {
                Text(
                    text = "Go back",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Get started button
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Get started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

/**
 * Streak Lost Dialog - Shows when user hasn't logged food in 24 hours
 * Matches the design from mockup with CalView AI header, gray flame, week indicators
 */
@Composable
fun StreakLostDialog(
    currentStreak: Int = 0,
    completedDays: List<Boolean> = listOf(false, false, false, false, false, false, false),
    onDismiss: () -> Unit
) {
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with CalView AI and streak count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // CalView AI icon placeholder - fire emoji
                        Text(
                            text = "🔥",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CalView AI",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    
                    // Streak count badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Whatshot,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$currentStreak",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Large gray flame icon
                Icon(
                    Icons.Filled.Whatshot,
                    contentDescription = "Streak lost",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFD9D9D9) // Light gray
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // "Streak lost" title
                Text(
                    text = "Streak lost",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Week day indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weekDays.forEachIndexed { index, day ->
                        val isToday = index == todayIndex
                        val isCompleted = completedDays.getOrElse(index) { false }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day,
                                fontSize = 14.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) Color(0xFFE57373) else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isCompleted) Color(0xFF424242) else Color(0xFFE5E5E5),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Motivational message
                Text(
                    text = "Don't give up. Log your meals today\nto get back on track!",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Continue button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Exercise Summary Card - Shows logged exercises for today
@Composable
fun ExerciseSummaryCard(
    exercises: List<com.example.calview.core.data.local.ExerciseEntity>,
    totalCalories: Int,
    modifier: Modifier = Modifier
) {
    CalAICard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    "Today's exercise summary. ${exercises.size} ${if (exercises.size == 1) "exercise" else "exercises"} logged, $totalCalories calories burned"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Exercise",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Total calories badge
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalCalories cal",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Exercise list (show max 3)
            exercises.take(3).forEachIndexed { index, exercise ->
                val typeName = exercise.type.name.lowercase().replaceFirstChar { it.uppercase() }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .semantics {
                            contentDescription =
                                "${exercise.name}, ${exercise.durationMinutes} minutes, ${exercise.caloriesBurned} calories, $typeName exercise"
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = getExerciseIcon(exercise.type),
                            contentDescription = null,
                            tint = getExerciseColor(exercise.type),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${exercise.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Text(
                        text = "${exercise.caloriesBurned} cal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
            
            // Show "and X more" if more than 3 exercises
            if (exercises.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+${exercises.size - 3} more exercise${if (exercises.size - 3 > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper function to get exercise type icon
private fun getExerciseIcon(type: com.example.calview.core.data.local.ExerciseType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        com.example.calview.core.data.local.ExerciseType.CARDIO -> Icons.Default.DirectionsRun
        com.example.calview.core.data.local.ExerciseType.STRENGTH -> Icons.Default.FitnessCenter
        com.example.calview.core.data.local.ExerciseType.FLEXIBILITY -> Icons.Default.SelfImprovement
        com.example.calview.core.data.local.ExerciseType.SPORT -> Icons.Default.SportsBaseball
        com.example.calview.core.data.local.ExerciseType.OTHER -> Icons.Default.DirectionsBike
    }
}

private fun getExerciseColor(type: com.example.calview.core.data.local.ExerciseType): Color {
    return when (type) {
        com.example.calview.core.data.local.ExerciseType.CARDIO -> Color(0xFFE53935)
        com.example.calview.core.data.local.ExerciseType.STRENGTH -> Color(0xFF1E88E5)
        com.example.calview.core.data.local.ExerciseType.FLEXIBILITY -> Color(0xFF43A047)
        com.example.calview.core.data.local.ExerciseType.SPORT -> Color(0xFFFF9800)
        com.example.calview.core.data.local.ExerciseType.OTHER -> Color(0xFF9C27B0)
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
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
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
            
            // Today's Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Steps
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            drawArc(color = Color(0xFF9C27B0), startAngle = -90f, sweepAngle = 360f * stepsProgress, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        }
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF9C27B0))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = todaySteps.toString(), fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = stringResource(R.string.steps_label), fontFamily = InterFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Burned
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            drawArc(color = Color(0xFFFF9800), startAngle = -90f, sweepAngle = 360f * burnProgress, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        }
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFF9800))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = caloriesBurned.toString(), fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = stringResource(R.string.burned_label), fontFamily = InterFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                // Exercise
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        val exerciseProgress = (manualExerciseCalories / 500f).coerceIn(0f, 1f)
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                            drawArc(color = Color(0xFF1E88E5), startAngle = -90f, sweepAngle = 360f * exerciseProgress, useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                        }
                        Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF1E88E5))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = manualExerciseCalories.toString(), fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = stringResource(R.string.exercise_label), fontFamily = InterFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
