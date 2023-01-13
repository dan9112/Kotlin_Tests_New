package com.example.camerax

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.camerax.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private var paused = false

    private var imageByteArray: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            ContextCompat.checkSelfPermission(
                this, CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }

            shouldShowRequestPermissionRationale(CAMERA) -> {
                Log.i(TAG, "Rationale...")
                requestPermissionLauncher.launch(CAMERA)
            }

            else -> {
                requestPermissionLauncher.launch(CAMERA)
            }
        }

        viewBinding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            imageByteArray = savedInstanceState?.getByteArray("imageByteArray")?.also {
                checkView.setImageBitmap(it.toImageBitmap())
            }

            // Set up the listeners for take photo and video capture buttons
            imageCaptureButton.setOnClickListener { takePhoto() }
            videoCaptureButton.setOnClickListener { captureVideo() }

            cameraExecutor = Executors.newSingleThreadExecutor()
        }
    }

    private fun ByteArray.toImageBitmap() = BitmapFactory.decodeByteArray(this, 0, size)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else if (paused) notGrantedAction()
    }

    private fun notGrantedAction() {
        Toast
            .makeText(this, "Permissions had not grant by the user.", Toast.LENGTH_SHORT)
            .show()
        finish()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this), object : OnImageCapturedCallback() {
                private fun Bitmap.toCorrectRotation(rotation: Int): Bitmap {
                    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
                }

                private fun ByteBuffer.toByteArray(): ByteArray {

                    rewind()    // Rewind the buffer to zero
                    val data = ByteArray(remaining())
                    get(data)   // Copy the buffer into a byte array
                    return data // Return the byte array
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val buffer = image.planes[0].buffer

                    val byteArray = buffer.toByteArray()
                    var bitmap = byteArray.toImageBitmap()
                    bitmap = bitmap.toCorrectRotation(image.imageInfo.rotationDegrees)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    imageByteArray = stream.toByteArray().also {
                        viewBinding.checkView.setImageBitmap(bitmap)
                    }
                }
            }
        )
    }

    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .apply {
                        setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .build()

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    with(cameraProvider) {
                        // Unbind use cases before rebinding
                        unbindAll()

                        // Bind use cases to camera
                        bindToLifecycle(this@MainActivity, cameraSelector, preview, imageCapture)
                    }
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }

            },
            ContextCompat.getMainExecutor(this)
        )
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(
            outState.apply {
                imageByteArray?.let { putByteArray("imageByteArray", imageByteArray) }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
