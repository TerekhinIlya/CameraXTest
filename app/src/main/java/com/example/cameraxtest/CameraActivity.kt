package com.example.cameraxtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: Button
    private lateinit var switchCameraButton: Button
    private var imageAnalyzer: ImageAnalysis? = null

    private var isFrontCamera = true
    private var imageCapture: ImageCapture? = null

    private val outputDirectory: File by lazy {
        externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        } ?: filesDir
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
//        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
//        captureButton = findViewById(R.id.captureButton)
//        switchCameraButton = findViewById(R.id.switchCameraButton)

        checkPermissions()
//        setupListeners()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder().build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build().apply {
                    setAnalyzer(ContextCompat.getMainExecutor(this@CameraActivity), ImageAnalyzer())
                }

            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка запуска камеры: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            Log.d("CameraActivity", "Permissions granted")
            startCamera()
        } else {
            Log.d("CameraActivity", "Permissions not granted")
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS)
        }
    }

    private class ImageAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            // Пример обработки изображения
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            // TODO: добавьте анализ данных изображения
            Log.d("ImageAnalyzer", "Analyzing frame of size ${data.size}")

            image.close()
        }
    }

//    private fun setupListeners() {
//        captureButton.setOnClickListener {
//            imageCapture?.let { capturePhoto(it) }
//        }
//
//        switchCameraButton.setOnClickListener {
//            switchCamera()
//        }
//    }

//    private fun capturePhoto(imageCapture: ImageCapture) {
//        val photoFile = File(
//            outputDirectory,
//            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
//        )
//
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    Log.d("TAG", "Фото сохранено: ${photoFile.absolutePath}")
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    Log.d("TAG", "Ошибка при сохранении фото: ${exception.message}")
//                }
//            }
//        )
//    }
//
//    private fun switchCamera() {
//        isFrontCamera = !isFrontCamera
//        startCamera()
//    }
}