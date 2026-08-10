package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.launch

class LibraryActivity : AppCompatActivity() {

    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var favoriteSongAdapter: SongAdapter
    private lateinit var albumAdapter: AlbumAdapter
    
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var rvFavoriteSongs: RecyclerView
    private lateinit var rvAlbums: RecyclerView
    
    private lateinit var btnAddPlaylist: LinearLayout
    private lateinit var sessionManager: SessionManager

    private lateinit var sectionPlaylists: View
    private lateinit var sectionAlbums: View
    private lateinit var sectionFavorites: View

    private lateinit var tabAll: TextView
    private lateinit var tabPlaylists: TextView
    private lateinit var tabAlbums: TextView
    private lateinit var tabFavorites: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        sessionManager = SessionManager(this)
        initViews()
        setupRecyclerViews()
        setupTabs()
        fetchData()

        NavigationHelper.setupBottomNav(this)
    }

    private fun initViews() {
        rvPlaylists = findViewById(R.id.rvPlaylists)
        rvFavoriteSongs = findViewById(R.id.rvFavoriteSongs)
        rvAlbums = findViewById(R.id.rvAlbums)
        btnAddPlaylist = findViewById(R.id.btnAddPlaylist)
        
        sectionPlaylists = findViewById(R.id.sectionPlaylists)
        sectionAlbums = findViewById(R.id.sectionAlbums)
        sectionFavorites = findViewById(R.id.sectionFavorites)

        tabAll = findViewById(R.id.tabAll)
        tabPlaylists = findViewById(R.id.tabPlaylists)
        tabAlbums = findViewById(R.id.tabAlbums)
        tabFavorites = findViewById(R.id.tabFavorites)

        btnAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }

    private fun setupTabs() {
        tabAll.setOnClickListener { updateTabSelection(it as TextView); showSections(true, true, true) }
        tabPlaylists.setOnClickListener { updateTabSelection(it as TextView); showSections(true, false, false) }
        tabAlbums.setOnClickListener { updateTabSelection(it as TextView); showSections(false, true, false) }
        tabFavorites.setOnClickListener { updateTabSelection(it as TextView); showSections(false, false, true) }
    }

    private fun updateTabSelection(selectedTab: TextView) {
        val tabs = listOf(tabAll, tabPlaylists, tabAlbums, tabFavorites)
        tabs.forEach { tab ->
            if (tab == selectedTab) {
                tab.setBackgroundResource(R.drawable.bg_library_tab_active)
                tab.setTextColor(ContextCompat.getColor(this, R.color.app_background))
            } else {
                tab.setBackgroundResource(R.drawable.bg_library_tab)
                tab.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
            }
        }
    }

    private fun showSections(playlists: Boolean, albums: Boolean, favorites: Boolean) {
        sectionPlaylists.visibility = if (playlists) View.VISIBLE else View.GONE
        sectionAlbums.visibility = if (albums) View.VISIBLE else View.GONE
        sectionFavorites.visibility = if (favorites) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerViews() {
        // Playlists
        playlistAdapter = PlaylistAdapter(emptyList()) { playlist ->
            val intent = Intent(this, PlaylistDetailActivity::class.java)
            intent.putExtra("PLAYLIST_ID", playlist.id)
            startActivity(intent)
        }
        rvPlaylists.layoutManager = LinearLayoutManager(this)
        rvPlaylists.adapter = playlistAdapter

        // Favorites
        favoriteSongAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = { song ->
                // Handled in search, but could be here too
            },
            onFavoriteClick = { song ->
                toggleFavorite(song)
            }
        )
        rvFavoriteSongs.layoutManager = LinearLayoutManager(this)
        rvFavoriteSongs.adapter = favoriteSongAdapter

        // Albums (Grid)
        albumAdapter = AlbumAdapter(
            albums = emptyList(),
            onItemClick = { album ->
                val intent = Intent(this, AlbumDetailActivity::class.java)
                intent.putExtra("ALBUM_ID", album.id)
                startActivity(intent)
            },
            onAddClick = null // No "+" button in library
        )
        rvAlbums.layoutManager = GridLayoutManager(this, 2)
        rvAlbums.adapter = albumAdapter
    }

    private fun fetchData() {
        fetchPlaylists()
        fetchFavorites()
        fetchFavoriteAlbums()
    }

    private fun fetchPlaylists() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.getUserPlaylists(userId)
                if (response.isSuccessful) {
                    response.body()?.let {
                        playlistAdapter.updateData(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error fetching playlists", e)
            }
        }
    }

    private fun fetchFavorites() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.favoriteApi.getFavorites(userId)
                if (response.isSuccessful) {
                    val favorites = response.body() ?: emptyList()
                    val songs = favorites.mapNotNull { it.song }.onEach { it.isFavorite = true }
                    favoriteSongAdapter.updateData(songs)
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error fetching favorites", e)
            }
        }
    }

    private fun fetchFavoriteAlbums() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.albumApi.getLibraryAlbums(userId)
                if (response.isSuccessful) {
                    val albums = response.body() ?: emptyList()
                    albumAdapter.updateData(albums)
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error fetching favorite albums", e)
            }
        }
    }

    private fun toggleFavorite(song: SongDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.favoriteApi.toggleFavorite(userId, song.id)
                if (response.isSuccessful) {
                    fetchFavorites()
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error toggling favorite", e)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val input = EditText(this)
        input.hint = "Nazwa playlisty"
        
        AlertDialog.Builder(this)
            .setTitle("Nowa playlista")
            .setView(input)
            .setPositiveButton("Stwórz") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    createPlaylist(name)
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun createPlaylist(name: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "Musisz być zalogowany, aby utworzyć playlistę", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.createPlaylist(userId, PlaylistDTO(name = name))
                if (response.isSuccessful) {
                    Toast.makeText(this@LibraryActivity, "Utworzono playlistę", Toast.LENGTH_SHORT).show()
                    fetchPlaylists()
                } else {
                    val message = response.errorBody()?.string().takeIf { !it.isNullOrBlank() } ?: "Błąd tworzenia playlisty"
                    Toast.makeText(this@LibraryActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LibraryActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}