package com.example.sonus.ui.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.viewModels
import com.example.sonus.*
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var albumAdapter: AlbumAdapter
    
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var rvAlbums: RecyclerView
    
    private lateinit var btnAddPlaylist: LinearLayout
    private lateinit var btnGoToFavorites: View
    private lateinit var sessionManager: SessionManager
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager

    private lateinit var sectionPlaylists: View
    private lateinit var sectionAlbums: View

    private lateinit var tabAll: TextView
    private lateinit var tabPlaylists: TextView
    private lateinit var tabAlbums: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        recentlyPlayedManager = RecentlyPlayedManager(requireContext())
        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        initViews(view)
        setupRecyclerViews()
        setupTabs()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.updateData(playlists)
        }

        viewModel.libraryAlbums.observe(viewLifecycleOwner) { albums ->
            albumAdapter.updateData(albums)
            sectionAlbums.visibility = if (albums.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchLibraryData(sessionManager.getUserId())
    }

    private fun initViews(view: View) {
        rvPlaylists = view.findViewById(R.id.rvPlaylists)
        rvAlbums = view.findViewById(R.id.rvAlbums)
        btnAddPlaylist = view.findViewById(R.id.btnAddPlaylist)
        btnGoToFavorites = view.findViewById(R.id.btnGoToFavorites)
        
        sectionPlaylists = view.findViewById(R.id.sectionPlaylists)
        sectionAlbums = view.findViewById(R.id.sectionAlbums)

        tabAll = view.findViewById(R.id.tabAll)
        tabPlaylists = view.findViewById(R.id.tabPlaylists)
        tabAlbums = view.findViewById(R.id.tabAlbums)

        btnAddPlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }

        btnGoToFavorites.setOnClickListener {
            findNavController().navigate(R.id.favoriteFragment)
        }
    }

    private fun setupTabs() {
        tabAll.setOnClickListener { updateTabSelection(it as TextView); showSections(true, true) }
        tabPlaylists.setOnClickListener { updateTabSelection(it as TextView); showSections(true, false) }
        tabAlbums.setOnClickListener { updateTabSelection(it as TextView); showSections(false, true) }
    }

    private fun updateTabSelection(selectedTab: TextView) {
        val tabs = listOf(tabAll, tabPlaylists, tabAlbums)
        tabs.forEach { tab ->
            if (tab == selectedTab) {
                tab.setBackgroundResource(R.drawable.bg_library_tab_active)
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.app_background))
            } else {
                tab.setBackgroundResource(R.drawable.bg_library_tab)
                tab.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark))
            }
        }
    }

    private fun showSections(playlists: Boolean, albums: Boolean) {
        sectionPlaylists.visibility = if (playlists) View.VISIBLE else View.GONE
        sectionAlbums.visibility = if (albums) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerViews() {
        playlistAdapter = PlaylistAdapter(
            playlists = emptyList(),
            onItemClick = { playlist ->
                val bundle = Bundle().apply { putLong("PLAYLIST_ID", playlist.id ?: -1L) }
                findNavController().navigate(R.id.playlistDetailFragment, bundle)
            },
            onLongClick = { playlist ->
                showPlaylistOptions(playlist)
            }
        )
        rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
        rvPlaylists.adapter = playlistAdapter

        albumAdapter = AlbumAdapter(
            albums = emptyList(),
            onItemClick = { album ->
                val bundle = Bundle().apply { putLong("ALBUM_ID", album.id ?: -1L) }
                findNavController().navigate(R.id.albumDetailFragment, bundle)
            },
            onAddClick = { album ->
                toggleAlbumLibrary(album)
            }
        )
        rvAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        rvAlbums.adapter = albumAdapter
    }

    private fun toggleAlbumLibrary(album: com.example.sonus.network.AlbumDTO) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (album.isSaved) {
                    val response = RetrofitClient.albumApi.removeAlbumFromLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Usunięto z biblioteki", Toast.LENGTH_SHORT).show()
                        viewModel.fetchLibraryData(userId)
                    }
                } else {
                    val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Dodano do biblioteki", Toast.LENGTH_SHORT).show()
                        viewModel.fetchLibraryData(userId)
                    }
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error toggling album", e)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnCancel = view.findViewById<View>(R.id.btnCancel)
        val btnConfirm = view.findViewById<View>(R.id.btnConfirm)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                createPlaylist(name)
                dialog.dismiss()
            } else {
                etName.error = "Podaj nazwę playlisty"
            }
        }
        dialog.show()
    }

    private fun createPlaylist(name: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.createPlaylist(userId, PlaylistDTO(name = name))
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Utworzono playlistę", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLibraryData(userId)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPlaylistOptions(playlist: PlaylistDTO) {
        val options = arrayOf("Usuń playlistę")
        AlertDialog.Builder(requireContext())
            .setTitle(playlist.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> confirmDeletePlaylist(playlist)
                }
            }
            .show()
    }

    private fun confirmDeletePlaylist(playlist: PlaylistDTO) {
        AlertDialog.Builder(requireContext())
            .setTitle("Usuń playlistę")
            .setMessage("Czy na pewno chcesz usunąć playlistę \"${playlist.name}\"?")
            .setPositiveButton("Usuń") { _, _ -> deletePlaylistById(playlist.id ?: return@setPositiveButton) }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun deletePlaylistById(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.deletePlaylist(id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Playlista usunięta", Toast.LENGTH_SHORT).show()
                    viewModel.fetchLibraryData(sessionManager.getUserId())
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error deleting playlist", e)
            }
        }
    }
}
