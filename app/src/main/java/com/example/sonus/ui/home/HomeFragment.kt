package com.example.sonus.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.sonus.*
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO

class HomeFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var recentlyPlayedManager: RecentlyPlayedManager
    private lateinit var rvRecentlyPlayed: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var tvRecentlyPlayedHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        recentlyPlayedManager = RecentlyPlayedManager(requireContext())

        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        checkNotificationPermission()

        val searchBar = view.findViewById<View>(R.id.searchBar)
        val etSearch = searchBar.findViewById<EditText>(R.id.etSearch)
        val ivSearchIcon = searchBar.findViewById<View>(R.id.ivSearchIcon)
        val cardFavorites = view.findViewById<View>(R.id.cardFavorites)
        val cardPlaylists = view.findViewById<View>(R.id.cardPlaylists)

        val goToSearch = View.OnClickListener {
            mainViewModel.setTabPage(1)
        }

        val goToLibrary = View.OnClickListener {
            mainViewModel.setTabPage(2)
        }

        searchBar.setOnClickListener(goToSearch)
        etSearch.setOnClickListener(goToSearch)
        ivSearchIcon.setOnClickListener(goToSearch)

        cardFavorites.setOnClickListener(goToLibrary)
        cardPlaylists.setOnClickListener(goToLibrary)

        rvRecentlyPlayed = view.findViewById(R.id.rvRecentlyPlayed)
        tvRecentlyPlayedHeader = view.findViewById(R.id.tvRecentlyPlayedHeader)
        
        setupRecentlyPlayed()
        observeViewModel()

        etSearch.isFocusable = false
        etSearch.isCursorVisible = false
    }

    private fun observeViewModel() {
        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { songs ->
            if (songs.isEmpty()) {
                tvRecentlyPlayedHeader.visibility = View.GONE
                rvRecentlyPlayed.visibility = View.GONE
            } else {
                tvRecentlyPlayedHeader.visibility = View.VISIBLE
                rvRecentlyPlayed.visibility = View.VISIBLE
                songAdapter.updateData(songs)
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(requireContext(), "Brak powiadomień uniemożliwi sterowanie muzyką w tle", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRecentlyPlayed(
            sessionManager.getUserId(),
            recentlyPlayedManager.getRecentSongs()
        )
    }

    private fun setupRecentlyPlayed() {
        songAdapter = SongAdapter(
            songs = emptyList(),
            onItemClick = { song ->
                playSong(song)
            },
            onAddClick = { song ->
                PlaylistHelper.showPlaylistSelectionDialog(requireActivity() as androidx.appcompat.app.AppCompatActivity, viewLifecycleOwner.lifecycleScope, sessionManager.getUserId(), song) {
                    songAdapter.notifyDataSetChanged()
                }
            }
        )
        rvRecentlyPlayed.layoutManager = LinearLayoutManager(requireContext())
        rvRecentlyPlayed.adapter = songAdapter

        SongTouchHelper.attach(
            rvRecentlyPlayed, 
            songAdapter, 
            sessionManager.getUserId(), 
            viewLifecycleOwner.lifecycleScope
        ) {
            // Updated
        }
    }

    private fun playSong(song: SongDTO) {
        val recentSongs = recentlyPlayedManager.getRecentSongs()
        recentlyPlayedManager.addSong(song)
        viewModel.loadRecentlyPlayed(sessionManager.getUserId(), recentSongs)
        
        PlayerState.play(requireContext(), song, recentSongs)
        Toast.makeText(requireContext(), "Odtwarzanie: ${song.title}", Toast.LENGTH_SHORT).show()
    }
}
