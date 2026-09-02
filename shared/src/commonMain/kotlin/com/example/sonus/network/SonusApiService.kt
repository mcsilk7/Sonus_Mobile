package com.example.sonus.network

interface SonusApiService {
    suspend fun getUserPlaylists(userId: Long, ifModifiedSince: String? = null): List<PlaylistDTO>
    suspend fun getSongsInPlaylist(playlistId: Long): List<SongDTO>
    suspend fun getSongCountInPlaylist(playlistId: Long): Int
    suspend fun getFavorites(userId: Long, ifModifiedSince: String? = null, page: Int? = null, size: Int? = null): List<SongDTO>
    suspend fun getLibraryAlbums(userId: Long, ifModifiedSince: String? = null): List<AlbumDTO>
    
    // Auth methods
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): RegisterResponse
    
    // Additional Music methods
    suspend fun getPlaylistById(playlistId: Long): PlaylistDTO
    suspend fun getAlbumById(albumId: Long): AlbumDTO
    suspend fun getSongsInAlbum(albumId: Long): List<SongDTO>
    suspend fun toggleFavorite(userId: Long, songId: Long): Map<String, Boolean>
}
