package com.example.sonus.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sonus.*
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private lateinit var etSearch: EditText
    private lateinit var rvSongs: RecyclerView
    private lateinit var rvAlbums: RecyclerView
    private lateinit var tvSongsHeader: TextView
    private lateinit var tvAlbumsHeader: TextView
    
    private lateinit var songAdapter: SongAdapter
    private lateinit var albumAdapter: AlbumAdapter
    
    private lateinit var sessionManager: SessionManager
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        initViews(view)
        setupRecyclerViews()
        setupSearchListener()
    }

    private fun initViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch)
        rvSongs = view.findViewById(R.id.rvSongResults)
        rvAlbums = view.findViewById(R.id.rvAlbumResults)
        tvSongsHeader = view.findViewById(R.id.tvSongsHeader)
        tvAlbumsHeader = view.findViewById(R.id.tvAlbumsHeader)
    }

    private fun setupRecyclerViews() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                PlayerState.play(requireContext(), song, songAdapter.getSongs())
                Toast.makeText(requireContext(), "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(requireActivity() as androidx.appcompat.app.AppCompatActivity, viewLifecycleOwner.lifecycleScope, sessionManager.getUserId(), song) {
                    songAdapter.notifyDataSetChanged()
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

        albumAdapter = AlbumAdapter(
            albums = emptyList(),
            onItemClick = { album ->
                val bundle = Bundle().apply { putLong("ALBUM_ID", album.id ?: -1L) }
                findNavController().navigate(R.id.albumDetailFragment, bundle)
            }
        )
        rvAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        rvAlbums.adapter = albumAdapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    performSearch(query)
                } else {
                    clearResults()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(300) // Debounce
            try {
                val songsResponse = RetrofitClient.searchApi.searchSongs(query)
                val albumsResponse = RetrofitClient.searchApi.searchAlbums(query)

                if (songsResponse.isSuccessful) {
                    val songs = songsResponse.body() ?: emptyList()
                    songAdapter.updateData(songs)
                    tvSongsHeader.visibility = if (songs.isNotEmpty()) View.VISIBLE else View.GONE
                }

                if (albumsResponse.isSuccessful) {
                    val albums = albumsResponse.body() ?: emptyList()
                    albumAdapter.updateData(albums)
                    tvAlbumsHeader.visibility = if (albums.isNotEmpty()) View.VISIBLE else View.GONE
                }
            } catch (e: Exception) {
                // Ignore search errors
            }
        }
    }

    private fun clearResults() {
        searchJob?.cancel()
        songAdapter.updateData(emptyList())
        albumAdapter.updateData(emptyList())
        tvSongsHeader.visibility = View.GONE
        tvAlbumsHeader.visibility = View.GONE
    }
}
