package com.example.sonus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager
    private lateinit var rvRecentlyPlayed: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var tvRecentlyPlayedHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()

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

        recentlyPlayedManager = RecentlyPlayedManager(this)
        rvRecentlyPlayed = findViewById(R.id.rvRecentlyPlayed)
        tvRecentlyPlayedHeader = findViewById(R.id.tvRecentlyPlayedHeader)
        
        initMiniPlayer()
        setupRecentlyPlayed()

        // Wyłączamy klawiaturę na tym ekranie
        etSearch.isFocusable = false
        etSearch.isCursorVisible = false

        NavigationHelper.setupBottomNav(this)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Brak powiadomień uniemożliwi sterowanie muzyką w tle", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        initMiniPlayer()
        updateRecentlyPlayed()
    }

    override fun onDestroy() {
        super.onDestroy()
        MiniPlayerHelper.onDestroy(this)
    }

    private fun setupRecentlyPlayed() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                playSong(song)
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(this, lifecycleScope, song) {
                    songAdapter.notifyDataSetChanged()
                }
            }
        )
        rvRecentlyPlayed.layoutManager = LinearLayoutManager(this)
        rvRecentlyPlayed.adapter = songAdapter
    }

    private fun initMiniPlayer() {
        MiniPlayerHelper.setupMiniPlayer(this)
    }

    private fun playSong(song: SongDTO) {
        val recentSongs = recentlyPlayedManager.getRecentSongs()
        recentlyPlayedManager.addSong(song)
        updateRecentlyPlayed()
        
        PlayerState.play(this, song, recentSongs)
        initMiniPlayer()
        
        Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
    }

    private fun updateRecentlyPlayed() {
        val userId = sessionManager.getUserId()
        val recentSongs = recentlyPlayedManager.getRecentSongs()
        if (recentSongs.isEmpty()) {
            tvRecentlyPlayedHeader.visibility = View.GONE
            rvRecentlyPlayed.visibility = View.GONE
        } else {
            tvRecentlyPlayedHeader.visibility = View.VISIBLE
            rvRecentlyPlayed.visibility = View.VISIBLE

            if (userId != -1L) {
                lifecycleScope.launch {
                    try {
                        val favoritesDeferred = async { RetrofitClient.favoriteApi.getFavorites(userId) }
                        val playlistSongsIdsDeferred = async { PlaylistHelper.getAllSongsInUserPlaylists(userId) }

                        val favoritesResponse = favoritesDeferred.await()
                        val playlistSongsIds = playlistSongsIdsDeferred.await()

                        if (favoritesResponse.isSuccessful) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            recentSongs.forEach { song ->
                                song.isFavorite = favoriteIds.contains(song.id)
                            }
                        }

                        PlaylistHelper.enrichSongsWithPlaylistState(recentSongs, playlistSongsIds)
                        
                        songAdapter.updateData(recentSongs)
                    } catch (e: Exception) {
                        songAdapter.updateData(recentSongs)
                    }
                }
            } else {
                songAdapter.updateData(recentSongs)
            }
        }
    }
}
