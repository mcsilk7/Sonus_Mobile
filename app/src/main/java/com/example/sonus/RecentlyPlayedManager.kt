package com.example.sonus

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.sonus.network.SongDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object RecentlyPlayedManager {
    
    private const val PREF_NAME = "sonus_recent_songs"
    private const val KEY_SONGS = "recent_songs_list"
    
    private val recentSongs = mutableListOf<SongDTO>()
    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadSongs()
    }

    fun addSong(song: SongDTO) {
        Log.d("RecentlyPlayed", "Adding song: ${song.title}")
        
        // Remove if already exists to move to top
        recentSongs.removeAll { it.id == song.id }
        
        // Add to the beginning
        recentSongs.add(0, song)
        
        // Keep only last 10
        if (recentSongs.size > 10) {
            recentSongs.removeAt(recentSongs.size - 1)
        }
        
        saveSongs()
    }

    fun getRecentSongs(): List<SongDTO> {
        return recentSongs.toList()
    }

    private fun saveSongs() {
        prefs?.edit()?.let {
            val json = gson.toJson(recentSongs)
            it.putString(KEY_SONGS, json)
            it.apply()
            Log.d("RecentlyPlayed", "Archive saved to disk")
        }
    }

    private fun loadSongs() {
        prefs?.getString(KEY_SONGS, null)?.let { json ->
            try {
                val type = object : TypeToken<List<SongDTO>>() {}.type
                val loadedList: List<SongDTO> = gson.fromJson(json, type)
                recentSongs.clear()
                recentSongs.addAll(loadedList)
                Log.d("RecentlyPlayed", "Archive loaded from disk: ${recentSongs.size} units")
            } catch (e: Exception) {
                Log.e("RecentlyPlayed", "Failed to load archive", e)
            }
        }
    }
}
