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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(uiState.activeChallenges) { challenge ->
                    ChallengeCard(challenge = challenge)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                if (uiState.activeChallenges.isEmpty() && !uiState.isLoading) {
                    item {
                        Text("No active challenges right now.", color = Color.Gray)
                    }
                }

                // Badges Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Trophy Case",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    BadgeGrid(badges = uiState.unlockedBadges)
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: ChallengeEntity) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = challenge.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Challenge trophy",
                    tint = Color(0xFFFFD700), // Gold
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${challenge.currentProgress}/${challenge.targetValue}",
                        fontSize = 12.sp,
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
        Text("No badges unlocked yet. Keep going!", color = Color.Gray)
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val tierColor = when (badge.tier) {
            BadgeTier.BRONZE -> Color(0xFFCD7F32)
            BadgeTier.SILVER -> Color(0xFFC0C0C0)
            BadgeTier.GOLD -> Color(0xFFFFD700)
            BadgeTier.PLATINUM -> Color(0xFFE5E4E2)
        }
        
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(tierColor.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star, // Placeholder for specific icons
                contentDescription = null,
                tint = tierColor,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = badge.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}
