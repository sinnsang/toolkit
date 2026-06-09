package com.noowar.smsforwarder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.noowar.smsforwarder.R

class SmsWatcherService : Service() {

    private val pollerThread = HandlerThread("SmsPoller").also { it.start() }
    private val handler = Handler(pollerThread.looper)
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

        cursor.use {
            if (!it.moveToFirst()) return
            val id = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms._ID))
            if (id <= lastSmsId) return
            lastSmsId = id
            val from = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: "unknown"
            val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""
            Log.d("SmsForwarder", "[SMS] from=$from, body=$body")
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
