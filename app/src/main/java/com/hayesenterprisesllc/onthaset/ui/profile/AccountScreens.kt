package com.hayesenterprisesllc.onthaset.ui.profile

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.billing.BillingManager
import com.hayesenterprisesllc.onthaset.data.AppConfig
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.BikeProgress
import com.hayesenterprisesllc.onthaset.data.EventPhoto
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.data.LocalEvent
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.Products
import com.hayesenterprisesllc.onthaset.data.PublicBikeBuild
import com.hayesenterprisesllc.onthaset.data.PublicEventPhoto
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.data.SupabaseUserProfile
import com.hayesenterprisesllc.onthaset.data.UserProfile
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ConfirmAlert
import com.hayesenterprisesllc.onthaset.ui.components.FullScreenImageViewer
import com.hayesenterprisesllc.onthaset.ui.components.GalleryViewer
import com.hayesenterprisesllc.onthaset.ui.components.ImagePlaceholder
import com.hayesenterprisesllc.onthaset.ui.components.ProfileAvatar
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import com.hayesenterprisesllc.onthaset.util.Intents
import kotlinx.coroutines.launch
import java.io.File

// ─────────────────────────────────────────────────────────────────────────────
// MyAccountView — own profile + floating action buttons
// ─────────────────────────────────────────────────────────────────────────────

enum class PostType { EVENT, PHOTO, BIKE }

/** Same post-permission logic as MyAccountView.handlePostAttempt. */
fun canUserPost(profile: UserProfile?): Boolean {
    val id = AuthService.currentUserId
    if (AppConfig.isOwner(id) || AppConfig.isDemoUser(id)) return true
    return BillingManager.purchasedProductIDs.value.contains(Products.SINGLE_POST) ||
        BillingManager.hasActiveSubscription.value ||
        (profile?.hasActiveSubscription == true)
}

@Composable
fun MyAccountScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onWelcomeSetup: () -> Unit,
    onPost: (PostType) -> Unit,
    onPayment: () -> Unit,
    onExtraPost: () -> Unit,
    onLocalEventClick: (LocalEvent) -> Unit
) {
    val user by AuthService.currentUser.collectAsState()
    val profiles by LocalStore.profiles.collectAsState()
    val profile = profiles.firstOrNull { it.appleUserID == user?.id } ?: profiles.firstOrNull { it.appleUserID.equals(user?.id, true) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(profile?.hasCompletedSetup) {
        if (profile != null && !profile.hasCompletedSetup) onWelcomeSetup()
    }

    if (profile == null) {
        BlackScreen {
            Column(Modifier.fillMaxSize()) {
                TopBar(onBack = onBack)
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
                    com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo(100.dp)
                    Text("Welcome to On Tha Set", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Sign in to post events and connect with the riding community.", color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                }
            }
        }
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        PublicProfileContent(profile, onLocalEventClick)
        TopBar(onBack = onBack, actions = {
            Box {
                IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreHoriz, "Menu", tint = AppColors.Yellow) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Edit Profile") }, leadingIcon = { Icon(Icons.Filled.Edit, null) }, onClick = { menuOpen = false; onEditProfile() })
                    DropdownMenuItem(text = { Text("Settings") }, leadingIcon = { Icon(Icons.Filled.Settings, null) }, onClick = { menuOpen = false; onSettings() })
                }
            }
        })
        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val attempt: (PostType) -> Unit = { t ->
                val id = AuthService.currentUserId
                val privileged = AppConfig.isOwner(id) || AppConfig.isDemoUser(id) ||
                    BillingManager.purchasedProductIDs.value.contains(Products.SINGLE_POST)
                when {
                    !canUserPost(profile) -> onPayment()
                    // Subscribers get 4 event posts per month (DefaultPageView.handlePostAttempt)
                    t == PostType.EVENT && !privileged && profile.withMonthlyCountChecked().postsThisMonth >= UserProfile.MONTHLY_POST_LIMIT -> onExtraPost()
                    else -> onPost(t)
                }
            }
            ActionButton(Icons.Filled.AddCircle, "Post Event", AppColors.Yellow, Modifier.weight(1f)) { attempt(PostType.EVENT) }
            ActionButton(Icons.Filled.CameraAlt, "Event Photo", AppColors.Purple, Modifier.weight(1f)) { attempt(PostType.PHOTO) }
            ActionButton(Icons.Filled.Build, "Bike Build", AppColors.Orange, Modifier.weight(1f)) { attempt(PostType.BIKE) }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.shadow(5.dp, RoundedCornerShape(12.dp), ambientColor = color, spotColor = color).clip(RoundedCornerShape(12.dp)).background(color)
            .clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = Color.Black)
        Text(label, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PublicProfileView (own profile body)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PublicProfileContent(profile: UserProfile, onLocalEventClick: (LocalEvent) -> Unit) {
    val scope = rememberCoroutineScope()
    val events by LocalStore.events.collectAsState()
    val photos by LocalStore.photos.collectAsState()
    val bikes by LocalStore.bikeProgress.collectAsState()
    val userEvents = events.sortedByDescending { it.date }
    val userPhotos = photos.filter { it.userID == profile.appleUserID }.sortedByDescending { it.eventDate }
    val userBikes = bikes.filter { it.userID == profile.appleUserID }

    var gallery by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }
    var fullImage by remember { mutableStateOf<Any?>(null) }
    var photoToDelete by remember { mutableStateOf<EventPhoto?>(null) }
    var bikeToDelete by remember { mutableStateOf<BikeProgress?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // BACKGROUND
        val bgModel: Any? = profile.backgroundImagePath?.takeIf { File(it).exists() }?.let { File(it) } ?: profile.backgroundImageURL.takeIf { it.isNotEmpty() }
        val gradient: @Composable () -> Unit = {
            Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(listOf(AppColors.Yellow.copy(alpha = 0.3f), Color.Black))))
        }
        if (bgModel != null) AppImage(bgModel, Modifier.fillMaxWidth().wrapContentHeight(), contentScale = ContentScale.FillWidth, placeholder = gradient, loading = gradient)
        else gradient()

        // HEADER
        Column(Modifier.fillMaxWidth().offset(y = (-50).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Box(Modifier.size(120.dp).shadow(10.dp, CircleShape).clip(CircleShape).background(Color.Black).border(4.dp, AppColors.Yellow, CircleShape), contentAlignment = Alignment.Center) {
                ProfileAvatar(profile.profileImagePath, profile.profileImageURL, 110.dp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(profile.displayName.ifEmpty { profile.email.substringBefore("@").ifEmpty { "Rider" } }, color = AppColors.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                if (profile.bio.isNotEmpty()) Text(profile.bio, color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
            }
        }

        Column(Modifier.padding(horizontal = 16.dp).offset(y = (-30).dp), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            ProfileCard {
                SectionTitle(Icons.Filled.Badge, "ABOUT ME")
                if (profile.hometown.isNotEmpty()) InfoRow(Icons.Filled.Home, "Hometown", profile.hometown)
                if (profile.club.isNotEmpty()) InfoRow(Icons.Filled.Groups, "Club", profile.club)
                if (profile.favoriteRide.isNotEmpty()) InfoRow(Icons.Filled.TwoWheeler, "Favorite Ride", profile.favoriteRide)
                if (profile.ridingSince.isNotEmpty()) InfoRow(Icons.Filled.CalendarMonth, "Riding Since", profile.ridingSince)
                InfoRow(Icons.Filled.Star, "Member Since", DateUtils.abbreviated(profile.memberSince))
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp)) {
                StatBox(Icons.Filled.EventAvailable, "${userEvents.size}", "Events", Modifier.weight(1f))
                StatBox(Icons.Filled.Image, "${profile.photosStoredTotal}/${UserProfile.TOTAL_PHOTO_LIMIT}", "Photos", Modifier.weight(1f))
                StatBox(Icons.Filled.Build, "${userBikes.size}", "Builds", Modifier.weight(1f))
            }
            ProfileCard {
                SectionTitle(Icons.Filled.TwoWheeler, "RIDING INFO")
                if (profile.preferredRideType.isNotEmpty()) InfoRow(Icons.Filled.People, "Ride Style", profile.preferredRideType)
                if (profile.favoriteRoute.isNotEmpty()) InfoRow(Icons.Filled.Map, "Favorite Route", profile.favoriteRoute)
            }
            SocialLinks(profile.instagramHandle, profile.tiktokHandle, profile.youtubeChannel, profile.facebookHandle, compact = true)

            if (userEvents.isNotEmpty()) ProfileCard {
                SectionTitle(Icons.Filled.CalendarMonth, "LATEST EVENTS")
                userEvents.take(6).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { e ->
                            Column(Modifier.weight(1f).clickable { onLocalEventClick(e) }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (e.imagePath != null) AppImage(e.imagePath, Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(10.dp)))
                                else Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.1f)))
                                Text(e.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (userPhotos.isNotEmpty()) ProfileCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle(Icons.Filled.PhotoLibrary, "PHOTO GALLERY")
                    Spacer(Modifier.weight(1f))
                    Text("${userPhotos.size}", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                userPhotos.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { photo ->
                            Box(Modifier.weight(1f).aspectRatio(1f)) {
                                val model: Any = if (photo.photoFileName.startsWith("http")) photo.photoFileName else LocalStore.localImageFile(photo.photoFileName)
                                AppImage(model, Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).clickable {
                                    val models: List<Any> = userPhotos.map { if (it.photoFileName.startsWith("http")) it.photoFileName else LocalStore.localImageFile(it.photoFileName) }
                                    gallery = models to userPhotos.indexOf(photo)
                                })
                                Icon(
                                    Icons.Filled.Cancel, "Delete", tint = AppColors.Red,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(18.dp).clip(CircleShape).background(Color.Black).clickable { photoToDelete = photo }
                                )
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            if (userBikes.isNotEmpty()) ProfileCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle(Icons.Filled.Build, "MY BIKE BUILDS")
                    Spacer(Modifier.weight(1f))
                    Text("${userBikes.size}", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                userBikes.forEach { b ->
                    BikeProgressCard(
                        title = b.modificationTitle, make = b.bikeMake, model = b.bikeModel, year = b.bikeYear,
                        dateText = DateUtils.longDate(b.createdAt), note = b.note,
                        beforeImage = b.beforeImage, afterImage = b.afterImage,
                        onImageTap = { fullImage = it },
                        onDelete = { bikeToDelete = b }
                    )
                }
            }
            Spacer(Modifier.height(160.dp))
        }
    }

    gallery?.let { (models, idx) -> GalleryViewer(models, idx) { gallery = null } }
    fullImage?.let { FullScreenImageViewer(it, onDismiss = { fullImage = null }) }
    photoToDelete?.let { photo ->
        ConfirmAlert("Delete Photo?", "This will permanently delete this photo.", "Delete", onConfirm = {
            val url = photo.photoFileName
            scope.launch {
                SupabaseManager.deleteStorageFile(url, "event-photos")
                SupabaseManager.deleteEventPhotoRecord(url, profile.appleUserID)
            }
            LocalStore.updateProfile(profile.appleUserID) { it.withDecrementedPhotoCount() }
            LocalStore.deletePhoto(photo)
        }, onDismiss = { photoToDelete = null })
    }
    bikeToDelete?.let { entry ->
        ConfirmAlert("Delete Bike Build?", "This will delete both photos. This cannot be undone.", "Delete", onConfirm = {
            scope.launch {
                SupabaseManager.deleteStorageFile(entry.beforeImage, "bike-progress")
                SupabaseManager.deleteStorageFile(entry.afterImage, "bike-progress")
                SupabaseManager.deleteBikeBuildRecord(entry.afterImage, profile.appleUserID)
            }
            LocalStore.deleteBikeProgress(entry)
        }, onDismiss = { bikeToDelete = null })
    }
}

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow)
        Spacer(Modifier.width(8.dp))
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.width(20.dp).size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun StatBox(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = AppColors.Yellow)
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppColors.Gray, fontSize = 11.sp)
    }
}

/** Instagram / TikTok / YouTube / Facebook link buttons. */
@Composable
fun SocialLinks(instagram: String?, tiktok: String?, youtube: String?, facebook: String?, compact: Boolean) {
    val context = LocalContext.current
    val links = buildList {
        if (!instagram.isNullOrEmpty()) add(Triple(Icons.Filled.CameraAlt to AppColors.Purple, "Instagram", "https://instagram.com/$instagram"))
        if (!tiktok.isNullOrEmpty()) add(Triple(Icons.Filled.MusicNote to AppColors.Pink, "TikTok", "https://tiktok.com/@$tiktok"))
        if (!youtube.isNullOrEmpty()) add(Triple(Icons.Filled.SmartDisplay to AppColors.Red, "YouTube", "https://youtube.com/@$youtube"))
        if (!facebook.isNullOrEmpty()) add(Triple(Icons.Filled.People to AppColors.Blue, "Facebook", if (facebook.startsWith("http")) facebook else "https://facebook.com/$facebook"))
    }
    if (links.isEmpty()) return
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 15.dp else 12.dp)) {
        if (compact) SectionTitle(Icons.Filled.Link, "CONNECT") else Text("SOCIAL MEDIA", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        if (compact) {
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                links.forEach { (ic, label, url) ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(ic.second.copy(alpha = 0.2f)).clickable { Intents.openUrl(context, url) }.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(ic.first, null, tint = ic.second)
                        Text(label, color = Color.White, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        } else links.forEach { (ic, label, url) ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Card).clickable { Intents.openUrl(context, url) }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(ic.second), contentAlignment = Alignment.Center) { Icon(ic.first, null, tint = Color.White) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(label, color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Icon(Icons.Filled.NorthEast, null, tint = AppColors.Gray, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BikeProgressCard (before / after)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BikeProgressCard(
    title: String, make: String, model: String, year: String, dateText: String?, note: String,
    beforeImage: String, afterImage: String,
    onImageTap: (Any) -> Unit, onDelete: (() -> Unit)? = null, panelHeight: androidx.compose.ui.unit.Dp = 160.dp
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Card)
            .border(1.dp, AppColors.Yellow.copy(alpha = 0.2f), RoundedCornerShape(14.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                if (make.isNotEmpty() || model.isNotEmpty() || year.isNotEmpty())
                    Text("$year $make $model".trim(), color = AppColors.Yellow, fontSize = 15.sp)
                if (dateText != null) Text(dateText, color = AppColors.Gray, fontSize = 12.sp)
            }
            if (onDelete != null) Icon(
                Icons.Filled.Delete, "Delete", tint = AppColors.Red.copy(alpha = 0.7f),
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.Red.copy(alpha = 0.1f)).clickable(onClick = onDelete).padding(8.dp).size(14.dp)
            )
        }
        if (beforeImage.isNotEmpty() || afterImage.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (beforeImage.isNotEmpty()) PhotoPanel(beforeImage, "BEFORE", AppColors.Gray, panelHeight, Modifier.weight(1f), onImageTap)
            if (afterImage.isNotEmpty()) PhotoPanel(afterImage, "AFTER", AppColors.Yellow, panelHeight, Modifier.weight(1f), onImageTap)
            else if (beforeImage.isNotEmpty()) Column(
                Modifier.weight(1f).height(panelHeight).clip(RoundedCornerShape(10.dp)).background(AppColors.Card),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, null, tint = AppColors.Yellow.copy(alpha = 0.5f))
                Text("AFTER\nCOMING SOON", color = AppColors.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
        if (note.isNotEmpty()) Text(note, color = AppColors.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun PhotoPanel(source: String, label: String, labelColor: Color, height: androidx.compose.ui.unit.Dp, modifier: Modifier, onTap: (Any) -> Unit) {
    val model: Any = if (source.startsWith("http")) source else LocalStore.localImageFile(source)
    Box(modifier.height(height).clip(RoundedCornerShape(10.dp)).clickable { onTap(model) }) {
        AppImage(model, Modifier.fillMaxSize(), placeholder = { ImagePlaceholder(Modifier.fillMaxSize(), label = "Photo unavailable", corner = 10.dp) })
        Text(
            label, color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp).clip(RoundedCornerShape(6.dp)).background(labelColor).padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PostedByProfileView — another rider's profile, from Supabase
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PostedByProfileScreen(userID: String, posterName: String, onBack: () -> Unit) {
    var isLoading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<SupabaseUserProfile?>(null) }
    var photos by remember { mutableStateOf<List<PublicEventPhoto>>(emptyList()) }
    var builds by remember { mutableStateOf<List<PublicBikeBuild>>(emptyList()) }
    var gallery by remember { mutableStateOf<Pair<List<Any>, Int>?>(null) }

    LaunchedEffect(userID) {
        if (userID.isNotEmpty() && !userID.startsWith("device-")) {
            val row = SupabaseManager.fetchUserRow(userID)
            profile = row?.let { runCatching { SupabaseUserProfile.fromJson(it) }.getOrNull() }
                ?: SupabaseUserProfile(appleUserID = userID, displayName = posterName, email = "")
        }
        isLoading = false
        if (userID.isNotEmpty()) {
            runCatching { Http.restGet("event_photos?uploaded_by=eq.${Http.enc(userID)}&order=created_at.desc&limit=50").jsonArray() }.getOrNull()?.let { arr ->
                photos = (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let { PublicEventPhoto.fromJson(it) } }
            }
            runCatching { Http.restGet("bike_builds?user_id=eq.${Http.enc(userID)}&order=created_at.desc").jsonArray() }.getOrNull()?.let { arr ->
                builds = (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let { PublicBikeBuild.fromJson(it) } }
            }
        }
    }

    BlackScreen {
        if (isLoading) {
            CircularProgressIndicator(color = AppColors.Yellow, modifier = Modifier.align(Alignment.Center))
        } else {
            val p = profile ?: SupabaseUserProfile(appleUserID = userID, displayName = posterName, email = "")
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding()) {
                val gradient: @Composable () -> Unit = {
                    Box(Modifier.fillMaxWidth().height(220.dp).background(Brush.verticalGradient(listOf(AppColors.Yellow.copy(alpha = 0.3f), Color.Black))))
                }
                if (!p.backgroundImageURL.isNullOrEmpty()) AppImage(p.backgroundImageURL, Modifier.fillMaxWidth().wrapContentHeight(), contentScale = ContentScale.FillWidth, placeholder = gradient, loading = gradient)
                else gradient()

                Column(Modifier.fillMaxWidth().offset(y = (-50).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(100.dp).shadow(8.dp, CircleShape, ambientColor = AppColors.Yellow, spotColor = AppColors.Yellow).clip(CircleShape).background(AppColors.Yellow.copy(alpha = 0.2f)).border(3.dp, AppColors.Yellow, CircleShape), contentAlignment = Alignment.Center) {
                        ProfileAvatar(null, p.profileImageURL, 100.dp)
                    }
                    Text(p.displayName.ifEmpty { posterName }, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (!p.club.isNullOrEmpty()) Text(
                        p.club, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(AppColors.Yellow).padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Column(Modifier.offset(y = (-30).dp).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!p.bio.isNullOrEmpty()) Text(p.bio, color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp))

                    val riding = listOfNotNull(
                        p.favoriteRide?.takeIf { it.isNotEmpty() }?.let { Triple(Icons.Filled.TwoWheeler, "Ride", it) },
                        p.ridingSince?.takeIf { it.isNotEmpty() }?.let { Triple(Icons.Filled.Flag, "Riding Since", it) },
                        p.hometown?.takeIf { it.isNotEmpty() }?.let { Triple(Icons.Filled.LocationOn, "Hometown", it) },
                        p.preferredRideType?.takeIf { it.isNotEmpty() }?.let { Triple(Icons.Filled.AddRoad, "Ride Style", it) }
                    )
                    if (riding.isNotEmpty()) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("RIDING INFO", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        riding.forEach { (icon, label, value) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.width(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(label, color = AppColors.Gray, fontSize = 12.sp)
                                    Text(value, color = Color.White, fontSize = 15.sp)
                                }
                            }
                        }
                    }

                    SocialLinks(p.instagramHandle, p.tiktokHandle, p.youtubeChannel, p.facebookHandle, compact = false)

                    if (photos.isNotEmpty()) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhotoLibrary, null, tint = AppColors.Yellow)
                            Spacer(Modifier.width(6.dp))
                            Text("EVENT PHOTOS", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("${photos.size}", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        val urls = photos.map { it.imageURL }
                        urls.chunked(3).forEachIndexed { rowIdx, row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEachIndexed { i, url ->
                                    AppImage(url, Modifier.weight(1f).height(90.dp).clip(RoundedCornerShape(8.dp)).clickable { gallery = urls to (rowIdx * 3 + i) })
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                        Text(photos.first().eventName ?: "", color = AppColors.Gray, fontSize = 11.sp)
                    }

                    if (builds.isNotEmpty()) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Build, null, tint = AppColors.Yellow)
                            Spacer(Modifier.width(6.dp))
                            Text("MY BIKE BUILDS", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("${builds.size}", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        builds.forEach { b ->
                            BikeProgressCard(
                                title = b.modificationTitle, make = b.bikeMake, model = b.bikeModel, year = b.bikeYear, dateText = null, note = b.note,
                                beforeImage = b.beforeImageURL, afterImage = b.afterImageURL, panelHeight = 130.dp,
                                onImageTap = { m -> gallery = listOf(m) to 0 }
                            )
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
        Box(Modifier.align(Alignment.TopCenter)) { TopBar(onBack = onBack, title = "Rider Profile") }
    }
    gallery?.let { (models, idx) -> GalleryViewer(models, idx) { gallery = null } }
}

// ─────────────────────────────────────────────────────────────────────────────
// SettingsView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(onDone: () -> Unit, onSignedOut: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmSignOut by remember { mutableStateOf(false) }
    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            com.hayesenterprisesllc.onthaset.ui.components.SheetHeader(title = "Settings", closeLabel = "Done", onClose = onDone)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E))) {
                    SettingsRow(Icons.Filled.Refresh, "Restore Purchases", AppColors.Yellow) { scope.launch { BillingManager.restorePurchases() } }
                    HorizontalDivider(color = Color(0xFF38383A))
                    SettingsRow(Icons.Filled.CreditCard, "Manage Subscription", AppColors.Yellow) { Intents.manageSubscriptions(context) }
                }
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1C1C1E))) {
                    SettingsRow(Icons.AutoMirrored.Filled.Logout, "Sign Out", AppColors.Red) { confirmSignOut = true }
                }
            }
        }
    }
    if (confirmSignOut) ConfirmAlert("Sign Out", "Are you sure you want to sign out?", "Sign Out", onConfirm = {
        AuthService.logout(); onSignedOut()
    }, onDismiss = { confirmSignOut = false })
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color)
        Spacer(Modifier.width(14.dp))
        Text(label, color = color, fontSize = 17.sp)
    }
}
