package com.hayesenterprisesllc.onthaset.ui.profile

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.Prefs
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.data.UserProfile
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.FormTextField
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.ProfileAvatar
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.components.rememberImagePicker
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.ImageUtils
import kotlinx.coroutines.launch
import org.json.JSONObject

/** Shared Supabase profile save logic (WelcomeSetupView + EditProfileView). */
object ProfileSync {
    fun fieldsJson(p: UserProfile): JSONObject = JSONObject().apply {
        put("display_name", p.displayName); put("bio", p.bio); put("hometown", p.hometown); put("club", p.club)
        put("favorite_ride", p.favoriteRide); put("riding_since", p.ridingSince)
        put("preferred_ride_type", p.preferredRideType); put("favorite_route", p.favoriteRoute)
        put("instagram_handle", p.instagramHandle); put("tiktok_handle", p.tiktokHandle)
        put("youtube_channel", p.youtubeChannel); put("facebook_handle", p.facebookHandle)
        if (p.profileImageURL.isNotEmpty()) put("profile_image_url", p.profileImageURL)
        if (p.backgroundImageURL.isNotEmpty()) put("background_image_url", p.backgroundImageURL)
    }

    /** Uploads profile/background images (if a local file exists) and returns the updated profile. */
    suspend fun uploadImages(p: UserProfile, profileBytes: ByteArray?, backgroundBytes: ByteArray?, lowercaseNames: Boolean): UserProfile {
        var profile = p
        val id = if (lowercaseNames) p.appleUserID.lowercase() else p.appleUserID
        if (profileBytes != null) {
            profile = profile.copy(profileImagePath = LocalStore.saveImageBytes(profileBytes, "profile-${p.appleUserID.hashCode()}.jpg"))
            runCatching { SupabaseManager.uploadImage(profileBytes, "profile-images", "profile-$id.jpg") }
                .onSuccess { profile = profile.copy(profileImageURL = it) }
                .onFailure { Http.log("❌ Profile image upload failed: $it") }
        }
        if (backgroundBytes != null) {
            profile = profile.copy(backgroundImagePath = LocalStore.saveImageBytes(backgroundBytes, "background-${p.appleUserID.hashCode()}.jpg"))
            runCatching { SupabaseManager.uploadImage(backgroundBytes, "profile-images", "background-$id.jpg") }
                .onSuccess { profile = profile.copy(backgroundImageURL = it) }
                .onFailure { Http.log("❌ Background image upload failed: $it") }
        }
        return profile
    }

    /** PATCH by id → PATCH by email → INSERT (WelcomeSetupView.saveToSupabase). */
    suspend fun saveWithFallbacks(p: UserProfile): Boolean {
        val body = fieldsJson(p)
        for (id in listOf(p.appleUserID, p.appleUserID.lowercase()).distinct()) {
            val r = runCatching { Http.restPatch("users?apple_user_id=eq.${Http.enc(id)}", body, returnRepresentation = true) }.getOrNull()
            if (r != null && r.code == 200 && r.jsonArray().length() > 0) return true
        }
        if (p.email.isNotEmpty()) {
            val r = runCatching { Http.restPatch("users?email=eq.${Http.enc(p.email)}", body, returnRepresentation = true) }.getOrNull()
            if (r != null && r.code == 200 && r.jsonArray().length() > 0) return true
        }
        body.put("apple_user_id", p.appleUserID); body.put("email", p.email)
        return runCatching { Http.restPost("users", body).code == 201 }.getOrDefault(false)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WelcomeSetupView — 4-step first-time profile setup
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun WelcomeSetupScreen(profile: UserProfile, onComplete: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var displayName by rememberSaveable { mutableStateOf(profile.displayName) }
    var bio by rememberSaveable { mutableStateOf(profile.bio) }
    var hometown by rememberSaveable { mutableStateOf(profile.hometown) }
    var favoriteRide by rememberSaveable { mutableStateOf(profile.favoriteRide) }
    var ridingSince by rememberSaveable { mutableStateOf(profile.ridingSince) }
    var rideType by rememberSaveable { mutableStateOf(profile.preferredRideType) }
    var route by rememberSaveable { mutableStateOf(profile.favoriteRoute) }
    var club by rememberSaveable { mutableStateOf(profile.club) }
    var instagram by rememberSaveable { mutableStateOf(profile.instagramHandle) }
    var tiktok by rememberSaveable { mutableStateOf(profile.tiktokHandle) }
    var youtube by rememberSaveable { mutableStateOf(profile.youtubeChannel) }
    var facebook by rememberSaveable { mutableStateOf(profile.facebookHandle) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val total = 4
    val titles = listOf("Photos & Name", "Your Ride", "Crew & Bio", "Social Media")
    val profilePicker = rememberImagePicker { profileUri = it }
    val bannerPicker = rememberImagePicker { bannerUri = it }
    val canProceed = when (step) { 0 -> displayName.isNotBlank(); 1 -> favoriteRide.isNotBlank(); else -> true }

    fun finish() {
        isSaving = true
        scope.launch {
            var p = (LocalStore.profileFor(profile.appleUserID) ?: profile).copy(
                displayName = displayName, bio = bio, hometown = hometown, favoriteRide = favoriteRide,
                ridingSince = ridingSince, preferredRideType = rideType, favoriteRoute = route, club = club,
                instagramHandle = instagram.replace("@", ""), tiktokHandle = tiktok.replace("@", ""),
                youtubeChannel = youtube, facebookHandle = facebook, hasCompletedSetup = true
            )
            Prefs.hasCompletedProfileSetup = true
            val pBytes = profileUri?.let { ImageUtils.uriToJpeg(context, it, quality = 80) }
            val bBytes = bannerUri?.let { ImageUtils.uriToJpeg(context, it, quality = 80) }
            p = ProfileSync.uploadImages(p, pBytes, bBytes, lowercaseNames = true)
            LocalStore.upsertProfile(p)
            ProfileSync.saveWithFallbacks(p)
            isSaving = false
            onComplete()
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            Box(Modifier.fillMaxWidth().statusBarsPadding().height(3.dp).background(Color.White.copy(alpha = 0.1f))) {
                Box(Modifier.fillMaxWidth((step + 1) / total.toFloat()).height(3.dp).background(AppColors.Yellow))
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(Modifier.padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShieldLogo(55.dp)
                    Text("Welcome to On Tha Set!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Step ${step + 1} of $total — ${titles[step]}", color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                when (step) {
                    0 -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.fillMaxWidth().padding(bottom = 42.dp), contentAlignment = Alignment.BottomCenter) {
                            if (bannerUri != null) AppImage(bannerUri, Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)))
                            else Column(
                                Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f)),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Filled.Image, null, tint = AppColors.Gray)
                                Text("Banner photo", color = AppColors.Gray, fontSize = 11.sp)
                            }
                            Box(Modifier.offset(y = 38.dp).size(82.dp).clip(CircleShape).background(Color.Black), contentAlignment = Alignment.Center) {
                                if (profileUri != null) AppImage(profileUri, Modifier.size(78.dp).clip(CircleShape))
                                else Box(Modifier.size(78.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Person, null, tint = AppColors.Yellow.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PickChip("Profile Photo", Icons.Filled.AddAPhoto) { profilePicker.pickFromLibrary() }
                            PickChip("Banner Photo", Icons.Filled.AddPhotoAlternate) { bannerPicker.pickFromLibrary() }
                        }
                        IconField(Icons.Filled.Person, "Rider name (required)", displayName) { displayName = it }
                    }
                    1 -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        IconField(Icons.Filled.TwoWheeler, "What do you ride? e.g. Road Glide (required)", favoriteRide) { favoriteRide = it }
                        IconField(Icons.Filled.Flag, "Riding since — year e.g. 2009", ridingSince, KeyboardType.Number) { ridingSince = it }
                        IconField(Icons.Filled.LocationOn, "Hometown e.g. Las Vegas, NV", hometown) { hometown = it }
                        IconField(Icons.Filled.Map, "Favorite route or road (optional)", route) { route = it }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AddRoad, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Preferred Ride Style", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            listOf("Solo", "Group Rides", "Long Distance", "Track Days", "Weekend Warrior", "Any").chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { style ->
                                        val sel = rideType == style
                                        Text(
                                            style, color = if (sel) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (sel) AppColors.Yellow else Color.White.copy(alpha = 0.08f))
                                                .clickable { rideType = style }.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        IconField(Icons.Filled.Groups, "Club or organization (optional)", club) { club = it }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FormatQuote, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Bio", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(Modifier.fillMaxWidth().heightIn(min = 120.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.08f)).padding(14.dp)) {
                            if (bio.isEmpty()) Text("Tell the community about yourself...", color = AppColors.Gray.copy(alpha = 0.5f))
                            BasicTextField(bio, { bio = it }, textStyle = TextStyle(color = Color.White, fontSize = 16.sp), cursorBrush = SolidColor(AppColors.Yellow), modifier = Modifier.fillMaxWidth())
                        }
                    }
                    else -> Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Connect your socials so other riders can follow you", color = AppColors.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        SocialField(Icons.Filled.CameraAlt, Color(0.8f, 0.1f, 0.5f), "Instagram username", instagram) { instagram = it }
                        SocialField(Icons.Filled.MusicNote, Color(0xFF111111), "TikTok username", tiktok) { tiktok = it }
                        SocialField(Icons.Filled.SmartDisplay, AppColors.Red, "YouTube channel", youtube) { youtube = it }
                        SocialField(Icons.Filled.People, AppColors.Blue, "Facebook name or URL", facebook) { facebook = it }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 0) Text(
                        "Back", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(AppColors.Yellow.copy(alpha = 0.15f))
                            .clickable { step -= 1 }.padding(vertical = 14.dp)
                    )
                    Row(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (canProceed) AppColors.Yellow else AppColors.Gray)
                            .clickable(enabled = canProceed && !isSaving) { if (step < total - 1) step += 1 else finish() }.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSaving) { CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                        Text(if (step < total - 1) "Next →" else "Let's Ride! 🏍️", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("Skip for now", color = AppColors.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 30.dp).clickable {
                    LocalStore.updateProfile(profile.appleUserID) { it.copy(hasCompletedSetup = true) }
                    Prefs.hasCompletedProfileSetup = true
                    onComplete()
                })
            }
        }
    }
}

@Composable
private fun PickChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.Yellow.copy(alpha = 0.15f)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IconField(icon: ImageVector, placeholder: String, value: String, keyboard: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.08f)).padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.width(20.dp))
        FormTextField(value, onChange, placeholder, background = Color.Transparent, keyboardType = keyboard)
    }
}

@Composable
private fun SocialField(icon: ImageVector, color: Color, placeholder: String, value: String, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.08f)).padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        FormTextField(value, onChange, placeholder, background = Color.Transparent, keyboardType = KeyboardType.Uri)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EditProfileView
// ─────────────────────────────────────────────────────────────────────────────

/** Extracts just the handle/username from a pasted social URL (EditProfileView.extractHandle). */
fun extractHandle(input: String): String {
    var value = input.trim()
    if (value.contains("facebook.com") || value.contains("fb.com") || value.contains("fb.me")) {
        if (!value.startsWith("http")) value = "https://$value"
        return value
    }
    if (!value.contains("://") && !value.contains(".com/")) return value.removePrefix("@")
    value.indexOf("://").takeIf { it >= 0 }?.let { value = value.substring(it + 3) }
    value = value.replace("www.", "")
    for (d in listOf("instagram.com/", "tiktok.com/@", "tiktok.com/", "youtube.com/@", "youtube.com/")) {
        if (value.startsWith(d)) { value = value.removePrefix(d); break }
    }
    value = value.substringBefore("?").substringBefore("#").trim('/')
    return value.removePrefix("@")
}

@Composable
fun EditProfileScreen(userID: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val initial = remember { LocalStore.profileFor(userID) }
    if (initial == null) { androidx.compose.runtime.LaunchedEffect(Unit) { onClose() }; return }
    var p by remember { mutableStateOf<UserProfile>(initial) }
    var newProfileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var newBgBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var bgUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    val profilePicker = rememberImagePicker { uri -> profileUri = uri; scope.launch { newProfileBytes = ImageUtils.uriToJpeg(context, uri) } }
    val bgPicker = rememberImagePicker { uri -> bgUri = uri; scope.launch { newBgBytes = ImageUtils.uriToJpeg(context, uri) } }

    fun save() {
        isSaving = true
        scope.launch {
            // Upload new images if picked, else re-upload existing local images like iOS did.
            val pBytes = newProfileBytes ?: p.profileImagePath?.let { runCatching { java.io.File(it).readBytes() }.getOrNull() }
            val bBytes = newBgBytes ?: p.backgroundImagePath?.let { runCatching { java.io.File(it).readBytes() }.getOrNull() }
            val updated = ProfileSync.uploadImages(p, pBytes, bBytes, lowercaseNames = false)
            LocalStore.upsertProfile(updated)
            val ok = SupabaseManager.updateUser(updated.appleUserID, ProfileSync.fieldsJson(updated))
            isSaving = false
            if (ok) onClose() else saveError = true
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().imePadding()) {
            TopBar(onBack = onClose)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(25.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("EDIT PROFILE", color = AppColors.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)

                // IMAGES
                Text("Profile Picture", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Box(Modifier.size(120.dp).clickable { profilePicker.pickFromLibrary() }) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        if (profileUri != null) AppImage(profileUri, Modifier.fillMaxSize().clip(CircleShape))
                        else ProfileAvatar(p.profileImagePath, p.profileImageURL, 120.dp)
                    }
                    Box(Modifier.align(Alignment.BottomEnd).padding(6.dp).clip(CircleShape).background(AppColors.Yellow).padding(8.dp)) {
                        Icon(Icons.Filled.CameraAlt, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    }
                }
                Text("Background Image", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).clickable { bgPicker.pickFromLibrary() }, contentAlignment = Alignment.Center) {
                    val bgModel: Any? = bgUri ?: p.backgroundImagePath?.takeIf { java.io.File(it).exists() } ?: p.backgroundImageURL.takeIf { it.isNotEmpty() }
                    if (bgModel != null) AppImage(bgModel, Modifier.fillMaxSize())
                    else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Yellow)
                        Text("Tap to add background", color = AppColors.Gray, fontSize = 12.sp)
                    }
                }

                EditSection("BASIC INFO") {
                    LabeledInput("Display Name", p.displayName, "Your name") { p = p.copy(displayName = it) }
                    LabeledInput("Bio", p.bio, "Tell us about yourself", singleLine = false) { p = p.copy(bio = it) }
                    LabeledInput("Hometown", p.hometown, "Where you're from") { p = p.copy(hometown = it) }
                    LabeledInput("Club", p.club, "Motorcycle club (optional)") { p = p.copy(club = it) }
                }
                EditSection("RIDING INFO") {
                    LabeledInput("Favorite Ride", p.favoriteRide, "Your motorcycle") { p = p.copy(favoriteRide = it) }
                    LabeledInput("Riding Since", p.ridingSince, "Year", keyboard = KeyboardType.Number) { p = p.copy(ridingSince = it) }
                    LabeledInput("Preferred Ride Type", p.preferredRideType, "Solo, Group, Long Distance, etc.") { p = p.copy(preferredRideType = it) }
                    LabeledInput("Favorite Route", p.favoriteRoute, "Your favorite place to ride") { p = p.copy(favoriteRoute = it) }
                }
                EditSection("SOCIAL MEDIA") {
                    Text("Paste your full profile URL or just your username", color = AppColors.Gray, fontSize = 12.sp)
                    SocialEdit("Instagram", Icons.Filled.CameraAlt, AppColors.Purple, "username or full URL", p.instagramHandle, "instagram.com/") { p = p.copy(instagramHandle = extractHandle(it)) }
                    SocialEdit("TikTok", Icons.Filled.MusicNote, AppColors.Pink, "username or full URL", p.tiktokHandle, "tiktok.com/@") { p = p.copy(tiktokHandle = extractHandle(it)) }
                    SocialEdit("YouTube", Icons.Filled.SmartDisplay, AppColors.Red, "channel name or full URL", p.youtubeChannel, "youtube.com/@") { p = p.copy(youtubeChannel = extractHandle(it)) }
                    SocialEdit("Facebook", Icons.Filled.People, AppColors.Blue, "Paste your full Facebook profile URL", p.facebookHandle, "") { p = p.copy(facebookHandle = extractHandle(it)) }
                }
                Box(Modifier.padding(bottom = 40.dp)) { YellowButton("SAVE PROFILE", ::save, loading = isSaving, corner = 12.dp) }
            }
        }
    }
    if (saveError) InfoAlert("Save Failed", "Your profile was saved locally but could not sync to the server. It will retry next time.", onDismiss = { saveError = false; onClose() })
}

@Composable
private fun EditSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Text(title, color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun LabeledInput(label: String, value: String, placeholder: String, singleLine: Boolean = true, keyboard: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        FormTextField(value, onChange, placeholder, singleLine = singleLine, minLines = if (singleLine) 1 else 3, keyboardType = keyboard)
    }
}

@Composable
private fun SocialEdit(label: String, icon: ImageVector, color: Color, placeholder: String, value: String, prefix: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        FormTextField(value, onChange, placeholder, keyboardType = KeyboardType.Uri)
        if (value.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = AppColors.Green, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (prefix.isEmpty()) value else "Will link to: $prefix$value", color = AppColors.Green, fontSize = 11.sp, maxLines = 1)
        }
    }
}
