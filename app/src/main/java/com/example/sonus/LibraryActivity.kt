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
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager

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
        recentlyPlayedManager = RecentlyPlayedManager(this)
        initViews()
        setupRecyclerViews()
        setupTabs()

        NavigationHelper.setupBottomNav(this)
    }

    override fun onResume() {
        super.onResume()
        fetchData()
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
            intent.putExtra("PLAYLIST_ID", playlist.id ?: -1L)
            startActivity(intent)
        }
        rvPlaylists.layoutManager = LinearLayoutManager(this)
        rvPlaylists.adapter = playlistAdapter

        // Favorites
        favoriteSongAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                recentlyPlayedManager.addSong(song)
                Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(this, lifecycleScope, song) {
                    favoriteSongAdapter.notifyDataSetChanged()
                    fetchPlaylists() // Refresh playlist counts
                }
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
            onAddClick = { album ->
                toggleAlbumLibrary(album)
            }
        )
        rvAlbums.layoutManager = GridLayoutManager(this, 2)
        rvAlbums.adapter = albumAdapter
    }

    private fun fetchData() {
        val userId = sessionManager.getUserId()
        Log.d("SonusLibrary", "Fetching data for userId: $userId")
        if (userId == -1L) {
            Toast.makeText(this, "Błąd sesji: Nie znaleziono ID użytkownika", Toast.LENGTH_LONG).show()
            return
        }
        fetchPlaylists()
        fetchFavorites()
        fetchFavoriteAlbums()
    }

    private fun fetchPlaylists() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.getUserPlaylists(userId)
                if (response.isSuccessful) {
                    val playlists = response.body() ?: emptyList()
                    Log.d("SonusLibrary", "Fetched ${playlists.size} playlists")
                    playlistAdapter.updateData(playlists)
                    
                    // Fetch song counts for playlists using the dedicated endpoint
                    playlists.forEachIndexed { index, playlist ->
                        val pid = playlist.id ?: return@forEachIndexed
                        launch {
                            try {
                                val countResponse = RetrofitClient.playlistApi.getSongCountInPlaylist(pid)
                                if (countResponse.isSuccessful) {
                                    val count = countResponse.body()?.toInt() ?: 0
                                    Log.d("SonusLibrary", "Playlist $pid count: $count")
                                    val updatedPlaylist = playlist.copy(songCount = count)
                                    playlistAdapter.updateItem(index, updatedPlaylist)
                                }
                            } catch (e: Exception) {
                                Log.e("SonusLibrary", "Error fetching count for playlist $pid", e)
                            }
                        }
                    }
                } else {
                    Log.e("SonusLibrary", "Playlists API error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Exception fetching playlists", e)
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
                    Log.d("SonusLibrary", "Fetched ${favorites.size} favorite items")
                    
                    val songs = favorites.mapNotNull { fav -> 
                        // Map the flattened structure from user's backend
                        val song = (fav.song ?: fav.songDto) ?: if (fav.songTitle != null) {
                            SongDTO(
                                id = fav.songId,
                                title = fav.songTitle,
                                artist = fav.songArtist ?: "Nieznany artysta",
                                duration = fav.songDuration,
                                coverPath = fav.coverPath,
                                isFavorite = true
                            )
                        } else null

                        song?.apply { isFavorite = true } ?: run {
                            Log.w("SonusLibrary", "Favorite item ${fav.id} has no song data. songId: ${fav.songId}")
                            null
                        }
                    }
                    
                    Log.d("SonusLibrary", "Displaying ${songs.size} favorite songs")
                    
                    // Also check if these favorite songs are in any playlist
                    launch {
                        val playlistSongsIds = PlaylistHelper.getAllSongsInUserPlaylists(userId)
                        PlaylistHelper.enrichSongsWithPlaylistState(songs, playlistSongsIds)
                        favoriteSongAdapter.notifyDataSetChanged()
                    }

                    favoriteSongAdapter.updateData(songs)
                    
                    if (favorites.isNotEmpty() && songs.isEmpty()) {
                        Toast.makeText(this@LibraryActivity, "Błąd: Brak danych piosenek", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.e("SonusLibrary", "Error fetching favorites: ${response.code()}")
                    Toast.makeText(this@LibraryActivity, "Błąd pobierania ulubionych", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Exception fetching favorites", e)
            }
        }
    }

    private fun updateSectionVisibility() {
        // This is a helper to ensure sections are visible if they have data when in "All" tab
        // Or if the specific tab is selected.
        // For now, let's just rely on the showSections call and fetchData being called.
    }

    private fun fetchFavoriteAlbums() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.albumApi.getLibraryAlbums(userId)
                if (response.isSuccessful) {
                    val albums = response.body() ?: emptyList()
                    albums.forEach { it.isSaved = true }
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

    private fun toggleAlbumLibrary(album: com.example.sonus.network.AlbumDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                if (album.isSaved) {
                    val response = RetrofitClient.albumApi.removeAlbumFromLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@LibraryActivity, "Usunięto z biblioteki", Toast.LENGTH_SHORT).show()
                        fetchFavoriteAlbums() // Refresh library
                    }
                } else {
                    val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@LibraryActivity, "Dodano do biblioteki", Toast.LENGTH_SHORT).show()
                        fetchFavoriteAlbums()
                    }
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error toggling album", e)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirm)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        // Make dialog background transparent to show custom shape if needed, 
        // but since we match app_background it's fine.
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                createPlaylist(name)
                dialog.dismiss()
            } else {
                etName.error = "Podaj nazwę playlisty"
            }
        }

        dialog.show()
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