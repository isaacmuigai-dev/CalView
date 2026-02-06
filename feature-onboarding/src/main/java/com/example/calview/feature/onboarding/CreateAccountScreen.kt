package com.example.calview.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Create an account screen - mandatory Google sign-in.
 * Updated design matching the SignInBottomSheet.
 */
@Composable
fun CreateAccountScreen(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean = false,
    isSignedIn: Boolean = false,
    onBack: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    val windowSizeClass = LocalWindowSizeClass.current
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass))
                .fillMaxSize()
                .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Top bar with back button and progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = if (isSignedIn) "Complete Account" else "Create an account",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Divider
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sign in with Google button - outlined style matching SignInBottomSheet
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSignedIn) {
                        // Google "G" logo with 4 colors
                        GoogleLogo(modifier = Modifier.size(20.dp))
                        
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Text(
                        text = if (isSignedIn) "Finish Setup" else "Sign in with Google",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Terms and Privacy Policy text with clickable links
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text(
                    text = "By continuing, you agree to CalViewAI's ",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                Text(
                    text = "Terms and",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onTermsClick() }
                        .semantics { role = Role.Button }
                )
            }
            Row {
                Text(
                    text = "Conditions",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onTermsClick() }
                )
                Text(
                    text = " and ",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Privacy Policy",
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onPrivacyClick() }
                        .semantics { role = Role.Button }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Google "G" logo with 4 colors: Blue, Green, Yellow, Red
 */
@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = com.example.calview.feature.onboarding.R.drawable.google_logo),
        contentDescription = "Google Logo",
        modifier = modifier.size(20.dp)
    )
}
