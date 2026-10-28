package com.hayesenterprisesllc.onthaset.ui.payment

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.billing.BillingManager
import com.hayesenterprisesllc.onthaset.billing.PurchaseOutcome
import com.hayesenterprisesllc.onthaset.billing.StoreProduct
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.Products
import com.hayesenterprisesllc.onthaset.data.SupabaseManager
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.LoadingOverlay
import com.hayesenterprisesllc.onthaset.ui.components.OutlineYellowButton
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** After a subscription purchase: mark the local profile as subscribed (same as iOS). */
private fun linkSubscriptionToProfile() {
    val id = AuthService.currentUserId ?: return
    LocalStore.updateProfile(id) { it.copy(hasActiveSubscription = true, subscriptionStartDate = Instant.now()) }
}

// ─────────────────────────────────────────────────────────────────────────────
// PaymentSelectionView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PaymentSelectionScreen(onClose: () -> Unit, onPurchased: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val products by BillingManager.products.collectAsState()
    val isLoading by BillingManager.isLoading.collectAsState()
    val profiles by LocalStore.profiles.collectAsState()
    val profile = LocalStore.profileFor(AuthService.currentUserId)
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { if (products.isEmpty()) BillingManager.loadProducts() }

    fun buy(p: StoreProduct) {
        val activity = context.findActivity() ?: return
        scope.launch {
            when (val r = BillingManager.purchase(activity, p)) {
                PurchaseOutcome.Success -> {
                    if (p.id == Products.MONTHLY_SUBSCRIPTION) linkSubscriptionToProfile()
                    onPurchased()
                }
                is PurchaseOutcome.Failed -> error = r.message
                else -> {}
            }
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Text("Close", color = AppColors.Yellow, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 25.dp, vertical = 20.dp).clickable(onClick = onClose))
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                ShieldLogo(140.dp, Modifier.padding(bottom = 40.dp))
                when {
                    isLoading && products.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        CircularProgressIndicator(color = AppColors.Yellow)
                        Text("Loading...", color = AppColors.Gray, fontSize = 17.sp)
                    }
                    products.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)) {
                        Text("UNABLE TO LOAD", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("Could not connect to payment services. Please check your connection and try again.", color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                        Box(Modifier.padding(horizontal = 40.dp)) { YellowButton("RETRY", { scope.launch { BillingManager.loadProducts() } }, corner = 12.dp, verticalPadding = 18.dp) }
                    }
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)) {
                        Text("CHOOSE YOUR PLAN", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Text("Select how you'd like to post events to the community.", color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
                        Column(Modifier.padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            BillingManager.subscriptionProduct?.let { sub ->
                                Column(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Yellow).clickable(enabled = !isLoading) { buy(sub) }.padding(vertical = 25.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                        Text(" BEST VALUE", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Monthly Subscription", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("${sub.displayPrice}/month", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Text("4 posts per month • Cancel anytime", color = Color.Black.copy(alpha = 0.7f), fontSize = 12.sp)
                                    if (profile != null && profile.displayName.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.AccountCircle, null, tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                                        Text(" Links to: ${profile.displayName}", color = Color.Black.copy(alpha = 0.6f), fontSize = 12.sp)
                                    }
                                }
                            }
                            Text("or", color = AppColors.Gray, fontSize = 15.sp)
                            BillingManager.singlePostProduct?.let { single ->
                                Column(
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card)
                                        .border(2.dp, AppColors.Yellow, RoundedCornerShape(12.dp)).clickable(enabled = !isLoading) { buy(single) }.padding(vertical = 25.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Single Event Post", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(single.displayPrice, color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Text("One-time payment", color = AppColors.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            Box(Modifier.padding(horizontal = 40.dp).padding(bottom = 40.dp, top = 10.dp)) { OutlineYellowButton("GO BACK", onClose) }
        }
        if (isLoading && products.isNotEmpty()) LoadingOverlay("Processing...")
    }
    error?.let { InfoAlert("Purchase Failed", it, onDismiss = { error = null }) }
}

// ─────────────────────────────────────────────────────────────────────────────
// ExtraPostPurchaseView — shown when a subscriber used all 4 monthly posts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ExtraPostPurchaseScreen(onClose: () -> Unit, onPurchased: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var purchasing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val resetDate = remember {
        LocalDate.now().plusMonths(1).withDayOfMonth(1).format(DateTimeFormatter.ofPattern("MMMM", Locale.US)) + " 1st"
    }

    fun buySingle() {
        val activity = context.findActivity() ?: return
        purchasing = true
        scope.launch {
            BillingManager.loadProducts()
            val p = BillingManager.singlePostProduct
            if (p == null) error = "Product not found. Please check your connection and try again."
            else when (val r = BillingManager.purchase(activity, p)) {
                PurchaseOutcome.Success -> onPurchased()
                is PurchaseOutcome.Failed -> error = r.message
                else -> {}
            }
            purchasing = false
        }
    }

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onClose, title = "Get More Posts", backIcon = Icons.Filled.Close)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                Column(Modifier.padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShieldLogo(80.dp)
                    Text("POST LIMIT REACHED", color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("You've used all 4 posts this month", color = AppColors.Gray, fontSize = 15.sp)
                }
                Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EventRepeat, null, tint = AppColors.Yellow)
                    Spacer(Modifier.width(15.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("FREE POSTS RESET", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Your 4 free posts reset on $resetDate", color = Color.White, fontSize = 15.sp)
                        Text("Or purchase additional posts below", color = AppColors.Gray, fontSize = 12.sp)
                    }
                }
                Column(Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Text("GET MORE POSTS NOW", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, AppColors.Yellow.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).clickable(enabled = !purchasing) { buySingle() }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Single Post", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Post one event right now", color = AppColors.Gray, fontSize = 12.sp)
                        }
                        if (purchasing) CircularProgressIndicator(color = AppColors.Yellow, modifier = Modifier.padding(horizontal = 20.dp).size(24.dp))
                        else PriceTag(BillingManager.singlePostProduct?.displayPrice ?: "$0.99", "one time", AppColors.Yellow)
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, AppColors.Green.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Monthly Subscription", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                Text("BEST VALUE", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black,
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(AppColors.Green).padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text("4 posts every month — auto-renews", color = AppColors.Gray, fontSize = 12.sp)
                            Text("Never run out of posts again", color = AppColors.Green, fontSize = 11.sp)
                        }
                        PriceTag(BillingManager.subscriptionProduct?.displayPrice ?: "$2.99", "per month", AppColors.Green)
                    }
                    Text("Subscriptions managed through your Google Play account", color = AppColors.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
                Text("I'll wait until $resetDate", color = AppColors.Gray, fontSize = 15.sp, textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(bottom = 40.dp).clickable(onClick = onClose))
            }
        }
    }
    error?.let { InfoAlert("Purchase Error", it, onDismiss = { error = null }) }
}

@Composable
private fun PriceTag(price: String, sub: String, bg: Color) {
    Column(Modifier.clip(RoundedCornerShape(10.dp)).background(bg).padding(horizontal = 16.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(price, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = Color.Black.copy(alpha = 0.7f), fontSize = 11.sp)
    }
}
