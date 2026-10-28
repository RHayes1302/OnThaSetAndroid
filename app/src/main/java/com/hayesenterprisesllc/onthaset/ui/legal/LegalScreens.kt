package com.hayesenterprisesllc.onthaset.ui.legal

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.BuildConfig
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.TopBar
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors

@Composable
private fun LegalPage(title: String, sections: List<Pair<String, String>>, onClose: () -> Unit) {
    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            TopBar(onBack = onClose, title = title, backIcon = Icons.Filled.Close)
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShieldLogo(60.dp)
                    Text(title, color = AppColors.Yellow, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(LEGAL_LAST_UPDATED, color = AppColors.Gray, fontSize = 12.sp)
                }
                sections.forEach { (t, c) ->
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.04f)).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(t, color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(c, color = AppColors.Gray, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onClose: () -> Unit) = LegalPage("Privacy Policy", privacySections, onClose)

@Composable
fun TermsOfServiceScreen(onClose: () -> Unit) = LegalPage("Terms of Service", termsSections, onClose)

// ─────────────────────────────────────────────────────────────────────────────
// AboutView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val user by AuthService.currentUser.collectAsState()
    val profiles by LocalStore.profiles.collectAsState()
    val profile = LocalStore.profileFor(user?.id)

    BlackScreen {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.Yellow)
                }
                ShieldLogo(60.dp, Modifier.align(Alignment.Center))
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(30.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Tag("WE ACCEPT")
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AppColors.Card).padding(16.dp)) {
                        PaymentIcon(Icons.Filled.AccountBalanceWallet, "Google Pay", Color.White, Modifier.weight(1f))
                        PaymentIcon(null, "Venmo", AppColors.Blue, Modifier.weight(1f), letter = "V")
                        PaymentIcon(Icons.Filled.AttachMoney, "Cash App", AppColors.Green, Modifier.weight(1f))
                    }
                }

                if (profile != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Tag("YOUR ACCOUNT")
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Card).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Logged in: ${profile.email}", color = Color.White, fontSize = 15.sp)
                            Row(Modifier.padding(top = 5.dp)) {
                                Text("Monthly Sets Posted", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                Text("${profile.postsThisMonth} / 4", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { (profile.postsThisMonth / 4f).coerceIn(0f, 1f) },
                                color = AppColors.Yellow, trackColor = AppColors.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth().height(6.dp)
                            )
                        }
                        Text(
                            "SIGN OUT", color = AppColors.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Red, RoundedCornerShape(5.dp))
                                .clickable { AuthService.logout(); onBack() }.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Tag("THE MISSION")
                    Text(
                        "On-Tha-Set was developed by and for people who enjoy riding and spreading love throughout the motorcycle community. Our focus was to create a platform designed to bridge the gap between event organizers and local or traveling motorcycle enthusiasts. We believe in real-time connection to promote and increase your event's profits and attendance for a fraction of the cost compared to the traditional flyers of years prior.",
                        color = Color.White, fontSize = 17.sp, lineHeight = 24.sp
                    )
                }
                HorizontalDivider(color = AppColors.Gray.copy(alpha = 0.3f))
                Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    Tag("HOW TO USE")
                    FeatureRow(Icons.Filled.LocalFireDepartment, "Discover", "Check for 'The Set' events while on the road, traveling, or looking for local MC-friendly establishments.")
                    FeatureRow(Icons.Filled.PinDrop, "Locate", "Find events within 50 miles of your current location.")
                    FeatureRow(Icons.Filled.AddBox, "Share", "Post your own events to the community.")
                }
                Spacer(Modifier.height(50.dp))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Version ${BuildConfig.VERSION_NAME}", color = AppColors.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("© 2026 ON-THA-SET", color = AppColors.Gray.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String) = Text(
    text, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold,
    modifier = Modifier.background(AppColors.Yellow).padding(horizontal = 8.dp, vertical = 2.dp)
)

@Composable
private fun PaymentIcon(icon: ImageVector?, label: String, color: Color, modifier: Modifier, letter: String? = null) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.height(35.dp), contentAlignment = Alignment.Center) {
            if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(30.dp))
            else Text(letter ?: "", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp,
                modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape).background(color).padding(horizontal = 9.dp, vertical = 2.dp))
        }
        Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
        Icon(icon, null, tint = AppColors.Yellow, modifier = Modifier.width(30.dp).size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = AppColors.Gray, fontSize = 12.sp)
        }
    }
}
