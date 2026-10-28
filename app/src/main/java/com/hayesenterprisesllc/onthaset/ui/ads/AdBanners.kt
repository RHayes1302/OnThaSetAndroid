package com.hayesenterprisesllc.onthaset.ui.ads

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// Category styling shared by all ad views
// ─────────────────────────────────────────────────────────────────────────────

object AdStyle {
    fun accent(category: String): Color = when (category) {
        "lawyer" -> AppColors.Yellow
        "biker_bar" -> Color(0.8f, 0.2f, 0.2f)
        "tattoo" -> Color(0.5f, 0.0f, 0.5f)
        "entertainer" -> Color(0.9f, 0.4f, 0.0f)
        "sewing" -> Color(0.2f, 0.6f, 0.5f)
        "custom_paint" -> Color(0.0f, 0.5f, 0.9f)
        "leather_apparel" -> Color(0.4f, 0.2f, 0.0f)
        "towing" -> Color(0.9f, 0.7f, 0.0f)
        "photographer" -> Color(0.2f, 0.2f, 0.8f)
        "hotel" -> Color(0.0f, 0.6f, 0.4f)
        "food_truck" -> Color(0.9f, 0.3f, 0.1f)
        "detailing" -> Color(0.0f, 0.7f, 0.7f)
        "dealer" -> Color(0.1f, 0.5f, 0.1f)
        "embroidery" -> Color(0.7f, 0.3f, 0.6f)
        "barber" -> Color(0.2f, 0.5f, 0.8f)
        else -> Color(1.0f, 0.6f, 0.0f)
    }

    fun icon(category: String): String = when (category) {
        "lawyer" -> "⚖️"; "biker_bar" -> "🍺"; "tattoo" -> "🖋️"; "entertainer" -> "🎭"
        "sewing" -> "🪡"; "custom_paint" -> "🎨"; "leather_apparel" -> "🧥"; "towing" -> "🚗"
        "photographer" -> "📸"; "hotel" -> "🏨"; "food_truck" -> "🍔"; "detailing" -> "✨"
        "dealer" -> "🏪"; "embroidery" -> "🧵"; "barber" -> "💈"
        else -> "🏍️"
    }

    fun shortLabel(category: String): String = when (category) {
        "lawyer" -> "LEGAL"; "biker_bar" -> "BAR"; "tattoo" -> "TATTOO"; "entertainer" -> "ENTERTAIN"
        "sewing" -> "SEWING"; "custom_paint" -> "PAINT"; "leather_apparel" -> "APPAREL"; "towing" -> "TOWING"
        "photographer" -> "PHOTO"; "hotel" -> "LODGING"; "food_truck" -> "FOOD"; "detailing" -> "DETAIL"
        "dealer" -> "DEALER"; "embroidery" -> "EMBROIDER"; "barber" -> "BARBER"
        else -> "SHOP"
    }

    fun longLabel(category: String): String = when (category) {
        "lawyer" -> "LEGAL SERVICES"; "biker_bar" -> "BIKER FRIENDLY BAR"; "tattoo" -> "TATTOO ARTIST"
        "entertainer" -> "ENTERTAINER / DJ / BAND"; "sewing" -> "SEWING & CUSTOM PATCHES"
        "custom_paint" -> "CUSTOM PAINT & BODY"; "leather_apparel" -> "LEATHER & APPAREL"
        "towing" -> "TOWING & ROADSIDE"; "photographer" -> "PHOTOGRAPHER"; "hotel" -> "HOTEL & LODGING"
        "food_truck" -> "FOOD TRUCK / CATERING"; "detailing" -> "BIKE DETAILING"
        "dealer" -> "MOTORCYCLE DEALER"; "embroidery" -> "EMBROIDERY"; "barber" -> "BARBER / HAIR / BRAIDS"
        else -> "MOTORCYCLE SHOP"
    }

    /** All categories offered in the signup / edit forms. */
    val categories: List<Pair<String, String>> = listOf(
        "⚖️ Lawyer / Legal" to "lawyer", "🏍️ Motorcycle Shop" to "motorcycle_shop",
        "🍺 Biker Friendly Bar" to "biker_bar", "🎭 Entertainer / DJ / Band" to "entertainer",
        "🪡 Sewing & Patches" to "sewing", "🎨 Custom Paint" to "custom_paint",
        "🧥 Leather & Apparel" to "leather_apparel", "🚗 Towing & Roadside" to "towing",
        "🖋️ Tattoo Artist" to "tattoo", "📸 Photographer" to "photographer",
        "🏨 Hotel & Lodging" to "hotel", "🍔 Food Truck" to "food_truck",
        "✨ Detailing" to "detailing", "🏍️ Motorcycle Dealer" to "dealer",
        "🧵 Embroidery" to "embroidery", "💈 Barber / Hair / Braids" to "barber"
    )

    fun isAppointmentOnly(ad: SupabaseAd) = ad.notes?.contains("appointment_only") == true
}

@Composable
private fun PlanChip(text: String, bg: Color) {
    Text(
        text, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black,
        modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(bg).padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AdBannerContentView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdBannerContent(ad: SupabaseAd, modifier: Modifier = Modifier) {
    val accent = AdStyle.accent(ad.category)
    val (borderColor, borderWidth, glow) = when (ad.plan) {
        "premium" -> Triple(AppColors.Yellow, 2.dp, 8.dp)
        "featured" -> Triple(AppColors.Orange, 1.5.dp, 5.dp)
        else -> Triple(accent.copy(alpha = 0.5f), 1.dp, 0.dp)
    }
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(if (ad.plan == "basic") 100.dp else 110.dp)
            .shadow(glow, shape, ambientColor = borderColor, spotColor = borderColor)
            .clip(shape)
            .background(Color(0xFF0F0F0F))
            .border(borderWidth, borderColor, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            val badge: @Composable () -> Unit = {
                Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(AdStyle.icon(ad.category), fontSize = 22.sp)
                    Text(
                        AdStyle.shortLabel(ad.category), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(accent).padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            if (!ad.imageURL.isNullOrEmpty()) {
                AppImage(ad.imageURL, Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit, placeholder = badge)
            } else badge()
            when (ad.plan) {
                "premium" -> Text("👑", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd))
                "featured" -> Text("⭐", fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            when (ad.plan) {
                "premium" -> PlanChip("👑 PREMIUM", AppColors.Yellow)
                "featured" -> PlanChip("⭐ FEATURED", AppColors.Orange)
            }
            Text(ad.businessName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ad.tagline, color = AppColors.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!ad.address.isNullOrEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = accent, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(3.dp))
                Text(ad.address, color = AppColors.Gray, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!ad.phone.isNullOrEmpty()) {
                val appt = AdStyle.isAppointmentOnly(ad)
                Row(
                    Modifier.clip(RoundedCornerShape(5.dp)).background(accent).padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (appt) Icons.Filled.CalendarMonth else Icons.Filled.Phone, null, tint = Color.Black, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(if (appt) "Schedule" else "Call", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
            }
            Text("TAP FOR INFO", color = AppColors.Gray.copy(alpha = 0.6f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PremiumAdBannerView + PremiumAdStripView (shown on Events list & Weather)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PremiumAdBanner(ad: SupabaseAd, onClick: () -> Unit) {
    val accent = AdStyle.accent(ad.category)
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(110.dp)
            .shadow(8.dp, shape, ambientColor = AppColors.Yellow, spotColor = AppColors.Yellow)
            .clip(shape)
            .background(Color(0xFF121212))
            .border(1.5.dp, AppColors.Yellow, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconFallback: @Composable () -> Unit = {
            Box(Modifier.size(70.dp), contentAlignment = Alignment.Center) { Text(AdStyle.icon(ad.category), fontSize = 36.sp) }
        }
        if (!ad.imageURL.isNullOrEmpty()) {
            AppImage(ad.imageURL, Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Fit, placeholder = iconFallback, loading = iconFallback)
        } else iconFallback()
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlanChip("👑 PREMIUM", AppColors.Yellow)
                Spacer(Modifier.width(6.dp))
                Text("SPONSORED", color = AppColors.Gray.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Text(ad.businessName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ad.tagline, color = AppColors.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!ad.address.isNullOrEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = accent, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(3.dp))
                Text(ad.address, color = AppColors.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = AppColors.Yellow)
    }
}

/** Rotates through premium ads every 8 seconds. */
@Composable
fun PremiumAdStrip(onAdClick: (SupabaseAd) -> Unit) {
    val ads by SupabaseManager.activeAds.collectAsState()
    val premium = ads.filter { it.plan == "premium" }
    LaunchedEffect(Unit) { SupabaseManager.fetchActiveAds() }
    if (premium.isEmpty()) return

    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(premium.size) {
        if (premium.size > 1) while (true) {
            delay(8000)
            index = (index + 1) % premium.size
        }
    }
    AnimatedContent(
        targetState = index % premium.size,
        transitionSpec = { (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut()) },
        label = "premiumAd",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    ) { i ->
        val ad = premium[i.coerceIn(0, premium.size - 1)]
        PremiumAdBanner(ad) { onAdClick(ad) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RotatingAdStripView + LiveAdStripView (home page)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RotatingAdStrip(ads: List<SupabaseAd>, onAdClick: (SupabaseAd) -> Unit) {
    val pager = rememberPagerState { ads.size }
    LaunchedEffect(ads.size, pager.currentPage) {
        if (ads.size > 1) {
            delay(6000)
            pager.animateScrollToPage((pager.currentPage + 1) % ads.size)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("SPONSORED", color = AppColors.Gray.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Spacer(Modifier.weight(1f))
            if (ads.size > 1) Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(ads.size) { i ->
                    val sel = i == pager.currentPage
                    Box(Modifier.size(if (sel) 8.dp else 5.dp).clip(CircleShape).background(if (sel) AppColors.Yellow else AppColors.Gray.copy(alpha = 0.3f)))
                }
            }
        }
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth().height(112.dp)) { page ->
            Box(Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                AdBannerContent(ads[page], Modifier.clickable { onAdClick(ads[page]) })
            }
        }
        if (ads.size > 1) Text(
            "Swipe to browse • Tap to view details", color = AppColors.Gray.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LiveAdStrip(onAdClick: (SupabaseAd) -> Unit) {
    val ads by SupabaseManager.activeAds.collectAsState()
    LaunchedEffect(Unit) { SupabaseManager.fetchActiveAds() }
    if (ads.isNotEmpty()) {
        Box(Modifier.padding(vertical = 4.dp)) { RotatingAdStrip(ads, onAdClick) }
    }
}
