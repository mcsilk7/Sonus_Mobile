package com.example.sonus

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.sonus.network.SongDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SearchHistoryManager {
    
    private const val PREF_NAME = "sonus_search_history"
    private const val KEY_SONGS = "search_history_list"
    
    private val historySongs = mutableListOf<SongDTO>()
    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadHistory()
    }

    fun addSong(song: SongDTO) {
        // Remove if already exists to move to top
        historySongs.removeAll { it.id == song.id }
        
        // Add to the beginning
        historySongs.add(0, song)
        
        // Keep only last 15 for better history
        if (historySongs.size > 15) {
            historySongs.removeAt(historySongs.size - 1)
        }
        
        saveHistory()
    }

    fun removeSong(songId: Long) {
        historySongs.removeAll { it.id == songId }
        saveHistory()
    }

    fun getHistory(): List<SongDTO> {
        return historySongs.toList()
    }

    private fun saveHistory() {
        prefs?.edit()?.let {
            val json = gson.toJson(historySongs)
            it.putString(KEY_SONGS, json)
            it.apply()
            Log.d("SearchHistory", "Search history saved to disk")
        }
    }

    private fun loadHistory() {
        prefs?.getString(KEY_SONGS, null)?.let { json ->
            try {
                val type = object : TypeToken<List<SongDTO>>() {}.type
                val loadedList: List<SongDTO> = gson.fromJson(json, type)
                historySongs.clear()
                historySongs.addAll(loadedList)
                Log.d("SearchHistory", "Search history loaded from disk: ${historySongs.size} units")
            } catch (e: Exception) {
                Log.e("SearchHistory", "Failed to load history", e)
            }
        }
    }
}
