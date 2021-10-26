package lord.kotlin.webview_advanced

import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import lord.kotlin.webview_advanced.databinding.ActivityMainBinding
import lord.kotlin.webview_advanced.databinding.CustomActionBarBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->// если пользователь нажал "Back", то он ничего не выбрал и вернётся null
            uri?.let { binding.webView.loadUrl(uri.toString()) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentTypeface =
            Typeface.createFromAsset(assets, "fonts/wooden_ship_decorated_[allfont.ru].ttf")
        createCustomActionBar(currentTypeface)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                webView.apply {
                    setBackgroundColor(0)
                    settings.apply {
                        builtInZoomControls = true
                        useWideViewPort = true
                        setInitialScale(1)
                    }

                    webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            galleryButton.isEnabled = false
                            localButton.isEnabled = false
                            webButton.isEnabled = false
                        }

                        override fun onPageFinished(view: WebView, url: String?) {
                            super.onPageFinished(view, url)
                            galleryButton.isEnabled = true
                            localButton.isEnabled = true
                            webButton.isEnabled = true
                        }
                    }
                }
                galleryButton.apply {
                    typeface = currentTypeface
                    setOnClickListener(this@MainActivity)
                }
                localButton.apply {
                    typeface = currentTypeface
                    setOnClickListener(this@MainActivity)
                }
                webButton.apply {
                    typeface = currentTypeface
                    setOnClickListener(this@MainActivity)
                }
            }
    }

    private fun createCustomActionBar(currentTypeface: Typeface) {
        supportActionBar?.let {
            it.apply {
                setDisplayShowCustomEnabled(true)
                setDisplayShowTitleEnabled(false)

                val binding = DataBindingUtil.inflate<CustomActionBarBinding>(
                    LayoutInflater.from(this@MainActivity),
                    R.layout.custom_action_bar,
                    null,
                    false
                )
                binding.title.typeface = currentTypeface

                customView = binding.root
            }
        }
    }

    override fun onClick(view: View) {
        binding.apply {
            when (view.id) {
                R.id.gallery_button -> getContent.launch("image/*")
                R.id.web_button -> webView.loadUrl("http://developer.alexanderklimov.ru/android/images/updowncat.jpg")
                R.id.local_button -> webView.loadUrl("file:///android_asset/images/1.png")
            }
        }
    }
}