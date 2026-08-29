package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.GET

interface GithubApi {
    @GET("repos/YOUR_GITHUB_USERNAME/Sonus/releases/latest")
    suspend fun getLatestRelease(): Response<GithubRelease>
}
