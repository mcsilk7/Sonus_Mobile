package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.network.SongDTO
import com.example.sonus.network.PlaylistDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.DesktopDI

class SearchViewModel(private val scope: CoroutineScope) {
    var query by mutableStateOf("")
    var results by mutableStateOf<List<SongDTO>>(emptyList())
    var history by mutableStateOf<List<String>>(emptyList())
    var playlists by mutableStateOf<List<PlaylistDTO>>(emptyList())
    var isSearching by mutableStateOf(false)
    
    private var searchJob: Job? = null

    init {
        observeHistory()
        loadPlaylists()
    }

    private fun loadPlaylists() {
        val userId = DesktopDI.sessionManager.getUserId()
        if (userId == -1L) return
        scope.launch {
            DesktopDI.container.repository.getPlaylistsFlow().collect {
                playlists = it
            }
        }
    }

    private fun observeHistory() {
        scope.launch {
            DesktopDI.container.repository.getSearchHistoryFlow().collect {
                history = it
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()
        searchJob = scope.launch(Dispatchers.IO) {
            if (newQuery.length < 2) {
                results = emptyList()
                return@launch
            }
            
            delay(500) // Debounce
            
            // Zapisz do historii
            DesktopDI.container.repository.addSearchQuery(newQuery)
            
            isSearching = true
            try {
                results = DesktopDI.container.repository.searchSongs(newQuery)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isSearching = false
            }
        }
    }

    fun toggleFavorite(song: SongDTO) {
        val userId = DesktopDI.sessionManager.getUserId()
        if (userId == -1L) return
        scope.launch {
            val result = DesktopDI.container.repository.toggleFavorite(userId, song)
            if (result != null) {
                results = results.map { 
                    if (it.id == song.id) it.copy(isFavorite = result) else it 
                }
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        scope.launch {
            DesktopDI.container.repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun clearHistory() {
        scope.launch {
            DesktopDI.container.repository.clearSearchHistory()
        }
    }

    fun deleteHistoryItem(item: String) {
        scope.launch {
            DesktopDI.container.repository.deleteSearchQuery(item)
        }
    }
}
