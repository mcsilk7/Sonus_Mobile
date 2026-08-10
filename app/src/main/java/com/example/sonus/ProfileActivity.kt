package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sonus.network.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        
        // Sesja: jeśli nie zalogowany, wróć do logowania
        if (!sessionManager.isLoggedIn()) {
            logout()
            return
        }

        initViews()
        displayUserData()
        NavigationHelper.setupBottomNav(this)
    }

    private fun initViews() {
        // Powrót
        findViewById<View>(R.id.btnProfileBack).setOnClickListener {
            finish()
        }

        // Ustawienia
        findViewById<View>(R.id.btnProfileSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Wyloguj
        findViewById<View>(R.id.btnProfileLogout).setOnClickListener {
            logout()
        }
    }

    private fun displayUserData() {
        val username = sessionManager.getUsername() ?: "Użytkownik"
        val role = sessionManager.getRole() ?: "USER"
        
        findViewById<TextView>(R.id.tvProfileName).text = username
        findViewById<TextView>(R.id.tvProfileEmail).text = "$username ($role)" 
        
        // Ustawienie pierwszej litery w awatarze
        val avatar = findViewById<TextView>(R.id.profileAvatar)
        avatar.text = username.take(1).uppercase()
        
        // Można tu też dodać pobieranie statystyk z API w przyszłości
    }

    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
