package com.example.sonus.network

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val username: String,
    val userId: Long,
    val role: String? = null
)


data class RegisterResponse(
    val userId: Long,
    val username: String,
    val role: String
)
