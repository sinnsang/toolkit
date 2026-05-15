package com.noowar.smsforwarder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

// Required stub for default SMS handler registration (RESPOND_VIA_MESSAGE)
class HeadlessSmsSendService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelfResult(startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
