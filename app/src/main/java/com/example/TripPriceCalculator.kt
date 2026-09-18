package com.example

data class LatLng(val latitude: Double, val longitude: Double)

object TripPriceCalculator {
    // Base fares and rates in Egyptian Pounds (EGP)
    const val BASE_FARE_TOKTOK = 10.0
    const val PER_KM_TOKTOK = 5.0

    const val BASE_FARE_SCOOTER = 12.0
    const val PER_KM_SCOOTER = 6.0

    const val BASE_FARE_MALAKI = 20.0
    const val PER_KM_MALAKI = 10.0

    // Haversine formula for distance calculation in kilometers
    @JvmStatic
    fun calculateDistanceKm(start: LatLng, end: LatLng): Double {
        val lat1 = start.latitude
        val lon1 = start.longitude
        val lat2 = end.latitude
        val lon2 = end.longitude

        val theta = lon1 - lon2
        var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta))
        dist = Math.acos(dist.coerceIn(-1.0, 1.0))
        dist = Math.toDegrees(dist)
        dist = dist * 60 * 1.1515 * 1.609344 // Convert to km
        return if (dist.isNaN()) 1.0 else Math.max(1.0, dist)
    }

    // Dynamic price calculation based on category
    @JvmStatic
    fun calculateSuggestedPrice(category: String, distanceKm: Double): Double {
        return when {
            category.contains("توك توك") || category.contains("TOKTOK") -> BASE_FARE_TOKTOK + (distanceKm * PER_KM_TOKTOK)
            category.contains("سكوتر") || category.contains("SCOOTER") -> BASE_FARE_SCOOTER + (distanceKm * PER_KM_SCOOTER)
            else -> BASE_FARE_MALAKI + (distanceKm * PER_KM_MALAKI)
        }
    }
}
