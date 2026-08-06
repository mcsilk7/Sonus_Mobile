package com.example.sonus.network

import android.content.Context

class SessionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "sonus_session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USERNAME = "username"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(token: String, username: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
