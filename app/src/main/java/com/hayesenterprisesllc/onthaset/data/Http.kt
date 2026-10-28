package com.hayesenterprisesllc.onthaset.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class HttpResult(val code: Int, val body: String) {
    val isSuccess: Boolean get() = code in 200..299
    fun jsonArray(): JSONArray = runCatching { JSONArray(body) }.getOrElse { JSONArray() }
    fun jsonObject(): JSONObject = runCatching { JSONObject(body) }.getOrElse { JSONObject() }
}

class HttpException(val code: Int, message: String) : Exception(message)

/** Thin OkHttp wrapper used in place of URLSession. */
object Http {
    private const val TAG = "OnThaSet"

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun enc(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    /** Headers every Supabase REST call in the iOS app sent. */
    private fun Request.Builder.supabaseHeaders(bearer: String = SupabaseConfig.ANON_KEY): Request.Builder =
        header("Content-Type", "application/json")
            .header("apikey", SupabaseConfig.ANON_KEY)
            .header("Authorization", "Bearer $bearer")

    suspend fun execute(request: Request): HttpResult = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { resp ->
            HttpResult(resp.code, resp.body?.string().orEmpty())
        }
    }

    /** GET a Supabase REST path, e.g. "events?date=gte.xxx" */
    suspend fun restGet(pathAndQuery: String): HttpResult = execute(
        Request.Builder().url("${SupabaseConfig.PROJECT_URL}/rest/v1/$pathAndQuery")
            .supabaseHeaders().get().build()
    )

    suspend fun restPost(path: String, body: JSONObject, preferMinimal: Boolean = true): HttpResult =
        execute(
            Request.Builder().url("${SupabaseConfig.PROJECT_URL}/rest/v1/$path")
                .supabaseHeaders()
                .apply { if (preferMinimal) header("Prefer", "return=minimal") }
                .post(body.toString().toRequestBody(JSON)).build()
        )

    suspend fun restPatch(pathAndQuery: String, body: JSONObject, returnRepresentation: Boolean = false): HttpResult = execute(
        Request.Builder().url("${SupabaseConfig.PROJECT_URL}/rest/v1/$pathAndQuery")
            .supabaseHeaders().header("Prefer", if (returnRepresentation) "return=representation" else "return=minimal")
            .patch(body.toString().toRequestBody(JSON)).build()
    )

    suspend fun restDelete(pathAndQuery: String): HttpResult = execute(
        Request.Builder().url("${SupabaseConfig.PROJECT_URL}/rest/v1/$pathAndQuery")
            .supabaseHeaders().delete().build()
    )

    suspend fun storageUpload(bucket: String, fileName: String, data: ByteArray, contentType: String = "image/jpeg"): HttpResult =
        execute(
            Request.Builder().url("${SupabaseConfig.PROJECT_URL}/storage/v1/object/$bucket/$fileName")
                .header("apikey", SupabaseConfig.ANON_KEY)
                .header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .header("x-upsert", "true")
                .post(data.toRequestBody(contentType.toMediaType())).build()
        )

    suspend fun storageDelete(bucket: String, fileName: String): HttpResult = execute(
        Request.Builder().url("${SupabaseConfig.PROJECT_URL}/storage/v1/object/$bucket/$fileName")
            .header("apikey", SupabaseConfig.ANON_KEY)
            .header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
            .delete().build()
    )

    /** Supabase GoTrue auth endpoints. */
    suspend fun authPost(pathAndQuery: String, body: JSONObject, bearer: String? = null): HttpResult = execute(
        Request.Builder().url("${SupabaseConfig.PROJECT_URL}/auth/v1/$pathAndQuery")
            .header("Content-Type", "application/json")
            .header("apikey", SupabaseConfig.ANON_KEY)
            .apply { if (bearer != null) header("Authorization", "Bearer $bearer") }
            .post(body.toString().toRequestBody(JSON)).build()
    )

    suspend fun authGet(path: String, bearer: String): HttpResult = execute(
        Request.Builder().url("${SupabaseConfig.PROJECT_URL}/auth/v1/$path")
            .header("apikey", SupabaseConfig.ANON_KEY)
            .header("Authorization", "Bearer $bearer")
            .get().build()
    )

    suspend fun getString(url: String): HttpResult = execute(Request.Builder().url(url).get().build())

    suspend fun getBytes(url: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(Request.Builder().url(url).get().build()).execute().use { r ->
                if (r.isSuccessful) r.body?.bytes() else null
            }
        }.getOrNull()
    }

    fun emptyBody(): RequestBody = ByteArray(0).toRequestBody(null)

    fun log(msg: String) = Log.d(TAG, msg)
}
