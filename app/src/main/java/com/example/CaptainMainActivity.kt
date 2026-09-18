package com.example

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MalakiGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YallaMeshwarTheme
import com.yalla.meshwar.CaptainBlockManager as YallaCaptainBlockManager

class CaptainMainActivity : ComponentActivity() {
    private var captainId: String = "CAPTAIN_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        captainId = intent.getStringExtra("CAPTAIN_ID") ?: "CAPTAIN_123"
        checkCaptainStatus()

        setContent {
            YallaMeshwarTheme {
                CaptainDashboardScreen(
                    captainId = captainId,
                    onAcceptTrip = {
                        val captain = com.yalla.meshwar.CaptainDetails(
                            captainId,
                            "كابتن أحمد محمود",
                            "01012345678",
                            "سكوتر",
                            "ق هـ ر 1234",
                            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                            4.9
                        )
                        CaptainAcceptTrip.acceptTripAndSendDetails("TRIP_12345", captain)
                        Toast.makeText(this, "تم قبول المشوار بنجاح وإرسال بياناتك للراكب!", Toast.LENGTH_SHORT).show()
                    },
                    onRejectTrip = {
                        CaptainBlockManager.recordTripRejection(captainId)
                        Toast.makeText(this, "تم رفض المشوار", Toast.LENGTH_SHORT).show()
                        checkCaptainStatus()
                    },
                    onOpenRadar = {
                        startActivity(Intent(this, CaptainRadarActivity::class.java))
                    },
                    onOpenWallet = {
                        startActivity(Intent(this, CaptainWalletActivity::class.java))
                    },
                    onBackPressed = { finish() }
                )
            }
        }
    }

    private fun checkCaptainStatus() {
        CaptainBlockManager.checkIfCaptainIsBlocked(captainId, object : YallaCaptainBlockManager.BlockCheckCallback {
            override fun onBlocked(remainingTimeMs: Long) {
                val hours = remainingTimeMs / (1000 * 60 * 60)
                val minutes = (remainingTimeMs / (1000 * 60)) % 60
                val message = "حسابك محظور مؤقتاً بسبب رفض الرحلات المتتالية (3 مرات) أو تكرار البلاغات.\n" +
                        "الوقت المتبقي لرفع الحظر: $hours ساعة و $minutes دقيقة."

                AlertDialog.Builder(this@CaptainMainActivity)
                    .setTitle("تنبيه حظر الحساب")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("حسناً") { _, _ -> finish() }
                    .show()
            }

            override fun onAllowed() {
                // مسموح للكابتن بالعمل
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptainDashboardScreen(
    captainId: String,
    onAcceptTrip: () -> Unit,
    onRejectTrip: () -> Unit,
    onOpenRadar: () -> Unit,
    onOpenWallet: () -> Unit,
    onBackPressed: () -> Unit
) {
    var consecutiveRejections by remember { mutableLongStateOf(0L) }
    var totalReports by remember { mutableLongStateOf(0L) }
    var activeIncomingRequest by remember { mutableStateOf(true) }

    fun refreshStats() {
        consecutiveRejections = CaptainBlockManager.getLocalConsecutiveRejections(captainId)
        totalReports = CaptainBlockManager.getLocalTotalReports(captainId)
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "لوحة تحكم كابتن يلا مشوار",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "كود الكابتن: $captainId",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenWallet) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "المحفظة",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status & Block Policy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MalakiGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حالة الحساب ونظام الانضباط",
                                color = MalakiGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MalakiGreen.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MalakiGreen)
                        ) {
                            Text(
                                text = "نشط ومتاح للعمل",
                                color = MalakiGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rejections & Reports counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Rejections card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (consecutiveRejections >= 2) ErrorRed else DarkBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "الرفض المتتالي",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$consecutiveRejections / ${CaptainBlockManager.MAX_REJECTIONS}",
                                    color = if (consecutiveRejections >= 2) ErrorRed else GoldPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (consecutiveRejections >= 2) "تحذير: حظر 24 ساعة عند 3" else "3 مرات = حظر 24 ساعة",
                                    color = if (consecutiveRejections >= 2) ErrorRed else TextSecondary,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Reports card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (totalReports >= 2) ErrorRed else DarkBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "بلاغات الركاب",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$totalReports / ${CaptainBlockManager.MAX_REPORTS}",
                                    color = if (totalReports >= 2) ErrorRed else GoldPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "3 بلاغات = حظر 24 ساعة",
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Incoming trip card
            AnimatedVisibility(visible = activeIncomingRequest) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incoming_trip_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "مشوار جديد قريب منك!",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "75 ج.م",
                                color = MalakiGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "من ميدان التحرير إلى الدقي (3.8 كم)",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "تقييم الراكب: 4.8 ⭐ (طريقة الدفع: كاش)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Accept / Reject buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // btnRejectTrip
                            Button(
                                onClick = {
                                    onRejectTrip()
                                    refreshStats()
                                    activeIncomingRequest = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btnRejectTrip"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed.copy(alpha = 0.2f),
                                    contentColor = ErrorRed
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "رفض", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // btnAcceptTrip
                            Button(
                                onClick = {
                                    onAcceptTrip()
                                    refreshStats()
                                    activeIncomingRequest = false
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp)
                                    .testTag("btnAcceptTrip"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MalakiGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "قبول المشوار", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            if (!activeIncomingRequest) {
                Button(
                    onClick = { activeIncomingRequest = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("البحث عن مشاوير جديدة")
                }
            }

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenRadar,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btnOpenRadar"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = GoldPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح الرادار")
                }

                Button(
                    onClick = onOpenWallet,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("btnOpenWallet"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = TextDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المحفظة والأرباح", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
