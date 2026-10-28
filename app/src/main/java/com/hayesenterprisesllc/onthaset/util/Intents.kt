package com.hayesenterprisesllc.onthaset.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.hayesenterprisesllc.onthaset.data.Http
import java.io.File

/** Replacements for UIApplication.shared.open, UIPasteboard and UIActivityViewController. */
object Intents {

    fun openUrl(context: Context, url: String) {
        var formatted = url.trim()
        if (!formatted.startsWith("http") && !formatted.contains(":")) formatted = "https://$formatted"
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formatted)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app can open this link", Toast.LENGTH_SHORT).show()
        }
    }

    fun email(context: Context, to: String, subject: String, body: String = "") {
        val uri = Uri.parse("mailto:$to?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
        try {
            context.startActivity(Intent(Intent.ACTION_SENDTO, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun dial(context: Context, phone: String) {
        val digits = phone.filter { it.isDigit() || it == '+' }
        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    fun sms(context: Context, phone: String, body: String) {
        val digits = phone.filter { it.isDigit() }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$digits")).putExtra("sms_body", body).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
        }
    }

    fun copy(context: Context, text: String, label: String = "On Tha Set") {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** Share text, optionally with an image (local file or remote URL downloaded first). */
    suspend fun share(context: Context, text: String, imageUrl: String? = null, imageFile: File? = null) {
        var file = imageFile
        if (file == null && !imageUrl.isNullOrEmpty()) {
            Http.getBytes(imageUrl)?.let { bytes ->
                val dir = File(context.cacheDir, "images").apply { mkdirs() }
                file = File(dir, "share_flyer.jpg").apply { writeBytes(bytes) }
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            val f = file
            if (f != null && f.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }
        }
        context.startActivity(Intent.createChooser(intent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun openAppSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun manageSubscriptions(context: Context) =
        openUrl(context, "https://play.google.com/store/account/subscriptions?package=${context.packageName}")

    // ── Navigation apps (Apple Maps / Google Maps / Waze) ────────────────────

    fun openDefaultMaps(context: Context, lat: Double, lng: Double, label: String) {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            openGoogleMaps(context, lat, lng)
        }
    }

    fun openGoogleMaps(context: Context, lat: Double, lng: Double) {
        val nav = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lng&mode=d"))
            .setPackage("com.google.android.apps.maps").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(nav)
        } catch (e: ActivityNotFoundException) {
            openUrl(context, "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        }
    }

    fun openWaze(context: Context, lat: Double, lng: Double) =
        openUrl(context, "https://waze.com/ul?ll=$lat,$lng&navigate=yes")

    /** Fallback when an event has no coordinates — search by address text. */
    fun openMapsSearch(context: Context, query: String) =
        openUrl(context, "https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
}
