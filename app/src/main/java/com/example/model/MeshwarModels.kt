package com.example.model

enum class UserRole(val title: String) {
    USER("راكب"),
    CAPTAIN("كابتن"),
    GUEST("زائر")
}

enum class ApplicationStatus(val title: String) {
    PENDING("قيد المراجعة"),
    APPROVED("تمت الموافقة"),
    REJECTED("مرفوض")
}

data class CaptainApplication(
    val id: String,
    val phone: String,
    val idFrontAttached: Boolean = true,
    val idBackAttached: Boolean = true,
    val licenseAttached: Boolean = true,
    val selfieAttached: Boolean = true,
    val vehicleType: String = "سكوتر",
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val submissionDate: String = "اليوم 10:30 ص"
)

enum class ServiceCategory(val displayName: String, val badgeColorHex: Long) {
    TOKTOK("توك توك", 0xFFFF9800),
    SCOOTER("سكوتر", 0xFF2196F3),
    MALAKI("ملاكي", 0xFF4CAF50)
}

data class RideOrder(
    val id: String,
    val userPhone: String,
    val category: String,
    val subOption: String,
    val paymentMethod: String,
    val fareEgp: Int,
    val pickupName: String = "ميدان التحرير، القاهرة",
    val dropoffName: String = "شارع جامعة الدول، المهندسين",
    val captainName: String = "أحمد محمود (كابتن يلا مشوار)",
    val captainPhone: String = "01012345678",
    val vehiclePlate: String = "سكوتر | ق هـ ر 4921",
    val etaMinutes: Int = 4,
    val safetyVerified: Boolean = true,
    val captainPhotoUrl: String = "",
    val captainRating: Double = 4.9,
    val status: String = "ACCEPTED"
)

data class TripModel(
    val tripId: String,
    val category: String = "سكوتر",
    val price: Double = 45.0,
    val status: String = "معروضة للرادار",
    val distanceKm: Double = 5.2,
    val pickupLocation: String = "ميدان التحرير",
    val dropoffLocation: String = "شارع جامعة الدول العربية",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val tripId: String,
    val sender: String, // "USER" or "CAPTAIN"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CaptainWallet(
    val captainId: String = "CAPTAIN_123",
    val totalEarnings: Double = 1850.0,
    val commissionRate: Double = 0.10,
    val completedTripsCount: Int = 34,
    val ratingAvg: Float = 4.9f
) {
    val appCommission: Double
        get() = totalEarnings * commissionRate

    val netWallet: Double
        get() = totalEarnings - appCommission
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val amount: Double,
    val isDeduction: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class RatingRecord(
    val id: String,
    val targetUserId: String,
    val tripId: String,
    val rating: Float,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
