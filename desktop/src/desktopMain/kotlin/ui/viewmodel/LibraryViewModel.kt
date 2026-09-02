package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ui.DesktopDI

class LibraryViewModel(private val scope: CoroutineScope) {
    var playlists by mutableStateOf<List<PlaylistDTO>>(emptyList())
    var favorites by mutableStateOf<List<SongDTO>>(emptyList())

    init {
        observeData()
    }

    private fun observeData() {
        scope.launch {
            DesktopDI.container.repository.getPlaylistsFlow().collectLatest {
                playlists = it
            }
        }
        scope.launch {
            DesktopDI.container.repository.getFavoriteSongsFlow().collectLatest {
                favorites = it
            }
        }
    }
}
