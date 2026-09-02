package com.example.sonus.repository

import com.example.sonus.PlatformDateFormatter
import com.example.sonus.PlatformNetworkMonitor
import com.example.sonus.SortOrder
import com.example.sonus.db.*
import com.example.sonus.network.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.IO

class MusicRepository(
    private val dao: MusicDao,
    private val apiService: SonusApiService,
    private val networkMonitor: PlatformNetworkMonitor,
    private val dateFormatter: PlatformDateFormatter
) {

    private suspend fun getIfModifiedSince(key: String): String? {
        val metadata = dao.getSyncMetadata(key)
        return if (metadata != null) {
            metadata.lastSyncTimestamp
        } else null
    }

    private suspend fun updateSyncMetadata(key: String, nowMillis: Long) {
        dao.insertSyncMetadata(SyncMetadata(key, dateFormatter.formatHttpDate(nowMillis), nowMillis))
    }

    fun getPlaylistsFlow(): Flow<List<PlaylistDTO>> {
        return dao.getAllPlaylistsFlow().map { list -> list.map { it.toDTO() } }
    }

    fun getFavoriteSongsFlow(): Flow<List<SongDTO>> {
        return dao.getFavoriteSongsFlow().map { list -> list.map { it.toDTO() } }
    }

    fun getAlbumsFlow(): Flow<List<AlbumDTO>> {
        return dao.getAllAlbumsFlow().map { list -> list.map { it.toDTO() } }
    }

    suspend fun refreshUserPlaylists(userId: Long) = withContext(Dispatchers.IO) {
        if (!networkMonitor.isNetworkAvailable()) return@withContext
        val ifModifiedSince = getIfModifiedSince("playlists")

        try {
            val playlists = apiService.getUserPlaylists(userId, ifModifiedSince)
            
            val enriched = coroutineScope {
                playlists.map { playlist ->
                    async {
                        try {
                            val count = apiService.getSongCountInPlaylist(playlist.id!!)
                            val songs = apiService.getSongsInPlaylist(playlist.id!!)
                            
                            val result = playlist.copy(songCount = count, songs = songs)
                            
                            songs.let { sList ->
                                dao.insertSongs(sList.map { it.toEntity() })
                                dao.insertPlaylistSongs(sList.map { PlaylistSongCrossRef(playlist.id, it.id) })
                            }
                            result
                        } catch (e: Exception) {
                            playlist
                        }
                    }
                }.awaitAll()
            }
            
            dao.removeObsoletePlaylists(enriched.map { it.id ?: -1L })
            dao.insertPlaylists(enriched.map { it.toEntity() })
        } catch (e: Exception) {
            // Handled
        }
    }

    suspend fun refreshFavoriteSongs(userId: Long) = withContext(Dispatchers.IO) {
        if (!networkMonitor.isNetworkAvailable()) return@withContext
        val ifModifiedSince = getIfModifiedSince("favorites")

        try {
            val songs = apiService.getFavorites(userId, ifModifiedSince)
            
            dao.removeObsoleteFavorites(songs.map { it.id })
            dao.insertSongs(songs.map { it.toEntity() })
        } catch (e: Exception) {
            // Handled
        }
    }

    suspend fun refreshLibraryAlbums(userId: Long) = withContext(Dispatchers.IO) {
        if (!networkMonitor.isNetworkAvailable()) return@withContext
        val ifModifiedSince = getIfModifiedSince("albums")
        
        try {
            val albums = apiService.getLibraryAlbums(userId, ifModifiedSince)
            dao.removeObsoleteAlbums(albums.map { it.id ?: -1L })
            dao.insertAlbums(albums.map { it.toEntity() })
        } catch (e: Exception) {
            // Handled
        }
    }

    suspend fun getFavoriteSongs(userId: Long): List<SongDTO> = withContext(Dispatchers.IO) {
        dao.getFavoriteSongs().map { it.toDTO() }
    }

    fun getFavoriteSongsPaging(userId: Long, sortOrder: SortOrder): Flow<PagingData<SongDTO>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                when (sortOrder) {
                    SortOrder.TITLE -> dao.getFavoriteSongsPagingByTitle()
                    SortOrder.ARTIST -> dao.getFavoriteSongsPagingByArtist()
                    SortOrder.DURATION -> dao.getFavoriteSongsPagingByDuration()
                    else -> dao.getFavoriteSongsPaging()
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDTO() }
        }
    }

    suspend fun getPlaylistDetails(playlistId: Long): PlaylistDTO? = withContext(Dispatchers.IO) {
        val isNetworkDown = !networkMonitor.isNetworkAvailable()
        
        if (isNetworkDown) {
            val cachedSongs = dao.getSongsForPlaylist(playlistId).map { it.toDTO() }
            val playlistEntity = dao.getAllPlaylists().find { it.id == playlistId }
            return@withContext playlistEntity?.toDTO()?.copy(songs = cachedSongs)
        }

        try {
            val playlist = apiService.getPlaylistById(playlistId)
            val songs = apiService.getSongsInPlaylist(playlistId)
            
            // Cache
            dao.insertPlaylists(listOf(playlist.toEntity()))
            val songsWithMetadata = enrichSongMetadata(songs)
            
            dao.insertSongs(songsWithMetadata.map { it.toEntity() })
            dao.insertPlaylistSongs(songsWithMetadata.map { PlaylistSongCrossRef(playlistId, it.id) })
            
            return@withContext playlist.copy(songs = songsWithMetadata)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAlbumDetails(albumId: Long): AlbumDTO? = withContext(Dispatchers.IO) {
        val isNetworkDown = !networkMonitor.isNetworkAvailable()

        if (isNetworkDown) {
            val albumEntity = dao.getAllAlbums().find { it.id == albumId }
            return@withContext albumEntity?.toDTO()
        }

        try {
            val album = apiService.getAlbumById(albumId)
            val songs = apiService.getSongsInAlbum(albumId)
            
            dao.insertAlbums(listOf(album.toEntity()))
            val rawSongs = songs.onEach { it.albumId = albumId }
            val songsWithMetadata = enrichSongMetadata(rawSongs)
            
            // Save to cache
            dao.insertSongs(songsWithMetadata.map { it.toEntity() })
            
            return@withContext album.copy(songs = songsWithMetadata)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun toggleFavorite(userId: Long, song: SongDTO): Boolean? = withContext(Dispatchers.IO) {
        val newStatus = !song.isFavorite
        dao.updateFavorite(song.id, newStatus)
        
        try {
            val result = apiService.toggleFavorite(userId, song.id)
            result["added"]
        } catch (e: Exception) {
            dao.insertSyncAction(SyncAction(actionType = "TOGGLE_FAVORITE", songId = song.id))
            newStatus
        }
    }

    suspend fun enrichSongMetadata(songs: List<SongDTO>): List<SongDTO> = withContext(Dispatchers.IO) {
        val favoriteIds = dao.getFavoriteSongs().map { it.id }.toSet()
        val inPlaylistIds = dao.getAllSongIdsInPlaylists().toSet()
        
        songs.onEach { song ->
            song.isFavorite = favoriteIds.contains(song.id)
            song.isInPlaylist = inPlaylistIds.contains(song.id)
        }
        songs
    }

    suspend fun enrichAlbumMetadata(albums: List<AlbumDTO>): List<AlbumDTO> = withContext(Dispatchers.IO) {
        val savedAlbumIds = dao.getAllAlbums().filter { it.isSaved }.map { it.id }.toSet()
        
        albums.onEach { album ->
            album.isSaved = album.id?.let { savedAlbumIds.contains(it) } ?: false
        }
        albums
    }

    private fun PlaylistWithSongs.toDTO(): PlaylistDTO {
        return playlist.toDTO().copy(songs = songs.map { it.toDTO() })
    }
}
