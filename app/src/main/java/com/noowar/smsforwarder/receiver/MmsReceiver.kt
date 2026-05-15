package com.noowar.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Required stub for default SMS handler registration (WAP_PUSH_DELIVER)
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MMS not handled in this version
    }
}
