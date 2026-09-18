package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YallaMeshwarTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

class OtpActivity : ComponentActivity() {
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var phoneNumber: String? = null
    private var initialErrorMsg: String? = null
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            auth = FirebaseAuth.getInstance()
        } catch (_: Exception) {
            auth = null
        }

        verificationId = intent.getStringExtra("VERIFICATION_ID")
        phoneNumber = intent.getStringExtra("PHONE") ?: "+201012345678"
        initialErrorMsg = intent.getStringExtra("ERROR_MSG")

        setContent {
            YallaMeshwarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    OtpScreen(
                        phoneNumber = phoneNumber ?: "",
                        verificationId = verificationId ?: "",
                        initialErrorMessage = initialErrorMsg,
                        onBackPressed = { finish() },
                        onVerify = { code, onDone ->
                            verifyCodeAndLogin(code, onDone)
                        },
                        onResend = {
                            resendSmsCode()
                        }
                    )
                }
            }
        }
    }

    private fun resendSmsCode() {
        val phone = phoneNumber ?: return
        val a = auth ?: return
        Toast.makeText(this, "جاري إرسال كود تحقق جديد عبر SMS...", Toast.LENGTH_SHORT).show()
        try {
            val optionsBuilder = PhoneAuthOptions.newBuilder(a)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)

            resendToken?.let { optionsBuilder.setForceResendingToken(it) }

            optionsBuilder.setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    a.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@OtpActivity, "تم التحقق التلقائي وتسجيل الدخول!", Toast.LENGTH_SHORT).show()
                            navigateToMain(phone)
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    val msg = e.localizedMessage ?: e.message ?: "فشل إعادة الإرسال"
                    Toast.makeText(this@OtpActivity, "فشل إرسال SMS: $msg", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    newVerificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = newVerificationId
                    resendToken = token
                    Toast.makeText(this@OtpActivity, "تم إرسال كود SMS جديد بنجاح إلى $phone", Toast.LENGTH_LONG).show()
                }
            })
            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        } catch (e: Exception) {
            Toast.makeText(this, "خطأ أثناء إعادة الإرسال: ${e.localizedMessage ?: e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCodeAndLogin(code: String, onDone: () -> Unit) {
        val vId = verificationId
        val phone = phoneNumber ?: ""

        if (vId.isNullOrEmpty()) {
            onDone()
            Toast.makeText(
                this,
                "لم يتم استلام معرّف التحقق عبر SMS بعد. يرجى الضغط على زر 'إعادة إرسال الرمز' أدناه لاستلام الكود.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val a = auth
        if (a == null) {
            onDone()
            Toast.makeText(this, "خدمة التحقق غير متوفرة حالياً، يرجى إعادة المحاولة", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(vId, code)
            a.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    onDone()
                    if (task.isSuccessful) {
                        Toast.makeText(this, "تم التحقق بنجاح من كود الـ SMS!", Toast.LENGTH_SHORT).show()
                        navigateToMain(phone)
                    } else {
                        val errorMsg = task.exception?.localizedMessage
                            ?: "كود التحقق غير صحيح، يرجى التأكد من الرمز المستلم في رسالة الـ SMS وإعادة المحاولة"
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
        } catch (e: Exception) {
            onDone()
            val msg = e.localizedMessage ?: e.message ?: "حدث خطأ أثناء التحقق من كود الـ SMS"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain(phone: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_PHONE", phone)
            putExtra("IS_GUEST", false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun OtpScreen(
    phoneNumber: String,
    verificationId: String,
    initialErrorMessage: String? = null,
    onBackPressed: () -> Unit,
    onVerify: (String, onDone: () -> Unit) -> Unit,
    onResend: () -> Unit = {}
) {
    val context = LocalContext.current
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E))
                    .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "تأكيد رقم الهاتف",
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "أدخل الرمز المكون من 6 أرقام المرسل عبر SMS إلى:",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (phoneNumber.isNotBlank()) phoneNumber else "+20 10xxxxxxxx",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!initialErrorMessage.isNullOrBlank() && verificationId.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color(0xFF331515), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ملاحظة: $initialErrorMessage\n(تأكد من تفعيل Phone Auth في Firebase Console وإضافة بصمة SHA-1)",
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = otpCode,
                onValueChange = { input ->
                    if (input.length <= 6 && input.all { it.isDigit() }) {
                        otpCode = input
                        if (input.length == 6) {
                            isLoading = true
                            onVerify(input) { isLoading = false }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .testTag("etOtpCode"),
                placeholder = {
                    Text(
                        text = "------",
                        color = Color(0xFF666666),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                textStyle = TextStyle(
                    color = Color(0xFFFFD700),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (0 until 6).forEach { index ->
                    val digit = otpCode.getOrNull(index)?.toString() ?: ""
                    val isFilled = digit.isNotEmpty()
                    val isCurrent = otpCode.length == index
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E1E))
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isFilled || isCurrent) Color(0xFFFFD700) else Color(0xFF333333),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            color = Color(0xFFFFD700),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (otpCode.length == 6) {
                        isLoading = true
                        onVerify(otpCode) { isLoading = false }
                    } else {
                        Toast.makeText(context, "يرجى إدخال كود كامل (6 أرقام)", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .testTag("btnVerify"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF000000)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "تأكيد الدخول",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (countdown > 0) {
                Text(
                    text = "إعادة إرسال الرمز خلال $countdown ثانية",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        countdown = 60
                        onResend()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إعادة إرسال SMS",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
