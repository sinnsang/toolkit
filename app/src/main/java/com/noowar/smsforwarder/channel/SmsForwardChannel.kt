package com.noowar.smsforwarder.channel

import android.content.Context
import android.telephony.SmsManager
import android.util.Log

class SmsForwardChannel(private val context: Context) : ForwardChannel {

    override suspend fun send(message: ForwardMessage): Result<Unit> {
        if (message.destination.isBlank()) {
            return Result.failure(IllegalStateException("목적지 번호 미설정"))
        }
        return try {
            @Suppress("DEPRECATION")
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message.text)
            if (parts.size == 1) {
                smsManager.sendTextMessage(message.destination, null, message.text, null, null)
            } else {
                smsManager.sendMultipartTextMessage(message.destination, null, parts, null, null)
            }
            Log.d("SmsForwarder", "[Forward] sent to ${message.destination}: ${message.text}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SmsForwarder", "[Forward] failed: ${e.message}")
            Result.failure(e)
        }
    }
}
