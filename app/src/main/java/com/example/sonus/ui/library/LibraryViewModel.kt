package com.example.sonus.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO
import com.example.sonus.repository.MusicRepository
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    private val repository = MusicRepository()

    private val _playlists = MutableLiveData<List<PlaylistDTO>>()
    val playlists: LiveData<List<PlaylistDTO>> = _playlists

    private val _favoriteSongs = MutableLiveData<List<SongDTO>>()
    val favoriteSongs: LiveData<List<SongDTO>> = _favoriteSongs

    private val _libraryAlbums = MutableLiveData<List<AlbumDTO>>()
    val libraryAlbums: LiveData<List<AlbumDTO>> = _libraryAlbums

    fun fetchLibraryData(userId: Long) {
        viewModelScope.launch {
            // Parallel execution
            launch {
                _playlists.value = repository.getUserPlaylists(userId)
            }
            launch {
                _favoriteSongs.value = repository.getFavoriteSongs(userId)
            }
            launch {
                _libraryAlbums.value = repository.getLibraryAlbums(userId)
            }
        }
    }

    fun toggleFavorite(userId: Long, song: SongDTO) {
        viewModelScope.launch {
            val added = repository.toggleFavorite(userId, song.id)
            if (added != null) {
                // Refresh favorites
                _favoriteSongs.value = repository.getFavoriteSongs(userId)
            }
        }
    }
}
