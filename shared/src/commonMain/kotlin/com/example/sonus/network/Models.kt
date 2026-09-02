package com.example.sonus.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val username: String,
    val userId: Long,
    val role: String? = null
)

@Serializable
data class RegisterResponse(
    val userId: Long,
    val username: String,
    val role: String
)

@Serializable
data class PlaylistDTO(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val songs: List<SongDTO>? = null,
    val songCount: Int? = null
)

@Serializable
data class SongDTO(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Int? = null,
    val coverPath: String? = null,
    val blurHash: String? = null,
    val filePath: String? = null,
    var albumId: Long? = null,
    var isFavorite: Boolean = false,
    var isInPlaylist: Boolean = false
)

@Serializable
data class AlbumDTO(
    val id: Long? = null,
    val title: String,
    val artist: String,
    val coverPath: String? = null,
    val blurHash: String? = null,
    val songIds: List<Long>? = null,
    val songs: List<SongDTO>? = null,
    var isSaved: Boolean = false
)

@Serializable
data class GithubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("body") val description: String,
    @SerialName("assets") val assets: List<GithubAsset>
)

@Serializable
data class GithubAsset(
    @SerialName("browser_download_url") val downloadUrl: String,
    @SerialName("name") val name: String
)

@Serializable
data class UpdateInfo(
    val version: String,
    val description: String,
    val downloadUrl: String
)
