package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MeshwarRepository
import com.example.model.TripModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MalakiGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YallaMeshwarTheme
import kotlinx.coroutines.delay

class CaptainRadarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val category = intent.getStringExtra("CATEGORY") ?: "سكوتر"
        val distanceKm = intent.getDoubleExtra("DISTANCE_KM", 5.2)

        setContent {
            YallaMeshwarTheme {
                CaptainRadarScreen(
                    category = category,
                    initialDistanceKm = distanceKm,
                    onBackPressed = { finish() },
                    onNavigateToChat = { tripId ->
                        val intent = Intent(this@CaptainRadarActivity, LiveChatActivity::class.java).apply {
                            putExtra("TRIP_ID", tripId)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptainRadarScreen(
    category: String,
    initialDistanceKm: Double,
    onBackPressed: () -> Unit,
    onNavigateToChat: (tripId: String) -> Unit
) {
    val context = LocalContext.current
    val initialSuggested = remember(category, initialDistanceKm) {
        TripPriceCalculator.calculateSuggestedPrice(category, initialDistanceKm)
    }

    var currentPrice by remember { mutableDoubleStateOf(initialSuggested) }
    val distanceKm by remember { mutableDoubleStateOf(initialDistanceKm) }
    var isSearchingRadar by remember { mutableStateOf(false) }
    var activeTripId by remember { mutableStateOf<String?>(null) }
    var captainAccepted by remember { mutableStateOf(false) }

    LaunchedEffect(activeTripId, isSearchingRadar) {
        val tripId = activeTripId
        if (isSearchingRadar && tripId != null) {
            delay(2800)
            MeshwarRepository.updateTripStatus(tripId, "ACCEPTED")
            MeshwarRepository.sendChatMessage(
                ChatMessage(
                    id = "welcome_${System.currentTimeMillis()}",
                    tripId = tripId,
                    sender = "CAPTAIN",
                    message = "تم قبول عرض السعر (${currentPrice.toInt()} ج.م)، أنا قادم إليك في خلال 4 دقائق!",
                    timestamp = System.currentTimeMillis()
                )
            )
            captainAccepted = true
            Toast.makeText(context, "وافق كابتن على عرض السعر! جاري فتح المحادثة...", Toast.LENGTH_SHORT).show()
            delay(1200)
            onNavigateToChat(tripId)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

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
                            text = "رادار كباتن يلا مشوار",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "وسيلة النقل: $category",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Distance card (tvDistance)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "مسافة المشوار: ${String.format("%.1f", distanceKm)} كم",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("tvDistance")
                        )
                        Text(
                            text = "من ميدان التحرير إلى المهندسين (القاهرة)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Negotiation & Price Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تفاوض على سعر المشوار العادل",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "يمكنك رفع أو خفض السعر بما يناسبك لاجتذاب الكباتن القريبين فوراً",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // Price controller row (+ / tvPrice / -)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // btnMinus (60dp x 60dp, background #D32F2F)
                        Button(
                            onClick = {
                                if (currentPrice > 10.0) {
                                    currentPrice -= 5.0
                                }
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .testTag("btnMinus"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "-", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        }

                        // tvPrice (gold, bold)
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${currentPrice.toInt()} ج.م",
                                color = GoldPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("tvPrice")
                            )
                        }

                        // btnPlus (60dp x 60dp, background #388E3C)
                        Button(
                            onClick = {
                                currentPrice += 5.0
                            },
                            modifier = Modifier
                                .size(60.dp)
                                .testTag("btnPlus"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF388E3C),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // btnSendOffer
                    Button(
                        onClick = {
                            val tripId = "TRIP_${System.currentTimeMillis() % 100000}"
                            activeTripId = tripId
                            val trip = TripModel(
                                tripId = tripId,
                                category = category,
                                price = currentPrice,
                                status = "معروضة للرادار",
                                distanceKm = distanceKm
                            )
                            MeshwarRepository.publishTrip(trip)
                            Toast.makeText(
                                context,
                                "تم بث المشوار إلى رادار الكباتن المحيطين بك!",
                                Toast.LENGTH_LONG
                            ).show()
                            isSearchingRadar = true
                        },
                        enabled = !isSearchingRadar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .testTag("btnSendOffer"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark,
                            disabledContainerColor = Color(0xFF3A3A3A),
                            disabledContentColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isSearchingRadar) "جاري البحث في الرادار..." else "إرسال العرض لكباتن المنطقة",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Radar Visualizer
            AnimatedVisibility(
                visible = isSearchingRadar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = pulseAlpha))
                            )

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceElevated)
                                    .border(2.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (captainAccepted) Icons.Default.CheckCircle else Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = if (captainAccepted) MalakiGreen else GoldPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (captainAccepted) "تم العثور على كابتن وقبول العرض!" else "جاري مسح الرادار في نطاق 5 كم...",
                            color = if (captainAccepted) MalakiGreen else GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (captainAccepted)
                                "جاري تحويلك للمحادثة المباشرة مع الكابتن..."
                            else
                                "تم إرسال سعرك المخصص (${currentPrice.toInt()} ج.م) إلى 7 كباتن في محيطك",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
