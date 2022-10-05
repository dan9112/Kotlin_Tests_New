package com.example.mvp_example

import android.content.ClipData.newPlainText
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.mvp_example.databinding.ActivityMainBinding


private const val textKey = "currentText"

class MainActivity : AppCompatActivity() {
    private lateinit var presenter: IMainActivityPresenter
    private lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(ActivityMainBinding.inflate(layoutInflater)) {
            setContentView(root)

            clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            presenter = MainActivityPresenter(IMainActivityView)

            button.setOnClickListener { presenter.changeText() }

            with(textView) {
                savedInstanceState?.let { text = it.getCharSequence(textKey) }
                setOnClickListener {
                    clipboardManager.setPrimaryClip(newPlainText("Saved text from text view", text))
                    makeText(this@MainActivity, "Copied", LENGTH_SHORT).show()
                }
            }
        }
    }

    private val ActivityMainBinding.IMainActivityView
        get() = object : IMainActivityView {

            override fun setText(text: String) {
                textView.text = text
            }

            override fun disableUI() {
                button.isEnabled = false
                textView.isEnabled = false
                progressBar.isVisible = true
            }

            override fun enableUI() {
                button.isEnabled = true
                textView.isEnabled = true
                progressBar.isVisible = false
            }

            override fun getCurrentText() = textView.text
        }

    override fun onSaveInstanceState(outState: Bundle) = super.onSaveInstanceState(
        outState.apply {
            putCharSequence(textKey, presenter.currentText)
        }
    )
}
