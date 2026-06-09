package com.noowar.smsforwarder.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.noowar.smsforwarder.channel.ForwardDedup
import com.noowar.smsforwarder.channel.ForwardMessage
import com.noowar.smsforwarder.channel.SmsForwardChannel
import com.noowar.smsforwarder.channel.TelegramChannel
import com.noowar.smsforwarder.data.AppDatabase
import com.noowar.smsforwarder.data.AppSettings
import com.noowar.smsforwarder.data.ForwardLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsNotificationListener : NotificationListenerService() {

    private val processed = mutableSetOf<String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != SAMSUNG_MESSAGES_PKG) return
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val key = "${sbn.key}:${sbn.postTime}"
        if (!processed.add(key)) return
        if (processed.size > 100) processed.clear()

        val extras = sbn.notification.extras
        val displayName = extras.getString(Notification.EXTRA_TITLE) ?: return
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: return

        scope.launch {
            val from = displayName
            val db = AppDatabase.getInstance(applicationContext)
            val rules = db.ruleDao().getEnabledRules()

            for (rule in rules) {
                if (!rule.matches(from, body)) {
                    Log.d("SmsForwarder", "[SMS/N] skip rule#${rule.id} from=$displayName($from)")
                    continue
                }
                val dedupKey = "${rule.id}:$from:${body.hashCode()}"
                if (!ForwardDedup.claim(dedupKey)) {
                    Log.d("SmsForwarder", "[SMS/N] dedup from=$displayName")
                    continue
                }
                val channel = if (rule.channelType == "TELEGRAM") {
                    val token = AppSettings.getTelegramToken(applicationContext)
                    if (token.isBlank()) {
                        Log.e("SmsForwarder", "[SMS/N] Telegram token not set, skip rule#${rule.id}")
                        continue
                    }
                    TelegramChannel(token)
                } else {
                    SmsForwardChannel(applicationContext)
                }
                val text = rule.formatMessage(from, body)
                Log.d("SmsForwarder", "[SMS/N] forward from=$displayName via rule#${rule.id} [${rule.channelType}]")
                val result = channel.send(ForwardMessage(rule.destination, text))
                db.forwardLogDao().insert(
                    ForwardLog(
                        timestamp = System.currentTimeMillis(),
                        fromNumber = from,
                        toNumber = rule.destination,
                        body = body.take(200),
                        ruleId = rule.id,
                        success = result.isSuccess,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                )
            }
        }
    }

    companion object {
        private const val SAMSUNG_MESSAGES_PKG = "com.samsung.android.messaging"
    }
}
