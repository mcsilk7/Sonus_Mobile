package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnSearchIcon: ImageView
    private lateinit var rvSongResults: RecyclerView
    private lateinit var rvAlbumResults: RecyclerView
    private lateinit var tvSongsHeader: TextView
    private lateinit var tvAlbumsHeader: TextView
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        sessionManager = SessionManager(this)
        initViews()
        setupRecyclerViews()
        setupSearch()

        NavigationHelper.setupBottomNav(this)
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch)
        btnSearchIcon = findViewById(R.id.ivSearchIcon)
        rvSongResults = findViewById(R.id.rvSongResults)
        rvAlbumResults = findViewById(R.id.rvAlbumResults)
        tvSongsHeader = findViewById(R.id.tvSongsHeader)
        tvAlbumsHeader = findViewById(R.id.tvAlbumsHeader)
    }

    private fun setupRecyclerViews() {
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
        rvSongResults.layoutManager = LinearLayoutManager(this)
        rvSongResults.adapter = songAdapter

        albumAdapter = AlbumAdapter(
            albums = emptyList(),
            onItemClick = { album ->
                val intent = Intent(this, AlbumDetailActivity::class.java)
                intent.putExtra("ALBUM_ID", album.id)
                startActivity(intent)
            },
            onAddClick = { album ->
                toggleAlbumFavorite(album)
            }
        )
        rvAlbumResults.layoutManager = GridLayoutManager(this, 2)
        rvAlbumResults.adapter = albumAdapter
    }

    private fun setupSearch() {
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.text.toString().trim())
                true
            } else {
                false
            }
        }

        btnSearchIcon.setOnClickListener {
            performSearch(etSearch.text.toString().trim())
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        Log.d("SonusSearch", "Searching for: $query")
        lifecycleScope.launch {
            try {
                val songsDeferred = async { RetrofitClient.searchApi.searchSongs(query) }
                val albumsDeferred = async { RetrofitClient.searchApi.searchAlbums(query) }

                val songResponse = songsDeferred.await()
                val albumResponse = albumsDeferred.await()

                var hasResults = false

                if (songResponse.isSuccessful) {
                    val songs = songResponse.body() ?: emptyList()
                    songAdapter.updateData(songs)
                    tvSongsHeader.visibility = if (songs.isNotEmpty()) View.VISIBLE else View.GONE
                    if (songs.isNotEmpty()) hasResults = true
                }

                if (albumResponse.isSuccessful) {
                    val albums = albumResponse.body() ?: emptyList()
                    albumAdapter.updateData(albums)
                    tvAlbumsHeader.visibility = if (albums.isNotEmpty()) View.VISIBLE else View.GONE
                    if (albums.isNotEmpty()) hasResults = true
                }

                if (!hasResults) {
                    val msg = "Brak wyników dla: $query (S:${songResponse.body()?.size ?: 0}, A:${albumResponse.body()?.size ?: 0})"
                    Toast.makeText(this@SearchActivity, msg, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("SonusSearch", "Search failed", e)
                Toast.makeText(this@SearchActivity, "Błąd połączenia: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showPlaylistSelectionDialog(song: SongDTO) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.getAllPlaylists()
                if (response.isSuccessful) {
                    val playlists = response.body() ?: emptyList()
                    if (playlists.isEmpty()) {
                        Toast.makeText(this@SearchActivity, "Nie masz jeszcze żadnych playlist", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val names = playlists.map { it.name }.toTypedArray()
                    AlertDialog.Builder(this@SearchActivity)
                        .setTitle("Dodaj do playlisty")
                        .setItems(names) { _, which ->
                            val selectedPlaylist = playlists[which]
                            addSongToPlaylist(selectedPlaylist.id!!, song.id)
                        }
                        .show()
                } else {
                    Toast.makeText(this@SearchActivity, "Błąd pobierania playlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSongToPlaylist(playlistId: Long, songId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.addSongToPlaylist(playlistId, songId)
                if (response.isSuccessful) {
                    Toast.makeText(this@SearchActivity, "Dodano do playlisty!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SearchActivity, "Błąd dodawania: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorite(song: SongDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "Musisz być zalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.favoriteApi.toggleFavorite(userId, song.id)
                if (response.isSuccessful) {
                    val added = response.body()?.get("added") ?: false
                    song.isFavorite = added
                    songAdapter.notifyDataSetChanged()
                    
                    val message = if (added) "Dodano do ulubionych!" else "Usunięto z ulubionych"
                    Toast.makeText(this@SearchActivity, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SearchActivity, "Błąd: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleAlbumFavorite(album: com.example.sonus.network.AlbumDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "Musisz być zalogowany", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Using the new POST endpoint provided by the user
                val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                if (response.isSuccessful) {
                    Toast.makeText(this@SearchActivity, "Album dodany do biblioteki!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SearchActivity, "Błąd: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
