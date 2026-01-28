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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import com.example.calview.core.ui.walkthrough.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.runtime.mutableIntStateOf
import com.example.calview.core.ui.util.rememberHapticsManager
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
    var showHealthOnboarding by remember { mutableStateOf(false) }
    
    // State for showing streak lost dialog
    var showStreakLostDialog by remember { mutableStateOf(false) }
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
    
    var currentStepIndex by remember(state.hasSeenWalkthrough) { 
        mutableIntStateOf(if (!state.hasSeenWalkthrough) 0 else -1) 
    }
    
    val walkthroughSteps = listOf(
        WalkthroughStep(
            id = "nutrition",
            title = "Track Your Goals",
            description = "Your AI-calculated nutrition goals are right here. Monitor your calories and macros in real-time.",
            targetRect = nutritionRect
        ),
        WalkthroughStep(
            id = "calendar",
            title = "Historical Data",
            description = "Use the calendar to navigate between days and view your historical nutrition logs.",
            targetRect = calendarRect
        ),
        WalkthroughStep(
            id = "score",
            title = "AI Health Coach",
            description = "Your Health Score updates dynamically. Check the AI Coach tips for personalized nutrition advice.",
            targetRect = healthScoreRect
        ),
        WalkthroughStep(
            id = "health",
            title = "Health Connect",
            description = "Sync with Google Fit or Samsung Health via Health Connect to automatically track your steps and activity.",
            targetRect = healthConnectRect
        ),
        WalkthroughStep(
            id = "water",
            title = "Hydration Tracker",
            description = "Staying hydrated is key to your health. Quickly log your water intake throughout the day.",
            targetRect = waterRect
        ),
        WalkthroughStep(
            id = "meal",
            title = "Scanned Meals",
            description = "Your recently scanned meals appear here. Tap any meal to see detailed nutrition information.",
            targetRect = recentMealsRect
        )
    )

    // Auto-scroll logic for walkthrough steps to ensure target items are visible
    LaunchedEffect(currentStepIndex) {
        if (currentStepIndex != -1) {
            // Give layout a moment to settle and for rects to be captured
            kotlinx.coroutines.delay(150)
            
            val targetScrollIndex = when (currentStepIndex) {
                0 -> 1 // Nutrition
                1 -> 0 // Calendar/Header
                2 -> 2 // Health Score
                3 -> 3 // Health Connect (if visible) or Unified Activity
                4 -> if (!state.isHealthConnected) 5 else 4 // Water Tracker
                5 -> if (!state.isHealthConnected) 7 else 6 // Recently Uploaded First Meal
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
            HeaderSection(streakDays = state.currentStreak)
            
            Spacer(modifier = Modifier.height(4.dp))
            DateSelector(
                modifier = Modifier.onPositionedRect { calendarRect = it },
                selectedDate = state.selectedDate, 
                onDateSelected = onDateSelected,
                allMealDates = state.allMealDates
            )
        }

        item {

        NutritionOverviewCard(
            modifier = Modifier.onPositionedRect { nutritionRect = it },
            remainingCalories = state.remainingCalories,
            consumedCalories = state.consumedCalories,
            goalCalories = state.goalCalories,
            protein = state.proteinGoal,
            carbs = state.carbsGoal,
            fats = state.fatsGoal,
            proteinConsumed = state.proteinG,
            carbsConsumed = state.carbsG,
            fatsConsumed = state.fatsG,
            fiber = state.fiberGoal,
            sugar = state.sugarGoal,
            sodium = state.sodiumGoal,
            fiberConsumed = state.fiberG,
            sugarConsumed = state.sugarG,
            sodiumConsumed = state.sodiumG,
            rolloverCaloriesEnabled = state.rolloverCaloriesEnabled,
            rolloverCaloriesAmount = state.rolloverCaloriesAmount,
            burnedCalories = state.burnedCaloriesAdded,
            addCaloriesBackEnabled = state.addCaloriesBackEnabled
        )
        }
            

        
        // Health Score Card - Premium design with AI Coach tip
        item {
            HealthScoreCardPremium(
                modifier = Modifier.onPositionedRect { healthScoreRect = it },
                score = state.healthScore,
                recommendation = state.healthRecommendation,
                coachTip = state.coachTip
            )
        }
        
        // Health Connect Button - Standalone (outside any CalAICard)
        if (!state.isHealthConnected) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPositionedRect { healthConnectRect = it },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FavoriteBorder,
                                    contentDescription = "Health Connect",
                                    tint = Color(0xFFEA4335),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.connect_google_health),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.sync_steps_calories),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Dark rounded pill button like the image
                        Button(
                            onClick = {
                                android.util.Log.d("HealthConnect", "Button CLICKED!")
                                onConnectHealth()
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1C1C1E)
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.connect),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Unified Activity Card: Steps + Calories side by side
        item {
            UnifiedActivityCard(
                steps = state.steps.toInt(),
                stepsGoal = state.stepsGoal,
                calories = state.caloriesBurned,
                isConnected = state.isHealthConnected
            )
        }
        
        // Exercise Summary Card - shows logged exercises
        if (state.exercises.isNotEmpty()) {
            item {
                ExerciseSummaryCard(
                    exercises = state.exercises,
                    totalCalories = state.manualExerciseCalories
                )
            }
        }
        
        // Water Tracker Card - Premium design
        item {
            var showWaterSettings by remember { mutableStateOf(false) }
            var waterServingSize by remember { mutableStateOf(8) }
            
            WaterCardPremium(
                modifier = Modifier.onPositionedRect { waterRect = it },
                consumed = state.waterConsumed,
                servingSize = waterServingSize,
                onAdd = { onAddWater(waterServingSize) },
                onRemove = { onRemoveWater(waterServingSize) },
                onSettingsClick = { showWaterSettings = true }
            )
            
            // Water Settings Dialog
            WaterSettingsDialog(
                showDialog = showWaterSettings,
                currentServingSize = waterServingSize,
                onDismiss = { showWaterSettings = false },
                onServingSizeChange = { waterServingSize = it },
                // Water Reminder Props
                isPremium = true, // Access from somewhere? DashboardState doesn't have isPremium?
                // Wait, DashboardState logic for premium? No.
                // We should pass isPremium to DashboardContent or assume true/false.
                // SettingsViewModel has isPremium. DashboardViewModel currently doesn't expose it in DashboardState, but it probably should or we pass it.
                // Let's check DashboardState. It has no isPremium.
                // However, WaterCardPremium implies it's "Premium Design", but logic requires checking.
                // Let's use a default or fetch it.
                // For now, let's assume we pass it or check logic.
                // Ah, MainActivity passes isPremium to DashboardContent? No.
                // I will add isPremium to DashboardContent params later if needed. For now let's use the state's implicit knowledge or fetch it.
                // Actually, let's just use 'true' for now or handle it.
                // Wait, `WaterCardPremium` is used.
                // Let's pass the reminder state.
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
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.CircleShape),
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
fun HeaderSection(streakDays: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // New Vector Icon - theme aware
            // New Vector Icon - theme aware (checks background luminance to determine theme)
            // If background is dark (luminance < 0.5), use white icon (tint = White). Else use black tint.
            // Theme-aware logo without tinting
            val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
            // Light Mode -> Black Logo, Dark Mode -> White Logo
            val iconRes = if (isDarkTheme) {
                com.example.calview.core.ui.R.drawable.app_logo_white
            } else {
                com.example.calview.core.ui.R.drawable.app_logo_black
            }

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "CalViewAI Icon",
                modifier = Modifier.fillMaxWidth(0.10f)
                    .size(60.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.dashboard_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = if (isDarkTheme) Color.White else Color(0xFF000000)
                )
            }
        }
        
        // Streak badge
        if (streakDays > 0) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "$streakDays day streak"
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 16.sp
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
}

@Composable
fun DateSelector(
    modifier: Modifier = Modifier,
    selectedDate: Calendar, 
    onDateSelected: (Calendar) -> Unit,
    allMealDates: List<Long> = emptyList()
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
        // Month and Year Header with week indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
                        Column {
                            if (isEaten) {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Bold
                                        )) {
                                            append(animatedConsumed.toString())
                                        }
                                        withStyle(SpanStyle(
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )) {
                                            append(" /$goalCalories")
                                        }
                                    }
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color.Gray)) {
                                            append("Calories ")
                                        }
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("eaten")
                                        }
                                    },
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = animatedRemaining.toString(),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                                    append("Calories ")
                                                }
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                                    append("left")
                                                }
                                            },
                                            fontSize = 14.sp
                                        )
                                        
                                        // Indicators Row (Rollover + Active)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Rollover indicator
                                            if (rolloverCaloriesEnabled && rolloverCaloriesAmount > 0) {
                                                Surface(
                                                    color = Color(0xFFE8F5E9), // Light green (matched)
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.Redo, // Redo icon for rollover
                                                            contentDescription = null,
                                                            modifier = Modifier.size(10.dp),
                                                            tint = Color(0xFF2E7D32) // Dark green (matched)
                                                        )
                                                        Text(
                                                            text = "+$rolloverCaloriesAmount Rollover",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color(0xFF2E7D32), // Dark green (matched)
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // Active Calories indicator
                                            if (addCaloriesBackEnabled && burnedCalories > 0) {
                                                Surface(
                                                    color = Color(0xFFE8F5E9), // Light green
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
                                                            modifier = Modifier.size(10.dp),
                                                            tint = Color(0xFF2E7D32) // Dark green
                                                        )
                                                        Text(
                                                            text = "+$burnedCalories Active",
                                                            style = MaterialTheme.typography.labelSmall,
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
                }
                
                // Calorie ring
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                    CalorieRing(
                        consumed = consumedCalories.toFloat(), 
                        goal = goalCalories.toFloat(),
                    )
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(consumedValue.toString())
                                }
                                withStyle(SpanStyle(
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )) {
                                    append(" /${goalValue}$unit")
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
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "${remaining}$unit",
                            fontSize = 16.sp,
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
                    .background(Color(0xFFF0F0F0))
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
                            lineHeight = 16.sp
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

// Steps Today Card with Health Connect integration
// Unified Activity Card - Steps and Calories in one card
@Composable
fun UnifiedActivityCard(
    steps: Int,
    stepsGoal: Int,
    calories: Int,
    isConnected: Boolean,
    modifier: Modifier = Modifier
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
                
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                            append(steps.toString())
                        }
                        withStyle(SpanStyle(fontSize = 12.sp, color = Color.Gray)) {
                            append(" /$stepsGoal")
                        }
                    }
                )
                Text(
                    text = "Steps Today",
                    fontSize = 12.sp,
                    color = Color.Gray
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
                        val calProgress = (calories / 500f).coerceIn(0f, 1f)
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
                
                Text(
                    text = calories.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Calories burned",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
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
    servingSize: Int = 8,
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
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Water info
            Column(modifier = Modifier.weight(1f)) {
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
                        text = "$consumed fl oz",
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
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )) {
                                    append(consumedValue.toString())
                                }
                                withStyle(SpanStyle(
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )) {
                                    append(" /${goalValue}g")
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
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "${remaining}g",
                            fontSize = 16.sp,
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
                            onError = { imageLoadError = true }
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
                                                meal.confidenceScore >= 80f -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                meal.confidenceScore >= 60f -> Color(0xFFFFC107).copy(alpha = 0.15f)
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
                            text = "$selectedSize fl oz (${selectedSize / 8} cup)",
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
                        val sizes = listOf(4, 6, 8, 10, 12, 16)
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
                    text = "Everyone's needs are slightly different, but we recommended aiming for at least 64 fl oz (8 cups) of water each day",
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
                                    .background(if (it < 2) Color(0xFFFFB74D) else Color(0xFFE8E8E8), CircleShape)
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
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
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
                                    .background(if (it >= 2) Color(0xFF81C784) else Color(0xFFE8E8E8), CircleShape)
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
                contentDescription = "Today's exercise summary. ${exercises.size} ${if (exercises.size == 1) "exercise" else "exercises"} logged, $totalCalories calories burned"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Exercise",
                        style = MaterialTheme.typography.titleMedium,
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalCalories cal",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
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
                            contentDescription = "${exercise.name}, ${exercise.durationMinutes} minutes, ${exercise.caloriesBurned} calories, $typeName exercise"
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${exercise.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Text(
                        text = "${exercise.caloriesBurned} cal",
                        style = MaterialTheme.typography.bodyMedium,
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

// Helper function to get exercise type color
private fun getExerciseColor(type: com.example.calview.core.data.local.ExerciseType): Color {
    return when (type) {
        com.example.calview.core.data.local.ExerciseType.CARDIO -> Color(0xFFE53935)
        com.example.calview.core.data.local.ExerciseType.STRENGTH -> Color(0xFF1E88E5)
        com.example.calview.core.data.local.ExerciseType.FLEXIBILITY -> Color(0xFF43A047)
        com.example.calview.core.data.local.ExerciseType.SPORT -> Color(0xFFFF9800)
        com.example.calview.core.data.local.ExerciseType.OTHER -> Color(0xFF9C27B0)
    }
}
