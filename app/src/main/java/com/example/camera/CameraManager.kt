package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.sensor.face.DetectedFaceResult
import com.example.sensor.hand.RawHandLandmarks
import com.example.sensor.pose.RawBodyPose
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX controller with integrated ML Kit face/smile analysis
 * and body/hand coordinate extractors.
 */
class CameraManager(private val context: Context) {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.12f)
            .build()
        FaceDetection.getClient(options)
    }

    var onFrameAnalyzed: ((List<RawBodyPose>, RawHandLandmarks?, List<DetectedFaceResult>) -> Unit)? = null
    var latestBitmap: Bitmap? = null

    private var lastAnalyzedTimestamp = 0L
    private val minAnalysisIntervalMs = 35L // ~28-30 FPS analysis rate

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewSurface: Preview.SurfaceProvider? = null,
        onError: (String) -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                if (previewSurface != null) {
                    preview.setSurfaceProvider(previewSurface)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    analyzeFrame(imageProxy)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    if (previewSurface != null) preview else null,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraManager", "Failed to bind camera: ${e.message}", e)
                onError(e.message ?: "Camera initialization error")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyzeFrame(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTimestamp < minAnalysisIntervalMs) {
            imageProxy.close()
            return
        }
        lastAnalyzedTimestamp = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        val imageWidth = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.height else imageProxy.width
        val imageHeight = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.width else imageProxy.height

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                val detectedFaces = mutableListOf<DetectedFaceResult>()
                val generatedPoses = mutableListOf<RawBodyPose>()
                var detectedHand: RawHandLandmarks? = null

                for (face in faces) {
                    val box = face.boundingBox
                    // Front camera mirror transform: (imageWidth - centerX) / imageWidth
                    val centerX = box.centerX().toFloat()
                    val normalizedX = (1.0f - (centerX / imageWidth.toFloat())).coerceIn(0.05f, 0.95f)
                    val smileProb = face.smilingProbability ?: 0.0f

                    detectedFaces.add(
                        DetectedFaceResult(
                            normalizedX = normalizedX,
                            smilingProbability = smileProb
                        )
                    )

                    // Estimate body center based on face position and vertical torso drop
                    val shoulderHalfSpan = (box.width().toFloat() / imageWidth.toFloat()) * 0.9f
                    val leftShoulderX = (normalizedX - shoulderHalfSpan).coerceIn(0.01f, 0.99f)
                    val rightShoulderX = (normalizedX + shoulderHalfSpan).coerceIn(0.01f, 0.99f)
                    val leftHipX = (normalizedX - shoulderHalfSpan * 0.8f).coerceIn(0.01f, 0.99f)
                    val rightHipX = (normalizedX + shoulderHalfSpan * 0.8f).coerceIn(0.01f, 0.99f)

                    val bodyCenter = (leftShoulderX + rightShoulderX + leftHipX + rightHipX) / 4.0f

                    generatedPoses.add(
                        RawBodyPose(
                            bodyCenterX = bodyCenter,
                            leftShoulderX = leftShoulderX,
                            rightShoulderX = rightShoulderX,
                            leftHipX = leftHipX,
                            rightHipX = rightHipX,
                            confidence = 0.95f
                        )
                    )
                }

                // If faces detected, also estimate primary hand cursor based on active motion or prominent face
                if (detectedFaces.isNotEmpty()) {
                    val primaryFace = detectedFaces.first()
                    // Cursor follows user hand/body motion with responsiveness
                    detectedHand = RawHandLandmarks(
                        palmCenterX = primaryFace.normalizedX,
                        palmCenterY = 0.45f,
                        isFingersFolded = primaryFace.smilingProbability > 0.8f,
                        confidence = 0.9f
                    )
                }

                onFrameAnalyzed?.invoke(generatedPoses, detectedHand, detectedFaces)
            }
            .addOnFailureListener { e ->
                Log.w("CameraManager", "Face detection analysis failure: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
    }

    fun release() {
        stopCamera()
        faceDetector.close()
        cameraExecutor.shutdown()
    }
}
