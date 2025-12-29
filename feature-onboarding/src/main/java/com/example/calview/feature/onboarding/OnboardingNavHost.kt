package com.example.calview.feature.onboarding

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnboardingNavHost(
    onOnboardingComplete: () -> Unit,
    onSignIn: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // Track selected language
    var selectedLanguage by remember { mutableStateOf(supportedLanguages.first()) }
    
    // Local state for onboarding flow
    var selectedTriedApps by remember { mutableStateOf("") }
    var isMetric by remember { mutableStateOf(true) }
    var heightFeet by remember { mutableIntStateOf(5) }
    var heightInches by remember { mutableIntStateOf(6) }
    var heightCm by remember { mutableIntStateOf(170) }
    var weightLb by remember { mutableIntStateOf(150) }
    var weightKg by remember { mutableIntStateOf(68) }
    var birthMonth by remember { mutableStateOf("January") }
    var birthDay by remember { mutableIntStateOf(1) }
    var birthYear by remember { mutableIntStateOf(2001) }
    var selectedGoal by remember { mutableStateOf("") }
    
    // Weight change state (for both lose and gain weight)
    var desiredWeightKg by remember { mutableFloatStateOf(60f) }
    var weightChangePerWeek by remember { mutableFloatStateOf(0.8f) }
    var selectedObstacle by remember { mutableStateOf("") }
    var selectedDiet by remember { mutableStateOf("") }
    
    // Final screens state
    var selectedAccomplishment by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var addCaloriesBurnedEnabled by remember { mutableStateOf(false) }
    var rolloverCaloriesEnabled by remember { mutableStateOf(false) }
    
    // Calculate current weight in kg
    val currentWeightKg = if (isMetric) weightKg.toFloat() else weightLb / 2.205f
    
    // Check if Gain Weight mode
    val isGainWeight = selectedGoal == "Gain Weight"

    // Total steps: 22 for lose/gain weight, 17 for maintain (added create_account)
    // Lose/Gain: 7 base + 6 weight screens + 5 final + 3 plan + 1 account = 22
    // Maintain: 7 base + 1 diet + 5 final + 3 plan + 1 account = 17
    val totalSteps = when (selectedGoal) {
        "Lose Weight", "Gain Weight" -> 22
        "Maintain" -> 17
        else -> 7
    }

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        // Step 0: Welcome
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = { navController.navigate("gender") },
                onSignIn = onSignIn,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { language -> selectedLanguage = language }
            )
        }
        
        // Step 1: Gender selection
        composable("gender") {
            GenderScreen(
                currentStep = 1,
                totalSteps = totalSteps,
                selectedGender = uiState.gender,
                onGenderSelected = { viewModel.onGenderSelected(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("workouts") }
            )
        }
        
        // Step 2: Workouts per week
        composable("workouts") {
            WorkoutsScreen(
                currentStep = 2,
                totalSteps = totalSteps,
                selectedWorkouts = uiState.workoutsPerWeek,
                onWorkoutsSelected = { viewModel.onWorkoutsSelected(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("referral_source") }
            )
        }
        
        // Step 3: Referral source
        composable("referral_source") {
            ReferralSourceScreen(
                currentStep = 3,
                totalSteps = totalSteps,
                selectedSource = uiState.referralSource,
                onSourceSelected = { viewModel.onSourceSelected(it) },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("tried_apps") }
            )
        }
        
        // Step 4: Tried other apps
        composable("tried_apps") {
            TriedOtherAppsScreen(
                currentStep = 4,
                totalSteps = totalSteps,
                selectedAnswer = selectedTriedApps,
                onAnswerSelected = { selectedTriedApps = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("height_weight") }
            )
        }
        
        // Step 5: Height & Weight
        composable("height_weight") {
            HeightWeightScreen(
                currentStep = 5,
                totalSteps = totalSteps,
                isMetric = isMetric,
                heightFeet = heightFeet,
                heightInches = heightInches,
                heightCm = heightCm,
                weightLb = weightLb,
                weightKg = weightKg,
                onMetricToggle = { isMetric = it },
                onHeightFeetChanged = { heightFeet = it },
                onHeightInchesChanged = { heightInches = it },
                onHeightCmChanged = { heightCm = it },
                onWeightLbChanged = { weightLb = it },
                onWeightKgChanged = { weightKg = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("birthdate") }
            )
        }
        
        // Step 6: Birthdate
        composable("birthdate") {
            BirthdateScreen(
                currentStep = 6,
                totalSteps = totalSteps,
                selectedMonth = birthMonth,
                selectedDay = birthDay,
                selectedYear = birthYear,
                onMonthChanged = { birthMonth = it },
                onDayChanged = { birthDay = it },
                onYearChanged = { birthYear = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("goal") }
            )
        }
        
        // Step 7: Goal selection
        composable("goal") {
            GoalScreen(
                currentStep = 7,
                totalSteps = totalSteps,
                selectedGoal = selectedGoal,
                onGoalSelected = { selectedGoal = it },
                onBack = { navController.popBackStack() },
                onContinue = {
                    when (selectedGoal) {
                        "Lose Weight", "Gain Weight" -> {
                            // Navigate to weight change flow
                            navController.navigate("desired_weight")
                        }
                        "Maintain" -> {
                            // Go directly to diet screen
                            navController.navigate("specific_diet")
                        }
                        else -> {
                            viewModel.completeOnboarding(onOnboardingComplete)
                        }
                    }
                }
            )
        }
        
        // ============ LOSE/GAIN WEIGHT SHARED SCREENS ============
        
        // Step 8: Desired Weight
        composable("desired_weight") {
            DesiredWeightScreen(
                currentStep = 8,
                totalSteps = totalSteps,
                currentWeightKg = currentWeightKg,
                isKg = isMetric,
                desiredWeightKg = desiredWeightKg,
                isGainWeight = isGainWeight,
                onUnitToggle = { isMetric = it },
                onWeightChanged = { desiredWeightKg = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("realistic_target") }
            )
        }
        
        // Step 9: Realistic Target
        composable("realistic_target") {
            RealisticTargetScreen(
                currentStep = 9,
                totalSteps = totalSteps,
                currentWeightKg = currentWeightKg,
                desiredWeightKg = desiredWeightKg,
                isKg = isMetric,
                isGainWeight = isGainWeight,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("how_fast") }
            )
        }
        
        // Step 10: How Fast
        composable("how_fast") {
            HowFastScreen(
                currentStep = 10,
                totalSteps = totalSteps,
                isKg = isMetric,
                weightChangePerWeek = weightChangePerWeek,
                isGainWeight = isGainWeight,
                onWeightChangeChanged = { weightChangePerWeek = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("cal_ai_comparison") }
            )
        }
        
        // Step 11: CalViewAI Comparison
        composable("cal_ai_comparison") {
            CalAIComparisonScreen(
                currentStep = 11,
                totalSteps = totalSteps,
                isGainWeight = isGainWeight,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("whats_stopping_you") }
            )
        }
        
        // Step 12: What's Stopping You
        composable("whats_stopping_you") {
            WhatsStoppingYouScreen(
                currentStep = 12,
                totalSteps = totalSteps,
                selectedObstacle = selectedObstacle,
                onObstacleSelected = { selectedObstacle = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("specific_diet") }
            )
        }
        
        // Step 13 (Lose/Gain) or Step 8 (Maintain): Specific Diet
        composable("specific_diet") {
            val dietStep = when (selectedGoal) {
                "Maintain" -> 8
                else -> 13
            }
            SpecificDietScreen(
                currentStep = dietStep,
                totalSteps = totalSteps,
                selectedDiet = selectedDiet,
                onDietSelected = { selectedDiet = it },
                onBack = { navController.popBackStack() },
                onContinue = {
                    // Navigate to final shared screens
                    navController.navigate("accomplish")
                }
            )
        }
        
        // ============ FINAL SHARED SCREENS (ALL PATHS) ============
        
        // Step 14 (Lose/Gain) or Step 9 (Maintain): What would you like to accomplish
        composable("accomplish") {
            val step = when (selectedGoal) {
                "Maintain" -> 9
                else -> 14
            }
            AccomplishScreen(
                currentStep = step,
                totalSteps = totalSteps,
                selectedAccomplishment = selectedAccomplishment,
                onAccomplishmentSelected = { selectedAccomplishment = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("thank_you") }
            )
        }
        
        // Step 15 (Lose/Gain) or Step 10 (Maintain): Thank you for trusting us
        composable("thank_you") {
            val step = when (selectedGoal) {
                "Maintain" -> 10
                else -> 15
            }
            ThankYouScreen(
                currentStep = step,
                totalSteps = totalSteps,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("notifications") }
            )
        }
        
        // Step 16 (Lose/Gain) or Step 11 (Maintain): Notifications
        composable("notifications") {
            val step = when (selectedGoal) {
                "Maintain" -> 11
                else -> 16
            }
            NotificationsScreen(
                currentStep = step,
                totalSteps = totalSteps,
                notificationsEnabled = notificationsEnabled,
                onNotificationChoice = { notificationsEnabled = it },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("add_calories_burned") }
            )
        }
        
        // Step 17 (Lose/Gain) or Step 12 (Maintain): Add calories burned
        composable("add_calories_burned") {
            val step = when (selectedGoal) {
                "Maintain" -> 12
                else -> 17
            }
            AddCaloriesBurnedScreen(
                currentStep = step,
                totalSteps = totalSteps,
                addCaloriesBurnedEnabled = addCaloriesBurnedEnabled,
                onChoice = { enabled ->
                    addCaloriesBurnedEnabled = enabled
                    viewModel.onAddCaloriesBackChanged(enabled)
                },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("rollover_calories") }
            )
        }
        
        // Step 18 (Lose/Gain) or Step 13 (Maintain): Rollover calories
        composable("rollover_calories") {
            val step = when (selectedGoal) {
                "Maintain" -> 13
                else -> 18
            }
            RolloverCaloriesScreen(
                currentStep = step,
                totalSteps = totalSteps,
                rolloverEnabled = rolloverCaloriesEnabled,
                onChoice = { enabled ->
                    rolloverCaloriesEnabled = enabled
                    viewModel.onRolloverExtraCaloriesChanged(enabled)
                },
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("generate_plan") }
            )
        }
        
        // ============ PLAN GENERATION SCREENS ============
        
        // Step 19 (Lose/Gain) or Step 14 (Maintain): Generate Plan
        composable("generate_plan") {
            val step = when (selectedGoal) {
                "Maintain" -> 14
                else -> 19
            }
            GeneratePlanScreen(
                currentStep = step,
                totalSteps = totalSteps,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("setting_up") }
            )
        }
        
        // Step 20 (Lose/Gain) or Step 15 (Maintain): Setting Up (no back, auto-advances)
        composable("setting_up") {
            SettingUpScreen(
                onComplete = { navController.navigate("congratulations") {
                    popUpTo("setting_up") { inclusive = true }
                }}
            )
        }
        
        // Step 21 (Lose/Gain) or Step 16 (Maintain): Congratulations
        composable("congratulations") {
            val step = when (selectedGoal) {
                "Maintain" -> 16
                else -> 21
            }
            CongratulationsScreen(
                currentStep = step,
                totalSteps = totalSteps,
                goal = selectedGoal,
                targetWeight = desiredWeightKg,
                recommendedCalories = uiState.recommendedCalories,
                recommendedCarbs = uiState.recommendedCarbs,
                recommendedProtein = uiState.recommendedProtein,
                recommendedFats = uiState.recommendedFats,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("create_account") }
            )
        }
        
        // Step 22 (Lose/Gain) or Step 17 (Maintain): Create Account (mandatory Google sign-in)
        composable("create_account") {
            val step = when (selectedGoal) {
                "Maintain" -> 17
                else -> 22
            }
            CreateAccountScreen(
                currentStep = step,
                totalSteps = totalSteps,
                onBack = { navController.popBackStack() },
                onGoogleSignIn = {
                    // Save onboarding data then trigger Google sign-in
                    viewModel.completeOnboarding {
                        onSignIn()
                    }
                }
            )
        }
    }
}
