package com.hayesenterprisesllc.onthaset.ui.posting

import android.net.Uri
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.billing.BillingManager
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.EventCategory
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.data.LocalEvent
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.LocationParts
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.location.LocationManager
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ConfirmAlert
import com.hayesenterprisesllc.onthaset.ui.components.DateTimeField
import com.hayesenterprisesllc.onthaset.ui.components.FormTextField
import com.hayesenterprisesllc.onthaset.ui.components.FullScreenImageViewer
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.LabeledField
import com.hayesenterprisesllc.onthaset.ui.components.MenuPicker
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.SmallMap
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.components.YellowDivider
import com.hayesenterprisesllc.onthaset.ui.components.rememberImagePicker
import com.hayesenterprisesllc.onthaset.ui.events.ActionTile
import com.hayesenterprisesllc.onthaset.ui.events.DetailRow
import com.hayesenterprisesllc.onthaset.ui.events.NavigationChooser
import com.hayesenterprisesllc.onthaset.ui.events.PostedByCard
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import com.hayesenterprisesllc.onthaset.util.ImageUtils
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.util.UUID

@Composable
private fun FormHeader(onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.Filled.Close, "Close", tint = AppColors.Yellow) }
        ShieldLogo(45.dp, Modifier.align(Alignment.Center))
    }
}

@Composable
private fun FlyerPicker(model: Any?, label: String, onPick: () -> Unit) {
    if (model != null) {
        AppImage(model, Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onPick), contentScale = ContentScale.Fit)
    } else {
        Column(
            Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).clickable(onClick = onPick),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Yellow, modifier = Modifier.size(30.dp))
            Text(label, color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AddEditEventView — post a new event (PostEventView / DefaultPageView navigateToPost)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddEditEventScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = AuthService.currentUserId
    val profile = LocalStore.profileFor(userId)

    var title by rememberSaveable { mutableStateOf("") }
    var date by remember { mutableStateOf(Instant.now()) }
    var venue by rememberSaveable { mutableStateOf("") }
    var street by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(EventCategory.COMMUNITY) }
    var details by rememberSaveable { mutableStateOf("") }
    var pin by rememberSaveable { mutableStateOf("") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var posting by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberImagePicker { uri ->
        imageUri = uri
        scope.launch { imageBytes = ImageUtils.uriToCompressedJpeg(context, uri, 500) }
    }
    val valid = title.isNotBlank() && pin.isNotBlank()

    fun post() {
        posting = true
        scope.launch {
            try {
                val coords = LocationManager.geocode("$street, $city, $state $zip")
                val combined = "$venue|$street|$city|$state|$zip"
                var imageURL: String? = null
                imageBytes?.let { bytes ->
                    imageURL = runCatching { SupabaseManager.uploadImage(bytes, "event-flyers", "flyer-${UUID.randomUUID().toString().uppercase()}.jpg") }
                        .onFailure { Http.log("⚠️ Image upload failed, continuing without image: $it") }.getOrNull()
                }
                val postedName = profile?.displayName?.takeIf { it.isNotEmpty() } ?: profile?.email ?: "Anonymous"
                SupabaseManager.postEvent(
                    SupabaseEvent(
                        title = title, date = date, category = category.rawValue, locationName = combined,
                        details = details, price = "0.00", latitude = coords?.first ?: 0.0, longitude = coords?.second ?: 0.0,
                        postedByUserID = profile?.appleUserID ?: userId.orEmpty(), postedByName = postedName, imageURL = imageURL
                    )
                )
                // onSave: keep a local copy (with the flyer) and count the post.
                val localPath = imageBytes?.let { LocalStore.saveImageBytes(it) }
                LocalStore.insertEvent(
                    LocalEvent(
                        title = title, date = date, category = category, locationName = combined, details = details,
                        securityCode = pin, price = "0.00", imagePath = localPath,
                        latitude = coords?.first ?: 0.0, longitude = coords?.second ?: 0.0,
                        postedByUserID = profile?.appleUserID ?: "", postedByName = postedName
                    )
                )
                if (profile != null && profile.hasActiveSubscription) {
                    LocalStore.updateProfile(profile.appleUserID) { it.withMonthlyCountChecked().withIncrementedPostCount() }
                }
                BillingManager.consumeSinglePostCredit()
                success = true
            } catch (e: Exception) {
                error = e.message ?: "Something went wrong. Please try again."
            }
            posting = false
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            FormHeader(onClose)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(25.dp)) {
                FlyerPicker(imageUri, "ADD EVENT FLYER") { picker.pickFromLibrary() }
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    LabeledField("EVENT TITLE") { FormTextField(title, { title = it }, "Event name") }
                    LabeledField("EVENT DATE & TIME") { DateTimeField(date, { date = it }, includeTime = true) }
                    LabeledField("VENUE NAME") { FormTextField(venue, { venue = it }, "Venue or location name") }
                    LabeledField("STREET ADDRESS") { FormTextField(street, { street = it }, "123 Main Street") }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { LabeledField("CITY") { FormTextField(city, { city = it }, "City") } }
                        Box(Modifier.width(80.dp)) { LabeledField("STATE") { FormTextField(state, { state = it }, "ST") } }
                    }
                    LabeledField("ZIP CODE") { FormTextField(zip, { zip = it }, "00000", keyboardType = KeyboardType.Number) }
                    LabeledField("CATEGORY") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EventCategory.entries.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { cat ->
                                        val sel = category == cat
                                        Row(
                                            Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (sel) AppColors.Yellow else Color.White.copy(alpha = 0.08f))
                                                .border(1.dp, if (sel) AppColors.Yellow else AppColors.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .clickable { category = cat }.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(cat.icon, fontSize = 12.sp)
                                            Spacer(Modifier.width(6.dp))
                                            Text(cat.displayName, color = if (sel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                                        }
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                            if (category.isNationalEvent) Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Yellow.copy(alpha = 0.1f))
                                    .border(1.dp, AppColors.Yellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Map, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("🗺️ This event will appear on the National Run Calendar", color = AppColors.Yellow, fontSize = 12.sp)
                            }
                        }
                    }
                    LabeledField("DETAILS") { FormTextField(details, { details = it }, "Describe your event", singleLine = false, minLines = 3) }
                    LabeledField("SECURITY PIN (REQUIRED)") { FormTextField(pin, { pin = it }, "4-digit pin", keyboardType = KeyboardType.NumberPassword) }
                }
            }
            Box(Modifier.padding(16.dp).navigationBarsPadding()) {
                YellowButton(if (posting) "POSTING..." else "POST EVENT", ::post, enabled = valid && !posting, loading = posting, corner = 12.dp)
            }
        }
    }
    if (success) InfoAlert(
        "Event Posted!",
        if (category.isNationalEvent) "Your event is now live and will appear on the National Run Calendar map!" else "Your event is now live and visible to all riders on On Tha Set!",
        onDismiss = { success = false; onClose() }
    )
    error?.let { InfoAlert("Error", it, onDismiss = { error = null }) }
}

// ─────────────────────────────────────────────────────────────────────────────
// EventDetailView — locally stored event (from your profile's "Latest Events")
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LocalEventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onWeather: (LocalEvent) -> Unit,
    onPostedBy: (LocalEvent) -> Unit
) {
    val scope = rememberCoroutineScope()
    val events by LocalStore.events.collectAsState()
    val event = events.firstOrNull { it.id == eventId }
    var showFull by remember { mutableStateOf(false) }
    var showNav by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    if (event == null) { androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }; return }
    val parts = LocationParts.split(event.locationName)

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack, showShield = true, actions = {
                IconButton(onClick = onReport) { Icon(Icons.Outlined.Flag, "Report", tint = AppColors.Red.copy(alpha = 0.8f)) }
                IconButton(onClick = onShare) { Icon(Icons.Filled.Share, "Share", tint = AppColors.Yellow) }
            })
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (event.imagePath != null && File(event.imagePath).exists()) {
                    AppImage(
                        event.imagePath,
                        Modifier.fillMaxWidth().height(300.dp).shadow(10.dp, RoundedCornerShape(15.dp), ambientColor = AppColors.Yellow, spotColor = AppColors.Yellow)
                            .clip(RoundedCornerShape(15.dp)).clickable { showFull = true },
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = 0.1f)),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Image, null, tint = AppColors.Gray, modifier = Modifier.size(50.dp))
                        Text("No Flyer", color = AppColors.Gray)
                    }
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Text(event.title.uppercase(), color = AppColors.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    if (event.postedByUserID.isNotEmpty()) PostedByCard(event.postedByName) { onPostedBy(event) }
                    YellowDivider()
                    DetailRow(Icons.Filled.CalendarMonth, DateUtils.longWithTime(event.date))
                    if (parts.size >= 5) Row {
                        Icon(Icons.Filled.LocationOn, null, tint = AppColors.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(parts[0], color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(parts[1], color = AppColors.Gray, fontSize = 15.sp)
                            Text("${parts[2]}, ${parts[3]} ${parts[4]}", color = AppColors.Gray, fontSize = 15.sp)
                        }
                    } else DetailRow(Icons.Filled.LocationOn, event.locationName)
                    DetailRow(Icons.Filled.LocalOffer, event.category.displayName)
                    if (event.details.isNotEmpty()) {
                        YellowDivider()
                        Text("Details", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text(event.details, color = Color.White)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionTile("Weather", "Forecast", Icons.Filled.WbCloudy, AppColors.Blue, Modifier.weight(1f)) { onWeather(event) }
                    ActionTile("Navigate", "Get Directions", Icons.Filled.Navigation, AppColors.Green, Modifier.weight(1f)) { showNav = true }
                }
                if (event.latitude != 0.0 && event.longitude != 0.0) {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp)) {
                        Text("LOCATION", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        SmallMap(event.latitude, event.longitude, event.title, Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(15.dp)))
                    }
                }
                YellowButton("SHARE EVENT", onShare, corner = 10.dp, leading = Icons.Filled.Share)
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.1f)).clickable(onClick = onEdit).padding(16.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Edit, null, tint = AppColors.Yellow); Spacer(Modifier.width(6.dp)); Text("Edit", color = AppColors.Yellow)
                    }
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(AppColors.Red.copy(alpha = 0.2f)).clickable { confirmDelete = true }.padding(16.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Delete, null, tint = AppColors.Red); Spacer(Modifier.width(6.dp)); Text("Delete", color = AppColors.Red)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
    if (showFull) FullScreenImageViewer(event.imagePath, onDismiss = { showFull = false })
    if (showNav) NavigationChooser(event.latitude, event.longitude, parts.firstOrNull() ?: event.title, event.locationName.replace("|", " ")) { showNav = false }
    if (confirmDelete) ConfirmAlert("Delete Event", "Are you sure you want to delete this event? This cannot be undone.", "Delete", onConfirm = {
        val t = event.title; val uid = event.postedByUserID
        scope.launch {
            SupabaseManager.deleteEventByTitleAndUser(t, uid)
            SupabaseManager.fetchAllEvents()
        }
        LocalStore.deleteEvent(event)
        onBack()
    }, onDismiss = { confirmDelete = false })
}

// ─────────────────────────────────────────────────────────────────────────────
// EditEventView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EditLocalEventScreen(eventId: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val event = remember { LocalStore.events.value.firstOrNull { it.id == eventId } }
    if (event == null) { androidx.compose.runtime.LaunchedEffect(Unit) { onClose() }; return }
    val parts = remember { LocationParts.split(event.locationName) }

    var title by rememberSaveable { mutableStateOf(event.title) }
    var date by remember { mutableStateOf(event.date) }
    var venue by rememberSaveable { mutableStateOf(parts.getOrElse(0) { "" }) }
    var street by rememberSaveable { mutableStateOf(if (parts.size >= 5) parts[1] else if (parts.size == 3) parts[2].substringBefore(",").trim() else "") }
    var city by rememberSaveable { mutableStateOf(if (parts.size >= 5) parts[2] else if (parts.size == 3) parts[1] else "") }
    var state by rememberSaveable { mutableStateOf(if (parts.size >= 5) parts[3] else "") }
    var zip by rememberSaveable { mutableStateOf(if (parts.size >= 5) parts[4] else "") }
    var category by rememberSaveable { mutableStateOf(event.category) }
    var details by rememberSaveable { mutableStateOf(event.details) }
    var newBytes by remember { mutableStateOf<ByteArray?>(null) }
    var newUri by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberImagePicker { uri -> newUri = uri; scope.launch { newBytes = ImageUtils.uriToCompressedJpeg(context, uri, 500) } }

    fun save() {
        saving = true
        scope.launch {
            try {
                val coords = LocationManager.geocode("$street, $city, $state $zip")
                val combined = "$venue|$street|$city|$state|$zip"
                // Upload a new flyer only if one was picked (iOS uploaded it but never saved the URL — fixed here).
                val newFlyerUrl = newBytes?.let { SupabaseManager.uploadImage(it, "event-flyers", "flyer-${event.id.uppercase()}-updated.jpg") }
                val lat = coords?.first ?: event.latitude
                val lng = coords?.second ?: event.longitude
                val body = JSONObject().apply {
                    put("title", title); put("date", DateUtils.iso(date)); put("category", category.rawValue)
                    put("location_name", combined); put("details", details)
                    put("latitude", lat); put("longitude", lng)
                    newFlyerUrl?.let { put("image_url", it) }
                }
                val res = Http.restPatch("events?title=eq.${Http.enc(event.title)}&posted_by_user_id=eq.${Http.enc(event.postedByUserID)}", body)
                if (!res.isSuccess) throw Exception(res.body.ifEmpty { "Update failed (${res.code})" })
                val newPath = newBytes?.let { LocalStore.saveImageBytes(it) } ?: event.imagePath
                LocalStore.updateEvent(event.copy(title = title, date = date, locationName = combined, category = category,
                    details = details, imagePath = newPath, latitude = lat, longitude = lng))
                success = true
            } catch (e: Exception) {
                error = e.message ?: "Something went wrong. Please try again."
            }
            saving = false
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            FormHeader(onClose)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                FlyerPicker(newUri ?: event.imagePath, "CHANGE EVENT FLYER") { picker.pickFromLibrary() }
                LabeledField("EVENT TITLE") { FormTextField(title, { title = it }, "Set Name") }
                LabeledField("EVENT DATE & TIME") { DateTimeField(date, { date = it }, includeTime = true) }
                LabeledField("VENUE NAME") { FormTextField(venue, { venue = it }, "e.g., The Hideout") }
                LabeledField("STREET ADDRESS") { FormTextField(street, { street = it }, "e.g., 4211 Fossatello Ave") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { LabeledField("CITY") { FormTextField(city, { city = it }, "Las Vegas") } }
                    Box(Modifier.width(80.dp)) { LabeledField("STATE") { FormTextField(state, { state = it }, "NV") } }
                }
                LabeledField("ZIP CODE") { FormTextField(zip, { zip = it }, "89084", keyboardType = KeyboardType.Number) }
                LabeledField("CATEGORY") {
                    MenuPicker(category.rawValue, "Category", EventCategory.entries.map { it.rawValue }) { raw ->
                        EventCategory.fromRaw(raw)?.let { category = it }
                    }
                }
                LabeledField("DETAILS") { FormTextField(details, { details = it }, "Description", singleLine = false, minLines = 3) }
            }
            Box(Modifier.padding(16.dp).navigationBarsPadding()) {
                YellowButton(if (saving) "SAVING..." else "SAVE CHANGES", ::save, enabled = title.isNotEmpty() && !saving, loading = saving, corner = 12.dp)
            }
        }
    }
    if (success) InfoAlert("Changes Saved!", "Your event has been updated for all riders on On Tha Set.", onDismiss = { success = false; onClose() })
    error?.let { InfoAlert("Error", it, onDismiss = { error = null }) }
}
