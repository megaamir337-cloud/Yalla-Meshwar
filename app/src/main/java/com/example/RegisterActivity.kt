package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.model.ApplicationStatus
import com.example.model.CaptainApplication
import com.example.model.MeshwarRepository
import com.example.model.UserRole
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MalakiGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YallaMeshwarTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class RegisterActivity : ComponentActivity() {
    private var mAuth: FirebaseAuth? = null
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            mAuth = FirebaseAuth.getInstance()
        } catch (_: Exception) {
            mAuth = null
        }

        requestAllPermissions()

        setContent {
            YallaMeshwarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    RegisterScreen(
                        onSendOtp = { phoneNumber ->
                            sendSMSCode(phoneNumber)
                        },
                        onNavigateToMain = { isGuest, phone ->
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java).apply {
                                putExtra("IS_GUEST", isGuest)
                                putExtra("USER_PHONE", phone)
                            }
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToAdmin = {
                            startActivity(Intent(this@RegisterActivity, AdminActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        )
        var allGranted = true
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun sendSMSCode(phoneNumber: String) {
        val auth = mAuth
        if (auth != null) {
            try {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Instant verification / auto-retrieval
                            auth.signInWithCredential(credential)
                                .addOnCompleteListener(this@RegisterActivity) { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@RegisterActivity, "تم التحقق التلقائي وتسجيل الدخول بنجاح!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@RegisterActivity, MainActivity::class.java).apply {
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
                            val errorMsg = e.localizedMessage ?: e.message ?: "حدث خطأ أثناء إرسال الكود"
                            Toast.makeText(this@RegisterActivity, "تنبيه إرسال SMS: $errorMsg", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@RegisterActivity, OtpActivity::class.java).apply {
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
                            Toast.makeText(this@RegisterActivity, "تم إرسال كود التحقق SMS بنجاح إلى $phoneNumber", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, OtpActivity::class.java).apply {
                                putExtra("VERIFICATION_ID", verificationId)
                                putExtra("PHONE", phoneNumber)
                            }
                            startActivity(intent)
                        }
                    })
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "حدث خطأ أثناء إرسال الكود"
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, OtpActivity::class.java).apply {
                    putExtra("VERIFICATION_ID", "")
                    putExtra("PHONE", phoneNumber)
                    putExtra("ERROR_MSG", errorMsg)
                }
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "جاري فتح التحقق...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, OtpActivity::class.java).apply {
                putExtra("VERIFICATION_ID", "")
                putExtra("PHONE", phoneNumber)
            }
            startActivity(intent)
        }
    }
}

@Composable
fun RegisterScreen(
    onSendOtp: (phoneNumber: String) -> Unit,
    onNavigateToMain: (isGuest: Boolean, phone: String) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var phoneInput by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }

    // Captain Documents state
    var idFrontUploaded by remember { mutableStateOf(false) }
    var idBackUploaded by remember { mutableStateOf(false) }
    var licenseUploaded by remember { mutableStateOf(false) }
    var selfieUploaded by remember { mutableStateOf(false) }
    var selectedVehicleType by remember { mutableStateOf("سكوتر") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "تم اختيار المستند بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "يلا مشوار",
                    color = Color(0xFFFFD700),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("app_title")
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GoldDark.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToAdmin() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("admin_shortcut_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "الإدارة",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "لوحة الإدارة",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "سجل حسابك كراكب أو انضم ككابتن يلا مشوار",
                color = Color(0xFFAAAAAA),
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Role selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("role_group_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "نوع الحساب المطلوب:",
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedRole = UserRole.USER }
                                .testTag("rb_user")
                        ) {
                            RadioButton(
                                selected = selectedRole == UserRole.USER,
                                onClick = { selectedRole = UserRole.USER },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700))
                            )
                            Text(
                                text = "راكب",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (selectedRole == UserRole.USER) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // Captain
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedRole = UserRole.CAPTAIN }
                                .testTag("rb_captain")
                        ) {
                            RadioButton(
                                selected = selectedRole == UserRole.CAPTAIN,
                                onClick = { selectedRole = UserRole.CAPTAIN },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700))
                            )
                            Text(
                                text = "كابتن",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (selectedRole == UserRole.CAPTAIN) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // Guest
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { selectedRole = UserRole.GUEST }
                                .testTag("rb_guest")
                        ) {
                            RadioButton(
                                selected = selectedRole == UserRole.GUEST,
                                onClick = { selectedRole = UserRole.GUEST },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700))
                            )
                            Text(
                                text = "زائر",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = if (selectedRole == UserRole.GUEST) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Phone number layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(
                        width = 1.dp,
                        color = if (phoneError.isNotEmpty()) Color(0xFFFF4444) else Color(0xFFFFD700).copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+20 ",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (phoneInput.isEmpty()) {
                            Text(
                                text = "10xxxxxxxx",
                                color = Color(0xFF666666),
                                fontSize = 16.sp
                            )
                        }
                        BasicTextField(
                            value = phoneInput,
                            onValueChange = { input ->
                                phoneError = ""
                                if (input.length <= 11 && input.all { it.isDigit() }) {
                                    phoneInput = input
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("etPhone"),
                            textStyle = TextStyle(
                                color = Color(0xFFFFFFFF),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            cursorBrush = SolidColor(Color(0xFFFFD700))
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "الهاتف",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (phoneError.isNotEmpty()) {
                Text(
                    text = phoneError,
                    color = Color(0xFFFF4444),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val phone = phoneInput.trim()
                    if (selectedRole == UserRole.GUEST) {
                        onNavigateToMain(true, phone)
                        return@Button
                    }
                    if (phone.isEmpty() || phone.length < 10) {
                        phoneError = "يرجى كتابة رقم هاتف مصري صحيح (10-11 رقم)"
                        Toast.makeText(context, "رقم الهاتف غير مكتمل", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSendingOtp = true
                    val cleanDigits = phone.filter { it.isDigit() }
                    val fullPhoneNumber = when {
                        cleanDigits.startsWith("20") && cleanDigits.length >= 12 -> "+$cleanDigits"
                        cleanDigits.startsWith("0") -> "+20${cleanDigits.substring(1)}"
                        else -> "+20$cleanDigits"
                    }
                    onSendOtp(fullPhoneNumber)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btnSendOtp"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF000000)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSendingOtp) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedRole == UserRole.GUEST) "دخول مباشر" else "إرسال كود التحقق SMS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF000000)
                        )
                    }
                }
            }

            // Captain Documents Section
            AnimatedVisibility(
                visible = selectedRole == UserRole.CAPTAIN,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .testTag("layout_captain_docs")
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "بيانات الكابتن والمستندات الرسمية",
                                    color = GoldPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "مطلوب للتحقق من هوية الكابتن لضمان سلامة الركاب بنسبة 100%",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            // Vehicle Type Selector
                            Text(
                                text = "نوع وسيلة النقل:",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("توك توك", "سكوتر", "ملاكي", "شحن").forEach { type ->
                                    val isSelected = selectedVehicleType == type
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) GoldPrimary else DarkSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isSelected) GoldDark else DarkBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedVehicleType = type }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = type,
                                            color = if (isSelected) TextDark else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 1. National ID Front
                            CaptainDocButton(
                                text = "بطاقة الرقم القومي (الوجه)",
                                isUploaded = idFrontUploaded,
                                testTag = "btn_upload_id_front",
                                onClick = {
                                    idFrontUploaded = !idFrontUploaded
                                    if (idFrontUploaded) {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 2. National ID Back
                            CaptainDocButton(
                                text = "بطاقة الرقم القومي (الظهر)",
                                isUploaded = idBackUploaded,
                                testTag = "btn_upload_id_back",
                                onClick = {
                                    idBackUploaded = !idBackUploaded
                                    if (idBackUploaded) {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 3. Driving License
                            CaptainDocButton(
                                text = "رخصة القيادة السارية",
                                isUploaded = licenseUploaded,
                                testTag = "btn_upload_license",
                                onClick = {
                                    licenseUploaded = !licenseUploaded
                                    if (licenseUploaded) {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 4. Selfie Photo
                            CaptainDocButton(
                                text = "صورة شخصية حديثة (سيلفي)",
                                isUploaded = selfieUploaded,
                                testTag = "btn_upload_selfie",
                                isSelfie = true,
                                onClick = {
                                    selfieUploaded = !selfieUploaded
                                    if (selfieUploaded) {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val phone = phoneInput.trim()
                                    if (phone.isEmpty()) {
                                        phoneError = "يرجى كتابة رقم هاتفك أولاً"
                                        Toast.makeText(context, "يرجى إدخال رقم الهاتف", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val newApp = CaptainApplication(
                                            id = "CAP-${System.currentTimeMillis() % 10000}",
                                            phone = phone,
                                            idFrontAttached = idFrontUploaded,
                                            idBackAttached = idBackUploaded,
                                            licenseAttached = licenseUploaded,
                                            selfieAttached = selfieUploaded,
                                            vehicleType = selectedVehicleType,
                                            status = ApplicationStatus.PENDING,
                                            submissionDate = "اليوم"
                                        )
                                        MeshwarRepository.addCaptainApplication(newApp)
                                        Toast.makeText(
                                            context,
                                            "تم إرسال طلب انضمام الكابتن للمراجعة بنجاح!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onSendOtp("+20$phone")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_submit_captain_docs"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MalakiGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "إرسال طلب الانضمام ككابتن",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "بالتسجيل، أنت توافق على شروط خدمة وسياسة أمان يلا مشوار",
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun CaptainDocButton(
    text: String,
    isUploaded: Boolean,
    testTag: String,
    isSelfie: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag(testTag),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isUploaded) Color(0xFF1B3820) else Color(0xFF333333),
            contentColor = if (isUploaded) MalakiGreen else GoldPrimary
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSelfie) Icons.Default.CameraAlt else Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = if (isUploaded) MalakiGreen else GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = if (isUploaded) MalakiGreen else GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (isUploaded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تم الرفع",
                        tint = MalakiGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مرفق",
                        color = MalakiGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "إرفاق مستند",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
