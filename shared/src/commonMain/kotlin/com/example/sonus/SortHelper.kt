package com.example.sonus

import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO

enum class SortOrder {
    DEFAULT, TITLE, ARTIST, DURATION
}

object SortHelper {
    fun sortSongs(songs: List<SongDTO>, order: SortOrder): List<SongDTO> {
        return when (order) {
            SortOrder.DEFAULT -> songs
            SortOrder.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SortOrder.DURATION -> songs.sortedBy { it.duration ?: 0 }
        }
    }

    fun sortPlaylists(playlists: List<PlaylistDTO>, order: SortOrder): List<PlaylistDTO> {
        return when (order) {
            SortOrder.DEFAULT -> playlists
            SortOrder.TITLE -> playlists.sortedBy { it.name.lowercase() }
            else -> playlists
        }
    }

    fun sortAlbums(albums: List<AlbumDTO>, order: SortOrder): List<AlbumDTO> {
        return when (order) {
            SortOrder.DEFAULT -> albums
            SortOrder.TITLE -> albums.sortedBy { it.title.lowercase() }
            SortOrder.ARTIST -> albums.sortedBy { it.artist.lowercase() }
            else -> albums
        }
    }
}
