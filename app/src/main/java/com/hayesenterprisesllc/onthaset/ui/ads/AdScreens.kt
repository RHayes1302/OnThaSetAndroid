package com.hayesenterprisesllc.onthaset.ui.ads

import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.FormTextField
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.LabeledField
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.components.rememberImagePicker
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.ImageUtils
import com.hayesenterprisesllc.onthaset.util.Intents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// AdDetailView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdDetailScreen(ad: SupabaseAd, onClose: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    val accent = AdStyle.accent(ad.category)
    val appt = AdStyle.isAppointmentOnly(ad)

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onClose, title = "Advertisement", backIcon = Icons.Filled.Close, actions = {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Edit", tint = AppColors.Yellow) }
            })
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                val iconBig: @Composable () -> Unit = { Text(AdStyle.icon(ad.category), fontSize = 80.sp) }
                if (!ad.imageURL.isNullOrEmpty()) {
                    AppImage(ad.imageURL, Modifier.size(120.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Fit, placeholder = iconBig)
                } else iconBig()

                Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (ad.plan) {
                        "premium" -> Pill("👑 PREMIUM ADVERTISER", AppColors.Yellow)
                        "featured" -> Pill("⭐ FEATURED ADVERTISER", AppColors.Orange)
                    }
                    Text(ad.businessName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(ad.tagline, color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            AdStyle.longLabel(ad.category), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(accent).padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        if (appt) Row(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("By Appointment", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card)) {
                    val rows = buildList {
                        if (!ad.address.isNullOrEmpty()) add(Triple(Icons.Filled.LocationOn, "Address" to ad.address) { Intents.openMapsSearch(context, ad.address) })
                        if (!ad.phone.isNullOrEmpty()) add(Triple(Icons.Filled.Phone, "Phone" to ad.phone) { Intents.dial(context, ad.phone) })
                        if (!ad.websiteURL.isNullOrEmpty()) add(Triple(Icons.Filled.Language, "Website" to ad.websiteURL) { Intents.openUrl(context, ad.websiteURL) })
                    }
                    rows.forEachIndexed { i, (icon, lv, action) ->
                        ContactRow(icon, lv.first, lv.second, accent, action)
                        if (i < rows.lastIndex) HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.2f))
                    }
                }

                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!ad.phone.isNullOrEmpty()) {
                        YellowButton(
                            if (appt) "CALL TO SCHEDULE APPOINTMENT" else "CALL NOW", { Intents.dial(context, ad.phone) },
                            color = accent, corner = 12.dp, leading = if (appt) Icons.Filled.CalendarMonth else Icons.Filled.Phone
                        )
                    } else if (appt && !ad.websiteURL.isNullOrEmpty()) {
                        YellowButton("BOOK APPOINTMENT ONLINE", { Intents.openUrl(context, ad.websiteURL) }, color = accent, corner = 12.dp, leading = Icons.Filled.CalendarMonth)
                    }
                    if (!ad.address.isNullOrEmpty()) OutlinedAccentButton("GET DIRECTIONS", Icons.Filled.Map, accent, Color.White) { Intents.openMapsSearch(context, ad.address) }
                    if (!ad.websiteURL.isNullOrEmpty()) OutlinedAccentButton("VISIT WEBSITE", Icons.Filled.Language, accent, accent) { Intents.openUrl(context, ad.websiteURL) }
                }

                Text("SPONSORED ADVERTISEMENT", color = AppColors.Gray.copy(alpha = 0.4f), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.clickable(onClick = onEdit).padding(bottom = 30.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Edit, null, tint = AppColors.Gray.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Are you the advertiser? Edit this ad", color = AppColors.Gray.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color) = Text(
    text, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black,
    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
)

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String, accent: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = accent, modifier = Modifier.width(24.dp))
        Spacer(Modifier.width(15.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 15.sp, maxLines = 2)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = AppColors.Gray)
    }
}

@Composable
private fun OutlinedAccentButton(text: String, icon: ImageVector, accent: Color, fg: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, accent, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = fg)
        Spacer(Modifier.width(8.dp))
        Text(text, color = fg, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdEditPinView — PIN gate before editing
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdEditPinScreen(ad: SupabaseAd, onBack: () -> Unit, onUnlocked: () -> Unit) {
    val scope = rememberCoroutineScope()
    val hasPin = !ad.advertiserPin.isNullOrEmpty()
    var settingPin by remember { mutableStateOf(!hasPin) }
    var entered by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf("") }
    var shake by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(shake) { if (shake) { delay(600); shake = false } }

    fun checkPin() {
        if (entered == ad.advertiserPin.orEmpty()) onUnlocked()
        else { shake = true; pinError = "Incorrect PIN. Try again."; entered = "" }
    }

    fun confirmNewPin() {
        if (confirm == entered) {
            isSaving = true
            scope.launch {
                val ok = ad.id != null && SupabaseManager.updateAd(ad.id, JSONObject().put("advertiser_pin", confirm))
                isSaving = false
                if (ok) onUnlocked() else pinError = "Failed to save PIN. Try again."
            }
        } else {
            shake = true; pinError = "PINs don't match. Try again."
            confirm = ""; isConfirming = false; entered = ""
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onBack)
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            ) {
                Icon(Icons.Filled.Lock, null, tint = AppColors.Yellow, modifier = Modifier.size(70.dp))
                val title: String
                val subtitle: String
                if (!settingPin) {
                    title = "Enter Your PIN"; subtitle = "Enter your 4-digit advertiser PIN"
                } else {
                    title = if (isConfirming) "Confirm Your PIN" else "Create a PIN"
                    subtitle = if (isConfirming) "Re-enter your 4-digit PIN to confirm" else "Choose a 4-digit PIN to protect your ad"
                }
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center)
                PinDots(if (settingPin && isConfirming) confirm.length else entered.length, shake)
                if (pinError.isNotEmpty()) Text(pinError, color = AppColors.Red, fontSize = 12.sp)
                if (isSaving) {
                    CircularProgressIndicator(color = AppColors.Yellow)
                    Text("Saving PIN...", color = AppColors.Yellow)
                } else NumPad(
                    onDigit = { d ->
                        pinError = ""
                        if (!settingPin) {
                            if (entered.length < 4) { entered += d; if (entered.length == 4) checkPin() }
                        } else if (isConfirming) {
                            if (confirm.length < 4) { confirm += d; if (confirm.length == 4) confirmNewPin() }
                        } else if (entered.length < 4) {
                            entered += d; if (entered.length == 4) isConfirming = true
                        }
                    },
                    onDelete = {
                        if (settingPin && isConfirming) confirm = confirm.dropLast(1) else entered = entered.dropLast(1)
                        pinError = ""
                    },
                    onConfirm = {
                        if (!settingPin) { if (entered.length == 4) checkPin() }
                        else if (isConfirming && confirm.length == 4) confirmNewPin()
                        else if (!isConfirming && entered.length == 4) isConfirming = true
                    }
                )
            }
        }
    }
}

@Composable
fun PinDots(filled: Int, shake: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.offset(x = if (shake) (-10).dp else 0.dp)) {
        repeat(4) { i ->
            Box(Modifier.size(16.dp).clip(CircleShape).background(if (i < filled) AppColors.Yellow else Color.White.copy(alpha = 0.2f)))
        }
    }
}

@Composable
fun NumPad(onDigit: (String) -> Unit, onDelete: () -> Unit, onConfirm: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9")).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(25.dp)) { row.forEach { n -> PinButton(n) { onDigit(n) } } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(25.dp)) {
            PinButton("⌫", AppColors.Red.copy(alpha = 0.7f), onDelete)
            PinButton("0") { onDigit("0") }
            PinButton("✓", AppColors.Green.copy(alpha = 0.7f), onConfirm)
        }
    }
}

@Composable
private fun PinButton(label: String, color: Color = Color.White.copy(alpha = 0.15f), onClick: () -> Unit) {
    Box(Modifier.size(72.dp).clip(CircleShape).background(color).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EditAdView — advertiser self-service
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EditAdScreen(ad: SupabaseAd, onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var phone by rememberSaveable { mutableStateOf(ad.phone.orEmpty()) }
    var address by rememberSaveable { mutableStateOf(ad.address.orEmpty()) }
    var tagline by rememberSaveable { mutableStateOf(ad.tagline) }
    var website by rememberSaveable { mutableStateOf(ad.websiteURL.orEmpty()) }
    var apptOnly by rememberSaveable { mutableStateOf(ad.notes?.contains("appointment_only:true") == true) }
    var hideAddress by rememberSaveable { mutableStateOf(ad.notes?.contains("hide_address:true") == true) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberImagePicker { selectedUri = it }

    fun save() {
        val id = ad.id ?: run { error = "Could not identify ad record."; return }
        isSaving = true
        scope.launch {
            var imageUrl = ad.imageURL.orEmpty()
            selectedUri?.let { uri ->
                ImageUtils.uriToJpeg(context, uri, quality = 80)?.let { bytes ->
                    runCatching {
                        SupabaseManager.uploadImage(bytes, "ad-banners", "ad_banner_${id}_${System.currentTimeMillis() / 1000}.jpg")
                    }.getOrNull()?.let { imageUrl = it }
                }
            }
            val flags = buildList { if (apptOnly) add("appointment_only:true"); if (hideAddress) add("hide_address:true") }
            val body = JSONObject().apply {
                put("phone", phone); put("tagline", tagline); put("website_url", website)
                put("image_url", imageUrl); put("notes", flags.joinToString(","))
                if (!hideAddress) put("address", address)
            }
            val ok = SupabaseManager.updateAd(id, body)
            isSaving = false
            if (ok) { showSuccess = true; SupabaseManager.fetchActiveAds() } else error = "Network error. Please try again."
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            TopBar(onBack = onBack, title = "Edit Ad")
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
                    Text(ad.businessName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Update Your Ad", color = AppColors.Gray, fontSize = 15.sp)
                }
                Text("AD PHOTO / LOGO", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                    val model: Any? = selectedUri ?: ad.imageURL?.takeIf { it.isNotEmpty() }
                    if (model != null) AppImage(model, Modifier.height(120.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                    else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Gray, modifier = Modifier.size(40.dp))
                        Text("Tap to add photo", color = AppColors.Gray, fontSize = 12.sp)
                    }
                }
                Row(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.08f)).clickable { picker.pickFromLibrary() }
                        .padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Yellow)
                    Spacer(Modifier.width(6.dp))
                    Text("Change Photo", color = AppColors.Yellow, fontWeight = FontWeight.Bold)
                }

                LabeledField("Phone Number") { FormTextField(phone, { phone = it }, "e.g. (702) 555-1234", keyboardType = KeyboardType.Phone) }
                LabeledField("Address") { FormTextField(address, { address = it }, "Street address") }
                LabeledField("Website") { FormTextField(website, { website = it }, "https://yoursite.com", keyboardType = KeyboardType.Uri) }
                LabeledField("Tagline") { FormTextField(tagline, { tagline = it }, "Short description of your business") }

                ToggleRow(Icons.Filled.CalendarMonth, "Appointment Only", "Shows 'Call to Schedule' instead of address", apptOnly) { apptOnly = it }
                ToggleRow(Icons.Filled.VisibilityOff, "Hide Address", "Keep your location private on the ad", hideAddress) { hideAddress = it }

                YellowButton(if (isSaving) "Saving..." else "Save Changes", ::save, loading = isSaving, corner = 12.dp, leading = Icons.Filled.CheckCircle)
                Spacer(Modifier.height(30.dp))
            }
        }
    }
    if (showSuccess) InfoAlert("Updated!", "Your ad has been updated successfully.", onDismiss = { showSuccess = false; onDone() }, confirmText = "Done")
    error?.let { InfoAlert("Update Failed", it, onDismiss = { error = null }) }
}

@Composable
private fun ToggleRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.06f)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AppColors.Yellow)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = AppColors.Gray, fontSize = 11.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Yellow, checkedThumbColor = Color.Black)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdvertiserSignupView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdvertiserSignupScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var businessName by rememberSaveable { mutableStateOf("") }
    var tagline by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("lawyer") }
    var plan by rememberSaveable { mutableStateOf("basic") }
    var billing by rememberSaveable { mutableStateOf("monthly") }
    var email by rememberSaveable { mutableStateOf("") }
    var pin by rememberSaveable { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var phone by rememberSaveable { mutableStateOf("") }
    var website by rememberSaveable { mutableStateOf("") }
    var street by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var zip by rememberSaveable { mutableStateOf("") }
    var apptOnly by rememberSaveable { mutableStateOf(false) }
    var hideAddress by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var logoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }

    val logoAllowed = plan == "featured" || plan == "premium"
    val valid = businessName.isNotBlank() && tagline.isNotBlank() && email.isNotBlank() && pin.length == 4
    val priceText = when (plan) { "premium" -> "$49.99"; "featured" -> "$29.99"; else -> "$19.99" }
    val billingText = if (billing == "monthly") "monthly recurring" else "one-time"

    val picker = rememberImagePicker { uri ->
        logoUri = uri
        scope.launch { logoBytes = ImageUtils.uriToCompressedJpeg(context, uri, maxSizeKB = 293) }
    }

    fun submit() {
        isSubmitting = true
        scope.launch {
            var logoUrl: String? = null
            val bytes = logoBytes
            if (logoAllowed && bytes != null) {
                logoUrl = runCatching { SupabaseManager.uploadImage(bytes, "ad-banners", "logo-${UUID.randomUUID().toString().uppercase()}.jpg") }.getOrNull()
            }
            val displayParts = buildList {
                if (!hideAddress && street.isNotEmpty()) add(street)
                if (city.isNotEmpty()) add(city); if (state.isNotEmpty()) add(state); if (zip.isNotEmpty()) add(zip)
            }
            val geoParts = listOf(street, city, state, zip).filter { it.isNotEmpty() }
            val fullAddress = displayParts.joinToString(", ")
            val geocodeAddress = geoParts.joinToString(", ")
            var notes = ""
            if (apptOnly) notes += "appointment_only"
            if (hideAddress) notes += if (notes.isEmpty()) "hide_address" else ",hide_address"
            if (hideAddress && geocodeAddress.isNotEmpty()) notes += ",geocode:$geocodeAddress"

            val newAd = SupabaseAd(
                businessName = businessName,
                tagline = if (apptOnly) "$tagline • By Appointment Only" else tagline,
                category = category,
                phone = phone.ifEmpty { null },
                websiteURL = website.ifEmpty { null },
                imageURL = logoUrl,
                address = fullAddress.ifEmpty { null },
                status = "pending",
                plan = plan,
                advertiserEmail = email.ifEmpty { null },
                paymentStatus = "unpaid",
                notes = notes.ifEmpty { null },
                billingPreference = billing,
                advertiserPin = pin.ifEmpty { null }
            )
            try {
                SupabaseManager.submitAd(newAd)
                showSuccess = true
            } catch (e: Exception) {
                error = e.message ?: "Something went wrong"
            }
            isSubmitting = false
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            Box(Modifier.fillMaxWidth().padding(top = 36.dp, start = 12.dp, end = 12.dp, bottom = 6.dp)) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.Filled.Close, "Close", tint = AppColors.Yellow) }
                ShieldLogo(45.dp, Modifier.align(Alignment.Center))
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ADVERTISE WITH US", color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Reach the riding community in your area", color = AppColors.Gray, fontSize = 12.sp)
                }
                // PLAN SELECTOR
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SELECT YOUR PLAN", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    PlanSelector("Basic", "$19.99/mo", null, "Name + tagline + address", "Standard banner • Home page only", "basic", AppColors.Gray, plan) { plan = it }
                    PlanSelector("Featured", "$29.99/mo", "⭐", "Logo + links + address", "Gold border • Logo upload • Home page", "featured", AppColors.Orange, plan) { plan = it }
                    PlanSelector("Premium", "$49.99/mo", "👑", "Maximum exposure", "Yellow glow • Logo • ALL pages", "premium", AppColors.Yellow, plan) { plan = it }
                }
                // BILLING
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("BILLING PREFERENCE", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BillingOption("🔄 MONTHLY", "Auto-renews each month", billing == "monthly", AppColors.Yellow, Modifier.weight(1f)) { billing = "monthly" }
                        BillingOption("1️⃣ ONE-TIME", "Manual renewal each month", billing == "onetime", AppColors.Orange, Modifier.weight(1f)) { billing = "onetime" }
                    }
                }
                // LOGO
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("BUSINESS LOGO", color = if (logoAllowed) AppColors.Yellow else AppColors.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
                        if (!logoAllowed) Text(" (Featured & Premium only)", color = AppColors.Gray, fontSize = 11.sp)
                    }
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (logoAllowed) {
                            Box(
                                Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.08f))
                                    .then(if (logoUri != null) Modifier.border(2.dp, AppColors.Yellow, RoundedCornerShape(16.dp)) else Modifier)
                                    .clickable { picker.pickFromLibrary() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoUri != null) AppImage(logoUri, Modifier.fillMaxSize())
                                else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Yellow)
                                    Text("Add Logo", color = AppColors.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Column(
                                Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.03f)),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.Lock, null, tint = AppColors.Gray.copy(alpha = 0.3f))
                                Text("Upgrade to\nFeatured", color = AppColors.Gray.copy(alpha = 0.4f), fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                    if (logoAllowed) Text("Logo appears in your ad banner", color = AppColors.Gray, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
                LabeledField("BUSINESS NAME") { FormTextField(businessName, { businessName = it }, "e.g. Iron Mile Cycles") }
                LabeledField("TAGLINE") { FormTextField(tagline, { tagline = it }, "e.g. Parts. Service. Custom builds.") }
                LabeledField("EMAIL ADDRESS (REQUIRED)") { FormTextField(email, { email = it }, "your@email.com", keyboardType = KeyboardType.Email) }
                LabeledField("SECURITY PIN (REQUIRED — 4 digits to edit your ad)") {
                    FormTextField(
                        pin, { v -> pin = v.filter { it.isDigit() }.take(4) }, "4-digit PIN",
                        keyboardType = KeyboardType.NumberPassword, isPassword = !showPin,
                        trailing = {
                            Icon(
                                if (showPin) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = AppColors.Gray,
                                modifier = Modifier.clickable { showPin = !showPin }
                            )
                        }
                    )
                    Text("You'll use this PIN to update your ad info later", color = AppColors.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
                LabeledField("CATEGORY") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdStyle.categories.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (label, value) ->
                                    val sel = category == value
                                    Text(
                                        label, color = if (sel) Color.Black else AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                            .background(if (sel) AppColors.Yellow else Color.White.copy(alpha = 0.06f))
                                            .border(1.dp, AppColors.Yellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable { category = value }.padding(vertical = 10.dp, horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                // BOOKING TYPE
                LabeledField("BOOKING TYPE") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BookingOption("Walk-ins Welcome", !apptOnly, Modifier.weight(1f)) { apptOnly = false }
                        BookingOption("By Appointment Only", apptOnly, Modifier.weight(1f)) { apptOnly = true }
                    }
                }
                // LOCATION
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("LOCATION", color = AppColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
                        Spacer(Modifier.weight(1f))
                        Row(
                            Modifier.clip(RoundedCornerShape(6.dp)).background(if (hideAddress) AppColors.Orange.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
                                .clickable { hideAddress = !hideAddress }.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val c = if (hideAddress) AppColors.Orange else AppColors.Gray
                            Icon(if (hideAddress) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = c, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(if (hideAddress) "Address Hidden" else "Show Address", color = c, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (hideAddress) {
                        Text(
                            "Your full address stays private. We only use your city/state to show your ad to nearby riders.",
                            color = AppColors.Gray, fontSize = 11.sp,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.Orange.copy(alpha = 0.08f)).padding(10.dp)
                        )
                    } else {
                        LabeledField("STREET ADDRESS (OPTIONAL)") { FormTextField(street, { street = it }, "123 Main Street") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { LabeledField("CITY") { FormTextField(city, { city = it }, "City") } }
                        Box(Modifier.width(76.dp)) { LabeledField("STATE") { FormTextField(state, { state = it }, "ST") } }
                        Box(Modifier.width(96.dp)) { LabeledField("ZIP") { FormTextField(zip, { zip = it }, "00000", keyboardType = KeyboardType.Number) } }
                    }
                }
                LabeledField("PHONE NUMBER (OPTIONAL)") { FormTextField(phone, { phone = it }, "e.g. 7025550100", keyboardType = KeyboardType.Phone) }
                LabeledField("WEBSITE (OPTIONAL)") { FormTextField(website, { website = it }, "e.g. yoursite.com", keyboardType = KeyboardType.Uri) }

                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Card).padding(16.dp)) {
                    Text("WHAT HAPPENS NEXT", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "After submitting your ad will be reviewed and approved within 24 hours. You will then be contacted via email or phone to complete your $billingText payment of $priceText.",
                        color = AppColors.Gray, fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
            Box(Modifier.padding(16.dp).navigationBarsPadding()) {
                YellowButton(if (isSubmitting) "SUBMITTING..." else "SUBMIT FOR REVIEW", ::submit, enabled = valid && !isSubmitting, loading = isSubmitting, corner = 12.dp)
            }
        }
    }
    if (showSuccess) InfoAlert(
        "Submitted!",
        "Your ad has been submitted for review. We will be in touch within 24 hours to complete your $billingText payment setup.",
        onDismiss = { showSuccess = false; onClose() }
    )
    error?.let { InfoAlert("Error", it, onDismiss = { error = null }) }
}

@Composable
private fun PlanSelector(title: String, price: String, badge: String?, desc: String, features: String, value: String, color: Color, selected: String, onSelect: (String) -> Unit) {
    val sel = selected == value
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (sel) color else Color.White.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, color.copy(alpha = if (sel) 0f else 0.4f)), RoundedCornerShape(10.dp))
            .clickable { onSelect(value) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (badge != null) Text(badge, fontSize = 14.sp)
                Text(title, color = if (sel) Color.Black else Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(price, color = if (sel) Color.Black.copy(alpha = 0.7f) else color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(desc, color = if (sel) Color.Black.copy(alpha = 0.7f) else AppColors.Gray, fontSize = 12.sp)
            Text(features, color = if (sel) Color.Black.copy(alpha = 0.6f) else AppColors.Gray.copy(alpha = 0.6f), fontSize = 10.sp)
        }
        if (sel) Icon(Icons.Filled.CheckCircle, null, tint = Color.Black)
    }
}

@Composable
private fun BillingOption(title: String, subtitle: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(8.dp)).background(if (selected) color else Color.White.copy(alpha = 0.06f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = if (selected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = if (selected) Color.Black.copy(alpha = 0.7f) else AppColors.Gray, fontSize = 9.sp)
    }
}

@Composable
private fun BookingOption(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(if (selected) AppColors.Yellow.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (selected) AppColors.Yellow.copy(alpha = 0.5f) else AppColors.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked, null, tint = AppColors.Yellow, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = if (selected) Color.White else AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
