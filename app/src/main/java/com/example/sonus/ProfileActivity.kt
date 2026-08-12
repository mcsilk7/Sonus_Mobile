package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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
        fetchUserStats()
        NavigationHelper.setupBottomNav(this)
    }

    private fun initViews() {
        // Powrót
        findViewById<View>(R.id.btnProfileBack).setOnClickListener {
            finish()
        }

        // Edytuj profil
        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
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
    }

    private fun fetchUserStats() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val playlistsDeferred = async { RetrofitClient.playlistApi.getUserPlaylists(userId) }
                val favoritesDeferred = async { RetrofitClient.favoriteApi.getFavorites(userId) }

                val playlistsResponse = playlistsDeferred.await()
                val favoritesResponse = favoritesDeferred.await()

                if (playlistsResponse.isSuccessful) {
                    val count = playlistsResponse.body()?.size ?: 0
                    findViewById<TextView>(R.id.tvStatsPlaylistsCount).text = count.toString()
                }

                if (favoritesResponse.isSuccessful) {
                    val favorites = favoritesResponse.body() ?: emptyList()
                    findViewById<TextView>(R.id.tvStatsFavoritesCount).text = favorites.size.toString()
                    
                    // Sum total hours from favorites as a sample "hours" stat
                    val totalSeconds = favorites.sumOf { it.songDuration ?: 0 }
                    val totalHours = totalSeconds / 3600
                    findViewById<TextView>(R.id.tvStatsHoursCount).text = totalHours.toString()
                }
            } catch (e: Exception) {
                // Ignore errors for stats
            }
        }
    }

    private fun showEditProfileDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etUsername = view.findViewById<EditText>(R.id.etEditUsername)
        val btnCancel = view.findViewById<View>(R.id.btnEditCancel)
        val btnSave = view.findViewById<View>(R.id.btnEditSave)

        etUsername.setText(sessionManager.getUsername())

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newName = etUsername.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateLocalUsername(newName)
                dialog.dismiss()
                Toast.makeText(this, "Profil zaktualizowany", Toast.LENGTH_SHORT).show()
            } else {
                etUsername.error = "Nazwa nie może być pusta"
            }
        }

        dialog.show()
    }

    private fun updateLocalUsername(newName: String) {
        val token = sessionManager.getToken() ?: ""
        val userId = sessionManager.getUserId()
        val role = sessionManager.getRole()
        sessionManager.saveSession(token, newName, userId, role)
        
        displayUserData()
    }

    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
