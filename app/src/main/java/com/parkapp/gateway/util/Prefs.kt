package com.parkapp.gateway.util

import android.content.Context

object Prefs {
    private const val NAME = "gw_prefs"
    const val KEY_SERVER_URL = "server_url"
    const val KEY_SECRET = "gateway_secret"
    const val KEY_POLL_SEC = "poll_seconds"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun serverUrl(ctx: Context): String = prefs(ctx).getString(KEY_SERVER_URL, "") ?: ""
    fun secret(ctx: Context): String    = prefs(ctx).getString(KEY_SECRET, "") ?: ""
    fun pollSec(ctx: Context): Int      = prefs(ctx).getInt(KEY_POLL_SEC, 15)

    fun save(ctx: Context, url: String, secret: String, pollSec: Int) {
        prefs(ctx).edit()
            .putString(KEY_SERVER_URL, url.trim())
            .putString(KEY_SECRET, secret.trim())
            .putInt(KEY_POLL_SEC, pollSec.coerceIn(5, 300))
            .apply()
    }
}
