package com.parkapp.gateway.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.parkapp.gateway.MainActivity
import com.parkapp.gateway.R
import com.parkapp.gateway.network.ApiClient
import com.parkapp.gateway.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GatewayService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val NOTIF_ID = 1001
        const val CHANNEL_ID = "gw_channel"
        private const val TAG = "GatewayService"

        fun start(ctx: Context) {
            val i = Intent(ctx, GatewayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ctx.startForegroundService(i)
            else
                ctx.startService(i)
        }

        fun stop(ctx: Context) = ctx.stopService(Intent(ctx, GatewayService::class.java))
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, buildNotif("Activ – aștept comenzi…"),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            startForeground(NOTIF_ID, buildNotif("Activ – aștept comenzi…"))
        }
        acquireWakeLock()
        startPolling()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Repornește serviciul dacă utilizatorul îl închide din recent apps
        val restart = Intent(applicationContext, GatewayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            applicationContext.startForegroundService(restart)
        else
            applicationContext.startService(restart)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        wakeLock?.release()
        scope.cancel()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ParkApp::GatewayWakeLock"
        ).apply { acquire(10 * 60 * 60 * 1000L) } // max 10 ore
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Polling ────────────────────────────────────────────────────

    private fun startPolling() {
        scope.launch {
            while (true) {
                try {
                    poll()
                } catch (e: Exception) {
                    Log.w(TAG, "Eroare polling: ${e.message}")
                    notify("Eroare conexiune: ${e.message?.take(60)}")
                }
                val interval = Prefs.pollSec(applicationContext)
                delay(interval * 1_000L)
            }
        }
    }

    private suspend fun poll() {
        val url    = Prefs.serverUrl(applicationContext)
        val secret = Prefs.secret(applicationContext)
        if (url.isEmpty() || secret.isEmpty()) return

        val api      = ApiClient.get(url)
        val response = api.getNextTask(secret)
        val task     = response.task ?: return

        Log.i(TAG, "Task primit: id=${task.id}, telefon=${task.gate_phone}")
        notify("Apelând poarta…")

        makeCall(task.gate_phone)
        delay(4_000)   // așteptăm să pornească apelul

        try {
            api.markDone(task.id, secret)
            Log.i(TAG, "Task #${task.id} marcat done")
        } catch (e: Exception) {
            Log.e(TAG, "Nu s-a putut marca task done: ${e.message}")
            runCatching { api.markFailed(task.id, secret) }
        }
        notify("Apel efectuat – aștept comenzi…")
    }

    private fun makeCall(phone: String) {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // ─── Notificări ─────────────────────────────────────────────────

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Gateway Poartă",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Serviciu de monitorizare comenzi poartă" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotif(text: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ParkApp Gateway")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun notify(text: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotif(text))
    }
}
