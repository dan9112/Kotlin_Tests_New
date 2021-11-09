package ru.kotlin_tests.biometric_autentification

import android.app.KeyguardManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var biometricManager: BiometricManager
    private val tvMessage by lazy { findViewById<TextView>(R.id.tvMessage) }
    private val btnAuthenticate by lazy { findViewById<Button>(R.id.btnAuthenticate) }
    private val keyguardManager by lazy { getSystemService(KEYGUARD_SERVICE) as KeyguardManager }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) showMessage("Authentication successful")
            else showMessage("Incorrect key")
        }

    private val authCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            showMessage("Authentication successful")
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            if (errString == getString(R.string.biometric_auth_button_another_way)) askStandardMethod(
                SDK_INT >= VERSION_CODES.R
            )
            else showMessage("Unrecoverable error => $errString")
        }

        override fun onAuthenticationFailed() {
            showMessage("Could not recognise the user")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAuthenticate.setOnClickListener { authenticateUser() }
        authenticateUser()
    }

    private fun authenticateUser() {
        biometricManager = from(this)

        val mode = SDK_INT >= VERSION_CODES.R

        when (if (mode) biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) else biometricManager.canAuthenticate(
            BIOMETRIC_STRONG
        )) {
            BIOMETRIC_SUCCESS -> showBiometricPrompt(mode)
            BIOMETRIC_ERROR_NO_HARDWARE -> askStandardMethod(mode)
            BIOMETRIC_ERROR_HW_UNAVAILABLE -> showMessage("The hardware is unavailable. Try again later")
            BIOMETRIC_ERROR_NONE_ENROLLED -> askStandardMethod(mode)
            BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> showMessage("A security vulnerability has been discovered with one or more hardware sensors")
            BIOMETRIC_ERROR_UNSUPPORTED -> showMessage("The specified options are incompatible with the current Android version")
            BIOMETRIC_STATUS_UNKNOWN -> askStandardMethod(mode)
        }
    }

    @Suppress("DEPRECATION")// Метод createConfirmDeviceCredentialIntent устарел на API 29, но студия не убирает предупреждение
    private fun askStandardMethod(mode: Boolean) {
        if (!keyguardManager.isDeviceSecure) showMessage("Authentication successful")
        else {
            if (!mode) {
                if (SDK_INT < VERSION_CODES.Q) {
                    val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                        "Unlock",
                        "Confirm your screen lock PIN, Pattern or Password"
                    )
                    getContent.launch(intent)
                } else showStandardAuthApi29()
            }
        }
    }

    /** Внимание!
     *
     * Функция не проверена на Android OS соответствующей версии */
    private fun showStandardAuthApi29() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("AndroidX Biometric")
            setSubtitle("Authenticate user via Biometric")
            setDescription("Please authenticate yourself here")
            setAllowedAuthenticators(DEVICE_CREDENTIAL)
            setNegativeButtonText(getString(R.string.biometric_auth_button_another_way))
        }.build()

        BiometricPrompt(this, authCallback).authenticate(promptInfo)
    }

    private fun showBiometricPrompt(mode: Boolean) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("AndroidX Biometric")
            setSubtitle("Authenticate user via Biometric")
            setDescription("Please authenticate yourself here")
            if (mode) setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            else {
                setAllowedAuthenticators(BIOMETRIC_STRONG)
                setNegativeButtonText(getString(R.string.biometric_auth_button_another_way))
            }
            setConfirmationRequired(false)
        }.build()

        val executor = ContextCompat.getMainExecutor(this)

        BiometricPrompt(this, executor, authCallback).authenticate(promptInfo)
    }

    private fun showMessage(message: String) {
        Log.d("MainActivity", "Message: $message")
        tvMessage.text = message
        makeText(this, message, LENGTH_LONG).show()
    }
}