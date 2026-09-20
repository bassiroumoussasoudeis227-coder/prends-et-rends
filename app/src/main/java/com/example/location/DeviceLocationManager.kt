package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class LocationResultData(
    val latitude: Double,
    val longitude: Double,
    val address: String?
)

class DeviceLocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        if (cont.isActive) cont.resume(Pair(loc.latitude, loc.longitude))
                    } else {
                        // Request single fresh update
                        requestSingleLocationUpdate { freshLoc ->
                            if (cont.isActive) {
                                if (freshLoc != null) cont.resume(Pair(freshLoc.latitude, freshLoc.longitude))
                                else cont.resume(null)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        } catch (_: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestSingleLocationUpdate(callback: (Location?) -> Unit) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    callback(result.lastLocation)
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (_: Exception) {
            callback(null)
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (cont.isActive) {
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                val street = addr.thoroughfare ?: addr.featureName ?: ""
                                val locality = addr.locality ?: addr.subAdminArea ?: ""
                                val country = addr.countryName ?: ""
                                val formatted = listOf(street, locality, country)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                cont.resume(if (formatted.isNotBlank()) formatted else "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}")
                            } else {
                                cont.resume("${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}")
                            }
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    val street = addr.thoroughfare ?: addr.featureName ?: ""
                    val locality = addr.locality ?: addr.subAdminArea ?: ""
                    val country = addr.countryName ?: ""
                    val formatted = listOf(street, locality, country)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                    if (formatted.isNotBlank()) formatted else "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                } else {
                    "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
                }
            }
        } catch (_: Exception) {
            "${String.format(Locale.US, "%.4f", latitude)}, ${String.format(Locale.US, "%.4f", longitude)}"
        }
    }

    suspend fun capturePreciseLocation(): LocationResultData? {
        val coords = getCurrentCoordinates() ?: return null
        val address = reverseGeocode(coords.first, coords.second)
        return LocationResultData(
            latitude = coords.first,
            longitude = coords.second,
            address = address
        )
    }
}
