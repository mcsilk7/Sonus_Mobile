package com.example.sonus.network

import android.content.Intent
import android.net.VpnService
import android.util.Log

class SonusVpnService : VpnService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("SonusVPN", "SonusVpnService started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("SonusVPN", "SonusVpnService destroyed")
    }
}
