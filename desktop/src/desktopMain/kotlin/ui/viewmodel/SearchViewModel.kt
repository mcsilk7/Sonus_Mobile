package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.DesktopDI

class SearchViewModel(private val scope: CoroutineScope) {
    var query by mutableStateOf("")
    var results by mutableStateOf<List<SongDTO>>(emptyList())
    var isSearching by mutableStateOf(false)
    
    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()
        searchJob = scope.launch(Dispatchers.IO) {
            if (newQuery.length < 2) {
                results = emptyList()
                return@launch
            }
            
            delay(500) // Debounce
            isSearching = true
            try {
                // Assuming shared module will have search functionality or we use apiService directly
                // For now, let's mock it or use apiService
                // val songs = DesktopDI.container.apiService.searchSongs(newQuery)
                // results = DesktopDI.container.repository.enrichSongMetadata(songs)
            } catch (e: Exception) {
                // Handle error
            } finally {
                isSearching = false
            }
        }
    }
}
