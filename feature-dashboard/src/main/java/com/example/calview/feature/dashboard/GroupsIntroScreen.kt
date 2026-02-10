package com.example.calview.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.feature.dashboard.R

@Composable
fun GroupsIntroScreen(
    onBack: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit
) {
    // Safe fallback gradient
    val safeGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE0F7FA), // Light Cyan
            Color(0xFFE8EAF6)  // Light Indigo
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(safeGradient) // Use safe gradient instead of theme property
    ) {
        Scaffold(
            topBar = {
                // No back button needed - navigation is handled by the floating nav bar
                Spacer(modifier = Modifier.statusBarsPadding().height(8.dp))
            },
            containerColor = Color.Transparent,
//            bottomBar = {
//            Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
//                Button(
//                    onClick = onCreateGroupClick,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    shape = RoundedCornerShape(28.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary,
//                        contentColor = MaterialTheme.colorScheme.onPrimary
//                    )
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(Icons.Default.Add, contentDescription = null)
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = stringResource(R.string.create_new_group_action),
//                            fontFamily = InterFontFamily,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                OutlinedButton(
//                    onClick = onJoinGroupClick,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    shape = RoundedCornerShape(28.dp),
//                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
//                ) {
//                    Text(
//                        text = stringResource(R.string.join_existing_group_action),
//                        fontFamily = InterFontFamily,
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
//                }
//            }
//        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Food Collage Section (Mocked)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                MockFoodCollage()
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.groups_intro_title),
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.groups_intro_subtitle),
                fontFamily = InterFontFamily,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCreateGroupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.create_new_group_action),
                        fontFamily = InterFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onJoinGroupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = stringResource(R.string.join_existing_group_action),
                    fontFamily = InterFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
        }
    }
}

@Composable
fun MockFoodCollage() {
    // Mocking the collage layout from the screenshot
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            MockFoodCard(modifier = Modifier.weight(1f).padding(4.dp), user = "Michael Jones", cal = "57", imageRes = null)
            MockFoodCard(modifier = Modifier.weight(1f).padding(4.dp), user = "Jane Smith", cal = "312", imageRes = null)
        }
        Row(modifier = Modifier.weight(1f)) {
            MockFoodCard(modifier = Modifier.weight(1f).padding(4.dp), user = "Emily Brown", cal = "190", imageRes = null)
            MockFoodCard(modifier = Modifier.weight(1f).padding(4.dp), user = "Rose Belvins", cal = "285", imageRes = null)
        }
    }
}

@Composable
fun MockFoodCard(modifier: Modifier, user: String, cal: String, imageRes: Int?) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Placeholder for image
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)))
        
        // Attribution Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = user, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "$cal Calories", color = Color.White, fontSize = 8.sp)
                }
            }
        }
    }
}
