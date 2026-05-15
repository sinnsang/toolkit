package com.noowar.smsforwarder.channel

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class TelegramChannel(private val token: String) : ForwardChannel {

    override suspend fun send(message: ForwardMessage): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.telegram.org/bot$token/sendMessage")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.doOutput = true

            val body = JSONObject().apply {
                put("chat_id", message.destination)
                put("text", message.text)
            }.toString().toByteArray(Charsets.UTF_8)

            conn.outputStream.use { it.write(body) }

            val code = conn.responseCode
            Log.d("SmsForwarder", "[Telegram] HTTP $code to ${message.destination}")
            if (code == 200) Result.success(Unit)
            else {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: "unknown"
                Result.failure(Exception("HTTP $code: $err"))
            }
        } catch (e: Exception) {
            Log.e("SmsForwarder", "[Telegram] failed: ${e.message}")
            Result.failure(e)
        }
    }
}
