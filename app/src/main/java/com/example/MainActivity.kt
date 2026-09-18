package com.example

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricRickshaw
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MeshwarRepository
import com.example.model.RideOrder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MalakiGreen
import com.example.ui.theme.ScooterBlue
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TokTokOrange
import com.example.ui.theme.VodafoneRed
import com.example.ui.theme.YallaMeshwarTheme

class MainActivity : ComponentActivity() {
    private var isGuest: Boolean = false
    private var userPhone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        isGuest = intent.getBooleanExtra("IS_GUEST", false)
        userPhone = intent.getStringExtra("USER_PHONE") ?: ""

        setContent {
            YallaMeshwarTheme {
                MainRideScreen(
                    isGuest = isGuest,
                    userPhone = userPhone,
                    onOpenSettings = {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    },
                    onOpenRegister = {
                        startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                    },
                    onOpenAdmin = {
                        startActivity(Intent(this@MainActivity, AdminActivity::class.java))
                    },
                    onOpenCaptainDashboard = {
                        startActivity(Intent(this@MainActivity, CaptainMainActivity::class.java))
                    },
                    onOpenRadar = { cat, dist ->
                        val intent = Intent(this@MainActivity, CaptainRadarActivity::class.java).apply {
                            putExtra("CATEGORY", cat)
                            putExtra("DISTANCE_KM", dist)
                        }
                        startActivity(intent)
                    },
                    onOpenChat = { tripId ->
                        val intent = Intent(this@MainActivity, LiveChatActivity::class.java).apply {
                            putExtra("TRIP_ID", tripId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun MainRideScreen(
    isGuest: Boolean,
    userPhone: String,
    onOpenSettings: () -> Unit,
    onOpenRegister: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenCaptainDashboard: () -> Unit = {},
    onOpenRadar: (category: String, distanceKm: Double) -> Unit,
    onOpenChat: (tripId: String) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<String?>("سكوتر") }
    var selectedSubOption by remember { mutableStateOf<String?>("مشاوير سريعة") }
    var paymentMethod by remember { mutableStateOf("كاش") }

    val activeOrder by MeshwarRepository.activeOrder.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Full Screen Interactive Map with Cairo (30.0444, 31.2357)
        InteractiveEgyptianMap(
            selectedCategory = selectedCategory,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Header Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // btnSettings
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC1E1E1E))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.6f), CircleShape)
                        .clickable { onOpenSettings() }
                        .testTag("btnSettings"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات والأمان",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Brand pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xDD1E1E1E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MalakiGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "يلا مشوار",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isGuest) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(زائر)",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Captain Dashboard shortcut
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC1E1E1E))
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
                            .clickable { onOpenCaptainDashboard() }
                            .testTag("btn_top_captain"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "واجهة الكابتن",
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Admin shortcut
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC1E1E1E))
                            .border(1.dp, DarkBorder, CircleShape)
                            .clickable { onOpenAdmin() }
                            .testTag("btn_top_admin"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "الإدارة",
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "الموقع الحالي: ميدان التحرير، القاهرة", Toast.LENGTH_SHORT).show()
                    },
                color = Color(0xEE1E1E1E),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "موقع الانطلاق (القاهرة)",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ميدان التحرير (30.0444, 31.2357)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "تغيير الوجهة",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Active Ride Card if trip was requested
        AnimatedVisibility(
            visible = activeOrder != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            activeOrder?.let { order ->
                ActiveOrderCard(
                    order = order,
                    onOpenChat = { onOpenChat(order.id) },
                    onCancel = {
                        MeshwarRepository.cancelOrder()
                        Toast.makeText(context, "تم إلغاء الطلب", Toast.LENGTH_SHORT).show()
                    },
                    onEmergency = {
                        val currentLat = 30.0444
                        val currentLng = 31.2357
                        val currentUserId = if (order.userPhone.isNotBlank()) order.userPhone else (if (userPhone.isNotBlank()) userPhone else "USER_MOBILE_NUMBER")
                        EmergencySosManager.callEgyptPoliceAndSendLocation(context, currentUserId, currentLat, currentLng)
                    }
                )
            }
        }

        // Floating SOS Button (btnSos) for Egypt Police 122
        if (activeOrder == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB71C1C))
                    .border(2.dp, GoldPrimary, CircleShape)
                    .clickable {
                        val currentLat = 30.0444
                        val currentLng = 31.2357
                        val currentUserId = if (userPhone.isNotBlank()) userPhone else "USER_MOBILE_NUMBER"
                        EmergencySosManager.callEgyptPoliceAndSendLocation(context, currentUserId, currentLat, currentLng)
                    }
                    .testTag("btnSos"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "طوارئ النجدة 122",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SOS",
                        color = GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Bottom Sheet for ordering rides
        if (activeOrder == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .testTag("bottomSheet"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Pull notch
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF555555))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "اختر وسيلة التوصيل:",
                        color = GoldPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories: btnTokTok, btnScooter, btnMalaki
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // btnTokTok (#FF9800)
                        Button(
                            onClick = {
                                selectedCategory = "توك توك"
                                selectedSubOption = "مشاوير سريعة"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("btnTokTok"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCategory == "توك توك") TokTokOrange else TokTokOrange.copy(alpha = 0.7f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.ElectricRickshaw, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "توك توك",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // btnScooter (#2196F3)
                        Button(
                            onClick = {
                                selectedCategory = "سكوتر"
                                selectedSubOption = "مشاوير سريعة"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("btnScooter"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCategory == "سكوتر") ScooterBlue else ScooterBlue.copy(alpha = 0.7f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "سكوتر",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // btnMalaki (#4CAF50)
                        Button(
                            onClick = {
                                selectedCategory = "ملاكي"
                                selectedSubOption = "مشاوير سريعة"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("btnMalaki"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCategory == "ملاكي") MalakiGreen else MalakiGreen.copy(alpha = 0.7f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "ملاكي",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Dynamic Sub-options layout
                    if (selectedCategory != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "خدمات $selectedCategory:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val subOptions = when (selectedCategory) {
                            "توك توك", "سكوتر" -> listOf("مشاوير سريعة", "طلبات وتوصيل")
                            "ملاكي" -> listOf("مشاوير سريعة", "سفر محافظات", "مناسبات وأفراح", "مشوار المطار")
                            else -> emptyList()
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("layoutSubOptions"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            subOptions.forEach { sub ->
                                val isSubSelected = selectedSubOption == sub
                                Button(
                                    onClick = {
                                        selectedSubOption = sub
                                        Toast.makeText(context, "الخدمة: $sub", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("sub_option_$sub"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSubSelected) GoldPrimary else Color(0xFF333333),
                                        contentColor = if (isSubSelected) TextDark else GoldPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method RadioGroup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paymentGroup"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "طريقة الدفع:",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        // payCash
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { paymentMethod = "كاش" }
                                .testTag("payCash")
                        ) {
                            RadioButton(
                                selected = paymentMethod == "كاش",
                                onClick = { paymentMethod = "كاش" },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                            )
                            Text(
                                text = "كاش",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // payVodafone
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { paymentMethod = "فودافون كاش" }
                                .testTag("payVodafone")
                        ) {
                            RadioButton(
                                selected = paymentMethod == "فودافون كاش",
                                onClick = { paymentMethod = "فودافون كاش" },
                                colors = RadioButtonDefaults.colors(selectedColor = VodafoneRed)
                            )
                            Text(
                                text = "فودافون كاش",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Trip estimate calculation via TripPriceCalculator
                    val activeCategory = selectedCategory ?: "سكوتر"
                    val distanceKm = 5.2
                    val calculatedPrice = TripPriceCalculator.calculateSuggestedPrice(activeCategory, distanceKm)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المسافة المتوقعة: $distanceKm كم",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${calculatedPrice.toInt()} جنيه تقريباً",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Negotiation & Radar button
                    Button(
                        onClick = {
                            val cat = selectedCategory ?: "سكوتر"
                            onOpenRadar(cat, distanceKm)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btnNegotiate"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E2600),
                            contentColor = GoldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "رادار الكباتن والتفاوض على السعر",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // btnOrder: "يلا مشوار الآن"
                    Button(
                        onClick = {
                            if (isGuest && (userPhone.isEmpty())) {
                                Toast.makeText(
                                    context,
                                    "يرجى تسجيل الدخول برقم هاتفك لطلب المشوار",
                                    Toast.LENGTH_LONG
                                ).show()
                                onOpenRegister()
                            } else {
                                val cat = selectedCategory ?: "سكوتر"
                                val sub = selectedSubOption ?: "مشاوير سريعة"
                                Toast.makeText(
                                    context,
                                    "جاري البحث عن أقرب كابتن...",
                                    Toast.LENGTH_LONG
                                ).show()
                                val baseFare = TripPriceCalculator.calculateSuggestedPrice(cat, distanceKm)
                                val fare = when (sub) {
                                    "سفر محافظات" -> (baseFare + 150).toInt()
                                    "مناسبات وأفراح" -> (baseFare + 250).toInt()
                                    "مشوار المطار" -> (baseFare + 300).toInt()
                                    else -> baseFare.toInt()
                                }
                                MeshwarRepository.createOrder(
                                    RideOrder(
                                        id = "MESHWAR-${(1000..9999).random()}",
                                        userPhone = userPhone.ifEmpty { "01012345678" },
                                        category = cat,
                                        subOption = sub,
                                        paymentMethod = paymentMethod,
                                        fareEgp = fare
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btnOrder"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "يلا مشوار الآن",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveOrderCard(
    order: RideOrder,
    onOpenChat: () -> Unit,
    onCancel: () -> Unit,
    onEmergency: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_order_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                            .background(MalakiGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "كابتن في الطريق إليك (${order.category})",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${order.etaMinutes} دقائق للوصول",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Captain details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.captainName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المركبة: ${order.vehiclePlate}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "الدفع: ${order.paymentMethod} (${order.fareEgp} ج.م)",
                        color = GoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Call captain
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MalakiGreen.copy(alpha = 0.2f))
                            .border(1.dp, MalakiGreen, CircleShape)
                            .clickable {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.captainPhone}"))
                                context.startActivity(dialIntent)
                            }
                            .testTag("btn_call_captain"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "اتصال بالكابتن", tint = MalakiGreen, modifier = Modifier.size(20.dp))
                    }

                    // Live Chat with captain
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, GoldPrimary, CircleShape)
                            .clickable { onOpenChat() }
                            .testTag("btn_chat_captain"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "محادثة الكابتن", tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    }

                    // Emergency SOS
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ErrorRed.copy(alpha = 0.2f))
                            .border(1.dp, ErrorRed, CircleShape)
                            .clickable { onEmergency() }
                            .testTag("btnSos")
                            .testTag("btn_sos_ride"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "طوارئ النجدة SOS", tint = ErrorRed, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons: Complete & Rate / Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        MeshwarRepository.cancelOrder()
                        RatingDialog.showRatingDialog(context, order.captainName, order.id)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_complete_and_rate"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "إنهاء وتقييم الرحلة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier.height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A),
                        contentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "إلغاء الطلب", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Egyptian Map renderer centering on Cairo (30.0444, 31.2357) with Nile River,
 * interactive zoom, user location marker, and dynamic nearby vehicle pins!
 */
@Composable
fun InteractiveEgyptianMap(
    selectedCategory: String?,
    modifier: Modifier = Modifier
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "map_radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + panOffsetX
            val centerY = height / 2f + panOffsetY

            // 1. Dark Map Canvas Background
            drawRect(color = Color(0xFF15181C))

            // 2. Cairo River Nile Path
            val nilePath = Path().apply {
                moveTo(centerX - 90f * zoomLevel, 0f)
                cubicTo(
                    centerX - 40f * zoomLevel, height * 0.3f,
                    centerX - 120f * zoomLevel, height * 0.6f,
                    centerX - 60f * zoomLevel, height
                )
            }
            drawPath(
                path = nilePath,
                color = Color(0xFF1B3A4B),
                style = Stroke(width = 38f * zoomLevel)
            )

            // Zamalek Island inside Nile
            drawCircle(
                color = Color(0xFF1F242B),
                radius = 18f * zoomLevel,
                center = Offset(centerX - 65f * zoomLevel, centerY - 20f * zoomLevel)
            )

            // 3. Cairo Major Roads & Bridges
            // 6th October Bridge
            drawLine(
                color = Color(0xFF2C323B),
                start = Offset(0f, centerY - 15f * zoomLevel),
                end = Offset(width, centerY - 15f * zoomLevel),
                strokeWidth = 6f * zoomLevel
            )
            // Kasr El Nile Bridge
            drawLine(
                color = Color(0xFF333A44),
                start = Offset(0f, centerY + 30f * zoomLevel),
                end = Offset(width, centerY + 30f * zoomLevel),
                strokeWidth = 5f * zoomLevel
            )
            // Nile Corniche Road
            drawLine(
                color = Color(0xFF2C323B),
                start = Offset(centerX - 10f * zoomLevel, 0f),
                end = Offset(centerX - 30f * zoomLevel, height),
                strokeWidth = 5f * zoomLevel
            )

            // 4. Cairo City Districts Labels / Zones
            // Tahrir Square (User center: 30.0444, 31.2357)
            val userLocation = Offset(centerX, centerY)

            // Radar pulse around user's location
            drawCircle(
                color = GoldPrimary.copy(alpha = 0.2f),
                radius = radarPulse * zoomLevel,
                center = userLocation
            )
            drawCircle(
                color = GoldPrimary,
                radius = 7f * zoomLevel,
                center = userLocation
            )
            drawCircle(
                color = TextDark,
                radius = 3f * zoomLevel,
                center = userLocation
            )

            // 5. Nearby simulated Captains in Cairo
            val nearbyDrivers = listOf(
                Triple(Offset(centerX + 60f * zoomLevel, centerY - 50f * zoomLevel), "توك توك", TokTokOrange),
                Triple(Offset(centerX - 70f * zoomLevel, centerY + 60f * zoomLevel), "توك توك", TokTokOrange),
                Triple(Offset(centerX + 110f * zoomLevel, centerY + 40f * zoomLevel), "سكوتر", ScooterBlue),
                Triple(Offset(centerX - 40f * zoomLevel, centerY - 90f * zoomLevel), "سكوتر", ScooterBlue),
                Triple(Offset(centerX + 90f * zoomLevel, centerY - 110f * zoomLevel), "ملاكي", MalakiGreen),
                Triple(Offset(centerX - 100f * zoomLevel, centerY - 30f * zoomLevel), "ملاكي", MalakiGreen),
                Triple(Offset(centerX + 30f * zoomLevel, centerY + 120f * zoomLevel), "ملاكي", MalakiGreen)
            )

            nearbyDrivers.forEach { (pos, cat, color) ->
                val isHighlighted = selectedCategory == null || selectedCategory == cat
                val alpha = if (isHighlighted) 1f else 0.25f

                drawCircle(
                    color = color.copy(alpha = 0.25f * alpha),
                    radius = 14f * zoomLevel,
                    center = pos
                )
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = 6f * zoomLevel,
                    center = pos
                )
            }
        }

        // Map UI Controls: Recenter, Zoom In, Zoom Out
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom in
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC1E1E1E))
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable { zoomLevel = (zoomLevel + 0.2f).coerceAtMost(2.5f) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "تكبير الخريطة", tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }

            // Zoom out
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC1E1E1E))
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable { zoomLevel = (zoomLevel - 0.2f).coerceAtLeast(0.7f) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "تصغير الخريطة", tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }

            // Recenter
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC1E1E1E))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomLevel = 1.0f
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "إعادة ضبط الموقع", tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
