package com.example.sonus.ui.playlist

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
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlaylistDetailFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var rvSongs: RecyclerView
    private lateinit var btnBack: View
    private lateinit var btnDelete: ImageView
    private lateinit var imgCover: ImageView
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var sessionManager: SessionManager
    
    private var playlistId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getLong("PLAYLIST_ID", -1) ?: -1
        if (playlistId == -1L) {
            findNavController().popBackStack()
            return
        }

        sessionManager = SessionManager(requireContext())
        initViews(view)
        setupRecyclerView()
        fetchPlaylistDetails()
        applyThemeStrings(view)
    }

    private fun applyThemeStrings(view: View) {
        val context = requireContext()
        view.findViewById<TextView>(R.id.tvPlaylistHeaderTop).text = LabelProvider.getLabel(context, "playlist_detail_top")
        view.findViewById<TextView>(R.id.tvPlaylistHeaderMain).text = LabelProvider.getLabel(context, "playlist_detail_main")
        view.findViewById<TextView>(R.id.tvPlaylistDataStreamLabel).text = LabelProvider.getLabel(context, "data_stream_list")
        view.findViewById<TextView>(R.id.btnBack).text = LabelProvider.getLabel(context, "nav_back")
    }

    private fun initViews(view: View) {
        tvName = view.findViewById(R.id.tvPlaylistNameDetail)
        tvDescription = view.findViewById(R.id.tvPlaylistDescription)
        rvSongs = view.findViewById(R.id.rvPlaylistSongs)
        btnBack = view.findViewById(R.id.btnBack)
        btnDelete = view.findViewById(R.id.btnDeletePlaylist)
        imgCover = view.findViewById(R.id.imgPlaylistCoverLarge)

        btnBack.setOnClickListener { findNavController().popBackStack() }
        btnDelete.setOnClickListener { confirmDeletePlaylist() }

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabPlayPlaylist).setOnClickListener {
            songAdapter.getSongs().firstOrNull()?.let { playSong(it) }
        }
    }

    private fun playSong(song: SongDTO) {
        PlayerState.play(requireContext(), song, songAdapter.getSongs())
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
            onLongClick = { song ->
                confirmRemoveFromPlaylist(song)
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

    private fun fetchPlaylistDetails() {
        val userId = sessionManager.getUserId()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val playlistDeferred = async { RetrofitClient.playlistApi.getPlaylistById(playlistId) }
                val favoritesDeferred = if (userId != -1L) async { RetrofitClient.favoriteApi.getFavorites(userId) } else null

                val playlistResponse = playlistDeferred.await()
                val favoritesResponse = favoritesDeferred?.await()

                if (playlistResponse.isSuccessful) {
                    var playlist = playlistResponse.body()
                    if (playlist != null) {
                        if (playlist.songs.isNullOrEmpty()) {
                            val songsResponse = RetrofitClient.playlistApi.getSongsInPlaylist(playlistId)
                            if (songsResponse.isSuccessful) {
                                playlist = playlist.copy(songs = songsResponse.body())
                            }
                        }

                        if (favoritesResponse?.isSuccessful == true) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            playlist.songs?.forEach { it.isFavorite = favoriteIds.contains(it.id) }
                        }
                        populateUI(playlist)
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Error", e)
            }
        }
    }

    private fun populateUI(playlist: PlaylistDTO) {
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        tvName.text = if (isTechnical) {
            getString(R.string.unit_id_prefix, playlist.id?.toString(16)?.uppercase() ?: "00")
        } else {
            playlist.name
        }
        
        val songs = playlist.songs ?: emptyList()
        val count = if (songs.isNotEmpty()) songs.size else (playlist.songCount ?: 0)
        
        tvDescription.text = if (isTechnical) {
            getString(R.string.log_prefix, "${playlist.name.uppercase()} (${formatSongCount(count)})")
        } else {
            getString(R.string.status_prefix_norm, formatSongCount(count))
        }

        val firstSong = songs.firstOrNull()
        val coverUrl = if (firstSong?.coverPath?.startsWith("http") == true) {
            firstSong.coverPath
        } else {
            firstSong?.let { RetrofitClient.BASE_URL + "api/songs/${it.id}/cover" }
        }
        val authenticatedUrl = com.example.sonus.network.GlideHelper.getAuthenticatedUrl(requireContext(), coverUrl)

        val radius = resources.getDimensionPixelSize(R.dimen.studio_radius)

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
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        return if (isTechnical) {
            getString(R.string.data_slots_prefix, count)
        } else {
            getString(R.string.tracks_count_norm, count)
        }
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
                Log.e("PlaylistDetail", "Favorite error", e)
            }
        }
    }

    private fun confirmDeletePlaylist() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Usuń playlistę")
            .setMessage("Czy na pewno chcesz usunąć tę playlistę?")
            .setPositiveButton("Usuń") { _, _ -> deletePlaylist() }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun deletePlaylist() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.deletePlaylist(playlistId)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Playlista usunięta", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Delete error", e)
            }
        }
    }

    private fun confirmRemoveFromPlaylist(song: SongDTO) {
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        val title = if (isTechnical) getString(R.string.confirm_remove_song_title).uppercase() else "Remove Track"
        val message = if (isTechnical) getString(R.string.confirm_remove_song_msg, song.title) else "Remove ${song.title} from this playlist?"
        val confirm = if (isTechnical) "::WIPE" else "Remove"
        val cancel = if (isTechnical) "[ ABORT ]" else "Cancel"

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirm) { _, _ -> removeSongFromPlaylist(song.id) }
            .setNegativeButton(cancel, null)
            .show()
    }

    private fun removeSongFromPlaylist(songId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.removeSongFromPlaylist(playlistId, songId)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Utwór usunięty z playlisty", Toast.LENGTH_SHORT).show()
                    fetchPlaylistDetails()
                }
            } catch (e: Exception) {
                Log.e("PlaylistDetail", "Remove error", e)
            }
        }
    }
}
