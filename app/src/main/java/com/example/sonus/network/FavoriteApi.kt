package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.*

interface FavoriteApi {

    @GET("api/favorites/{userId}")
    suspend fun getFavorites(@Path("userId") userId: Long): Response<List<FavoriteSongDTO>>

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
    val song: SongDTO? = null
)

data class FavoriteAlbumDTO(
    val id: Long? = null,
    val userId: Long,
    val albumId: Long,
    val album: AlbumDTO? = null
)
