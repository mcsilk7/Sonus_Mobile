package com.example.sonus.ui.album

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.*
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AlbumDetailFragment : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: View
    private lateinit var imgCover: ImageView
    private lateinit var btnSaveAlbum: ImageView
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var sessionManager: SessionManager
    
    private var albumId: Long = -1
    private var currentAlbum: AlbumDTO? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        albumId = arguments?.getLong("ALBUM_ID", -1) ?: -1
        if (albumId == -1L) {
            findNavController().popBackStack()
            return
        }

        sessionManager = SessionManager(requireContext())
        initViews(view)
        setupRecyclerView()
        fetchAlbumDetails()
        applyThemeStrings(view)
    }

    private fun applyThemeStrings(view: View) {
        val context = requireContext()
        view.findViewById<TextView>(R.id.tvAlbumHeaderTop).text = LabelProvider.getLabel(context, "album_detail_top")
        view.findViewById<TextView>(R.id.tvAlbumHeaderMain).text = LabelProvider.getLabel(context, "album_detail_main")
        view.findViewById<TextView>(R.id.tvAlbumDataStreamLabel).text = LabelProvider.getLabel(context, "data_stream_list")
        view.findViewById<TextView>(R.id.btnBackAlbum).text = LabelProvider.getLabel(context, "nav_back")
    }

    private fun initViews(view: View) {
        tvTitle = view.findViewById(R.id.tvAlbumTitleDetail)
        tvArtist = view.findViewById(R.id.tvAlbumArtistDetail)
        rvSongs = view.findViewById(R.id.rvAlbumSongs)
        btnBack = view.findViewById(R.id.btnBackAlbum)
        imgCover = view.findViewById(R.id.imgAlbumCoverLarge)
        btnSaveAlbum = view.findViewById(R.id.btnSaveAlbum)

        btnBack.setOnClickListener { findNavController().popBackStack() }
        btnSaveAlbum.setOnClickListener { toggleAlbumSave() }

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPlayAlbum).setOnClickListener {
            currentAlbum?.songs?.firstOrNull()?.let { playSong(it) }
        }
    }

    private fun playSong(song: SongDTO) {
        PlayerState.play(requireContext(), song, currentAlbum?.songs ?: emptyList())
        Toast.makeText(requireContext(), getString(R.string.toast_playing, song.title), Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                playSong(song)
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(requireActivity() as androidx.appcompat.app.AppCompatActivity, viewLifecycleOwner.lifecycleScope, sessionManager.getUserId(), song) {
                    songAdapter.notifyDataSetChanged()
                }
            },
            onFavoriteClick = { song ->
                toggleFavorite(song)
            }
        )
        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        rvSongs.adapter = songAdapter

        SongTouchHelper.attach(
            rvSongs,
            songAdapter,
            sessionManager.getUserId(),
            viewLifecycleOwner.lifecycleScope
        ) {
            // Updated
        }
    }

    private fun fetchAlbumDetails() {
        val userId = sessionManager.getUserId()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
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
                        if (album.songs.isNullOrEmpty()) {
                            val songsResponse = RetrofitClient.albumApi.getSongsInAlbum(albumId)
                            if (songsResponse.isSuccessful) {
                                album = album.copy(songs = songsResponse.body())
                            }
                        }

                        if (favoritesResponse?.isSuccessful == true) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            album.songs?.forEach { it.isFavorite = favoriteIds.contains(it.id) }
                        }
                        
                        album.songs?.let { PlaylistHelper.enrichSongsWithPlaylistState(it, playlistSongsIds) }
                        
                        if (libraryAlbumsResponse?.isSuccessful == true) {
                            val libraryIds = libraryAlbumsResponse.body()?.map { it.id }?.toSet() ?: emptySet()
                            album.isSaved = libraryIds.contains(album.id)
                        }
                        
                        populateUI(album)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlbumDetail", "Error", e)
            }
        }
    }

    private fun populateUI(album: AlbumDTO) {
        currentAlbum = album
        val isTechnical = SettingsManager(requireContext()).getThemeId() == 0
        
        tvTitle.text = if (isTechnical) album.title.uppercase() else album.title
        tvArtist.text = if (isTechnical) {
            getString(R.string.src_prefix, album.artist.uppercase())
        } else {
            getString(R.string.artist_prefix_norm, album.artist)
        }
        btnSaveAlbum.visibility = View.VISIBLE
        updateSaveButtonState(album.isSaved)

        val coverUrl = if (album.coverPath?.startsWith("http") == true) {
            album.coverPath
        } else {
            RetrofitClient.BASE_URL + "api/albums/$albumId/cover"
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(requireContext(), coverUrl)

        val radius = resources.getDimensionPixelSize(R.dimen.studio_radius)

        com.bumptech.glide.Glide.with(this)
            .load(authenticatedUrl)
            .placeholder(R.drawable.bg_playlist_head)
            .error(R.drawable.bg_playlist_head)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
            .into(imgCover)
        
        songAdapter.updateData(album.songs ?: emptyList())
    }

    private fun toggleAlbumSave() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        val album = currentAlbum ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (album.isSaved) {
                    val response = RetrofitClient.albumApi.removeAlbumFromLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        album.isSaved = false
                        updateSaveButtonState(false)
                    }
                } else {
                    val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        album.isSaved = true
                        updateSaveButtonState(true)
                    }
                }
            } catch (e: Exception) {
                Log.e("AlbumDetail", "Error", e)
            }
        }
    }

    private fun updateSaveButtonState(isSaved: Boolean) {
        val color = if (isSaved) R.color.studio_red else R.color.studio_bg
        btnSaveAlbum.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), color))
    }

    private fun toggleFavorite(song: SongDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
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
