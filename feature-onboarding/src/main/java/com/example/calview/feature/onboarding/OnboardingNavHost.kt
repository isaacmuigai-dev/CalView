package com.example.calview.feature.onboarding

import androidx.compose.runtime.Composable
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

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onGetStarted = { navController.navigate("accomplishments") },
                onSignIn = onSignIn
            )
        }
        composable("accomplishments") {
            AccomplishmentsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("ratings") }
            )
        }
        composable("ratings") {
            RatingsScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("trust") }
            )
        }
        composable("trust") {
            TrustScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("transition") }
            )
        }
        composable("transition") {
            WeightTransitionScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("user_profile") }
            )
        }
        // Consolidated user profile screen replaces: gender, birthdate, height_weight, workouts, goal, diet
        composable("user_profile") {
            UserProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = { navController.navigate("calories_burned") }
            )
        }
        composable("calories_burned") {
            CaloriesBurnedSettingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("calorie_rollover") }
            )
        }
        composable("calorie_rollover") {
            CalorieRolloverScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("previous_apps") }
            )
        }
        composable("previous_apps") {
            PreviousAppsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("source") }
            )
        }
        composable("source") {
            SourceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("referral") }
            )
        }
        composable("referral") {
            ReferralCodeScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate("generation") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("generation") {
            PlanGenerationScreen(
                onContinue = { navController.navigate("setup") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("setup") {
            SetupProgressScreen(
                onFinish = { navController.navigate("plan_ready") }
            )
        }
        composable("plan_ready") {
            CustomPlanReadyScreen(
                viewModel = viewModel,
                onContinue = { navController.navigate("goals_explanation") }
            )
        }
        composable("goals_explanation") {
            GoalsExplanationScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("create_account") }
            )
        }
        composable("create_account") {
            CreateAccountScreen(
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("trial") }
            )
        }
        composable("trial") {
            TrialScreen(
                onContinue = { navController.navigate("discount_spin") }
            )
        }
        composable("discount_spin") {
            DiscountSpinScreen(
                onContinue = { navController.navigate("one_time_offer") }
            )
        }
        composable("one_time_offer") {
            OneTimeOfferScreen(
                onStartTrial = { viewModel.completeOnboarding(onOnboardingComplete) },
                onClose = { viewModel.completeOnboarding(onOnboardingComplete) }
            )
        }
    }
}
