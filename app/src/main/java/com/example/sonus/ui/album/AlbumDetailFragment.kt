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
import androidx.fragment.app.activityViewModels
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

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: View
    private lateinit var imgCover: ImageView
    private lateinit var btnSaveAlbum: ImageView
    private lateinit var fabPlay: com.google.android.material.floatingactionbutton.FloatingActionButton
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var settingsManager: SettingsManager
    private val repository = com.example.sonus.repository.MusicRepository()
    
    private var albumId: Long = -1
    private var currentAlbum: AlbumDTO? = null
    private var currentSortOrder = SortOrder.DEFAULT
    private var originalSongs = emptyList<SongDTO>()

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
        settingsManager = SettingsManager(requireContext())
        currentSortOrder = settingsManager.getSortOrder("album_songs")
        
        initViews(view)
        setupRecyclerView()
        fetchAlbumDetails()
        applyThemeStrings(view)
        
        mainViewModel.isOfflineMode.observe(viewLifecycleOwner) { isOffline ->
            view.findViewById<View>(R.id.tvAlbumOfflineStatus).visibility = if (isOffline) View.VISIBLE else View.GONE
            songAdapter.notifyDataSetChanged()
            updateFabState()
        }

        observeDownloads()
    }

    private fun updateFabState() {
        val context = context ?: return
        val isOffline = mainViewModel.isOfflineMode.value == true
        val firstSong = currentAlbum?.songs?.firstOrNull()
        
        if (isOffline && firstSong != null && !DownloadManager.isSongDownloaded(context, firstSong.id)) {
            fabPlay.alpha = 0.5f
            fabPlay.isClickable = false
        } else {
            fabPlay.alpha = 1.0f
            fabPlay.isClickable = true
        }
    }

    private fun observeDownloads() {
        DownloadManager.downloadProgress.observe(viewLifecycleOwner) { progressMap ->
            songAdapter.setDownloadProgress(progressMap)
            updateFabState()
        }
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
        fabPlay = view.findViewById(R.id.fabPlayAlbum)

        btnBack.setOnClickListener { findNavController().popBackStack() }
        btnSaveAlbum.setOnClickListener { toggleAlbumSave() }
        
        view.findViewById<View>(R.id.btnSortAlbum).setOnClickListener {
            showSortDialog()
        }

        fabPlay.setOnClickListener {
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
            },
            onDownloadClick = { song ->
                if (!DownloadManager.isSongDownloaded(requireContext(), song.id)) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        DownloadManager.downloadSong(requireContext(), song)
                        songAdapter.notifyDataSetChanged()
                    }
                }
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
            val album = repository.getAlbumDetails(requireContext(), albumId, userId)
            if (album != null) {
                populateUI(album)
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
        val placeholder = com.example.sonus.network.GlideHelper.getBlurHashPlaceholder(requireContext(), album.blurHash) ?: androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_playlist_head)

        val radius = resources.getDimensionPixelSize(R.dimen.studio_radius)

        com.bumptech.glide.Glide.with(this)
            .load(authenticatedUrl)
            .placeholder(placeholder)
            .error(R.drawable.bg_playlist_head)
            .transform(com.bumptech.glide.load.resource.bitmap.CenterCrop(), com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
            .into(imgCover)
        
        originalSongs = album.songs ?: emptyList()
        applyCurrentSort()
        updateFabState()
    }

    private fun applyCurrentSort() {
        val sorted = SortHelper.sortSongs(originalSongs, currentSortOrder)
        songAdapter.updateData(sorted)
    }

    private fun showSortDialog() {
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        val options = if (isTechnical) {
            arrayOf(
                getString(R.string.sort_default),
                getString(R.string.sort_title),
                getString(R.string.sort_artist),
                getString(R.string.sort_duration)
            )
        } else {
            arrayOf(
                getString(R.string.sort_default_norm),
                getString(R.string.sort_title_norm),
                getString(R.string.sort_artist_norm),
                getString(R.string.sort_duration_norm)
            )
        }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(if (isTechnical) getString(R.string.sort_order_label) else getString(R.string.sort_by_norm))
            .setItems(options) { _, which ->
                currentSortOrder = when (which) {
                    0 -> SortOrder.DEFAULT
                    1 -> SortOrder.TITLE
                    2 -> SortOrder.ARTIST
                    3 -> SortOrder.DURATION
                    else -> SortOrder.DEFAULT
                }
                settingsManager.setSortOrder("album_songs", currentSortOrder)
                applyCurrentSort()
            }
            .show()
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
        if (isSaved) {
            btnSaveAlbum.setImageResource(R.drawable.ic_favorite)
            btnSaveAlbum.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.studio_red))
        } else {
            btnSaveAlbum.setImageResource(R.drawable.ic_favorite_border)
            btnSaveAlbum.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.studio_text_dim))
        }
    }

    private fun toggleFavorite(song: SongDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val added = repository.toggleFavorite(requireContext(), userId, song)
                if (added != null) {
                    song.isFavorite = added
                    songAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("AlbumDetail", "Favorite error", e)
            }
        }
    }
}
