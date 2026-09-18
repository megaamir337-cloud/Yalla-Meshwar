package com.example

import com.yalla.meshwar.CaptainDetails as YallaCaptainDetails
import com.yalla.meshwar.CaptainAcceptTrip as YallaCaptainAcceptTrip

typealias CaptainDetails = YallaCaptainDetails

object CaptainAcceptTrip {
    @JvmStatic
    fun acceptTripAndSendDetails(tripId: String, captain: YallaCaptainDetails) {
        YallaCaptainAcceptTrip.acceptTripAndSendDetails(tripId, captain)
    }
}
