package com.hayesenterprisesllc.onthaset.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Port of ImageCompressor.swift + PhotosPicker data loading. */
object ImageUtils {

    /** Decodes an image Uri (photo picker / camera), downscaled so the long edge ≤ maxDimension. */
    suspend fun loadBitmap(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val src = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val w = info.size.width; val h = info.size.height
                    val ratio = maxDimension.toFloat() / maxOf(w, h)
                    if (ratio < 1f) decoder.setTargetSize((w * ratio).toInt(), (h * ratio).toInt())
                }
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxDimension) sample *= 2
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                }
            }
        }.getOrNull()
    }

    /** Compresses to JPEG ≤ maxSizeKB: lowers quality first, then resizes to 1200px max. */
    fun compress(bitmap: Bitmap, maxSizeKB: Int = 500): ByteArray {
        val maxBytes = maxSizeKB * 1024
        var quality = 80
        var data = jpeg(bitmap, quality)
        while (data.size > maxBytes && quality > 10) {
            quality -= 10
            data = jpeg(bitmap, quality)
        }
        if (data.size > maxBytes) {
            data = jpeg(resize(bitmap, 1200), 70)
        }
        return data
    }

    fun jpeg(bitmap: Bitmap, quality: Int = 90): ByteArray =
        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }

    fun resize(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val ratio = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
        if (ratio >= 1f) return bitmap
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
    }

    /** Convenience: Uri → compressed JPEG bytes. */
    suspend fun uriToCompressedJpeg(context: Context, uri: Uri, maxSizeKB: Int = 500): ByteArray? {
        val bmp = loadBitmap(context, uri) ?: return null
        return withContext(Dispatchers.Default) { compress(bmp, maxSizeKB) }
    }

    /** Uri → JPEG bytes at fixed quality (used for profile pictures, like raw PhotosPicker data). */
    suspend fun uriToJpeg(context: Context, uri: Uri, maxDimension: Int = 1600, quality: Int = 85): ByteArray? {
        val bmp = loadBitmap(context, uri, maxDimension) ?: return null
        return withContext(Dispatchers.Default) { jpeg(bmp, quality) }
    }
}
