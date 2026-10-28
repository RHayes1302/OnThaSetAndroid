package com.hayesenterprisesllc.onthaset.ui.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.Http
import com.hayesenterprisesllc.onthaset.data.LocationParts
import com.hayesenterprisesllc.onthaset.data.Prefs
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.data.optStringOrNull
import com.hayesenterprisesllc.onthaset.ui.ads.NumPad
import com.hayesenterprisesllc.onthaset.ui.ads.PinDots
import com.hayesenterprisesllc.onthaset.ui.components.AppImage
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ConfirmAlert
import com.hayesenterprisesllc.onthaset.ui.components.DateTimeField
import com.hayesenterprisesllc.onthaset.ui.components.FormTextField
import com.hayesenterprisesllc.onthaset.ui.components.SheetHeader
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.events.capitalizedWords
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import com.hayesenterprisesllc.onthaset.util.DateUtils
import com.hayesenterprisesllc.onthaset.util.Intents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

object StripeLinks {
    const val basicRecurring = "https://buy.stripe.com/7sYaEW97Z2ur5YQ6oHdUY01"
    const val basicOneTime = "https://buy.stripe.com/6oUeVcckb2uraf6cN5dUY02"
    const val featuredRecurring = "https://buy.stripe.com/aFa8wO4RJglhcne8wPdUY04"
    const val featuredOneTime = "https://buy.stripe.com/dRm00i3NF3yvfzq8wPdUY03"
    const val premiumRecurring = "https://buy.stripe.com/6oU6oGckbfhd9b2cN5dUY06"
    const val premiumOneTime = "https://buy.stripe.com/cNieVcfwnc51af68wPdUY05"
}

data class AdminEventReport(
    val id: String, val eventID: String, val eventTitle: String, val reportedByUserID: String,
    val reason: String, val additionalNotes: String, val status: String, val createdAt: String?
) {
    companion object {
        fun fromJson(o: JSONObject) = AdminEventReport(
            id = o.optString("id"), eventID = o.optString("event_id"), eventTitle = o.optString("event_title"),
            reportedByUserID = o.optString("reported_by_user_id"), reason = o.optString("reason"),
            additionalNotes = o.optString("additional_notes"), status = o.optString("status"),
            createdAt = o.optStringOrNull("created_at")
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminLockView
// ─────────────────────────────────────────────────────────────────────────────

private const val ADMIN_PIN = "012230"

@Composable
fun AdminLockScreen(onCancel: () -> Unit, onUnlocked: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var shake by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    LaunchedEffect(shake) { if (shake) { delay(600); shake = false } }

    fun check() {
        if (pin == ADMIN_PIN) { pin = ""; onUnlocked() }
        else { shake = true; showError = true; pin = "" }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            SheetHeader(closeLabel = "Cancel", onClose = onCancel)
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
            ) {
                ShieldLogo(80.dp)
                Text("ADMIN ACCESS", color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Enter your admin PIN", color = AppColors.Gray, fontSize = 15.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    repeat(6) { i -> Box(Modifier.size(16.dp).clip(CircleShape).background(if (i < pin.length) AppColors.Yellow else Color.White.copy(alpha = 0.2f))) }
                }
                if (showError) Text("Incorrect PIN", color = AppColors.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                NumPad(
                    onDigit = { d -> if (pin.length < 6) { pin += d; showError = false; if (pin.length == 6) check() } },
                    onDelete = { if (pin.isNotEmpty()) { pin = pin.dropLast(1); showError = false } },
                    onConfirm = { check() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminDashboardView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AdminDashboardScreen(onBack: () -> Unit, onViewEvent: (SupabaseEvent) -> Unit) {
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<List<SupabaseAd>>(emptyList()) }
    var active by remember { mutableStateOf<List<SupabaseAd>>(emptyList()) }
    var paused by remember { mutableStateOf<List<SupabaseAd>>(emptyList()) }
    var expired by remember { mutableStateOf<List<SupabaseAd>>(emptyList()) }
    var events by remember { mutableStateOf<List<SupabaseEvent>>(emptyList()) }
    var reports by remember { mutableStateOf<List<AdminEventReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    var reviewMode by remember { mutableStateOf(Prefs.adminReviewModeOn ?: true) }

    suspend fun loadAds() {
        val today = LocalDate.now().toString()
        // Bulk auto-expiry (idempotent)
        runCatching { Http.restPatch("ads?status=eq.active&paid_until=lt.$today&sponsored=eq.false", JSONObject().put("status", "expired")) }
        val now = Instant.now()
        val ads = SupabaseManager.fetchAds("order=created_at.desc").map { ad ->
            if (ad.status == "active" && ad.sponsored != true && ad.paidUntil != null && ad.paidUntil.isBefore(now)) ad.copy(status = "expired") else ad
        }
        pending = ads.filter { it.status == "pending" || it.status == "approved" }
        active = ads.filter { it.status == "active" }
        paused = ads.filter { it.status == "rejected" || it.status == "inactive" }
        expired = ads.filter { it.status == "expired" }
    }

    suspend fun loadEvents() {
        val cutoff = Http.enc(DateUtils.iso(Instant.now().minus(24, ChronoUnit.HOURS)))
        runCatching { Http.restGet("events?date=lt.$cutoff&select=id,image_url").jsonArray() }.getOrNull()?.let { arr ->
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.optStringOrNull("image_url")?.takeIf { it.isNotEmpty() }?.let { SupabaseManager.deleteStorageFile(it, "event-flyers") }
            }
        }
        runCatching { Http.restDelete("events?date=lt.$cutoff") }
        val now = Http.enc(DateUtils.iso(Instant.now()))
        runCatching { Http.restGet("events?date=gte.$now&order=date.asc").jsonArray() }.getOrNull()?.let { arr ->
            events = (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let { SupabaseEvent.fromJson(it) } }
        }
    }

    suspend fun loadReports() {
        runCatching { Http.restGet("event_reports?status=eq.pending&order=created_at.desc").jsonArray() }.getOrNull()?.let { arr ->
            reports = (0 until arr.length()).mapNotNull { arr.optJSONObject(it)?.let { AdminEventReport.fromJson(it) } }
        }
    }

    suspend fun loadAll() { isLoading = true; loadAds(); loadEvents(); loadReports(); isLoading = false }

    fun updateStatus(ad: SupabaseAd, status: String) = scope.launch {
        ad.id?.let { SupabaseManager.updateAd(it, JSONObject().put("status", status)) }
        loadAds(); SupabaseManager.fetchActiveAds()
    }
    fun approve(ad: SupabaseAd) = updateStatus(ad, if (ad.sponsored == true) "active" else "approved")
    fun reject(ad: SupabaseAd) = updateStatus(ad, "rejected")
    fun delete(ad: SupabaseAd) = scope.launch {
        val id = ad.id ?: return@launch
        ad.imageURL?.takeIf { it.isNotEmpty() }?.let { SupabaseManager.deleteStorageFile(it, "ad-banners") }
        SupabaseManager.deleteAd(id)
        loadAds(); SupabaseManager.fetchActiveAds()
    }

    LaunchedEffect(Unit) { loadAll() }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.Yellow) }
                ShieldLogo(45.dp)
                Spacer(Modifier.width(8.dp))
                Text("ADMIN", color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { scope.launch { loadAll() } }) { Icon(Icons.Filled.Refresh, "Refresh", tint = AppColors.Yellow) }
            }
            Row(Modifier.fillMaxWidth().background(AppColors.Card)) {
                StatCell(pending.size, "PENDING", AppColors.Orange, Modifier.weight(1f))
                StatCell(active.size, "ACTIVE ADS", AppColors.Green, Modifier.weight(1f))
                StatCell(events.size, "EVENTS", AppColors.Yellow, Modifier.weight(1f))
                StatCell(reports.size, "REPORTS", AppColors.Red, Modifier.weight(1f))
                StatCell(expired.size, "EXPIRED", AppColors.Gray, Modifier.weight(1f))
            }
            val tabs = listOf("Pending (${pending.size})", "Active Ads", "Events", "Reports (${reports.size})", "Paused (${paused.size})", "Expired (${expired.size})", "⚙️ Settings")
            ScrollableTabRow(
                selectedTabIndex = tab, containerColor = Color.Black, contentColor = AppColors.Yellow, edgePadding = 8.dp,
                indicator = { pos -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(pos[tab]), color = AppColors.Yellow) }
            ) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t, color = if (tab == i) AppColors.Yellow else AppColors.Gray, fontSize = 13.sp) })
                }
            }

            if (isLoading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.Yellow) }
            else LazyColumn(
                Modifier.fillMaxSize().navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    0 -> if (pending.isEmpty()) item { EmptyState("No pending ads", Icons.Filled.CheckCircle) } else items(pending, key = { it.id ?: it.businessName }) { ad ->
                        AdminAdCard(ad, onApprove = { approve(ad) }, onReject = { reject(ad) }, onDelete = { delete(ad) }, onRefresh = { scope.launch { loadAll() } })
                    }
                    1 -> if (active.isEmpty()) item { EmptyState("No active ads", Icons.Filled.Campaign) } else items(active, key = { it.id ?: it.businessName }) { ad ->
                        AdminAdCard(ad, onApprove = null, onReject = { reject(ad) }, onDelete = { delete(ad) }, onRefresh = { scope.launch { loadAll() } })
                    }
                    2 -> if (events.isEmpty()) item { EmptyState("No events", Icons.Filled.CalendarMonth) } else items(events, key = { it.id ?: it.title }) { e ->
                        AdminEventCard(e) { scope.launch { e.id?.let { SupabaseManager.deleteEvent(it) }; loadEvents() } }
                    }
                    3 -> if (reports.isEmpty()) item { EmptyState("No reports", Icons.Filled.OutlinedFlag) } else items(reports, key = { it.id }) { r ->
                        AdminReportCard(
                            r,
                            onDismiss = { scope.launch { runCatching { Http.restPatch("event_reports?id=eq.${r.id}", JSONObject().put("status", "dismissed")) }; loadReports() } },
                            onDeleteEvent = {
                                scope.launch {
                                    runCatching { Http.restDelete("events?id=eq.${r.eventID}") }
                                    runCatching { Http.restPatch("event_reports?id=eq.${r.id}", JSONObject().put("status", "resolved")) }
                                    loadReports(); loadEvents()
                                }
                            },
                            onViewEvent = onViewEvent
                        )
                    }
                    4 -> if (paused.isEmpty()) item { EmptyState("No paused ads", Icons.Filled.PauseCircle) } else items(paused, key = { it.id ?: it.businessName }) { ad ->
                        AdminAdCard(ad, onApprove = { approve(ad) }, onReject = null, onDelete = { delete(ad) }, onRefresh = { scope.launch { loadAll() } })
                    }
                    5 -> if (expired.isEmpty()) item { EmptyState("No expired ads", Icons.Filled.Timer) } else items(expired, key = { it.id ?: it.businessName }) { ad ->
                        AdminAdCard(ad, onApprove = { approve(ad) }, onReject = { reject(ad) }, onDelete = { delete(ad) }, onRefresh = { scope.launch { loadAll() } })
                    }
                    else -> item { AdminSettings(reviewMode) { reviewMode = it; Prefs.adminReviewModeOn = it } }
                }
            }
        }
    }
}

@Composable
private fun StatCell(count: Int, label: String, color: Color, modifier: Modifier) {
    Column(modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$count", color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = AppColors.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyState(message: String, icon: ImageVector) {
    Column(Modifier.fillMaxWidth().padding(vertical = 60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
        Icon(icon, null, tint = AppColors.Yellow.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
        Text(message, color = AppColors.Gray)
    }
}

@Composable
private fun AdminSettings(reviewMode: Boolean, onChange: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.03f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("APP REVIEW SETTINGS", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(AppColors.Yellow.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 4.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card)
                .border(1.dp, AppColors.Yellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(16.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("App Review Mode", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("When ON: demo accounts can post events without a subscription. Turn OFF after Play Store approval.", color = AppColors.Gray, fontSize = 12.sp)
            }
            Switch(reviewMode, onChange, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Yellow, checkedThumbColor = Color.Black))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(if (reviewMode) AppColors.Green else AppColors.Red))
            Spacer(Modifier.width(8.dp))
            Text(if (reviewMode) "REVIEW MODE ON — Demo accounts can post for free" else "NORMAL MODE — Subscription required to post",
                color = if (reviewMode) AppColors.Green else AppColors.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.03f)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("HOW TO USE", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            listOf(
                "Before submitting to the Play Store: make sure this is ON",
                "After Google approves your app: toggle this OFF",
                "No resubmission needed — changes take effect immediately"
            ).forEachIndexed { i, t ->
                Row {
                    Text("${i + 1}", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(AppColors.Yellow))
                    Spacer(Modifier.width(10.dp))
                    Text(t, color = AppColors.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminReportCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminReportCard(report: AdminEventReport, onDismiss: () -> Unit, onDeleteEvent: () -> Unit, onViewEvent: (SupabaseEvent) -> Unit) {
    val context = LocalContext.current
    var event by remember { mutableStateOf<SupabaseEvent?>(null) }
    var posterEmail by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(report.id) {
        if (report.eventID.isNotEmpty()) {
            val e = runCatching { Http.restGet("events?id=eq.${report.eventID}&limit=1").jsonArray().optJSONObject(0) }.getOrNull()?.let { SupabaseEvent.fromJson(it) }
            event = e
            if (e != null && e.postedByUserID.isNotEmpty()) {
                posterEmail = runCatching {
                    Http.restGet("users?apple_user_id=eq.${Http.enc(e.postedByUserID)}&select=email&limit=1").jsonArray().optJSONObject(0)?.optString("email")
                }.getOrNull().orEmpty()
            }
        }
    }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppColors.Card)
            .border(1.dp, AppColors.Red.copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row {
            Box(Modifier.size(44.dp).clip(CircleShape).background(AppColors.Red.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Flag, null, tint = AppColors.Red)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(report.eventTitle, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, null, tint = AppColors.Orange, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(report.reason, color = AppColors.Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            report.createdAt?.let { DateUtils.parseSupabaseDate(it) }?.let {
                Text(java.time.format.DateTimeFormatter.ofPattern("M/d/yy").format(it.atZone(ZoneId.systemDefault())), color = AppColors.Gray, fontSize = 11.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountCircle, null, tint = AppColors.Gray, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reporter: ${report.reportedByUserID.take(12)}...", color = AppColors.Gray, fontSize = 12.sp)
        }
        if (report.additionalNotes.isNotBlank()) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Card).padding(10.dp)) {
            Text("NOTES", color = AppColors.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(report.additionalNotes, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
        event?.let { e ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, AppColors.Yellow.copy(alpha = 0.2f), RoundedCornerShape(10.dp)).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row {
                    Text("REPORTED EVENT", color = AppColors.Yellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(e.category.replace("_", " ").uppercase(), color = AppColors.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                if (!e.imageURL.isNullOrEmpty()) AppImage(e.imageURL, Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(8.dp)), placeholder = {})
                Text(e.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(e.locationName, color = AppColors.Gray, fontSize = 12.sp)
                if (e.details.isNotEmpty()) Text(e.details, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 3)
                Text("Posted by: ${e.postedByName}", color = AppColors.Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Yellow.copy(alpha = 0.15f))
                        .border(1.dp, AppColors.Yellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).clickable { onViewEvent(e) }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Visibility, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("VIEW FULL EVENT", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        if (posterEmail.isNotEmpty() && posterEmail != "no-email@placeholder.com") {
            SmallAction("CONTACT POSTER", Icons.Filled.Email, AppColors.Blue.copy(alpha = 0.7f), Color.White, Modifier.fillMaxWidth()) {
                Intents.email(
                    context, posterEmail, "Regarding your event: ${report.eventTitle}",
                    "Hi,\n\nYour event \"${report.eventTitle}\" has been reported for: ${report.reason}.\n\nNotes: ${report.additionalNotes}\n\nPlease respond to verify or clarify this report.\n\nOn Tha Set Admin"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallAction("DISMISS", Icons.Filled.CheckCircle, Color.White.copy(alpha = 0.1f), Color.White, Modifier.weight(1f), onDismiss)
            SmallAction("DELETE EVENT", Icons.Filled.Delete, AppColors.Red.copy(alpha = 0.8f), Color.White, Modifier.weight(1f)) { confirmDelete = true }
        }
    }
    if (confirmDelete) ConfirmAlert(
        "Delete This Event?", "This will permanently remove \"${report.eventTitle}\" and mark the report as resolved.", "Delete",
        onConfirm = onDeleteEvent, onDismiss = { confirmDelete = false }
    )
}

@Composable
private fun SmallAction(text: String, icon: ImageVector?, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(8.dp)).background(bg).clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) { Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)) }
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminAdCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminAdCard(ad: SupabaseAd, onApprove: (() -> Unit)?, onReject: (() -> Unit)?, onDelete: (() -> Unit)?, onRefresh: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var paymentStatus by remember(ad.id) { mutableStateOf(ad.paymentStatus ?: "unpaid") }
    var paidUntil by remember(ad.id) { mutableStateOf(ad.paidUntil ?: Instant.now()) }
    var showDate by remember { mutableStateOf(false) }
    var notes by remember(ad.id) { mutableStateOf(ad.notes ?: "") }
    var showNotes by remember { mutableStateOf(false) }
    var savingPayment by remember { mutableStateOf(false) }
    var sponsored by remember(ad.id) { mutableStateOf(ad.sponsored ?: false) }
    var savingSponsored by remember { mutableStateOf(false) }

    val statusColor = when (ad.status) { "active" -> AppColors.Green; "approved" -> AppColors.Blue; "rejected" -> AppColors.Red; else -> AppColors.Orange }
    val planColor = when (ad.plan) { "premium" -> AppColors.Yellow; "featured" -> AppColors.Orange; else -> AppColors.Gray }
    val planBadge = when (ad.plan) { "premium" -> "👑 PREMIUM"; "featured" -> "⭐ FEATURED"; else -> "BASIC" }
    val planPrice = when (ad.plan) { "premium" -> "$49.99"; "featured" -> "$29.99"; else -> "$19.99" }
    val recurringLink = when (ad.plan) { "premium" -> StripeLinks.premiumRecurring; "featured" -> StripeLinks.featuredRecurring; else -> StripeLinks.basicRecurring }
    val oneTimeLink = when (ad.plan) { "premium" -> StripeLinks.premiumOneTime; "featured" -> StripeLinks.featuredOneTime; else -> StripeLinks.basicOneTime }
    fun stripeLink(base: String) = ad.id?.let { "$base?client_reference_id=${it.uppercase()}" } ?: base

    fun sendText(recurring: Boolean) {
        val phone = ad.phone?.takeIf { it.isNotEmpty() } ?: return
        val link = stripeLink(if (recurring) recurringLink else oneTimeLink)
        val billingType = if (recurring) "monthly recurring" else "one-time"
        val emailLine = ad.advertiserEmail?.let { " ⚠️ Important: use $it as your email at checkout so we can activate your ad." } ?: ""
        Intents.sms(context, phone, "Hi ${ad.businessName}! This is On Tha Set 🏍️ Your $planBadge ad has been approved! Complete your $planPrice $billingType payment to go live: $link$emailLine Reply here with any questions!")
    }

    fun sendEmail(recurring: Boolean) {
        val email = ad.advertiserEmail?.takeIf { it.isNotEmpty() } ?: return
        val link = stripeLink(if (recurring) recurringLink else oneTimeLink)
        val billingType = if (recurring) "monthly recurring" else "one-time"
        Intents.email(
            context, email, "On Tha Set — Your Ad Has Been Approved!",
            "Hi ${ad.businessName},\n\nGreat news! Your On Tha Set advertisement has been approved.\n\nPlan: $planBadge\nPrice: $planPrice/mo ($billingType)\n\nComplete your payment here:\n$link\n\n⚠️ Important: when checking out, make sure to enter this exact email address:\n$email\n\nThis is how we verify and activate your ad. Using a different email may delay activation.\n\nRide safe,\nOn Tha Set Team 🏍️"
        )
    }

    val daysLeft: Long? = ad.paidUntil?.let { ChronoUnit.DAYS.between(Instant.now(), it) }

    fun sendRenewal() {
        val pu = ad.paidUntil ?: return
        val d = ChronoUnit.DAYS.between(Instant.now(), pu)
        if (d > 7) return
        val dateStr = DateUtils.abbreviated(pu)
        val isRecurring = (ad.billingPreference ?: "recurring") == "recurring"
        val link = stripeLink(if (isRecurring) recurringLink else oneTimeLink)
        val phone = ad.phone; val email = ad.advertiserEmail
        if (!phone.isNullOrEmpty()) {
            val note = email?.let { " ⚠️ Use $it at checkout." } ?: ""
            val msg = if (d <= 0) "Hi ${ad.businessName}! 🏍️ Your On Tha Set $planBadge ad has expired. Renew now to go back live: $link$note"
            else "Hi ${ad.businessName}! 🏍️ Your On Tha Set $planBadge ad expires in $d day${if (d == 1L) "" else "s"} ($dateStr). Renew here: $link$note"
            Intents.sms(context, phone, msg)
        } else if (!email.isNullOrEmpty()) {
            val subject = if (d <= 0) "On Tha Set — Your Ad Has Expired" else "On Tha Set — Your Ad Expires in $d Day${if (d == 1L) "" else "s"}"
            val warn = "\n\n⚠️ Important: use $email at checkout so we can reactivate your ad immediately."
            val body = if (d <= 0) "Hi ${ad.businessName},\n\nYour On Tha Set $planBadge ad has expired. Renew now to go back live:\n$link$warn\n\nRide safe,\nOn Tha Set Team 🏍️"
            else "Hi ${ad.businessName},\n\nYour On Tha Set $planBadge ad expires in $d day${if (d == 1L) "" else "s"} on $dateStr. Renew now to keep your spot:\n$link$warn\n\nRide safe,\nOn Tha Set Team 🏍️"
            Intents.email(context, email, subject, body)
        }
    }

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card)
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ad.businessName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    if (sponsored) {
                        Spacer(Modifier.width(6.dp))
                        Pill("⭐ SPONSORED", AppColors.Yellow, 8)
                    }
                }
                Text(ad.tagline, color = AppColors.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Pill(ad.status.uppercase(), statusColor, 11)
                Pill(planBadge, planColor, 11)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ad.phone?.takeIf { it.isNotEmpty() }?.let { InfoLine(Icons.Filled.Phone, it) }
            ad.advertiserEmail?.takeIf { it.isNotEmpty() }?.let { InfoLine(Icons.Filled.Email, it) }
            ad.address?.takeIf { it.isNotEmpty() }?.let { InfoLine(Icons.Filled.LocationOn, it) }
            ad.websiteURL?.takeIf { it.isNotEmpty() }?.let { InfoLine(Icons.Filled.Language, it) }
        }
        HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.3f))

        // SPONSORED TOGGLE
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (sponsored) AppColors.Yellow.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f))
                .border(1.dp, if (sponsored) AppColors.Yellow.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(10.dp)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("SPONSORED AD", color = AppColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(if (sponsored) "Never expires — exempt from billing" else "Standard paid ad", color = AppColors.Gray, fontSize = 9.sp)
            }
            if (savingSponsored) CircularProgressIndicator(color = AppColors.Yellow, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Switch(sponsored, { v ->
                sponsored = v
                savingSponsored = true
                scope.launch {
                    ad.id?.let { SupabaseManager.updateAd(it, JSONObject().put("sponsored", v)) }
                    savingSponsored = false
                    onRefresh()
                }
            }, colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Yellow, checkedThumbColor = Color.Black))
        }
        HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.3f))

        // PAYMENT
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PAYMENT", color = AppColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("unpaid", "paid", "expired").forEach { s ->
                    val sel = paymentStatus == s
                    val c = when (s) { "paid" -> AppColors.Green; "expired" -> AppColors.Red; else -> AppColors.Orange }
                    Text(s.uppercase(), color = if (sel) Color.Black else AppColors.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(if (sel) c else Color.White.copy(alpha = 0.08f)).clickable { paymentStatus = s }.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("$planPrice/mo", color = planColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            if (!sponsored && paymentStatus == "paid") {
                Row(Modifier.clickable { showDate = !showDate }, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Paid Until: ${DateUtils.abbreviated(paidUntil)}", color = Color.White, fontSize = 12.sp)
                    Icon(Icons.Filled.ArrowDropDown, null, tint = AppColors.Gray)
                }
                if (showDate) DateTimeField(paidUntil, { paidUntil = it }, includeTime = false)
            }
            Row(Modifier.clickable { showNotes = !showNotes }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Note, null, tint = AppColors.Yellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(notes.ifEmpty { "Add notes..." }, color = if (notes.isEmpty()) AppColors.Gray.copy(alpha = 0.5f) else AppColors.Gray, fontSize = 12.sp, maxLines = 2)
            }
            if (showNotes) FormTextField(notes, { notes = it }, "Notes (payment method, date, etc.)")
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Yellow).clickable(enabled = !savingPayment) {
                    savingPayment = true
                    scope.launch {
                        val body = JSONObject().put("payment_status", paymentStatus).put("notes", notes)
                        if (paymentStatus == "paid") body.put("paid_until", paidUntil.atZone(ZoneId.systemDefault()).toLocalDate().toString())
                        ad.id?.let { SupabaseManager.updateAd(it, body) }
                        savingPayment = false
                        onRefresh()
                    }
                }.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                if (savingPayment) { CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(14.dp), strokeWidth = 2.dp); Spacer(Modifier.width(6.dp)) }
                Text(if (savingPayment) "SAVING..." else "SAVE PAYMENT STATUS", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.3f))

        // SEND PAYMENT LINK
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SEND PAYMENT LINK", color = AppColors.Yellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("💬 TEXT", color = AppColors.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    SmallAction("MONTHLY", null, AppColors.Green, Color.Black, Modifier.fillMaxWidth()) { sendText(true) }
                    SmallAction("ONE-TIME", null, AppColors.Green.copy(alpha = 0.6f), Color.Black, Modifier.fillMaxWidth()) { sendText(false) }
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📧 EMAIL", color = AppColors.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    SmallAction("MONTHLY", null, AppColors.Blue, Color.Black, Modifier.fillMaxWidth()) { sendEmail(true) }
                    SmallAction("ONE-TIME", null, AppColors.Blue.copy(alpha = 0.6f), Color.Black, Modifier.fillMaxWidth()) { sendEmail(false) }
                }
            }
            if (!sponsored && daysLeft != null && daysLeft <= 7) {
                SmallAction(
                    if (daysLeft <= 0) "SEND EXPIRED RENEWAL" else "SEND RENEWAL REMINDER (${daysLeft}d left)",
                    if (daysLeft <= 0) Icons.Filled.Warning else Icons.Filled.NotificationsActive,
                    if (daysLeft <= 0) AppColors.Red.copy(alpha = 0.8f) else AppColors.Orange.copy(alpha = 0.85f), Color.White, Modifier.fillMaxWidth()
                ) { sendRenewal() }
            }
        }

        if (onApprove != null || onReject != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onReject != null) SmallAction(if (ad.status == "active") "DEACTIVATE" else "REJECT", null, AppColors.Red.copy(alpha = 0.7f), Color.White, Modifier.weight(1f), onReject)
                if (onApprove != null) SmallAction(
                    if (sponsored) "APPROVE & GO LIVE" else "APPROVE & SEND LINK",
                    if (sponsored) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.Send,
                    if (sponsored) AppColors.Yellow else AppColors.Green, Color.Black, Modifier.weight(1f)
                ) {
                    onApprove()
                    if (ad.sponsored != true) scope.launch {
                        delay(500)
                        val recurring = (ad.billingPreference ?: "recurring") == "recurring"
                        if (ad.phone != null) sendText(recurring) else sendEmail(recurring)
                    }
                }
            }
            if (onDelete != null) Text(
                "DELETE PERMANENTLY", color = AppColors.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AppColors.Red.copy(alpha = 0.15f))
                    .border(1.dp, AppColors.Red.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).clickable(onClick = onDelete).padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, size: Int) = Text(
    text, color = Color.Black, fontSize = size.sp, fontWeight = FontWeight.Black,
    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)
)

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = AppColors.Gray, fontSize = 12.sp, maxLines = 1)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AdminEventCard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AdminEventCard(event: SupabaseEvent, onDelete: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card)
            .border(1.dp, AppColors.Yellow.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(event.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Posted by ${event.postedByName}", color = AppColors.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Filled.Delete, "Delete", tint = AppColors.Red,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(AppColors.Red.copy(alpha = 0.1f)).clickable { confirm = true }.padding(8.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarMonth, null, tint = AppColors.Yellow, modifier = Modifier.size(13.dp))
                Text(" ${DateUtils.abbreviatedWithTime(event.date)}", color = AppColors.Yellow, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalOffer, null, tint = AppColors.Gray, modifier = Modifier.size(13.dp))
                Text(" ${event.category.capitalizedWords()}", color = AppColors.Gray, fontSize = 12.sp)
            }
        }
        val parts = LocationParts.split(event.locationName)
        if (parts.size >= 3) Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, null, tint = AppColors.Gray, modifier = Modifier.size(13.dp))
            Text(" ${parts[0]} — ${parts[2]}", color = AppColors.Gray, fontSize = 12.sp)
        }
    }
    if (confirm) ConfirmAlert("Delete Event", "Delete \"${event.title}\"? This cannot be undone.", "Delete", onConfirm = onDelete, onDismiss = { confirm = false })
}
