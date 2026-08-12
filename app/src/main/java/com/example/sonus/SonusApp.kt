package com.example.sonus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager

class SonusApp : Application() {

    companion object {
        const val CHANNEL_ID = "playback_channel"
    }

    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
        
        val sessionManager = SessionManager(this)
        val themeMode = sessionManager.getTheme()
        if (themeMode != -1) {
            AppCompatDelegate.setDefaultNightMode(themeMode)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Odtwarzanie muzyki"
            val descriptionText = "Powiadomienie o aktualnie odtwarzanym utworze"
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

