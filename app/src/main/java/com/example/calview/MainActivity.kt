package com.example.calview

import android.content.Context
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.calview.core.data.repository.AuthRepository
import com.example.calview.core.ui.theme.CalViewTheme
import com.example.calview.feature.dashboard.DashboardScreen
import com.example.calview.feature.dashboard.DashboardViewModel
import com.example.calview.feature.dashboard.EditNameScreen
import com.example.calview.feature.dashboard.ReferYourFriendScreen
import com.example.calview.feature.dashboard.SettingsScreen
import com.example.calview.feature.dashboard.SettingsViewModel
import com.example.calview.feature.dashboard.GroupsUnifiedProfileSetupScreen
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
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.Typography
import com.example.calview.feature.dashboard.NutritionGoals
import com.example.calview.feature.dashboard.FastingScreen
import com.example.calview.feature.dashboard.ChallengesScreen

import com.example.calview.feature.dashboard.SocialChallengesScreen
import com.example.calview.feature.dashboard.GroupsIntroScreen


import com.example.calview.feature.dashboard.GroupsProfileViewModel
import com.example.calview.feature.dashboard.AvatarGradients
import com.example.calview.feature.dashboard.GroupsSetupFlow
import com.example.calview.feature.dashboard.JoinGroupInvitationScreen
import com.example.calview.feature.dashboard.SocialChallengesViewModel
import com.example.calview.feature.dashboard.CreateGroupViewModel
import com.example.calview.feature.dashboard.CreateGroupNameScreen
import com.example.calview.feature.dashboard.CreateGroupPhotoScreen
import com.example.calview.feature.dashboard.GroupDashboardScreen
import com.example.calview.feature.dashboard.GroupSettingsScreen
import com.example.calview.feature.dashboard.LogExerciseScreen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject
import com.example.calview.feature.trends.WeightEntry as TrendWeightEntry
import com.example.calview.feature.dashboard.WeightEntry as DashboardWeightEntry
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.calview.core.ui.util.LocalWindowSizeClass
import java.time.Instant
import java.time.ZoneId
import java.util.Date


import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
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
    lateinit var exerciseRepository: com.example.calview.core.data.repository.ExerciseRepository
    
    @Inject
    lateinit var fastingRepository: com.example.calview.core.data.repository.FastingRepository

    @Inject
    lateinit var streakFreezeRepository: com.example.calview.core.data.repository.StreakFreezeRepository

    @Inject
    lateinit var weightHistoryRepository: com.example.calview.core.data.repository.WeightHistoryRepository

    @Inject
    lateinit var waterReminderRepository: com.example.calview.core.data.repository.WaterReminderRepository

    @Inject
    lateinit var socialChallengeRepository: com.example.calview.core.data.repository.SocialChallengeRepository

    @Inject
    lateinit var gamificationRepository: com.example.calview.core.data.repository.GamificationRepository

    @Inject
    lateinit var groupsRepository: com.example.calview.core.data.repository.GroupsRepository

    @Inject
    lateinit var billingManager: BillingManager
    
    // State for pending deep link invite code
    private val pendingInviteCode = mutableStateOf<String?>(null)
    
    // State for pending navigation from notification
    private val pendingNavigationTarget = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Handle deep link from intent
        handleIntent(intent)
        
        // Update widget when app opens to ensure it shows latest data
        com.example.calview.widget.CaloriesWidgetProvider.requestUpdate(this)
        
        // Set user email for billing manager (enables test account premium bypass)
        billingManager.setUserEmail(authRepository.getUserEmail().takeIf { it.isNotEmpty() })
        
        // Observe language preference and update app locale
        lifecycleScope.launch {
            userPreferencesRepository.language
                .distinctUntilChanged()
                .collect { languageCode ->
                    val currentLocales = AppCompatDelegate.getApplicationLocales()
                    if (currentLocales.toLanguageTags() != languageCode) {
                        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
        }
        
        setContent {
            // Calculate window size class for adaptive layouts
            val windowSizeClass = calculateWindowSizeClass(this)
            
            // Collect appearance mode and onboarding status from preferences
            // Use null as initial to distinguish "not loaded yet" from "loaded as false"
            val appearanceMode by userPreferencesRepository.appearanceMode.collectAsState(initial = "automatic")
            val isOnboardingCompleteNullable by userPreferencesRepository.isOnboardingComplete
                .map<Boolean, Boolean?> { it }
                .collectAsState(initial = null)
            val isOnboardingComplete = isOnboardingCompleteNullable ?: false
            val isPreferencesLoaded = isOnboardingCompleteNullable != null
            val currentUser by authRepository.authState.collectAsState(initial = authRepository.getCurrentUser())
            
            // Provide WindowSizeClass to the entire app
            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                CalViewTheme(appearanceMode = appearanceMode) {
                    AppNavigation(
                        isSignedIn = currentUser != null,
                        isOnboardingComplete = isOnboardingComplete,
                        isPreferencesLoaded = isPreferencesLoaded,
                        deepLinkInviteCode = pendingInviteCode, // Pass deep link state
                        onDeepLinkHandled = { clearPendingInviteCode() }, // Reset callback
                        navigationTarget = pendingNavigationTarget, // Pass navigation target
                        onNavigationHandled = { clearPendingNavigationTarget() },
                        userPreferencesRepository = userPreferencesRepository,
                        authRepository = authRepository,
                        mealRepository = mealRepository,
                        dailyLogRepository = dailyLogRepository,
                        firestoreRepository = firestoreRepository,
                        fastingRepository = fastingRepository,
                        streakFreezeRepository = streakFreezeRepository,
                        weightHistoryRepository = weightHistoryRepository,
                        waterReminderRepository = waterReminderRepository,
                        socialChallengeRepository = socialChallengeRepository,
                        gamificationRepository = gamificationRepository,
                        groupsRepository = groupsRepository,
                        exerciseRepository = exerciseRepository,
                        billingManager = billingManager
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        // Handle deep link
        intent.data?.let { uri ->
            if (uri.scheme == "calview" && uri.host == "join") {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    pendingInviteCode.value = code
                }
            }
        }

        // Handle notification target
        intent.getStringExtra("navigate_to")?.let { target ->
            pendingNavigationTarget.value = target
        }
    }

    fun clearPendingInviteCode() {
        pendingInviteCode.value = null
    }

    fun clearPendingNavigationTarget() {
        pendingNavigationTarget.value = null
    }
}

// Helper to find Activity from context
fun Context.findActivity(): android.app.Activity? {
    var context = this
    while (context is android.content.ContextWrapper) {
        if (context is android.app.Activity) return context
        context = context.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    isSignedIn: Boolean = false,
    isOnboardingComplete: Boolean = true,
    isPreferencesLoaded: Boolean = true,  // Whether preferences have been loaded from DataStore
    deepLinkInviteCode: State<String?> = mutableStateOf(null),
    onDeepLinkHandled: () -> Unit = {},
    navigationTarget: State<String?> = mutableStateOf(null),
    onNavigationHandled: () -> Unit = {},
    userPreferencesRepository: com.example.calview.core.data.repository.UserPreferencesRepository? = null,
    authRepository: AuthRepository? = null,
    mealRepository: com.example.calview.core.data.repository.MealRepository? = null,
    dailyLogRepository: com.example.calview.core.data.repository.DailyLogRepository? = null,
    firestoreRepository: com.example.calview.core.data.repository.FirestoreRepository? = null,
    fastingRepository: com.example.calview.core.data.repository.FastingRepository? = null,
    streakFreezeRepository: com.example.calview.core.data.repository.StreakFreezeRepository? = null,
    weightHistoryRepository: com.example.calview.core.data.repository.WeightHistoryRepository? = null,
    waterReminderRepository: com.example.calview.core.data.repository.WaterReminderRepository? = null,
    socialChallengeRepository: com.example.calview.core.data.repository.SocialChallengeRepository? = null,
    gamificationRepository: com.example.calview.core.data.repository.GamificationRepository? = null,
    groupsRepository: com.example.calview.core.data.repository.GroupsRepository? = null,
    exerciseRepository: com.example.calview.core.data.repository.ExerciseRepository? = null,
    billingManager: BillingManager? = null
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State for sign-in bottom sheet
    var showSignInSheet by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isRedirecting by remember { mutableStateOf(false) }
    
    // Silent Restore for returning users (e.g. re-install without manual sign-in)
    LaunchedEffect(isSignedIn) {
        if (isSignedIn && mealRepository != null) {
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Check if we have any data. using meals as a proxy for "clean install"
                    val hasMeals = mealRepository.hasAnyMeals()
                    if (!hasMeals) {
                        Log.d("SilentRestore", "User is signed in but local DB is empty. Attempting detailed restore...")
                        
                        // Restore preferences first (contains levels, xp, goals)
                        userPreferencesRepository?.restoreFromCloud()
                        
                        // Restore content
                        val mealsRestored = mealRepository.restoreFromCloud()
                        if (mealsRestored) {
                            Log.d("SilentRestore", "Restored meals from cloud.")
                        }
                        
                        // Restore other entities
                        exerciseRepository?.restoreFromCloud()
                        weightHistoryRepository?.restoreFromCloud()
                        dailyLogRepository?.restoreFromCloud()
                        streakFreezeRepository?.restoreFromCloud()
                        gamificationRepository?.restoreFromCloud()
                        fastingRepository?.restoreFromCloud()
                        waterReminderRepository?.restoreFromCloud()
                        groupsRepository?.restoreUserGroups()
                        
                        Log.d("SilentRestore", "Silent restore sequence completed.")
                    }
                } catch (e: Exception) {
                    Log.e("SilentRestore", "Error during silent restore", e)
                }
            }
        }
    }

    // Global navigation redirect based on state
    // Note: Only redirect from "onboarding", NOT from "splash"
    // This ensures splash screen always shows for the full duration on cold start
    LaunchedEffect(isSignedIn, isOnboardingComplete) {
        Log.d("Navigation", "State changed: isSignedIn=$isSignedIn, isOnboardingComplete=$isOnboardingComplete")
        if (isSignedIn && isOnboardingComplete) {
            val currentRoute = navController.currentDestination?.route
            Log.d("Navigation", "Current route before global redirect: $currentRoute")
            if (currentRoute == "onboarding") {
                Log.d("Navigation", "Triggering global redirect to main from onboarding")
                showSignInSheet = false
                isRedirecting = false
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Handle deep link navigation
    val deepLinkCode = deepLinkInviteCode.value
    LaunchedEffect(deepLinkCode, isSignedIn, isOnboardingComplete) {
        if (deepLinkCode != null && isSignedIn && isOnboardingComplete) {
            Log.d("DeepLink", "Navigating to join invitation with code: $deepLinkCode")
            navController.navigate("groups_join_invitation")
            // Pass the code via savedStateHandle or a custom route if needed
            // For now, we'll assume JoinGroupInvitationScreen can pick it up or we update the route
            // Let's use a route with parameter for simplicity if possible
            // But since JoinGroupInvitationScreen is already built, let's see if we can pass it there.
            
            // Actually, let's update JoinGroupInvitationScreen to accept an optional code
            // Or just set it in the savedStateHandle
            navController.currentBackStackEntry?.savedStateHandle?.set("inviteCode", deepLinkCode)
            
            // Reset the pending code so we don't navigate again
            onDeepLinkHandled()
        }
    }

    // Handle notification navigation target
    val target = navigationTarget.value
    LaunchedEffect(target, isSignedIn, isOnboardingComplete) {
        if (target == "groups" && isSignedIn && isOnboardingComplete) {
            Log.d("Navigation", "Navigating to Groups tab from notification")
            navController.navigate("main?tab=2") {
                popUpTo("main") { inclusive = true }
            }
            onNavigationHandled()
        }
    }
    
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
                
                val activity = context.findActivity()
                if (activity == null) {
                    Log.e("SignIn", "Could not find activity from context")
                    isSigningIn = false
                    return@launch
                }
                
                Log.d("SignIn", "Calling getCredential...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )
                Log.d("SignIn", "getCredential result received: $result")
                
                handleSignInResult(result) { success, displayName, photoUrl, email ->
                    Log.d("SignIn", "Result handled: success=$success, email=$email")
                    isSigningIn = false
                    if (success) {
                        isRedirecting = true
                        Log.d("SignIn", "Google Sign-In successful for $displayName")
                        // Crucial: Update BillingManager with email and check for existing purchases
                        billingManager?.setUserEmail(email)
                        billingManager?.queryPurchases()
                        
                        // Save Google profile info and restore data from cloud
                        scope.launch {
                            // Check if user already completed onboarding locally
                            val localOnboardingComplete = userPreferencesRepository?.isOnboardingComplete?.first() ?: false
                            
                            // Restore from cloud
                            val restored = userPreferencesRepository?.restoreFromCloud() ?: false
                            // Also restore other entities
                            mealRepository?.restoreFromCloud()
                            dailyLogRepository?.restoreFromCloud()
                            streakFreezeRepository?.restoreFromCloud()
                            fastingRepository?.restoreFromCloud()
                            waterReminderRepository?.restoreFromCloud()
                            gamificationRepository?.restoreFromCloud()
                            exerciseRepository?.restoreFromCloud()
                            groupsRepository?.restoreUserGroups()
                            
                            // If restored, were we complete?
                            val cloudOnboardingComplete = userPreferencesRepository?.isOnboardingComplete?.first() ?: false
                            
                            // Check if user has any actual data (meals) - indicates returning user
                            val hasMeals = mealRepository?.hasAnyMeals() ?: false
                            Log.d("SignIn", "restored=$restored, cloudOnboardingComplete=$cloudOnboardingComplete, hasMeals=$hasMeals, localOnboardingComplete=$localOnboardingComplete")
                            
                            if (restored && cloudOnboardingComplete) {
                                Log.d("SignIn", "Old user restored with completed onboarding. Navigating to main.")
                                showSignInSheet = false
                                isRedirecting = false
                                
                                // HIGHER IMPORTANCE: Suppress tours for returning users
                                userPreferencesRepository?.setHasSeenDashboardWalkthrough(true)
                                userPreferencesRepository?.setHasSeenProgressWalkthrough(true)
                                
                                navController.navigate("main") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else if (restored && hasMeals) {
                                // User has data but isOnboardingComplete was false - fix the flag and navigate
                                Log.d("SignIn", "Returning user with data but incomplete flag. Fixing and navigating to main.")
                                userPreferencesRepository?.setOnboardingComplete(true)
                                userPreferencesRepository?.syncToCloud()
                                
                                showSignInSheet = false
                                isRedirecting = false
                                
                                // Suppress tours for returning users
                                userPreferencesRepository?.setHasSeenDashboardWalkthrough(true)
                                userPreferencesRepository?.setHasSeenProgressWalkthrough(true)
                                
                                navController.navigate("main") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else if (localOnboardingComplete) {
                                Log.d("SignIn", "User completed onboarding locally. Syncing and navigating to main.")
                                userPreferencesRepository?.syncToCloud()
                                userPreferencesRepository?.setUserName(displayName)
                                userPreferencesRepository?.setPhotoUrl(photoUrl)
                                
                                showSignInSheet = false
                                isRedirecting = false
                                navController.navigate("main") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                Log.d("SignIn", "New user detected. Redirecting to profile_setup via state change.")
                                userPreferencesRepository?.setUserName(displayName)
                                userPreferencesRepository?.setPhotoUrl(photoUrl)
                                
                                if (userPreferencesRepository?.referralCode?.first().isNullOrEmpty()) {
                                    val code = com.example.calview.core.data.repository.UserPreferencesRepositoryImpl.generateReferralCode()
                                    userPreferencesRepository?.setReferralCode(code)
                                }
                                
                                showSignInSheet = false
                                isRedirecting = false
                                // Navigation to profile_setup will now happen in OnboardingNavHost
                                // because isSignedIn will be true and isOnboardingComplete will be false.
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e("GoogleSignIn", "Sign-in failed with fatal error", e)
                isSigningIn = false
            }
        }
    }
    
    // Sign-in bottom sheet
    if (showSignInSheet) {
        SignInBottomSheet(
            onDismiss = { 
                showSignInSheet = false
                isSigningIn = false
                isRedirecting = false
            },
            onGoogleSignIn = { signInWithGoogle() },
            onTermsClick = { 
                showSignInSheet = false
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/terms_of_service.html")))
            },
            onPrivacyClick = { 
                showSignInSheet = false
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://isaacmuigai-dev.github.io/CalView/privacy_policy.html")))
            },
            isLoading = isSigningIn || isRedirecting,
            loadingText = if (isRedirecting) "Redirecting..." else null
        )
    }

    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash screen - always shows first, then redirects based on auth state
        // CRITICAL: Wait for preferences to load before navigating to avoid
        // incorrectly sending logged-in users to onboarding
        composable("splash") {
            // Track if we've already navigated to prevent multiple navigations
            var hasNavigated by remember { mutableStateOf(false) }
            
            // Effect to handle navigation once preferences are loaded
            LaunchedEffect(isPreferencesLoaded, isSignedIn, isOnboardingComplete) {
                if (isPreferencesLoaded && !hasNavigated) {
                    // Small delay to ensure splash is visible for minimum time
                    kotlinx.coroutines.delay(500)
                    hasNavigated = true
                    val destination = if (isSignedIn && isOnboardingComplete) "main" else "onboarding"
                    Log.d("Navigation", "Splash: preferences loaded, navigating to $destination (isSignedIn=$isSignedIn, isOnboardingComplete=$isOnboardingComplete)")
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            
            com.example.calview.feature.onboarding.SplashScreen(
                onTimeout = {
                    // Only use timeout navigation if preferences aren't loaded yet
                    // This serves as a fallback and maximum wait time
                    if (!hasNavigated) {
                        hasNavigated = true
                        val destination = if (isSignedIn && isOnboardingComplete) "main" else "onboarding"
                        Log.d("Navigation", "Splash timeout: destination=$destination (isSignedIn=$isSignedIn, isOnboardingComplete=$isOnboardingComplete, prefsLoaded=$isPreferencesLoaded)")
                        navController.navigate(destination) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("onboarding") {
            OnboardingNavHost(
                isSignedIn = isSignedIn,
                isSigningIn = isSigningIn,
                isRedirecting = isRedirecting,
                isOnboardingComplete = isOnboardingComplete,
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
            route = "main?tab={tab}&scrollToUploads={scrollToUploads}&showScanMenu={showScanMenu}&scrollToMealId={scrollToMealId}",
            arguments = listOf(
                androidx.navigation.navArgument("tab") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = 0
                },
                androidx.navigation.navArgument("scrollToUploads") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                },
                androidx.navigation.navArgument("showScanMenu") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                },
                androidx.navigation.navArgument("scrollToMealId") {
                    type = androidx.navigation.NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            val scrollToUploads = backStackEntry.arguments?.getBoolean("scrollToUploads") ?: false
            val showScanMenuArg = backStackEntry.arguments?.getBoolean("showScanMenu") ?: false
            val showScanMenuBackstack by backStackEntry.savedStateHandle.getLiveData<Boolean>("showScanMenu").observeAsState(false)
            val scrollToMealId = backStackEntry.arguments?.getLong("scrollToMealId") ?: -1L
            val isPremium by (billingManager?.isPremium ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
            
            // Reset the backstack flag once handled to prevent re-triggering on tab switch
            LaunchedEffect(showScanMenuBackstack) {
                if (showScanMenuBackstack) {
                    backStackEntry.savedStateHandle["showScanMenu"] = false
                }
            }
            
            MainTabs(
                navController = navController,
                userPreferencesRepository = userPreferencesRepository,
                initialTab = initialTab,
                scrollToRecentUploads = scrollToUploads,
                scrollToMealId = scrollToMealId,
                showScanMenuOnStart = showScanMenuArg || showScanMenuBackstack,
                onScanMenuConsumed = {
                    if (showScanMenuBackstack) {
                        backStackEntry.savedStateHandle["showScanMenu"] = false
                    }
                },
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
                onFastingClick = { navController.navigate("fasting") },
                onChallengesClick = { navController.navigate("challenges") },
                onAchievementsClick = { navController.navigate("achievements") },
                onLogExerciseClick = { navController.navigate("log_exercise") },
                onGroupsClick = { navController.navigate("groups_intro") },
                onDeleteAccount = {
                    // Navigation is now triggered by SettingsViewModel's isDeletionComplete flag
                    // but we still provide this for cleanup/safety
                    navController.navigate("splash") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate("splash") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable("how_to_add_widget") {
            HowToAddWidgetScreen(
                onBack = { 
                    navController.popBackStack()
                }
            )
        }
        

        
        composable("feature_request") {
            val feedbackHubViewModel: com.example.calview.feature.dashboard.FeedbackHubViewModel = hiltViewModel()
            FeatureRequestScreen(
                onBack = { 
                    navController.popBackStack()
                },
                viewModel = feedbackHubViewModel
            )
        }
        
        composable("open_source_licenses") {
            OpenSourceLicensesScreen(
                onBack = { 
                    navController.popBackStack()
                }
            )
        }
        
        composable("scanner") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            val isPremium by (billingManager?.isPremium ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
            
            ScannerScreen(
                viewModel = scannerViewModel,
                isPremium = isPremium,
                onClose = { navController.popBackStack() },
                onNavigateToDashboard = { mealId ->
                    // Navigate to dashboard (Home tab) after food capture
                    // Pass mealId to trigger auto-scroll
                    val route = if (mealId != null) "main?tab=0&scrollToMealId=$mealId" else "main?tab=0"
                    navController.navigate(route) {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onUpgradeClick = { navController.navigate("paywall") }
            )
        }
        
        composable("paywall") {
            billingManager?.let { manager ->
                PaywallScreen(
                    billingManager = manager,
                    onClose = {
                        // Navigate to Dashboard instead of popping back to Settings
                        navController.navigate("main?tab=0") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    onSubscriptionSuccess = {
                        // Navigate to Dashboard and show scanning menu
                        navController.navigate("main?tab=0&showScanMenu=true") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable("logFood") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            val meals by scannerViewModel.getAllMealsFlow().collectAsState(initial = emptyList())
            
            // Get goals from repository
            val currentCalories by userPreferencesRepository?.recommendedCalories?.collectAsState(initial = 2000)
                ?: remember { mutableIntStateOf(2000) }
            val currentProtein by userPreferencesRepository?.recommendedProtein?.collectAsState(initial = 125)
                ?: remember { mutableIntStateOf(125) }
            
            MyMealsScreen(
                meals = meals,
                onBack = { 
                    navController.previousBackStackEntry?.savedStateHandle?.set("showScanMenu", true)
                    navController.popBackStack("main", false)
                },
                onScanFood = { navController.navigate("scanner") },
                onMealClick = { meal ->
                    navController.navigate("meal_detail/${meal.id}")
                },
                onCreateMeal = { name, calories, protein, carbs, fats ->
                    scannerViewModel.createCustomMeal(name, calories, protein, carbs, fats)
                },
                onDeleteMeal = { mealId ->
                    scannerViewModel.deleteMeal(mealId)
                },
                calorieGoal = currentCalories,
                proteinGoal = currentProtein
            )
        }
        
        composable(
            route = "meal_detail/{mealId}",
            arguments = listOf(
                androidx.navigation.navArgument("mealId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val mealId = backStackEntry.arguments?.getLong("mealId") ?: -1L
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            val meal by scannerViewModel.getMealById(mealId).collectAsState(initial = null)
            
            meal?.let { m ->
                com.example.calview.feature.dashboard.FoodDetailScreen(
                    meal = m,
                    onBack = { navController.popBackStack() },
                    onDelete = { mealToDelete ->
                        scannerViewModel.deleteMeal(mealToDelete.id)
                        navController.popBackStack()
                    },
                    onUpdate = { updatedMeal ->
                        scannerViewModel.updateMeal(updatedMeal)
                    }
                )
            }
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
                    navController.popBackStack()
                }
            )
        }
        
        composable("personal_details") {
            PersonalDetailsScreen(
                onBack = { 
                    navController.popBackStack()
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
                    navController.popBackStack()
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
                    navController.popBackStack()
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
                onComplete = { goals, data ->
                    // Save generated goals to repository
                    scope.launch {
                        userPreferencesRepository?.saveRecommendedMacros(
                            calories = goals.calories,
                            protein = goals.protein,
                            carbs = goals.carbs,
                            fats = goals.fats
                        )
                        // Save underlying parameters for Goal Journey card
                        userPreferencesRepository?.setGoalWeight(data.desiredWeightKg)
                        userPreferencesRepository?.saveUserProfile(
                            goal = data.goal,
                            gender = data.gender,
                            age = data.age,
                            weight = data.weightKg,
                            height = data.heightCm
                        )
                        userPreferencesRepository?.setWeightChangePerWeek(data.weightChangePerWeek)
                    }
                    // Navigate back past auto_generate_goals to edit_nutrition_goals
                    navController.popBackStack()
                }
            )
        }
        
        composable("weight_history") {
            val weightHistoryEntities by weightHistoryRepository?.getAllWeightHistory()?.collectAsState(initial = emptyList())
                ?: remember { mutableStateOf(emptyList()) }
            val weightHistory = remember(weightHistoryEntities) {
                weightHistoryEntities.map { entity ->
                    TrendWeightEntry(
                        date = java.time.Instant.ofEpochMilli(entity.timestamp)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate(),
                        weight = entity.weight
                    ).let { trendEntry ->
                        // Map to the dashboard version of WeightEntry which uses java.util.Date
                        DashboardWeightEntry(
                            weight = trendEntry.weight,
                            date = java.util.Date.from(trendEntry.date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                        )
                    }
                }
            }
            
            WeightHistoryScreen(
                weightHistory = weightHistory,
                onBack = { 
                    navController.popBackStack()
                }
            )
        }
        
        composable("language_selector") {
            LanguageSelectorScreen(
                currentLanguage = "en",
                onBack = { 
                    navController.popBackStack()
                },
                onLanguageSelected = { languageCode ->
                    // Save language preference
                    scope.launch {
                        userPreferencesRepository?.let { repo ->
                            repo.setLanguage(languageCode)
                        }
                    }
                    navController.popBackStack()
                }
            )
        }
        
        composable("challenges") {
            val isPremium by (billingManager?.isPremium ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
            val viewModel: SocialChallengesViewModel = hiltViewModel()
            SocialChallengesScreen(
                isPremium = isPremium,
                onNavigateBack = { 
                    navController.previousBackStackEntry?.savedStateHandle?.set("showScanMenu", true)
                    navController.popBackStack("main", false)
                },
                onUpgradeClick = { navController.navigate("paywall") },
                viewModel = viewModel
            )
        }
        


        composable("achievements") {
            ChallengesScreen(
                onNavigateBack = { 
                    navController.previousBackStackEntry?.savedStateHandle?.set("showScanMenu", true)
                    navController.popBackStack("main", false)
                }
            )
        }
        
        composable("fasting") {
            FastingScreen(
                onNavigateBack = { 
                    navController.previousBackStackEntry?.savedStateHandle?.set("showScanMenu", true)
                    navController.popBackStack("main", false)
                }
            )
        }
        
        composable("log_exercise") {
            LogExerciseScreen(
                onNavigateBack = { 
                    navController.previousBackStackEntry?.savedStateHandle?.set("showScanMenu", true)
                    navController.popBackStack("main", false)
                }
            )
        }

        // Groups Onboarding
        composable("groups_intro") {
            GroupsIntroScreen(
                onBack = { navController.popBackStack() },
                onCreateGroupClick = { navController.navigate("groups_creation_flow?flow=CREATE") },
                onJoinGroupClick = { navController.navigate("groups_creation_flow?flow=JOIN") }
            )
        }

        navigation(
            startDestination = "groups_unified_setup",
            route = "groups_creation_flow?flow={flow}",
            arguments = listOf(androidx.navigation.navArgument("flow") { defaultValue = "CREATE" })
        ) {
            composable("groups_unified_setup") { backStackEntry ->
                // Scope ViewModel to the navigation graph
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("groups_creation_flow?flow=${backStackEntry.arguments?.getString("flow")}")
                }
                val flowValue = parentEntry.arguments?.getString("flow") ?: "CREATE"
                val viewModel: GroupsProfileViewModel = hiltViewModel(parentEntry)
                
                LaunchedEffect(flowValue) {
                    viewModel.setSetupFlow(if (flowValue == "JOIN") GroupsSetupFlow.JOIN else GroupsSetupFlow.CREATE)
                }

                GroupsUnifiedProfileSetupScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onProfileCreated = {
                        // Navigate based on flow
                        if (flowValue == "JOIN") {
                            navController.navigate("groups_join_invitation") {
                                popUpTo("groups_creation_flow?flow=$flowValue") { inclusive = true }
                            }
                        } else {
                            navController.navigate("group_creation_process") {
                                popUpTo("groups_creation_flow?flow=$flowValue") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        composable("groups_join_invitation") {
            JoinGroupInvitationScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate("main?tab=2") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        navigation(
            startDestination = "create_group_name",
            route = "group_creation_process"
        ) {
            composable("create_group_name") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("group_creation_process")
                }
                val viewModel: CreateGroupViewModel = hiltViewModel(parentEntry)
                CreateGroupNameScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate("create_group_photo") }
                )
            }

            composable("create_group_photo") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("group_creation_process")
                }
                val viewModel: CreateGroupViewModel = hiltViewModel(parentEntry)
                val scope = rememberCoroutineScope()
                CreateGroupPhotoScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreateGroup = {
                        viewModel.createGroup {
                            scope.launch {
                                userPreferencesRepository?.setGroupCreated(true)
                                navController.navigate("main?tab=2") {
                                    popUpTo("main") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            }
        }

        composable("group_dashboard") {
            GroupDashboardScreen(
                onGroupSettingsClick = { navController.navigate("group_settings") },
                onJoinGroupClick = { navController.navigate("groups_join_invitation") },
                onCreateGroupClick = { navController.navigate("group_creation_process") }
            )
        }

        composable("group_settings") {
            GroupSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Handle the credential result and authenticate with Firebase.
 */
private fun handleSignInResult(
    result: GetCredentialResponse,
    onComplete: (success: Boolean, displayName: String, photoUrl: String, email: String?) -> Unit
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
                                val email = googleIdTokenCredential.id
                                onComplete(true, displayName, photoUrl, email)
                            } else {
                                onComplete(false, "", "", null)
                            }
                        }
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("GoogleSignIn", "Invalid Google ID Token", e)
                    onComplete(false, "", "", null)
                }
            } else {
                Log.e("GoogleSignIn", "Unexpected credential type")
                onComplete(false, "", "", null)
            }
        }
        else -> {
            Log.e("GoogleSignIn", "Unexpected credential type")
            onComplete(false, "", "", null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(
    navController: androidx.navigation.NavHostController,
    userPreferencesRepository: com.example.calview.core.data.repository.UserPreferencesRepository? = null,
    initialTab: Int = 0,
    scrollToRecentUploads: Boolean = false,
    scrollToMealId: Long = -1L,
    showScanMenuOnStart: Boolean = false,
    isPremium: Boolean = false,
    onPaywallClick: () -> Unit = {},
    onScanClick: () -> Unit,
    onFoodDatabaseClick: () -> Unit,
    onScanMenuConsumed: () -> Unit = {},
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

    onFastingClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onLogExerciseClick: () -> Unit = {},
    onGroupsClick: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var showCameraMenu by remember { mutableStateOf(showScanMenuOnStart) }
    
    // Automatically show menu when signaled from navigation
    LaunchedEffect(showScanMenuOnStart) {
        if (showScanMenuOnStart) {
            showCameraMenu = true
            onScanMenuConsumed()
        }
    }
    
    // State to track if we should auto-scroll to uploads (reset after handled)
    var shouldScrollToUploads by rememberSaveable(scrollToRecentUploads) { mutableStateOf(scrollToRecentUploads) }
    var currentScrollToMealId by rememberSaveable(scrollToMealId) { mutableLongStateOf(scrollToMealId) }
    
    // Hoist scroll states at MainTabs level to survive navigation
    val settingsScrollState = androidx.compose.foundation.rememberScrollState()
    val dashboardLazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()
    
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
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Row 1: Food & Scan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionMenuItem(
                        icon = Icons.Rounded.Search,
                        label = "Database",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCameraMenu = false
                            onFoodDatabaseClick()
                        }
                    )
                    ActionMenuItem(
                        icon = Icons.Rounded.CameraAlt,
                        label = "Scan Food",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCameraMenu = false
                            onScanClick()
                        }
                    )
                }
                
                // Row 2: Fasting & Challenges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionMenuItem(
                        icon = Icons.Rounded.Timer,
                        label = "Fasting",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCameraMenu = false
                            onFastingClick()
                        }
                    )
                    ActionMenuItem(
                        icon = Icons.Rounded.EmojiEvents,
                        label = "Challenges",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCameraMenu = false
                            onChallengesClick()
                        }
                    )
                }

                // Row 3: Achievements & Exercises
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionMenuItem(
                        icon = Icons.Rounded.Star,
                        label = "Badges",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCameraMenu = false
                            onAchievementsClick()
                        }
                    )
                    ActionMenuItem(
                        icon = Icons.Rounded.FitnessCenter,
                        label = "Exercise",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showCameraMenu = false
                            onLogExerciseClick()
                        }
                    )
                }
            }
        }
    }
    
    // Navigation items data
    val navigationItems = listOf(
        Triple(Icons.Filled.Home, "Home", 0),
        Triple(Icons.Filled.BarChart, "Progress", 1),
        Triple(Icons.Filled.Groups, "Groups", 2),
        Triple(Icons.Filled.Person, "Profile", 3)
    )

    // Collect profile data for the Profile icon
    val userName by (userPreferencesRepository?.userName?.collectAsState(initial = "") ?: remember { mutableStateOf("") })
    val groupsFirstName by (userPreferencesRepository?.groupsFirstName?.collectAsState(initial = "") ?: remember { mutableStateOf("") })
    val groupsLastName by (userPreferencesRepository?.groupsLastName?.collectAsState(initial = "") ?: remember { mutableStateOf("") })
    
    val profileInitials = remember(userName, groupsFirstName, groupsLastName) {
        if (groupsFirstName.isNotEmpty() || groupsLastName.isNotEmpty()) {
            (groupsFirstName.take(1) + groupsLastName.take(1)).uppercase()
        } else if (userName.isNotEmpty()) {
            userName.split(" ").filter { it.isNotEmpty() }.let {
                if (it.size >= 2) (it[0].take(1) + it[1].take(1)).uppercase()
                else it[0].take(1).uppercase()
            }
        } else {
            "U"
        }
    }

    val isGroupsComplete by (userPreferencesRepository?.isGroupsProfileComplete?.collectAsState(initial = false) ?: remember { mutableStateOf(false) })

    if (useNavigationRail) {
        // Medium/Expanded: Use Navigation Rail on the side
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Handle edge-to-edge for status bar
                .navigationBarsPadding() // Handle edge-to-edge for navigation bar
        ) {
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
                        onClick = { 
                            if (selectedTab == index && index == 0) {
                                scope.launch {
                                    dashboardLazyListState.animateScrollToItem(0)
                                }
                            }
                            selectedTab = index 
                        },
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
                    scrollToRecentUploads = shouldScrollToUploads,
                    scrollToMealId = currentScrollToMealId,
                    onScrollHandled = { 
                        shouldScrollToUploads = false 
                        currentScrollToMealId = -1L
                    },
                    userPreferencesRepository = userPreferencesRepository,
                    navController = navController,
                    settingsScrollState = settingsScrollState,
                    dashboardLazyListState = dashboardLazyListState,
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
                    onFastingClick = onFastingClick,
                    onChallengesClick = onChallengesClick,
                    onGroupsClick = onGroupsClick,
                    onGroupSettingsClick = { navController.navigate("group_settings") },
                    onJoinGroupClick = { navController.navigate("groups_creation_flow?flow=JOIN") },
                    onCreateGroupClick = { navController.navigate("groups_creation_flow?flow=CREATE") },
                    onDeleteAccount = onDeleteAccount,
                    onLogout = onLogout
                )
            }
        }
    } else {
        // Compact: Use Bottom Navigation Bar with aligned FAB
        // Use Box instead of Scaffold to allow content to extend behind the floating nav bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content - extends full screen behind the nav bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                MainTabsContent(
                    selectedTab = selectedTab,
                    scrollToRecentUploads = shouldScrollToUploads,
                    scrollToMealId = currentScrollToMealId,
                    onScrollHandled = { 
                        shouldScrollToUploads = false 
                        currentScrollToMealId = -1L
                    },
                    userPreferencesRepository = userPreferencesRepository,
                    navController = navController,
                    settingsScrollState = settingsScrollState,
                    dashboardLazyListState = dashboardLazyListState,
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
                    onFastingClick = onFastingClick,
                    onChallengesClick = onChallengesClick,
                    onGroupsClick = onGroupsClick,
                    onGroupSettingsClick = { navController.navigate("group_settings") },
                    onJoinGroupClick = { navController.navigate("groups_creation_flow?flow=JOIN") },
                    onCreateGroupClick = { navController.navigate("groups_creation_flow?flow=CREATE") },
                    onDeleteAccount = onDeleteAccount,
                    onLogout = onLogout
                )
            }
            
            // Floating Navigation Bar - positioned at bottom
            // Floating Navigation Bar - positioned at bottom
            val infiniteTransition = rememberInfiniteTransition(label = "navEdgeLighting")
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "lightingPhase"
            )

            val isDark = isSystemInDarkTheme()
            val lightingColors = if (isDark) {
                listOf(Color(0xFFBB86FC), Color(0xFF03DAC6), Color(0xFF6200EE), Color(0xFFBB86FC))
            } else {
                // Dark colors for light theme as requested
                listOf(Color(0xFF2C3E50), Color(0xFF000000), Color(0xFF34495E), Color(0xFF2C3E50))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
                    .navigationBarsPadding()
                    .height(76.dp) // Slightly taller to accommodate border
                    .clip(RoundedCornerShape(32.dp)) // Clip the entire box to the shape
            ) {
                // 1. Rotating Gradient Background (The Edge Lighting)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            rotate(phase) {
                                drawCircle(
                                    brush = Brush.sweepGradient(lightingColors),
                                    radius = size.maxDimension,
                                    center = center
                                )
                            }
                        }
                )

                // 2. Solid White Content (The actual bar)
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp), // 2dp padding reveals the rotating gradient behind
                    color = if (isDark) Color.Black else Color.White,
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(30.dp) // Slightly smaller radius for inner content
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home
                        NavigationItemButton(
                            icon = Icons.Filled.Home,
                            label = "Home",
                            isSelected = selectedTab == 0,
                            onClick = { 
                                if (selectedTab == 0) {
                                    scope.launch {
                                        dashboardLazyListState.animateScrollToItem(0)
                                    }
                                } else {
                                    selectedTab = 0 
                                }
                            }
                        )
                        
                        // Progress
                        NavigationItemButton(
                            icon = Icons.Filled.BarChart,
                            label = "Progress",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        
                        // Groups
                        NavigationItemButton(
                            icon = Icons.Filled.Groups,
                            label = "Groups",
                            isSelected = selectedTab == 2,
                            onClick = {
                                // Always switch to Groups tab - the tab content handles both states
                                selectedTab = 2
                            }
                        )

                        // Profile (with Initials)
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTab = 3 }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedTab == 3) 
                                            AvatarGradients[0] // Use first gradient for selected profile tab
                                        else 
                                            Brush.linearGradient(listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profileInitials,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 3) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Profile",
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTab == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        // Action FAB (Quick Actions)
                        Surface(
                            onClick = onFabClick,
                            color = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Add, 
                                    contentDescription = "Quick Actions", 
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
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
    scrollToMealId: Long = -1L,
    onScrollHandled: () -> Unit = {},
    userPreferencesRepository: com.example.calview.core.data.repository.UserPreferencesRepository? = null,
    navController: androidx.navigation.NavHostController,
    settingsScrollState: androidx.compose.foundation.ScrollState,
    dashboardLazyListState: androidx.compose.foundation.lazy.LazyListState,
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
    onFastingClick: () -> Unit,
    onChallengesClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onGroupSettingsClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    when (selectedTab) {
        0 -> {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel, 
                lazyListState = dashboardLazyListState,
                scrollToRecentUploads = scrollToRecentUploads,
                scrollToMealId = scrollToMealId,
                onScrollHandled = onScrollHandled,
                onFastingClick = onFastingClick,
                onChallengesClick = onChallengesClick
            )
        }
        1 -> ProgressScreen()
        2 -> {
            val isGroupCreated by (userPreferencesRepository?.isGroupCreated?.map<Boolean, Boolean?> { it }?.collectAsState(initial = null) ?: remember { mutableStateOf(null) })
            val isGroupsProfileComplete by (userPreferencesRepository?.isGroupsProfileComplete?.map<Boolean, Boolean?> { it }?.collectAsState(initial = null) ?: remember { mutableStateOf(null) })
            
            if (isGroupCreated == null || isGroupsProfileComplete == null) {
                // Loading state - show nothing or a subtle spinner to prevent flashing wrong screen
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            } else if (isGroupCreated == true) {
                // User has created/joined a group - show the dashboard
                GroupDashboardScreen(
                    onGroupSettingsClick = onGroupSettingsClick,
                    onJoinGroupClick = onJoinGroupClick,
                    onCreateGroupClick = onCreateGroupClick
                )
            } else {
                // User hasn't set up groups - show intro screen
                GroupsIntroScreen(
                    onBack = { },
                    onCreateGroupClick = {
                        if (isGroupsProfileComplete == true) {
                            onCreateGroupClick() 
                        } else {
                            navController.navigate("groups_creation_flow?flow=CREATE")
                        }
                    },
                    onJoinGroupClick = {
                        if (isGroupsProfileComplete == true) {
                            onJoinGroupClick()
                        } else {
                            navController.navigate("groups_creation_flow?flow=JOIN")
                        }
                    }
                )
            }
        }
        3 -> {
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
private fun ActionMenuItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7) // iOS system grouped background
    val contentColor = if (isDark) Color.White else Color.Black
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "pressScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Custom navigation item button for bottom bar with theme support.
 * Only icon and text color change when selected - no background box.
 */
@Composable
private fun NavigationItemButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animated icon/text color
    val isDark = isSystemInDarkTheme()
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "navContentColor"
    )
    
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
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
