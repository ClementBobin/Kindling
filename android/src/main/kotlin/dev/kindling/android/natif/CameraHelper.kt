package dev.kindling.android.natif

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executor

// ─────────────────────────────────────────────
//  CameraConfig
// ─────────────────────────────────────────────

/**
 * Décrit la configuration de la session caméra.
 *
 * Presets :
 * - [CameraConfig.BackPhoto]  → caméra arrière, capture photo
 * - [CameraConfig.FrontPhoto] → caméra avant (selfie), capture photo
 */
data class CameraConfig(
    val lensFacing: Int                = CameraSelector.LENS_FACING_BACK,
    val captureMode: Int               = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
    val flashMode: Int                 = ImageCapture.FLASH_MODE_AUTO,
    val jpegQuality: Int               = 95
) {
    companion object {
        val BackPhoto  = CameraConfig(lensFacing = CameraSelector.LENS_FACING_BACK)
        val FrontPhoto = CameraConfig(lensFacing = CameraSelector.LENS_FACING_FRONT)
    }
}

// ─────────────────────────────────────────────
//  CaptureResult
// ─────────────────────────────────────────────

sealed class CaptureResult {
    data class Success(val file: File)        : CaptureResult()
    data class Error(val message: String)     : CaptureResult()
}

// ─────────────────────────────────────────────
//  CameraHelper
// ─────────────────────────────────────────────

/**
 * Helper CameraX centralisé.
 *
 * Nécessite dans `android/build.gradle.kts` :
 * ```kotlin
 * val cameraxVersion = "1.4.2"
 * implementation("androidx.camera:camera-core:$cameraxVersion")
 * implementation("androidx.camera:camera-camera2:$cameraxVersion")
 * implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
 * implementation("androidx.camera:camera-view:$cameraxVersion")
 * ```
 *
 * Permission requise : `android.permission.CAMERA`
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { CameraHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Démarrer un preview (depuis un Fragment/Activity)
 * cameraHelper.startPreview(
 *     context       = this,
 *     lifecycleOwner = this,
 *     previewView   = binding.previewView,
 *     config        = CameraConfig.BackPhoto
 * )
 *
 * // Capturer une photo
 * cameraHelper.capturePhoto(
 *     outputFile = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
 * ) { result ->
 *     when (result) {
 *         is CaptureResult.Success -> showPhoto(result.file)
 *         is CaptureResult.Error   -> showError(result.message)
 *     }
 * }
 *
 * // Libérer en onDestroy
 * cameraHelper.release()
 * ```
 */
class CameraHelper(context: Context) {

    internal val appContext = context.applicationContext
    internal var imageCapture: ImageCapture? = null
    internal var camera: Camera? = null

    // ── Preview + bind ────────────────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.CAMERA)
    fun startPreview(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        config: CameraConfig = CameraConfig.BackPhoto,
        onReady: ((Camera) -> Unit)? = null
    ) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()

            val selector = CameraSelector.Builder()
                .requireLensFacing(config.lensFacing)
                .build()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(config.captureMode)
                .setFlashMode(config.flashMode)
                .setJpegQuality(config.jpegQuality)
                .build()

            provider.unbindAll()
            camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            onReady?.invoke(camera!!)

        }, ContextCompat.getMainExecutor(context))
    }

    // ── Capture ───────────────────────────────────────────────────────────────

    fun capturePhoto(
        outputFile: File,
        executor: Executor = ContextCompat.getMainExecutor(appContext),
        onResult: (CaptureResult) -> Unit
    ) {
        val capture = imageCapture
            ?: return onResult(CaptureResult.Error("Camera not started — call startPreview first"))

        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        capture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onResult(CaptureResult.Success(outputFile))
            }
            override fun onError(exception: ImageCaptureException) {
                onResult(CaptureResult.Error(exception.message ?: "Capture failed"))
            }
        })
    }

    // ── Flash ─────────────────────────────────────────────────────────────────

    fun setFlashMode(mode: Int) {
        imageCapture?.flashMode = mode
    }

    fun toggleTorch(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    fun setZoom(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    // ── Availability ──────────────────────────────────────────────────────────

    fun hasCamera(context: Context, lensFacing: Int = CameraSelector.LENS_FACING_BACK): Boolean =
        ProcessCameraProvider.getInstance(context).get()
            .hasCamera(CameraSelector.Builder().requireLensFacing(lensFacing).build())

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun release() {
        imageCapture = null
        camera = null
    }
}