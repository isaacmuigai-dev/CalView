package com.example.calview.core.ml

import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.example.calview.core.ml.model.DetectedObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * ML Kit based object detector for finding food items in camera frames
 */
@Singleton
class FoodObjectDetector @Inject constructor() {
    
    private val detector: ObjectDetector
    
    init {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .enableMultipleObjects()
            .build()
        detector = ObjectDetection.getClient(options)
    }
    
    /**
     * Detect objects in an ImageProxy from CameraX
     * Filters to only return objects classified as "Food"
     */
    @androidx.camera.core.ExperimentalGetImage
    suspend fun detect(imageProxy: ImageProxy): List<DetectedObject> {
        val mediaImage = imageProxy.image ?: return emptyList()
        
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        return suspendCancellableCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { detectedObjects ->
                    val foods = detectedObjects
                        .filter { obj ->
                            // Filter for objects classified as "Food" or high-confidence objects
                            obj.labels.any { label -> 
                                label.text.equals("Food", ignoreCase = true) ||
                                label.text.equals("Food item", ignoreCase = true)
                            } || obj.labels.isEmpty() // Also include unclassified objects
                        }
                        .map { obj ->
                            DetectedObject(
                                boundingBox = RectF(obj.boundingBox),
                                trackingId = obj.trackingId,
                                category = obj.labels.firstOrNull()?.text
                            )
                        }
                    continuation.resume(foods)
                }
                .addOnFailureListener { 
                    continuation.resume(emptyList())
                }
        }
    }
    
    /**
     * Detect all objects (not filtered by category)
     */
    @androidx.camera.core.ExperimentalGetImage
    suspend fun detectAll(imageProxy: ImageProxy): List<DetectedObject> {
        val mediaImage = imageProxy.image ?: return emptyList()
        
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        return suspendCancellableCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { detectedObjects ->
                    val objects = detectedObjects.map { obj ->
                        DetectedObject(
                            boundingBox = RectF(obj.boundingBox),
                            trackingId = obj.trackingId,
                            category = obj.labels.firstOrNull()?.text
                        )
                    }
                    continuation.resume(objects)
                }
                .addOnFailureListener { 
                    continuation.resume(emptyList())
                }
        }
    }
    
    fun close() {
        detector.close()
    }
}
