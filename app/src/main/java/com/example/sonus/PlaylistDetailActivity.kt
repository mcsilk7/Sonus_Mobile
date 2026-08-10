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
import kotlinx.coroutines.launch

class PlaylistDetailActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: ImageButton
    private lateinit var imgCover: ImageView
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var sessionManager: SessionManager
    
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

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPlayPlaylist).setOnClickListener {
            Toast.makeText(this, "Odtwarzanie playlisty...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = null,
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
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.getPlaylistById(playlistId)
                if (response.isSuccessful) {
                    response.body()?.let { populateUI(it) }
                } else {
                    Toast.makeText(this@PlaylistDetailActivity, "Błąd pobierania szczegółów", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Error", e)
            }
        }
    }

    private fun populateUI(playlist: PlaylistDTO) {
        tvName.text = playlist.name
        tvDescription.text = playlist.description ?: "${playlist.songCount ?: 0} utworów"
        songAdapter.updateData(playlist.songs ?: emptyList())
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
