package com.noowar.smsforwarder.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class VersionInfo(
    val minVersionCode: Int,
    val latestVersionCode: Int,
    val updateUrl: String
)

object VersionCheck {
    const val VERSION_JSON_URL = "https://sinnsang.github.io/toolkit/version.json"

    suspend fun fetch(url: String = VERSION_JSON_URL): VersionInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            if (conn.responseCode != 200) return@withContext null
            val body = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(body)
            VersionInfo(
                minVersionCode = obj.getInt("min_version_code"),
                latestVersionCode = obj.getInt("latest_version_code"),
                updateUrl = obj.getString("update_url")
            )
        } catch (_: Exception) {
            null // network unavailable or parse error — skip check silently
        }
    }
}
