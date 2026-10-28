package com.hayesenterprisesllc.onthaset.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.hayesenterprisesllc.onthaset.data.Http
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

enum class LocationAuthStatus { NOT_DETERMINED, DENIED, AUTHORIZED }

/**
 * Singleton location service — ported from LocationManager.swift (CoreLocation → Fused Location).
 */
object LocationManager {
    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation

    private val _authorizationStatus = MutableStateFlow(LocationAuthStatus.NOT_DETERMINED)
    val authorizationStatus: StateFlow<LocationAuthStatus> = _authorizationStatus

    /** Set by MainActivity: launches the runtime permission dialog. */
    var permissionRequester: (() -> Unit)? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        refreshStatus()
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun refreshStatus() {
        if (hasPermission()) _authorizationStatus.value = LocationAuthStatus.AUTHORIZED
    }

    /** Called from MainActivity's permission callback. */
    fun onPermissionResult(granted: Boolean) {
        _authorizationStatus.value = if (granted) LocationAuthStatus.AUTHORIZED else LocationAuthStatus.DENIED
        if (granted) requestLocation()
    }

    fun requestLocation() {
        if (!hasPermission()) {
            Http.log("📍 Requesting authorization...")
            permissionRequester?.invoke()
            return
        }
        _authorizationStatus.value = LocationAuthStatus.AUTHORIZED
        scope.launch { fetchCurrentLocation() }
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): Location? {
        if (!hasPermission()) return null
        return runCatching {
            val client = LocationServices.getFusedLocationProviderClient(appContext)
            val loc = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()
                ?: client.lastLocation.await()
            if (loc != null) {
                _userLocation.value = loc
                Http.log("✅ Location updated: ${loc.latitude}, ${loc.longitude}")
            }
            loc
        }.onFailure { Http.log("❌ Location error: $it") }.getOrNull()
    }

    fun distanceMiles(lat: Double, lng: Double): Double? {
        val user = _userLocation.value ?: return null
        if (lat == 0.0 && lng == 0.0) return null
        val out = FloatArray(1)
        Location.distanceBetween(user.latitude, user.longitude, lat, lng, out)
        return out[0] / 1609.34
    }

    fun distanceString(lat: Double, lng: Double): String? {
        val miles = distanceMiles(lat, lng) ?: return null
        return if (miles < 1) String.format(Locale.US, "%.1f mi", miles) else String.format(Locale.US, "%.0f mi", miles)
    }

    // ── Geocoding (CLGeocoder replacement) ───────────────────────────────────

    @Suppress("DEPRECATION")
    suspend fun geocode(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (address.isBlank() || !Geocoder.isPresent()) return@withContext null
        runCatching {
            Geocoder(appContext, Locale.US).getFromLocationName(address, 1)
                ?.firstOrNull()?.let { it.latitude to it.longitude }
        }.onFailure { Http.log("⚠️ Could not geocode address: $address — $it") }.getOrNull()
    }

    @Suppress("DEPRECATION")
    suspend fun reverseGeocodeCity(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        runCatching {
            Geocoder(appContext, Locale.US).getFromLocation(lat, lng, 1)
                ?.firstOrNull()?.let { it.locality ?: it.adminArea }
        }.getOrNull()
    }

    fun haversineMiles(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 3958.8
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}
