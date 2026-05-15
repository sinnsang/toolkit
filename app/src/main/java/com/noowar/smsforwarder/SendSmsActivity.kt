package com.noowar.smsforwarder

import android.app.Activity
import android.os.Bundle

// Required stub for default SMS handler registration (SENDTO intent)
class SendSmsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
