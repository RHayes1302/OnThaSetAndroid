package com.hayesenterprisesllc.onthaset.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.LocationParts
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.ui.ads.PremiumAdStrip
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ChoiceDialog
import com.hayesenterprisesllc.onthaset.ui.components.FullScreenImageViewer
import com.hayesenterprisesllc.onthaset.ui.components.ImagePlaceholder
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.SheetHeader
import com.hayesenterprisesllc.onthaset.ui.components.SmallMap
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.components.YellowDivider
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import com.hayesenterprisesllc.onthaset.util.Intents
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

fun String.capitalizedWords(): String =
    split(" ").joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.titlecase() } }

// ─────────────────────────────────────────────────────────────────────────────
// EventHomeView — list / calendar of Supabase events
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventHomeScreen(
    initialMode: String,
    onBack: () -> Unit,
    onEventClick: (SupabaseEvent) -> Unit,
    onAdClick: (SupabaseAd) -> Unit
) {
    val scope = rememberCoroutineScope()
    val events by SupabaseManager.events.collectAsState()
    val isLoading by SupabaseManager.isLoading.collectAsState()
    var mode by rememberSaveable { mutableStateOf(initialMode) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { SupabaseManager.fetchAllEvents() }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.Yellow)
                }
                ShieldLogo(65.dp, Modifier.align(Alignment.Center))
            }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(16.dp)) {
                listOf("list" to "List", "calendar" to "Calendar").forEachIndexed { i, (key, label) ->
                    SegmentedButton(
                        selected = mode == key,
                        onClick = { mode = key },
                        shape = SegmentedButtonDefaults.itemShape(i, 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AppColors.Yellow, activeContentColor = Color.Black,
                            inactiveContainerColor = AppColors.Yellow.copy(alpha = 0.5f), inactiveContentColor = Color.Black
                        ),
                        icon = {}
                    ) { Text(label, fontWeight = FontWeight.SemiBold) }
                }
            }

            if (mode == "list") {
                PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = { scope.launch { refreshing = true; SupabaseManager.fetchAllEvents(); refreshing = false } },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { PremiumAdStrip(onAdClick) }
                        when {
                            isLoading && events.isEmpty() -> item {
                                Box(Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AppColors.Yellow)
                                }
                            }
                            events.isEmpty() -> item { EmptyEvents() }
                            else -> items(events, key = { it.id ?: it.title }) { e ->
                                SupabaseEventRow(
                                    e,
                                    Modifier.clip(RoundedCornerShape(12.dp)).background(AppColors.Card)
                                        .border(1.dp, AppColors.Yellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .clickable { onEventClick(e) }.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                val dpState = rememberDatePickerState(initialSelectedDateMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                val selected = dpState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() } ?: LocalDate.now()
                val filtered = events.filter { DateUtils.isSameDay(it.date, selected) }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    DatePicker(
                        state = dpState,
                        title = null, headline = null, showModeToggle = false,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.Transparent,
                            selectedDayContainerColor = AppColors.Yellow, selectedDayContentColor = Color.Black,
                            todayDateBorderColor = AppColors.Yellow, todayContentColor = AppColors.Yellow,
                            dayContentColor = Color.White, weekdayContentColor = AppColors.Gray,
                            navigationContentColor = AppColors.Yellow, subheadContentColor = Color.White,
                            yearContentColor = Color.White, selectedYearContainerColor = AppColors.Yellow, selectedYearContentColor = Color.Black
                        ),
                        modifier = Modifier.padding(16.dp).clip(RoundedCornerShape(15.dp)).background(AppColors.Card)
                    )
                    filtered.forEach { e ->
                        SupabaseEventRow(
                            e,
                            Modifier.padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(10.dp))
                                .background(AppColors.Card).clickable { onEventClick(e) }.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyEvents() {
    Column(Modifier.fillMaxWidth().padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.Directions, null, tint = AppColors.Gray, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text("No Events Posted", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("The road is empty. Be the first to post an event!", color = AppColors.Gray, textAlign = TextAlign.Center)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SupabaseEventRow
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SupabaseEventRow(
    event: SupabaseEvent,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val ph: @Composable () -> Unit = { ImagePlaceholder(Modifier.size(70.dp)) }
        if (!event.imageURL.isNullOrEmpty()) {
            AppImage(event.imageURL, Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)), placeholder = ph)
        } else ph()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            SmallInfo(Icons.Filled.CalendarMonth, DateUtils.abbreviatedWithTime(event.date))
            SmallInfo(Icons.Filled.LocationOn, LocationParts.short(event.locationName))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { SmallInfo(Icons.Filled.LocalOffer, event.category.capitalizedWords()) }
                Text("Posted by ${event.postedByName}", color = AppColors.Gray.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (trailing != null) trailing()
    }
}

@Composable
fun SmallInfo(icon: ImageVector, text: String, color: Color = AppColors.Gray) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SupabaseEventDetailView (also used for NearbyEventDetailView)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SupabaseEventDetailScreen(
    event: SupabaseEvent,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onWeather: () -> Unit,
    onPostedBy: () -> Unit,
    distanceMiles: Double? = null
) {
    val context = LocalContext.current
    var showFull by remember { mutableStateOf(false) }
    var showNav by remember { mutableStateOf(false) }
    val parts = LocationParts.split(event.locationName)
    val venue = parts.firstOrNull() ?: event.title

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack, showShield = true, actions = {
                IconButton(onClick = onReport) { Icon(Icons.Outlined.Flag, "Report", tint = AppColors.Red.copy(alpha = 0.8f)) }
                IconButton(onClick = onShare) { Icon(Icons.Filled.Share, "Share", tint = AppColors.Yellow) }
            })
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // FLYER
                val flyerPh: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, null, tint = AppColors.Gray, modifier = Modifier.size(40.dp))
                            Text("No Flyer", color = AppColors.Gray)
                        }
                    }
                }
                if (!event.imageURL.isNullOrEmpty()) {
                    AppImage(
                        event.imageURL,
                        Modifier.fillMaxWidth().height(300.dp)
                            .shadow(10.dp, RoundedCornerShape(15.dp), ambientColor = AppColors.Yellow, spotColor = AppColors.Yellow)
                            .clip(RoundedCornerShape(15.dp)).clickable { showFull = true },
                        contentScale = ContentScale.Fit, placeholder = flyerPh
                    )
                } else flyerPh()

                // INFO CARD
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text(event.title.uppercase(), color = AppColors.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    YellowDivider()
                    DetailRow(Icons.Filled.CalendarMonth, DateUtils.longWithTime(event.date))
                    if (parts.size >= 5) {
                        Row {
                            Icon(Icons.Filled.LocationOn, null, tint = AppColors.Yellow)
                            Spacer(Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(parts[0], color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text(parts[1], color = AppColors.Gray, fontSize = 15.sp)
                                Text("${parts[2]}, ${parts[3]} ${parts[4]}", color = AppColors.Gray, fontSize = 15.sp)
                            }
                        }
                    } else DetailRow(Icons.Filled.LocationOn, event.locationName)
                    if (distanceMiles != null) DetailRow(Icons.Filled.Navigation, String.format(java.util.Locale.US, "%.1f miles away", distanceMiles))
                    DetailRow(Icons.Filled.LocalOffer, event.category.capitalizedWords())
                    if (event.price != "0.00" && event.price.isNotEmpty()) DetailRow(Icons.Filled.AttachMoney, event.price)
                    if (event.details.isNotEmpty()) {
                        YellowDivider()
                        Text("Details", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text(event.details, color = Color.White)
                    }
                    PostedByCard(event.postedByName, onPostedBy)
                }

                YellowButton("SHARE EVENT", onShare, corner = 10.dp, leading = Icons.Filled.Share)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionTile("Weather", "Forecast", Icons.Filled.WbCloudy, AppColors.Blue, Modifier.weight(1f), onWeather)
                    ActionTile("Navigate", "Get Directions", Icons.Filled.Navigation, AppColors.Green, Modifier.weight(1f)) { showNav = true }
                }

                if (event.latitude != 0.0 && event.longitude != 0.0) {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp)) {
                        Text("LOCATION", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        SmallMap(event.latitude, event.longitude, venue, Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(15.dp)))
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showFull) FullScreenImageViewer(event.imageURL, onDismiss = { showFull = false })
    if (showNav) NavigationChooser(event.latitude, event.longitude, venue, event.locationName.replace("|", " ")) { showNav = false }
}

@Composable
fun NavigationChooser(lat: Double, lng: Double, label: String, addressText: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val hasCoords = lat != 0.0 || lng != 0.0
    ChoiceDialog(
        title = "Choose Navigation App",
        message = "Get directions to this event",
        choices = listOf(
            "Default Maps App" to { if (hasCoords) Intents.openDefaultMaps(context, lat, lng, label) else Intents.openMapsSearch(context, addressText) },
            "Google Maps" to { if (hasCoords) Intents.openGoogleMaps(context, lat, lng) else Intents.openMapsSearch(context, addressText) },
            "Waze" to { Intents.openWaze(context, lat, lng) }
        ),
        onDismiss = onDismiss
    )
}

@Composable
fun DetailRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow)
        Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White)
    }
}

@Composable
fun PostedByCard(name: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Card)
            .border(1.dp, AppColors.Yellow.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.AccountCircle, null, tint = AppColors.Yellow, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Posted by", color = AppColors.Gray, fontSize = 12.sp)
            Text(name.ifEmpty { "Member" }, color = AppColors.Yellow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = AppColors.Yellow.copy(alpha = 0.6f))
    }
}

@Composable
fun ActionTile(title: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.2f)).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = if (color == AppColors.Blue) AppColors.Yellow else AppColors.Yellow)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = AppColors.Gray, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SupabaseEventShareView
// ─────────────────────────────────────────────────────────────────────────────

object ShareHelper {
    fun message(title: String, date: Instant, locationName: String, details: String, category: String): String {
        val parts = LocationParts.split(locationName)
        val venue = parts.firstOrNull() ?: "TBA"
        val cityState = if (parts.size >= 5) "${parts[2]}, ${parts[3]}" else ""
        return buildString {
            append("🏍️ CHECK OUT THIS EVENT! 🏍️\n\n")
            append(title.uppercase()).append("\n\n")
            append("📅 ").append(DateUtils.longWithTime(date)).append("\n")
            append("📍 ").append(venue).append("\n")
            if (cityState.isNotEmpty()) append("   ").append(cityState).append("\n")
            if (details.isNotEmpty()) append("ℹ️ ").append(details).append("\n")
            append("\n🛣️ Find more events on the ON THA SET app!\n\n")
            append("#OnThaSet #MotoLife #").append(category.replace(" ", ""))
        }
    }
}

@Composable
fun EventShareScreen(
    title: String,
    date: Instant,
    locationName: String,
    details: String,
    category: String,
    imageModel: Any?,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }
    val msg = remember(title, date, locationName, details, category) { ShareHelper.message(title, date, locationName, details, category) }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            SheetHeader(onClose = onClose)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(25.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 20.dp, start = 16.dp, end = 16.dp)) {
                    Icon(Icons.Filled.Campaign, null, tint = AppColors.Yellow, modifier = Modifier.size(50.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Share This Event", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Help spread the word and grow the community!", color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Column(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text("PREVIEW", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (imageModel != null && imageModel.toString().isNotEmpty()) {
                        AppImage(imageModel, Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
                    }
                    Text(msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Card).padding(16.dp))
                }
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    YellowButton("Share via...", {
                        scope.launch {
                            when (imageModel) {
                                is java.io.File -> Intents.share(context, msg, imageFile = imageModel)
                                is String -> if (imageModel.startsWith("http")) Intents.share(context, msg, imageUrl = imageModel)
                                    else Intents.share(context, msg, imageFile = java.io.File(imageModel))
                                else -> Intents.share(context, msg)
                            }
                        }
                    }, corner = 12.dp, leading = Icons.Filled.Share)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f))
                            .clickable { Intents.copy(context, msg); copied = true }.padding(16.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ContentCopy, null, tint = AppColors.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Event Details", color = AppColors.Yellow, fontWeight = FontWeight.SemiBold)
                    }
                }
                Column(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡 SHARING TIPS", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TipRow(Icons.Filled.Sms, "Text friends & family directly")
                    TipRow(Icons.Filled.PhotoLibrary, "Post to Instagram/Facebook stories")
                    TipRow(Icons.Filled.Message, "Share in group chats")
                    TipRow(Icons.Filled.Link, "Copy and paste everywhere!")
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
    if (copied) InfoAlert("Copied!", "Event details copied to clipboard", onDismiss = { copied = false })
}

@Composable
private fun TipRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.width(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = AppColors.Gray, fontSize = 12.sp)
    }
}
