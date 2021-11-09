package ru.kotlin.tests.dictophone

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonRecord).setOnClickListener {
            getContent.launch(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let {
                val savedUri = it.data!!
                Toast.makeText(
                    this,
                    "Saved: " + savedUri.path, Toast.LENGTH_LONG
                ).show()
            }
        }
}