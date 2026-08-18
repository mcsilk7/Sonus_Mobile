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

    private val _terminalLogs = MutableLiveData<List<String>>()
    val terminalLogs: LiveData<List<String>> = _terminalLogs

    init {
        _terminalLogs.value = listOf(
            "INITIALIZING_SYSTEM_CORE...",
            "BOOT_SEQUENCE_COMPLETE",
            "AUTHENTICATING_OPERATOR_V1.0",
            "CONNECTING_TO_REMOTE_SERVER...",
            "SCANNING_ARCHIVE_MODULES...",
            "SYSTEM_STATUS: OPTIMAL",
            "WAITING_FOR_SIGNAL..."
        )
    }

    fun addTerminalLog(message: String) {
        val current = _terminalLogs.value?.toMutableList() ?: mutableListOf()
        current.add(message.uppercase())
        if (current.size > 20) current.removeAt(0)
        _terminalLogs.value = current
    }

    fun loadRecentlyPlayed(userId: Long, localRecentSongs: List<SongDTO>) {
        viewModelScope.launch {
            val enrichedSongs = repository.enrichSongMetadata(userId, localRecentSongs)
            _recentlyPlayed.value = enrichedSongs
        }
    }
}
