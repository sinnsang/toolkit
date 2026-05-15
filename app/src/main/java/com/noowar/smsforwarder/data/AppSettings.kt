package com.noowar.smsforwarder.data

import android.content.Context

object AppSettings {
    private const val PREFS = "app_settings"
    private const val KEY_TG_TOKEN = "telegram_bot_token"

    fun getTelegramToken(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TG_TOKEN, "") ?: ""

    fun setTelegramToken(context: Context, token: String) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TG_TOKEN, token).apply()
}
