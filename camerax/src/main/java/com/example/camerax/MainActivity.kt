package com.example.camerax

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.camerax.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private var imageByteArray: ByteArray? = null

    private var toSettings: Boolean? = true
    private var launchAfterRationale = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            imageByteArray = it.getByteArray(keyImageByteArray)
            toSettings = if (it.containsKey(keyToSettingsFlag)) it.getBoolean(keyToSettingsFlag, true) else null
            launchAfterRationale = it.getBoolean(keyPermissionAskTwiceFlag, false)
        }

        if (ContextCompat.checkSelfPermission(
                this, CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) startWork()
        else requestPermissionLauncher.launch(CAMERA)

        viewBinding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            imageByteArray?.also {
                checkView.setImageBitmap(it.toBitmap())
            }

            // Set up the listeners for take photo and video capture buttons
            imageCaptureButton.setOnClickListener { takePhoto() }
            videoCaptureButton.setOnClickListener { captureVideo() }

            cameraExecutor = Executors.newSingleThreadExecutor()
        }
    }

    override fun onResume() {
        super.onResume()
        if (toSettings == null) {
            toSettings = false
        } else if (toSettings == false) {
            if (ContextCompat.checkSelfPermission(
                    this, CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedAction()
                toSettings = true
            } else {
                startWork()
            }
        }
    }

    private fun startWork() {
        toSettings = true
        launchAfterRationale = false
        startCamera()
    }

    private fun ByteArray.toBitmap() =
        with(BitmapFactory.decodeByteArray(this, Int.SIZE_BYTES, size - Int.SIZE_BYTES)) {
            val rotation = ByteBuffer.wrap(this@toBitmap, 0, Int.SIZE_BYTES).int
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startWork()
        else askPermission()
    }

    private fun askPermission() {
        when {
            shouldShowRequestPermissionRationale(CAMERA) -> {
                Log.i(TAG, "Rationale...")
                if (!launchAfterRationale) {
                    launchAfterRationale = true
                    requestPermissionLauncher.launch(CAMERA)
                } else {
                    notGrantedAction()
                }
            }

            toSettings == true -> {
                Log.i(TAG, "Rationale with action...")
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        data = Uri.fromParts("package", packageName, null)
                    }
                )
                toSettings = null
            }

            else -> {
                notGrantedAction()
            }
        }
    }

    @Synchronized
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
                private fun ByteBuffer.toByteArray(rotation: Int): ByteArray {
                    val rotationBytes = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(rotation).array()

                    rewind()    // Rewind the buffer to zero
                    val data = ByteArray(remaining())
                    get(data)   // Copy the buffer into a byte array

                    return rotationBytes + data // Return the byte array
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val buffer = image.planes[0].buffer
                    imageByteArray = buffer.toByteArray(image.imageInfo.rotationDegrees).apply {
                        viewBinding.checkView.setImageBitmap(toBitmap())
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(
            outState.apply {
                imageByteArray?.let { putByteArray(keyImageByteArray, it) }
                toSettings?.let { putBoolean(keyToSettingsFlag, it) }
                putBoolean(keyPermissionAskTwiceFlag, launchAfterRationale)
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val keyImageByteArray = "Key_0"
        private const val keyToSettingsFlag = "Key_1"
        private const val keyPermissionAskTwiceFlag = "Key_2"
    }
}
