package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.GET

interface GithubApi {
    @GET("repos/mcsilk7/Sonus_mobile/releases/latest")
    suspend fun getLatestRelease(): Response<GithubRelease>
}
