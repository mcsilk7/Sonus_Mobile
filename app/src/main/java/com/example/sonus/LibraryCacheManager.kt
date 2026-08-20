package com.example.sonus

import android.content.Context
import android.content.SharedPreferences
import com.example.sonus.network.AlbumDTO
import com.example.sonus.network.PlaylistDTO
import com.example.sonus.network.SongDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object LibraryCacheManager {
    private const val PREF_NAME = "sonus_library_cache"
    private const val KEY_PLAYLISTS = "cached_playlists"
    private const val KEY_FAVORITES = "cached_favorites"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun cachePlaylists(context: Context, playlists: List<PlaylistDTO>) {
        val json = Gson().toJson(playlists)
        getPrefs(context).edit().putString(KEY_PLAYLISTS, json).apply()
    }

    fun getCachedPlaylists(context: Context): List<PlaylistDTO> {
        val json = getPrefs(context).getString(KEY_PLAYLISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<PlaylistDTO>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun cacheAlbums(context: Context, albums: List<AlbumDTO>) {
        val json = Gson().toJson(albums)
        getPrefs(context).edit().putString("cached_albums", json).apply()
    }

    fun getCachedAlbums(context: Context): List<AlbumDTO> {
        val json = getPrefs(context).getString("cached_albums", null) ?: return emptyList()
        val type = object : TypeToken<List<AlbumDTO>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun cacheFavorites(context: Context, favorites: List<SongDTO>) {
        val json = Gson().toJson(favorites)
        getPrefs(context).edit().putString(KEY_FAVORITES, json).apply()
    }

    fun getCachedFavorites(context: Context): List<SongDTO> {
        val json = getPrefs(context).getString(KEY_FAVORITES, null) ?: return emptyList()
        val type = object : TypeToken<List<SongDTO>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun cachePlaylistDetail(context: Context, playlist: PlaylistDTO) {
        val prefs = getPrefs(context)
        val json = Gson().toJson(playlist)
        prefs.edit().putString("playlist_${playlist.id}", json).apply()
    }

    fun getCachedPlaylistDetail(context: Context, playlistId: Long): PlaylistDTO? {
        val json = getPrefs(context).getString("playlist_$playlistId", null) ?: return null
        val type = object : TypeToken<PlaylistDTO>() {}.type
        return Gson().fromJson(json, type)
    }

    fun cacheAlbumDetail(context: Context, album: AlbumDTO) {
        val prefs = getPrefs(context)
        val json = Gson().toJson(album)
        prefs.edit().putString("album_${album.id}", json).apply()
    }

    fun getCachedAlbumDetail(context: Context, albumId: Long): AlbumDTO? {
        val json = getPrefs(context).getString("album_$albumId", null) ?: return null
        val type = object : TypeToken<AlbumDTO>() {}.type
        return Gson().fromJson(json, type)
    }
}
