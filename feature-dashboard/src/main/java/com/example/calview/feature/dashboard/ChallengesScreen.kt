package com.example.calview.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.data.local.BadgeEntity
import com.example.calview.core.data.local.ChallengeEntity
import com.example.calview.core.data.local.BadgeTier
import com.example.calview.core.ui.util.rememberHapticsManager
import com.example.calview.core.ui.util.rememberSoundManager
import com.example.calview.core.ui.util.SoundManager
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import androidx.compose.foundation.clickable
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.core.ui.theme.InterFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Challenges & Badges") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    ) { paddingValues ->
        // Adaptive layout
        val windowSizeClass = LocalWindowSizeClass.current
        val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
        val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Active Challenges Section
                item {
                    Text(
                        text = "Active Weekly Challenges",
                        fontFamily = InterFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(uiState.activeChallenges) { uiModel ->
                    ChallengeCard(uiModel = uiModel)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (uiState.activeChallenges.isEmpty() && !uiState.isLoading) {
                    item {
                        Text("No active challenges right now.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Badges Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Trophy Case",
                        fontFamily = InterFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    BadgeGrid(badges = uiState.allBadges)
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(uiModel: ChallengeUiModel) {
    val challenge = uiModel.challenge
    val progress = if (challenge.targetValue > 0) challenge.currentProgress.toFloat() / challenge.targetValue else 0f
    val haptics = rememberHapticsManager()
    val soundManager = rememberSoundManager()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (progress >= 1f) {
                    haptics.success()
                    soundManager.play(SoundManager.SoundType.COMPLETE)
                } else {
                    haptics.click()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = challenge.description,
                        fontFamily = InterFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Challenge trophy",
                    tint = MaterialTheme.colorScheme.tertiary, // Trophy color
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Column {
                Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Social Pulse Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.4f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${uiModel.participantsCount} joined",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = "${challenge.currentProgress}/${challenge.targetValue}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.02).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun BadgeGrid(badges: List<BadgeEntity>) {
    // Using a simpler row/flow layout for inside LazyColumn since LazyVerticalGrid inside LazyColumn is tricky without fixed height
    // Or we can just calculate rows. For now, let's just show them in a flow row or multiple rows manually.
    // Actually, distinct LazyVerticalGrid is nested scrollable.
    // Better to use FlowRow (if available) or distinct rows.
    
    // Fallback: Just column of rows logic or strictly vertical list if few badges.
    // Let's use a fixed height grid logic for simplicity or a specialized layout.
    
    if (badges.isEmpty()) {
        Text("No badges unlocked yet. Keep going!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    // Grid emulation
    val rows = badges.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { rowBadges ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowBadges.forEach { badge ->
                    BadgeItem(badge = badge, modifier = Modifier.weight(1f))
                }
                // Fill empty space if row has < 3 items
                repeat(3 - rowBadges.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BadgeItem(badge: BadgeEntity, modifier: Modifier = Modifier) {
    val isLocked = badge.dateUnlocked == 0L
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val tierColor = if (isLocked) Color.Gray else when (badge.tier) {
            BadgeTier.BRONZE -> Color(0xFFCD7F32)
            BadgeTier.SILVER -> Color(0xFFC0C0C0)
            BadgeTier.GOLD -> Color(0xFFFFD700)
            BadgeTier.PLATINUM -> Color(0xFFE5E4E2)
        }
        
        Box(
            modifier = Modifier
                .size(80.dp) // Slightly bigger
                .background(
                    brush = if (isLocked) {
                        Brush.radialGradient(colors = listOf(Color.Black.copy(alpha = 0.1f), Color.Transparent))
                    } else {
                        Brush.radialGradient(colors = listOf(tierColor.copy(alpha = 0.2f), Color.Transparent))
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Badge",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Star, // Placeholder for specific icons
                    contentDescription = null,
                    tint = tierColor,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = badge.name,
            fontFamily = InterFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = badge.description,
            fontFamily = InterFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            minLines = 2,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}
