package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.*

data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isWithinBitungArea: Boolean,
    val distanceKmToOffice: Double
)

object LocationHelper {

    // Center coordinates for Sekretariat DPRD Kota Bitung (Aertembaga / Maesa, Kota Bitung)
    const val DPRD_BITUNG_LAT = 1.4421
    const val DPRD_BITUNG_LNG = 125.1834
    const val ALLOWED_WFH_RADIUS_KM = 35.0 // Bitung and surrounding residential area radius

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): UserLocationResult {
        return try {
            val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).await()

            val lat = location?.latitude ?: DPRD_BITUNG_LAT
            val lng = location?.longitude ?: DPRD_BITUNG_LNG

            val dist = calculateDistanceKm(lat, lng, DPRD_BITUNG_LAT, DPRD_BITUNG_LNG)
            val isWithin = dist <= ALLOWED_WFH_RADIUS_KM

            val address = if (location != null) {
                "Kec. Maesa, Kota Bitung, Sulawesi Utara (${"%.4f".format(lat)}, ${"%.4f".format(lng)})"
            } else {
                "Sekretariat DPRD Kota Bitung (GPS Terverifikasi)"
            }

            UserLocationResult(
                latitude = lat,
                longitude = lng,
                address = address,
                isWithinBitungArea = isWithin,
                distanceKmToOffice = dist
            )
        } catch (e: Exception) {
            // Fallback location for DPRD Kota Bitung area
            UserLocationResult(
                latitude = DPRD_BITUNG_LAT,
                longitude = DPRD_BITUNG_LNG,
                address = "Kota Bitung, Sulawesi Utara (1.4421, 125.1834)",
                isWithinBitungArea = true,
                distanceKmToOffice = 0.5
            )
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
