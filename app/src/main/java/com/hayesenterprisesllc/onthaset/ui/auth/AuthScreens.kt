package com.hayesenterprisesllc.onthaset.ui.auth

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.Prefs
import com.hayesenterprisesllc.onthaset.ui.components.BlackScreen
import com.hayesenterprisesllc.onthaset.ui.components.FormTextField
import com.hayesenterprisesllc.onthaset.ui.components.InfoAlert
import com.hayesenterprisesllc.onthaset.ui.components.OutlineYellowButton
import com.hayesenterprisesllc.onthaset.ui.components.ShieldLogo
import com.hayesenterprisesllc.onthaset.ui.components.YellowButton
import com.hayesenterprisesllc.onthaset.ui.legal.PrivacyPolicyScreen
import com.hayesenterprisesllc.onthaset.ui.legal.TermsOfServiceScreen
import com.hayesenterprisesllc.onthaset.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// FirstLaunchDisclaimerView (OnboardingView.swift)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FirstLaunchDisclaimerScreen(onAccepted: () -> Unit) {
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var agreedTerms by rememberSaveable { mutableStateOf(false) }
    var agreedLiability by rememberSaveable { mutableStateOf(false) }
    var shake by remember { mutableStateOf(false) }
    val canProceed = agreedTerms && agreedLiability
    val shakeOffset by animateDpAsState(if (shake) (-8).dp else 0.dp, label = "shake")
    LaunchedEffect(shake) { if (shake) { delay(500); shake = false } }

    if (showTerms) { TermsOfServiceScreen { showTerms = false }; return }
    if (showPrivacy) { PrivacyPolicyScreen { showPrivacy = false }; return }

    BlackScreen {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Column(Modifier.padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShieldLogo(100.dp)
                Text("Welcome to On Tha Set", color = AppColors.Yellow, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("The Motorcycle Community Platform", color = AppColors.Gray, fontSize = 12.sp)
            }

            Column(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(AppColors.Card).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.GppMaybe, null, tint = AppColors.Yellow)
                    Spacer(Modifier.width(10.dp))
                    Text("IMPORTANT NOTICE", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Text("Before using On Tha Set, please read and acknowledge the following:", color = AppColors.Gray, fontSize = 15.sp)
                HorizontalDivider(color = AppColors.Yellow.copy(alpha = 0.3f))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(AppColors.Orange.copy(alpha = 0.08f))
                        .border(1.dp, AppColors.Orange.copy(alpha = 0.3f), RoundedCornerShape(10.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = AppColors.Orange, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("EVENT LIABILITY NOTICE", color = AppColors.Orange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "On Tha Set is a technology platform only. We do not organize, host, sponsor, or control any events listed on this platform. Attendance at any event discovered through On Tha Set is entirely at your own risk.",
                        color = AppColors.Gray, fontSize = 12.sp
                    )
                    Text(
                        "On Tha Set, its owners, operators, and affiliates are NOT responsible for any injury, death, property damage, criminal acts, accidents, or harm of any kind that occurs in connection with events posted on this platform.",
                        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp).offset(x = shakeOffset), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CheckRow(agreedTerms, { agreedTerms = !agreedTerms }) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("I agree to the Terms of Service and Privacy Policy", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Read Terms", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { showTerms = true })
                            Text("Read Privacy Policy", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { showPrivacy = true })
                        }
                    }
                }
                CheckRow(agreedLiability, { agreedLiability = !agreedLiability }) {
                    Text(
                        "I understand that On Tha Set is not responsible for any harm, injury, death, or damage that occurs at or in connection with any event posted on this platform. I attend all events at my own risk.",
                        color = Color.White, fontSize = 15.sp
                    )
                }
            }

            if (!canProceed) Text("You must agree to both statements to continue", color = AppColors.Orange, fontSize = 12.sp, textAlign = TextAlign.Center)

            Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 40.dp)) {
                Row(
                    Modifier.fillMaxWidth()
                        .then(if (canProceed) Modifier.shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = AppColors.Yellow, spotColor = AppColors.Yellow) else Modifier)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canProceed) AppColors.Yellow else AppColors.Gray.copy(alpha = 0.4f))
                        .clickable {
                            if (canProceed) {
                                Prefs.termsAcceptedDate = System.currentTimeMillis() / 1000
                                Prefs.hasAcceptedTerms = true
                                onAccepted()
                            } else shake = true
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (canProceed) Icons.Filled.GppGood else Icons.Filled.Lock, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text(if (canProceed) "ENTER ON THA SET" else "AGREE TO CONTINUE", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CheckRow(checked: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (checked) AppColors.Yellow.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f))
            .border(1.dp, if (checked) AppColors.Yellow.copy(alpha = 0.5f) else AppColors.Gray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle).padding(16.dp)
    ) {
        Icon(if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank, null, tint = if (checked) AppColors.Yellow else AppColors.Gray)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GatePage (Gate.swift) — Sign in with Apple replaced by email-first sign in
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GateScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLoading by AuthService.isLoading.collectAsState()
    val authError by AuthService.authError.collectAsState()
    var showEmailAuth by remember { mutableStateOf(false) }
    if (showEmailAuth) { EmailAuthScreen(onDismiss = { showEmailAuth = false }); return }

    BlackScreen {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Spacer(Modifier.height(30.dp))
            ShieldLogo(140.dp)
            Text("SIGN IN TO POST", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text(
                "Log in to manage your 4 monthly events and keep the community updated.",
                color = AppColors.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp)
            )
            Spacer(Modifier.height(10.dp))

            Column(Modifier.padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                if (isLoading) CircularProgressIndicator(color = AppColors.Yellow)
                // SIGN IN WITH GOOGLE (Android). iOS keeps Sign in with Apple.
                Text(
                    "Sign in with Google", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White)
                        .clickable(enabled = !isLoading) {
                            AuthService.setAuthError(null)
                            androidx.browser.customtabs.CustomTabsIntent.Builder().build()
                                .launchUrl(context, android.net.Uri.parse(AuthService.googleSignInUrl()))
                        }
                        .padding(vertical = 15.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f), color = AppColors.Gray.copy(alpha = 0.3f))
                    Text("  or  ", color = AppColors.Gray, fontSize = 12.sp)
                    HorizontalDivider(Modifier.weight(1f), color = AppColors.Gray.copy(alpha = 0.3f))
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f))
                        .border(2.dp, AppColors.Yellow, RoundedCornerShape(12.dp)).clickable { showEmailAuth = true }.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Email, null, tint = AppColors.Yellow)
                    Spacer(Modifier.width(8.dp))
                    Text("Continue with Email", color = AppColors.Yellow, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                authError?.let { Text(it, color = AppColors.Red, fontSize = 12.sp, textAlign = TextAlign.Center) }
                Text(
                    "Explore Without Signing In", color = AppColors.Gray, fontSize = 13.sp, textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { AuthService.createDemoProfile() }
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EmailAuthView
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmailAuthScreen(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val isLoading by AuthService.isLoading.collectAsState()
    val authError by AuthService.authError.collectAsState()
    var signUp by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetSent by remember { mutableStateOf(false) }

    fun handleAuth() {
        val e = email.trim(); val p = password.trim()
        if (e.isEmpty() || p.isEmpty()) { AuthService.setAuthError("Please enter your email and password."); return }
        if (signUp) {
            if (p != confirm) { AuthService.setAuthError("Passwords do not match."); return }
            if (p.length < 6) { AuthService.setAuthError("Password must be at least 6 characters."); return }
        }
        scope.launch {
            val ok = if (signUp) AuthService.signUpWithEmail(e, p) else AuthService.signInWithEmail(e, p)
            if (ok) onDismiss()
        }
    }

    BlackScreen {
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            ShieldLogo(100.dp, Modifier.padding(top = 60.dp))

            Row(Modifier.padding(horizontal = 40.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.05f))) {
                ModeButton("Sign In", !signUp, Modifier.weight(1f)) { signUp = false }
                ModeButton("Sign Up", signUp, Modifier.weight(1f)) { signUp = true }
            }

            Column(Modifier.padding(horizontal = 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Field("Email") {
                    FormTextField(email, { email = it }, "your@email.com", keyboardType = KeyboardType.Email,
                        background = Color.White.copy(alpha = 0.05f), borderColor = AppColors.Yellow.copy(alpha = 0.3f))
                }
                Field("Password") {
                    FormTextField(password, { password = it }, "••••••••", isPassword = !showPassword,
                        background = Color.White.copy(alpha = 0.05f), borderColor = AppColors.Yellow.copy(alpha = 0.3f),
                        trailing = {
                            Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null, tint = AppColors.Gray,
                                modifier = Modifier.clickable { showPassword = !showPassword })
                        })
                }
                if (signUp) Field("Confirm Password") {
                    FormTextField(confirm, { confirm = it }, "••••••••", isPassword = true,
                        background = Color.White.copy(alpha = 0.05f), borderColor = AppColors.Yellow.copy(alpha = 0.3f))
                }
            }

            if (!signUp) Text("Forgot Password?", color = AppColors.Yellow.copy(alpha = 0.8f), fontSize = 13.sp,
                modifier = Modifier.clickable { resetEmail = email; showReset = true })

            Box(Modifier.padding(horizontal = 40.dp)) {
                YellowButton(if (signUp) "CREATE ACCOUNT" else "SIGN IN", ::handleAuth, loading = isLoading, corner = 12.dp, verticalPadding = 18.dp)
            }

            authError?.let { Text(it, color = AppColors.Red, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp)) }

            Text(
                "GO BACK", color = AppColors.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 40.dp).border(1.dp, AppColors.Yellow).clickable(onClick = onDismiss).padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }

    if (showReset) AlertDialog(
        onDismissRequest = { showReset = false },
        title = { Text("Reset Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter your email and we'll send a reset link.")
                FormTextField(resetEmail, { resetEmail = it }, "Email address", keyboardType = KeyboardType.Email)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                showReset = false
                scope.launch { if (AuthService.resetPassword(resetEmail.trim())) resetSent = true }
            }) { Text("Send Reset Link", color = AppColors.Yellow) }
        },
        dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel", color = AppColors.Gray) } },
        containerColor = Color(0xFF1C1C1E), titleContentColor = Color.White, textContentColor = Color(0xFFD1D1D6)
    )
    if (resetSent) InfoAlert("Email Sent", "Check your inbox for a password reset link.", onDismiss = { resetSent = false })
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = AppColors.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun ModeButton(title: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        title, color = if (selected) Color.Black else AppColors.Gray, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) AppColors.Yellow else Color.Transparent)
            .clickable(onClick = onClick).padding(vertical = 12.dp)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile loading spinner (ContentRootView while syncing)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ProfileLoadingScreen() {
    BlackScreen {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
            ShieldLogo(80.dp)
            CircularProgressIndicator(color = AppColors.Yellow)
            Text("Loading your profile...", color = AppColors.Gray, fontSize = 15.sp)
        }
    }
}