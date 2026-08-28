package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.*

interface PlaylistApi {

    @GET("api/playlists")
    suspend fun getAllPlaylists(): Response<List<PlaylistDTO>>

    @GET("api/playlists/user/{userId}")
    suspend fun getUserPlaylists(
        @Path("userId") userId: Long,
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): Response<List<PlaylistDTO>>

    @GET("api/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") id: Long): Response<PlaylistDTO>

    @GET("api/playlists/{id}/songs")
    suspend fun getSongsInPlaylist(@Path("id") id: Long): Response<List<SongDTO>>

    @GET("api/playlists/{id}/songs/count")
    suspend fun getSongCountInPlaylist(@Path("id") id: Long): Response<Long>

    @GET("api/playlists/search")
    suspend fun searchPlaylists(@Query("name") name: String): Response<List<PlaylistDTO>>

    @POST("api/playlists/user/{userId}")
    suspend fun createPlaylist(@Path("userId") userId: Long, @Body dto: PlaylistDTO): Response<PlaylistDTO>

    @PUT("api/playlists/{id}")
    suspend fun updatePlaylist(@Path("id") id: Long, @Body dto: PlaylistDTO): Response<PlaylistDTO>

    @POST("api/playlists/{playlistId}/songs/{songId}")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("songId") songId: Long
    ): Response<PlaylistDTO>

    @DELETE("api/playlists/{playlistId}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("songId") songId: Long
    ): Response<PlaylistDTO>

    @DELETE("api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") id: Long): Response<Unit>
}
