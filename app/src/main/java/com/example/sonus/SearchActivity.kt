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
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        sessionManager = SessionManager(this)
        recentlyPlayedManager = RecentlyPlayedManager(this)
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
                recentlyPlayedManager.addSong(song)
                Toast.makeText(this, "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
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

        val userId = sessionManager.getUserId()
        Log.d("SonusSearch", "Searching for: $query")
        lifecycleScope.launch {
            try {
                val songsDeferred = async { RetrofitClient.searchApi.searchSongs(query) }
                val albumsDeferred = async { RetrofitClient.searchApi.searchAlbums(query) }
                val favoritesDeferred = if (userId != -1L) async { RetrofitClient.favoriteApi.getFavorites(userId) } else null
                val libraryAlbumsDeferred = if (userId != -1L) async { RetrofitClient.albumApi.getLibraryAlbums(userId) } else null

                val songResponse = songsDeferred.await()
                val albumResponse = albumsDeferred.await()
                val favoritesResponse = favoritesDeferred?.await()
                val libraryAlbumsResponse = libraryAlbumsDeferred?.await()

                var hasResults = false

                if (songResponse.isSuccessful) {
                    val songs = songResponse.body() ?: emptyList()

                    // Mark as favorite if in user's favorites
                    if (favoritesResponse?.isSuccessful == true) {
                        val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                        songs.forEach { song ->
                            song.isFavorite = favoriteIds.contains(song.id)
                        }
                    }

                    // Mark as in playlist if in any user playlist
                    if (userId != -1L) {
                        launch {
                            val playlistSongsIds = PlaylistHelper.getAllSongsInUserPlaylists(userId)
                            PlaylistHelper.enrichSongsWithPlaylistState(songs, playlistSongsIds)
                            songAdapter.notifyDataSetChanged()
                        }
                    }

                    songAdapter.updateData(songs)
                    tvSongsHeader.visibility = if (songs.isNotEmpty()) View.VISIBLE else View.GONE
                    if (songs.isNotEmpty()) hasResults = true
                }

                if (albumResponse.isSuccessful) {
                    val albums = albumResponse.body() ?: emptyList()
                    
                    // Mark as saved if in user's library
                    if (libraryAlbumsResponse?.isSuccessful == true) {
                        val libraryIds = libraryAlbumsResponse.body()?.map { it.id }?.toSet() ?: emptySet()
                        albums.forEach { album ->
                            album.isSaved = libraryIds.contains(album.id)
                        }
                    }
                    
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
                if (album.isSaved) {
                    val response = RetrofitClient.albumApi.removeAlbumFromLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@SearchActivity, "Usunięto z biblioteki", Toast.LENGTH_SHORT).show()
                        album.isSaved = false
                        albumAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@SearchActivity, "Błąd: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(this@SearchActivity, "Album dodany do biblioteki!", Toast.LENGTH_SHORT).show()
                        album.isSaved = true
                        albumAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@SearchActivity, "Błąd: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
