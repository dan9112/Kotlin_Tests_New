package com.example.camerax

import android.Manifest.permission.CAMERA
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.camera.video.VideoRecordEvent.Start
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.camerax.MainActivity.Companion.BytesConverter.toByteArray
import com.example.camerax.MainActivity.Companion.BytesConverter.toRotatedBitmap
import com.example.camerax.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "MainActivity"

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private var imageByteArray: ByteArray? = null

    private var toSettings: Boolean? = true
    private var launchAfterRationale = false

    private var photoAction = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            imageByteArray = it.getByteArray(keyImageByteArray)
            toSettings = if (it.containsKey(keyToSettingsFlag)) it.getBoolean(keyToSettingsFlag, true) else null
            launchAfterRationale = it.getBoolean(keyPermissionAskTwiceFlag, false)
        }

        viewBinding = ActivityMainBinding.inflate(layoutInflater).apply {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity, CAMERA
                ) == PERMISSION_GRANTED
            ) startWork()
            else requestPermissionLauncher.launch(CAMERA)
            setContentView(root)
            imageByteArray?.also {
                checkView.setImageBitmap(it.toRotatedBitmap())
                imageViewContainer.isVisible = true
            }

            // Set up the listeners for take photo and video capture buttons
            mainButton.setOnClickListener {
                if (photoAction) takePhoto()
                else captureVideo()
            }
            mainButton.setOnLongClickListener {
                photoAction = !photoAction
                mainButton.text = getString(if (photoAction) R.string.take_photo else R.string.start_capture)
                if (luminosity.isVisible) luminosity.isVisible = false
                startWork()
                true
            }

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
                ) != PERMISSION_GRANTED
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
            cameraExecutor,
            object : OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val buffer = image.planes[0].buffer
                    imageByteArray = (image.imageInfo.rotationDegrees.toByteArray() + buffer.toByteArray()).apply {
                        image.close()
                        val bitmap = toRotatedBitmap()
                        runOnUiThread {
                            with(viewBinding) {
                                checkView.setImageBitmap(bitmap)
                                imageViewContainer.isVisible = true
                            }
                        }
                    }
                }
            }
        )
    }

    private fun captureVideo() {
        val videoCapture = videoCapture ?: return

        viewBinding.mainButton.isEnabled = false

        val curRecording = recording
        curRecording?.let {
            it.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture
            .output
            .prepareRecording(this, mediaStoreOutputOptions)
            .start(cameraExecutor) { recordEvent ->
                when (recordEvent) {
                    is Start -> {
                        runOnUiThread {
                            with(viewBinding.mainButton) {
                                text = getString(R.string.stop_capture)
                                isEnabled = true
                            }
                        }
                    }

                    is Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            runOnUiThread {
                                Toast
                                    .makeText(baseContext, msg, Toast.LENGTH_SHORT)
                                    .show()
                            }
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(
                                TAG, "Video capture ends with error: " +
                                        "${recordEvent.error}"
                            )
                        }
                        runOnUiThread {
                            with(viewBinding.mainButton) {
                                text = getString(R.string.start_capture)
                                isEnabled = true
                            }
                        }
                    }
                }
            }
    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview
                    .Builder()
                    .build()
                    .apply {
                        setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                    }

                val recorder = Recorder
                    .Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HIGHEST,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                        )
                    )
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                imageCapture = ImageCapture
                    .Builder()
                    .build()

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    with(cameraProvider) {
                        // Unbind use cases before rebinding
                        unbindAll()

                        // Bind use cases to camera
                        if (photoAction) {
                            val imageAnalyzer = ImageAnalysis
                                .Builder()
                                .build()
                                .also {
                                    it.setAnalyzer(
                                        cameraExecutor,
                                        LuminosityAnalyzer { luma ->
                                            runOnUiThread {
                                                with(viewBinding.luminosity) {
                                                    if (!isVisible) isVisible = true
                                                    text = getString(
                                                        R.string.luminosity_output,
                                                        luma.format(3)
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            bindToLifecycle(this@MainActivity, cameraSelector, preview, imageCapture, imageAnalyzer)
                        } else {
                            bindToLifecycle(this@MainActivity, cameraSelector, preview, videoCapture)
                        }
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

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private const val keyImageByteArray = "Key_0"
        private const val keyToSettingsFlag = "Key_1"
        private const val keyPermissionAskTwiceFlag = "Key_2"
        private fun Double.format(afterDots: Int): String {
            val string = DecimalFormat("#.###").format(this).format(3)
            return with(string) {
                val delimiter = when {// Не нашёл как получить от системы
                    contains(',') -> ","
                    contains('.') -> "."
                    else -> with((this@format + 0.1).toString()) {
                        get(lastIndex - 1).toString()
                    }
                }
                val before = substringBefore(delimiter)
                val after = StringBuilder(substringAfter(delimiter, ""))
                while (after.length < afterDots) {
                    after.append('0')
                }
                "$before$delimiter$after"
            }
        }

        private object BytesConverter {
            fun Int.toByteArray(): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()

            private fun ByteArray.toInt(): Int = ByteBuffer.wrap(this).int

            private fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, size)

            fun ByteBuffer.toByteArray(): ByteArray {
                rewind()    // Rewind the buffer to zero
                val data = ByteArray(remaining())
                get(data)   // Copy the buffer into a byte array
                return data // Return the byte array
            }

            fun ByteArray.toRotatedBitmap(): Bitmap =
                with(copyOfRange(Int.SIZE_BYTES, size - Int.SIZE_BYTES).toBitmap()) {
                    val rotation = copyOfRange(0, Int.SIZE_BYTES).toInt()
                    val matrix = Matrix().apply { setRotate(rotation.toFloat()) }
                    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
                }
        }
    }
}
