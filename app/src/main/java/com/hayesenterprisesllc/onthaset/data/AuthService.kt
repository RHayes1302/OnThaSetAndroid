package com.hayesenterprisesllc.onthaset.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Port of AuthService (ObserableObject.swift) + profile sync from Gate.swift.
 *
 * Sign in with Apple is iOS-only, so Android uses Supabase email/password auth
 * (which the iOS app already supported) plus the "Explore Without Signing In" demo mode.
 */
object AuthService {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    val isLoggedIn: Boolean get() = _currentUser.value != null
    val currentUserId: String? get() = _currentUser.value?.id

    fun restoreSession() {
        val id = Prefs.currentUserId ?: return
        _currentUser.value = AppUser(id = id, username = Prefs.currentUsername ?: "User")
    }

    fun setAuthError(message: String?) { _authError.value = message }

    private fun setUser(id: String, email: String) {
        val username = email.substringBefore("@").ifEmpty { "User" }
        _currentUser.value = AppUser(id = id, username = username)
        Prefs.currentUserId = id
        Prefs.currentUsername = username
    }

    /** Equivalent of loginWithApple(userID:email:) — used for demo mode. */
    fun loginLocal(userID: String, email: String) = setUser(userID, email)

    private fun parseAuthError(res: HttpResult): String {
        val o = res.jsonObject()
        return o.optStringOrNull("msg")
            ?: o.optStringOrNull("error_description")
            ?: o.optStringOrNull("message")
            ?: o.optStringOrNull("error")
            ?: "Authentication failed (${res.code})"
    }

    /** iOS stored `user.id.uuidString`, which is UPPERCASE — keep ids identical across platforms. */
    private fun userIdFrom(o: JSONObject): String? =
        (o.optJSONObject("user")?.optStringOrNull("id") ?: o.optStringOrNull("id"))?.uppercase()

    // ── Email Sign Up (brand new user — show setup) ──────────────────────────

    suspend fun signUpWithEmail(email: String, password: String): Boolean {
        _isLoading.value = true; _authError.value = null
        return try {
            val res = Http.authPost("signup", JSONObject().put("email", email).put("password", password))
            if (!res.isSuccess) throw Exception(parseAuthError(res))
            val o = res.jsonObject()
            val userID = userIdFrom(o) ?: throw Exception("Sign up failed — no user returned")
            o.optStringOrNull("access_token")?.let { Prefs.accessToken = it }

            SupabaseManager.ensureUserRow(userID, email, email.substringBefore("@").ifEmpty { "Rider" })
            createLocalProfile(userID, email, skipSetup = false)
            setUser(userID, email)
            Http.log("✅ Email sign up successful: $userID")
            true
        } catch (e: Exception) {
            _authError.value = e.message
            Http.log("❌ Email sign up error: $e")
            false
        } finally {
            _isLoading.value = false
        }
    }

    // ── Email Sign In (returning user — always skip setup) ───────────────────

    suspend fun signInWithEmail(email: String, password: String): Boolean {
        _isLoading.value = true; _authError.value = null
        Prefs.hasCompletedProfileSetup = true
        return try {
            val res = Http.authPost("token?grant_type=password", JSONObject().put("email", email).put("password", password))
            if (!res.isSuccess) throw Exception(parseAuthError(res))
            val o = res.jsonObject()
            val userID = userIdFrom(o) ?: throw Exception("Sign in failed — no user returned")
            o.optStringOrNull("access_token")?.let { Prefs.accessToken = it }

            syncOrCreateProfile(userID, email)
            setUser(userID, email)
            Http.log("✅ Email sign in successful")
            true
        } catch (e: Exception) {
            Prefs.hasCompletedProfileSetup = false
            _authError.value = e.message
            Http.log("❌ Email sign in error: $e")
            false
        } finally {
            _isLoading.value = false
        }
    }

    // ── Profile sync ─────────────────────────────────────────────────────────

    private suspend fun syncOrCreateProfile(userID: String, email: String) {
        val data = SupabaseManager.fetchUserRowByEmail(email)
            ?: SupabaseManager.fetchUserRow(userID.lowercase())

        var profile = LocalStore.profileFor(userID) ?: UserProfile(appleUserID = userID, email = email)
        if (data != null) profile = applyRemoteFields(profile, data, email)
        profile = profile.copy(hasCompletedSetup = true)
        Prefs.hasCompletedProfileSetup = true
        LocalStore.upsertProfile(profile)
        Http.log("✅ Profile synced with images: ${profile.displayName}")
    }

    /**
     * Copies every column from a Supabase `users` row onto the local profile and downloads
     * profile/background images so they work offline (same as the iOS sync).
     */
    suspend fun applyRemoteFields(p: UserProfile, data: JSONObject, fallbackEmail: String): UserProfile {
        var profile = p.copy(
            email = data.optStringOrNull("email") ?: fallbackEmail,
            displayName = data.optStringOrNull("display_name") ?: p.displayName,
            bio = data.optStringOrNull("bio") ?: "",
            hometown = data.optStringOrNull("hometown") ?: "",
            club = data.optStringOrNull("club") ?: "",
            favoriteRide = data.optStringOrNull("favorite_ride") ?: "",
            ridingSince = data.optStringOrNull("riding_since") ?: "",
            preferredRideType = data.optStringOrNull("preferred_ride_type") ?: "",
            favoriteRoute = data.optStringOrNull("favorite_route") ?: "",
            instagramHandle = data.optStringOrNull("instagram_handle") ?: "",
            tiktokHandle = data.optStringOrNull("tiktok_handle") ?: "",
            youtubeChannel = data.optStringOrNull("youtube_channel") ?: "",
            facebookHandle = data.optStringOrNull("facebook_handle") ?: "",
            hasActiveSubscription = data.optFlexibleBool("has_subscription")
        )
        data.optStringOrNull("profile_image_url")?.takeIf { it.isNotEmpty() }?.let { url ->
            profile = profile.copy(profileImageURL = url)
            Http.getBytes(url)?.let { bytes ->
                profile = profile.copy(profileImagePath = LocalStore.saveImageBytes(bytes, "profile-${profile.appleUserID.hashCode()}.jpg"))
            }
        }
        data.optStringOrNull("background_image_url")?.takeIf { it.isNotEmpty() }?.let { url ->
            profile = profile.copy(backgroundImageURL = url)
            Http.getBytes(url)?.let { bytes ->
                profile = profile.copy(backgroundImagePath = LocalStore.saveImageBytes(bytes, "background-${profile.appleUserID.hashCode()}.jpg"))
            }
        }
        return profile
    }

    private fun createLocalProfile(userID: String, email: String, skipSetup: Boolean) {
        val existing = LocalStore.profileFor(userID)
        if (existing == null) {
            LocalStore.upsertProfile(UserProfile(appleUserID = userID, email = email, hasCompletedSetup = skipSetup))
            Prefs.hasCompletedProfileSetup = skipSetup
        } else if (skipSetup) {
            LocalStore.upsertProfile(existing.copy(hasCompletedSetup = true))
            Prefs.hasCompletedProfileSetup = true
        }
    }

    // ── Sign in with Apple (via Supabase OAuth in the browser) ───────────────

    const val OAUTH_REDIRECT = "onthaset://auth-callback"

    /** URL that starts Apple sign-in through Supabase; it redirects back to [OAUTH_REDIRECT]. */
    fun appleSignInUrl(): String =
        "${SupabaseConfig.PROJECT_URL}/auth/v1/authorize?provider=apple&redirect_to=${Http.enc(OAUTH_REDIRECT)}&scopes=${Http.enc("name email")}"

    /**
     * Handles onthaset://auth-callback#access_token=…  (or ?error_description=…).
     * The account id used everywhere is Apple's stable user id (the token `sub`), which is exactly
     * what the iOS app stores as `apple_user_id` — so the same Apple account maps to the same
     * profile, events, photos and builds on both platforms.
     */
    suspend fun completeOAuth(callback: android.net.Uri): Boolean {
        _isLoading.value = true; _authError.value = null
        try {
            val params = (callback.fragment ?: callback.query ?: "").split("&").mapNotNull {
                val i = it.indexOf('='); if (i <= 0) null else it.substring(0, i) to android.net.Uri.decode(it.substring(i + 1))
            }.toMap()
            params["error_description"]?.let { throw Exception(it.replace('+', ' ')) }
            val token = params["access_token"] ?: throw Exception("Sign in was cancelled.")
            Prefs.accessToken = token

            val res = Http.authGet("user", token)
            if (!res.isSuccess) throw Exception(parseAuthError(res))
            val user = res.jsonObject()
            val identities = user.optJSONArray("identities")
            var appleSub: String? = null
            var name = ""
            if (identities != null) for (i in 0 until identities.length()) {
                val idn = identities.optJSONObject(i) ?: continue
                if (idn.optString("provider") == "apple") {
                    val data = idn.optJSONObject("identity_data")
                    appleSub = data?.optStringOrNull("sub") ?: idn.optStringOrNull("id")
                    name = data?.optStringOrNull("full_name") ?: data?.optStringOrNull("name") ?: ""
                }
            }
            val meta = user.optJSONObject("user_metadata")
            if (appleSub == null) appleSub = meta?.optStringOrNull("sub") ?: meta?.optStringOrNull("provider_id")
            if (name.isEmpty()) name = meta?.optStringOrNull("full_name") ?: meta?.optStringOrNull("name") ?: ""
            val userID = appleSub ?: user.optString("id").uppercase()
            val email = user.optStringOrNull("email") ?: meta?.optStringOrNull("email") ?: "no-email@placeholder.com"

            // Same steps as Gate.swift: save user row, then sync the full profile.
            SupabaseManager.ensureUserRow(userID, email, name)
            val row = SupabaseManager.fetchUserRow(userID)
            var profile = LocalStore.profileFor(userID) ?: UserProfile(appleUserID = userID, email = email, displayName = name)
            if (row != null) {
                val returning = row.optString("display_name").trim().isNotEmpty()
                profile = applyRemoteFields(profile, row, email).copy(hasCompletedSetup = returning || profile.hasCompletedSetup)
                Prefs.hasCompletedProfileSetup = profile.hasCompletedSetup
            }
            LocalStore.upsertProfile(profile)
            setUser(userID, email)
            Http.log("✅ Apple sign in complete — $userID")
            return true
        } catch (e: Exception) {
            _authError.value = e.message
            Http.log("❌ Apple sign in error: $e")
            return false
        } finally {
            _isLoading.value = false
        }
    }

    // ── Demo / Explore Without Signing In (Gate.swift) ───────────────────────

    fun createDemoProfile() {
        val demoUserID = AppConfig.DEMO_USER_ID
        val demoEmail = "reviewer@onthaset.app"
        val existing = LocalStore.profileFor(demoUserID)
        if (existing == null) {
            LocalStore.upsertProfile(
                UserProfile(appleUserID = demoUserID, email = demoEmail, displayName = "Guest", hasCompletedSetup = true)
            )
        } else {
            LocalStore.upsertProfile(existing.copy(hasCompletedSetup = true))
        }
        loginLocal(demoUserID, demoEmail)
    }

    // ── Password Reset ───────────────────────────────────────────────────────

    suspend fun resetPassword(email: String): Boolean = try {
        val res = Http.authPost("recover", JSONObject().put("email", email))
        if (!res.isSuccess) throw Exception(parseAuthError(res))
        true
    } catch (e: Exception) {
        _authError.value = e.message
        false
    }

    // ── Logout ───────────────────────────────────────────────────────────────

    fun logout() {
        val token = Prefs.accessToken
        if (token != null) scope.launch { runCatching { Http.authPost("logout", JSONObject(), bearer = token) } }
        Prefs.accessToken = null
        Prefs.hasCompletedProfileSetup = false
        Prefs.currentUserId = null
        Prefs.currentUsername = null
        _currentUser.value = null
    }
}
