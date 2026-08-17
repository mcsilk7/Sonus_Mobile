package com.example.sonus

import android.content.Context
import android.util.Log
import com.example.sonus.network.SongDTO

object RecentlyPlayedManager {
    
    private val recentSongs = mutableListOf<SongDTO>()

    fun addSong(song: SongDTO) {
        Log.d("RecentlyPlayed", "Adding song: ${song.title}")
        // Remove if already exists to move to top
        recentSongs.removeAll { it.id == song.id }
        
        // Add to the beginning
        recentSongs.add(0, song)
        
        // Keep only last 10 for a better scroll, but user requested 4 originally.
        // Let's keep 10 to make the horizontal scroll actually useful.
        if (recentSongs.size > 10) {
            recentSongs.removeAt(recentSongs.size - 1)
        }
    }

    fun getRecentSongs(): List<SongDTO> {
        return recentSongs.toList()
    }
}
