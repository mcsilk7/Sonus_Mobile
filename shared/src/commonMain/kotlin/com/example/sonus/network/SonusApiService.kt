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

    // Library modifications
    suspend fun addAlbumToLibrary(albumId: Long, userId: Long): Map<String, Boolean>
    suspend fun removeAlbumFromLibrary(albumId: Long, userId: Long): Map<String, Boolean>
    
    // Playlist modifications
    suspend fun createPlaylist(userId: Long, name: String, description: String?): PlaylistDTO
    suspend fun deletePlaylist(playlistId: Long): Map<String, Boolean>
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Map<String, Boolean>
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long): Map<String, Boolean>
    
    // Search
    suspend fun searchSongs(query: String): List<SongDTO>
    suspend fun searchAlbums(query: String): List<AlbumDTO>
}
