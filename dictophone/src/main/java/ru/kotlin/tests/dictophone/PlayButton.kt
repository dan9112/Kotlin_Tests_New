package ru.kotlin.tests.dictophone

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatButton

@SuppressLint("SetTextI18n")
class PlayButton(context: Context) : AppCompatButton(context) {
    var isPlaying = false

    fun click() {
            text = if (isPlaying) "Start playing"
            else "Stop playing"

            isPlaying = !isPlaying
        }

    init {
        text = "Start playing"
    }
}