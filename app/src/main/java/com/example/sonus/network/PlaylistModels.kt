package com.example.sonus.network

data class PlaylistDTO(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val songs: List<SongDTO>? = null,
    val songCount: Int? = null
)

data class SongDTO(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Int? = null,
    val coverPath: String? = null,
    val filePath: String? = null,
    var albumId: Long? = null,
    var isFavorite: Boolean = false,
    var isInPlaylist: Boolean = false
)

data class AlbumDTO(
    val id: Long? = null,
    val title: String,
    val artist: String,
    val coverPath: String? = null,
    val songIds: List<Long>? = null,
    val songs: List<SongDTO>? = null, // Added to support 1:M detail view
    var isSaved: Boolean = false
)


