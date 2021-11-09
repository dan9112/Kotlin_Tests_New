package ru.kotlin.tests.dictophone

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION
import android.widget.Button
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonRecord).setOnClickListener {
            getContent.launch(Intent(RECORD_SOUND_ACTION))
        }
    }

    private val getContent = registerForActivityResult(StartActivityForResult()) { result ->
        result.data?.let {
            makeText(this, "Saved: ${it.data!!.path}", LENGTH_LONG).show()
        }
    }
}
