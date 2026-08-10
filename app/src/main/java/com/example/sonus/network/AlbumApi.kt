package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.*

interface AlbumApi {

    @GET("api/albums/library/user/{userId}")
    suspend fun getLibraryAlbums(@Path("userId") userId: Long): Response<List<AlbumDTO>>

    @GET("api/albums/{id}")
    suspend fun getAlbumById(@Path("id") id: Long): Response<AlbumDTO>

    @POST("api/albums/{albumId}/library/{userId}")
    suspend fun addAlbumToLibrary(
        @Path("albumId") albumId: Long,
        @Path("userId") userId: Long
    ): Response<AlbumDTO>

    @DELETE("api/albums/{albumId}/library/{userId}")
    suspend fun removeAlbumFromLibrary(
        @Path("albumId") albumId: Long,
        @Path("userId") userId: Long
    ): Response<Unit>
}
