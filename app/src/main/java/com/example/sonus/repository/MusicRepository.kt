package com.example.sonus.repository

import android.content.Context
import com.example.sonus.LibraryCacheManager
import com.example.sonus.NetworkHelper
import com.example.sonus.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class MusicRepository {

    suspend fun getUserPlaylists(context: Context, userId: Long): List<PlaylistDTO> = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            val cached = LibraryCacheManager.getCachedPlaylists(context)
            val downloadedSongs = com.example.sonus.DownloadManager.getDownloadedSongs(context)
            val downloadedSongIds = downloadedSongs.map { it.id }.toSet()
            
            return@withContext cached.filter { playlist ->
                // Check if we have any downloaded song that belongs to this playlist
                // Since summary DTO might have take(4), we check those first
                val hasSongsInSummary = playlist.songs?.any { it.id in downloadedSongIds } ?: false
                if (hasSongsInSummary) return@filter true
                
                // If not in summary, check the cached full detail
                val detail = LibraryCacheManager.getCachedPlaylistDetail(context, playlist.id ?: -1L)
                detail?.songs?.any { it.id in downloadedSongIds } ?: false
            }
        }

        try {
            val response = RetrofitClient.playlistApi.getUserPlaylists(userId)
            if (response.isSuccessful) {
                val playlists = response.body() ?: emptyList()
                // Optimization: Fetch counts and first 4 songs for collage in parallel
                val enriched = coroutineScope {
                    playlists.map { playlist ->
                        async {
                            try {
                                val countDeferred = async { RetrofitClient.playlistApi.getSongCountInPlaylist(playlist.id!!) }
                                val songsDeferred = async { RetrofitClient.playlistApi.getSongsInPlaylist(playlist.id!!) }
                                
                                val countRes = countDeferred.await()
                                val songsRes = songsDeferred.await()
                                
                                val count = if (countRes.isSuccessful) countRes.body()?.toInt() ?: 0 else 0
                                val songs = if (songsRes.isSuccessful) songsRes.body()?.take(4) else null
                                
                                playlist.copy(songCount = count, songs = songs)
                            } catch (e: Exception) {
                                NetworkMonitor.logError("UNIT_SCAN_ERR: ${playlist.id}")
                                playlist
                            }
                        }
                    }.awaitAll()
                }
                LibraryCacheManager.cachePlaylists(context, enriched)
                enriched
            } else {
                NetworkMonitor.logError("LIB_FETCH_FAIL: ${response.code()}")
                LibraryCacheManager.getCachedPlaylists(context)
            }
        } catch (e: Exception) {
            NetworkMonitor.logError("NET_ERR: ${e.message}")
            LibraryCacheManager.getCachedPlaylists(context)
        }
    }

    suspend fun getFavoriteSongs(context: Context, userId: Long): List<SongDTO> = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            val cached = LibraryCacheManager.getCachedFavorites(context)
            // Filter to show only downloaded songs when offline, as per user request
            return@withContext cached.filter { com.example.sonus.DownloadManager.isSongDownloaded(context, it.id) }
        }

        try {
            val response = RetrofitClient.favoriteApi.getFavorites(userId)
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
                LibraryCacheManager.cacheFavorites(context, songs)
                songs
            } else {
                val cached = LibraryCacheManager.getCachedFavorites(context)
                filterDownloadedOnly(context, cached)
            }
        } catch (e: Exception) {
            NetworkMonitor.logError("NET_ERR: ${e.message}")
            val cached = LibraryCacheManager.getCachedFavorites(context)
            filterDownloadedOnly(context, cached)
        }
    }

    private fun filterDownloadedOnly(context: Context, songs: List<SongDTO>?): List<SongDTO> {
        return songs?.filter { com.example.sonus.DownloadManager.isSongDownloaded(context, it.id) } ?: emptyList()
    }

    suspend fun getLibraryAlbums(context: Context, userId: Long): List<AlbumDTO> = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            val cached = LibraryCacheManager.getCachedAlbums(context)
            val downloadedSongs = com.example.sonus.DownloadManager.getDownloadedSongs(context)
            val downloadedAlbumIds = downloadedSongs.mapNotNull { it.albumId }.toSet()
            
            return@withContext cached.filter { it.id in downloadedAlbumIds }
        }
        
        try {
            val response = RetrofitClient.albumApi.getLibraryAlbums(userId)
            if (response.isSuccessful) {
                val albums = response.body()?.onEach { it.isSaved = true } ?: emptyList()
                LibraryCacheManager.cacheAlbums(context, albums)
                albums
            } else {
                LibraryCacheManager.getCachedAlbums(context)
            }
        } catch (e: Exception) {
            NetworkMonitor.logError("NET_ERR: ${e.message}")
            LibraryCacheManager.getCachedAlbums(context)
        }
    }

    suspend fun getPlaylistDetails(context: Context, playlistId: Long, userId: Long): PlaylistDTO? = withContext(Dispatchers.IO) {
        val isNetworkDown = !NetworkHelper.isNetworkAvailable(context)
        
        if (isNetworkDown) {
            val cached = LibraryCacheManager.getCachedPlaylistDetail(context, playlistId)
            return@withContext cached?.copy(songs = filterDownloadedOnly(context, cached.songs))
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
                    
                    // Enrich favorites status if userId provided
                    if (userId != -1L) {
                        val favoritesResponse = RetrofitClient.favoriteApi.getFavorites(userId)
                        if (favoritesResponse.isSuccessful) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            playlist.songs?.forEach { it.isFavorite = favoriteIds.contains(it.id) }
                        }
                        
                        val playlistSongsIds = com.example.sonus.PlaylistHelper.getAllSongsInUserPlaylists(userId)
                        playlist.songs?.forEach { it.isInPlaylist = playlistSongsIds.contains(it.id) }
                    }
                    
                    LibraryCacheManager.cachePlaylistDetail(context, playlist)
                    return@withContext playlist
                }
            }
        } catch (e: Exception) {
            NetworkMonitor.logError("NET_ERR: ${e.message}")
            val cached = LibraryCacheManager.getCachedPlaylistDetail(context, playlistId)
            return@withContext cached?.copy(songs = filterDownloadedOnly(context, cached.songs))
        }
        null
    }

    suspend fun getAlbumDetails(context: Context, albumId: Long, userId: Long): AlbumDTO? = withContext(Dispatchers.IO) {
        val isNetworkDown = !NetworkHelper.isNetworkAvailable(context)

        if (isNetworkDown) {
            val cached = LibraryCacheManager.getCachedAlbumDetail(context, albumId)
            return@withContext cached?.copy(songs = filterDownloadedOnly(context, cached.songs))
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
                    
                    // Enrich favorites status if userId provided
                    if (userId != -1L) {
                        val favoritesResponse = RetrofitClient.favoriteApi.getFavorites(userId)
                        if (favoritesResponse.isSuccessful) {
                            val favoriteIds = favoritesResponse.body()?.map { it.songId }?.toSet() ?: emptySet()
                            album.songs?.forEach { it.isFavorite = favoriteIds.contains(it.id) }
                        }
                        
                        val libraryAlbumsResponse = RetrofitClient.albumApi.getLibraryAlbums(userId)
                        if (libraryAlbumsResponse.isSuccessful) {
                            val libraryIds = libraryAlbumsResponse.body()?.map { it.id }?.toSet() ?: emptySet()
                            album.isSaved = libraryIds.contains(album.id)
                        }
                        
                        val playlistSongsIds = com.example.sonus.PlaylistHelper.getAllSongsInUserPlaylists(userId)
                        album.songs?.forEach { it.isInPlaylist = playlistSongsIds.contains(it.id) }
                    }
                    
                    LibraryCacheManager.cacheAlbumDetail(context, album)
                    return@withContext album
                }
            }
        } catch (e: Exception) {
            NetworkMonitor.logError("NET_ERR: ${e.message}")
            val cached = LibraryCacheManager.getCachedAlbumDetail(context, albumId)
            return@withContext cached?.copy(songs = filterDownloadedOnly(context, cached.songs))
        }
        null
    }

    suspend fun toggleFavorite(userId: Long, songId: Long): Boolean? = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.favoriteApi.toggleFavorite(userId, songId)
            if (response.isSuccessful) {
                response.body()?.get("added")
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Centralized method to enrich songs with their favorite and playlist status.
     * This reduces redundant code in Fragments.
     */
    suspend fun enrichSongMetadata(userId: Long, songs: List<SongDTO>): List<SongDTO> = withContext(Dispatchers.IO) {
        if (userId == -1L) return@withContext songs
        
        try {
            coroutineScope {
                val favoritesDeferred = async { RetrofitClient.favoriteApi.getFavorites(userId) }
                val playlistsResponse = RetrofitClient.playlistApi.getUserPlaylists(userId)
                
                val favorites = try {
                    val favRes = favoritesDeferred.await()
                    if (favRes.isSuccessful) {
                        favRes.body()?.map { it.songId }?.toSet() ?: emptySet()
                    } else emptySet()
                } catch (e: Exception) {
                    emptySet()
                }
                
                // This is still a bit heavy, ideally backend should return this
                val songIdsInPlaylists = mutableSetOf<Long>()
                try {
                    if (playlistsResponse.isSuccessful) {
                        playlistsResponse.body()?.forEach { playlist ->
                            val songsInPlaylist = RetrofitClient.playlistApi.getSongsInPlaylist(playlist.id!!)
                            if (songsInPlaylist.isSuccessful) {
                                songsInPlaylist.body()?.forEach { songIdsInPlaylists.add(it.id) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore and proceed with what we have
                }

                songs.onEach { song ->
                    song.isFavorite = favorites.contains(song.id)
                    song.isInPlaylist = songIdsInPlaylists.contains(song.id)
                }
            }
        } catch (e: Exception) {
            NetworkMonitor.logError("ENRICH_ERR: ${e.message}")
            songs
        }
    }
}
