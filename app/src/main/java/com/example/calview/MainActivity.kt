package com.example.calview

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
import com.example.calview.feature.dashboard.TermsAndConditionsScreen
import com.example.calview.feature.dashboard.PrivacyPolicyScreen
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                        authRepository = authRepository
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
    authRepository: AuthRepository? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for sign-in bottom sheet
    var showSignInSheet by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    
    // Determine start destination based on auth state
    val startDestination = if (isSignedIn) "main" else "onboarding"
    
    // Google Sign-In function
    fun signInWithGoogle() {
        scope.launch {
            isSigningIn = true
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Configure Google ID request
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("81536221642-8c2roff66j6aqpmoo705ifvtssu80min.apps.googleusercontent.com")
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
                        // Save Google profile info
                        scope.launch {
                            userPreferencesRepository?.setUserName(displayName)
                            userPreferencesRepository?.setPhotoUrl(photoUrl)
                            // Generate referral code if needed
                            val code = com.example.calview.core.data.repository.UserPreferencesRepositoryImpl.generateReferralCode()
                            userPreferencesRepository?.setReferralCode(code)
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
                navController.navigate("terms_and_conditions") 
            },
            onPrivacyClick = { 
                showSignInSheet = false
                navController.navigate("privacy_policy") 
            },
            isLoading = isSigningIn
        )
    }

    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingNavHost(
                onOnboardingComplete = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onSignIn = { showSignInSheet = true },
                onTermsClick = { navController.navigate("terms_and_conditions") },
                onPrivacyClick = { navController.navigate("privacy_policy") }
            )
        }
        
        composable(
            route = "main?tab={tab}",
            arguments = listOf(
                androidx.navigation.navArgument("tab") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            MainTabs(
                initialTab = initialTab,
                onScanClick = { navController.navigate("scanner") },
                onFoodDatabaseClick = { navController.navigate("logFood") },
                onEditNameClick = { name -> navController.navigate("edit_name/$name") },
                onReferFriendClick = { navController.navigate("refer_friend") },
                onPersonalDetailsClick = { navController.navigate("personal_details") },
                onEditMacrosClick = { navController.navigate("edit_nutrition_goals") },
                onWeightHistoryClick = { navController.navigate("weight_history") },
                onLanguageClick = { navController.navigate("language_selector") },
                onHowToAddWidgetClick = { navController.navigate("how_to_add_widget") },
                onTermsClick = { navController.navigate("terms_and_conditions") },
                onPrivacyClick = { navController.navigate("privacy_policy") },
                onFeatureRequestClick = { navController.navigate("feature_request") },
                onDeleteAccount = {
                    scope.launch {
                        authRepository?.deleteAccount()?.let { result ->
                            if (result.isSuccess) {
                                // Navigate to login screen after account deletion
                                navController.navigate("login") {
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
                                            .setServerClientId("908765412338-qakrfmcnnkkgqbq89dqvcqq08rr97gqv.apps.googleusercontent.com")
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
                                                            navController.navigate("login") {
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
        
        composable("terms_and_conditions") {
            TermsAndConditionsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("privacy_policy") {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("feature_request") {
            FeatureRequestScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("scanner") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                viewModel = scannerViewModel,
                onClose = { navController.popBackStack() }
            )
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
                onBack = { navController.popBackStack() }
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
                currentWeightLbs = uiState.currentWeight,
                onBack = { navController.popBackStack() },
                onSave = { heightCm, weightLbs ->
                    viewModel.updateHeight(heightCm)
                    viewModel.updateWeight(weightLbs)
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
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var showCameraMenu by remember { mutableStateOf(false) }
    
    // Get window size class for adaptive layout
    val windowSizeClass = LocalWindowSizeClass.current
    val useNavigationRail = windowSizeClass.widthSizeClass != androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact
    
    // Camera menu bottom sheet
    if (showCameraMenu) {
        ModalBottomSheet(
            onDismissRequest = { showCameraMenu = false },
            containerColor = Color.White,
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
                    onClick = { showCameraMenu = true },
                    containerColor = Color.Black,
                    contentColor = Color.White,
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
                    onDeleteAccount = onDeleteAccount,
                    onLogout = onLogout
                )
            }
        }
    } else {
        // Compact: Use Bottom Navigation Bar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    navigationItems.forEach { (icon, label, index) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.Black,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                    // Offset for FAB
                    Spacer(modifier = Modifier.width(72.dp))
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCameraMenu = true },
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .offset(y = 48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Log Food", modifier = Modifier.size(32.dp))
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                MainTabsContent(
                    selectedTab = selectedTab,
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
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    when (selectedTab) {
        0 -> {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(viewModel = dashboardViewModel)
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
                onDeleteAccount = onDeleteAccount,
                onLogout = onLogout,
                remainingCalories = dashboardState.remainingCalories,
                proteinLeft = dashboardState.proteinG,
                carbsLeft = dashboardState.carbsG,
                fatsLeft = dashboardState.fatsG,
                streakDays = 0 // TODO: Add streak tracking
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
        color = Color(0xFFF5F5F5)
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
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
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
