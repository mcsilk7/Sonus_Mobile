package com.example.sonus.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.*
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var songAdapter: SongAdapter
    private lateinit var rvSongs: RecyclerView

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
        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        
        view.findViewById<View>(R.id.btnBackFavorite).setOnClickListener {
            findNavController().popBackStack()
        }

        rvSongs = view.findViewById(R.id.rvFavoriteSongsFull)
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
                        DownloadManager.downloadSong(requireContext(), song.id)
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
            songAdapter.updateData(songs)
        }
    }
}
