package com.example.sonus.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class KtorSonusClient(
    private val baseUrl: String = "http://10.0.0.1:8080/",
    private val getToken: () -> String?
) : SonusApiService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        install(Auth) {
            bearer {
                loadTokens {
                    getToken()?.let { BearerTokens(it, "") }
                }
            }
        }
        defaultRequest {
            url(baseUrl)
        }
        install(HttpRequestRetry) {
            retryOnExceptionOrServerErrors(maxRetries = 2)
            exponentialDelay()
        }
    }

    override suspend fun getUserPlaylists(userId: Long, ifModifiedSince: String?): List<PlaylistDTO> {
        return client.get("api/playlists/user/$userId") {
            ifModifiedSince?.let { header(HttpHeaders.IfModifiedSince, it) }
        }.body()
    }

    override suspend fun getSongsInPlaylist(playlistId: Long): List<SongDTO> {
        return client.get("api/playlists/$playlistId/songs").body()
    }

    override suspend fun getSongCountInPlaylist(playlistId: Long): Int {
        return client.get("api/playlists/$playlistId/songs/count").body<String>().toInt()
    }

    override suspend fun getFavorites(userId: Long, ifModifiedSince: String?, page: Int?, size: Int?): List<SongDTO> {
        return client.get("api/favorites/$userId") {
            ifModifiedSince?.let { header(HttpHeaders.IfModifiedSince, it) }
            page?.let { parameter("page", it) }
            size?.let { parameter("size", it) }
        }.body()
    }

    override suspend fun getLibraryAlbums(userId: Long, ifModifiedSince: String?): List<AlbumDTO> {
        return client.get("api/albums/library/user/$userId") {
            ifModifiedSince?.let { header(HttpHeaders.IfModifiedSince, it) }
        }.body()
    }

    override suspend fun login(request: LoginRequest): AuthResponse {
        val response = client.post("authenticate") {
            setBody(request)
            contentType(ContentType.Application.Json)
        }
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            val errorBody = try { response.body<String>() } catch (_: Exception) { response.status.description }
            throw Exception("Login failed (${response.status.value}): $errorBody")
        }
    }

    override suspend fun register(request: RegisterRequest): RegisterResponse {
        return client.post("api/user/register") {
            setBody(request)
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun getPlaylistById(playlistId: Long): PlaylistDTO {
        return client.get("api/playlists/$playlistId").body()
    }

    override suspend fun getAlbumById(albumId: Long): AlbumDTO {
        return client.get("api/albums/$albumId").body()
    }

    override suspend fun getSongsInAlbum(albumId: Long): List<SongDTO> {
        return client.get("api/albums/$albumId/songs").body()
    }

    override suspend fun toggleFavorite(userId: Long, songId: Long): Map<String, Boolean> {
        return client.post("api/favorites/$userId/songs/$songId/toggle").body()
    }

    override suspend fun addAlbumToLibrary(albumId: Long, userId: Long): Map<String, Boolean> {
        return client.post("api/albums/$albumId/library/$userId").body()
    }

    override suspend fun removeAlbumFromLibrary(albumId: Long, userId: Long): Map<String, Boolean> {
        return client.delete("api/albums/$albumId/library/$userId").body()
    }

    override suspend fun createPlaylist(userId: Long, name: String, description: String?): PlaylistDTO {
        return client.post("api/playlists/user/$userId") {
            setBody(PlaylistDTO(id = null, name = name, description = description, songs = null, songCount = 0))
            contentType(ContentType.Application.Json)
        }.body()
    }

    override suspend fun deletePlaylist(playlistId: Long): Map<String, Boolean> {
        return client.delete("api/playlists/$playlistId").body()
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Map<String, Boolean> {
        return client.post("api/playlists/$playlistId/songs/$songId").body()
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long): Map<String, Boolean> {
        return client.delete("api/playlists/$playlistId/songs/$songId").body()
    }

    override suspend fun searchSongs(query: String): List<SongDTO> {
        return client.get("api/songs/search") {
            parameter("q", query)
        }.body()
    }

    override suspend fun searchAlbums(query: String): List<AlbumDTO> {
        return client.get("api/albums/search") {
            parameter("title", query)
        }.body()
    }
}
