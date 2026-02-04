package com.example.calview.feature.onboarding

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log

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
    isSignedIn: Boolean = false,
    isSigningIn: Boolean = false,
    isRedirecting: Boolean = false,
    isOnboardingComplete: Boolean = false,
    onOnboardingComplete: () -> Unit,
    onSignIn: () -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    Log.d("OnboardingNav", "OnboardingNavHost entered: isSignedIn=$isSignedIn, isRedirecting=$isRedirecting")
    val navController = rememberNavController()
    
    // NOTE: Removed auto-navigation to profile_setup on sign-in
    // Navigation is now controlled entirely by MainActivity:
    // - For returning users: MainActivity navigates directly to "main"
    // For new users: MainActivity keeps them in onboarding, user clicks "Get Started" to go to profile_setup
    // This prevents the race condition where profile_setup shows briefly before redirecting to main

    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Track selected language
    val currentLanguageCode by viewModel.language.collectAsState()
    
    // Initialize selected language based on stored preference
    var selectedLanguage by remember(currentLanguageCode) { 
        mutableStateOf(
            supportedLanguages.find { it.code.equals(currentLanguageCode, ignoreCase = true) } 
            ?: supportedLanguages.first()
        ) 
    }
    
    // Profile setup state
    // Metric-only app (kg/cm)
    // Metric-only app (kg/cm)
    var heightCm by remember { mutableIntStateOf(170) }
    var weightKg by remember { mutableIntStateOf(68) }
    var birthMonth by remember { mutableIntStateOf(1) }
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
    
    // Total steps: 10 screens
    val totalSteps = 10

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
            // Auto-navigate to profile setup if already signed in AND not currently checking/redirecting
            // This handles the case where a new user signs in on the Welcome screen
            LaunchedEffect(isSignedIn, isSigningIn, isRedirecting, isOnboardingComplete) {
                if (isSignedIn && !isSigningIn && !isRedirecting && !isOnboardingComplete) {
                    Log.d("OnboardingNav", "Navigating to profile_setup: signed in and checks complete")
                    navController.navigate("profile_setup")
                }
            }

            WelcomeScreen(
                onGetStarted = { navController.navigate("profile_setup") },
                onSignIn = onSignIn,
                isLoading = isSigningIn || isRedirecting,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { language -> 
                    selectedLanguage = language
                    viewModel.setLanguage(language.code)
                }
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
                    viewModel.onBirthDateChanged(it, birthDay, birthYear)
                },
                onDayChanged = { 
                    birthDay = it
                    viewModel.onBirthDateChanged(birthMonth, it, birthYear)
                },
                onYearChanged = { 
                    birthYear = it
                    viewModel.onBirthDateChanged(birthMonth, birthDay, it)
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
                onContinue = { navController.navigate("features_explain") }
            )
        }
        
        // ============ STEP 8: FEATURES EXPLAIN ============
        composable("features_explain") {
            FeaturesExplainScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate("referral_code") }
            )
        }
        
        // ============ STEP 9: REFERRAL CODE (Optional) ============
        composable("referral_code") {
            ReferralCodeScreen(
                currentStep = 9,
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
        
        // ============ STEP 10: CREATE ACCOUNT ============
        composable("create_account") {
            CreateAccountScreen(
                currentStep = 10,
                totalSteps = totalSteps,
                isSignedIn = isSignedIn,
                onBack = { navController.popBackStack() },
                onGoogleSignIn = {
                    // Logic handles both "Sign In" (if not signed in) and "Complete" (if signed in)
                    
                    // Save all collected data
                    viewModel.onHeightCmChanged(heightCm)
                    viewModel.onGoalWeightChanged(targetWeightKg)
                    viewModel.onWeightChanged(currentWeightKg)
                    viewModel.onBirthDateChanged(
                        birthMonth,
                        birthDay,
                        birthYear
                    )
                    
                    if (isSignedIn) {
                        // Already signed in (e.g. from Welcome screen), just complete onboarding
                        viewModel.completeOnboarding {
                            onOnboardingComplete()
                        }
                    } else {
                        // Not signed in, trigger sign in flow
                        // Data will be saved/completed in the completeOnboarding generic flow or handled by MainActivity return?
                        // Actually, MainActivity return handles the "success" case but WE need to call completeOnboarding
                        // The pattern used before was: completeOnboarding THEN sign in.
                        // But if sign in fails, we have saved data but not auth.
                        // The original code was: COMPLETE then SIGN IN. 
                        
                        viewModel.completeOnboarding {
                            onSignIn()
                        }
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

