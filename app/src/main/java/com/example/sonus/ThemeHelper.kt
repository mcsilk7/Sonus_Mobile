package com.example.sonus

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sonus.network.SessionManager

object ThemeHelper {
    fun applyTheme(activity: Activity) {
        val sessionManager = SessionManager(activity)
        val theme = sessionManager.getTheme()
        
        when (theme) {
            0 -> { // Light
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            1 -> { // Dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            2 -> { // Violet
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                activity.setTheme(R.style.Theme_Sonus_Violet)
            }
            else -> { // System
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
}
