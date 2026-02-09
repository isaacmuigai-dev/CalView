package com.example.calview.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.InterFontFamily
import com.example.calview.core.ui.theme.SpaceGroteskFontFamily
import com.example.calview.feature.dashboard.R

@Composable
fun CreateGroupNameScreen(
    viewModel: CreateGroupViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    GroupSetupScaffold(
        title = stringResource(R.string.create_group_name_title),
        subtitle = stringResource(R.string.create_group_name_subtitle),
        onBack = onBack,
        onNext = onNext,
        nextEnabled = uiState.groupName.isNotBlank()
    ) {
        GroupsTextField(
            value = uiState.groupName,
            onValueChange = viewModel::updateGroupName,
            label = stringResource(R.string.group_name_label),
            placeholder = stringResource(R.string.group_name_placeholder)
        )
        Spacer(modifier = Modifier.height(24.dp))
        GroupsTextField(
            value = uiState.description,
            onValueChange = viewModel::updateDescription,
            label = stringResource(R.string.group_description_label),
            placeholder = stringResource(R.string.group_description_placeholder)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Name Suggestions
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.nameSuggestions) { suggestion ->
                SuggestionChip(
                    label = suggestion,
                    onClick = { viewModel.updateGroupName(suggestion) }
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CreateGroupPhotoScreen(
    viewModel: CreateGroupViewModel,
    onBack: () -> Unit,
    onCreateGroup: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Image picker launcher
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updateCustomPhotoUri(it) }
    }
    
    // Pager state for emoji pages (15 emojis per page)
    val emojisPerPage = 15
    val emojiChunks = remember(viewModel.preGeneratedPhotos) { 
        viewModel.preGeneratedPhotos.chunked(emojisPerPage) 
    }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { emojiChunks.size })
    
    GroupSetupScaffold(
        title = stringResource(R.string.choose_group_photo_title),
        subtitle = stringResource(R.string.choose_group_photo_subtitle),
        onBack = onBack,
        onNext = onCreateGroup,
        nextLabel = if (uiState.isCreating) "" else stringResource(R.string.create_group_final_action),
        nextEnabled = !uiState.isCreating,
        isLoading = uiState.isCreating // Added generic loading support to scaffold
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            
            // --- Custom Photo Section ---
            if (uiState.customPhotoUri != null) {
                Box(
                    modifier = Modifier
                        .size(160.dp) // Larger size
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(uiState.customPhotoUri),
                        contentDescription = "Selected photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    // Edit overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.upload_photo_action), // Reusing string as label
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = { viewModel.updateSelectedPhotoIndex(0) }) {
                    Text("Remove custom photo")
                }
            } else {
                // --- Emoji Pager Section ---
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(280.dp), // Fixed height for grid
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) { page ->
                    val emojis = emojiChunks.getOrNull(page) ?: emptyList()
                    
                    // 3 columns x 5 rows grid manually
                    // Actually FlowRow is easier
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center,
                        maxItemsInEachRow = 5
                    ) {
                        emojis.forEach { emoji ->
                            val globalIndex = (page * emojisPerPage) + emojis.indexOf(emoji)
                            val isSelected = uiState.selectedPhotoIndex == globalIndex
                            
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateSelectedPhotoIndex(globalIndex) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 32.sp)
                            }
                        }
                    }
                }
                
                // Page Indicator
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 48.dp))
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.upload_photo_action), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontFamily = InterFontFamily,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSetupScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextLabel: String = "Next",
    nextEnabled: Boolean = false,
    isLoading: Boolean = false, // Added isLoading
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.create_group_final_action), 
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                    }
                },
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp)) {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    enabled = nextEnabled && !isLoading // Disable when loading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = nextLabel,
                            fontFamily = InterFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            
            content()

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
