package com.example.sonus

import android.app.Application
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.example.sonus.network.RetrofitClient

class SonusApp : Application() {

    companion object {
        const val CHANNEL_ID = "playback_channel"
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        RetrofitClient.init(this)
        RecentlyPlayedManager.init(this)
        SearchHistoryManager.init(this)
        com.example.sonus.network.WireGuardManager.init(this)

        // Force dark mode for Retro Studio theme globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.playback_channel_name)
            val descriptionText = getString(R.string.playback_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
