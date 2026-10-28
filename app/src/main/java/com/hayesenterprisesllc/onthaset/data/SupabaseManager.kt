package com.hayesenterprisesllc.onthaset.data

import com.hayesenterprisesllc.onthaset.location.LocationManager
import com.hayesenterprisesllc.onthaset.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/** Port of SupabaseManager.swift — all calls use the Supabase REST API directly. */
object SupabaseManager {

    private val _events = MutableStateFlow<List<SupabaseEvent>>(emptyList())
    val events: StateFlow<List<SupabaseEvent>> = _events

    private val _activeAds = MutableStateFlow<List<SupabaseAd>>(emptyList())
    val activeAds: StateFlow<List<SupabaseAd>> = _activeAds

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private fun JSONArray.toEvents(): List<SupabaseEvent> =
        (0 until length()).mapNotNull { optJSONObject(it)?.let { o -> runCatching { SupabaseEvent.fromJson(o) }.getOrNull() } }

    private fun JSONArray.toAds(): List<SupabaseAd> =
        (0 until length()).mapNotNull { optJSONObject(it)?.let { o -> runCatching { SupabaseAd.fromJson(o) }.getOrNull() } }

    // ── Events ───────────────────────────────────────────────────────────────

    suspend fun fetchAllEvents() {
        _isLoading.value = true
        try {
            val cutoff = Http.enc(DateUtils.iso(DateUtils.eventCutoff()))
            val res = Http.restGet("events?date=gte.$cutoff&order=date.asc")
            if (res.isSuccess) {
                _events.value = res.jsonArray().toEvents()
                Http.log("✅ Fetched ${_events.value.size} upcoming events from Supabase")
            } else {
                _errorMessage.value = "HTTP ${res.code}"
            }
        } catch (e: Exception) {
            Http.log("❌ Error fetching events: $e")
            _errorMessage.value = e.localizedMessage
        }
        _isLoading.value = false
    }

    suspend fun postEvent(event: SupabaseEvent) {
        val body = JSONObject().apply {
            put("title", event.title)
            put("date", DateUtils.iso(event.date))
            put("category", event.category)
            put("location_name", event.locationName)
            put("details", event.details)
            put("price", event.price)
            put("latitude", event.latitude)
            put("longitude", event.longitude)
            put("posted_by_user_id", event.postedByUserID)
            put("posted_by_name", event.postedByName)
            event.imageURL?.let { put("image_url", it) }
        }
        val res = Http.restPost("events", body)
        Http.log("🔵 HTTP Status: ${res.code}")
        if (res.isSuccess) {
            fetchAllEvents()
        } else {
            throw HttpException(res.code, res.body.ifEmpty { "Unknown error" })
        }
    }

    suspend fun updateEvent(id: String, fields: JSONObject): Boolean {
        val res = Http.restPatch("events?id=eq.$id", fields)
        if (res.isSuccess) fetchAllEvents()
        return res.isSuccess
    }

    private fun firstImageUrl(res: HttpResult): String? {
        val arr = res.jsonArray()
        val first = arr.optJSONObject(0) ?: return null
        return first.optStringOrNull("image_url")?.takeIf { it.isNotEmpty() }
    }

    suspend fun deleteEvent(id: String) {
        runCatching { Http.restGet("events?id=eq.$id&select=image_url") }.getOrNull()
            ?.let(::firstImageUrl)
            ?.let { deleteStorageFile(it, "event-flyers") }
        Http.restDelete("events?id=eq.$id")
        Http.log("✅ Event and flyer deleted from Supabase")
        fetchAllEvents()
    }

    suspend fun deleteEventByTitleAndUser(title: String, userID: String) {
        val t = Http.enc(title)
        runCatching { Http.restGet("events?title=eq.$t&posted_by_user_id=eq.$userID&select=image_url") }.getOrNull()
            ?.let(::firstImageUrl)
            ?.let { deleteStorageFile(it, "event-flyers") }
        runCatching { Http.restDelete("events?title=eq.$t&posted_by_user_id=eq.$userID") }
    }

    suspend fun saveEventPhotoMetadata(
        userID: String, eventName: String, eventDate: Instant,
        location: String, caption: String, photoURL: String
    ) {
        val body = JSONObject().apply {
            put("uploaded_by", userID)
            put("event_name", eventName)
            put("event_date", DateUtils.iso(eventDate))
            put("location", location)
            put("caption", caption)
            put("image_url", photoURL)
        }
        runCatching { Http.restPost("event_photos", body) }
            .onSuccess { Http.log(if (it.code == 201) "✅ Event photo metadata saved" else "❌ Event photo metadata FAILED (${it.code}): ${it.body}") }
    }

    suspend fun saveBikeBuildMetadata(
        userID: String, modificationTitle: String, note: String,
        beforeImageURL: String, afterImageURL: String,
        bikeMake: String, bikeModel: String, bikeYear: String
    ) {
        val body = JSONObject().apply {
            put("user_id", userID)
            put("modification_title", modificationTitle)
            put("note", note)
            put("before_image_url", beforeImageURL)
            put("after_image_url", afterImageURL)
            put("bike_make", bikeMake)
            put("bike_model", bikeModel)
            put("bike_year", bikeYear)
        }
        runCatching { Http.restPost("bike_builds", body) }
    }

    suspend fun deleteEventPhotoRecord(imageURL: String, userID: String) {
        runCatching { Http.restDelete("event_photos?image_url=eq.${Http.enc(imageURL)}&uploaded_by=eq.$userID") }
    }

    suspend fun deleteBikeBuildRecord(afterImageURL: String, userID: String) {
        runCatching { Http.restDelete("bike_builds?after_image_url=eq.${Http.enc(afterImageURL)}&user_id=eq.$userID") }
    }

    suspend fun deleteStorageFile(imageURL: String, bucket: String) {
        if (imageURL.isEmpty()) return
        val fileName = imageURL.substringAfterLast("/$bucket/", "").ifEmpty { return }
        runCatching { Http.storageDelete(bucket, fileName) }
            .onSuccess { Http.log(if (it.code == 200) "✅ Deleted storage file: $fileName" else "⚠️ Storage delete returned: ${it.code}") }
    }

    // ── Nearby Events ────────────────────────────────────────────────────────

    suspend fun fetchNearbyEvents(lat: Double, lng: Double, radiusMiles: Double = 25.0): List<NearbyEvent> {
        val cutoff = Http.enc(DateUtils.iso(DateUtils.eventCutoff()))
        val res = Http.restGet("events?date=gte.$cutoff&order=date.asc")
        if (res.code != 200) throw HttpException(res.code, "HTTP ${res.code}")
        val all = res.jsonArray().toEvents()
        return all.mapNotNull { e ->
            if (e.latitude == 0.0 || e.longitude == 0.0) return@mapNotNull null
            val d = LocationManager.haversineMiles(lat, lng, e.latitude, e.longitude)
            if (d > radiusMiles) return@mapNotNull null
            NearbyEvent(
                id = e.id ?: java.util.UUID.randomUUID().toString(),
                title = e.title, date = e.date, category = e.category, locationName = e.locationName,
                details = e.details, price = e.price, latitude = e.latitude, longitude = e.longitude,
                postedByName = e.postedByName, postedByUserID = e.postedByUserID,
                distanceMiles = d, imageURL = e.imageURL
            )
        }.sortedBy { it.distanceMiles }
    }

    // ── Ads ──────────────────────────────────────────────────────────────────

    suspend fun fetchActiveAds() {
        try {
            val res = Http.restGet("ads?status=eq.active")
            val all = res.jsonArray().toAds()
            val user = LocationManager.userLocation.value
            _activeAds.value = if (user != null) {
                all.filter { ad ->
                    val la = ad.latitude; val lo = ad.longitude
                    if (la == null || lo == null || la == 0.0 || lo == 0.0) true
                    else LocationManager.haversineMiles(user.latitude, user.longitude, la, lo) <= 100.0
                }
            } else all
            Http.log("✅ Fetched ${_activeAds.value.size} active ads")
        } catch (e: Exception) {
            Http.log("❌ Error fetching ads: $e")
        }
    }

    suspend fun submitAd(ad: SupabaseAd) {
        var geocodeString = ad.address.orEmpty()
        ad.notes?.let { notes ->
            val i = notes.indexOf("geocode:")
            if (i >= 0) {
                var extracted = notes.substring(i + "geocode:".length)
                for (suffix in listOf(",appointment_only", ",hide_address")) {
                    val r = extracted.indexOf(suffix)
                    if (r >= 0) extracted = extracted.substring(0, r)
                }
                if (extracted.isNotEmpty()) geocodeString = extracted
            }
        }
        val coords = if (geocodeString.isNotEmpty()) LocationManager.geocode(geocodeString) else null

        val body = JSONObject().apply {
            put("business_name", ad.businessName)
            put("tagline", ad.tagline)
            put("category", ad.category)
            put("phone", ad.phone ?: "")
            put("website_url", ad.websiteURL ?: "")
            put("status", "pending")
            put("plan", ad.plan)
            put("payment_status", "unpaid")
            ad.address?.takeIf { it.isNotEmpty() }?.let { put("address", it) }
            ad.imageURL?.takeIf { it.isNotEmpty() }?.let { put("image_url", it) }
            ad.advertiserEmail?.takeIf { it.isNotEmpty() }?.let { put("advertiser_email", it) }
            ad.billingPreference?.let { put("billing_preference", it) }
            ad.advertiserPin?.takeIf { it.isNotEmpty() }?.let { put("advertiser_pin", it) }
            ad.notes?.takeIf { it.isNotEmpty() }?.let { put("notes", it) }
            coords?.let { put("latitude", it.first); put("longitude", it.second) }
        }
        val res = Http.restPost("ads", body)
        if (!res.isSuccess) throw HttpException(res.code, res.body.ifEmpty { "Unknown error" })
    }

    suspend fun fetchAds(query: String): List<SupabaseAd> =
        runCatching { Http.restGet("ads?$query").jsonArray().toAds() }.getOrDefault(emptyList())

    suspend fun updateAd(id: String, fields: JSONObject): Boolean =
        runCatching { Http.restPatch("ads?id=eq.$id", fields).isSuccess }.getOrDefault(false)

    suspend fun deleteAd(id: String): Boolean =
        runCatching { Http.restDelete("ads?id=eq.$id").isSuccess }.getOrDefault(false)

    // ── Users ────────────────────────────────────────────────────────────────

    suspend fun fetchUserRow(appleUserID: String): JSONObject? =
        runCatching { Http.restGet("users?apple_user_id=eq.${Http.enc(appleUserID)}&limit=1").jsonArray().optJSONObject(0) }.getOrNull()

    suspend fun fetchUserRowByEmail(email: String): JSONObject? =
        runCatching { Http.restGet("users?email=eq.${Http.enc(email)}&limit=1").jsonArray().optJSONObject(0) }.getOrNull()

    /** Inserts a users row if one does not exist yet for this id. */
    suspend fun ensureUserRow(appleUserID: String, email: String, displayName: String) {
        try {
            val existing = Http.restGet("users?apple_user_id=eq.${Http.enc(appleUserID)}&select=apple_user_id").jsonArray()
            if (existing.length() == 0) {
                Http.restPost("users", JSONObject().apply {
                    put("apple_user_id", appleUserID)
                    put("email", email)
                    put("display_name", displayName)
                })
                Http.log("✅ User saved to Supabase: $email")
            }
        } catch (e: Exception) {
            Http.log("❌ Supabase user save error: $e")
        }
    }

    suspend fun updateUser(appleUserID: String, fields: JSONObject): Boolean =
        runCatching { Http.restPatch("users?apple_user_id=eq.${Http.enc(appleUserID)}", fields).isSuccess }.getOrDefault(false)

    // ── Image Upload ─────────────────────────────────────────────────────────

    suspend fun uploadImage(data: ByteArray, bucket: String, fileName: String): String {
        val res = Http.storageUpload(bucket, fileName, data)
        Http.log("🔵 Image upload status: ${res.code}")
        if (res.code != 200 && res.code != 201) throw HttpException(res.code, res.body.ifEmpty { "Upload failed" })
        return "${SupabaseConfig.PROJECT_URL}/storage/v1/object/public/$bucket/$fileName"
    }
}
