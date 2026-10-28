package com.hayesenterprisesllc.onthaset.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.LocationParts
import com.hayesenterprisesllc.onthaset.data.NearbyEvent
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.location.LocationManager
import com.hayesenterprisesllc.onthaset.ui.ads.PremiumAdStrip
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ImagePlaceholder
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class TimeFilter(val display: String) { TODAY("Today"), THIS_WEEK("This Week"), NEXT_MONTH("Month"), ALL("All Upcoming") }

/** Port of NearbyEventsView. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyEventsScreen(onBack: () -> Unit, onEventClick: (NearbyEvent) -> Unit, onAdClick: (SupabaseAd) -> Unit) {
    val scope = rememberCoroutineScope()
    val userLocation by LocationManager.userLocation.collectAsState()
    var events by remember { mutableStateOf<List<NearbyEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var radius by rememberSaveable { mutableDoubleStateOf(100.0) }
    var timeFilter by rememberSaveable { mutableStateOf(TimeFilter.NEXT_MONTH) }
    var hasFetched by remember { mutableStateOf(false) }

    suspend fun fetch() {
        val loc = LocationManager.userLocation.value
        val lat = loc?.latitude ?: 36.1699
        val lng = loc?.longitude ?: -115.1398
        isLoading = true
        runCatching { SupabaseManager.fetchNearbyEvents(lat, lng, if (radius >= 9999) 9999.0 else radius) }
            .onSuccess { events = it; hasFetched = true }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        LocationManager.requestLocation()
        delay(2000)
        fetch()
    }
    LaunchedEffect(userLocation) { if (userLocation != null && !hasFetched) fetch() }

    val now = Instant.now()
    val filtered = events.filter { e ->
        if (e.date.isBefore(now)) return@filter false
        when (timeFilter) {
            TimeFilter.TODAY -> e.date.atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
            TimeFilter.THIS_WEEK -> !e.date.isAfter(now.plus(7, ChronoUnit.DAYS))
            TimeFilter.NEXT_MONTH -> !e.date.isAfter(now.atZone(ZoneId.systemDefault()).plusMonths(1).toInstant())
            TimeFilter.ALL -> true
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.Yellow) }
                ShieldLogo(70.dp, Modifier.align(Alignment.Center))
                IconButton(onClick = {
                    hasFetched = false
                    LocationManager.requestLocation()
                    scope.launch { fetch() }
                }, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Filled.Refresh, "Refresh", tint = AppColors.Yellow) }
            }

            Column(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Time Range", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeFilter.entries.forEach { f -> Chip(f.display, timeFilter == f) { timeFilter = f; scope.launch { fetch() } } }
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 15.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (radius >= 9999) "Search Radius: MAX" else "Search Radius: ${radius.toInt()} miles", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(100.0, 200.0, 300.0).forEach { r -> Chip("${r.toInt()}mi", radius == r) { radius = r; scope.launch { fetch() } } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(400.0, 500.0).forEach { r -> Chip("${r.toInt()}mi", radius == r) { radius = r; scope.launch { fetch() } } }
                    Chip("MAX", radius >= 9999) { radius = 99999.0; scope.launch { fetch() } }
                }
            }

            when {
                isLoading -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = AppColors.Yellow)
                    Text("Finding events near you...", color = AppColors.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                userLocation == null && !hasFetched -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
                    CircularProgressIndicator(color = AppColors.Yellow)
                    Text("Finding your location...", color = AppColors.Gray, fontSize = 17.sp)
                    SmallButton("SEARCH ANYWAY", AppColors.Yellow) { LocationManager.requestLocation(); scope.launch { fetch() } }
                }
                filtered.isEmpty() -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
                    Icon(Icons.Filled.PinDrop, null, tint = AppColors.Yellow.copy(alpha = 0.3f), modifier = Modifier.size(50.dp))
                    Text("No events found", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (radius >= 9999) "No events in the ${timeFilter.display.lowercase()} timeframe" else "No events within ${radius.toInt()} miles",
                        color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (radius < 99999) SmallButton("EXPAND RADIUS", AppColors.Yellow) { radius = minOf(radius + 100, 99999.0); scope.launch { fetch() } }
                        if (timeFilter != TimeFilter.ALL) SmallButton("SHOW ALL", AppColors.Yellow.copy(alpha = 0.7f)) { timeFilter = TimeFilter.ALL; scope.launch { fetch() } }
                    }
                }
                else -> Column(Modifier.fillMaxSize()) {
                    Row(Modifier.fillMaxWidth().background(AppColors.Card).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("${filtered.size} event${if (filtered.size == 1) "" else "s"} found", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(timeFilter.display, color = AppColors.Gray, fontSize = 11.sp)
                    }
                    var refreshing by remember { mutableStateOf(false) }
                    PullToRefreshBox(isRefreshing = refreshing, onRefresh = { scope.launch { refreshing = true; fetch(); refreshing = false } }) {
                        LazyColumn(Modifier.fillMaxSize()) {
                            item { Box(Modifier.padding(top = 4.dp)) { PremiumAdStrip(onAdClick) } }
                            items(filtered, key = { it.id }) { e ->
                                NearbyEventRow(e, Modifier.clickable { onEventClick(e) }.padding(horizontal = 16.dp, vertical = 8.dp))
                                HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text, color = if (selected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(15.dp)).background(if (selected) AppColors.Yellow else Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun SmallButton(text: String, bg: Color, onClick: () -> Unit) {
    Text(
        text, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(bg).clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
fun NearbyEventRow(event: NearbyEvent, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val ph: @Composable () -> Unit = { ImagePlaceholder(Modifier.size(60.dp)) }
        if (!event.imageURL.isNullOrEmpty()) AppImage(event.imageURL, Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)), placeholder = ph) else ph()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(event.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = AppColors.Yellow, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(8.dp))
                Text(LocationParts.short(event.locationName), color = AppColors.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(String.format(Locale.US, "%.1f mi away", event.distanceMiles), color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, null, tint = AppColors.Yellow, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(8.dp))
                Text(DateUtils.abbreviatedWithTime(event.date), color = AppColors.Gray, fontSize = 12.sp)
            }
        }
    }
}
