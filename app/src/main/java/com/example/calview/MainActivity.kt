package com.example.calview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.ui.theme.CalViewTheme
import com.example.calview.feature.dashboard.DashboardScreen
import com.example.calview.feature.dashboard.DashboardViewModel
import com.example.calview.feature.dashboard.EditNameScreen
import com.example.calview.feature.dashboard.ReferYourFriendScreen
import com.example.calview.feature.dashboard.SettingsScreen
import com.example.calview.feature.dashboard.SettingsViewModel
import com.example.calview.feature.onboarding.OnboardingNavHost
import com.example.calview.feature.onboarding.SignInBottomSheet
import com.example.calview.feature.scanner.MyMealsScreen
import com.example.calview.feature.scanner.ScannerScreen
import com.example.calview.feature.scanner.ScannerViewModel
import com.example.calview.feature.trends.ProgressScreen
import com.example.calview.feature.subscription.PaywallScreen
import com.example.calview.core.data.billing.BillingManager
import com.example.calview.feature.dashboard.PersonalDetailsScreen
import com.example.calview.feature.dashboard.PersonalDetailsViewModel
import com.example.calview.feature.dashboard.EditStepsGoalScreen
import com.example.calview.feature.dashboard.EditGoalWeightScreen
import com.example.calview.feature.dashboard.EditHeightWeightScreen
import com.example.calview.feature.dashboard.EditBirthdayScreen
import com.example.calview.feature.dashboard.EditGenderScreen
import com.example.calview.feature.dashboard.EditNutritionGoalsScreen
import com.example.calview.feature.dashboard.WeightHistoryScreen
import com.example.calview.feature.dashboard.HowToAddWidgetScreen

import com.example.calview.feature.dashboard.OpenSourceLicensesScreen
import com.example.calview.feature.dashboard.FeatureRequestScreen
import com.example.calview.feature.dashboard.LanguageSelectorScreen
import com.example.calview.feature.dashboard.AutoGenerateGoalsNavHost
import com.example.calview.feature.dashboard.NutritionGoals
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.calview.core.ui.util.LocalWindowSizeClass


@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var userPreferencesRepository: com.example.calview.core.data.repository.UserPreferencesRepository
    
    @Inject
    lateinit var mealRepository: com.example.calview.core.data.repository.MealRepository
    
    @Inject
    lateinit var dailyLogRepository: com.example.calview.core.data.repository.DailyLogRepository
    
    @Inject
    lateinit var firestoreRepository: com.example.calview.core.data.repository.FirestoreRepository
    
    @Inject
    lateinit var billingManager: BillingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Update widget when app opens to ensure it shows latest data
        com.example.calview.widget.CaloriesWidgetProvider.requestUpdate(this)
        
        // Set user email for billing manager (enables test account premium bypass)
        billingManager.setUserEmail(authRepository.getUserEmail().takeIf { it.isNotEmpty() })
        
        setContent {
            // Calculate window size class for adaptive layouts
            val windowSizeClass = calculateWindowSizeClass(this)
            
            // Collect appearance mode from preferences
            val appearanceMode by userPreferencesRepository.appearanceMode.collectAsState(initial = "automatic")
            
            // Provide WindowSizeClass to the entire app
            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                CalViewTheme(appearanceMode = appearanceMode) {
                    AppNavigation(
                        isSignedIn = authRepository.isSignedIn(),
                        userPreferencesRepository = userPreferencesRepository,
                        authRepository = authRepository,
                        mealRepository = mealRepository,
                        dailyLogRepository = dailyLogRepository,
                        firestoreRepository = firestoreRepository,
                        billingManager = billingManager
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    isSignedIn: Boolean = false,
    userPreferencesRepository: com.example.calview.core.data.repository.UserPreferencesRepository? = null,
    authRepository: AuthRepository? = null,
    mealRepository: com.example.calview.core.data.repository.MealRepository? = null,
    dailyLogRepository: com.example.calview.core.data.repository.DailyLogRepository? = null,
    firestoreRepository: com.example.calview.core.data.repository.FirestoreRepository? = null,
    billingManager: BillingManager? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for sign-in bottom sheet
    var showSignInSheet by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    
    // Always start at splash - it will redirect based on auth state
    val startDestination = "splash"
    
    // Google Sign-In function
    fun signInWithGoogle() {
        scope.launch {
            isSigningIn = true
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Configure Google ID request
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setAutoSelectEnabled(false)
                    .build()
                
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                
                val result = credentialManager.getCredential(
                    request = request,
                    context = context as android.app.Activity
                )
                
                handleSignInResult(result) { success, displayName, photoUrl ->
                    isSigningIn = false
                    if (success) {
                        // Save Google profile info and restore data from cloud
                        scope.launch {
                            // Restore user data from Firestore first
                            val restored = userPreferencesRepository?.restoreFromCloud() ?: false
                            
                            // Also restore meals and daily logs
                            mealRepository?.restoreFromCloud()
                            dailyLogRepository?.restoreFromCloud()
                            
                            if (!restored) {
                                // If no cloud data found (new user), save profile info
                                userPreferencesRepository?.setUserName(displayName)
                                userPreferencesRepository?.setPhotoUrl(photoUrl)
                                // Generate referral code if needed
                                val code = com.example.calview.core.data.repository.UserPreferencesRepositoryImpl.generateReferralCode()
                                userPreferencesRepository?.setReferralCode(code)
                            } else {
                                // Cloud data restored - update name/photo if they changed
                                userPreferencesRepository?.setUserName(displayName)
                                userPreferencesRepository?.setPhotoUrl(photoUrl)
                            }
                        }
                        showSignInSheet = false
                        // Navigate to main (dashboard)
                        navController.navigate("main") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
                isSigningIn = false
            }
        }
    }
    
    // Sign-in bottom sheet
    if (showSignInSheet) {
        SignInBottomSheet(
            onDismiss = { showSignInSheet = false },
            onGoogleSignIn = { signInWithGoogle() },
            onTermsClick = { 
                showSignInSheet = false
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/terms_of_service.html")))
            },
            onPrivacyClick = { 
                showSignInSheet = false
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/privacy_policy.html")))
            },
            isLoading = isSigningIn
        )
    }

    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash screen - always shows first, then redirects based on auth state
        composable("splash") {
            com.example.calview.feature.onboarding.SplashScreen(
                onTimeout = {
                    val destination = if (isSignedIn) "main" else "onboarding"
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("onboarding") {
            OnboardingNavHost(
                onOnboardingComplete = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onSignIn = { showSignInSheet = true },
                onTermsClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/terms_of_service.html"))) },
                onPrivacyClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/privacy_policy.html"))) }
            )
        }
        
        composable(
            route = "main?tab={tab}&scrollToUploads={scrollToUploads}",
            arguments = listOf(
                androidx.navigation.navArgument("tab") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                },
                androidx.navigation.navArgument("scrollToUploads") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            val scrollToUploads = backStackEntry.arguments?.getBoolean("scrollToUploads") ?: false
            val isPremium by (billingManager?.isPremium ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
            MainTabs(
                initialTab = initialTab,
                scrollToRecentUploads = scrollToUploads,
                isPremium = isPremium,
                onPaywallClick = { navController.navigate("paywall") },
                onScanClick = { navController.navigate("scanner") },
                onFoodDatabaseClick = { navController.navigate("logFood") },
                onEditNameClick = { name -> navController.navigate("edit_name/$name") },
                onReferFriendClick = { navController.navigate("refer_friend") },
                onPersonalDetailsClick = { navController.navigate("personal_details") },
                onEditMacrosClick = { navController.navigate("edit_nutrition_goals") },
                onWeightHistoryClick = { navController.navigate("weight_history") },
                onLanguageClick = { navController.navigate("language_selector") },
                onHowToAddWidgetClick = { navController.navigate("how_to_add_widget") },
                onTermsClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/terms_of_service.html"))) },
                onPrivacyClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/privacy_policy.html"))) },
                onFeatureRequestClick = { navController.navigate("feature_request") },
                onLicensesClick = { navController.navigate("open_source_licenses") },
                onDeleteAccount = {
                    scope.launch {
                        // Get user ID before deletion (needed for Firestore cleanup)
                        val userId = authRepository?.getUserId() ?: ""
                        
                        if (userId.isEmpty()) {
                            android.util.Log.e("DeleteAccount", "No user ID found")
                            return@launch
                        }
                        
                        // First, delete all user data from Firestore
                        try {
                            android.util.Log.d("DeleteAccount", "Deleting Firestore data for user: $userId")
                            firestoreRepository?.deleteUserData(userId)
                            android.util.Log.d("DeleteAccount", "Firestore data deleted successfully")
                        } catch (e: Exception) {
                            android.util.Log.e("DeleteAccount", "Failed to delete Firestore data", e)
                            // Continue with local cleanup even if Firestore cleanup fails
                        }
                        
                        // Clear local Room database (meals)
                        try {
                            android.util.Log.d("DeleteAccount", "Clearing local meals...")
                            mealRepository?.clearAllMeals()
                            android.util.Log.d("DeleteAccount", "Local meals cleared")
                        } catch (e: Exception) {
                            android.util.Log.e("DeleteAccount", "Failed to clear local meals", e)
                        }
                        
                        // Clear local DataStore preferences
                        try {
                            android.util.Log.d("DeleteAccount", "Clearing local preferences...")
                            userPreferencesRepository?.clearAllData()
                            android.util.Log.d("DeleteAccount", "Local preferences cleared")
                        } catch (e: Exception) {
                            android.util.Log.e("DeleteAccount", "Failed to clear local preferences", e)
                        }
                        
                        // Then delete Firebase Auth account
                        authRepository?.deleteAccount()?.let { result ->
                            if (result.isSuccess) {
                                // Navigate to splash screen after account deletion (splash -> welcome)
                                navController.navigate("splash") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                val exception = result.exceptionOrNull()
                                if (exception is com.example.calview.core.data.repository.ReAuthenticationRequiredException) {
                                    // Need to re-authenticate - trigger Google Sign-In
                                    try {
                                        val credentialManager = CredentialManager.create(context)
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(true)
                                            .setServerClientId(context.getString(R.string.default_web_client_id))
                                            .build()
                                        
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()
                                        
                                        val response = credentialManager.getCredential(context, request)
                                        val credential = response.credential
                                        
                                        if (credential is androidx.credentials.CustomCredential &&
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                            val idToken = googleIdTokenCredential.idToken
                                            
                                            // Re-authenticate with Firebase
                                            authRepository.reauthenticateWithGoogle(idToken).let { reAuthResult ->
                                                if (reAuthResult.isSuccess) {
                                                    // Retry delete after re-authentication
                                                    authRepository.deleteAccount().let { deleteResult ->
                                                        if (deleteResult.isSuccess) {
                                                            // Navigate to splash screen (splash -> welcome)
                                                            navController.navigate("splash") {
                                                                popUpTo(0) { inclusive = true }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Re-authentication failed
                                        android.util.Log.e("DeleteAccount", "Re-auth failed", e)
                                    }
                                }
                            }
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        // Sync all user data to Firestore before signing out
                        userPreferencesRepository?.syncToCloud()
                        authRepository?.signOut()
                        // Navigate to welcome/onboarding screen after logout
                        navController.navigate("onboarding") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("how_to_add_widget") {
            HowToAddWidgetScreen(
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                }
            )
        }
        

        
        composable("feature_request") {
            val feedbackHubViewModel: com.example.calview.feature.dashboard.FeedbackHubViewModel = hiltViewModel()
            FeatureRequestScreen(
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                },
                viewModel = feedbackHubViewModel
            )
        }
        
        composable("open_source_licenses") {
            OpenSourceLicensesScreen(
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                }
            )
        }
        
        composable("scanner") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                viewModel = scannerViewModel,
                onClose = { navController.popBackStack() },
                onFoodCaptured = {
                    // Navigate to dashboard (Home tab) after food capture
                    // scrollToUploads=true triggers auto-scroll to Recent Uploads section
                    navController.navigate("main?tab=0&scrollToUploads=true") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
        
        composable("paywall") {
            billingManager?.let { manager ->
                PaywallScreen(
                    billingManager = manager,
                    onClose = { navController.popBackStack() },
                    onSubscriptionSuccess = {
                        // Navigate back and proceed to camera menu
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable("logFood") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            val meals by scannerViewModel.getAllMealsFlow().collectAsState(initial = emptyList())
            
            MyMealsScreen(
                meals = meals,
                onBack = { navController.popBackStack() },
                onScanFood = { navController.navigate("scanner") },
                onCreateMeal = { name, calories, protein, carbs, fats ->
                    scannerViewModel.createCustomMeal(name, calories, protein, carbs, fats)
                },
                onDeleteMeal = { mealId ->
                    scannerViewModel.deleteMeal(mealId)
                }
            )
        }
        
        composable("edit_name/{currentName}") { backStackEntry ->
            val currentName = backStackEntry.arguments?.getString("currentName") ?: ""
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            EditNameScreen(
                currentName = currentName,
                onBack = { navController.popBackStack() },
                onSave = { newName ->
                    settingsViewModel.updateUserName(newName)
                    navController.popBackStack()
                }
            )
        }
        
        composable("refer_friend") {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsState()
            ReferYourFriendScreen(
                referralCode = uiState.referralCode,
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                }
            )
        }
        
        composable("personal_details") {
            PersonalDetailsScreen(
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                },
                onEditGoalWeight = { navController.navigate("edit_goal_weight") },
                onEditHeightWeight = { navController.navigate("edit_height_weight") },
                onEditBirthday = { navController.navigate("edit_birthday") },
                onEditGender = { navController.navigate("edit_gender") },
                onEditStepsGoal = { navController.navigate("edit_steps_goal") }
            )
        }
        
        composable("edit_steps_goal") {
            val viewModel: PersonalDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            EditStepsGoalScreen(
                currentStepsGoal = uiState.dailyStepsGoal,
                onBack = { navController.popBackStack() },
                onSave = { steps ->
                    viewModel.updateDailyStepsGoal(steps)
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_goal_weight") {
            val viewModel: PersonalDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            EditGoalWeightScreen(
                currentWeight = uiState.currentWeight,
                goalWeight = uiState.goalWeight,
                onBack = { navController.popBackStack() },
                onSave = { weight ->
                    viewModel.updateGoalWeight(weight)
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_height_weight") {
            val viewModel: PersonalDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            EditHeightWeightScreen(
                currentHeightCm = uiState.height,
                currentWeightKg = uiState.currentWeight,
                onBack = { navController.popBackStack() },
                onSave = { heightCm, weightKg ->
                    viewModel.updateHeight(heightCm)
                    viewModel.updateWeight(weightKg)
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_birthday") {
            val viewModel: PersonalDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            EditBirthdayScreen(
                currentMonth = uiState.birthMonth,
                currentDay = uiState.birthDay,
                currentYear = uiState.birthYear,
                onBack = { navController.popBackStack() },
                onSave = { month, day, year ->
                    viewModel.updateBirthDate(month, day, year)
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_gender") {
            val viewModel: PersonalDetailsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            EditGenderScreen(
                currentGender = uiState.gender,
                onBack = { navController.popBackStack() },
                onSave = { gender ->
                    viewModel.updateGender(gender)
                    navController.popBackStack()
                }
            )
        }
        
        composable("edit_nutrition_goals") {
            // Collect current values from repository
            val currentCalories by userPreferencesRepository?.recommendedCalories?.collectAsState(initial = 1730)
                ?: remember { mutableStateOf(1730) }
            val currentProtein by userPreferencesRepository?.recommendedProtein?.collectAsState(initial = 116)
                ?: remember { mutableStateOf(116) }
            val currentCarbs by userPreferencesRepository?.recommendedCarbs?.collectAsState(initial = 207)
                ?: remember { mutableStateOf(207) }
            val currentFats by userPreferencesRepository?.recommendedFats?.collectAsState(initial = 48)
                ?: remember { mutableStateOf(48) }
            
            EditNutritionGoalsScreen(
                currentCalories = currentCalories,
                currentProtein = currentProtein,
                currentCarbs = currentCarbs,
                currentFats = currentFats,
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                },
                onSave = { calories, protein, carbs, fats, fiber, sugar, sodium ->
                    // Save nutrition goals to repository
                    scope.launch {
                        userPreferencesRepository?.saveRecommendedMacros(
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fats = fats
                        )
                    }
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                },
                onAutoGenerate = {
                    navController.navigate("auto_generate_goals")
                }
            )
        }
        
        composable("auto_generate_goals") {
            // Load current user data from repository
            val currentGender by userPreferencesRepository?.gender?.collectAsState(initial = "Male")
                ?: remember { mutableStateOf("Male") }
            val currentAge by userPreferencesRepository?.age?.collectAsState(initial = 25)
                ?: remember { mutableStateOf(25) }
            val currentHeight by userPreferencesRepository?.height?.collectAsState(initial = 170)
                ?: remember { mutableStateOf(170) }
            val currentWeight by userPreferencesRepository?.weight?.collectAsState(initial = 70f)
                ?: remember { mutableStateOf(70f) }
            
            AutoGenerateGoalsNavHost(
                initialGender = currentGender,
                initialAge = currentAge,
                initialHeightCm = currentHeight,
                initialWeightKg = currentWeight,
                onBack = { navController.popBackStack() },
                onComplete = { goals ->
                    // Save generated goals to repository
                    scope.launch {
                        userPreferencesRepository?.saveRecommendedMacros(
                            calories = goals.calories,
                            protein = goals.protein,
                            carbs = goals.carbs,
                            fats = goals.fats
                        )
                    }
                    // Navigate back past auto_generate_goals to edit_nutrition_goals
                    navController.popBackStack()
                }
            )
        }
        
        composable("weight_history") {
            WeightHistoryScreen(
                weightHistory = emptyList(), // TODO: Load from repository
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                }
            )
        }
        
        composable("language_selector") {
            LanguageSelectorScreen(
                currentLanguage = "en",
                onBack = { 
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                },
                onLanguageSelected = { languageCode ->
                    // Save language preference
                    navController.navigate("main?tab=2") {
                        popUpTo("main?tab=2") { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Handle the credential result and authenticate with Firebase.
 */
private fun handleSignInResult(
    result: GetCredentialResponse,
    onComplete: (success: Boolean, displayName: String, photoUrl: String) -> Unit
) {
    when (val credential = result.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    
                    // Authenticate with Firebase
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleIdTokenCredential.idToken,
                        null
                    )
                    
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Save Google profile info
                                val displayName = googleIdTokenCredential.displayName ?: ""
                                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
                                onComplete(true, displayName, photoUrl)
                            } else {
                                onComplete(false, "", "")
                            }
                        }
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("GoogleSignIn", "Invalid Google ID Token", e)
                    onComplete(false, "", "")
                }
            } else {
                Log.e("GoogleSignIn", "Unexpected credential type")
                onComplete(false, "", "")
            }
        }
        else -> {
            Log.e("GoogleSignIn", "Unexpected credential type")
            onComplete(false, "", "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(
    initialTab: Int = 0,
    scrollToRecentUploads: Boolean = false,
    isPremium: Boolean = false,
    onPaywallClick: () -> Unit = {},
    onScanClick: () -> Unit,
    onFoodDatabaseClick: () -> Unit,
    onEditNameClick: (String) -> Unit = {},
    onReferFriendClick: () -> Unit = {},
    onPersonalDetailsClick: () -> Unit = {},
    onEditMacrosClick: () -> Unit = {},
    onWeightHistoryClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onHowToAddWidgetClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onFeatureRequestClick: () -> Unit = {},
    onLicensesClick: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showCameraMenu by remember { mutableStateOf(false) }
    
    // Handler for FAB click - checks premium status first
    val onFabClick: () -> Unit = {
        if (isPremium) {
            showCameraMenu = true
        } else {
            onPaywallClick()
        }
    }
    
    // Get window size class for adaptive layout
    val windowSizeClass = LocalWindowSizeClass.current
    val useNavigationRail = windowSizeClass.widthSizeClass != androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
    
    // Camera menu bottom sheet
    if (showCameraMenu) {
        ModalBottomSheet(
            onDismissRequest = { showCameraMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Food Database option
                CameraMenuItem(
                    icon = Icons.Filled.Search,
                    label = "Food Database",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showCameraMenu = false
                        onFoodDatabaseClick()
                    }
                )
                
                // Scan Food option
                CameraMenuItem(
                    icon = Icons.Filled.CameraAlt,
                    label = "Scan Food",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showCameraMenu = false
                        onScanClick()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Navigation items data
    val navigationItems = listOf(
        Triple(Icons.Filled.Home, "Home", 0),
        Triple(Icons.Filled.BarChart, "Progress", 1),
        Triple(Icons.Filled.Settings, "Settings", 2)
    )
    
    if (useNavigationRail) {
        // Medium/Expanded: Use Navigation Rail on the side
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // FAB at the top of rail
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Log Food")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                navigationItems.forEach { (icon, label, index) ->
                    NavigationRailItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            
            // Main content area
            Box(modifier = Modifier.fillMaxSize()) {
                MainTabsContent(
                    selectedTab = selectedTab,
                    scrollToRecentUploads = scrollToRecentUploads,
                    onEditNameClick = onEditNameClick,
                    onReferFriendClick = onReferFriendClick,
                    onPersonalDetailsClick = onPersonalDetailsClick,
                    onEditMacrosClick = onEditMacrosClick,
                    onWeightHistoryClick = onWeightHistoryClick,
                    onLanguageClick = onLanguageClick,
                    onHowToAddWidgetClick = onHowToAddWidgetClick,
                    onTermsClick = onTermsClick,
                    onPrivacyClick = onPrivacyClick,
                    onFeatureRequestClick = onFeatureRequestClick,
                    onLicensesClick = onLicensesClick,
                    onDeleteAccount = onDeleteAccount,
                    onLogout = onLogout
                )
            }
        }
    } else {
        // Compact: Use Bottom Navigation Bar with aligned FAB
        Scaffold(
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home
                        NavigationItemButton(
                            icon = Icons.Filled.Home,
                            label = "Home",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        
                        // Progress
                        NavigationItemButton(
                            icon = Icons.Filled.BarChart,
                            label = "Progress",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        
                        // Settings
                        NavigationItemButton(
                            icon = Icons.Filled.Settings,
                            label = "Settings",
                            isSelected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                        
                        // Camera FAB at the end
                        FloatingActionButton(
                            onClick = onFabClick,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add, 
                                contentDescription = "Log Food", 
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                MainTabsContent(
                    selectedTab = selectedTab,
                    scrollToRecentUploads = scrollToRecentUploads,
                    onEditNameClick = onEditNameClick,
                    onReferFriendClick = onReferFriendClick,
                    onPersonalDetailsClick = onPersonalDetailsClick,
                    onEditMacrosClick = onEditMacrosClick,
                    onWeightHistoryClick = onWeightHistoryClick,
                    onLanguageClick = onLanguageClick,
                    onHowToAddWidgetClick = onHowToAddWidgetClick,
                    onTermsClick = onTermsClick,
                    onPrivacyClick = onPrivacyClick,
                    onFeatureRequestClick = onFeatureRequestClick,
                    onLicensesClick = onLicensesClick,
                    onDeleteAccount = onDeleteAccount,
                    onLogout = onLogout
                )
            }
        }
    }
}

/**
 * Extracted content for reuse in both navigation layouts.
 */
@Composable
private fun MainTabsContent(
    selectedTab: Int,
    scrollToRecentUploads: Boolean = false,
    onEditNameClick: (String) -> Unit,
    onReferFriendClick: () -> Unit,
    onPersonalDetailsClick: () -> Unit,
    onEditMacrosClick: () -> Unit,
    onWeightHistoryClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onHowToAddWidgetClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onFeatureRequestClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    // Hoist scroll state for settings to preserve position when navigating back
    val settingsScrollState = androidx.compose.foundation.rememberScrollState()
    // Hoist lazy list state for dashboard to preserve scroll position
    val dashboardLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    when (selectedTab) {
        0 -> {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel, 
                lazyListState = dashboardLazyListState,
                scrollToRecentUploads = scrollToRecentUploads
            )
        }
        1 -> ProgressScreen()
        2 -> {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            val dashboardState by dashboardViewModel.dashboardState.collectAsState()
            SettingsScreen(
                onNavigateToEditName = onEditNameClick,
                onNavigateToReferFriend = onReferFriendClick,
                onNavigateToPersonalDetails = onPersonalDetailsClick,
                onNavigateToEditMacros = onEditMacrosClick,
                onNavigateToWeightHistory = onWeightHistoryClick,
                onNavigateToLanguage = onLanguageClick,
                onNavigateToHowToAddWidget = onHowToAddWidgetClick,
                onNavigateToTerms = onTermsClick,
                onNavigateToPrivacy = onPrivacyClick,
                onNavigateToFeatureRequest = onFeatureRequestClick,
                onNavigateToLicenses = onLicensesClick,
                onDeleteAccount = onDeleteAccount,
                onLogout = onLogout,
                remainingCalories = dashboardState.remainingCalories,
                proteinLeft = dashboardState.proteinG,
                carbsLeft = dashboardState.carbsG,
                fatsLeft = dashboardState.fatsG,
                streakDays = 0, // TODO: Add streak tracking
                scrollState = settingsScrollState
            )
        }
    }
}

@Composable
fun CameraMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Custom navigation item button for bottom bar with theme support.
 */
@Composable
private fun NavigationItemButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CalViewTheme {
        Greeting("Android")
    }
}
