package com.example.calview.feature.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executor
import java.util.concurrent.Executors

// Color palette for premium design
private val GradientStart = Color(0xFF1E3A5F)  // Darker navy blue
private val GradientEnd = Color(0xFF2D1F3D)    // Darker purple
private val AccentCyan = Color(0xFF00D4AA)
private val DarkText = Color(0xFF1F2937)
private val MutedText = Color(0xFF6B7280)

// Scan modes enum
enum class ScanMode {
    SCAN_FOOD, BARCODE, FOOD_LABEL, GALLERY
}

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onClose: () -> Unit,
    onFoodCaptured: () -> Unit = onClose // Default to onClose for backwards compatibility
) {
    val context = LocalContext.current
    
    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showPermissionDenied by remember { mutableStateOf(false) }
    
    // Tutorial state - collect from ViewModel
    val hasSeenTutorial by viewModel.hasSeenCameraTutorial.collectAsState()
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            showPermissionDenied = true
        }
    }
    
    // Check permission state
    when {
        hasCameraPermission && !hasSeenTutorial -> {
            // Permission granted but tutorial not seen - show best practices
            CameraBestPracticesScreen(
                onDismiss = {
                    viewModel.markTutorialSeen()
                }
            )
        }
        hasCameraPermission -> {
            // Permission granted and tutorial seen - show camera
            ScannerCameraContent(
                viewModel = viewModel,
                onClose = onClose,
                onFoodCaptured = onFoodCaptured
            )
        }
        showPermissionDenied -> {
            // Permission denied - show denied screen
            CameraPermissionDeniedScreen(
                onGoBack = onClose,
                onRetry = {
                    showPermissionDenied = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
        else -> {
            // Show permission onboarding screen
            CameraPermissionScreen(
                onGoBack = onClose,
                onGrantPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}


/**
 * Camera Permission Onboarding Screen - Premium design
 */
@Composable
fun CameraPermissionScreen(
    onGoBack: () -> Unit,
    onGrantPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onGoBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Camera icon with decorative ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Title
            Text(
                text = "Camera Access Required",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "CalViewAI needs camera access to scan your food and provide instant nutrition information.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Feature highlights
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionFeatureItem(
                    icon = Icons.Filled.Restaurant,
                    title = "Scan any food",
                    description = "Take a photo of your meal for instant analysis"
                )
                PermissionFeatureItem(
                    icon = Icons.Filled.QrCodeScanner,
                    title = "Barcode scanning",
                    description = "Scan product barcodes for nutrition info"
                )
                PermissionFeatureItem(
                    icon = Icons.Filled.Bolt,
                    title = "AI-powered",
                    description = "Get accurate calorie estimates in seconds"
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Grant permission button
            Button(
                onClick = onGrantPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = GradientStart,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Allow Camera Access",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Skip text
            TextButton(onClick = onGoBack) {
                Text(
                    text = "Maybe Later",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Camera Best Practices Screen - One-time tutorial for first-time users
 * Shows tips for positioning food for optimal AI scanning accuracy
 * Premium gradient design matching CameraPermissionScreen
 */
@Composable
fun CameraBestPracticesScreen(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header illustration with glassmorphism rings
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Title
            Text(
                text = "📸 Tips for Best Results",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Subtitle
            Text(
                text = "Follow these tips to get the most accurate nutrition analysis from our AI",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Tips cards with glassmorphism
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassTipCard(
                    icon = Icons.Filled.CenterFocusStrong,
                    title = "Center your food",
                    description = "Place the main item in the center of the viewfinder for best recognition"
                )
                
                GlassTipCard(
                    icon = Icons.Filled.WbSunny,
                    title = "Good lighting",
                    description = "Natural daylight works best. Avoid harsh shadows or dim environments"
                )
                
                GlassTipCard(
                    icon = Icons.Filled.CropFree,
                    title = "Show the full plate",
                    description = "Capture all food items to get accurate portion estimates"
                )
                
                GlassTipCard(
                    icon = Icons.Filled.ZoomIn,
                    title = "Fill the frame",
                    description = "Get close enough so food takes up 70-80% of the screen"
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pro tip with glassmorphism
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFCD34D), // Warm yellow
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pro tip: For mixed plates, try to spread items out so the AI can identify each component",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Start scanning button - White with gradient text
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = GradientStart,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Scanning",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GradientStart
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Skip option
            TextButton(onClick = onDismiss) {
                Text(
                    text = "I'll figure it out",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Glassmorphism-styled tip card for gradient backgrounds
 */
@Composable
private fun GlassTipCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun BestPracticeTipCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconTint.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}


/**
 * Permission Denied Screen
 */
@Composable
fun CameraPermissionDeniedScreen(
    onGoBack: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Warning icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = Color(0xFFEF4444)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Camera Permission Denied",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "To scan food and get nutrition information, please allow camera access in your device settings.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Try Again",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = onGoBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = "Go Back",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Main camera content - shown after permission is granted
 */
@Composable
fun ScannerCameraContent(
    viewModel: ScannerViewModel,
    onClose: () -> Unit,
    onFoodCaptured: () -> Unit = onClose
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
    
    // Photo picker launcher for gallery mode
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            // Convert URI to bitmap and analyze
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.analyzeImage(bitmap)
                }
            } catch (e: Exception) {
                Log.e("ScannerScreen", "Failed to load image from gallery", e)
            }
        }
        // Reset mode back to scan food after picker closes
        selectedMode = ScanMode.SCAN_FOOD
    }
    
    // Launch photo picker when gallery mode is selected
    LaunchedEffect(selectedMode) {
        if (selectedMode == ScanMode.GALLERY) {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

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
                    // Different capture behavior based on mode
                    when (selectedMode) {
                        ScanMode.BARCODE -> {
                            // Capture and scan barcode
                            captureImage(imageCapture, cameraExecutor, context) { bitmap ->
                                val image = InputImage.fromBitmap(bitmap, 0)
                                val scanner = BarcodeScanning.getClient()
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        val barcode = barcodes.firstOrNull()?.rawValue
                                        if (barcode != null) {
                                            viewModel.lookupBarcode(barcode)
                                        } else {
                                            viewModel.reset()
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("ScannerScreen", "Barcode scan failed", it)
                                    }
                            }
                        }
                        ScanMode.FOOD_LABEL -> {
                            // Capture and OCR
                            captureImage(imageCapture, cameraExecutor, context) { bitmap ->
                                val image = InputImage.fromBitmap(bitmap, 0)
                                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                recognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        viewModel.parseNutritionFromText(visionText.text)
                                    }
                                    .addOnFailureListener {
                                        Log.e("ScannerScreen", "OCR failed", it)
                                    }
                            }
                        }
                        else -> {
                            // Default: AI food analysis
                            captureImage(imageCapture, cameraExecutor, context) { bitmap ->
                                viewModel.analyzeImage(bitmap)
                            }
                        }
                    }
                },
                onLogMeal = { response ->
                    viewModel.logMeal(response)
                },
                onLogBarcodeProduct = { product ->
                    viewModel.logBarcodeProduct(product)
                },
                onLogOcrNutrition = { nutrition ->
                    viewModel.logOcrNutrition(nutrition)
                },
                onReset = { viewModel.reset() }
            )
            
            // Navigate to dashboard when analysis starts
            if (uiState is ScannerUiState.NavigateToDashboard) {
                LaunchedEffect(Unit) {
                    onFoodCaptured()
                }
            }
            
            if (uiState is ScannerUiState.Logged) {
                LaunchedEffect(Unit) {
                    onFoodCaptured()
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
    onLogBarcodeProduct: (ProductInfo) -> Unit,
    onLogOcrNutrition: (ParsedNutrition) -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar - Close and Info buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // Fixed: Avoid status bar overlap
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
                .fillMaxWidth()
                .navigationBarsPadding(), // Fixed: Avoid nav bar overlap
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
                is ScannerUiState.BarcodeResult -> {
                    BarcodeResultBottomSheet(
                        product = uiState.product,
                        onLog = { onLogBarcodeProduct(uiState.product) },
                        onDismiss = onReset
                    )
                }
                is ScannerUiState.OcrResult -> {
                    OcrResultBottomSheet(
                        nutrition = uiState.nutrition,
                        onLog = { onLogOcrNutrition(uiState.nutrition) },
                        onDismiss = onReset
                    )
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
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ScanModeTab(
            icon = Icons.Filled.CameraAlt,
            label = "Scan",
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
            icon = Icons.Filled.Description,
            label = "Label",
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
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Fixed: Avoid nav bar overlap
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

/**
 * Bottom sheet for displaying barcode scan results
 */
@Composable
fun BarcodeResultBottomSheet(
    product: ProductInfo,
    onLog: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Fixed: Avoid nav bar overlap
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Product Found",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = MutedText)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Product name
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkText,
                textAlign = TextAlign.Center
            )
            
            if (product.servingSize != null) {
                Text(
                    text = "Per 100g (serving: ${product.servingSize})",
                    fontSize = 12.sp,
                    color = MutedText
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nutrition info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionBadge("🔥", "${product.calories}", "cal", GradientStart)
                NutritionBadge("🥩", "${product.protein.toInt()}g", "protein", Color(0xFFEF4444))
                NutritionBadge("🍞", "${product.carbs.toInt()}g", "carbs", Color(0xFFF59E0B))
                NutritionBadge("🧈", "${product.fats.toInt()}g", "fat", Color(0xFF3B82F6))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Log button
            Button(
                onClick = onLog,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log This Product", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Bottom sheet for displaying OCR nutrition results
 */
@Composable
fun OcrResultBottomSheet(
    nutrition: ParsedNutrition,
    onLog: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Fixed: Avoid nav bar overlap
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nutrition Detected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = MutedText)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Nutrition info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionBadge("🔥", "${nutrition.calories}", "cal", GradientStart)
                NutritionBadge("🥩", "${nutrition.protein.toInt()}g", "protein", Color(0xFFEF4444))
                NutritionBadge("🍞", "${nutrition.carbs.toInt()}g", "carbs", Color(0xFFF59E0B))
                NutritionBadge("🧈", "${nutrition.fats.toInt()}g", "fat", Color(0xFF3B82F6))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Log button
            Button(
                onClick = onLog,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log This Food", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NutritionBadge(
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MutedText
        )
    }
}

