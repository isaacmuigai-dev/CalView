package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.google.firebase.auth.FirebaseAuth
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.data.model.GroupDto
import com.example.calview.core.data.model.GroupMessageDto
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.feature.dashboard.R
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupDashboardScreen(
    onGroupSettingsClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    viewModel: GroupDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGroupSwitcher by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error snackbars
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Auto-scroll to bottom on new messages ONLY if we are already at the bottom
    LaunchedEffect(uiState.messages.size) {
        if (!uiState.isLoadingOlder && uiState.messages.isNotEmpty() && listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(0)
        }
    }

    // Infinite scroll: Load more when reaching the top
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= uiState.messages.size - 5 && uiState.hasMoreMessages && !uiState.isLoadingOlder
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMoreMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(com.example.calview.core.ui.theme.CalViewTheme.gradient)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group Selector
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f), // More vibrant/visible in both modes
                    tonalElevation = 4.dp, // Increased elevation for pop
                    modifier = Modifier
                        .clickable { showGroupSwitcher = !showGroupSwitcher }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), // Added vertical padding and increased horizontal
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp) // Slightly larger icon
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = uiState.currentGroup?.photoUrl
                            if (!photoUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(photoUrl),
                                    contentDescription = "Group Icon",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = uiState.currentGroup?.name?.firstOrNull()?.toString() ?: "G",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.currentGroup?.name ?: stringResource(R.string.loading_text),
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.onlineCount > 0) Color(0xFF4ADE80) else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.memberCount} members • ${uiState.onlineCount} online",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand group list",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Settings Icon
                IconButton(
                    onClick = onGroupSettingsClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = stringResource(R.string.group_settings_header),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Message Feed
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.messages.isEmpty()) {
                    EmptyFeedState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                        reverseLayout = true // Chat-like behavior
                    ) {
                        val grouped = uiState.messages.groupBy { 
                            java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
                                .format(it.timestamp) 
                        }

                        grouped.forEach { (date, messages) ->
                            items(
                                items = messages,
                                key = { it.id }
                            ) { message ->
                                Box(modifier = Modifier.animateItemPlacement()) {
                                    MessageCard(
                                        message = message,
                                        isLiked = uiState.likedMessageIds.contains(message.id),
                                        onLikeToggle = { 
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.toggleLikeMessage(message.id) 
                                        },
                                        onReply = { 
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            viewModel.onReplySelected(message) 
                                        }
                                    )
                                }
                            }
                            
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            stickyHeader {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(16.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            text = date,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Loading more indicator at the "top" (end of list in reverse)
                        if (uiState.isLoadingOlder) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Typing Indicator
            if (uiState.typingUsers.isNotEmpty()) {
                Text(
                    text = if (uiState.typingUsers.size == 1) {
                        "${uiState.typingUsers.first()} is typing..."
                    } else {
                        "Several people are typing..."
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }
            
            // Input Bar
            MessageInputBar(
                text = uiState.inputText,
                onTextChange = viewModel::onInputTextChanged,
                onSend = viewModel::sendMessage,
                attachedImage = uiState.attachedImageUrl,
                onImageClick = { /* TODO: Launch Image Picker */ },
                onRemoveImage = { viewModel.onImageAttached(null) },
                replyMessage = uiState.selectedReplyMessage,
                onCancelReply = viewModel::cancelReply,
                isSending = uiState.isSending
            )
            Spacer(modifier = Modifier.height(130.dp))
        }

        // Group Switcher
        if (showGroupSwitcher) {
            GroupSwitcherOverlay(
                currentGroup = uiState.currentGroup,
                availableGroups = uiState.availableGroups, // Added availableGroups
                onDismiss = { showGroupSwitcher = false },
                onGroupClick = { viewModel.switchGroup(it) }, // Added onGroupClick
                onJoinClick = onJoinGroupClick,
                onCreateClick = onCreateGroupClick
            )
        }
    }
    }
}

@Composable
fun MessageCard(
    message: GroupMessageDto,
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    onReply: () -> Unit
) {
    val isMe = message.senderId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    
    // Pop animation state for likes
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.8f else 1f, label = "like_scale")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .pointerInput(Unit) {
               detectTapGestures(
                   onLongPress = { onReply() }
               )
            },
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (!message.senderPhotoUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(message.senderPhotoUrl),
                        contentDescription = "Sender Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = message.senderName.take(1).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (!isMe) {
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Surface(
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMe) 18.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 18.dp
                ),
                shadowElevation = 2.dp // Added shadow for premium feel
            ) {
                Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)) {
                    // Reply Quote Bubble
                    if (message.replyToId != null) {
                        Surface(
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .fillMaxWidth(),
                            color = (if (isMe) Color.Black else MaterialTheme.colorScheme.primary).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(IntrinsicSize.Min)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = message.replyToSenderName ?: "Unknown",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = message.replyToText ?: "",
                                        fontSize = 12.sp,
                                        color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (message.imageUrl != null) {
                        Icon(Icons.Default.Image, contentDescription = "Attached image", tint = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        fontFamily = InterFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    attachedImage: String?,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    replyMessage: GroupMessageDto?,
    onCancelReply: () -> Unit,
    isSending: Boolean = false
) {
    Column {
        // Reply Preview
        androidx.compose.animation.AnimatedVisibility(
            visible = replyMessage != null,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            if (replyMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = replyMessage.senderName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = replyMessage.text,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp, top = if (replyMessage != null) 0.dp else 16.dp),
            shape = if (replyMessage != null) 
                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp) 
            else 
                RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Column {
                // Image Preview (if attached)
                if (attachedImage != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Image, 
                            contentDescription = null, 
                            modifier = Modifier.align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = onRemoveImage,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = stringResource(R.string.remove_attachment_desc),
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onImageClick) {
                        Icon(
                            Icons.Default.AddAPhoto, 
                            contentDescription = stringResource(R.string.attach_photo_desc),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 120.dp),
                    placeholder = { 
                        Text(
                            stringResource(R.string.type_something_placeholder), 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                FloatingActionButton(
                    onClick = { if (!isSending) onSend() },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    containerColor = if ((text.isNotBlank() || attachedImage != null) && !isSending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if ((text.isNotBlank() || attachedImage != null) && !isSending) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = if ((text.isNotBlank() || attachedImage != null) && !isSending) 4.dp else 0.dp,
                        pressedElevation = if ((text.isNotBlank() || attachedImage != null) && !isSending) 8.dp else 0.dp
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Send, 
                            contentDescription = stringResource(R.string.send_message_desc), 
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun EmptyFeedState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = "Empty conversation",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_posts_yet_title),
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.be_first_share_desc),
            fontFamily = InterFontFamily,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}

@Composable
fun GroupSwitcherOverlay(
    currentGroup: GroupDto?,
    availableGroups: List<GroupDto>,
    onDismiss: () -> Unit,
    onGroupClick: (GroupDto) -> Unit,
    onJoinClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() }
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 64.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp).fillMaxWidth(0.85f)) {
                // List of groups
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(availableGroups) { group ->
                        val isSelected = group.id == currentGroup?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onGroupClick(group); onDismiss() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!group.photoUrl.isNullOrEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(group.photoUrl),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = group.name.take(1).uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = group.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = SpaceGroteskFontFamily,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                
                // Join Group
                ListItem(
                    headlineContent = { Text(stringResource(R.string.join_group_menu), fontFamily = SpaceGroteskFontFamily) },
                    leadingContent = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                    modifier = Modifier.clickable { onDismiss(); onJoinClick() }
                )
                
                // Create Group
                ListItem(
                    headlineContent = { Text(stringResource(R.string.create_group_menu), fontFamily = SpaceGroteskFontFamily) },
                    leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.clickable { onDismiss(); onCreateClick() }
                )
            }
        }
    }
}
