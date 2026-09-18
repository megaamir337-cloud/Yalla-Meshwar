package com.example.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MeshwarRepository {
    private val _captainApplications = MutableStateFlow<List<CaptainApplication>>(
        listOf(
            CaptainApplication(
                id = "CAP-101",
                phone = "01098765432",
                idFrontAttached = true,
                idBackAttached = true,
                licenseAttached = true,
                selfieAttached = true,
                vehicleType = "سكوتر",
                status = ApplicationStatus.PENDING,
                submissionDate = "اليوم 11:15 ص"
            ),
            CaptainApplication(
                id = "CAP-102",
                phone = "01123456789",
                idFrontAttached = true,
                idBackAttached = true,
                licenseAttached = true,
                selfieAttached = true,
                vehicleType = "توك توك",
                status = ApplicationStatus.PENDING,
                submissionDate = "اليوم 09:40 ص"
            )
        )
    )
    val captainApplications: StateFlow<List<CaptainApplication>> = _captainApplications.asStateFlow()

    private val _activeOrder = MutableStateFlow<RideOrder?>(null)
    val activeOrder: StateFlow<RideOrder?> = _activeOrder.asStateFlow()

    private val _activeTrips = MutableStateFlow<Map<String, TripModel>>(emptyMap())
    val activeTrips: StateFlow<Map<String, TripModel>> = _activeTrips.asStateFlow()

    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "default" to listOf(
                ChatMessage(
                    id = "msg_0",
                    tripId = "default",
                    sender = "CAPTAIN",
                    message = "السلام عليكم، أنا في طريقي إليك الآن يا فندم",
                    timestamp = System.currentTimeMillis() - 60000
                )
            )
        )
    )
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    private val _captainWallet = MutableStateFlow(CaptainWallet())
    val captainWallet: StateFlow<CaptainWallet> = _captainWallet.asStateFlow()

    private val _walletTransactions = MutableStateFlow(
        listOf(
            WalletTransaction("tx_1", "رحلة سكوتر (التحرير - المهندسين)", 65.0),
            WalletTransaction("tx_2", "توصيل طلب (الدقي)", 35.0),
            WalletTransaction("tx_3", "رحلة توك توك (فيصل)", 40.0),
            WalletTransaction("tx_4", "سحب فودافون كاش", 500.0, isDeduction = true),
            WalletTransaction("tx_5", "مشوار ملاكي (المطار)", 110.0)
        )
    )
    val walletTransactions: StateFlow<List<WalletTransaction>> = _walletTransactions.asStateFlow()

    private val _ratings = MutableStateFlow<List<RatingRecord>>(emptyList())
    val ratings: StateFlow<List<RatingRecord>> = _ratings.asStateFlow()

    fun addCaptainApplication(application: CaptainApplication) {
        _captainApplications.value = listOf(application) + _captainApplications.value
    }

    fun updateCaptainStatus(id: String, newStatus: ApplicationStatus) {
        _captainApplications.value = _captainApplications.value.map { app ->
            if (app.id == id) app.copy(status = newStatus) else app
        }
    }

    fun createOrder(order: RideOrder) {
        _activeOrder.value = order
    }

    fun cancelOrder() {
        _activeOrder.value = null
    }

    fun publishTrip(trip: TripModel) {
        _activeTrips.value = _activeTrips.value + (trip.tripId to trip)
    }

    fun updateTripStatus(tripId: String, status: String) {
        val trip = _activeTrips.value[tripId]
        if (trip != null) {
            _activeTrips.value = _activeTrips.value + (tripId to trip.copy(status = status))
        }
    }

    fun sendChatMessage(message: ChatMessage) {
        val list = _chatMessages.value[message.tripId] ?: emptyList()
        _chatMessages.value = _chatMessages.value + (message.tripId to (list + message))
    }

    fun submitRating(record: RatingRecord) {
        _ratings.value = _ratings.value + record
    }

    fun requestWithdrawal(amount: Double) {
        val current = _captainWallet.value
        val newTotal = (current.totalEarnings - (amount / (1.0 - current.commissionRate))).coerceAtLeast(0.0)
        _captainWallet.value = current.copy(totalEarnings = newTotal)
        _walletTransactions.value = listOf(
            WalletTransaction(
                id = "tx_${System.currentTimeMillis()}",
                title = "سحب إلى فودافون كاش",
                amount = amount,
                isDeduction = true
            )
        ) + _walletTransactions.value
    }

    fun onCaptainAcceptedTrip(
        tripId: String,
        captainId: String,
        captainName: String,
        captainPhone: String,
        captainVehicle: String,
        captainPlate: String,
        captainPhotoUrl: String,
        captainRating: Double
    ) {
        val currentOrder = _activeOrder.value
        if (currentOrder != null) {
            _activeOrder.value = currentOrder.copy(
                status = "ACCEPTED",
                captainName = captainName,
                captainPhone = captainPhone,
                vehiclePlate = "$captainVehicle | $captainPlate",
                captainPhotoUrl = captainPhotoUrl,
                captainRating = captainRating
            )
        }
    }
}
