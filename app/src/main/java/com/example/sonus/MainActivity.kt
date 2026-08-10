package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.sonus.network.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val searchBar = findViewById<View>(R.id.searchBar)
        val etSearch = searchBar.findViewById<EditText>(R.id.etSearch)
        val ivSearchIcon = searchBar.findViewById<View>(R.id.ivSearchIcon)
        val cardFavorites = findViewById<View>(R.id.cardFavorites)
        val cardPlaylists = findViewById<View>(R.id.cardPlaylists)

        // Cały pasek (w tym ikona i tekst) przenosi do wyszukiwarki
        val goToSearch = View.OnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Karty przenoszą do biblioteki
        val goToLibrary = View.OnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }

        searchBar.setOnClickListener(goToSearch)
        etSearch.setOnClickListener(goToSearch)
        ivSearchIcon.setOnClickListener(goToSearch)

        cardFavorites.setOnClickListener(goToLibrary)
        cardPlaylists.setOnClickListener(goToLibrary)

        // Wyłączamy klawiaturę na tym ekranie
        etSearch.isFocusable = false
        etSearch.isCursorVisible = false

        NavigationHelper.setupBottomNav(this)
    }
}