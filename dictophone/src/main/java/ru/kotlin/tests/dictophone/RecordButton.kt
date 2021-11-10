package ru.kotlin.tests.dictophone

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatButton

@SuppressLint("SetTextI18n")
class RecordButton(context: Context) : AppCompatButton(context) {
    var isRecording = false

    fun click() {
            text = if (isRecording) "Start recording"
            else "Stop recording"
            isRecording = !isRecording
        }

    init {
        text = "Start recording"
    }
}