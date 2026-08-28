package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.*

interface FavoriteApi {

    @GET("api/favorites/{userId}")
    suspend fun getFavorites(
        @Path("userId") userId: Long,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Header("If-Modified-Since") ifModifiedSince: String? = null
    ): Response<List<FavoriteSongDTO>>

    @GET("api/favorites/{userId}/songs/{songId}/check")
    suspend fun isFavorite(
        @Path("userId") userId: Long,
        @Path("songId") songId: Long
    ): Response<Map<String, Boolean>>

    @POST("api/favorites/{userId}/songs/{songId}/toggle")
    suspend fun toggleFavorite(
        @Path("userId") userId: Long,
        @Path("songId") songId: Long
    ): Response<Map<String, Boolean>>

    @POST("api/favorites/{userId}/songs/{songId}")
    suspend fun addToFavorites(
        @Path("userId") userId: Long,
        @Path("songId") songId: Long
    ): Response<FavoriteSongDTO>

    @DELETE("api/favorites/{userId}/songs/{songId}")
    suspend fun removeFromFavorites(
        @Path("userId") userId: Long,
        @Path("songId") songId: Long
    ): Response<Unit>
}

data class FavoriteSongDTO(
    val id: Long? = null,
    val userId: Long,
    val songId: Long,
    val songTitle: String? = null,
    val songArtist: String? = null,
    val songDuration: Int? = null,
    val coverPath: String? = null,
    val addedAt: String? = null,
    val song: SongDTO? = null, // Still keep for compatibility if needed
    val songDto: SongDTO? = null
)

data class FavoriteAlbumDTO(
    val id: Long? = null,
    val userId: Long,
    val albumId: Long,
    val album: AlbumDTO? = null
)
