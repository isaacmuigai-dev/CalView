package com.example.calview.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calview.ui.viewmodels.OnboardingViewModel

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
                onContinue = { navController.navigate("gender") }
            )
        }
        composable("gender") {
            GenderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("birthdate") }
            )
        }
        composable("birthdate") {
            BirthDateScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("height_weight") }
            )
        }
        composable("height_weight") {
            HeightWeightScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("workouts") }
            )
        }
        composable("workouts") {
            WorkoutsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("goal") }
            )
        }
        composable("goal") {
            GoalSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("diet") }
            )
        }
        composable("diet") {
            DietPreferenceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("calories_burned") }
            )
        }
        composable("calories_burned") {
            CaloriesBurnedSettingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("calorie_rollover") }
            )
        }
        composable("calorie_rollover") {
            CalorieRolloverScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = { navController.navigate("previous_apps") }
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
                onBack = { navController.popBackStack() },
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
