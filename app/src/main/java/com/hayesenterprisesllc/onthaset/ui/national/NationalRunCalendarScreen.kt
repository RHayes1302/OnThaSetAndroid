package com.hayesenterprisesllc.onthaset.ui.national

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.hayesenterprisesllc.onthaset.data.EventCategory
import com.hayesenterprisesllc.onthaset.data.LocationParts
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.events.NavigationChooser
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

object NationalStyle {
    fun pinHex(cat: EventCategory?): String = when (cat) {
        EventCategory.RALLY -> "#FF3B30"
        EventCategory.CHARITY -> "#007AFF"
        EventCategory.MC_ANNUAL -> "#FFD60A"
        EventCategory.SC_ANNUAL -> "#FF69B4"
        EventCategory.RC_ANNUAL -> "#34C759"
        EventCategory.UNITY_RUN -> "#AF52DE"
        else -> "#8E8E93"
    }

    fun pinColor(cat: EventCategory?): Color = when (cat) {
        EventCategory.RALLY -> AppColors.Red
        EventCategory.CHARITY -> AppColors.Blue
        EventCategory.MC_ANNUAL -> AppColors.Yellow
        EventCategory.SC_ANNUAL -> Color(1.0f, 0.4f, 0.7f)
        EventCategory.RC_ANNUAL -> AppColors.Green
        EventCategory.UNITY_RUN -> AppColors.Purple
        else -> AppColors.Gray
    }

    private val stateAbbr = mapOf(
        "Alabama" to "AL", "Alaska" to "AK", "Arizona" to "AZ", "Arkansas" to "AR", "California" to "CA",
        "Colorado" to "CO", "Connecticut" to "CT", "Delaware" to "DE", "Florida" to "FL", "Georgia" to "GA",
        "Hawaii" to "HI", "Idaho" to "ID", "Illinois" to "IL", "Indiana" to "IN", "Iowa" to "IA", "Kansas" to "KS",
        "Kentucky" to "KY", "Louisiana" to "LA", "Maine" to "ME", "Maryland" to "MD", "Massachusetts" to "MA",
        "Michigan" to "MI", "Minnesota" to "MN", "Mississippi" to "MS", "Missouri" to "MO", "Montana" to "MT",
        "Nebraska" to "NE", "Nevada" to "NV", "New Hampshire" to "NH", "New Jersey" to "NJ", "New Mexico" to "NM",
        "New York" to "NY", "North Carolina" to "NC", "North Dakota" to "ND", "Ohio" to "OH", "Oklahoma" to "OK",
        "Oregon" to "OR", "Pennsylvania" to "PA", "Rhode Island" to "RI", "South Carolina" to "SC",
        "South Dakota" to "SD", "Tennessee" to "TN", "Texas" to "TX", "Utah" to "UT", "Vermont" to "VT",
        "Virginia" to "VA", "Washington" to "WA", "West Virginia" to "WV", "Wisconsin" to "WI", "Wyoming" to "WY",
        "Puerto Rico" to "PR", "Washington DC" to "DC", "District of Columbia" to "DC"
    )

    fun abbreviation(fullName: String): String = stateAbbr[fullName] ?: fullName

    fun stateOf(e: SupabaseEvent): String = LocationParts.split(e.locationName).let { if (it.size >= 4) it[3] else "" }
    fun cityOf(e: SupabaseEvent): String = LocationParts.split(e.locationName).let { if (it.size >= 3) it[2] else e.locationName }

    fun pinsJson(events: List<SupabaseEvent>): String = JSONArray().apply {
        events.forEach { e ->
            val id = e.id ?: return@forEach
            val cat = EventCategory.fromRaw(e.category)
            put(JSONObject().apply {
                put("id", id); put("lat", e.latitude); put("lng", e.longitude)
                put("color", pinHex(cat)); put("icon", cat?.icon ?: "📍"); put("title", e.title); put("state", stateOf(e))
            })
        }
    }.toString()
}

/** Bridge for the Leaflet page (replaces WKScriptMessageHandler). */
private class MapBridge(val onState: (String) -> Unit, val onEvent: (String) -> Unit) {
    private val main = Handler(Looper.getMainLooper())
    @JavascriptInterface fun stateTapped(name: String) { main.post { onState(name) } }
    @JavascriptInterface fun eventTapped(id: String) { main.post { onEvent(id) } }
    @JavascriptInterface fun mapReady() {}
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NationalRunCalendarScreen(onBack: () -> Unit, onEventClick: (SupabaseEvent) -> Unit) {
    val context = LocalContext.current
    val events by SupabaseManager.events.collectAsState()
    val isLoading by SupabaseManager.isLoading.collectAsState()
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var isMap by rememberSaveable { mutableStateOf(true) }
    val today = LocalDate.now()
    var month by rememberSaveable { mutableIntStateOf(today.monthValue) }
    var year by rememberSaveable { mutableIntStateOf(today.year) }
    var selectedState by rememberSaveable { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) { SupabaseManager.fetchAllEvents() }

    val national = events.filter { e ->
        val cat = EventCategory.fromRaw(e.category) ?: return@filter false
        cat.isNationalEvent && e.latitude != 0.0 && e.longitude != 0.0 && (category == null || cat.rawValue == category)
    }
    val byMonth = national.filter { e ->
        val d = e.date.atZone(ZoneId.systemDefault())
        d.monthValue == month && d.year == year
    }
    val inState = selectedState?.let { st ->
        byMonth.filter { e ->
            val es = NationalStyle.stateOf(e)
            es.equals(st, true) || NationalStyle.abbreviation(st).equals(es, true)
        }
    } ?: emptyList()

    val pinsJson = NationalStyle.pinsJson(byMonth)
    LaunchedEffect(pinsJson, webView) { webView?.evaluateJavascript("if (typeof updatePins==='function') updatePins($pinsJson);", null) }

    fun resetMap() { webView?.evaluateJavascript("resetToUSA();", null) }
    fun back() { if (selectedState != null) { selectedState = null; resetMap() } else onBack() }
    BackHandler(enabled = selectedState != null) { back() }

    val currentByMonth by rememberUpdatedState(byMonth)

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            // HEADER
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clickable { back() }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.Yellow)
                    selectedState?.let { Text(" $it", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.weight(1f))
                ShieldLogo(45.dp)
                Spacer(Modifier.weight(1f))
                ModeButton(Icons.Filled.Map, isMap) { isMap = true }
                Spacer(Modifier.width(4.dp))
                ModeButton(Icons.AutoMirrored.Filled.List, !isMap) { isMap = false }
            }
            Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🗺️ NATIONAL RUN CALENDAR", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                if (selectedState != null) Text("$selectedState Events", color = AppColors.Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                else Text("Coast to Coast • All Clubs Welcome", color = AppColors.Gray, fontSize = 12.sp)
            }
            // CATEGORY FILTER
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("ALL", null, category == null) { category = null }
                EventCategory.entries.filter { it.isNationalEvent }.forEach { cat ->
                    FilterChip(cat.displayName, NationalStyle.pinColor(cat), category == cat.rawValue) {
                        category = if (category == cat.rawValue) null else cat.rawValue
                    }
                }
            }
            // MONTH SELECTOR
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ChevronLeft, "Previous", tint = AppColors.Yellow, modifier = Modifier.clickable {
                    if (month == 1) { month = 12; year -= 1 } else month -= 1
                })
                Text(DateUtils.monthYear(LocalDate.of(year, month, 1)), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(120.dp))
                Icon(Icons.Filled.ChevronRight, "Next", tint = AppColors.Yellow, modifier = Modifier.clickable {
                    if (month == 12) { month = 1; year += 1 } else month += 1
                })
                Spacer(Modifier.weight(1f))
                if (selectedState != null) {
                    Row(
                        Modifier.clip(RoundedCornerShape(6.dp)).background(Color.White.copy(alpha = 0.1f))
                            .clickable { selectedState = null; resetMap() }.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Refresh, null, tint = AppColors.Yellow, modifier = Modifier.size(12.dp))
                        Text(" ALL STATES", color = AppColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text("${byMonth.size} event${if (byMonth.size == 1) "" else "s"}", color = AppColors.Gray, fontSize = 11.sp)
            }

            when {
                isLoading && events.isEmpty() -> Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = AppColors.Yellow)
                    Text("Loading national events...", color = AppColors.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                isMap -> Column(Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(if (selectedState != null) 280.dp else 380.dp),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                setBackgroundColor(android.graphics.Color.rgb(26, 26, 26))
                                webViewClient = WebViewClient()
                                addJavascriptInterface(
                                    MapBridge(
                                        onState = { selectedState = it },
                                        onEvent = { id -> currentByMonth.firstOrNull { it.id.equals(id, true) }?.let(onEventClick) }
                                    ), "Android"
                                )
                                val template = ctx.assets.open("us_map.html").bufferedReader().use { it.readText() }
                                val html = template.replaceFirst("<body>", "<body><script>window.__initialPins=$pinsJson;</script>")
                                loadDataWithBaseURL("https://unpkg.com", html, "text/html", "utf-8", null)
                                webView = this
                            }
                        }
                    )
                    val st = selectedState
                    if (st != null) {
                        if (inState.isEmpty()) {
                            Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.LocationOff, null, tint = AppColors.Yellow.copy(alpha = 0.4f), modifier = Modifier.size(30.dp))
                                Text("No events in $st this month", color = AppColors.Gray, fontSize = 15.sp)
                            }
                        } else {
                            LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Text("${inState.size} event${if (inState.size == 1) "" else "s"} in $st", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                                items(inState.sortedBy { it.date }) { e ->
                                    NationalEventRow(e, Modifier.padding(horizontal = 16.dp).clickable { onEventClick(e) })
                                }
                            }
                        }
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (byMonth.isEmpty()) item {
                        Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
                            Icon(Icons.Filled.EventBusy, null, tint = AppColors.Yellow.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                            Text("No national events this month", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text("Try a different month or category", color = AppColors.Gray, fontSize = 15.sp)
                        }
                    } else items(byMonth.sortedBy { it.date }) { e ->
                        NationalEventRow(e, Modifier.clickable { onEventClick(e) })
                    }
                }
            }
        }
    }
    DisposableEffect(Unit) { onDispose { webView?.destroy(); webView = null } }
}

@Composable
private fun ModeButton(icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(8.dp)).background(if (selected) AppColors.Yellow else Color.White.copy(alpha = 0.1f)).clickable(onClick = onClick).padding(8.dp)
    ) { Icon(icon, null, tint = if (selected) Color.Black else AppColors.Yellow) }
}

@Composable
private fun FilterChip(text: String, dot: Color?, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(15.dp)).background(if (selected) AppColors.Yellow else Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot != null) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = if (selected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NationalEventRow(event: SupabaseEvent, modifier: Modifier = Modifier) {
    val cat = EventCategory.fromRaw(event.category)
    val state = NationalStyle.stateOf(event)
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card)
            .border(1.dp, if (cat != null) NationalStyle.pinColor(cat).copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.width(45.dp).clip(RoundedCornerShape(10.dp)).background(AppColors.Card).padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(DateUtils.monthShort(event.date).lowercase().replaceFirstChar { it.uppercase() }, color = AppColors.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(DateUtils.dayOfMonth(event.date), color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(event.date.atZone(ZoneId.systemDefault()).year.toString(), color = AppColors.Gray, fontSize = 9.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (cat != null) Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).shadow(3.dp, CircleShape).clip(CircleShape).background(NationalStyle.pinColor(cat)))
                Spacer(Modifier.width(6.dp))
                Text(cat.displayName, color = AppColors.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(event.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = AppColors.Yellow, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(NationalStyle.cityOf(event) + if (state.isEmpty()) "" else ", $state", color = AppColors.Gray, fontSize = 12.sp)
            }
            Text(DateUtils.time(event.date), color = AppColors.Gray, fontSize = 11.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = AppColors.Gray)
    }
}

/** NationalEventDetailSheet */
@Composable
fun NationalEventDetailScreen(event: SupabaseEvent, onClose: () -> Unit, onWeather: () -> Unit, onShare: () -> Unit) {
    val cat = EventCategory.fromRaw(event.category)
    val venue = LocationParts.split(event.locationName).firstOrNull() ?: event.title
    val state = NationalStyle.stateOf(event)
    var showNav by remember { mutableStateOf(false) }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onClose, title = "National Run Calendar", backIcon = Icons.Filled.Close)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (!event.imageURL.isNullOrEmpty()) {
                    AppImage(
                        event.imageURL,
                        Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(220.dp).clip(RoundedCornerShape(15.dp)),
                        contentScale = ContentScale.Fit, placeholder = {}
                    )
                }
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (cat != null) Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(NationalStyle.pinColor(cat)))
                        Spacer(Modifier.width(8.dp))
                        Text(cat.displayName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(event.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                Column(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = AppColors.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Text(DateUtils.longWithTime(event.date), color = Color.White)
                    }
                    Row {
                        Icon(Icons.Filled.LocationOn, null, tint = AppColors.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(venue, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(NationalStyle.cityOf(event) + if (state.isEmpty()) "" else ", $state", color = AppColors.Gray, fontSize = 15.sp)
                        }
                    }
                }
                if (event.details.isNotEmpty()) Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DETAILS", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(event.details, color = Color.White)
                }
                Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(AppColors.Blue.copy(alpha = 0.2f))
                                .border(1.dp, AppColors.Blue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).clickable(onClick = onWeather).padding(16.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.WbCloudy, null, tint = AppColors.Yellow)
                            Spacer(Modifier.width(6.dp))
                            Text("WEATHER", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, AppColors.Yellow, RoundedCornerShape(12.dp)).clickable { showNav = true }.padding(16.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Navigation, null, tint = AppColors.Yellow)
                            Spacer(Modifier.width(6.dp))
                            Text("DIRECTIONS", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    YellowButton("SHARE EVENT", onShare, corner = 12.dp, leading = Icons.Filled.Share)
                }
            }
        }
    }
    if (showNav) NavigationChooser(event.latitude, event.longitude, venue, event.locationName.replace("|", " ")) { showNav = false }
}
