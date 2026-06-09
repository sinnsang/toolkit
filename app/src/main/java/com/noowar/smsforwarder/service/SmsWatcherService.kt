package com.noowar.smsforwarder.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.noowar.smsforwarder.R
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

class SmsWatcherService : Service() {

    private val pollerThread = HandlerThread("SmsPoller").also { it.start() }
    private val handler = Handler(pollerThread.looper)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastSmsId = -1L

    private val pollRunnable = object : Runnable {
        override fun run() {
            checkNewSms()
            handler.postDelayed(this, POLL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildNotification())
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("SmsForwarder", "[Watcher] READ_SMS not granted, stopping")
            stopSelf()
            return
        }
        lastSmsId = queryLatestSmsId()
        Log.d("SmsForwarder", "[Watcher] started, lastId=$lastSmsId")
        handler.postDelayed(pollRunnable, POLL_MS)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun checkNewSms() {
        val cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
            null, null,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return

        val id: Long
        val from: String
        val body: String
        cursor.use {
            if (!it.moveToFirst()) return
            id = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms._ID))
            if (id <= lastSmsId) return
            lastSmsId = id
            from = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: "unknown"
            body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""
        }

        Log.d("SmsForwarder", "[Watcher] new SMS from=$from")
        scope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val rules = db.ruleDao().getEnabledRules()
            for (rule in rules) {
                if (!rule.matches(from, body)) {
                    Log.d("SmsForwarder", "[Watcher] skip rule#${rule.id}")
                    continue
                }
                val dedupKey = "${rule.id}:$from:${body.hashCode()}"
                if (!ForwardDedup.claim(dedupKey)) {
                    Log.d("SmsForwarder", "[Watcher] dedup from=$from")
                    continue
                }
                val channel = if (rule.channelType == "TELEGRAM") {
                    val token = AppSettings.getTelegramToken(applicationContext)
                    if (token.isBlank()) {
                        Log.e("SmsForwarder", "[Watcher] Telegram token not set, skip rule#${rule.id}")
                        continue
                    }
                    TelegramChannel(token)
                } else {
                    SmsForwardChannel(applicationContext)
                }
                val text = rule.formatMessage(from, body)
                Log.d("SmsForwarder", "[Watcher] forward from=$from via rule#${rule.id} [${rule.channelType}]")
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

    private fun queryLatestSmsId(): Long {
        val cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms._ID),
            null, null,
            "${Telephony.Sms.DATE} DESC"
        ) ?: return -1L
        return cursor.use { if (it.moveToFirst()) it.getLong(0) else -1L }
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        pollerThread.quitSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "sms_watcher"
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_LOW)
                    .apply { setShowBadge(false) }
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .build()
    }

    companion object {
        private const val POLL_MS = 3000L
        private const val NOTIF_ID = 1
    }
}
