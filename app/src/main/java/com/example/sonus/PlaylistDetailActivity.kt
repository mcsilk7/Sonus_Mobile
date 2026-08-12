package com.example.sonus

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlaylistDetailActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var imgCover: ImageView
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager
    
    private var playlistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        playlistId = intent.getLongExtra("PLAYLIST_ID", -1)
        if (playlistId == -1L) {
            finish()
            return
        }

        sessionManager = SessionManager(this)
        recentlyPlayedManager = RecentlyPlayedManager(this)
        initViews()
        setupRecyclerView()
        fetchPlaylistDetails()
    }

    private fun initViews() {
        tvName = findViewById(R.id.tvPlaylistNameDetail)
        tvDescription = findViewById(R.id.tvPlaylistDescription)
        rvSongs = findViewById(R.id.rvPlaylistSongs)
        btnBack = findViewById(R.id.btnBack)
        imgCover = findViewById(R.id.imgPlaylistCoverLarge)

        btnBack.setOnClickListener { finish() }

        initMiniPlayer()

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPlayPlaylist).setOnClickListener {
            songAdapter.getSongs().firstOrNull()?.let { playSong(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        initMiniPlayer()
    }

    private fun initMiniPlayer() {
        MiniPlayerHelper.setupMiniPlayer(this)
    }

    private fun playSong(song: SongDTO) {
        recentlyPlayedManager.addSong(song)
        PlayerState.play(this, song, songAdapter.getSongs())
        initMiniPlayer()
        Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                playSong(song)
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(this, lifecycleScope, song) {
                    songAdapter.notifyDataSetChanged()
                }
            },
            onFavoriteClick = { song ->
                toggleFavorite(song)
            },
            onLongClick = { song ->
                confirmRemoveFromPlaylist(song)
            }
        )
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter = songAdapter
    }

    private fun fetchPlaylistDetails() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                // Fetch playlist details and user favorites in parallel
                val playlistDeferred = async { RetrofitClient.playlistApi.getPlaylistById(playlistId) }
                val favoritesDeferred = if (userId != -1L) async { RetrofitClient.favoriteApi.getFavorites(userId) } else null

                val playlistResponse = playlistDeferred.await()
                val favoritesResponse = favoritesDeferred?.await()

                if (playlistResponse.isSuccessful) {
                    var playlist = playlistResponse.body()
                    if (playlist != null) {
                        Log.d("PlaylistDetail", "Fetched playlist: ${playlist.name}, initial songs: ${playlist.songs?.size}")
                        
                        // Always try to fetch songs if the list is empty or null to be sure
                        if (playlist.songs.isNullOrEmpty()) {
                            Log.d("PlaylistDetail", "Songs list empty, fetching from /api/playlists/${playlistId}/songs")
                            val songsResponse = RetrofitClient.playlistApi.getSongsInPlaylist(playlistId)
                            if (songsResponse.isSuccessful) {
                                val fetchedSongs = songsResponse.body()
                                Log.d("PlaylistDetail", "Fetched ${fetchedSongs?.size} songs for playlist")
                                playlist = playlist.copy(songs = fetchedSongs)
                            } else {
                                Log.e("PlaylistDetail", "Failed to fetch songs: ${songsResponse.code()}")
                                Toast.makeText(this@PlaylistDetailActivity, "Błąd pobierania utworów", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Mark songs as favorites if they are in the user's favorites list
                        if (favoritesResponse?.isSuccessful == true) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            playlist.songs?.forEach { song ->
                                song.isFavorite = favoriteIds.contains(song.id)
                            }
                        }
                        populateUI(playlist)
                    }
                } else {
                    Log.e("PlaylistDetail", "Playlist API error: ${playlistResponse.code()}")
                    Toast.makeText(this@PlaylistDetailActivity, "Błąd pobierania szczegółów playlisty", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Error", e)
            }
        }
    }

    private fun populateUI(playlist: PlaylistDTO) {
        tvName.text = playlist.name
        val songs = playlist.songs ?: emptyList()
        val count = if (songs.isNotEmpty()) songs.size else (playlist.songCount ?: 0)
        tvDescription.text = playlist.description ?: formatSongCount(count)

        // Load playlist cover using the new endpoint of the first song
        val firstSong = songs.firstOrNull()
        val authenticatedUrl = firstSong?.let {
            val coverUrl = RetrofitClient.BASE_URL + "api/songs/${it.id}/cover"
            com.example.sonus.network.GlideHelper.getAuthenticatedUrl(this, coverUrl)
        }

        val radius = (20 * resources.displayMetrics.density).toInt()

        com.bumptech.glide.Glide.with(this)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_playlist_head)
            .error(R.drawable.bg_playlist_head)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
            .into(imgCover)
        
        songs.forEach { it.isInPlaylist = true }
        songAdapter.updateData(songs)
    }

    private fun formatSongCount(count: Int): String {
        return when {
            count == 0 -> "Brak utworów"
            count == 1 -> "1 utwór"
            count % 10 in 2..4 && (count % 100 !in 12..14) -> "$count utwory"
            else -> "$count utworów"
        }
    }

    private fun toggleFavorite(song: SongDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.favoriteApi.toggleFavorite(userId, song.id)
                if (response.isSuccessful) {
                    val added = response.body()?.get("added") ?: false
                    song.isFavorite = added
                    songAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Favorite error", e)
            }
        }
    }

    private fun confirmRemoveFromPlaylist(song: SongDTO) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Usuń utwór")
            .setMessage("Usunąć ${song.title} z tej playlisty?")
            .setPositiveButton("Usuń") { _, _ -> removeSongFromPlaylist(song.id) }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun removeSongFromPlaylist(songId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.removeSongFromPlaylist(playlistId, songId)
                if (response.isSuccessful) {
                    Toast.makeText(this@PlaylistDetailActivity, "Utwór usunięty z playlisty", Toast.LENGTH_SHORT).show()
                    fetchPlaylistDetails()
                } else {
                    Toast.makeText(this@PlaylistDetailActivity, "Błąd usuwania: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Remove error", e)
            }
        }
    }
}
