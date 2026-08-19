package com.example.sonus.repository

import android.content.Context
import com.example.sonus.LibraryCacheManager
import com.example.sonus.NetworkHelper
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO
import com.example.sonus.network.AlbumDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class MusicRepository {

    suspend fun getUserPlaylists(context: Context, userId: Long): List<PlaylistDTO> = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            return@withContext LibraryCacheManager.getCachedPlaylists(context)
        }

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
                            playlist
                        }
                    }
                }.awaitAll()
            }
            LibraryCacheManager.cachePlaylists(context, enriched)
            enriched
        } else {
            LibraryCacheManager.getCachedPlaylists(context)
        }
    }

    suspend fun getFavoriteSongs(context: Context, userId: Long): List<SongDTO> = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            val cached = LibraryCacheManager.getCachedFavorites(context)
            // Filter to show only downloaded songs when offline, as per user request
            return@withContext cached.filter { com.example.sonus.DownloadManager.isSongDownloaded(context, it.id) }
        }

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
            LibraryCacheManager.getCachedFavorites(context)
        }
    }

    suspend fun getLibraryAlbums(context: Context, userId: Long): List<AlbumDTO> = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) return@withContext emptyList()
        
        val response = RetrofitClient.albumApi.getLibraryAlbums(userId)
        if (response.isSuccessful) {
            response.body()?.onEach { it.isSaved = true } ?: emptyList()
        } else emptyList()
    }

    suspend fun getPlaylistDetails(context: Context, playlistId: Long, userId: Long): PlaylistDTO? = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            val cached = LibraryCacheManager.getCachedPlaylistDetail(context, playlistId)
            // Filter to show only downloaded songs when offline
            return@withContext cached?.copy(songs = cached.songs?.filter { com.example.sonus.DownloadManager.isSongDownloaded(context, it.id) })
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
            return@withContext LibraryCacheManager.getCachedPlaylistDetail(context, playlistId)
        }
        null
    }

    suspend fun getAlbumDetails(context: Context, albumId: Long, userId: Long): AlbumDTO? = withContext(Dispatchers.IO) {
        if (!NetworkHelper.isNetworkAvailable(context)) {
            val cached = LibraryCacheManager.getCachedAlbumDetail(context, albumId)
            // Filter to show only downloaded songs when offline
            return@withContext cached?.copy(songs = cached.songs?.filter { com.example.sonus.DownloadManager.isSongDownloaded(context, it.id) })
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
            return@withContext LibraryCacheManager.getCachedAlbumDetail(context, albumId)
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
        
        coroutineScope {
            val favoritesDeferred = async { RetrofitClient.favoriteApi.getFavorites(userId) }
            val playlistsResponse = RetrofitClient.playlistApi.getUserPlaylists(userId)
            
            val favorites = if (favoritesDeferred.await().isSuccessful) {
                favoritesDeferred.await().body()?.map { it.songId }?.toSet() ?: emptySet()
            } else emptySet()
            
            // This is still a bit heavy, ideally backend should return this
            val songIdsInPlaylists = mutableSetOf<Long>()
            if (playlistsResponse.isSuccessful) {
                playlistsResponse.body()?.forEach { playlist ->
                    val songsInPlaylist = RetrofitClient.playlistApi.getSongsInPlaylist(playlist.id!!)
                    if (songsInPlaylist.isSuccessful) {
                        songsInPlaylist.body()?.forEach { songIdsInPlaylists.add(it.id) }
                    }
                }
            }

            songs.onEach { song ->
                song.isFavorite = favorites.contains(song.id)
                song.isInPlaylist = songIdsInPlaylists.contains(song.id)
            }
        }
    }
}
