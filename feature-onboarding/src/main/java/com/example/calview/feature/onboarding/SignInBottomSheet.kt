package com.example.calview.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter

/**
 * Sign-in bottom sheet with Google Sign-In button only.
 * Features clickable Terms and Conditions and Privacy Policy links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInBottomSheet(
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    isLoading: Boolean = false,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with title and close button
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sign in",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Google Sign-In Button
            GoogleSignInButton(
                onClick = onGoogleSignIn,
                isLoading = isLoading
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Terms and Privacy Policy text with clickable links
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        text = "By continuing, you agree to Cal AI's ",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Row {
                    Text(
                        text = "Terms and",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onTermsClick() }
                    )
                }
                Row {
                    Text(
                        text = "Conditions",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onTermsClick() }
                    )
                    Text(
                        text = " and ",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Privacy Policy",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { onPrivacyClick() }
                    )
                }
            }
        }
    }
}

/**
 * Google Sign-In button with official Google G icon.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFE0E0E0)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.Black
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google G icon (colored version)
                GoogleGIcon()
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "Sign in with Google",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Official Google G icon with proper colors
 */
@Composable
private fun GoogleGIcon(
    modifier: Modifier = Modifier
) {
    // Try to use the vector drawable, fallback to custom drawn G
    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Custom Google G with proper colors
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(20.dp)
        ) {
            val size = this.size
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.width * 0.4f
            
            // Draw the colored G segments
            // Blue (right side)
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = true,
                size = size
            )
            // Green (bottom right)
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 45f,
                sweepAngle = 90f,
                useCenter = true,
                size = size
            )
            // Yellow (bottom left)
            drawArc(
                color = Color(0xFFFBBC05),
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = true,
                size = size
            )
            // Red (top left)
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = true,
                size = size
            )
            
            // White center circle
            drawCircle(
                color = Color.White,
                radius = radius * 0.6f,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY)
            )
            
            // Blue bar (horizontal line for the G)
            drawRect(
                color = Color(0xFF4285F4),
                topLeft = androidx.compose.ui.geometry.Offset(centerX - radius * 0.1f, centerY - radius * 0.25f),
                size = androidx.compose.ui.geometry.Size(radius * 0.8f, radius * 0.5f)
            )
        }
    }
}
