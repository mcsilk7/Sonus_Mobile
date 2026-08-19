package com.example.sonus.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.*
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.launch

class DownloadedSongsFragment : Fragment() {

    private lateinit var rvSongs: RecyclerView
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloaded_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBackDownloaded).setOnClickListener {
            findNavController().popBackStack()
        }

        applyThemeStrings(view)
        
        rvSongs = view.findViewById(R.id.rvDownloadedSongs)
        setupRecyclerView()
        loadDownloadedSongs()
        observeDownloadEvents()
        observeDownloadProgress()
    }

    private fun observeDownloadProgress() {
        DownloadManager.downloadProgress.observe(viewLifecycleOwner) { progressMap ->
            songAdapter.setDownloadProgress(progressMap)
            // If new songs started downloading, we might need to reload to show them
            loadDownloadedSongs()
        }
    }

    private fun observeDownloadEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            DownloadManager.events.collect {
                loadDownloadedSongs()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDownloadedSongs()
    }

    private fun applyThemeStrings(view: View) {
        val context = requireContext()
        view.findViewById<TextView>(R.id.tvDownloadedHeaderTop).text = LabelProvider.getLabel(context, "data_sector_management")
        view.findViewById<TextView>(R.id.tvDownloadedHeaderMain).text = LabelProvider.getLabel(context, "local_data_cached")
        view.findViewById<TextView>(R.id.btnBackDownloaded).text = LabelProvider.getLabel(context, "nav_back")
        view.findViewById<TextView>(R.id.tvNoDownloadedData).text = LabelProvider.getLabel(context, "no_local_data")
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                PlayerState.play(requireContext(), song, songAdapter.getSongs())
            },
            onLongClick = { song ->
                showDeleteConfirmation(song)
            }
        )
        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        rvSongs.adapter = songAdapter
    }

    private fun loadDownloadedSongs() {
        val songs = DownloadManager.getDownloadedSongs(requireContext())
        songAdapter.updateData(songs)
        
        view?.findViewById<View>(R.id.tvNoDownloadedData)?.visibility = 
            if (songs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showDeleteConfirmation(song: SongDTO) {
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        val title = if (isTechnical) "WIPE_SIGNAL_FROM_DISK?" else "Delete downloaded track?"
        val confirm = if (isTechnical) "::WIPE" else "Delete"
        val cancel = if (isTechnical) "[ ABORT ]" else "Cancel"

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(song.title)
            .setPositiveButton(confirm) { _, _ ->
                DownloadManager.deleteSong(context, song.id)
                loadDownloadedSongs()
                Toast.makeText(context, LabelProvider.getLabel(context, "profile_wipe"), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(cancel, null)
            .show()
    }
}
