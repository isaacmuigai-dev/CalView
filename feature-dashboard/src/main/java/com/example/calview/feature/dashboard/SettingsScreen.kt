package com.example.calview.feature.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
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
    onDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {},
    // Widget data from dashboard
    remainingCalories: Int = 0,
    proteinLeft: Int = 0,
    carbsLeft: Int = 0,
    fatsLeft: Int = 0,
    streakDays: Int = 0
) {
    val uiState by viewModel.uiState.collectAsState()
    
    SettingsContent(
        userName = uiState.userName.ifEmpty { "User" },
        photoUrl = uiState.photoUrl,
        ageStr = if (uiState.age > 0) "${uiState.age} years old" else "",
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
        onDeleteAccount = onDeleteAccount,
        onLogout = onLogout,
        remainingCalories = remainingCalories,
        proteinLeft = proteinLeft,
        carbsLeft = carbsLeft,
        fatsLeft = fatsLeft,
        streakDays = streakDays,
        userEmail = uiState.userEmail,
        userId = uiState.userId
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
    userId: String = ""
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
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Title
        Text(
            text = "Settings",
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
            remainingCalories = remainingCalories,
            proteinLeft = proteinLeft,
            carbsLeft = carbsLeft,
            fatsLeft = fatsLeft,
            streakDays = streakDays,
            onHowToAddClick = onHowToAddWidgetClick
        )
        
        LegalSection(
            onTermsClick = onTermsClick,
            onPrivacyClick = onPrivacyClick,
            onSupportClick = onSupportEmailClick,
            onFeatureRequestClick = onFeatureRequestClick,
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
                Text("Invite friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                // Mock image background
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Text(
                        "The journey\nis easier together.",
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
                            "Refer a friend to earn \$10",
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
            SettingsItem(Icons.Default.Badge, "Personal details", onClick = onPersonalDetailsClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Refresh, "Adjust macronutrients", onClick = onEditMacrosClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Flag, "Goal & current weight", onClick = onPersonalDetailsClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.History, "Weight history", onClick = onWeightHistoryClick)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Translate, "Language", onClick = onLanguageClick)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
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
                    Text("Appearance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Choose light, dark, or system appearance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                title = "Add Burned Calories",
                subtitle = "Add burned calories to daily goal",
                checked = addCaloriesBack,
                onCheckedChange = onAddCaloriesBackChange
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Rollover Calories toggle
            PreferenceToggle(
                title = "Rollover calories",
                subtitle = "Add up to 200 left over calories from yesterday into today's daily goal",
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
    remainingCalories: Int = 0,
    proteinLeft: Int = 0,
    carbsLeft: Int = 0,
    fatsLeft: Int = 0,
    streakDays: Int = 0,
    onHowToAddClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Widgets",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "How to add?",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onHowToAddClick() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item { WidgetCaloriesWithMacros(
                remainingCalories = remainingCalories,
                proteinLeft = proteinLeft,
                carbsLeft = carbsLeft,
                fatsLeft = fatsLeft
            ) }
            item { WidgetQuickActions() }
        }
    }
}

@Composable
fun WidgetCaloriesOnly(remainingCalories: Int = 0) {
    Surface(
        modifier = Modifier.size(width = 160.dp, height = 180.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { 0.5f },
                    color = MaterialTheme.colorScheme.onSurface,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        remainingCalories.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Calories left",
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "+ Log your food",
                    color = MaterialTheme.colorScheme.surface,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun WidgetCaloriesWithMacros(
    remainingCalories: Int = 0,
    proteinLeft: Int = 0,
    carbsLeft: Int = 0,
    fatsLeft: Int = 0
) {
    Surface(
        modifier = Modifier.size(width = 240.dp, height = 180.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { 0.5f },
                    color = MaterialTheme.colorScheme.outlineVariant,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        remainingCalories.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Calories left",
                        fontSize = 7.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WidgetMacroItem(Color(0xFFD64D50), Icons.Default.Favorite, "${proteinLeft}g", "Protein left")
                WidgetMacroItem(Color(0xFFE5A87B), Icons.Default.Grass, "${carbsLeft}g", "Carbs left")
                WidgetMacroItem(Color(0xFF6A8FB3), Icons.Default.Opacity, "${fatsLeft}g", "Fats left")
            }
        }
    }
}

@Composable
fun WidgetQuickActions() {
    Surface(
        modifier = Modifier.size(width = 140.dp, height = 180.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FilterCenterFocus,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Scan Food",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Barcode",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
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
            SettingsItem(Icons.Default.PersonRemove, "Delete Account?", onClick = onDeleteAccountClick)
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit = {}) {
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
        "VERSION 1.0.184",
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
                            .background(Color(0xFFF5F5F5))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
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
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.LightGray
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Log out Button
                    Button(
                        onClick = onConfirmLogout,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF8A5A5),
                            contentColor = Color(0xFFD32F2F)
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
                            .background(Color(0xFFF5F5F5))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
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
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.LightGray
                        )
                    ) {
                        Text(
                            "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                    
                    // Delete Button
                    Button(
                        onClick = onConfirmDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF8A5A5),
                            contentColor = Color(0xFFD32F2F)
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
