package com.example.sonus.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface SongApi {
    @Streaming
    @GET("api/songs/{id}/stream")
    suspend fun downloadSong(@Path("id") id: Long): Response<ResponseBody>
}
