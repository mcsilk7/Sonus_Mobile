package com.example.sonus.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonus.network.SongDTO
import com.example.sonus.repository.MusicRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MusicRepository()

    private val _recentlyPlayed = MutableLiveData<List<SongDTO>>()
    val recentlyPlayed: LiveData<List<SongDTO>> = _recentlyPlayed

    private val _isOffline = MutableLiveData<Boolean>(false)
    val isOffline: LiveData<Boolean> = _isOffline

    fun loadRecentlyPlayed(userId: Long, localRecentSongs: List<SongDTO>, offline: Boolean = false) {
        viewModelScope.launch {
            _isOffline.value = offline
            if (offline) {
                // In offline mode, skip server enrichment
                _recentlyPlayed.value = localRecentSongs
            } else {
                try {
                    val enrichedSongs = repository.enrichSongMetadata(userId, localRecentSongs)
                    _recentlyPlayed.value = enrichedSongs
                } catch (e: Exception) {
                    // Fallback to local songs if enrichment fails
                    _recentlyPlayed.value = localRecentSongs
                }
            }
        }
    }
}
