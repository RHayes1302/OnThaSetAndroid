package com.hayesenterprisesllc.onthaset.ui.posting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.BikeProgress
import com.hayesenterprisesllc.onthaset.data.EventPhoto
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.data.UserProfile
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.DateTimeField
import com.hayesenterprisesllc.onthaset.ui.components.FormTextField
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.LabeledField
import com.hayesenterprisesllc.onthaset.ui.components.LoadingOverlay
import com.hayesenterprisesllc.onthaset.ui.components.MenuPicker
import com.hayesenterprisesllc.onthaset.ui.components.SheetHeader
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.components.rememberImagePicker
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.ImageUtils
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// UploadEventPhotoView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun UploadEventPhotoScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profiles by LocalStore.profiles.collectAsState()
    val profile = profiles.firstOrNull { it.appleUserID == AuthService.currentUserId }
    var eventName by rememberSaveable { mutableStateOf("") }
    var eventDate by remember { mutableStateOf(Instant.now()) }
    var location by rememberSaveable { mutableStateOf("") }
    var caption by rememberSaveable { mutableStateOf("") }
    var images by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val maxSel = minOf(10, profile?.remainingPhotoSlots() ?: 10).coerceAtLeast(2)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(maxSel)) { uris ->
        if (uris.isNotEmpty()) images = uris.take(minOf(10, profile?.remainingPhotoSlots() ?: 10).coerceAtLeast(1))
    }
    val canSave = profile != null && eventName.isNotEmpty() && location.isNotEmpty() && images.isNotEmpty()

    fun save() {
        val p = profile ?: return
        isSaving = true
        scope.launch {
            var saved = 0
            images.forEachIndexed { index, uri ->
                val bytes = ImageUtils.uriToJpeg(context, uri, quality = 80) ?: return@forEachIndexed
                val fileName = "event_photo_${p.appleUserID}_${UUID.randomUUID().toString().uppercase()}.jpg"
                runCatching {
                    val url = SupabaseManager.uploadImage(bytes, "event-photos", fileName)
                    val cap = caption.ifEmpty { "Photo ${index + 1}" }
                    SupabaseManager.saveEventPhotoMetadata(p.appleUserID, eventName, eventDate, location, cap, url)
                    LocalStore.insertPhoto(EventPhoto(eventName = eventName, eventDate = eventDate, location = location, caption = cap, photoFileName = url, userID = p.appleUserID))
                    saved++
                }.onFailure { Http.log("❌ Event photo upload failed: $it") }
            }
            if (saved > 0) {
                // iOS added `saved` to totalPhotosPosted and also incremented once per photo.
                LocalStore.updateProfile(p.appleUserID) { prof ->
                    var x = prof.copy(totalPhotosPosted = prof.totalPhotosPosted + saved)
                    repeat(saved) { x = x.withIncrementedPhotoCount() }
                    x
                }
                success = true
            } else error = "Failed to upload photos. Please try again."
            isSaving = false
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            SheetHeader(title = "Event Photo", closeLabel = "Cancel", onClose = onClose)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                if (profile != null && profile.hasActiveSubscription) PhotoLimitBanner(profile)
                Column(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Collections, null, tint = AppColors.Yellow, modifier = Modifier.size(60.dp))
                    Text("Share Event Photos", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Upload photos from events you attended", color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                LabeledField("Event Name") { FormTextField(eventName, { eventName = it }, "e.g., Annual Bike Rally 2026") }
                LabeledField("Event Date") { DateTimeField(eventDate, { eventDate = it }, includeTime = false) }
                LabeledField("Location") { FormTextField(location, { location = it }, "City, State") }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PHOTOS", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(" (Required)", color = AppColors.Orange, fontSize = 11.sp)
                        Spacer(Modifier.weight(1f))
                        Row(Modifier.clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AddCircle, null, tint = AppColors.Yellow, modifier = Modifier.size(16.dp))
                            Text(" Add Photos", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (images.isEmpty()) {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card).padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Gray, modifier = Modifier.size(50.dp))
                            Text("No photos selected", color = AppColors.Gray, fontSize = 12.sp)
                            Text("Tap 'Add Photos' to select up to 10 photos", color = AppColors.Gray.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        images.chunked(3).forEachIndexed { r, row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEachIndexed { c, uri ->
                                    val index = r * 3 + c
                                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                                        AppImage(uri, Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                                        Text(
                                            "${index + 1}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).clip(CircleShape).background(AppColors.Yellow).padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                        Icon(Icons.Filled.Cancel, "Remove", tint = AppColors.Red, modifier = Modifier.align(Alignment.TopStart).offset((-4).dp, (-4).dp)
                                            .clip(CircleShape).background(Color.White).clickable { images = images.filterIndexed { i, _ -> i != index } })
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }

                LabeledField("Caption (Optional)") {
                    FormTextField(caption, { caption = it }, "Add a caption for your photos...", singleLine = false, minLines = 4)
                }
                YellowButton("POST ${images.size} PHOTO${if (images.size == 1) "" else "S"}", ::save, enabled = canSave, corner = 10.dp)
            }
        }
        if (isSaving) LoadingOverlay("Uploading photos...")
    }
    if (success) InfoAlert("Success!", "Your event photos have been posted!", onDismiss = { success = false; onClose() })
    error?.let { InfoAlert("Upload Failed", it, onDismiss = { error = null }) }
}

@Composable
private fun PhotoLimitBanner(profile: UserProfile) {
    val monthly = profile.remainingPhotosThisMonth()
    val total = profile.remainingPhotoSlots()
    val atLimit = monthly == 0 || total == 0
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (atLimit) AppColors.Red.copy(alpha = 0.1f) else AppColors.Card)
            .border(1.dp, if (atLimit) AppColors.Red.copy(alpha = 0.3f) else AppColors.Yellow.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (atLimit) Icons.Filled.Error else Icons.Filled.PhotoLibrary, null, tint = if (atLimit) AppColors.Red else AppColors.Yellow)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(if (atLimit) "Photo limit reached" else "Photo storage", color = if (atLimit) AppColors.Red else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("$monthly uploads left this month  •  $total/${UserProfile.TOTAL_PHOTO_LIMIT} slots used", color = AppColors.Gray, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// UploadBikeProgressView
// ─────────────────────────────────────────────────────────────────────────────

private val bikeMakes = listOf(
    "Harley-Davidson", "Indian", "Honda", "Yamaha", "Kawasaki", "Suzuki", "BMW", "Ducati", "Triumph", "KTM",
    "Aprilia", "Moto Guzzi", "Royal Enfield", "Can-Am", "Victory", "Other"
).sorted()

private fun modelsFor(make: String): List<String> = when (make) {
    "Harley-Davidson" -> listOf("Street Glide", "Road Glide", "Softail", "Sportster", "Fat Boy", "Road King", "Ultra Limited", "Breakout", "Low Rider", "Heritage Classic", "Other")
    "Indian" -> listOf("Chief", "Scout", "Challenger", "Chieftain", "Roadmaster", "Springfield", "FTR", "Pursuit", "Other")
    "Honda" -> listOf("Gold Wing", "Rebel", "Shadow", "CBR", "CB", "CRF", "Africa Twin", "NC750X", "Other")
    "Yamaha" -> listOf("YZF-R1", "YZF-R6", "MT-09", "MT-07", "V-Star", "Bolt", "FJR1300", "Tracer", "Other")
    "Kawasaki" -> listOf("Ninja", "Z900", "Vulcan", "Versys", "KLR", "ZX-10R", "Z650", "Concours", "Other")
    "Suzuki" -> listOf("Hayabusa", "GSX-R", "V-Strom", "Boulevard", "SV650", "Katana", "GSX-S", "Other")
    "BMW" -> listOf("R1250GS", "R1250RT", "S1000RR", "F850GS", "K1600", "R18", "F900R", "Other")
    "Ducati" -> listOf("Panigale", "Monster", "Multistrada", "Diavel", "Scrambler", "Supersport", "Streetfighter", "Other")
    "Triumph" -> listOf("Bonneville", "Speed Triple", "Tiger", "Street Triple", "Rocket", "Scrambler", "Thruxton", "Other")
    else -> listOf("Other")
}

@Composable
fun UploadBikeProgressScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile = LocalStore.profileFor(AuthService.currentUserId)
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var make by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var before by remember { mutableStateOf<Uri?>(null) }
    var after by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val beforePicker = rememberImagePicker { before = it }
    val afterPicker = rememberImagePicker { after = it }
    val years = remember { (2026 downTo 1950).map { it.toString() } }
    val canSave = title.isNotEmpty() && after != null

    fun save() {
        val p = profile ?: return
        val afterUri = after ?: return
        isSaving = true
        scope.launch {
            try {
                val afterBytes = ImageUtils.uriToJpeg(context, afterUri, quality = 80) ?: throw Exception("Could not convert after image to data")
                val afterURL = SupabaseManager.uploadImage(afterBytes, "bike-progress", "bike_after_${p.appleUserID}_${UUID.randomUUID().toString().uppercase()}.jpg")
                var beforeURL = ""
                before?.let { b ->
                    ImageUtils.uriToJpeg(context, b, quality = 80)?.let { bytes ->
                        beforeURL = SupabaseManager.uploadImage(bytes, "bike-progress", "bike_before_${p.appleUserID}_${UUID.randomUUID().toString().uppercase()}.jpg")
                    }
                }
                LocalStore.insertBikeProgress(
                    BikeProgress(modificationTitle = title, note = note, beforeImage = beforeURL, afterImage = afterURL,
                        bikeMake = make, bikeModel = model, bikeYear = year, userID = p.appleUserID)
                )
                LocalStore.updateProfile(p.appleUserID) { it.copy(totalBikeProgressPosts = it.totalBikeProgressPosts + 1) }
                SupabaseManager.saveBikeBuildMetadata(p.appleUserID, title, note, beforeURL, afterURL, make, model, year)
                success = true
            } catch (e: Exception) {
                error = e.message ?: "Upload failed"
            }
            isSaving = false
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            SheetHeader(title = "Bike Update", closeLabel = "Cancel", onClose = onClose)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                LabeledField("Modification Title") { FormTextField(title, { title = it }, "e.g., New Exhaust System") }

                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Text("BIKE INFO", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LabeledField("Year", AppColors.Gray) { MenuPicker(year, "Select Year", years) { year = it } }
                    LabeledField("Make", AppColors.Gray) { MenuPicker(make, "Select Make", bikeMakes) { make = it; model = "" } }
                    if (make.isNotEmpty()) LabeledField("Model", AppColors.Gray) { MenuPicker(model, "Select Model", modelsFor(make)) { model = it } }
                }

                PhotoSlot("BEFORE PHOTOS", "(1 photo)", AppColors.Gray, "No before photos added", before, { beforePicker.pickFromLibrary() }) { before = null }
                PhotoSlot("AFTER PHOTOS", "(1 photo required)", AppColors.Orange, "No after photos added", after, { afterPicker.pickFromLibrary() }) { after = null }

                LabeledField("Notes") { FormTextField(note, { note = it }, "", singleLine = false, minLines = 4) }
                YellowButton("POST BIKE UPDATE", ::save, enabled = canSave, corner = 10.dp)
            }
        }
        if (isSaving) LoadingOverlay("Uploading photos...")
    }
    if (success) InfoAlert("Success!", "Your bike update has been posted!", onDismiss = { success = false; onClose() })
    error?.let { InfoAlert("Upload Failed", it, onDismiss = { error = null }) }
}

@Composable
private fun PhotoSlot(label: String, hint: String, hintColor: Color, emptyText: String, uri: Uri?, onPick: () -> Unit, onRemove: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(" $hint", color = hintColor, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Row(Modifier.clickable(onClick = onPick), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (uri == null) Icons.Filled.AddCircle else Icons.Filled.SwapHoriz, null, tint = AppColors.Yellow, modifier = Modifier.size(16.dp))
                Text(if (uri == null) " Add" else " Change", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (uri == null) {
            Text(emptyText, color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Card).padding(16.dp))
        } else {
            Box(Modifier.size(100.dp)) {
                AppImage(uri, Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                Icon(Icons.Filled.Cancel, "Remove", tint = AppColors.Red, modifier = Modifier.align(Alignment.TopEnd).offset(5.dp, (-5).dp)
                    .clip(CircleShape).background(Color.White).clickable(onClick = onRemove))
            }
        }
    }
}
