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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadRecentlyPlayed(userId: Long, localRecentSongs: List<SongDTO>) {
        viewModelScope.launch {
            _isLoading.value = true
            val enrichedSongs = repository.enrichSongMetadata(userId, localRecentSongs)
            _recentlyPlayed.value = enrichedSongs
            _isLoading.value = false
        }
    }
}
