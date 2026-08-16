package com.example.sonus

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AlbumDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var imgCover: ImageView
    private lateinit var btnSaveAlbum: ImageButton
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager
    
    private var albumId: Long = -1
    private var currentAlbum: AlbumDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)

        albumId = intent.getLongExtra("ALBUM_ID", -1)
        if (albumId == -1L) {
            finish()
            return
        }

        sessionManager = SessionManager(this)
        recentlyPlayedManager = RecentlyPlayedManager(this)
        initViews()
        setupRecyclerView()
        fetchAlbumDetails()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvAlbumTitleDetail)
        tvArtist = findViewById(R.id.tvAlbumArtistDetail)
        rvSongs = findViewById(R.id.rvAlbumSongs)
        btnBack = findViewById(R.id.btnBackAlbum)
        imgCover = findViewById(R.id.imgAlbumCoverLarge)
        btnSaveAlbum = findViewById(R.id.btnSaveAlbum)

        btnBack.setOnClickListener { finish() }
        btnSaveAlbum.setOnClickListener { toggleAlbumSave() }

        initMiniPlayer()

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPlayAlbum).setOnClickListener {
            currentAlbum?.songs?.firstOrNull()?.let { playSong(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        initMiniPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        MiniPlayerHelper.onDestroy(this)
    }

    private fun initMiniPlayer() {
        MiniPlayerHelper.setupMiniPlayer(this)
    }

    private fun playSong(song: SongDTO) {
        recentlyPlayedManager.addSong(song)
        PlayerState.play(this, song, currentAlbum?.songs ?: emptyList())
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
            }
        )
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter = songAdapter
    }

    private fun fetchAlbumDetails() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            try {
                // Fetch album details, favorites, playlists and library in parallel
                val albumDeferred = async { RetrofitClient.albumApi.getAlbumById(albumId) }
                val favoritesDeferred = if (userId != -1L) async { RetrofitClient.favoriteApi.getFavorites(userId) } else null
                val libraryAlbumsDeferred = if (userId != -1L) async { RetrofitClient.albumApi.getLibraryAlbums(userId) } else null
                val playlistSongsIdsDeferred = if (userId != -1L) async { PlaylistHelper.getAllSongsInUserPlaylists(userId) } else null

                val albumResponse = albumDeferred.await()
                val favoritesResponse = favoritesDeferred?.await()
                val libraryAlbumsResponse = libraryAlbumsDeferred?.await()
                val playlistSongsIds = playlistSongsIdsDeferred?.await() ?: emptySet()

                if (albumResponse.isSuccessful) {
                    var album = albumResponse.body()
                    if (album != null) {
                        Log.d("AlbumDetail", "Fetched album: ${album.title}, initial songs: ${album.songs?.size}")

                        // Always try to fetch songs if the list is empty or null to be sure
                        if (album.songs.isNullOrEmpty()) {
                            Log.d("AlbumDetail", "Songs list empty, fetching from /api/albums/${albumId}/songs")
                            val songsResponse = RetrofitClient.albumApi.getSongsInAlbum(albumId)
                            if (songsResponse.isSuccessful) {
                                val fetchedSongs = songsResponse.body()
                                Log.d("AlbumDetail", "Fetched ${fetchedSongs?.size} songs for album")
                                album = album.copy(songs = fetchedSongs)
                            } else {
                                Log.e("AlbumDetail", "Failed to fetch songs: ${songsResponse.code()}")
                            }
                        }

                        // Mark songs as favorites if they are in the user's favorites list
                        if (favoritesResponse?.isSuccessful == true) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            album.songs?.forEach { song ->
                                song.isFavorite = favoriteIds.contains(song.id)
                            }
                        }
                        
                        // Mark songs as in playlist
                        album.songs?.let {
                            PlaylistHelper.enrichSongsWithPlaylistState(it, playlistSongsIds)
                        }
                        
                        // Mark album as saved if in user's library
                        if (libraryAlbumsResponse?.isSuccessful == true) {
                            val libraryIds = libraryAlbumsResponse.body()?.map { it.id }?.toSet() ?: emptySet()
                            album.isSaved = libraryIds.contains(album.id)
                        }
                        
                        populateUI(album)
                    }
                } else {
                    Toast.makeText(this@AlbumDetailActivity, "Błąd pobierania albumu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AlbumDetail", "Error", e)
            }
        }
    }

    private fun populateUI(album: AlbumDTO) {
        currentAlbum = album
        tvTitle.text = album.title
        tvArtist.text = album.artist
        btnSaveAlbum.visibility = View.VISIBLE
        updateSaveButtonState(album.isSaved)

        val coverUrl = if (album.coverPath?.startsWith("http") == true) {
            album.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/albums/$albumId/cover"
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(this, coverUrl)

        val radius = (20 * resources.displayMetrics.density).toInt()

        com.bumptech.glide.Glide.with(this)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_playlist_head)
            .error(R.drawable.bg_playlist_head)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
            .into(imgCover)
        
        val songs = album.songs ?: emptyList()
        Log.d("AlbumDetail", "Updating adapter with ${songs.size} songs")
        songAdapter.updateData(songs)
    }

    private fun toggleAlbumSave() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "Musisz być zalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        val album = currentAlbum ?: return
        lifecycleScope.launch {
            try {
                if (album.isSaved) {
                    val response = RetrofitClient.albumApi.removeAlbumFromLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AlbumDetailActivity, "Usunięto z biblioteki", Toast.LENGTH_SHORT).show()
                        album.isSaved = false
                        updateSaveButtonState(false)
                    } else {
                        Toast.makeText(this@AlbumDetailActivity, "Błąd: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AlbumDetailActivity, "Album zapisany do biblioteki!", Toast.LENGTH_SHORT).show()
                        album.isSaved = true
                        updateSaveButtonState(true)
                    } else {
                        Toast.makeText(this@AlbumDetailActivity, "Błąd zapisu albumu: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlbumDetailActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSaveButtonState(isSaved: Boolean) {
        if (isSaved) {
            btnSaveAlbum.setColorFilter(androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_red_dark))
        } else {
            btnSaveAlbum.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.app_background))
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
                Log.e("AlbumDetail", "Favorite error", e)
            }
        }
    }
}
