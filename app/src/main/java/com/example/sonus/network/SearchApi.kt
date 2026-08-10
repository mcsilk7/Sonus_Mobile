package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    @GET("api/songs/search")
    suspend fun searchSongs(@Query("q") query: String): Response<List<SongDTO>>

    @GET("api/albums/search")
    suspend fun searchAlbums(@Query("title") title: String): Response<List<AlbumDTO>>
}
