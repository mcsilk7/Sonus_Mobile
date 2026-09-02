package data

import java.util.prefs.Preferences

class DesktopSessionManager {
    private val prefs = Preferences.userRoot().node("com.example.sonus.desktop")

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "role"
    }

    fun saveSession(token: String, username: String, userId: Long, role: String?) {
        prefs.put(KEY_TOKEN, token)
        prefs.put(KEY_USERNAME, username)
        prefs.putLong(KEY_USER_ID, userId)
        role?.let { prefs.put(KEY_ROLE, it) }
        prefs.flush()
    }

    fun getToken(): String? = prefs.get(KEY_TOKEN, null)
    fun getUsername(): String? = prefs.get(KEY_USERNAME, null)
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)
    fun getRole(): String? = prefs.get(KEY_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.remove(KEY_TOKEN)
        prefs.remove(KEY_USERNAME)
        prefs.remove(KEY_USER_ID)
        prefs.remove(KEY_ROLE)
        prefs.flush()
    }
}
