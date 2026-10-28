package com.hayesenterprisesllc.onthaset.ui.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hayesenterprisesllc.onthaset.R
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.location.LocationAuthStatus
import com.hayesenterprisesllc.onthaset.location.LocationManager
import com.hayesenterprisesllc.onthaset.ui.ads.PremiumAdStrip
import com.hayesenterprisesllc.onthaset.ui.components.LoadingOverlay
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.Intents
import com.hayesenterprisesllc.onthaset.weather.WeatherViewModel

/** Which of the three iOS weather views this screen represents. */
sealed class WeatherSource {
    data object Gps : WeatherSource()                                              // WeatherView
    data class Coordinates(val lat: Double, val lng: Double, val name: String) : WeatherSource() // WeatherViewForCoordinates
    data class City(val city: String) : WeatherSource()                            // WeatherViewForEvent
}

fun weatherIcon(code: Int): Pair<ImageVector, Color> = when (WeatherViewModel.iconKind(code)) {
    "sun" -> Icons.Filled.WbSunny to Color(0xFFFFCC00)
    "cloud_sun" -> Icons.Filled.FilterDrama to Color(0xFFFFB340)
    "fog" -> Icons.Filled.Cloud to Color(0xFFB0B0B5)
    "rain" -> Icons.Filled.Umbrella to Color(0xFF64B5F6)
    "storm" -> Icons.Filled.FlashOn to Color(0xFF7E57C2)
    else -> Icons.Filled.WbCloudy to Color(0xFF9E9E9E)
}

@Composable
fun WeatherScreen(source: WeatherSource, onBack: () -> Unit, onAdClick: (SupabaseAd) -> Unit) {
    val context = LocalContext.current
    val vm: WeatherViewModel = viewModel()
    val s by vm.state.collectAsState()
    val userLocation by LocationManager.userLocation.collectAsState()
    val auth by LocationManager.authorizationStatus.collectAsState()
    var hasLoaded by remember { mutableStateOf(false) }

    when (source) {
        is WeatherSource.Gps -> {
            LaunchedEffect(Unit) { LocationManager.requestLocation() }
            LaunchedEffect(userLocation) {
                val loc = userLocation
                if (loc != null && !hasLoaded) { hasLoaded = true; vm.fetchWeatherByLocation(loc.latitude, loc.longitude) }
            }
        }
        is WeatherSource.Coordinates -> LaunchedEffect(source) { vm.searchWeatherByCoordinates(source.lat, source.lng, source.name) }
        is WeatherSource.City -> LaunchedEffect(source) { vm.searchWeather(source.city) }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(painterResource(R.drawable.road), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(25.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShieldLogo(80.dp, Modifier.padding(top = 90.dp))
            Text(
                "Ride Forecast", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif, textAlign = TextAlign.Center
            )

            when (source) {
                is WeatherSource.Gps -> if (userLocation != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (s.cityName.isEmpty()) "Loading..." else s.cityName.uppercase(), color = AppColors.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("📍 Your Location", color = AppColors.Gray, fontSize = 12.sp)
                    }
                } else {
                    Column(Modifier.padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        Icon(Icons.Filled.LocationOff, null, tint = AppColors.Yellow.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                        Text("Location Not Available", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        if (auth == LocationAuthStatus.DENIED) {
                            Text("Please enable location services in Settings", color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                            SmallYellowButton("OPEN SETTINGS") { Intents.openAppSettings(context) }
                        } else {
                            Text("Requesting location...", color = AppColors.Gray, fontSize = 12.sp)
                            SmallYellowButton("ENABLE LOCATION") { LocationManager.requestLocation() }
                        }
                    }
                }
                is WeatherSource.Coordinates -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(source.name.uppercase(), color = AppColors.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("📍 Event Location", color = AppColors.Gray, fontSize = 11.sp)
                }
                is WeatherSource.City -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (s.cityName.isEmpty()) "Loading ${source.city}..." else s.cityName.uppercase(), color = AppColors.Yellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (s.cityName.isEmpty() && !s.isLoading) Text("Unable to load weather for ${source.city}", color = AppColors.Red, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            }

            // RIDE SAFETY BANNER
            if (s.rideSafetyMessage.isNotEmpty()) {
                Row(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(s.rideSafetyColor.copy(alpha = 0.95f)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShieldLogo(45.dp)
                    Spacer(Modifier.width(15.dp))
                    Column {
                        Text(s.rideSafetyMessage, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        val label = if (source is WeatherSource.Coordinates) source.name else s.cityName
                        Text(label.uppercase(), color = Color.White, fontSize = 11.sp, letterSpacing = 2.sp)
                    }
                }
            }

            PremiumAdStrip(onAdClick)

            // RIGHT NOW
            if (s.currentTemp.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("RIGHT NOW", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.08f)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (icon, tint) = weatherIcon(s.currentWeatherCode)
                        Icon(icon, null, tint = tint, modifier = Modifier.size(60.dp))
                        Spacer(Modifier.width(20.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(s.currentTemp, color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Black, lineHeight = 64.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Air, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Wind: ${s.currentWindSpeed}", color = AppColors.Gray, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            // 7-DAY
            if (s.dailyForecasts.isNotEmpty()) {
                Text("7-DAY FORECAST", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color.White)) {
                    s.dailyForecasts.forEachIndexed { i, day ->
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(day.day, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(75.dp))
                            Spacer(Modifier.weight(1f))
                            val (icon, tint) = weatherIcon(day.iconCode)
                            Icon(icon, null, tint = tint)
                            Spacer(Modifier.weight(1f))
                            Row(Modifier.width(90.dp), horizontalArrangement = Arrangement.End) {
                                Text(day.lowTemp, color = Color.Black.copy(alpha = 0.7f))
                                Text(" / ", color = Color.Black)
                                Text(day.highTemp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (i < s.dailyForecasts.lastIndex) HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.3f))
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }

        IconButton(
            onClick = { vm.reset(); onBack() },
            modifier = Modifier.statusBarsPadding().padding(start = 20.dp, top = 10.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.6f))
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.Yellow) }

        if (s.isLoading) LoadingOverlay("ANALYZING...")
    }
}

@Composable
private fun SmallYellowButton(text: String, onClick: () -> Unit) {
    Text(
        text, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(AppColors.Yellow).clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp)
    )
}
