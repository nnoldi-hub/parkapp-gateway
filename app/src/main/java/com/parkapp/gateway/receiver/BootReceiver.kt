package com.parkapp.gateway.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.parkapp.gateway.service.GatewayService
import com.parkapp.gateway.util.Prefs

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Repornire automată dacă serviciul era activ înainte de restart
            if (Prefs.isServiceOn(context) && Prefs.serverUrl(context).isNotEmpty()) {
                GatewayService.start(context)
            }
        }
    }
}
