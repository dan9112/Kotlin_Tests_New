package ru.kotlin.tests.dictophone

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaRecorder.AudioEncoder.AMR_NB
import android.media.MediaRecorder.AudioSource.MIC
import android.media.MediaRecorder.OutputFormat.THREE_GPP
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val fileName by lazy { "${externalCacheDir!!.absolutePath}/audiorecordtest.3gp" }

    private val recordButton by lazy {
        RecordButton(this).apply {
            setOnClickListener {
                onRecord(isRecording)
                click()
            }
        }
    }
    private var recorder: MediaRecorder? = null

    private val playButton by lazy {
        PlayButton(this).apply {
            setOnClickListener {
                onPlay(isPlaying)
                click()
            }
        }
    }
    private var player: MediaPlayer? = null

    private val requestMultiplePermissions =
        registerForActivityResult(RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.e("MainActivity", "${it.key} = ${it.value}")
            }
        }

    private fun onRecord(start: Boolean) = if (!start) startRecording()
    else stopRecording()


    private fun onPlay(start: Boolean) = if (!start) startPlaying()
    else stopPlaying()


    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
                setOnCompletionListener {
                    playButton.click()
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.run {
            release()
            null
        }
    }

    @Suppress("DEPRECATION")// Для использования конструктора MediaRecorder(), который устарел в Android 12
    private fun startRecording() {
        recorder =
            (if (SDK_INT >= VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()).apply {
                setAudioSource(MIC)
                setOutputFormat(THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(AMR_NB)

                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e("MainActivity", "prepare() failed")
                }

                start()
            }
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            release()
            null
        }
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        requestMultiplePermissions.launch(arrayOf(Manifest.permission.RECORD_AUDIO))

        setContentView(LinearLayout(this).apply { addViews(recordButton, playButton) })
    }

    private fun ViewGroup.addViews(vararg views: View) {
        views.forEach {
            addView(
                it,
                LinearLayout.LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT,
                    0f
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        recorder?.run {
            release()
            null
        }
        player?.run {
            release()
            null
        }
    }
}
