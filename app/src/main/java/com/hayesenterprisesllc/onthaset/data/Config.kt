package com.hayesenterprisesllc.onthaset.data

/**
 * Supabase project configuration (ported from SupabaseClient.swift / SupabaseManager.swift).
 */
object SupabaseConfig {
    const val PROJECT_URL = "https://zlvqhowflvgyaythfzkx.supabase.co"

    /** Legacy anon JWT key — used by every REST call in the iOS app. */
    const val ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpsdnFob3dmbHZneWF5dGhmemt4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ1NDQ4OTEsImV4cCI6MjA5MDEyMDg5MX0.mtw-bDXWk0U513symOwPR7AQuKH01Kykt55SEIaBtzI"

    /** Publishable key — used by the iOS Supabase SDK client (auth). */
    const val PUBLISHABLE_KEY = "sb_publishable_1zuluNt1jdJ2H5VCyFwy-g_dsGnyo3z"
}

/**
 * Global configuration — ported from AppConfig.swift.
 * Review mode is toggled from the Admin Dashboard (5-tap shield).
 */
object AppConfig {
    const val DEMO_USER_ID = "demo-reviewer-user"

    val isAppReviewMode: Boolean
        get() = Prefs.adminReviewModeOn ?: true

    /** Owner account — ALWAYS has free posting access regardless of toggle.
     *  NOTE: the first entry is the owner's Sign in with Apple ID from iOS. On Android the
     *  owner signs in with email, so add that Supabase auth user id (UUID) here too. */
    val ownerUserIDs: List<String> = listOf(
        "000366.ce60803dfe5742d2888b18ee5a388623.0248"
    )

    val demoUserIDs: List<String> = listOf(
        DEMO_USER_ID,
        "55f91a3c-2e28-4d7e-8106-1b2ac2d86681"
    )

    fun isOwner(userID: String?): Boolean {
        val id = userID ?: return false
        return ownerUserIDs.contains(id.lowercase())
    }

    fun isDemoUser(userID: String?): Boolean {
        if (!isAppReviewMode) return false
        val id = userID ?: return false
        return demoUserIDs.contains(id.lowercase())
    }
}

object Products {
    const val SINGLE_POST = "com.onthaset.singlepost"
    const val MONTHLY_SUBSCRIPTION = "com.hayesenterprisellc.onthaset.membership.monthly"
    const val CONTACT_EMAIL = "contact.onthaset@gmail.com"
}
