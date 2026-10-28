package com.hayesenterprisesllc.onthaset.weather

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

/** One row in the 7-day forecast (WeatherModels.swift DayForecast). */
data class DayForecast(
    val id: String = UUID.randomUUID().toString(),
    val day: String,
    val highTemp: String,
    val lowTemp: String,
    val iconCode: Int
)

data class WeatherState(
    val cityName: String = "",
    val dailyForecasts: List<DayForecast> = emptyList(),
    val isLoading: Boolean = false,
    val rideSafetyMessage: String = "",
    val rideSafetyColor: Color = Color(0xFF34C759),
    val errorMessage: String = "",
    val currentTemp: String = "",
    val currentWindSpeed: String = "",
    val currentWeatherCode: Int = 0
)

/** Port of WeatherViewModel.swift — uses the free Open-Meteo API. */
class WeatherViewModel : ViewModel() {
    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state

    suspend fun searchWeather(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        _state.update { it.copy(isLoading = true, errorMessage = "") }
        try {
            val res = Http.getString("https://geocoding-api.open-meteo.com/v1/search?name=${Http.enc(q)}&count=1")
            val loc = res.jsonObject().optJSONArray("results")?.optJSONObject(0)
            if (loc != null) {
                fetchWeatherData(loc.optDouble("latitude"), loc.optDouble("longitude"), loc.optString("name"))
            } else {
                _state.update { it.copy(errorMessage = "Location not found") }
            }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = "Failed to load weather") }
        }
        _state.update { it.copy(isLoading = false) }
    }

    suspend fun searchWeatherByCoordinates(latitude: Double, longitude: Double, locationName: String) {
        _state.update { it.copy(isLoading = true, errorMessage = "") }
        fetchWeatherData(latitude, longitude, locationName)
        _state.update { it.copy(isLoading = false) }
    }

    suspend fun fetchWeatherByLocation(lat: Double, lng: Double) {
        _state.update { it.copy(isLoading = true, errorMessage = "") }
        val city = LocationManager.reverseGeocodeCity(lat, lng) ?: "Your Location"
        fetchWeatherData(lat, lng, city)
        _state.update { it.copy(isLoading = false) }
    }

    private suspend fun fetchWeatherData(lat: Double, lng: Double, name: String) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng" +
            "&current_weather=true&daily=weathercode,temperature_2m_max,temperature_2m_min" +
            "&timezone=auto&temperature_unit=fahrenheit&windspeed_unit=mph"
        try {
            val o = Http.getString(url).jsonObject()
            val current = o.getJSONObject("current_weather")
            val daily = o.getJSONObject("daily")
            val temp = current.getDouble("temperature")
            val wind = current.getDouble("windspeed")
            val code = current.getInt("weathercode")

            val (msg, color) = when {
                wind > 20 -> "DANGEROUS WINDS: HIGH RISK" to Color(0xFFFF3B30)
                wind > 12 -> "CAUTION: STICKY CONDITIONS" to Color(0xFFFFCC00)
                else -> "CLEAR TO RIDE: OPTIMAL" to Color(0xFF34C759)
            }

            val times = daily.getJSONArray("time")
            val codes = daily.getJSONArray("weathercode")
            val maxs = daily.getJSONArray("temperature_2m_max")
            val mins = daily.getJSONArray("temperature_2m_min")
            val forecasts = (0 until times.length()).map { i ->
                DayForecast(
                    day = if (i == 0) "TODAY" else formatDate(times.getString(i)),
                    highTemp = "${maxs.getDouble(i).toInt()}°F",
                    lowTemp = "${mins.getDouble(i).toInt()}°F",
                    iconCode = codes.getInt(i)
                )
            }
            _state.update {
                it.copy(
                    cityName = name,
                    currentTemp = "${temp.toInt()}°F",
                    currentWindSpeed = "${wind.toInt()} mph",
                    currentWeatherCode = code,
                    rideSafetyMessage = msg,
                    rideSafetyColor = color,
                    dailyForecasts = forecasts
                )
            }
        } catch (e: Exception) {
            Http.log("❌ Weather error: $e")
            _state.update { it.copy(errorMessage = "Failed to load weather") }
        }
    }

    private fun formatDate(dateStr: String): String = runCatching {
        LocalDate.parse(dateStr).format(DateTimeFormatter.ofPattern("EEE", Locale.US)).uppercase()
    }.getOrDefault(dateStr)

    fun reset() { _state.value = WeatherState() }

    companion object {
        /** Maps WMO weather codes to a short category used to pick an icon. */
        fun iconKind(code: Int): String = when (code) {
            0 -> "sun"
            1, 2, 3 -> "cloud_sun"
            45, 48 -> "fog"
            in 51..77 -> "rain"
            in 80..99 -> "storm"
            else -> "cloud"
        }
    }
}
