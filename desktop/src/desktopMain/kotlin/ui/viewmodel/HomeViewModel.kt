package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ui.DesktopDI

class HomeViewModel(private val scope: CoroutineScope) {
    var playlists by mutableStateOf<List<PlaylistDTO>>(emptyList())
    var albums by mutableStateOf<List<AlbumDTO>>(emptyList())
    var recentlyPlayed by mutableStateOf<List<SongDTO>>(emptyList())
    var isRefreshing by mutableStateOf(false)

    init {
        observeData()
        refreshData()
    }

    private fun observeData() {
        scope.launch {
            DesktopDI.container.repository.getPlaylistsFlow().collectLatest {
                playlists = it
            }
        }
        scope.launch {
            DesktopDI.container.repository.getAlbumsFlow().collectLatest {
                albums = it
            }
        }
        scope.launch {
            DesktopDI.container.repository.getRecentlyPlayedFlow().collectLatest {
                recentlyPlayed = it
            }
        }
    }

    fun refreshData() {
        val userId = DesktopDI.sessionManager.getUserId()
        if (userId == -1L) return

        isRefreshing = true
        scope.launch(Dispatchers.IO) {
            try {
                DesktopDI.container.repository.refreshUserPlaylists(userId)
                DesktopDI.container.repository.refreshLibraryAlbums(userId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isRefreshing = false
            }
        }
    }
}
