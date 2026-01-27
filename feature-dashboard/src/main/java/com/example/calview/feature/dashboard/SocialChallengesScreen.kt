package com.example.calview.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.calview.core.data.local.ChallengeParticipantEntity
import com.example.calview.core.data.local.SocialChallengeEntity
import com.example.calview.core.data.local.SocialChallengeType
import com.example.calview.core.ui.components.PremiumBadge
import com.example.calview.core.ui.components.PremiumLockedCard
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass

/**
 * Social Challenges Screen - Premium feature for friend-based challenges.
 * Supports adaptive layouts for Compact, Medium, and Expanded window sizes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialChallengesScreen(
    isPremium: Boolean,
    onNavigateBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    viewModel: SocialChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    
    // Adaptive layout support
    val windowSizeClass = LocalWindowSizeClass.current
    val widthClass = windowSizeClass.widthSizeClass
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(widthClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(widthClass)
    val useGridLayout = widthClass == WindowWidthSizeClass.Expanded
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { 
                            heading()
                        }
                    ) {
                        Text("Social Challenges")
                        if (!isPremium) {
                            Spacer(modifier = Modifier.width(8.dp))
                            PremiumBadge()
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            if (isPremium) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        contentDescription = "Create new challenge"
                        role = Role.Button
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Challenge")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            // Constrain content width on larger screens
            Box(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxSize()
            ) {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (uiState.error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontalPadding, 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.error ?: "An error occurred",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (!isPremium) {
                    // Premium locked view
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontalPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        PremiumLockedCard(
                            featureName = "Social Challenges",
                            featureDescription = "Challenge your friends to weekly competitions! Track who logs most meals, drinks most water, or maintains the longest streak.",
                            icon = Icons.Default.Group,
                            onUpgradeClick = onUpgradeClick
                        )
                    }
                } else {
                    // Use grid layout on expanded screens
                    if (useGridLayout && uiState.challenges.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 300.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Join card spans full width
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                JoinChallengeCard(
                                    onClick = { showJoinDialog = true }
                                )
                            }
                            
                            items(uiState.challenges) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        participants = uiState.participantsMap[challenge.id] ?: emptyList(),
                                        currentUserId = uiState.currentUserId,
                                        onShareClick = { viewModel.shareChallenge(challenge) },
                                        onLeaveClick = { viewModel.leaveChallenge(challenge.id) },
                                        onEndClick = { viewModel.endChallenge(challenge.id) }
                                    )
                            }
                        }
                    } else {
                        // Standard list layout for compact/medium
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = horizontalPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Join Challenge Section
                            item {
                                JoinChallengeCard(onClick = { showJoinDialog = true })
                            }
                            
                            // Active Challenges Header
                            item {
                                Text(
                                    "Your Active Challenges",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .semantics { heading() }
                                )
                            }
                            
                            // Challenges List
                            if (uiState.challenges.isEmpty()) {
                                item {
                                    EmptyChallengesCard(onCreateClick = { showCreateDialog = true })
                                }
                            } else {
                                items(uiState.challenges) { challenge ->
                                    ChallengeCard(
                                        challenge = challenge,
                                        participants = uiState.participantsMap[challenge.id] ?: emptyList(),
                                        currentUserId = uiState.currentUserId,
                                        onShareClick = { viewModel.shareChallenge(challenge) },
                                        onLeaveClick = { viewModel.leaveChallenge(challenge.id) },
                                        onEndClick = { viewModel.endChallenge(challenge.id) }
                                    )
                                }
                            }
                            
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
        
        // Create Challenge Dialog
        if (showCreateDialog) {
            CreateChallengeDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, type, duration ->
                    viewModel.createChallenge(title, type, duration)
                    showCreateDialog = false
                }
            )
        }
        
        // Join Challenge Dialog
        if (showJoinDialog) {
            JoinChallengeDialog(
                onDismiss = { showJoinDialog = false },
                onJoin = { code ->
                    viewModel.joinChallenge(code)
                    showJoinDialog = false
                }
            )
        }
        
        // Invite Code Success Dialog - show after challenge creation
        uiState.createdChallenge?.let { challenge ->
            InviteCodeSuccessDialog(
                challenge = challenge,
                onDismiss = { viewModel.clearCreatedChallenge() }
            )
        }
    }
}

@Composable
private fun JoinChallengeCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onClickLabel = "Join a challenge with invite code"
            )
            .semantics {
                role = Role.Button
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.QrCode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Join a Challenge",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Enter invite code from a friend",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
private fun ChallengeCard(
    challenge: SocialChallengeEntity,
    participants: List<ChallengeParticipantEntity>,
    currentUserId: String?,
    onShareClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                // Accessibility: Summary of challenge
                val userRank = participants.find { it.odsmUserId == currentUserId }?.rank
                val userProgress = participants.find { it.odsmUserId == currentUserId }?.currentProgress
                contentDescription = "${challenge.title}, ${getChallengeTypeLabel(challenge.type)}. " +
                        (if (userRank != null) "You are ranked $userRank with $userProgress points. " else "") +
                        "Leader: ${participants.firstOrNull()?.displayName ?: "None"}."
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = challenge.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getChallengeTypeLabel(challenge.type),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    val context = LocalContext.current
                    
                    if (challenge.creatorId == currentUserId) {
                        IconButton(onClick = onEndClick) {
                            Icon(Icons.Default.StopCircle, "End challenge", tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    IconButton(onClick = {
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                "Join my challenge \"${challenge.title}\" on CalView! Use invite code: ${challenge.inviteCode}"
                            )
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Challenge"))
                    }) {
                        Icon(Icons.Default.Share, "Share invite", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Leaderboard
            Text(
                "Leaderboard",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val participantList = participants.take(5)
            participantList.forEachIndexed { index, participant ->
                LeaderboardRow(
                    rank = index + 1,
                    participant = participant,
                    isCurrentUser = participant.odsmUserId == currentUserId
                )
                if (index < participantList.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Invite code
            Spacer(modifier = Modifier.height(12.dp))
            val context = LocalContext.current
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Invite Code", challenge.inviteCode)
                            clipboardManager.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Code copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Invite code: ${challenge.inviteCode}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        Icons.Default.ContentCopy,
                        "Copy",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    participant: ChallengeParticipantEntity,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .semantics {
                // Accessibility: Row description
                contentDescription = "Rank $rank, ${if (isCurrentUser) "You" else participant.displayName}, ${participant.currentProgress} points"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        val rankIcon = when (rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "$rank"
        }
        Text(
            text = rankIcon,
            fontSize = 18.sp,
            modifier = Modifier.width(32.dp)
        )
        
        // Avatar
        if (participant.photoUrl.isNotEmpty()) {
            AsyncImage(
                model = participant.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.displayName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isCurrentUser) "You" else participant.displayName,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
        
        // Progress
        Text(
            text = participant.currentProgress.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyChallengesCard(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Active Challenges",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                "Create a challenge or join one using an invite code!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onCreateClick) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Challenge")
            }
        }
    }
}

@Composable
private fun CreateChallengeDialog(
    onDismiss: () -> Unit,
    onCreate: (String, SocialChallengeType, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SocialChallengeType.LOGGING) }
    var duration by remember { mutableStateOf(7) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Challenge") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Challenge Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Challenge Type", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                
                SocialChallengeType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Text(getChallengeTypeLabel(type.name))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Duration: $duration days", fontWeight = FontWeight.Medium)
                Slider(
                    value = duration.toFloat(),
                    onValueChange = { duration = it.toInt() },
                    valueRange = 3f..30f,
                    steps = 8
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title.ifEmpty { "Weekly Challenge" }, selectedType, duration) },
                enabled = title.isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InviteCodeSuccessDialog(
    challenge: SocialChallengeEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = remember { 
        context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager 
    }
    var copied by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Challenge Created!")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Share this invite code with friends:",
                    textAlign = TextAlign.Center
                )
                
                // Invite code display
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clip = android.content.ClipData.newPlainText("Invite Code", challenge.inviteCode)
                                clipboardManager.setPrimaryClip(clip)
                                copied = true
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = challenge.inviteCode,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy to clipboard",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                if (copied) {
                    Text(
                        "Copied to clipboard!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    "\"${challenge.title}\"",
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Share intent
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(
                            android.content.Intent.EXTRA_TEXT,
                            "Join my challenge \"${challenge.title}\" on CalView! Use invite code: ${challenge.inviteCode}"
                        )
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Challenge"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun JoinChallengeDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Challenge") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase().take(6) },
                label = { Text("Invite Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onJoin(code) },
                enabled = code.length == 6
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getChallengeTypeLabel(type: String): String {
    return when (type) {
        "STREAK" -> "🔥 Streak Challenge"
        "WATER" -> "💧 Water Champion"
        "CALORIES" -> "🎯 Calorie Goals"
        "PROTEIN" -> "💪 Protein Power"
        "STEPS" -> "👟 Step Counter"
        "LOGGING" -> "📝 Most Consistent Logger"
        else -> type
    }
}
