package com.example.sonus

import android.content.Context
import com.example.sonus.network.SongDTO

class RecentlyPlayedManager(context: Context) {
    
    companion object {
        private val recentSongs = mutableListOf<SongDTO>()
    }

    fun addSong(song: SongDTO) {
        // Remove if already exists to move to top
        recentSongs.removeAll { it.id == song.id }
        
        // Add to the beginning
        recentSongs.add(0, song)
        
        // Keep only last 4
        if (recentSongs.size > 4) {
            val toRemove = recentSongs.size - 4
            repeat(toRemove) {
                recentSongs.removeAt(recentSongs.size - 1)
            }
        }
    }

    fun getRecentSongs(): List<SongDTO> {
        return recentSongs.toList()
    }
}
