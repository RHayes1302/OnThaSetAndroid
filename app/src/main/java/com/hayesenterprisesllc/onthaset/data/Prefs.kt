package com.hayesenterprisesllc.onthaset.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Replacement for UserDefaults / @AppStorage. */
object Prefs {
    private lateinit var sp: SharedPreferences

    private val _hasAcceptedTerms = MutableStateFlow(false)
    val hasAcceptedTermsFlow: StateFlow<Boolean> = _hasAcceptedTerms

    fun init(context: Context) {
        sp = context.getSharedPreferences("onthaset_prefs", Context.MODE_PRIVATE)
        _hasAcceptedTerms.value = sp.getBoolean("hasAcceptedTerms", false)
    }

    var hasAcceptedTerms: Boolean
        get() = _hasAcceptedTerms.value
        set(value) {
            sp.edit().putBoolean("hasAcceptedTerms", value).apply()
            _hasAcceptedTerms.value = value
        }

    var termsAcceptedDate: Long
        get() = sp.getLong("termsAcceptedDate", 0L)
        set(value) = sp.edit().putLong("termsAcceptedDate", value).apply()

    var hasCompletedProfileSetup: Boolean
        get() = sp.getBoolean("hasCompletedProfileSetup", false)
        set(value) = sp.edit().putBoolean("hasCompletedProfileSetup", value).apply()

    var hasAskedForLocation: Boolean
        get() = sp.getBoolean("HasAskedForLocation", false)
        set(value) = sp.edit().putBoolean("HasAskedForLocation", value).apply()

    /** null when never set (iOS treats "unset" as review mode ON). */
    var adminReviewModeOn: Boolean?
        get() = if (sp.contains("adminReviewModeOn")) sp.getBoolean("adminReviewModeOn", true) else null
        set(value) {
            if (value == null) sp.edit().remove("adminReviewModeOn").apply()
            else sp.edit().putBoolean("adminReviewModeOn", value).apply()
        }

    // Persisted signed-in user (the iOS app kept this in memory only).
    var currentUserId: String?
        get() = sp.getString("currentUserId", null)
        set(value) = sp.edit().putString("currentUserId", value).apply()

    var currentUsername: String?
        get() = sp.getString("currentUsername", null)
        set(value) = sp.edit().putString("currentUsername", value).apply()

    var accessToken: String?
        get() = sp.getString("accessToken", null)
        set(value) = sp.edit().putString("accessToken", value).apply()

    /** Single-post purchases that have been bought but not yet used (consumable). */
    var unusedSinglePosts: Int
        get() = sp.getInt("unusedSinglePosts", 0)
        set(value) = sp.edit().putInt("unusedSinglePosts", value).apply()

    fun getString(key: String, def: String? = null): String? = sp.getString(key, def)
    fun putString(key: String, value: String?) = sp.edit().putString(key, value).apply()
    fun getBoolean(key: String, def: Boolean = false): Boolean = sp.getBoolean(key, def)
    fun putBoolean(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()
}
