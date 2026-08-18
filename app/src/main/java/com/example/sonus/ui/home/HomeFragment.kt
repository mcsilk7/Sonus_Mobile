package com.example.sonus.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
    private lateinit var rvRecentlyPlayed: RecyclerView
    private lateinit var rvTerminalLog: RecyclerView
    private lateinit var tapeAdapter: TapeReelAdapter
    private lateinit var terminalLogAdapter: TerminalLogAdapter
    private lateinit var tvRecentlyPlayedHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private val playerListener = object : PlayerState.PlayerStateListener {
        override fun onStateChanged() {
            val song = PlayerState.currentSong
            if (PlayerState.isPlaying && song != null) {
                viewModel.addTerminalLog(getString(R.string.terminal_signal_locked, song.title))
                viewModel.addTerminalLog(getString(R.string.terminal_buffering, song.id.toString(16).uppercase()))
            }
            // AUTO-REFRESH ARCHIVE on any player state change (like new song played)
            viewModel.loadRecentlyPlayed(
                sessionManager.getUserId(),
                RecentlyPlayedManager.getRecentSongs()
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        checkNotificationPermission()
        PlayerState.addStateListener(playerListener)

        val cardFavorites = view.findViewById<View>(R.id.cardFavorites)
        val cardPlaylists = view.findViewById<View>(R.id.cardPlaylists)

        cardFavorites.setOnClickListener {
            findNavController().navigate(R.id.favoriteFragment)
        }
        cardPlaylists.setOnClickListener {
            mainViewModel.setLibraryFilter(1) // 1: PLAYLISTS
            mainViewModel.setTabPage(2) // 2: LIB
        }

        rvRecentlyPlayed = view.findViewById(R.id.rvRecentlyPlayed)
        rvTerminalLog = view.findViewById(R.id.rvTerminalLog)
        tvRecentlyPlayedHeader = view.findViewById(R.id.tvRecentlyPlayedHeader)
        
        setupRecentlyPlayed()
        setupTerminal()
        observeViewModel()
        applyThemeStrings(view)
    }

    private fun applyThemeStrings(view: View) {
        val context = requireContext()
        view.findViewById<TextView>(R.id.tvHomeHeaderTop).text = LabelProvider.getLabel(context, "home_header_top")
        view.findViewById<TextView>(R.id.tvHomeHeaderMain).text = LabelProvider.getLabel(context, "home_header_main")
        view.findViewById<TextView>(R.id.tvFavCardLabel).text = LabelProvider.getLabel(context, "home_fav_card")
        view.findViewById<TextView>(R.id.tvPlCardLabel).text = LabelProvider.getLabel(context, "home_pl_card")
        
        view.findViewById<TextView>(R.id.tvFavCardDesc).text = LabelProvider.getLabel(context, "home_fav_desc")
        view.findViewById<TextView>(R.id.tvPlCardDesc).text = LabelProvider.getLabel(context, "home_pl_desc")
        view.findViewById<TextView>(R.id.tvFavCardAccess).text = LabelProvider.getLabel(context, "home_access")
        view.findViewById<TextView>(R.id.tvPlCardAccess).text = LabelProvider.getLabel(context, "home_access")
        
        view.findViewById<TextView>(R.id.tvTerminalHeader).text = LabelProvider.getLabel(context, "home_log_header")
        view.findViewById<TextView>(R.id.tvTerminalLive).text = LabelProvider.getLabel(context, "home_live_feed")
        
        tvRecentlyPlayedHeader.text = LabelProvider.getLabel(context, "home_recent_header")
    }

    private fun setupTerminal() {
        terminalLogAdapter = TerminalLogAdapter()
        rvTerminalLog.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Traditional terminal behavior
        }
        rvTerminalLog.adapter = terminalLogAdapter
    }

    private fun observeViewModel() {
        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { songs ->
            if (songs.isEmpty()) {
                rvRecentlyPlayed.visibility = View.GONE
            } else {
                rvRecentlyPlayed.visibility = View.VISIBLE
                tapeAdapter.updateData(songs)
            }
        }

        viewModel.terminalLogs.observe(viewLifecycleOwner) { logs ->
            terminalLogAdapter.setLogs(logs)
            rvTerminalLog.scrollToPosition(terminalLogAdapter.itemCount - 1)
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
        viewModel.addTerminalLog(getString(R.string.terminal_session_resumed))
        viewModel.loadRecentlyPlayed(
            sessionManager.getUserId(),
            RecentlyPlayedManager.getRecentSongs()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PlayerState.removeStateListener(playerListener)
    }

    private fun setupRecentlyPlayed() {
        tapeAdapter = TapeReelAdapter(emptyList()) { song ->
            playSong(song)
        }
        rvRecentlyPlayed.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvRecentlyPlayed.adapter = tapeAdapter

        // Swipe removed for horizontal tape rack to avoid gesture conflicts
    }

    private fun playSong(song: SongDTO) {
        RecentlyPlayedManager.addSong(song)
        val updatedRecentSongs = RecentlyPlayedManager.getRecentSongs()
        viewModel.loadRecentlyPlayed(sessionManager.getUserId(), updatedRecentSongs)
        viewModel.addTerminalLog(getString(R.string.terminal_reel_loaded, song.title.uppercase()))
        
        PlayerState.play(requireContext(), song, updatedRecentSongs)
        Toast.makeText(requireContext(), getString(R.string.toast_playing, song.title), Toast.LENGTH_SHORT).show()
    }
}
