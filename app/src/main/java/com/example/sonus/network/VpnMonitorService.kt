package com.example.sonus.network

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class VpnMonitorService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("SonusVPN", "Aplikacja została usunięta z listy zadań. Zamykanie VPN...")
        WireGuardManager.stopVpnSync()
        stopSelf()
    }
}
