package com.example.calview.feature.onboarding

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Consolidated onboarding navigation flow.
 * Reduced from 23 screens to 10 screens while maintaining all data collection.
 * 
 * Flow:
 * Welcome → Profile Setup → Goals & Preferences → Notifications → 
 * Health Connect → Generate Plan → Setting Up → Congratulations → 
 * Referral Code → Create Account
 */
@Composable
fun OnboardingNavHost(
    onOnboardingComplete: () -> Unit,
    onSignIn: () -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Track selected language
    var selectedLanguage by remember { mutableStateOf(supportedLanguages.first()) }
    
    // Profile setup state
    // Metric-only app (kg/cm)
    // Metric-only app (kg/cm)
    var heightCm by remember { mutableIntStateOf(170) }
    var weightKg by remember { mutableIntStateOf(68) }
    var birthMonth by remember { mutableStateOf("January") }
    var birthDay by remember { mutableIntStateOf(1) }
    var birthYear by remember { mutableIntStateOf(2001) }
    
    // Goal preferences state
    var selectedGoal by remember { mutableStateOf("") }
    var targetWeightKg by remember { mutableFloatStateOf(60f) }
    var weightChangePerWeek by remember { mutableFloatStateOf(0.5f) }
    var selectedDiet by remember { mutableStateOf("No preference") }
    var rolloverCaloriesEnabled by remember { mutableStateOf(false) }
    var addCaloriesBurnedEnabled by remember { mutableStateOf(false) }
    
    // Settings state
    var notificationsEnabled by remember { mutableStateOf(false) }
    var usedReferralCode by remember { mutableStateOf("") }
    
    // Calculate current weight in kg
    val currentWeightKg = weightKg.toFloat()
    
    // Total steps: 9 screens (removed AddCaloriesBurnedScreen)
    val totalSteps = 9

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // ============ SPLASH SCREEN ============
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        // ============ STEP 1: WELCOME ============
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = { navController.navigate("profile_setup") },
                onSignIn = onSignIn,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { language -> selectedLanguage = language }
            )
        }
        
        // ============ STEP 2: PROFILE SETUP (Consolidated) ============
        composable("profile_setup") {
            ProfileSetupScreen(
                currentStep = 2,
                totalSteps = totalSteps,
                // Gender
                selectedGender = uiState.gender,
                onGenderSelected = { viewModel.onGenderSelected(it) },
                // Birthdate
                birthMonth = birthMonth,
                birthDay = birthDay,
                birthYear = birthYear,
                onMonthChanged = { 
                    birthMonth = it
                    viewModel.onBirthDateChanged(getMonthNumber(it), birthDay, birthYear)
                },
                onDayChanged = { 
                    birthDay = it
                    viewModel.onBirthDateChanged(getMonthNumber(birthMonth), it, birthYear)
                },
                onYearChanged = { 
                    birthYear = it
                    viewModel.onBirthDateChanged(getMonthNumber(birthMonth), birthDay, it)
                },
                // Height & Weight (metric only)
                heightCm = heightCm,
                weightKg = weightKg,
                onHeightCmChanged = { 
                    heightCm = it
                    viewModel.onHeightCmChanged(it)
                },
                onWeightKgChanged = { 
                    weightKg = it
                    viewModel.onWeightChanged(it.toFloat())
                },
                // Activity Level
                selectedWorkouts = uiState.workoutsPerWeek,
                onWorkoutsSelected = { viewModel.onWorkoutsSelected(it) },
                // Navigation
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("goal_preferences") }
            )
        }
        
        // ============ STEP 3: GOAL & PREFERENCES (Consolidated) ============
        composable("goal_preferences") {
            GoalPreferencesScreen(
                currentStep = 3,
                totalSteps = totalSteps,
                // Goal
                selectedGoal = selectedGoal,
                onGoalSelected = { 
                    selectedGoal = it
                    viewModel.onGoalSelected(it)
                },
                // Weight
                currentWeightKg = currentWeightKg,
                targetWeightKg = targetWeightKg,
                onTargetWeightChanged = { 
                    targetWeightKg = it
                    viewModel.onGoalWeightChanged(it)
                },
                // Pace
                weightChangePerWeek = weightChangePerWeek,
                onPaceChanged = { weightChangePerWeek = it },
                // Diet
                selectedDiet = selectedDiet,
                onDietSelected = { 
                    selectedDiet = it
                    viewModel.onDietSelected(it)
                },
                // Rollover
                rolloverEnabled = rolloverCaloriesEnabled,
                onRolloverChanged = { 
                    rolloverCaloriesEnabled = it
                    viewModel.onRolloverExtraCaloriesChanged(it)
                },
                // Add Burned Calories
                addCaloriesBurnedEnabled = addCaloriesBurnedEnabled,
                onAddCaloriesBurnedChanged = { enabled ->
                    addCaloriesBurnedEnabled = enabled
                    viewModel.onAddCaloriesBackChanged(enabled)
                },
                // Navigation
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("notifications") }
            )
        }
        
        // ============ STEP 4: NOTIFICATIONS ============
        composable("notifications") {
            NotificationsScreen(
                currentStep = 4,
                totalSteps = totalSteps,
                notificationsEnabled = notificationsEnabled,
                onNotificationChoice = { notificationsEnabled = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("generate_plan") }
            )
        }
        
        // ============ STEP 5: GENERATE PLAN ============
        composable("generate_plan") {
            GeneratePlanScreen(
                currentStep = 5,
                totalSteps = totalSteps,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("setting_up") }
            )
        }
        
        // ============ STEP 6: SETTING UP (Loading Animation) ============
        composable("setting_up") {
            SettingUpScreen(
                onComplete = { 
                    navController.navigate("congratulations") {
                        popUpTo("setting_up") { inclusive = true }
                    }
                }
            )
        }
        
        // ============ STEP 7: CONGRATULATIONS ============
        composable("congratulations") {
            // Weights in kg for display
            CongratulationsScreen(
                currentStep = 7,
                totalSteps = totalSteps,
                goal = selectedGoal,
                currentWeight = currentWeightKg,
                targetWeight = targetWeightKg,
                weeklyPace = weightChangePerWeek,
                recommendedCalories = uiState.recommendedCalories,
                recommendedCarbs = uiState.recommendedCarbs,
                recommendedProtein = uiState.recommendedProtein,
                recommendedFats = uiState.recommendedFats,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("referral_code") }
            )
        }
        
        // ============ STEP 8: REFERRAL CODE (Optional) ============
        composable("referral_code") {
            ReferralCodeScreen(
                currentStep = 8,
                totalSteps = totalSteps,
                onBack = { navController.popBackStack() },
                onContinue = { code ->
                    usedReferralCode = code
                    viewModel.onReferralCodeUsed(code)
                    navController.navigate("create_account")
                },
                onSkip = { navController.navigate("create_account") }
            )
        }
        
        // ============ STEP 9: CREATE ACCOUNT ============
        composable("create_account") {
            CreateAccountScreen(
                currentStep = 9,
                totalSteps = totalSteps,
                onBack = { navController.popBackStack() },
                onGoogleSignIn = {
                    // Save all collected data before triggering sign-in
                    viewModel.onHeightCmChanged(heightCm) // Height stored in cm
                    viewModel.onGoalWeightChanged(targetWeightKg) // Goal weight
                    viewModel.onWeightChanged(currentWeightKg)
                    viewModel.onBirthDateChanged(
                        getMonthNumber(birthMonth),
                        birthDay,
                        birthYear
                    )
                    
                    viewModel.completeOnboarding {
                        onSignIn()
                    }
                },
                onTermsClick = onTermsClick,
                onPrivacyClick = onPrivacyClick
            )
        }
    }
}

/**
 * Convert month name to month number (1-12)
 */
private fun getMonthNumber(monthName: String): Int {
    return when (monthName) {
        "January" -> 1
        "February" -> 2
        "March" -> 3
        "April" -> 4
        "May" -> 5
        "June" -> 6
        "July" -> 7
        "August" -> 8
        "September" -> 9
        "October" -> 10
        "November" -> 11
        "December" -> 12
        else -> 1
    }
}
