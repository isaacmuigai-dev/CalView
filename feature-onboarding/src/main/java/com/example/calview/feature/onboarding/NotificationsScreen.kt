package com.example.calview.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.calview.core.ui.theme.Inter
import com.example.calview.feature.onboarding.components.OnboardingScreenLayout

/**
 * Reach your goals with notifications screen.
 * Shows notification permission request with Allow/Don't Allow options.
 * On Android 13+ (API 33), clicking Allow will launch the system permission dialog.
 */
@Composable
fun NotificationsScreen(
    currentStep: Int,
    totalSteps: Int,
    notificationsEnabled: Boolean = false,
    onNotificationChoice: (Boolean) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    
    // Check if we need to request permission (Android 13+)
    val needsPermissionRequest = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    
    // Check current permission status
    val hasNotificationPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required on older Android versions
        }
    }
    
    // Permission launcher for notification permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onNotificationChoice(isGranted)
    }
    
    // Handle Allow button click
    val onAllowClick: () -> Unit = {
        if (needsPermissionRequest && !hasNotificationPermission) {
            // Launch system permission dialog on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Permission already granted or not needed
            onNotificationChoice(true)
        }
    }
    
    OnboardingScreenLayout(
        currentStep = currentStep,
        totalSteps = totalSteps,
        title = "",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue,
        continueEnabled = true
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Reach your goals with\nnotifications",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Notification permission card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CalViewAI would like to send you\nNotifications",
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Allow/Don't Allow buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Don't Allow button
                        Surface(
                            onClick = { onNotificationChoice(false) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!notificationsEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Don't Allow",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        // Allow button - launches permission request
                        Surface(
                            onClick = onAllowClick,
                            shape = RoundedCornerShape(12.dp),
                            color = if (notificationsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Allow",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (notificationsEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Thumbs up emoji
            Text(
                text = "👍",
                fontSize = 40.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(0.4f))
    }
}
