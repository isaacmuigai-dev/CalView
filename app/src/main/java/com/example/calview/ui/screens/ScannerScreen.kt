package com.example.calview.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.calview.ai.model.FoodAnalysisResponse
import com.example.calview.ui.theme.CalAIGreen
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.viewmodels.ScannerUiState
import com.example.calview.ui.viewmodels.ScannerViewModel
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay & Controls
        ScannerOverlay(
            uiState = uiState,
            onClose = onClose,
            onCapture = {
                captureImage(imageCapture, cameraExecutor, context) { bitmap ->
                    viewModel.analyzeImage(bitmap)
                }
            },
            onLogMeal = {
                viewModel.logMeal(it)
            },
            onReset = { viewModel.reset() }
        )

        if (uiState is ScannerUiState.Logged) {
            LaunchedEffect(Unit) {
                onClose()
            }
        }
    }
}

@Composable
fun ScannerOverlay(
    uiState: ScannerUiState,
    onClose: () -> Unit,
    onCapture: () -> Unit,
    onLogMeal: (FoodAnalysisResponse) -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // App Bar / Close Button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.3f), MaterialTheme.shapes.extraLarge)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        // Bottom Controls / Analysis Result
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            when (uiState) {
                is ScannerUiState.Idle -> {
                    CaptureButton(onClick = onCapture, modifier = Modifier.align(Alignment.Center))
                }
                is ScannerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
                is ScannerUiState.Success -> {
                    AnalysisResultBottomSheet(
                        response = uiState.response,
                        onLogMeal = { 
                            onLogMeal(uiState.response)
                        },
                        onDismiss = onReset
                    )
                }
                is ScannerUiState.Error -> {
                    Text(
                        uiState.message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.White, MaterialTheme.shapes.medium)
                            .padding(16.dp)
                    )
                    Button(onClick = onReset, modifier = Modifier.padding(top = 72.dp)) {
                        Text("Retry")
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun CaptureButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.size(80.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        // Empty to look like a camera button
    }
}

@Composable
fun AnalysisResultBottomSheet(
    response: FoodAnalysisResponse,
    onLogMeal: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                response.detected_items.firstOrNull()?.name ?: "Food Detected",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${response.total.calories} kcal | ${response.total.protein}P ${response.total.carbs}C ${response.total.fats}F",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                response.health_insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Discard")
                }
                Button(
                    onClick = onLogMeal,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CalAIGreen)
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
            try {
                // Convert ImageProxy (YUV format) to Bitmap
                val yBuffer = image.planes[0].buffer
                val uBuffer = image.planes[1].buffer
                val vBuffer = image.planes[2].buffer
                
                val ySize = yBuffer.remaining()
                val uSize = uBuffer.remaining()
                val vSize = vBuffer.remaining()
                
                val nv21 = ByteArray(ySize + uSize + vSize)
                yBuffer.get(nv21, 0, ySize)
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)
                
                val yuvImage = android.graphics.YuvImage(
                    nv21,
                    android.graphics.ImageFormat.NV21,
                    image.width,
                    image.height,
                    null
                )
                val out = java.io.ByteArrayOutputStream()
                yuvImage.compressToJpeg(
                    android.graphics.Rect(0, 0, image.width, image.height),
                    100,
                    out
                )
                val imageBytes = out.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                image.close()
                onCaptured(bitmap)
            } catch (e: Exception) {
                Log.e("ScannerScreen", "Bitmap conversion failed", e)
                image.close()
            }
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("ScannerScreen", "Capture failed", exception)
        }
    })
}

@Preview(showBackground = true)
@Composable
fun ScannerScreenPreview() {
    CalViewTheme {
        // Note: This preview requires a ViewModel and camera access. In a real preview, you'd need to provide a mock ViewModel.
    }
}
