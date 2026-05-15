package com.noowar.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION &&
            intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages == null || messages.isEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val rules = db.ruleDao().getEnabledRules()
                for (message in messages) {
                    val from = message.originatingAddress ?: "unknown"
                    val body = message.messageBody ?: ""
                    for (rule in rules) {
                        if (!rule.matches(from, body)) {
                            Log.d("SmsForwarder", "[SMS] skip rule#${rule.id} from=$from")
                            continue
                        }
                        val dedupKey = "${rule.id}:$from:${body.hashCode()}"
                        if (!ForwardDedup.claim(dedupKey)) {
                            Log.d("SmsForwarder", "[SMS] dedup from=$from")
                            continue
                        }
                        val channel = if (rule.channelType == "TELEGRAM") {
                            val token = AppSettings.getTelegramToken(context)
                            if (token.isBlank()) {
                                Log.e("SmsForwarder", "[SMS] Telegram token not set, skip rule#${rule.id}")
                                continue
                            }
                            TelegramChannel(token)
                        } else {
                            SmsForwardChannel(context)
                        }
                        val text = rule.formatMessage(from, body)
                        Log.d("SmsForwarder", "[SMS] forward from=$from via rule#${rule.id} [${rule.channelType}]")
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
            } finally {
                pendingResult.finish()
            }
        }
    }

}
