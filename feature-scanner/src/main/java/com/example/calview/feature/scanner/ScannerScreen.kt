package com.example.calview.feature.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Executors

// Scan modes enum
enum class ScanMode {
    SCAN_FOOD, BARCODE, FOOD_LABEL, GALLERY
}

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Selected scan mode
    var selectedMode by remember { mutableStateOf(ScanMode.SCAN_FOOD) }
    var flashEnabled by remember { mutableStateOf(false) }
    
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("ScannerScreen", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    // Toggle flash
    LaunchedEffect(flashEnabled) {
        camera?.cameraControl?.enableTorch(flashEnabled)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay with controls
        ScannerOverlayWithModes(
            uiState = uiState,
            selectedMode = selectedMode,
            flashEnabled = flashEnabled,
            onModeSelected = { selectedMode = it },
            onFlashToggle = { flashEnabled = !flashEnabled },
            onClose = onClose,
            onCapture = {
                captureImage(imageCapture, cameraExecutor, context) { bitmap ->
                    viewModel.analyzeImage(bitmap)
                }
            },
            onLogMeal = { response ->
                viewModel.logMeal(response)
            },
            onReset = { viewModel.reset() }
        )
        
        // Navigate to dashboard when analysis starts
        if (uiState is ScannerUiState.NavigateToDashboard) {
            LaunchedEffect(Unit) {
                onClose()
            }
        }
        
        if (uiState is ScannerUiState.Logged) {
            LaunchedEffect(Unit) {
                onClose()
            }
        }
    }
}

@Composable
fun ScannerOverlayWithModes(
    uiState: ScannerUiState,
    selectedMode: ScanMode,
    flashEnabled: Boolean,
    onModeSelected: (ScanMode) -> Unit,
    onFlashToggle: () -> Unit,
    onClose: () -> Unit,
    onCapture: () -> Unit,
    onLogMeal: (com.example.calview.core.ai.model.FoodAnalysisResponse) -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar - Close and Info buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close button (X)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            // Info button
            IconButton(
                onClick = { /* Show info */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = Color.White
                )
            }
        }
        
        // Center viewfinder - changes based on mode
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            when (selectedMode) {
                ScanMode.BARCODE -> {
                    // Barcode viewfinder - rectangular with text above
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Barcode Scanner",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(140.dp)
                                .border(3.dp, Color.White, RoundedCornerShape(12.dp))
                        )
                    }
                }
                else -> {
                    // Square corner brackets viewfinder for Scan Food and Food Label
                    CornerBracketsViewfinder()
                }
            }
        }
        
        // Bottom section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode selector tabs
            ModeTabsRow(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bottom controls - Flash and Capture
            when (uiState) {
                is ScannerUiState.Idle -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flash toggle
                        IconButton(
                            onClick = onFlashToggle,
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (flashEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "Flash",
                                tint = Color.White
                            )
                        }
                        
                        // Capture button - shutter style
                        ShutterButton(onClick = onCapture)
                        
                        // Empty space for balance
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
                is ScannerUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is ScannerUiState.Success -> {
                    AnalysisResultBottomSheet(
                        response = uiState.response,
                        onLogMeal = { onLogMeal(uiState.response) },
                        onDismiss = onReset
                    )
                }
                is ScannerUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            uiState.message,
                            color = Color.Red,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onReset) {
                            Text("Retry")
                        }
                    }
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CornerBracketsViewfinder() {
    val size = 280.dp
    val cornerLength = 40.dp
    val strokeWidth = 4.dp
    
    Box(
        modifier = Modifier.size(size)
    ) {
        // Top-left corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(cornerLength)
                .height(strokeWidth)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .width(strokeWidth)
                .height(cornerLength)
                .background(Color.White)
        )
        
        // Top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(cornerLength)
                .height(strokeWidth)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(strokeWidth)
                .height(cornerLength)
                .background(Color.White)
        )
        
        // Bottom-left corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(cornerLength)
                .height(strokeWidth)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(strokeWidth)
                .height(cornerLength)
                .background(Color.White)
        )
        
        // Bottom-right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(cornerLength)
                .height(strokeWidth)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .width(strokeWidth)
                .height(cornerLength)
                .background(Color.White)
        )
    }
}

@Composable
fun ModeTabsRow(
    selectedMode: ScanMode,
    onModeSelected: (ScanMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ScanModeTab(
            icon = Icons.Filled.CameraAlt,
            label = "Scan Food",
            isSelected = selectedMode == ScanMode.SCAN_FOOD,
            onClick = { onModeSelected(ScanMode.SCAN_FOOD) },
            modifier = Modifier.weight(1f)
        )
        ScanModeTab(
            icon = Icons.Filled.QrCodeScanner,
            label = "Barcode",
            isSelected = selectedMode == ScanMode.BARCODE,
            onClick = { onModeSelected(ScanMode.BARCODE) },
            modifier = Modifier.weight(1f)
        )
        ScanModeTab(
            icon = Icons.Filled.Label,
            label = "Food label",
            isSelected = selectedMode == ScanMode.FOOD_LABEL,
            onClick = { onModeSelected(ScanMode.FOOD_LABEL) },
            modifier = Modifier.weight(1f)
        )
        ScanModeTab(
            icon = Icons.Filled.PhotoLibrary,
            label = "Gallery",
            isSelected = selectedMode == ScanMode.GALLERY,
            onClick = { onModeSelected(ScanMode.GALLERY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ScanModeTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(70.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle with shutter icon pattern
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Shutter aperture style icon
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Capture",
                tint = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AnalysisResultBottomSheet(
    response: com.example.calview.core.ai.model.FoodAnalysisResponse,
    onLogMeal: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                response.detected_items.firstOrNull()?.name ?: "Food Detected",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${response.total.calories} kcal | ${response.total.protein}P ${response.total.carbs}C ${response.total.fats}F",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                response.health_insight,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Discard", color = Color.Black)
                }
                Button(
                    onClick = onLogMeal,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Log Meal")
                }
            }
        }
    }
}

private fun captureImage(
    imageCapture: ImageCapture,
    executor: Executor,
    context: Context,
    onCaptured: (Bitmap) -> Unit
) {
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            image.close()
            onCaptured(bitmap)
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("ScannerScreen", "Capture failed", exception)
        }
    })
}
