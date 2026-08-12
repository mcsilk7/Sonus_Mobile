package com.example.sonus

import android.content.Context
import android.content.SharedPreferences
import com.example.sonus.network.SongDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentlyPlayedManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("recently_played_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY_RECENT_SONGS = "recent_songs"

    fun addSong(song: SongDTO) {
        val currentList = getRecentSongs().toMutableList()
        
        // Remove if already exists to move to top
        currentList.removeAll { it.id == song.id }
        
        // Add to the beginning
        currentList.add(0, song)
        
        // Keep only last 4
        val limitedList = currentList.take(4)
        
        val json = gson.toJson(limitedList)
        prefs.edit().putString(KEY_RECENT_SONGS, json).apply()
    }

    fun getRecentSongs(): List<SongDTO> {
        val json = prefs.getString(KEY_RECENT_SONGS, null) ?: return emptyList()
        val type = object : TypeToken<List<SongDTO>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
