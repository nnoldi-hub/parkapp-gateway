package com.parkapp.gateway

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.parkapp.gateway.databinding.ActivityMainBinding
import com.parkapp.gateway.service.GatewayService
import com.parkapp.gateway.util.Prefs

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var serviceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        loadPrefs()
        requestNeededPermissions()

        b.btnSave.setOnClickListener {
            if (savePrefs()) Toast.makeText(this, "Setări salvate", Toast.LENGTH_SHORT).show()
        }

        b.btnToggle.setOnClickListener {
            if (serviceRunning) {
                GatewayService.stop(this)
                serviceRunning = false
            } else {
                if (savePrefs()) {
                    GatewayService.start(this)
                    serviceRunning = true
                }
            }
            updateUi()
        }
    }

    private fun loadPrefs() {
        b.editUrl.setText(Prefs.serverUrl(this))
        b.editSecret.setText(Prefs.secret(this))
        b.editPoll.setText(Prefs.pollSec(this).toString())
    }

    private fun savePrefs(): Boolean {
        val url    = b.editUrl.text.toString().trim()
        val secret = b.editSecret.text.toString().trim()
        val poll   = b.editPoll.text.toString().toIntOrNull() ?: 15

        if (url.isEmpty() || secret.isEmpty()) {
            Toast.makeText(this, "URL server și secret sunt obligatorii", Toast.LENGTH_SHORT).show()
            return false
        }
        Prefs.save(this, url, secret, poll)
        return true
    }

    private fun updateUi() {
        if (serviceRunning) {
            b.tvStatus.text = "● Serviciu ACTIV"
            b.tvStatus.setTextColor(getColor(R.color.status_running))
            b.btnToggle.text = "Oprește serviciul"
        } else {
            b.tvStatus.text = "● Serviciu OPRIT"
            b.tvStatus.setTextColor(getColor(R.color.status_stopped))
            b.btnToggle.text = "Pornește serviciul"
        }
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.CALL_PHONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        if (needed.isNotEmpty())
            requestPermissions(needed.toTypedArray(), 42)
    }
}
