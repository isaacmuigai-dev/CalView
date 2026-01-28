package com.example.calview.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.calview.core.data.exercise.ExerciseTemplate
import com.example.calview.core.data.local.ExerciseEntity
import com.example.calview.core.data.local.ExerciseType
import com.example.calview.core.ui.util.AdaptiveLayoutUtils
import com.example.calview.core.ui.util.LocalWindowSizeClass
import com.example.calview.core.ui.util.rememberHapticsManager
import com.example.calview.feature.dashboard.components.VisualIntensitySlider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogExerciseScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = rememberHapticsManager()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Handle save success/error
    LaunchedEffect(uiState.saveSuccess, uiState.saveError) {
        if (uiState.saveSuccess) {
            haptics.success()
            snackbarHostState.showSnackbar("Exercise logged successfully!")
            viewModel.clearSaveState()
        }
        uiState.saveError?.let { error ->
            haptics.error()
            snackbarHostState.showSnackbar(error)
            viewModel.clearSaveState()
        }
    }
    
    // Adaptive layout
    val windowSizeClass = LocalWindowSizeClass.current
    val horizontalPadding = AdaptiveLayoutUtils.getHorizontalPadding(windowSizeClass.widthSizeClass)
    val maxContentWidth = AdaptiveLayoutUtils.getMaxContentWidth(windowSizeClass.widthSizeClass)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Log Exercise",
                        modifier = Modifier.semantics { heading() }
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { 
                            contentDescription = "Navigate back to previous screen"
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxSize()
            ) {
                // Tab selector
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.semantics {
                        contentDescription = "Exercise logging tabs: Search, AI Smart, and Today's exercises"
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Search") },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.semantics {
                            contentDescription = "Search tab: Find and log exercises from database"
                            stateDescription = if (selectedTabIndex == 0) "Selected" else "Not selected"
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI Smart")
                                if (uiState.isPremium) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFFFD700)
                                    )
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                        modifier = Modifier.semantics {
                            contentDescription = "AI Smart tab: Use AI to parse exercise descriptions. ${if (uiState.isPremium) "Premium feature enabled" else "Premium feature required"}"
                            stateDescription = if (selectedTabIndex == 1) "Selected" else "Not selected"
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Today") },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                        modifier = Modifier.semantics {
                            contentDescription = "Today tab: View today's logged exercises"
                            stateDescription = if (selectedTabIndex == 2) "Selected" else "Not selected"
                        }
                    )
                }
                
                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> SearchExerciseTab(
                        uiState = uiState,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onTypeFilter = viewModel::filterByType,
                        onExerciseSelect = viewModel::selectExercise,
                        onDurationChange = viewModel::updateDuration,
                        onIntensityChange = viewModel::updateIntensity,
                        onSave = viewModel::saveManualExercise,
                        onClearSelection = viewModel::clearSelectedExercise,
                        horizontalPadding = horizontalPadding
                    )
                    1 -> AiSmartTab(
                        uiState = uiState,
                        onInputChange = viewModel::updateAiInputText,
                        onParse = viewModel::parseExerciseWithAi,
                        onSave = viewModel::saveAiParsedExercises,
                        onClearError = viewModel::clearAiError,
                        horizontalPadding = horizontalPadding
                    )
                    2 -> TodaysExercisesTab(
                        exercises = uiState.todaysExercises,
                        totalCalories = uiState.todaysTotalCalories,
                        onDelete = viewModel::deleteExercise,
                        horizontalPadding = horizontalPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchExerciseTab(
    uiState: ExerciseUiState,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilter: (ExerciseType?) -> Unit,
    onExerciseSelect: (ExerciseTemplate) -> Unit,
    onDurationChange: (Int) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onSave: () -> Unit,
    onClearSelection: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
    ) {
        // If exercise is selected, show configuration
        if (uiState.selectedExercise != null) {
            ExerciseConfigCard(
                exercise = uiState.selectedExercise,
                duration = uiState.durationMinutes,
                intensity = uiState.intensity,
                estimatedCalories = uiState.estimatedCalories,
                isLoading = uiState.isLoading,
                onDurationChange = onDurationChange,
                onIntensityChange = onIntensityChange,
                onSave = onSave,
                onCancel = onClearSelection
            )
        } else {
            // Search field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Search exercises. Type to filter the exercise list"
                    },
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.semantics {
                                contentDescription = "Clear search query"
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Type filter chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Filter exercises by type"
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedType == null,
                    onClick = { onTypeFilter(null) },
                    label = { Text("All") },
                    modifier = Modifier.semantics {
                        stateDescription = if (uiState.selectedType == null) "Selected" else "Not selected"
                    }
                )
                ExerciseType.entries.forEach { type ->
                    val typeName = type.name.lowercase().replaceFirstChar { it.uppercase() }
                    FilterChip(
                        selected = uiState.selectedType == type,
                        onClick = { onTypeFilter(type) },
                        label = { Text(typeName) },
                        leadingIcon = {
                            Icon(
                                imageVector = getExerciseTypeIcon(type),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Filter by $typeName exercises"
                            stateDescription = if (uiState.selectedType == type) "Selected" else "Not selected"
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Exercise list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredExercises) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        onClick = { onExerciseSelect(exercise) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    exercise: ExerciseTemplate,
    onClick: () -> Unit
) {
    val typeName = exercise.type.name.lowercase().replaceFirstChar { it.uppercase() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                onClickLabel = "Select ${exercise.name} to configure and log"
            )
            .semantics {
                contentDescription = "${exercise.name}, ${typeName} exercise, MET value ${String.format("%.1f", exercise.metValue)}. Tap to select and configure this exercise"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getExerciseTypeIcon(exercise.type),
                contentDescription = null, // Covered by parent semantics
                tint = getExerciseTypeColor(exercise.type),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "MET: ${String.format("%.1f", exercise.metValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null, // Covered by parent semantics
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ExerciseConfigCard(
    exercise: ExerciseTemplate,
    duration: Int,
    intensity: Float,
    estimatedCalories: Int,
    isLoading: Boolean,
    onDurationChange: (Int) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val haptics = rememberHapticsManager()
    val typeName = exercise.type.name.lowercase().replaceFirstChar { it.uppercase() }
    val intensityLabel = when {
        intensity < 0.2f -> "Very Light"
        intensity < 0.4f -> "Light"
        intensity < 0.6f -> "Moderate"
        intensity < 0.8f -> "High"
        else -> "Maximum"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Configure ${exercise.name} exercise. Duration: $duration minutes, Intensity: $intensityLabel, Estimated calories: $estimatedCalories"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Exercise header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getExerciseTypeIcon(exercise.type),
                    contentDescription = null,
                    tint = getExerciseTypeColor(exercise.type),
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = typeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.semantics {
                        contentDescription = "Cancel exercise configuration and return to search"
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Duration slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Duration", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$duration min",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
            }
            
            Slider(
                value = duration.toFloat(),
                onValueChange = { 
                    val newDuration = it.toInt()
                    if (newDuration != duration) {
                        haptics.tick()
                    }
                    onDurationChange(newDuration)
                },
                valueRange = 5f..180f,
                steps = 34, // 5-minute increments
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Exercise duration slider. Current value: $duration minutes. Range 5 to 180 minutes"
                        stateDescription = "$duration minutes"
                    }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Intensity slider
            VisualIntensitySlider(
                value = intensity,
                onValueChange = onIntensityChange,
                showLabels = true,
                showCalories = true,
                estimatedCalories = estimatedCalories
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
            Button(
                onClick = {
                    haptics.click()
                    onSave()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = if (isLoading) "Saving exercise, please wait" else "Log exercise: ${exercise.name}, $duration minutes, $estimatedCalories calories"
                    },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Exercise", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AiSmartTab(
    uiState: ExerciseUiState,
    onInputChange: (String) -> Unit,
    onParse: () -> Unit,
    onSave: () -> Unit,
    onClearError: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val focusManager = LocalFocusManager.current
    val haptics = rememberHapticsManager()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Premium badge or lock
        if (!uiState.isPremium) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Premium feature required. Upgrade to CalView Premium to use AI-powered exercise logging"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Premium Feature",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            "Upgrade to use AI-powered exercise logging",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // AI input card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "AI workout analyzer. Describe your workout in natural language and AI will calculate calories burned"
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (uiState.isPremium) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Describe your workout",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = uiState.aiInputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .semantics {
                            contentDescription = "Workout description input. ${if (!uiState.isPremium) "Disabled. Premium required." else "Enter your workout description here."}"
                        },
                    enabled = uiState.isPremium,
                    placeholder = { 
                        Text("e.g., \"Ran 5km in 30 mins\" or \"Did a heavy chest workout for an hour\"")
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        focusManager.clearFocus()
                        if (uiState.isPremium) onParse()
                    }),
                    shape = RoundedCornerShape(12.dp)
                )
                
                // Error message
                AnimatedVisibility(visible = uiState.aiError != null) {
                    uiState.aiError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .semantics {
                                    liveRegion = LiveRegionMode.Assertive
                                    contentDescription = "Error: $error"
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        haptics.click()
                        focusManager.clearFocus()
                        onParse()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = when {
                                !uiState.isPremium -> "Analyze workout button. Disabled. Premium required."
                                uiState.isAiParsing -> "Analyzing workout, please wait"
                                uiState.aiInputText.isBlank() -> "Analyze workout button. Disabled. Enter a workout description first."
                                else -> "Analyze workout with AI"
                            }
                        },
                    enabled = uiState.isPremium && uiState.aiInputText.isNotBlank() && !uiState.isAiParsing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isAiParsing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Workout")
                    }
                }
            }
        }
        
        // Parsed exercises
        if (uiState.aiParsedExercises.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Detected Exercises",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { 
                    heading()
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Detected ${uiState.aiParsedExercises.size} exercises from your description"
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            uiState.aiParsedExercises.forEachIndexed { index, parsed ->
                val durationText = parsed.duration_minutes?.let { "$it minutes" } ?: ""
                val intensityText = parsed.intensity.replaceFirstChar { it.uppercase() }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .semantics {
                            contentDescription = "Exercise ${index + 1}: ${parsed.name}. $durationText, $intensityText intensity, ${parsed.estimated_calories} calories"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = parsed.name,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row {
                                parsed.duration_minutes?.let {
                                    Text(
                                        text = "$it min",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = intensityText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Text(
                            text = "${parsed.estimated_calories} cal",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total and save button
            val totalCalories = uiState.aiParsedExercises.sumOf { it.estimated_calories }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = "Total calories burned: $totalCalories calories"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Calories Burned")
                    Text(
                        text = "$totalCalories cal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    haptics.heavyClick()
                    onSave()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = if (uiState.isLoading) 
                            "Saving exercises, please wait" 
                        else 
                            "Log all ${uiState.aiParsedExercises.size} exercises totaling $totalCalories calories"
                    },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log All Exercises", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Example prompts
        Text(
            "Try saying:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val examples = listOf(
            "\"Ran 5km in 30 minutes\"",
            "\"Did HIIT for 20 minutes, it was intense\"",
            "\"Hour of basketball with friends\"",
            "\"30 mins yoga followed by stretching\""
        )
        
        examples.forEach { example ->
            Text(
                text = "• $example",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun TodaysExercisesTab(
    exercises: List<ExerciseEntity>,
    totalCalories: Int,
    onDelete: (ExerciseEntity) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    val haptics = rememberHapticsManager()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
    ) {
        // Summary card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Today's summary: $totalCalories calories burned from ${exercises.size} ${if (exercises.size == 1) "exercise" else "exercises"}"
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$totalCalories",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "calories burned today",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${exercises.size} exercise${if (exercises.size != 1) "s" else ""} logged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .semantics {
                        contentDescription = "No exercises logged today. Use the Search or AI Smart tabs to log your first workout"
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No exercises logged today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Search or use AI to log your first workout!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = "List of today's logged exercises"
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises) { exercise ->
                    val typeName = exercise.type.name.lowercase().replaceFirstChar { it.uppercase() }
                    val aiText = if (exercise.isAiGenerated) ", logged with AI" else ""
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "${exercise.name}, ${exercise.durationMinutes} minutes, ${exercise.caloriesBurned} calories, $typeName exercise$aiText"
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getExerciseTypeIcon(exercise.type),
                                contentDescription = null,
                                tint = getExerciseTypeColor(exercise.type),
                                modifier = Modifier.size(40.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = exercise.name,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (exercise.isAiGenerated) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFFFD700)
                                        )
                                    }
                                }
                                Row {
                                    Text(
                                        text = "${exercise.durationMinutes} min",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        " • ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${exercise.caloriesBurned} cal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    haptics.click()
                                    onDelete(exercise)
                                },
                                modifier = Modifier.semantics {
                                    contentDescription = "Delete ${exercise.name} exercise"
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
private fun getExerciseTypeIcon(type: ExerciseType): ImageVector {
    return when (type) {
        ExerciseType.CARDIO -> Icons.Default.DirectionsRun
        ExerciseType.STRENGTH -> Icons.Default.FitnessCenter
        ExerciseType.FLEXIBILITY -> Icons.Default.SelfImprovement
        ExerciseType.SPORT -> Icons.Default.SportsBaseball
        ExerciseType.OTHER -> Icons.Default.DirectionsBike
    }
}

private fun getExerciseTypeColor(type: ExerciseType): Color {
    return when (type) {
        ExerciseType.CARDIO -> Color(0xFFE53935)
        ExerciseType.STRENGTH -> Color(0xFF1E88E5)
        ExerciseType.FLEXIBILITY -> Color(0xFF43A047)
        ExerciseType.SPORT -> Color(0xFFFF9800)
        ExerciseType.OTHER -> Color(0xFF9C27B0)
    }
}
