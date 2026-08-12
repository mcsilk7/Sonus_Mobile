package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.sonus.network.SessionManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sessionManager = SessionManager(this)
        NavigationHelper.setupBottomNav(this)

        displayUserData()
        updateThemeText()

        // Profil
        findViewById<View>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Motyw
        findViewById<View>(R.id.btnTheme).setOnClickListener {
            showThemeSelectionDialog()
        }

        // Wyloguj
        findViewById<View>(R.id.btnLogout).setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun displayUserData() {
        val username = sessionManager.getUsername() ?: "Użytkownik"
        val role = sessionManager.getRole() ?: "USER"

        findViewById<TextView>(R.id.tvSettingsName).text = username
        findViewById<TextView>(R.id.tvSettingsEmail).text = "$username ($role)"

        val avatar = findViewById<TextView>(R.id.tvSettingsAvatar)
        avatar.text = username.take(1).uppercase()
    }

    private fun updateThemeText() {
        val themeText = findViewById<TextView>(R.id.tvCurrentTheme)
        val mode = sessionManager.getTheme()
        themeText.text = when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Jasny"
            AppCompatDelegate.MODE_NIGHT_YES -> "Ciemny"
            else -> "Podążaj za systemem"
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Jasny", "Ciemny", "Podążaj za systemem")
        val checkedItem = when (sessionManager.getTheme()) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        AlertDialog.Builder(this)
            .setTitle("Wybierz motyw")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val mode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                sessionManager.saveTheme(mode)
                AppCompatDelegate.setDefaultNightMode(mode)
                updateThemeText()
                dialog.dismiss()
            }
            .show()
    }
}

