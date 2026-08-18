package com.example.sonus.repository

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

    suspend fun getUserPlaylists(userId: Long): List<PlaylistDTO> = withContext(Dispatchers.IO) {
        val response = RetrofitClient.playlistApi.getUserPlaylists(userId)
        if (response.isSuccessful) {
            val playlists = response.body() ?: emptyList()
            // Optimization: Fetch counts and first 4 songs for collage in parallel
            coroutineScope {
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
        } else emptyList()
    }

    suspend fun getFavoriteSongs(userId: Long): List<SongDTO> = withContext(Dispatchers.IO) {
        val response = RetrofitClient.favoriteApi.getFavorites(userId)
        if (response.isSuccessful) {
            response.body()?.mapNotNull { fav ->
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
        } else emptyList()
    }

    suspend fun getLibraryAlbums(userId: Long): List<AlbumDTO> = withContext(Dispatchers.IO) {
        val response = RetrofitClient.albumApi.getLibraryAlbums(userId)
        if (response.isSuccessful) {
            response.body()?.onEach { it.isSaved = true } ?: emptyList()
        } else emptyList()
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
