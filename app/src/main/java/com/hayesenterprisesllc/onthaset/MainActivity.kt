package com.hayesenterprisesllc.onthaset

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.Prefs
import com.hayesenterprisesllc.onthaset.data.UserProfile
import com.hayesenterprisesllc.onthaset.location.LocationManager
import com.hayesenterprisesllc.onthaset.ui.auth.FirstLaunchDisclaimerScreen
import com.hayesenterprisesllc.onthaset.ui.auth.GateScreen
import com.hayesenterprisesllc.onthaset.ui.auth.ProfileLoadingScreen
import com.hayesenterprisesllc.onthaset.ui.navigation.AppNavHost
import com.hayesenterprisesllc.onthaset.ui.profile.WelcomeSetupScreen
import com.hayesenterprisesllc.onthaset.ui.theme.OnThaSetTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        LocationManager.onPermissionResult(result.values.any { it })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )
        super.onCreate(savedInstanceState)

        LocationManager.permissionRequester = {
            Prefs.hasAskedForLocation = true
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        setContent {
            OnThaSetTheme { ContentRoot() }
        }
        handleAuthCallback(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleAuthCallback(intent)
    }

    /** onthaset://auth-callback#access_token=… from Sign in with Apple. */
    private fun handleAuthCallback(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "onthaset" || uri.host != "auth-callback") return
        intent.data = null
        lifecycleScope.launch { AuthService.completeOAuth(uri) }
    }

    override fun onResume() {
        super.onResume()
        LocationManager.refreshStatus()
    }

    override fun onDestroy() {
        LocationManager.permissionRequester = null
        super.onDestroy()
    }
}

/** Port of ContentRootView: disclaimer → sign-in gate → profile setup → main app. */
@Composable
private fun ContentRoot() {
    val accepted by Prefs.hasAcceptedTermsFlow.collectAsState()
    val user by AuthService.currentUser.collectAsState()
    val profiles by LocalStore.profiles.collectAsState()

    val u = user
    when {
        !accepted -> FirstLaunchDisclaimerScreen(onAccepted = {})
        u == null -> GateScreen()
        else -> {
            val profile = profiles.firstOrNull { it.appleUserID == u.id }
                ?: profiles.firstOrNull { it.appleUserID.equals(u.id, ignoreCase = true) }
            when {
                profile == null -> {
                    ProfileLoadingScreen()
                    // Safety net: if no local profile appears (e.g. restored session), create one.
                    LaunchedEffect(u.id) {
                        delay(3000)
                        if (LocalStore.profileFor(u.id) == null) {
                            LocalStore.upsertProfile(UserProfile(appleUserID = u.id, email = "", hasCompletedSetup = Prefs.hasCompletedProfileSetup))
                        }
                    }
                }
                !profile.hasCompletedSetup -> WelcomeSetupScreen(profile) {
                    LocalStore.updateProfile(profile.appleUserID) { it.copy(hasCompletedSetup = true) }
                }
                else -> AppNavHost()
            }
        }
    }
}
