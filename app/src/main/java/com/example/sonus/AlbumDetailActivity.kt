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
    
    private var albumId: Long = -1
    private var currentAlbum: AlbumDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_detail)

        albumId = intent.getLongExtra("ALBUM_ID", -1)
        if (albumId == -1L) {
            finish()
            return
        }

        sessionManager = SessionManager(this)
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
        btnSaveAlbum.setOnClickListener { saveAlbumToLibrary() }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPlayAlbum).setOnClickListener {
            Toast.makeText(this, "Odtwarzanie albumu...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = { song ->
                showPlaylistSelectionDialog(song)
            },
            onFavoriteClick = { song ->
                toggleFavorite(song)
            }
        )
        rvSongs.layoutManager = LinearLayoutManager(this)
        rvSongs.adapter = songAdapter
    }

    private fun fetchAlbumDetails() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.albumApi.getAlbumById(albumId)
                if (response.isSuccessful) {
                    response.body()?.let { populateUI(it) }
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
        // Here we use the songs list from AlbumDTO (1:M relationship)
        songAdapter.updateData(album.songs ?: emptyList())
    }

    private fun showPlaylistSelectionDialog(song: SongDTO) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.getAllPlaylists()
                if (response.isSuccessful) {
                    val playlists = response.body() ?: emptyList()
                    if (playlists.isEmpty()) {
                        Toast.makeText(this@AlbumDetailActivity, "Nie masz jeszcze żadnych playlist", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val names = playlists.map { it.name }.toTypedArray()
                    androidx.appcompat.app.AlertDialog.Builder(this@AlbumDetailActivity)
                        .setTitle("Dodaj do playlisty")
                        .setItems(names) { _, which ->
                            val selectedPlaylist = playlists[which]
                            addSongToPlaylist(selectedPlaylist.id!!, song.id)
                        }
                        .show()
                } else {
                    Toast.makeText(this@AlbumDetailActivity, "Błąd pobierania playlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlbumDetailActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSongToPlaylist(playlistId: Long, songId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.addSongToPlaylist(playlistId, songId)
                if (response.isSuccessful) {
                    Toast.makeText(this@AlbumDetailActivity, "Dodano do playlisty!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AlbumDetailActivity, "Błąd dodawania: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlbumDetailActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAlbumToLibrary() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "Musisz być zalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        val album = currentAlbum ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                if (response.isSuccessful) {
                    Toast.makeText(this@AlbumDetailActivity, "Album zapisany do biblioteki!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AlbumDetailActivity, "Błąd zapisu albumu: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlbumDetailActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
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
