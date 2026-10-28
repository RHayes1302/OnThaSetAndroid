package com.hayesenterprisesllc.onthaset.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * On-device persistence replacing SwiftData's ModelContainer
 * (Event, UserProfile, BikeProgress, EventPhoto).
 * Data is kept in memory as StateFlows and written to a JSON file on every change.
 * Binary image data lives in files under filesDir/images.
 */
object LocalStore {
    private lateinit var dbFile: File
    lateinit var imagesDir: File
        private set

    private val _profiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val profiles: StateFlow<List<UserProfile>> = _profiles

    private val _events = MutableStateFlow<List<LocalEvent>>(emptyList())
    val events: StateFlow<List<LocalEvent>> = _events

    private val _photos = MutableStateFlow<List<EventPhoto>>(emptyList())
    val photos: StateFlow<List<EventPhoto>> = _photos

    private val _bikeProgress = MutableStateFlow<List<BikeProgress>>(emptyList())
    val bikeProgress: StateFlow<List<BikeProgress>> = _bikeProgress

    fun init(context: Context) {
        dbFile = File(context.filesDir, "onthaset_store.json")
        imagesDir = File(context.filesDir, "images").apply { mkdirs() }
        load()
    }

    @Synchronized
    private fun load() {
        if (!dbFile.exists()) return
        runCatching {
            val root = JSONObject(dbFile.readText())
            _profiles.value = root.optJSONArray("profiles").mapObjects { UserProfile.fromJson(it) }
            _events.value = root.optJSONArray("events").mapObjects { LocalEvent.fromJson(it) }
            _photos.value = root.optJSONArray("photos").mapObjects { EventPhoto.fromJson(it) }
            _bikeProgress.value = root.optJSONArray("bikeProgress").mapObjects { BikeProgress.fromJson(it) }
        }.onFailure { Http.log("❌ LocalStore load failed: $it") }
    }

    @Synchronized
    private fun save() {
        runCatching {
            val root = JSONObject()
            root.put("profiles", JSONArray(_profiles.value.map { it.toJson() }))
            root.put("events", JSONArray(_events.value.map { it.toJson() }))
            root.put("photos", JSONArray(_photos.value.map { it.toJson() }))
            root.put("bikeProgress", JSONArray(_bikeProgress.value.map { it.toJson() }))
            val tmp = File(dbFile.parentFile, dbFile.name + ".tmp")
            tmp.writeText(root.toString())
            tmp.renameTo(dbFile)
        }.onFailure { Http.log("❌ LocalStore save failed: $it") }
    }

    // ── Profiles ────────────────────────────────────────────────────────────

    fun profileFor(userID: String?): UserProfile? {
        if (userID == null) return null
        val all = _profiles.value
        return all.firstOrNull { it.appleUserID == userID }
            ?: all.firstOrNull { it.appleUserID.equals(userID, ignoreCase = true) }
    }

    @Synchronized
    fun upsertProfile(profile: UserProfile) {
        val list = _profiles.value.toMutableList()
        val idx = list.indexOfFirst { it.appleUserID == profile.appleUserID }
        if (idx >= 0) list[idx] = profile else list.add(profile)
        _profiles.value = list
        save()
    }

    /** Fetch-modify-save, like mutating a SwiftData object then calling context.save(). */
    @Synchronized
    fun updateProfile(userID: String, transform: (UserProfile) -> UserProfile): UserProfile? {
        val existing = profileFor(userID) ?: return null
        val updated = transform(existing)
        upsertProfile(updated)
        return updated
    }

    // ── Events ──────────────────────────────────────────────────────────────

    @Synchronized
    fun insertEvent(event: LocalEvent) {
        _events.value = _events.value + event
        save()
    }

    @Synchronized
    fun updateEvent(event: LocalEvent) {
        _events.value = _events.value.map { if (it.id == event.id) event else it }
        save()
    }

    @Synchronized
    fun deleteEvent(event: LocalEvent) {
        event.imagePath?.let { File(it).delete() }
        _events.value = _events.value.filterNot { it.id == event.id }
        save()
    }

    @Synchronized
    fun deleteEvents(predicate: (LocalEvent) -> Boolean) {
        val (gone, keep) = _events.value.partition(predicate)
        gone.forEach { e -> e.imagePath?.let { File(it).delete() } }
        _events.value = keep
        save()
    }

    // ── Photos ──────────────────────────────────────────────────────────────

    @Synchronized
    fun insertPhoto(photo: EventPhoto) {
        _photos.value = _photos.value + photo
        save()
    }

    @Synchronized
    fun deletePhoto(photo: EventPhoto) {
        if (!photo.photoFileName.startsWith("http")) File(imagesDir, photo.photoFileName).delete()
        _photos.value = _photos.value.filterNot { it.id == photo.id }
        save()
    }

    // ── Bike progress ──────────────────────────────────────────────────────

    @Synchronized
    fun insertBikeProgress(entry: BikeProgress) {
        _bikeProgress.value = _bikeProgress.value + entry
        save()
    }

    @Synchronized
    fun deleteBikeProgress(entry: BikeProgress) {
        listOf(entry.beforeImage, entry.afterImage)
            .filter { it.isNotEmpty() && !it.startsWith("http") }
            .forEach { File(imagesDir, it).delete() }
        _bikeProgress.value = _bikeProgress.value.filterNot { it.id == entry.id }
        save()
    }

    // ── Image files (ImageFileStore.swift) ──────────────────────────────────

    /** Writes bytes to a new file and returns its absolute path. */
    fun saveImageBytes(bytes: ByteArray, name: String = "${UUID.randomUUID()}.jpg"): String {
        val f = File(imagesDir, name)
        f.writeBytes(bytes)
        return f.absolutePath
    }

    fun localImageFile(fileName: String): File = File(imagesDir, fileName)

    private fun <T> JSONArray?.mapObjects(f: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { i -> optJSONObject(i)?.let { runCatching { f(it) }.getOrNull() } }
    }
}
