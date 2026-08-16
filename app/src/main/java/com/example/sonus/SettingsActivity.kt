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
        ThemeHelper.applyTheme(this)
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
        themeText.text = when (sessionManager.getTheme()) {
            0 -> "Jasny"
            1 -> "Ciemny"
            2 -> "Fioletowy"
            else -> "Podążaj za systemem"
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Jasny", "Ciemny", "Fioletowy", "Podążaj za systemem")
        val currentTheme = sessionManager.getTheme()
        val checkedItem = when (currentTheme) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> 3
        }

        AlertDialog.Builder(this)
            .setTitle("Wybierz motyw")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> 0 // Light
                    1 -> 1 // Dark
                    2 -> 2 // Violet
                    else -> -1 // System
                }
                sessionManager.saveTheme(selectedTheme)
                ThemeHelper.applyTheme(this)
                updateThemeText()
                dialog.dismiss()
                recreate()
            }
            .show()
    }
}

