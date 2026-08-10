package com.example.sonus.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("authenticate")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/user/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
