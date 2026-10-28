package com.hayesenterprisesllc.onthaset.ui.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.R
import com.hayesenterprisesllc.onthaset.billing.BillingManager
import com.hayesenterprisesllc.onthaset.data.AppConfig
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.Products
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.location.LocationManager
import com.hayesenterprisesllc.onthaset.ui.ads.LiveAdStrip
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ProfileAvatar
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.Intents

/** Port of DefaultPageView.swift — the main menu after sign-in. */
@Composable
fun HomeScreen(
    onViewEvents: () -> Unit,
    onNearby: () -> Unit,
    onNational: () -> Unit,
    onWeather: () -> Unit,
    onAccount: () -> Unit,
    onWelcomeSetup: () -> Unit,
    onAdvertise: () -> Unit,
    onAdClick: (SupabaseAd) -> Unit,
    onAdminLock: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit
) {
    val context = LocalContext.current
    val user by AuthService.currentUser.collectAsState()
    val profiles by LocalStore.profiles.collectAsState()
    val hasSub by BillingManager.hasActiveSubscription.collectAsState()
    val profile = remember(user, profiles) { LocalStore.profileFor(user?.id) }
    val needsSetup = profile != null && !profile.hasCompletedSetup
    var menuOpen by remember { mutableStateOf(false) }

    // 5-tap secret admin gesture on the shield
    var taps by remember { mutableIntStateOf(0) }
    var lastTap by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        if (hasSub && profile != null) LocalStore.updateProfile(profile.appleUserID) { it.copy(hasActiveSubscription = true) }
        SupabaseManager.fetchActiveAds()
        SupabaseManager.fetchAllEvents()
        // Remove local events that no longer exist on the server.
        val titles = SupabaseManager.events.value.map { it.title.lowercase() }.toSet()
        if (titles.isNotEmpty()) LocalStore.deleteEvents { it.title.lowercase() !in titles }
        else LocalStore.deleteEvents { true }
    }

    BlackScreen {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            ShieldLogo(
                85.dp,
                Modifier.padding(top = 70.dp, bottom = 10.dp).clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {
                    val now = System.currentTimeMillis()
                    taps = if (now - lastTap < 600) taps + 1 else 1
                    lastTap = now
                    if (taps >= 5) { taps = 0; onAdminLock() }
                }
            )

            if (user?.id == AppConfig.DEMO_USER_ID) {
                Box(Modifier.padding(horizontal = 40.dp)) {
                    YellowButton("Exit Demo — Sign In to Continue", { AuthService.logout() }, verticalPadding = 10.dp, leading = Icons.AutoMirrored.Filled.ArrowBack)
                }
            }

            Image(
                painterResource(R.drawable.onthaset_logo), contentDescription = "On Tha Set",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(280.dp).border(1.dp, AppColors.Yellow.copy(alpha = 0.5f))
            )

            Text("What's On Tha Set Nearby", color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            LiveAdStrip(onAdClick)

            Column(Modifier.padding(horizontal = 40.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                YellowButton("VIEW POSTED EVENTS", onViewEvents)
                YellowButton("EVENTS NEARBY", { LocationManager.requestLocation(); onNearby() })

                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Yellow)
                        .clickable(onClick = onNational).padding(vertical = 14.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Map, null, tint = Color.Black)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("NATIONAL RUN CALENDAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Rallies • Annuals • Unity Runs • Charity", color = Color.Black.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("🗺️", fontSize = 20.sp)
                }

                YellowButton("RIDE FORECAST", { LocationManager.requestLocation(); onWeather() })

                // MY ACCOUNT
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, AppColors.Yellow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { if (needsSetup) onWelcomeSetup() else onAccount() }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(56.dp).clip(CircleShape).background(AppColors.Yellow.copy(alpha = 0.2f))
                            .border(2.dp, AppColors.Yellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { ProfileAvatar(profile?.profileImagePath, profile?.profileImageURL, 54.dp) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            profile?.displayName?.takeIf { it.isNotEmpty() } ?: "MY ACCOUNT",
                            color = AppColors.Yellow, fontWeight = FontWeight.Bold, fontSize = 17.sp
                        )
                        Text("Post Events • Photos • Bike Builds", color = AppColors.Gray, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = AppColors.Yellow)
                }
            }

            // ADVERTISE WITH US
            Row(
                Modifier.padding(horizontal = 40.dp).fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = AppColors.Yellow, spotColor = AppColors.Yellow)
                    .clip(RoundedCornerShape(12.dp)).background(Color.Black)
                    .border(2.dp, AppColors.Yellow, RoundedCornerShape(12.dp))
                    .clickable(onClick = onAdvertise)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Campaign, null, tint = AppColors.Yellow)
                        Spacer(Modifier.width(6.dp))
                        Text("ADVERTISE WITH US", color = AppColors.Yellow, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    Text("Reach thousands of riders across the community", color = AppColors.Gray, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Plans from", color = AppColors.Gray, fontSize = 11.sp)
                        Text("$19.99/mo", color = AppColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("•", color = AppColors.Gray, fontSize = 11.sp)
                        Text("Basic", color = AppColors.Gray, fontSize = 11.sp)
                        Text("⭐ Featured", color = AppColors.Orange, fontSize = 11.sp)
                        Text("👑 Premium", color = AppColors.Yellow, fontSize = 11.sp)
                    }
                }
                Icon(Icons.Filled.ChevronRight, null, tint = AppColors.Yellow)
            }
            Spacer(Modifier.height(40.dp))
        }

        // Top-right "…" menu
        Box(Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp)) {
            IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreHoriz, "Menu", tint = AppColors.Yellow) }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("About On Tha Set") }, leadingIcon = { Icon(Icons.Filled.Info, null) }, onClick = { menuOpen = false; onAbout() })
                DropdownMenuItem(text = { Text("Privacy Policy") }, leadingIcon = { Icon(Icons.Filled.PanTool, null) }, onClick = { menuOpen = false; onPrivacy() })
                DropdownMenuItem(text = { Text("Terms of Service") }, leadingIcon = { Icon(Icons.Filled.Description, null) }, onClick = { menuOpen = false; onTerms() })
                DropdownMenuItem(text = { Text("Contact Us") }, leadingIcon = { Icon(Icons.Filled.Email, null) }, onClick = {
                    menuOpen = false
                    Intents.email(context, Products.CONTACT_EMAIL, "On Tha Set Inquiry")
                })
            }
        }
    }
}
