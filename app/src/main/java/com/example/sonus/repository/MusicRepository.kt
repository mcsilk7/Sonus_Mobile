package com.example.sonus.repository

import android.content.Context
import com.example.sonus.NetworkHelper
import com.example.sonus.SortOrder
import com.example.sonus.db.*
import com.example.sonus.network.*
import kotlinx.coroutines.Dispatchers
import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class MusicRepository {

    private fun getDao(context: Context) = SonusDatabase.getDatabase(context).musicDao()
    
    private val httpDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }

    private suspend fun getIfModifiedSince(dao: MusicDao, key: String): String? {
        val metadata = dao.getSyncMetadata(key)
        // Only return timestamp if last sync was more than 1 minute ago to avoid spamming
        return if (metadata != null && System.currentTimeMillis() - metadata.lastSyncMillis > 60000) {
            metadata.lastSyncTimestamp
        } else null
    }

    private suspend fun updateSyncMetadata(dao: MusicDao, key: String) {
        val now = System.currentTimeMillis()
        dao.insertSyncMetadata(SyncMetadata(key, httpDateFormat.format(Date(now)), now))
    }

    fun getPlaylistsFlow(context: Context): Flow<List<PlaylistDTO>> {
        return getDao(context).getAllPlaylistsFlow().map { list -> list.map { it.toDTO() } }
    }

    fun getFavoriteSongsFlow(context: Context): Flow<List<SongDTO>> {
        return getDao(context).getFavoriteSongsFlow().map { list -> list.map { it.toDTO() } }
    }

    fun getAlbumsFlow(context: Context): Flow<List<AlbumDTO>> {
        return getDao(context).getAllAlbumsFlow().map { list -> list.map { it.toDTO() } }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFavoriteSongsPaging(context: Context, userId: Long, sortOrder: SortOrder = SortOrder.DEFAULT): Flow<PagingData<SongDTO>> {
        val database = SonusDatabase.getDatabase(context)
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = FavoriteRemoteMediator(database, userId),
            pagingSourceFactory = { 
                when (sortOrder) {
                    SortOrder.DEFAULT -> database.musicDao().getFavoriteSongsPaging()
                    SortOrder.TITLE -> database.musicDao().getFavoriteSongsPagingByTitle()
                    SortOrder.ARTIST -> database.musicDao().getFavoriteSongsPagingByArtist()
                    SortOrder.DURATION -> database.musicDao().getFavoriteSongsPagingByDuration()
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDTO() }
        }
    }

    suspend fun refreshUserPlaylists(context: Context, userId: Long) = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) return@withContext
        val dao = getDao(context)
        val ifModifiedSince = getIfModifiedSince(dao, "playlists")

        try {
            val response = RetrofitClient.playlistApi.getUserPlaylists(userId, ifModifiedSince = ifModifiedSince)
            
            if (response.code() == 304) {
                updateSyncMetadata(dao, "playlists")
                return@withContext
            }

            if (response.isSuccessful) {
                val playlists = response.body() ?: emptyList()
                
                val enriched = coroutineScope {
                    playlists.map { playlist ->
                        async {
                            try {
                                val countRes = RetrofitClient.playlistApi.getSongCountInPlaylist(playlist.id!!)
                                val songsRes = RetrofitClient.playlistApi.getSongsInPlaylist(playlist.id!!)
                                
                                val count = if (countRes.isSuccessful) countRes.body()?.toInt() ?: 0 else 0
                                val songs = if (songsRes.isSuccessful) songsRes.body()?.take(4) else null
                                
                                val result = playlist.copy(songCount = count, songs = songs)
                                
                                songs?.let { sList ->
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
                updateSyncMetadata(dao, "playlists")
            }
        } catch (e: Exception) {
            // Handled
        }
    }

    suspend fun refreshFavoriteSongs(context: Context, userId: Long) = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) return@withContext
        val dao = getDao(context)
        val ifModifiedSince = getIfModifiedSince(dao, "favorites")

        try {
            val response = RetrofitClient.favoriteApi.getFavorites(userId, ifModifiedSince = ifModifiedSince)
            
            if (response.code() == 304) {
                updateSyncMetadata(dao, "favorites")
                return@withContext
            }

            if (response.isSuccessful) {
                val songs = response.body()?.mapNotNull { fav ->
                    val song = (fav.song ?: fav.songDto) ?: fav.songTitle?.let { title ->
                        SongDTO(
                            id = fav.songId,
                            title = title,
                            artist = fav.songArtist ?: "Nieznany artysta",
                            duration = fav.songDuration,
                            coverPath = fav.coverPath,
                            isFavorite = true
                        )
                    }
                    song?.apply { isFavorite = true }
                } ?: emptyList()
                
                dao.removeObsoleteFavorites(songs.map { it.id })
                dao.insertSongs(songs.map { it.toEntity() })
                updateSyncMetadata(dao, "favorites")
            }
        } catch (e: Exception) {
            // Handled
        }
    }

    suspend fun refreshLibraryAlbums(context: Context, userId: Long) = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) return@withContext
        val dao = getDao(context)
        val ifModifiedSince = getIfModifiedSince(dao, "albums")
        
        try {
            val response = RetrofitClient.albumApi.getLibraryAlbums(userId, ifModifiedSince = ifModifiedSince)
            
            if (response.code() == 304) {
                updateSyncMetadata(dao, "albums")
                return@withContext
            }

            if (response.isSuccessful) {
                val albums = response.body()?.onEach { it.isSaved = true } ?: emptyList()
                dao.removeObsoleteAlbums(albums.map { it.id ?: -1L })
                dao.insertAlbums(albums.map { it.toEntity() })
                updateSyncMetadata(dao, "albums")
            }
        } catch (e: Exception) {
            // Handled
        }
    }

    suspend fun getUserPlaylists(context: Context, userId: Long): List<PlaylistDTO> = withContext(Dispatchers.IO) {
        // Fallback to suspend version for compatibility if needed, but UI should use Flow
        refreshUserPlaylists(context, userId)
        getDao(context).getAllPlaylists().map { it.toDTO() }
    }

    suspend fun getFavoriteSongs(context: Context, userId: Long): List<SongDTO> = withContext(Dispatchers.IO) {
        refreshFavoriteSongs(context, userId)
        getDao(context).getFavoriteSongs().map { it.toDTO() }
    }

    suspend fun getLibraryAlbums(context: Context, userId: Long): List<AlbumDTO> = withContext(Dispatchers.IO) {
        refreshLibraryAlbums(context, userId)
        getDao(context).getAllAlbums().map { it.toDTO() }
    }

    suspend fun getPlaylistDetails(context: Context, playlistId: Long, userId: Long): PlaylistDTO? = withContext(Dispatchers.IO) {
        val dao = getDao(context)
        val isNetworkDown = !NetworkHelper.isNetworkAvailable(context)
        
        if (isNetworkDown) {
            val cachedSongs = dao.getSongsForPlaylist(playlistId).map { it.toDTO() }
            val playlistEntity = dao.getAllPlaylists().find { it.id == playlistId }
            return@withContext playlistEntity?.toDTO()?.copy(songs = cachedSongs)
        }

        try {
            val response = RetrofitClient.playlistApi.getPlaylistById(playlistId)
            if (response.isSuccessful) {
                var playlist = response.body()
                if (playlist != null) {
                    val songsResponse = RetrofitClient.playlistApi.getSongsInPlaylist(playlistId)
                    if (songsResponse.isSuccessful) {
                        playlist = playlist.copy(songs = songsResponse.body())
                    }
                    
                    // Cache
                    dao.insertPlaylists(listOf(playlist.toEntity()))
                    val rawSongs = playlist.songs ?: emptyList()
                    val songsWithMetadata = enrichSongMetadata(context, userId, rawSongs)
                    
                    dao.insertSongs(songsWithMetadata.map { it.toEntity() })
                    dao.insertPlaylistSongs(songsWithMetadata.map { PlaylistSongCrossRef(playlistId, it.id) })
                    
                    return@withContext playlist.copy(songs = songsWithMetadata)
                }
            }
        } catch (e: Exception) {
            null
        }
        null
    }

    suspend fun getAlbumDetails(context: Context, albumId: Long, userId: Long): AlbumDTO? = withContext(Dispatchers.IO) {
        val dao = getDao(context)
        val isNetworkDown = !NetworkHelper.isNetworkAvailable(context)

        if (isNetworkDown) {
            val albumEntity = dao.getAllAlbums().find { it.id == albumId }
            return@withContext albumEntity?.toDTO()
        }

        try {
            val response = RetrofitClient.albumApi.getAlbumById(albumId)
            if (response.isSuccessful) {
                var album = response.body()
                if (album != null) {
                    val songsResponse = RetrofitClient.albumApi.getSongsInAlbum(albumId)
                    if (songsResponse.isSuccessful) {
                        album = album.copy(songs = songsResponse.body())
                    }
                    
                    dao.insertAlbums(listOf(album.toEntity()))
                    val rawSongs = album.songs?.onEach { it.albumId = albumId } ?: emptyList()
                    val songsWithMetadata = enrichSongMetadata(context, userId, rawSongs)
                    
                    // Save to cache
                    dao.insertSongs(songsWithMetadata.map { it.toEntity() })
                    
                    return@withContext album.copy(songs = songsWithMetadata)
                }
            }
        } catch (e: Exception) {
            null
        }
        null
    }

    suspend fun toggleFavorite(context: Context, userId: Long, song: SongDTO): Boolean? = withContext(Dispatchers.IO) {
        val dao = getDao(context)
        
        // Optimistic update in DB
        val newStatus = !song.isFavorite
        dao.updateFavorite(song.id, newStatus)
        
        try {
            val response = RetrofitClient.favoriteApi.toggleFavorite(userId, song.id)
            if (response.isSuccessful) {
                response.body()?.get("added")
            } else {
                dao.insertSyncAction(SyncAction(actionType = "TOGGLE_FAVORITE", songId = song.id))
                newStatus
            }
        } catch (e: Exception) {
            dao.insertSyncAction(SyncAction(actionType = "TOGGLE_FAVORITE", songId = song.id))
            newStatus
        }
    }
    
    suspend fun enrichSongMetadata(context: Context, userId: Long, songs: List<SongDTO>): List<SongDTO> = withContext(Dispatchers.IO) {
        val dao = getDao(context)
        val favoriteIds = dao.getFavoriteSongs().map { it.id }.toSet()
        val inPlaylistIds = dao.getAllSongIdsInPlaylists().toSet()
        
        songs.onEach { song ->
            song.isFavorite = favoriteIds.contains(song.id)
            song.isInPlaylist = inPlaylistIds.contains(song.id)
        }
        songs
    }

    suspend fun enrichAlbumMetadata(context: Context, albums: List<AlbumDTO>): List<AlbumDTO> = withContext(Dispatchers.IO) {
        val dao = getDao(context)
        val savedAlbumIds = dao.getAllAlbums().filter { it.isSaved }.map { it.id }.toSet()
        
        albums.onEach { album ->
            album.isSaved = savedAlbumIds.contains(album.id)
        }
        albums
    }

    // Mapper extensions
    private fun SongDTO.toEntity() = SongEntity(id, title, artist, duration, coverPath, blurHash, filePath, albumId, isFavorite, isInPlaylist)
    private fun SongEntity.toDTO() = SongDTO(id, title, artist, duration, coverPath, blurHash, filePath, albumId, isFavorite, isInPlaylist)
    private fun AlbumDTO.toEntity() = AlbumEntity(id ?: -1L, title, artist, coverPath, blurHash, isSaved)
    private fun AlbumEntity.toDTO() = AlbumDTO(id, title, artist, coverPath, blurHash, isSaved = isSaved)
    private fun PlaylistDTO.toEntity() = PlaylistEntity(id ?: -1L, name, description, songCount)
    private fun PlaylistEntity.toDTO() = PlaylistDTO(id, name, description, songCount = songCount)

    private fun PlaylistWithSongs.toDTO(): PlaylistDTO {
        return playlist.toDTO().copy(songs = songs.map { it.toDTO() })
    }
}
