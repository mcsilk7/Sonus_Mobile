package com.example.sonus

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    /**
     * Applies the Retro Studio theme to the given activity.
     * This is the primary theme for the Sonus application.
     */
    fun applyTheme(activity: Activity) {
        // Always use dark mode for the Retro Studio look
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        activity.setTheme(R.style.Theme_Sonus)
    }
}
