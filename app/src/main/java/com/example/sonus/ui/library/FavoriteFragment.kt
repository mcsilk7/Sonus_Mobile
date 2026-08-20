package com.example.sonus.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.*
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var songAdapter: SongAdapter
    private lateinit var rvSongs: RecyclerView
    
    private var currentSortOrder = SortOrder.DEFAULT
    private var originalSongs = emptyList<com.example.sonus.network.SongDTO>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        settingsManager = SettingsManager(requireContext())
        currentSortOrder = settingsManager.getSortOrder("favorites")
        
        mainViewModel.isOfflineMode.observe(viewLifecycleOwner) { isOffline ->
            view.findViewById<View>(R.id.tvFavoriteOfflineStatus).visibility = if (isOffline) View.VISIBLE else View.GONE
        }

        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        
        view.findViewById<View>(R.id.btnBackFavorite).setOnClickListener {
            findNavController().popBackStack()
        }

        rvSongs = view.findViewById(R.id.rvFavoriteSongsFull)
        
        view.findViewById<View>(R.id.btnSortFavorite).setOnClickListener {
            showSortDialog()
        }
        
        setupRecyclerView()
        observeViewModel()
        observeDownloads()
        
        viewModel.fetchLibraryData(requireContext(), sessionManager.getUserId())
    }

    private fun observeDownloads() {
        DownloadManager.downloadProgress.observe(viewLifecycleOwner) { progressMap ->
            songAdapter.setDownloadProgress(progressMap)
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                PlayerState.play(requireContext(), song, songAdapter.getSongs())
                Toast.makeText(requireContext(), getString(R.string.toast_playing, song.title), Toast.LENGTH_SHORT).show()
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(requireActivity() as androidx.appcompat.app.AppCompatActivity, viewLifecycleOwner.lifecycleScope, sessionManager.getUserId(), song) {
                    viewModel.fetchLibraryData(requireContext(), sessionManager.getUserId())
                }
            },
            onFavoriteClick = { song ->
                viewModel.toggleFavorite(requireContext(), sessionManager.getUserId(), song)
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
            viewModel.fetchLibraryData(requireContext(), sessionManager.getUserId())
        }
    }

    private fun observeViewModel() {
        viewModel.favoriteSongs.observe(viewLifecycleOwner) { songs ->
            originalSongs = songs
            applyCurrentSort()
        }
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
                settingsManager.setSortOrder("favorites", currentSortOrder)
                applyCurrentSort()
            }
            .show()
    }
}
