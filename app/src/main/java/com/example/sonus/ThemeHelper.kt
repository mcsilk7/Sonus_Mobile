package com.example.sonus

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    /**
     * Applies the Retro Studio theme to the given activity.
     * This is the primary theme for the Sonus application.
     */
    fun applyTheme(activity: Activity) {
        val settingsManager = SettingsManager(activity)
        val themeId = settingsManager.getThemeId()

        when (themeId) {
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                activity.setTheme(R.style.Theme_Sonus_Industrial_Dark)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                activity.setTheme(R.style.Theme_Sonus_Industrial_Light)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                activity.setTheme(R.style.Theme_Sonus)
            }
        }
    }
}
