package com.hayesenterprisesllc.onthaset.data

import com.hayesenterprisesllc.onthaset.util.DateUtils
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// Event categories (EventCategory.swift)
// ─────────────────────────────────────────────────────────────────────────────

enum class EventCategory(val rawValue: String, val icon: String) {
    COMMUNITY("Community", "🤝"),
    CHARITY("Charity Event / Fundraiser", "❤️"),
    WEEKLY_CLUBHOUSE("Weekly Clubhouse", "🏠"),
    BIKE_NIGHT("Bike Night", "🏍️"),
    RALLY("Motorcycle Rally", "🎪"),
    MC_ANNUAL("MC Annual", "🎉"),
    SC_ANNUAL("Social Club Annual", "🌹"),
    RC_ANNUAL("Riding Club Annual", "🚦"),
    UNITY_RUN("Unity Run", "🤝");

    val displayName: String get() = rawValue

    /** Whether this category shows on the National Run Calendar map. */
    val isNationalEvent: Boolean
        get() = when (this) {
            RALLY, CHARITY, MC_ANNUAL, SC_ANNUAL, RC_ANNUAL, UNITY_RUN -> true
            else -> false
        }

    /** Pin color on national map. */
    val mapPinColor: String
        get() = when (this) {
            RALLY -> "red"
            CHARITY -> "blue"
            MC_ANNUAL -> "yellow"
            SC_ANNUAL -> "pink"
            RC_ANNUAL -> "green"
            UNITY_RUN -> "purple"
            else -> "gray"
        }

    companion object {
        fun fromRaw(raw: String?): EventCategory? =
            entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Local models (SwiftData @Model classes → JSON-persisted data classes)
// ─────────────────────────────────────────────────────────────────────────────

/** Local event (EventLayout.swift `Event`). */
data class LocalEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val date: Instant = Instant.now(),
    val category: EventCategory = EventCategory.COMMUNITY,
    val locationName: String = "",
    val details: String = "",
    val securityCode: String = "",
    val price: String = "3.00",
    val isFavorite: Boolean = false,
    val imagePath: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val postedByUserID: String = "",
    val postedByName: String = "",
    val postedDate: Instant = Instant.now()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("title", title); put("date", date.toEpochMilli())
        put("category", category.rawValue); put("locationName", locationName)
        put("details", details); put("securityCode", securityCode); put("price", price)
        put("isFavorite", isFavorite); put("imagePath", imagePath ?: JSONObject.NULL)
        put("latitude", latitude); put("longitude", longitude)
        put("postedByUserID", postedByUserID); put("postedByName", postedByName)
        put("postedDate", postedDate.toEpochMilli())
    }

    companion object {
        fun fromJson(o: JSONObject) = LocalEvent(
            id = o.optString("id", UUID.randomUUID().toString()),
            title = o.optString("title"),
            date = Instant.ofEpochMilli(o.optLong("date")),
            category = EventCategory.fromRaw(o.optString("category")) ?: EventCategory.COMMUNITY,
            locationName = o.optString("locationName"),
            details = o.optString("details"),
            securityCode = o.optString("securityCode"),
            price = o.optString("price", "3.00"),
            isFavorite = o.optBoolean("isFavorite"),
            imagePath = o.optStringOrNull("imagePath"),
            latitude = o.optDouble("latitude", 0.0),
            longitude = o.optDouble("longitude", 0.0),
            postedByUserID = o.optString("postedByUserID"),
            postedByName = o.optString("postedByName"),
            postedDate = Instant.ofEpochMilli(o.optLong("postedDate", System.currentTimeMillis()))
        )
    }
}

/** EventPhotoModel.swift */
data class EventPhoto(
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Instant = Instant.now(),
    val eventName: String,
    val eventDate: Instant,
    val location: String,
    val caption: String,
    /** Either a Supabase public URL (http…) or a legacy local filename. */
    val photoFileName: String,
    val userID: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("createdAt", createdAt.toEpochMilli()); put("eventName", eventName)
        put("eventDate", eventDate.toEpochMilli()); put("location", location)
        put("caption", caption); put("photoFileName", photoFileName); put("userID", userID)
    }

    companion object {
        fun fromJson(o: JSONObject) = EventPhoto(
            id = o.optString("id"),
            createdAt = Instant.ofEpochMilli(o.optLong("createdAt")),
            eventName = o.optString("eventName"),
            eventDate = Instant.ofEpochMilli(o.optLong("eventDate")),
            location = o.optString("location"),
            caption = o.optString("caption"),
            photoFileName = o.optString("photoFileName"),
            userID = o.optString("userID")
        )
    }
}

/** BikeProgressModel.swift */
data class BikeProgress(
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Instant = Instant.now(),
    val modificationTitle: String,
    val note: String,
    val beforeImage: String,
    val afterImage: String,
    val bikeMake: String = "",
    val bikeModel: String = "",
    val bikeYear: String = "",
    val userID: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("createdAt", createdAt.toEpochMilli())
        put("modificationTitle", modificationTitle); put("note", note)
        put("beforeImage", beforeImage); put("afterImage", afterImage)
        put("bikeMake", bikeMake); put("bikeModel", bikeModel); put("bikeYear", bikeYear)
        put("userID", userID)
    }

    companion object {
        fun fromJson(o: JSONObject) = BikeProgress(
            id = o.optString("id"),
            createdAt = Instant.ofEpochMilli(o.optLong("createdAt")),
            modificationTitle = o.optString("modificationTitle"),
            note = o.optString("note"),
            beforeImage = o.optString("beforeImage"),
            afterImage = o.optString("afterImage"),
            bikeMake = o.optString("bikeMake"),
            bikeModel = o.optString("bikeModel"),
            bikeYear = o.optString("bikeYear"),
            userID = o.optString("userID")
        )
    }
}

/** UserProfileEnhanced.swift — local profile, keyed by appleUserID (the auth user id). */
data class UserProfile(
    val appleUserID: String,
    val email: String,
    val postsThisMonth: Int = 0,
    val lastPostDate: Instant? = null,
    val lastResetDate: Instant? = Instant.now(),
    val hasActiveSubscription: Boolean = false,
    val subscriptionStartDate: Instant? = null,
    val displayName: String = "",
    val bio: String = "",
    val hometown: String = "",
    val club: String = "",
    val favoriteRide: String = "",
    val ridingSince: String = "",
    /** Local file path of the cached profile image (replaces profileImageData). */
    val profileImagePath: String? = null,
    val backgroundImagePath: String? = null,
    val instagramHandle: String = "",
    val tiktokHandle: String = "",
    val youtubeChannel: String = "",
    val facebookHandle: String = "",
    val preferredRideType: String = "",
    val favoriteRoute: String = "",
    val totalPhotosPosted: Int = 0,
    val totalBikeProgressPosts: Int = 0,
    val memberSince: Instant = Instant.now(),
    val hasCompletedSetup: Boolean = false,
    val profileImageURL: String = "",
    val backgroundImageURL: String = "",
    val photosThisMonth: Int = 0,
    val photosStoredTotal: Int = 0,
    val lastPhotoResetDate: Instant? = null
) {
    companion object {
        const val MONTHLY_POST_LIMIT = 4
        const val MONTHLY_PHOTO_LIMIT = 20
        const val TOTAL_PHOTO_LIMIT = 50

        private fun isNewMonth(last: Instant?): Boolean {
            if (last == null) return false
            val zone = ZoneId.systemDefault()
            val now = Instant.now().atZone(zone)
            val then = last.atZone(zone)
            return now.monthValue != then.monthValue || now.year != then.year
        }

        fun fromJson(o: JSONObject) = UserProfile(
            appleUserID = o.optString("appleUserID"),
            email = o.optString("email"),
            postsThisMonth = o.optInt("postsThisMonth"),
            lastPostDate = o.optInstant("lastPostDate"),
            lastResetDate = o.optInstant("lastResetDate"),
            hasActiveSubscription = o.optBoolean("hasActiveSubscription"),
            subscriptionStartDate = o.optInstant("subscriptionStartDate"),
            displayName = o.optString("displayName"),
            bio = o.optString("bio"),
            hometown = o.optString("hometown"),
            club = o.optString("club"),
            favoriteRide = o.optString("favoriteRide"),
            ridingSince = o.optString("ridingSince"),
            profileImagePath = o.optStringOrNull("profileImagePath"),
            backgroundImagePath = o.optStringOrNull("backgroundImagePath"),
            instagramHandle = o.optString("instagramHandle"),
            tiktokHandle = o.optString("tiktokHandle"),
            youtubeChannel = o.optString("youtubeChannel"),
            facebookHandle = o.optString("facebookHandle"),
            preferredRideType = o.optString("preferredRideType"),
            favoriteRoute = o.optString("favoriteRoute"),
            totalPhotosPosted = o.optInt("totalPhotosPosted"),
            totalBikeProgressPosts = o.optInt("totalBikeProgressPosts"),
            memberSince = o.optInstant("memberSince") ?: Instant.now(),
            hasCompletedSetup = o.optBoolean("hasCompletedSetup"),
            profileImageURL = o.optString("profileImageURL"),
            backgroundImageURL = o.optString("backgroundImageURL"),
            photosThisMonth = o.optInt("photosThisMonth"),
            photosStoredTotal = o.optInt("photosStoredTotal"),
            lastPhotoResetDate = o.optInstant("lastPhotoResetDate")
        )
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("appleUserID", appleUserID); put("email", email)
        put("postsThisMonth", postsThisMonth); putInstant("lastPostDate", lastPostDate)
        putInstant("lastResetDate", lastResetDate)
        put("hasActiveSubscription", hasActiveSubscription)
        putInstant("subscriptionStartDate", subscriptionStartDate)
        put("displayName", displayName); put("bio", bio); put("hometown", hometown)
        put("club", club); put("favoriteRide", favoriteRide); put("ridingSince", ridingSince)
        put("profileImagePath", profileImagePath ?: JSONObject.NULL)
        put("backgroundImagePath", backgroundImagePath ?: JSONObject.NULL)
        put("instagramHandle", instagramHandle); put("tiktokHandle", tiktokHandle)
        put("youtubeChannel", youtubeChannel); put("facebookHandle", facebookHandle)
        put("preferredRideType", preferredRideType); put("favoriteRoute", favoriteRoute)
        put("totalPhotosPosted", totalPhotosPosted)
        put("totalBikeProgressPosts", totalBikeProgressPosts)
        putInstant("memberSince", memberSince)
        put("hasCompletedSetup", hasCompletedSetup)
        put("profileImageURL", profileImageURL); put("backgroundImageURL", backgroundImageURL)
        put("photosThisMonth", photosThisMonth); put("photosStoredTotal", photosStoredTotal)
        putInstant("lastPhotoResetDate", lastPhotoResetDate)
    }

    // ── Post tracking ────────────────────────────────────────────────────────

    /** Returns a copy with the monthly post count reset if a new month began. */
    fun withMonthlyCountChecked(): UserProfile =
        when {
            lastResetDate == null -> copy(lastResetDate = Instant.now())
            isNewMonth(lastResetDate) -> copy(postsThisMonth = 0, lastResetDate = Instant.now())
            else -> this
        }

    fun canPost(): Boolean =
        hasActiveSubscription && withMonthlyCountChecked().postsThisMonth < MONTHLY_POST_LIMIT

    fun withIncrementedPostCount(): UserProfile =
        copy(postsThisMonth = postsThisMonth + 1, lastPostDate = Instant.now())

    fun remainingPosts(): Int =
        if (!hasActiveSubscription) 0
        else maxOf(0, MONTHLY_POST_LIMIT - withMonthlyCountChecked().postsThisMonth)

    fun isProfileComplete(): Boolean =
        displayName.isNotEmpty() && bio.isNotEmpty() && favoriteRide.isNotEmpty()

    // ── Photo limits (20/month, 50 total) ────────────────────────────────────

    fun withMonthlyPhotoCountChecked(): UserProfile =
        when {
            lastPhotoResetDate == null -> copy(lastPhotoResetDate = Instant.now())
            isNewMonth(lastPhotoResetDate) -> copy(photosThisMonth = 0, lastPhotoResetDate = Instant.now())
            else -> this
        }

    fun canUploadPhoto(): Boolean {
        if (!hasActiveSubscription) return false
        val p = withMonthlyPhotoCountChecked()
        return p.photosThisMonth < MONTHLY_PHOTO_LIMIT && p.photosStoredTotal < TOTAL_PHOTO_LIMIT
    }

    fun remainingPhotosThisMonth(): Int =
        maxOf(0, MONTHLY_PHOTO_LIMIT - withMonthlyPhotoCountChecked().photosThisMonth)

    fun remainingPhotoSlots(): Int = maxOf(0, TOTAL_PHOTO_LIMIT - photosStoredTotal)

    fun withIncrementedPhotoCount(): UserProfile = copy(
        photosThisMonth = photosThisMonth + 1,
        photosStoredTotal = photosStoredTotal + 1,
        totalPhotosPosted = totalPhotosPosted + 1
    )

    fun withDecrementedPhotoCount(): UserProfile =
        copy(photosStoredTotal = maxOf(0, photosStoredTotal - 1))

    val nameOrEmail: String get() = displayName.ifEmpty { email }
}

// ─────────────────────────────────────────────────────────────────────────────
// Supabase models (SupabaseManager.swift / PostedByProfileView.swift)
// ─────────────────────────────────────────────────────────────────────────────

data class SupabaseEvent(
    val id: String? = null,
    val title: String,
    val date: Instant,
    val category: String,
    val locationName: String,
    val details: String,
    val price: String,
    val latitude: Double,
    val longitude: Double,
    val postedByUserID: String,
    val postedByName: String,
    val createdAt: Instant? = null,
    val imageURL: String? = null
) {
    companion object {
        fun fromJson(o: JSONObject) = SupabaseEvent(
            id = o.optStringOrNull("id"),
            title = o.optString("title"),
            date = DateUtils.parseSupabaseDate(o.optString("date")) ?: Instant.EPOCH,
            category = o.optString("category"),
            locationName = o.optString("location_name"),
            details = o.optString("details"),
            price = o.optString("price"),
            latitude = o.optDouble("latitude", 0.0).let { if (it.isNaN()) 0.0 else it },
            longitude = o.optDouble("longitude", 0.0).let { if (it.isNaN()) 0.0 else it },
            postedByUserID = o.optString("posted_by_user_id"),
            postedByName = o.optString("posted_by_name"),
            createdAt = DateUtils.parseSupabaseDate(o.optStringOrNull("created_at")),
            imageURL = o.optStringOrNull("image_url")
        )
    }
}

data class NearbyEvent(
    val id: String,
    val title: String,
    val date: Instant,
    val category: String,
    val locationName: String,
    val details: String,
    val price: String,
    val latitude: Double,
    val longitude: Double,
    val postedByName: String,
    val postedByUserID: String,
    val distanceMiles: Double,
    val imageURL: String?
) {
    fun toSupabaseEvent() = SupabaseEvent(
        id = id, title = title, date = date, category = category, locationName = locationName,
        details = details, price = price, latitude = latitude, longitude = longitude,
        postedByUserID = postedByUserID, postedByName = postedByName, imageURL = imageURL
    )
}

data class SupabaseAd(
    val id: String? = null,
    val businessName: String,
    val tagline: String,
    val category: String,
    val phone: String? = null,
    val websiteURL: String? = null,
    val imageURL: String? = null,
    val address: String? = null,
    val status: String = "pending",
    val plan: String = "basic",
    val advertiserEmail: String? = null,
    val paymentStatus: String? = null,
    val paidUntil: Instant? = null,
    val notes: String? = null,
    val billingPreference: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val advertiserPin: String? = null,
    val sponsored: Boolean? = null,
    val createdAt: Instant? = null
) {
    companion object {
        fun fromJson(o: JSONObject) = SupabaseAd(
            id = o.optStringOrNull("id"),
            businessName = o.optString("business_name"),
            tagline = o.optString("tagline"),
            category = o.optString("category"),
            phone = o.optStringOrNull("phone"),
            websiteURL = o.optStringOrNull("website_url"),
            imageURL = o.optStringOrNull("image_url"),
            address = o.optStringOrNull("address"),
            status = o.optString("status"),
            plan = o.optString("plan"),
            advertiserEmail = o.optStringOrNull("advertiser_email"),
            paymentStatus = o.optStringOrNull("payment_status"),
            paidUntil = DateUtils.parseSupabaseDate(o.optStringOrNull("paid_until")),
            notes = o.optStringOrNull("notes"),
            billingPreference = o.optStringOrNull("billing_preference"),
            latitude = o.optDoubleOrNull("latitude"),
            longitude = o.optDoubleOrNull("longitude"),
            advertiserPin = o.optStringOrNull("advertiser_pin"),
            sponsored = if (o.has("sponsored") && !o.isNull("sponsored")) o.optBoolean("sponsored") else null,
            createdAt = DateUtils.parseSupabaseDate(o.optStringOrNull("created_at"))
        )
    }
}

data class SupabaseUserProfile(
    val appleUserID: String,
    val displayName: String,
    val email: String,
    val bio: String? = null,
    val hometown: String? = null,
    val club: String? = null,
    val favoriteRide: String? = null,
    val ridingSince: String? = null,
    val preferredRideType: String? = null,
    val favoriteRoute: String? = null,
    val instagramHandle: String? = null,
    val tiktokHandle: String? = null,
    val youtubeChannel: String? = null,
    val facebookHandle: String? = null,
    val profileImageURL: String? = null,
    val backgroundImageURL: String? = null
) {
    companion object {
        fun fromJson(o: JSONObject) = SupabaseUserProfile(
            appleUserID = o.optString("apple_user_id"),
            displayName = o.optString("display_name"),
            email = o.optString("email"),
            bio = o.optStringOrNull("bio"),
            hometown = o.optStringOrNull("hometown"),
            club = o.optStringOrNull("club"),
            favoriteRide = o.optStringOrNull("favorite_ride"),
            ridingSince = o.optStringOrNull("riding_since"),
            preferredRideType = o.optStringOrNull("preferred_ride_type"),
            favoriteRoute = o.optStringOrNull("favorite_route"),
            instagramHandle = o.optStringOrNull("instagram_handle"),
            tiktokHandle = o.optStringOrNull("tiktok_handle"),
            youtubeChannel = o.optStringOrNull("youtube_channel"),
            facebookHandle = o.optStringOrNull("facebook_handle"),
            profileImageURL = o.optStringOrNull("profile_image_url"),
            backgroundImageURL = o.optStringOrNull("background_image_url")
        )
    }
}

data class PublicEventPhoto(
    val id: String?,
    val uploadedBy: String,
    val eventName: String?,
    val caption: String?,
    val imageURL: String,
    val createdAt: String?
) {
    companion object {
        fun fromJson(o: JSONObject) = PublicEventPhoto(
            id = o.optStringOrNull("id"),
            uploadedBy = o.optString("uploaded_by"),
            eventName = o.optStringOrNull("event_name"),
            caption = o.optStringOrNull("caption"),
            imageURL = o.optString("image_url"),
            createdAt = o.optStringOrNull("created_at")
        )
    }
}

data class PublicBikeBuild(
    val id: String?,
    val userID: String,
    val modificationTitle: String,
    val note: String,
    val beforeImageURL: String,
    val afterImageURL: String,
    val bikeMake: String,
    val bikeModel: String,
    val bikeYear: String,
    val createdAt: String?
) {
    companion object {
        fun fromJson(o: JSONObject) = PublicBikeBuild(
            id = o.optStringOrNull("id"),
            userID = o.optString("user_id"),
            modificationTitle = o.optString("modification_title"),
            note = o.optString("note"),
            beforeImageURL = o.optString("before_image_url"),
            afterImageURL = o.optString("after_image_url"),
            bikeMake = o.optString("bike_make"),
            bikeModel = o.optString("bike_model"),
            bikeYear = o.optString("bike_year"),
            createdAt = o.optStringOrNull("created_at")
        )
    }
}

/** AppUser.swift */
data class AppUser(val id: String, val username: String, val role: UserRole = UserRole.MEMBER)

enum class UserRole { ADMIN, MEMBER, GUEST }

// ─────────────────────────────────────────────────────────────────────────────
// Location string helpers — events store "venue|street|city|state|zip"
// ─────────────────────────────────────────────────────────────────────────────

object LocationParts {
    fun split(locationName: String): List<String> = locationName.split("|").filter { it.isNotEmpty() }

    fun venue(locationName: String, fallback: String = "TBA"): String =
        split(locationName).firstOrNull() ?: fallback

    fun city(locationName: String): String {
        val p = split(locationName)
        return if (p.size >= 3) p[2] else locationName
    }

    /** "Venue — City" for list rows. */
    fun short(locationName: String): String {
        val p = split(locationName)
        return if (p.size >= 3) "${p[0]} — ${p[2]}" else locationName
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// JSON helpers
// ─────────────────────────────────────────────────────────────────────────────

fun JSONObject.optStringOrNull(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key)

fun JSONObject.optDoubleOrNull(key: String): Double? =
    if (!has(key) || isNull(key)) null else optDouble(key).takeUnless { it.isNaN() }

fun JSONObject.optInstant(key: String): Instant? =
    if (!has(key) || isNull(key)) null else Instant.ofEpochMilli(optLong(key))

fun JSONObject.putInstant(key: String, value: Instant?) {
    put(key, value?.toEpochMilli() ?: JSONObject.NULL)
}

/** Supabase may return has_subscription as Bool or Int. */
fun JSONObject.optFlexibleBool(key: String): Boolean {
    if (!has(key) || isNull(key)) return false
    return when (val v = opt(key)) {
        is Boolean -> v
        is Number -> v.toInt() == 1
        is String -> v == "true" || v == "1"
        else -> false
    }
}
