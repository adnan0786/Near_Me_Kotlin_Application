package com.example.nearmekotlindemo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.biometric.BiometricPrompt
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.nearmekotlindemo.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor

class SplashActivity : AppCompatActivity() {

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: androidx.biometric.BiometricPrompt.PromptInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = androidx.biometric.BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        this@SplashActivity,
                        "Authentication error $errString",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    Toast.makeText(
                        this@SplashActivity,
                        "Authentication failed ",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Finger print authentication required to login")
            .setNegativeButtonText("Use account password")
            .build()

        Handler(Looper.getMainLooper()).postDelayed({
            if (Firebase.auth.currentUser != null) {

                val finger = getSharedPreferences("user", MODE_PRIVATE)
                    .getBoolean("finger", false)
                if (finger) {
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }, 3000)
    }


}