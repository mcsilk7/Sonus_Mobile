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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.sonus.*
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var albumAdapter: AlbumAdapter
    
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var rvAlbums: RecyclerView
    
    private lateinit var btnAddPlaylist: LinearLayout
    private lateinit var btnGoToFavorites: View
    private lateinit var sessionManager: SessionManager

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
        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        initViews(view)
        setupRecyclerViews()
        setupTabs()
        observeViewModel()
        applyThemeStrings(view)
        
        mainViewModel.libraryFilter.observe(viewLifecycleOwner) { filter ->
            when (filter) {
                0 -> { updateTabSelection(tabAll); showSections(true, true) }
                1 -> { updateTabSelection(tabPlaylists); showSections(true, false) }
                2 -> { updateTabSelection(tabAlbums); showSections(false, true) }
            }
        }
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
        tabAll.setOnClickListener { 
            mainViewModel.setLibraryFilter(0)
        }
        tabPlaylists.setOnClickListener { 
            mainViewModel.setLibraryFilter(1)
        }
        tabAlbums.setOnClickListener { 
            mainViewModel.setLibraryFilter(2)
        }
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

    private fun applyThemeStrings(view: View) {
        val context = requireContext()
        view.findViewById<TextView>(R.id.tvLibHeaderTop).text = LabelProvider.getLabel(context, "library_header_top")
        view.findViewById<TextView>(R.id.tvLibHeaderMain).text = LabelProvider.getLabel(context, "library_header_main")
        
        view.findViewById<TextView>(R.id.tabAll).text = LabelProvider.getLabel(context, "library_tab_all")
        view.findViewById<TextView>(R.id.tabPlaylists).text = LabelProvider.getLabel(context, "library_tab_playlists")
        view.findViewById<TextView>(R.id.tabAlbums).text = LabelProvider.getLabel(context, "library_tab_albums")
        
        view.findViewById<TextView>(R.id.tvLibSecPlaylists).text = LabelProvider.getLabel(context, "library_sec_playlists")
        view.findViewById<TextView>(R.id.tvLibSecAlbums).text = LabelProvider.getLabel(context, "library_sec_albums")
        
        // Add Playlist Item
        view.findViewById<TextView>(R.id.tvAddPlaylistTitle).text = LabelProvider.getLabel(context, "library_init_playlist")
        view.findViewById<TextView>(R.id.tvAddPlaylistDesc).text = LabelProvider.getLabel(context, "library_create_disk")
        view.findViewById<TextView>(R.id.tvAddPlaylistExe).text = LabelProvider.getLabel(context, "library_exe")

        // Favorite Module
        view.findViewById<TextView>(R.id.tvLibFavSignals).text = LabelProvider.getLabel(context, "library_fav_signals")
        view.findViewById<TextView>(R.id.tvLibFavOpenArchive).text = LabelProvider.getLabel(context, "library_open_archive")
        view.findViewById<TextView>(R.id.tvLibFavOpen).text = LabelProvider.getLabel(context, "library_open")
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
                        Toast.makeText(requireContext(), getString(R.string.toast_song_removed), Toast.LENGTH_SHORT).show()
                        viewModel.fetchLibraryData(userId)
                    }
                } else {
                    val response = RetrofitClient.albumApi.addAlbumToLibrary(album.id!!, userId)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), getString(R.string.toast_added_to_playlist), Toast.LENGTH_SHORT).show()
                        viewModel.fetchLibraryData(userId)
                    }
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error toggling album", e)
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        val view = layoutInflater.inflate(R.layout.dialog_create_playlist, null)
        val etName = view.findViewById<EditText>(R.id.etPlaylistName)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnConfirm = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnConfirm)

        val title = view.findViewById<TextView>(R.id.tvCreatePlaylistTitle)
        val subtitle = view.findViewById<TextView>(R.id.tvCreatePlaylistSubtitle)

        title.text = if (isTechnical) getString(R.string.init_new_unit) else getString(R.string.new_playlist_norm)
        subtitle.text = if (isTechnical) getString(R.string.assign_label_identifier) else getString(R.string.new_disk_norm)
        etName.hint = if (isTechnical) getString(R.string.hint_enter_name_technical) else getString(R.string.hint_enter_name_pl)
        btnCancel.text = if (isTechnical) getString(R.string.abort_bracket) else getString(R.string.cancel_pl)
        btnConfirm.text = if (isTechnical) getString(R.string.initialize) else getString(R.string.save_pl)

        val dialog = AlertDialog.Builder(context)
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
                etName.error = getString(R.string.error_name_empty)
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
                    Toast.makeText(requireContext(), getString(R.string.toast_playlist_created), Toast.LENGTH_SHORT).show()
                    viewModel.fetchLibraryData(userId)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.toast_network_error, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPlaylistOptions(playlist: PlaylistDTO) {
        val options = arrayOf(getString(R.string.btn_delete))
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
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        val title = if (isTechnical) getString(R.string.confirm_delete_playlist_title).uppercase() else "Delete Playlist"
        val message = if (isTechnical) getString(R.string.confirm_delete_playlist_msg, playlist.name) else "Are you sure you want to delete \"${playlist.name}\"?"
        val confirm = if (isTechnical) getString(R.string.btn_delete).uppercase() else "Delete"
        val cancel = if (isTechnical) getString(R.string.btn_cancel).uppercase() else "Cancel"

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirm) { _, _ -> deletePlaylistById(playlist.id ?: return@setPositiveButton) }
            .setNegativeButton(cancel, null)
            .show()
    }

    private fun deletePlaylistById(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.playlistApi.deletePlaylist(id)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.toast_playlist_deleted), Toast.LENGTH_SHORT).show()
                    viewModel.fetchLibraryData(sessionManager.getUserId())
                }
            } catch (e: Exception) {
                Log.e("SonusLibrary", "Error deleting playlist", e)
            }
        }
    }
}
