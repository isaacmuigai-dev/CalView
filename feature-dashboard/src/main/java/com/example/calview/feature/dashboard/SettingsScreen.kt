package com.example.calview.feature.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.calview.core.ui.components.CalAICard
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import androidx.compose.ui.tooling.preview.Preview
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToEditName: (String) -> Unit = {},
    onNavigateToReferFriend: () -> Unit = {},
    onNavigateToPersonalDetails: () -> Unit = {},
    onNavigateToEditMacros: () -> Unit = {},
    onNavigateToWeightHistory: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onNavigateToHowToAddWidget: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToFeatureRequest: () -> Unit = {},
    onNavigateToLicenses: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    // Widget data from dashboard
    remainingCalories: Int = 0,
    proteinLeft: Int = 0,
    carbsLeft: Int = 0,
    fatsLeft: Int = 0,
    streakDays: Int = 0,
    // Scroll state for position memory
    scrollState: androidx.compose.foundation.ScrollState = rememberScrollState()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    SettingsContent(
        userName = uiState.userName.ifEmpty { "User" },
        photoUrl = uiState.photoUrl,
        ageStr = if (uiState.age > 0) "${uiState.age} ${stringResource(R.string.years_old_suffix)}" else "",
        appearanceMode = uiState.appearanceMode,
        addCaloriesBack = uiState.addCaloriesBack,
        rolloverCalories = uiState.rolloverCalories,
        onAppearanceModeChange = viewModel::setAppearanceMode,
        onAddCaloriesBackChange = viewModel::setAddCaloriesBack,
        onRolloverCaloriesChange = viewModel::setRolloverCalories,
        onNameClick = { onNavigateToEditName(uiState.userName) },
        onReferFriendClick = onNavigateToReferFriend,
        onPersonalDetailsClick = onNavigateToPersonalDetails,
        onEditMacrosClick = onNavigateToEditMacros,
        onWeightHistoryClick = onNavigateToWeightHistory,
        onLanguageClick = onNavigateToLanguage,
        onHowToAddWidgetClick = onNavigateToHowToAddWidget,
        onTermsClick = onNavigateToTerms,
        onPrivacyClick = onNavigateToPrivacy,
        onFeatureRequestClick = onNavigateToFeatureRequest,
        onLicensesClick = onNavigateToLicenses,
        onDeleteAccount = onDeleteAccount,
        onLogout = onLogout,
        remainingCalories = remainingCalories,
        proteinLeft = proteinLeft,
        carbsLeft = carbsLeft,
        fatsLeft = fatsLeft,
        streakDays = streakDays,

        userEmail = uiState.userEmail,
        userId = uiState.userId,
        scrollState = scrollState,
        onUpgradeClick = onNavigateToSubscription,
        // Streak Freeze
        remainingFreezes = uiState.remainingFreezes,
        maxFreezes = uiState.maxFreezes,
        yesterdayMissed = uiState.yesterdayMissed,
        onUseFreeze = viewModel::useFreeze,
        isPremium = uiState.isPremium
    )
}

@Composable
fun SettingsContent(
    userName: String,
    photoUrl: String = "",
    ageStr: String,
    appearanceMode: String = "automatic",
    addCaloriesBack: Boolean = false,
    rolloverCalories: Boolean = false,
    onAppearanceModeChange: (String) -> Unit = {},
    onAddCaloriesBackChange: (Boolean) -> Unit = {},
    onRolloverCaloriesChange: (Boolean) -> Unit = {},
    onNameClick: () -> Unit = {},
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
    onLogout: () -> Unit = {},
    // Widget data
    remainingCalories: Int = 0,
    proteinLeft: Int = 0,
    carbsLeft: Int = 0,
    fatsLeft: Int = 0,
    streakDays: Int = 0,

    // Support email data
    userEmail: String = "",
    userId: String = "",
    // Scroll state for position memory
    scrollState: androidx.compose.foundation.ScrollState = rememberScrollState(),
    onUpgradeClick: () -> Unit = {},
    // Streak Freeze
    remainingFreezes: Int = 2,
    maxFreezes: Int = 2,
    yesterdayMissed: Boolean = false,
    onUseFreeze: () -> Unit = {},
    isPremium: Boolean = false
) {
    val context = LocalContext.current
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Support email handler
    val onSupportEmailClick = {
        val appVersion = "1.0" // TODO: Get from BuildConfig
        val androidVersion = Build.VERSION.RELEASE
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        
        val emailBody = """
            |.......
            |Please describe your issue above this line.
            |
            |User ID: $userId
            |Email: $userEmail
            |Version: $appVersion
            |Provider Id: $userId
            |
            |Platform: Android
            |Android Version: $androidVersion
            |Device: $deviceModel
        """.trimMargin()
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("isaacmuigai.dev@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    // Get adaptive layout values based on screen size
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
    
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(com.example.calview.core.ui.theme.CalViewTheme.gradient),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Title
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        ProfileHeader(
            name = userName,
            age = ageStr,
            photoUrl = photoUrl,
            onNameClick = onNameClick
        )
        
        InviteFriendsCard(onReferFriendClick = onReferFriendClick)
        
        SettingsList(
            onPersonalDetailsClick = onPersonalDetailsClick,
            onEditMacrosClick = onEditMacrosClick,
            onWeightHistoryClick = onWeightHistoryClick,
            onLanguageClick = onLanguageClick
        )
        
        PreferencesSection(
            appearanceMode = appearanceMode,
            addCaloriesBack = addCaloriesBack,
            rolloverCalories = rolloverCalories,
            onAppearanceModeChange = onAppearanceModeChange,
            onAddCaloriesBackChange = onAddCaloriesBackChange,
            onRolloverCaloriesChange = onRolloverCaloriesChange
        )
        

        

        
        WidgetsSection(
            onHowToAddClick = onHowToAddWidgetClick
        )
        
        LegalSection(
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
            onSupportClick = onSupportEmailClick,
            onFeatureRequestClick = onFeatureRequestClick,
            onLicensesClick = onLicensesClick,
            onDeleteAccountClick = { showDeleteAccountDialog = true }
        )
        
        LogoutButton(onClick = { showLogoutDialog = true })
        
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirmDelete = {
                showDeleteAccountDialog = false
                onDeleteAccount()
            }
        )
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirmLogout = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
    } // Close Box
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        userName = "Isaac muigai",
        ageStr = "27 years old"
    )
}

@Composable
fun ProfileHeader(
    name: String,
    age: String,
    photoUrl: String = "",
    onNameClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNameClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture
            if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (age.isNotEmpty()) {
                    Text(age, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun InviteFriendsCard(onReferFriendClick: () -> Unit = {}) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.invite_friends), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // Background Image
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_refer_friend_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Dark overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                // Content
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Text(
                        stringResource(R.string.invite_friends_desc),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onReferFriendClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.refer_friend_action),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsList(
    onPersonalDetailsClick: () -> Unit = {},
    onEditMacrosClick: () -> Unit = {},
    onWeightHistoryClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {}
) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingsItem(Icons.Default.Badge, stringResource(R.string.personal_details), onClick = onPersonalDetailsClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Refresh, stringResource(R.string.adjust_macros), onClick = onEditMacrosClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Flag, stringResource(R.string.goal_current_weight), onClick = onPersonalDetailsClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.History, stringResource(R.string.weight_history), onClick = onWeightHistoryClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Translate, stringResource(R.string.language), onClick = onLanguageClick)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = title
            }
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PreferencesSection(
    appearanceMode: String = "automatic",
    addCaloriesBack: Boolean = false,
    rolloverCalories: Boolean = false,
    onAppearanceModeChange: (String) -> Unit = {},
    onAddCaloriesBackChange: (Boolean) -> Unit = {},
    onRolloverCaloriesChange: (Boolean) -> Unit = {}
) {
    var isAppearanceExpanded by remember { mutableStateOf(false) }
    
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appearance - Expandable dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAppearanceExpanded = !isAppearanceExpanded }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.appearance), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.appearance_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appearanceMode.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (isAppearanceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Appearance options (expandable)
            if (isAppearanceExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 8.dp)
                ) {
                    listOf("Light", "Dark", "Automatic").forEach { option ->
                        val optionValue = option.lowercase()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    onAppearanceModeChange(optionValue)
                                    isAppearanceExpanded = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appearanceMode == optionValue,
                                onClick = { 
                                    onAppearanceModeChange(optionValue)
                                    isAppearanceExpanded = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (option != "Automatic") {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Add Burned Calories toggle
            PreferenceToggle(
                title = stringResource(R.string.add_burned_calories),
                subtitle = stringResource(R.string.add_burned_calories_desc),
                checked = addCaloriesBack,
                onCheckedChange = onAddCaloriesBackChange
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Rollover Calories toggle
            PreferenceToggle(
                title = stringResource(R.string.rollover_calories),
                subtitle = stringResource(R.string.rollover_calories_desc),
                checked = rolloverCalories,
                onCheckedChange = onRolloverCaloriesChange
            )
        }
    }
}

@Composable
fun PreferenceToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun WidgetsSection(
    onHowToAddClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.widgets),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            stringResource(R.string.how_to_add),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onHowToAddClick() }
        )
    }
}

@Composable
fun WidgetPreview(
    remainingCalories: Int,
    proteinLeft: Int,
    carbsLeft: Int,
    fatsLeft: Int,
    streakDays: Int
) {
    // Theme-aware colors matching widget_calories.xml
    val widgetBackground = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFF0EB),
            Color(0xFFF5EEF8)
        )
    )
    
    // "Smaller and compact" -> add horizontal padding and width constraint
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp) // Simulate widget width for compactness
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(widgetBackground)
                    .padding(12.dp)
            ) {
                // --- Row 1: Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Section
                    Column {
                        Text(
                            text = "TODAY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3000000), // #B3000000
                            letterSpacing = 0.1.sp
                        )
                        Text(
                            text = "Jan 19",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000) // #000000
                        )
                    }

                    // Streak & BMI
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Streak
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF5722), // #FF5722
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = streakDays.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                        }

                        // BMI Badge
                        Surface(
                            color = Color(0xFFE0F2FE), // widget_bmi_background approx
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BMI",
                                    fontSize = 12.sp,
                                    color = Color(0x99000000), // #99000000
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = "22.0",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF000000)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Row 2: Main Content (Rings + Macros) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Nested Rings
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(85.dp)
                            .padding(end = 12.dp)
                    ) {
                        // Outer Ring (Calories)
                        CircularProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier.size(85.dp),
                            color = Color(0xFFFF5722),
                            trackColor = Color(0xFFFFCCBC),
                            strokeWidth = 6.dp
                        )
                        // Inner Ring (Steps)
                        CircularProgressIndicator(
                            progress = { 0.4f },
                            modifier = Modifier.size(60.dp),
                            color = Color(0xFF2196F3),
                            trackColor = Color(0xFFBBDEFB),
                            strokeWidth = 6.dp
                        )
                        // Center Text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = remainingCalories.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A) // #1A1A1A
                            )
                            Text(
                                text = "left",
                                fontSize = 9.sp,
                                color = Color(0xFF666666) // #666666
                            )
                            // Indicators (Rollover/Active)
                            Row(
                                modifier = Modifier.padding(top = 1.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("+0", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFC107))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+9", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    // Right: Macros
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MacroPreviewItem(label = "Pro", amount = "${proteinLeft}g", color = Color(0xFFEF4444))
                        MacroPreviewItem(label = "Car", amount = "${carbsLeft}g", color = Color(0xFFF59E0B))
                        MacroPreviewItem(label = "Fat", amount = "${fatsLeft}g", color = Color(0xFF3B82F6))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Row 3: Activity Stats ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 6.dp)
                ) {
                    val itemModifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)

                    StatChipPreview(label = "Steps", value = "226", modifier = itemModifier)
                    StatChipPreview(label = "Burn", value = "9", modifier = itemModifier)
                    StatChipPreview(label = "7d Burn", value = "740", modifier = itemModifier)
                    StatChipPreview(label = "Record", value = "232", modifier = Modifier.weight(1f))
                }
                
                // --- BMI Section ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp)
                ) {
                    // Height/Weight Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BMI 22.0",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151), // #374151
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("60 kg", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(end = 12.dp))
                            Text("165 cm", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    // Gradient Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF3B82F6),
                                        Color(0xFF10B981),
                                        Color(0xFFF59E0B),
                                        Color(0xFFEF4444)
                                    )
                                )
                            )
                    ) {
                        // Marker
                        Box(
                            modifier = Modifier
                                .padding(start = 100.dp) // Dummy position
                                .size(12.dp)
                                .align(Alignment.CenterStart)
                                .clip(CircleShape) // IMPORTANT: Clip to Circle for circular background
                                .background(Color.White)
                                .border(2.dp, Color(0xFF10B981), CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    // Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Under", fontSize = 9.sp, color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                        Text("Healthy", fontSize = 9.sp, color = Color(0xFF10B981), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Over", fontSize = 9.sp, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Text("Obese", fontSize = 9.sp, color = Color(0xFFEF4444), modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }

                    // Category Text
                    Text(
                        text = "Healthy Weight",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MacroPreviewItem(label: String, amount: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666),
            modifier = Modifier.width(24.dp)
        )
        
        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
        
        Text(
            text = amount,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
    }
}

@Composable
fun StatChipPreview(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, fontSize = 7.sp, color = Color(0xFF666666))
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
        }
    }
}

@Composable
fun ControlCenterWidgetPreview(
    isDarkTheme: Boolean = false
) {
    val widgetBackground = if (isDarkTheme) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(Color(0xFF1A1A1A), Color(0xFF1A1A1A))
        )
    } else {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF0EB), Color(0xFFF5EEF8))
        )
    }
    
    val textColorPrimary = if (isDarkTheme) Color.White else Color.Black
    val textColorSecondary = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color(0xFF888888)
    val sectionBg = if (isDarkTheme) Color(0xFF2A2A2A) else Color.White.copy(alpha = 0.8f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .background(widgetBackground)
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Control Center",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary
                )
                Text(
                    "Today",
                    fontSize = 12.sp,
                    color = textColorSecondary
                )
            }
            
            // Water Section
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = sectionBg
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = "Water",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Water", fontSize = 12.sp, color = textColorSecondary)
                        Text("500 ml", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColorPrimary)
                    }
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "+250ml",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            // Fasting Section
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = sectionBg
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = "Fasting",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Fasting", fontSize = 12.sp, color = textColorSecondary)
                        Text("4h 32m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColorPrimary)
                    }
                    Surface(
                        color = Color(0xFFE53935),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Stop",
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ControlCenterQuickAction(icon = Icons.Default.CameraAlt, label = "Scan", textColor = textColorSecondary)
                ControlCenterQuickAction(icon = Icons.Default.Add, label = "Log", textColor = textColorSecondary)
                ControlCenterQuickAction(icon = Icons.Default.EmojiEmotions, label = "Mood", textColor = textColorSecondary)
            }
            
            // Quote
            Text(
                "\"Stay consistent, stay strong!\"",
                fontSize = 10.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = textColorSecondary,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ControlCenterQuickAction(
    icon: ImageVector,
    label: String,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(28.dp)
        )
        Text(
            label,
            fontSize = 10.sp,
            color = textColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun WidgetStreak(streakDays: Int = 0) {
    Surface(
        modifier = Modifier.size(width = 160.dp, height = 180.dp),
        color = Color(0xFFFFF3E0), // Warm orange background
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Flame icon in background
            Icon(
                Icons.Default.LocalFireDepartment,
                null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(100.dp)
            )
            // Streak count overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 30.dp)
            ) {
                Text(
                    streakDays.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color(0xFFE65100)
                )
                Text(
                    "day streak",
                    fontSize = 10.sp,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

@Composable
fun WidgetMacroItem(color: Color, icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 8.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LegalSection(
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onFeatureRequestClick: () -> Unit = {},
    onLicensesClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
) {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingsItem(Icons.Default.Description, "Terms and Conditions", onClick = onTermsClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Security, "Privacy Policy", onClick = onPrivacyClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Email, "Support Email", onClick = onSupportClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Campaign, "Feature Request", onClick = onFeatureRequestClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Code, "Open Source Licenses", onClick = onLicensesClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.PersonRemove, "Delete Account?", onClick = onDeleteAccountClick)
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit = {}) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
    val versionCode = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            1L
        }
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Logout, null, tint = Color.Black)
            Text(" Logout", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
    Text(
        "VERSION $versionName ($versionCode)",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        fontSize = 12.sp,
        color = Color.LightGray
    )
}

/**
 * Logout Confirmation Dialog
 */
@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header Row with Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log out?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Message
                Text(
                    text = "Are you sure you want to log out?",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Log out Button - use error color for destructive action
                    Button(
                        onClick = onConfirmLogout,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            "Log out",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Delete Account Confirmation Dialog
 */
@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header Row with Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Delete Account?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Warning Message
                Text(
                    text = "Are you sure want to permanently delete your account?",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Delete Button - use error color for destructive action
                    Button(
                        onClick = onConfirmDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(
                            "Delete",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}




