package com.example.sonus.ui.library

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.paging.*
import androidx.work.*
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO
import com.example.sonus.network.SyncWorker
import com.example.sonus.SortOrder
import com.example.sonus.SonusApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SonusApp.di.repository
    
    val playlists: LiveData<List<PlaylistDTO>> = repository.getPlaylistsFlow().asLiveData()
    val favoriteSongs: LiveData<List<SongDTO>> = repository.getFavoriteSongsFlow().asLiveData()
    val libraryAlbums: LiveData<List<AlbumDTO>> = repository.getAlbumsFlow().asLiveData()

    private val _favoriteSortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val favoriteSortOrder: StateFlow<SortOrder> = _favoriteSortOrder

    fun setFavoriteSortOrder(order: SortOrder) {
        _favoriteSortOrder.value = order
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFavoriteSongsPaging(userId: Long): Flow<PagingData<SongDTO>> {
        return _favoriteSortOrder.flatMapLatest { order ->
            repository.getFavoriteSongsPaging(userId, order)
        }.cachedIn(viewModelScope)
    }

    suspend fun getAllFavoriteSongs(userId: Long): List<SongDTO> {
        return repository.getFavoriteSongs(userId)
    }

    fun fetchLibraryData(userId: Long) {
        viewModelScope.launch {
            try {
                // Trigger refreshes in parallel
                launch { repository.refreshUserPlaylists(userId) }
                launch { repository.refreshFavoriteSongs(userId) }
                launch { repository.refreshLibraryAlbums(userId) }
            } catch (e: Exception) {
                // Logged in repository
            }
        }
    }

    fun toggleFavorite(userId: Long, song: SongDTO) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(userId, song)
                
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                
                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .build()
                
                WorkManager.getInstance(getApplication()).enqueueUniqueWork(
                    "sync_favorites",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    syncRequest
                )
            } catch (e: Exception) {
                // Handled
            }
        }
    }
}
