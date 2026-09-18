package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.screens.SplashLoginScreen
import com.example.ui.theme.YallaMeshwarTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SplashActivity : ComponentActivity() {
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            auth = FirebaseAuth.getInstance()
        } catch (_: Exception) {
            auth = null
        }

        setContent {
            YallaMeshwarTheme {
                SplashLoginScreen(
                    onSendOtp = { phoneNumber ->
                        sendSMSCode(phoneNumber)
                    },
                    onContinueAsGuest = {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                            putExtra("IS_GUEST", true)
                            putExtra("USER_PHONE", "")
                        }
                        startActivity(intent)
                        finish()
                    },
                    onRegisterCaptain = {
                        startActivity(Intent(this@SplashActivity, RegisterActivity::class.java))
                    }
                )
            }
        }
    }

    private fun sendSMSCode(phoneNumber: String) {
        val mAuth = auth
        if (mAuth != null) {
            try {
                val options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@SplashActivity, "تم التحقق التلقائي وتسجيل الدخول بنجاح!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                                        putExtra("USER_PHONE", phoneNumber)
                                        putExtra("IS_GUEST", false)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            }
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            val errorMsg = e.localizedMessage ?: e.message ?: "فشل التحقق عبر SMS"
                            Toast.makeText(this@SplashActivity, "تنبيه إرسال SMS: $errorMsg", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@SplashActivity, OtpActivity::class.java).apply {
                                putExtra("VERIFICATION_ID", "")
                                putExtra("PHONE", phoneNumber)
                                putExtra("ERROR_MSG", errorMsg)
                            }
                            startActivity(intent)
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            Toast.makeText(this@SplashActivity, "تم إرسال كود الـ SMS الحقيقي إلى $phoneNumber", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SplashActivity, OtpActivity::class.java).apply {
                                putExtra("VERIFICATION_ID", verificationId)
                                putExtra("PHONE", phoneNumber)
                            }
                            startActivity(intent)
                        }
                    })
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "حدث خطأ غير متوقع"
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                val intent = Intent(this, OtpActivity::class.java).apply {
                    putExtra("VERIFICATION_ID", "")
                    putExtra("PHONE", phoneNumber)
                    putExtra("ERROR_MSG", errorMsg)
                }
                startActivity(intent)
            }
        } else {
            val intent = Intent(this, OtpActivity::class.java).apply {
                putExtra("VERIFICATION_ID", "")
                putExtra("PHONE", phoneNumber)
            }
            startActivity(intent)
        }
    }
}
