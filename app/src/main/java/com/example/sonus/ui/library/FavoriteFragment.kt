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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var songAdapter: SongPagingAdapter
    private lateinit var rvSongs: RecyclerView
    
    private var currentSortOrder = SortOrder.DEFAULT

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
        viewModel.setFavoriteSortOrder(currentSortOrder)
        
        mainViewModel.isOfflineMode.observe(viewLifecycleOwner) { isOffline ->
            view.findViewById<View>(R.id.tvFavoriteOfflineStatus).visibility = if (isOffline) View.VISIBLE else View.GONE
            if (::songAdapter.isInitialized) {
                songAdapter.notifyDataSetChanged()
            }
        }

        val fabPlay = view.findViewById<View>(R.id.fabPlayFavorites)
        mainViewModel.isMiniPlayerVisible.observe(viewLifecycleOwner) { isVisible ->
            val params = fabPlay.layoutParams as ViewGroup.MarginLayoutParams
            val marginDp = if (isVisible) 160 else 100
            params.bottomMargin = (marginDp * resources.displayMetrics.density).toInt()
            fabPlay.layoutParams = params
        }

        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        
        view.findViewById<View>(R.id.btnBackFavorite).setOnClickListener {
            findNavController().popBackStack()
        }

        rvSongs = view.findViewById(R.id.rvFavoriteSongsFull)
        
        view.findViewById<View>(R.id.btnSortFavorite).setOnClickListener {
            showSortDialog()
        }
        
        view.findViewById<View>(R.id.fabPlayFavorites).setOnClickListener {
            playAllFavorites()
        }
        
        setupRecyclerView()
        observeViewModel()
        observeDownloads()
        
        viewModel.fetchLibraryData(sessionManager.getUserId())
    }

    private fun observeDownloads() {
        DownloadManager.downloadProgress.observe(viewLifecycleOwner) { progressMap ->
            songAdapter.setDownloadProgress(progressMap)
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongPagingAdapter(
            onItemClick = { song ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val userId = sessionManager.getUserId()
                    val allFavorites = viewModel.getAllFavoriteSongs(userId)
                    val sorted = SortHelper.sortSongs(allFavorites, currentSortOrder)
                    PlayerState.play(requireContext(), song, sorted)
                    Toast.makeText(requireContext(), getString(R.string.toast_playing, song.title), Toast.LENGTH_SHORT).show()
                }
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(requireActivity() as androidx.appcompat.app.AppCompatActivity, viewLifecycleOwner.lifecycleScope, sessionManager.getUserId(), song) {
                    viewModel.fetchLibraryData(sessionManager.getUserId())
                }
            },
            onFavoriteClick = { song ->
                viewModel.toggleFavorite(sessionManager.getUserId(), song)
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
    }

    private fun playAllFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            val allFavorites = viewModel.getAllFavoriteSongs(userId)
            if (allFavorites.isNotEmpty()) {
                val sorted = SortHelper.sortSongs(allFavorites, currentSortOrder)
                PlayerState.play(requireContext(), sorted.first(), sorted)
                Toast.makeText(requireContext(), getString(R.string.toast_playing, sorted.first().title), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Brak ulubionych utworów", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getFavoriteSongsPaging(sessionManager.getUserId()).collectLatest { pagingData ->
                songAdapter.submitData(pagingData)
            }
        }
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
                val order = when (which) {
                    0 -> SortOrder.DEFAULT
                    1 -> SortOrder.TITLE
                    2 -> SortOrder.ARTIST
                    3 -> SortOrder.DURATION
                    else -> SortOrder.DEFAULT
                }
                currentSortOrder = order
                settingsManager.setSortOrder("favorites", currentSortOrder)
                viewModel.setFavoriteSortOrder(order)
            }
            .show()
    }
}
