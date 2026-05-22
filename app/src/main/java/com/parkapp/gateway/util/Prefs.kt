package com.parkapp.gateway.util

import android.content.Context

object Prefs {
    private const val NAME = "gw_prefs"
    const val KEY_SERVER_URL = "server_url"
    const val KEY_SECRET = "gateway_secret"
    const val KEY_POLL_SEC = "poll_seconds"
    const val KEY_SIM_SLOT = "sim_slot"       // 0=auto, 1=SIM1, 2=SIM2
    const val KEY_SERVICE_ON = "service_on"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun serverUrl(ctx: Context): String    = prefs(ctx).getString(KEY_SERVER_URL, "") ?: ""
    fun secret(ctx: Context): String       = prefs(ctx).getString(KEY_SECRET, "") ?: ""
    fun pollSec(ctx: Context): Int         = prefs(ctx).getInt(KEY_POLL_SEC, 15)
    fun simSlot(ctx: Context): Int         = prefs(ctx).getInt(KEY_SIM_SLOT, 0)
    fun isServiceOn(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_SERVICE_ON, false)

    fun save(ctx: Context, url: String, secret: String, pollSec: Int, simSlot: Int) {
        prefs(ctx).edit()
            .putString(KEY_SERVER_URL, url.trim())
            .putString(KEY_SECRET, secret.trim())
            .putInt(KEY_POLL_SEC, pollSec.coerceIn(5, 300))
            .putInt(KEY_SIM_SLOT, simSlot)
            .apply()
    }

    fun setServiceOn(ctx: Context, on: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_SERVICE_ON, on).apply()
    }
}
