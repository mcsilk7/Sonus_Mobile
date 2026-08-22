package com.example.sonus

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sonus_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REELS_ENABLED = "reels_enabled"
        private const val KEY_THEME_ID = "app_theme_id"
    }

    fun isReelsEnabled(): Boolean = prefs.getBoolean(KEY_REELS_ENABLED, true)

    fun setReelsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REELS_ENABLED, enabled).apply()
    }

    fun getThemeId(): Int = prefs.getInt(KEY_THEME_ID, 0) // 0: AMBER, 1: INDUSTRIAL_DARK

    fun setThemeId(themeId: Int) {
        prefs.edit().putInt(KEY_THEME_ID, themeId).apply()
    }

    fun getSortOrder(contextKey: String): SortOrder {
        val name = prefs.getString("sort_order_$contextKey", SortOrder.DEFAULT.name)
        return try {
            SortOrder.valueOf(name ?: SortOrder.DEFAULT.name)
        } catch (e: Exception) {
            SortOrder.DEFAULT
        }
    }

    fun setSortOrder(contextKey: String, order: SortOrder) {
        prefs.edit().putString("sort_order_$contextKey", order.name).apply()
    }
}
