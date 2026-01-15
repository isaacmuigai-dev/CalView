package com.example.calview.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.calview.feature.dashboard.R
import androidx.compose.ui.res.stringResource

// Data Models
data class FeatureRequest(
    val id: String,
    val title: String,
    val description: String,
    val authorName: String,
    val authorPhotoUrl: String = "",
    val votes: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime,
    val status: RequestStatus = RequestStatus.OPEN,
    val hasVoted: Boolean = false,
    val tags: List<String> = emptyList()
)

data class Comment(
    val id: String,
    val authorName: String,
    val authorPhotoUrl: String = "",
    val content: String,
    val createdAt: LocalDateTime,
    val likes: Int = 0,
    val hasLiked: Boolean = false
)

enum class RequestStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    DECLINED
}

enum class FilterTab {
    TRENDING,
    NEWEST,
    MY_POSTS
}

// Semantic accent colors (kept for specific meanings)
private val AccentGreen = Color(0xFF00C853)   // Success/completed
private val AccentOrange = Color(0xFFFF6D00)  // Warning/in-progress

// Theme-aware accent colors - use MaterialTheme.colorScheme.primary/secondary in composables

/**
 * Feature Request Screen - X-inspired social feed for feature suggestions
 * Connected to Firestore for real-time sync across all users
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureRequestScreen(
    onBack: () -> Unit,
    viewModel: FeedbackHubViewModel
) {
    var selectedTab by remember { mutableStateOf(FilterTab.TRENDING) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedRequest by remember { mutableStateOf<FeatureRequest?>(null) }
    
    // Collect ViewModel state
    val requestDtos by viewModel.featureRequests.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Convert DTOs to UI models
    val requests = remember(requestDtos, currentUserId) {
        requestDtos.map { dto ->
            FeatureRequest(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                authorName = dto.authorName,
                authorPhotoUrl = dto.authorPhotoUrl,
                votes = dto.votes,
                commentCount = dto.commentCount,
                createdAt = java.time.Instant.ofEpochMilli(dto.createdAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime(),
                status = try { RequestStatus.valueOf(dto.status) } catch (e: Exception) { RequestStatus.OPEN },
                hasVoted = dto.votedBy.contains(currentUserId),
                tags = dto.tags
            )
        }
    }
    
    val filteredRequests = remember(requests, selectedTab, searchQuery, currentUserName) {
        val filtered = when (selectedTab) {
            FilterTab.TRENDING -> requests.sortedByDescending { it.votes }
            FilterTab.NEWEST -> requests.sortedByDescending { it.createdAt }
            FilterTab.MY_POSTS -> requests.filter { it.authorName == currentUserName }
        }
        if (searchQuery.isNotEmpty()) {
            filtered.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        } else {
            filtered
        }
    }
    
    // Show error snackbar
    LaunchedEffect(error) {
        if (error != null) {
            // Error will auto-clear or user dismisses
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                FeatureRequestTopBar(
                    onBack = onBack,
                    onRefresh = { /* Firestore handles real-time refresh */ }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_request_desc),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.feature_requests_title),
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.shape_future_desc),
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Filter Tabs
                FilterTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // Loading indicator
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Request Feed
                if (filteredRequests.isEmpty() && !isLoading) {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredRequests) { request ->
                            FeatureRequestCard(
                                request = request,
                                onVote = { 
                                    viewModel.voteOnRequest(request.id)
                                },
                                onClick = { 
                                    selectedRequest = request
                                    viewModel.selectRequest(request.id) 
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
        
        // Error Snackbar
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(stringResource(R.string.dismiss_action), color = Color.White)
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
        
        // Create Request Bottom Sheet
        if (showCreateSheet) {
            CreateRequestSheet(
                userName = currentUserName,
                onDismiss = { showCreateSheet = false },
                onSubmit = { title, description, tags ->
                    viewModel.postFeatureRequest(title, description, tags)
                    showCreateSheet = false
                }
            )
        }
        
        // Request Detail Sheet
        selectedRequest?.let { request ->
            RequestDetailSheet(
                request = request,
                viewModel = viewModel,
                onDismiss = { 
                    selectedRequest = null 
                    viewModel.selectRequest(null)
                },
                onVote = {
                    viewModel.voteOnRequest(request.id)
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeatureRequestTopBar(
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.feedback_hub_title),
                fontFamily = Inter,
                fontWeight = FontWeight.Bold
            )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_desc))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun FilterTabRow(
    selectedTab: FilterTab,
    onTabSelected: (FilterTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val bgColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                label = "tabBgColor"
            )
            val textColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "tabTextColor"
            )
            
            Surface(
                onClick = { onTabSelected(tab) },
                color = bgColor,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (tab) {
                        FilterTab.TRENDING -> stringResource(R.string.tab_trending)
                        FilterTab.NEWEST -> stringResource(R.string.tab_newest)
                        FilterTab.MY_POSTS -> stringResource(R.string.tab_my_posts)
                    },
                    fontFamily = Inter,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { 
            Text(
                text = stringResource(R.string.search_placeholder),
                fontFamily = Inter,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun FeatureRequestCard(
    request: FeatureRequest,
    onVote: () -> Unit,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "cardScale"
    )
    
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vote Section
            VoteButton(
                votes = request.votes,
                hasVoted = request.hasVoted,
                onVote = onVote
            )
            
            // Content Section
            Column(modifier = Modifier.weight(1f)) {
                // Author Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            request.authorName.take(1).uppercase(),
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        request.authorName,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        " · ${formatTimeAgo(request.createdAt)}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Title
                Text(
                    request.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description
                if (request.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        request.description,
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
                
                // Tags
                if (request.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(request.tags) { tag ->
                            TagChip(tag = tag)
                        }
                    }
                }
                
                // Actions
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Comments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onClick() }
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.comments_title, 0).substringBefore(" ("), // hacky approach for description
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${request.commentCount}",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Status Badge
                    StatusBadge(status = request.status)
                }
            }
        }
    }
}

@Composable
private fun VoteButton(
    votes: Int,
    hasVoted: Boolean,
    onVote: () -> Unit
) {
    val bgColor by animateColorAsState(
        if (hasVoted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "voteBgColor"
    )
    val iconColor by animateColorAsState(
        if (hasVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "voteIconColor"
    )
    val scale by animateFloatAsState(
        if (hasVoted) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "voteScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onVote() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            if (hasVoted) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowUp,
            contentDescription = "Upvote",
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )
        Text(
            "$votes",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = iconColor
        )
    }
}

@Composable
private fun TagChip(tag: String) {
    val tagColors = mapOf(
        "bug" to AccentOrange,
        "feature" to MaterialTheme.colorScheme.primary,
        "ui" to MaterialTheme.colorScheme.secondary,
        "health" to AccentGreen
    )
    val color = tagColors[tag.lowercase()] ?: MaterialTheme.colorScheme.primary
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "#$tag",
            fontFamily = Inter,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusBadge(status: RequestStatus) {
    val (text, color) = when (status) {
        RequestStatus.OPEN -> stringResource(R.string.status_open) to MaterialTheme.colorScheme.onSurfaceVariant
        RequestStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress) to AccentOrange
        RequestStatus.COMPLETED -> stringResource(R.string.status_completed) to AccentGreen
        RequestStatus.DECLINED -> stringResource(R.string.status_declined) to Color.Gray
    }
    
    if (status != RequestStatus.OPEN) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text,
                fontFamily = Inter,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lightbulb,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_state_title),
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_state_desc),
            fontFamily = Inter,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRequestSheet(
    userName: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, description: String, tags: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    val availableTags = listOf("Feature", "UI", "Health", "Bug", "Performance")
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.take(1).uppercase(),
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.new_feature_request_title),
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.share_idea_desc),
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.feature_title_label), fontFamily = Inter) },
                placeholder = { Text(stringResource(R.string.feature_title_placeholder), fontFamily = Inter) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label), fontFamily = Inter) },
                placeholder = { Text(stringResource(R.string.description_placeholder), fontFamily = Inter) },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Tags
            Text(
                text = stringResource(R.string.add_tags_label),
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableTags) { tag ->
                    val isSelected = selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTags = if (isSelected) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                        },
                        label = { 
                            Text(
                                tag,
                                fontFamily = Inter,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            Button(
                onClick = { onSubmit(title, description, selectedTags) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.post_request_action),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestDetailSheet(
    request: FeatureRequest,
    viewModel: FeedbackHubViewModel,
    onDismiss: () -> Unit,
    onVote: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // Collect comments from ViewModel (real-time from Firestore)
    val commentDtos by viewModel.comments.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    // Convert DTOs to UI models
    val comments = remember(commentDtos, currentUserId) {
        commentDtos.map { dto ->
            Comment(
                id = dto.id,
                authorName = dto.authorName,
                authorPhotoUrl = dto.authorPhotoUrl,
                content = dto.content,
                createdAt = java.time.Instant.ofEpochMilli(dto.createdAt)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime(),
                likes = dto.likes,
                hasLiked = dto.likedBy.contains(currentUserId)
            )
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                VoteButton(
                    votes = request.votes,
                    hasVoted = request.hasVoted,
                    onVote = onVote
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        request.title,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                request.authorName.take(1).uppercase(),
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${request.authorName} · ${formatTimeAgo(request.createdAt)}",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Description
            if (request.description.isNotEmpty()) {
                Text(
                    request.description,
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Tags
            if (request.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(request.tags) { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // Comments Section
            Text(
                text = stringResource(R.string.comments_title, comments.size),
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Comment Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { 
                        Text(
                            text = stringResource(R.string.add_comment_placeholder),
                            fontFamily = Inter,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commentText.isNotBlank()) {
                                viewModel.postComment(commentText)
                                commentText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.postComment(commentText)
                            commentText = ""
                            focusManager.clearFocus()
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send_desc),
                        tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Comments List
            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_comments_yet),
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.be_first_comment),
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(
                            comment = comment,
                            onLike = {
                                viewModel.likeComment(comment.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CommentItem(
    comment: Comment,
    onLike: () -> Unit
) {
    val likeScale by animateFloatAsState(
        if (comment.hasLiked) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "likeScale"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                comment.authorName.take(1).uppercase(),
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    comment.authorName,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    " · ${formatTimeAgo(comment.createdAt)}",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                comment.content,
                fontFamily = Inter,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
                // Like Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onLike() }
            ) {
                Icon(
                    if (comment.hasLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (comment.hasLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .scale(likeScale)
                )
                if (comment.likes > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${comment.likes}",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = if (comment.hasLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Helper function to format time ago
@Composable
private fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    val days = ChronoUnit.DAYS.between(dateTime, now)
    
    return when {
        minutes < 1 -> stringResource(R.string.just_now)
        minutes < 60 -> "${minutes}${stringResource(R.string.minutes_ago_suffix)}"
        hours < 24 -> "${hours}${stringResource(R.string.hours_ago_suffix)}"
        days < 7 -> "${days}${stringResource(R.string.days_ago_suffix)}"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

// Sample data for demonstration
private fun getSampleRequests(): List<FeatureRequest> = listOf(
    FeatureRequest(
        id = "1",
        title = "Sync with Samsung Health",
        description = "Would love to see integration with Samsung Health for automatic step and workout tracking.",
        authorName = "Jordan",
        votes = 384,
        commentCount = 45,
        createdAt = LocalDateTime.now().minusDays(2),
        status = RequestStatus.IN_PROGRESS,
        tags = listOf("Health", "Feature")
    ),
    FeatureRequest(
        id = "2",
        title = "Dark mode improvements",
        description = "The current dark mode is good but could use some refinements. Maybe add AMOLED black option?",
        authorName = "Alex",
        votes = 187,
        commentCount = 23,
        createdAt = LocalDateTime.now().minusHours(8),
        tags = listOf("UI")
    ),
    FeatureRequest(
        id = "3",
        title = "Intermittent fasting timer",
        description = "Add a feature to track intermittent fasting windows with start/end times and notifications.",
        authorName = "Sam",
        votes = 156,
        commentCount = 18,
        createdAt = LocalDateTime.now().minusDays(1),
        tags = listOf("Health", "Feature")
    ),
    FeatureRequest(
        id = "4",
        title = "Recipe suggestions based on macros",
        description = "Would be awesome to get recipe ideas that fit remaining daily macros!",
        authorName = "Taylor",
        votes = 89,
        commentCount = 12,
        createdAt = LocalDateTime.now().minusHours(4),
        tags = listOf("Feature")
    ),
    FeatureRequest(
        id = "5",
        title = "Widget improvements",
        description = "Make widgets more customizable - choose what data to display.",
        authorName = "Morgan",
        votes = 72,
        commentCount = 8,
        createdAt = LocalDateTime.now().minusMinutes(30),
        tags = listOf("UI", "Feature")
    )
)

private fun getSampleComments(requestId: String): List<Comment> = listOf(
    Comment(
        id = "c1",
        authorName = "Riley",
        content = "This would be amazing! Really need this feature.",
        createdAt = LocalDateTime.now().minusHours(2),
        likes = 12,
        hasLiked = false
    ),
    Comment(
        id = "c2",
        authorName = "Casey",
        content = "Upvoted! Would use this every day.",
        createdAt = LocalDateTime.now().minusHours(5),
        likes = 8,
        hasLiked = true
    ),
    Comment(
        id = "c3",
        authorName = "Drew",
        content = "Great suggestion. Hope the team considers this!",
        createdAt = LocalDateTime.now().minusDays(1),
        likes = 5
    )
)
