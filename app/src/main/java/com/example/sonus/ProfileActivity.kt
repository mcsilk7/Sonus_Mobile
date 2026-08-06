package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sonus.network.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        NavigationHelper.setupBottomNav(this)

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
            sessionManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
