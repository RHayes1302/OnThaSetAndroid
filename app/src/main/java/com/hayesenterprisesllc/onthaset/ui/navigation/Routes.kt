package com.hayesenterprisesllc.onthaset.ui.navigation

import android.net.Uri
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent

/** Route names for the NavHost (each iOS sheet / NavigationLink destination). */
object Routes {
    const val HOME = "home"
    const val EVENTS = "events?mode={mode}"
    fun events(mode: String = "list") = "events?mode=$mode"
    const val EVENT_DETAIL = "eventDetail"
    const val EVENT_SHARE = "eventShare"
    const val NEARBY = "nearby"
    const val NATIONAL = "national"
    const val WEATHER = "weather"
    const val WEATHER_COORDS = "weatherCoords/{lat}/{lng}/{name}"
    fun weatherCoords(lat: Double, lng: Double, name: String) = "weatherCoords/$lat/$lng/${Uri.encode(name.ifEmpty { "Event" })}"
    const val WEATHER_CITY = "weatherCity/{city}"
    fun weatherCity(city: String) = "weatherCity/${Uri.encode(city.ifEmpty { "Unknown" })}"
    const val POSTED_BY = "postedBy/{userId}/{name}"
    fun postedBy(userId: String, name: String) = "postedBy/${Uri.encode(userId.ifEmpty { "-" })}/${Uri.encode(name.ifEmpty { "-" })}"
    const val REPORT = "report/{eventId}/{title}"
    fun report(eventId: String, title: String) = "report/${Uri.encode(eventId.ifEmpty { "-" })}/${Uri.encode(title.ifEmpty { "-" })}"

    const val ACCOUNT = "account"
    const val EDIT_PROFILE = "editProfile"
    const val SETTINGS = "settings"
    const val POST_EVENT = "postEvent"
    const val UPLOAD_PHOTO = "uploadPhoto"
    const val UPLOAD_BIKE = "uploadBike"
    const val PAYMENT = "payment"
    const val EXTRA_POST = "extraPost"
    const val WELCOME_SETUP = "welcomeSetup"
    const val LOCAL_EVENT_DETAIL = "localEvent/{id}"
    fun localEvent(id: String) = "localEvent/$id"
    const val LOCAL_EVENT_EDIT = "localEventEdit/{id}"
    fun localEventEdit(id: String) = "localEventEdit/$id"
    const val LOCAL_EVENT_SHARE = "localEventShare/{id}"
    fun localEventShare(id: String) = "localEventShare/$id"

    const val ADVERTISE = "advertise"
    const val AD_DETAIL = "adDetail"
    const val AD_PIN = "adPin"
    const val AD_EDIT = "adEdit"

    const val ADMIN_LOCK = "adminLock"
    const val ADMIN = "admin"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
}

/**
 * Objects handed between screens (the iOS app passed structs directly to destination views).
 * Kept in memory — simple and avoids serialising every model into route arguments.
 */
object NavData {
    var selectedEvent: SupabaseEvent? = null
    var selectedDistance: Double? = null
    var selectedAd: SupabaseAd? = null
}

/** Navigation already URL-decodes path arguments; "-" stands for an empty value. */
fun String?.navArg(): String = this?.let { if (it == "-") "" else it } ?: ""
