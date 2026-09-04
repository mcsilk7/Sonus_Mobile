package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ui.DesktopDI

class LibraryViewModel(private val scope: CoroutineScope) {
    var playlists by mutableStateOf<List<PlaylistDTO>>(emptyList())
    var albums by mutableStateOf<List<AlbumDTO>>(emptyList())
    var favorites by mutableStateOf<List<SongDTO>>(emptyList())
    var isDeleting by mutableStateOf(false)

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
            DesktopDI.container.repository.getFavoriteSongsFlow().collectLatest {
                favorites = it
            }
        }
    }

    fun refreshData() {
        val userId = DesktopDI.sessionManager.getUserId()
        if (userId == -1L) return

        scope.launch {
            try {
                DesktopDI.container.repository.refreshUserPlaylists(userId)
                DesktopDI.container.repository.refreshLibraryAlbums(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createPlaylist(name: String) {
        val userId = DesktopDI.sessionManager.getUserId()
        if (userId == -1L) return

        scope.launch {
            try {
                DesktopDI.container.repository.createPlaylist(userId, name, null)
                DesktopDI.container.repository.refreshUserPlaylists(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        isDeleting = true
        scope.launch {
            try {
                DesktopDI.container.repository.deletePlaylist(playlistId)
                DesktopDI.container.repository.refreshUserPlaylists(DesktopDI.sessionManager.getUserId())
            } catch (e: Exception) {
                // Handle error
            } finally {
                isDeleting = false
            }
        }
    }
}
