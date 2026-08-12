package com.example.sonus

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.*

object PlaylistHelper {
    /**
     * Pobiera identyfikatory wszystkich piosenek znajdujących się na wszystkich playlistach użytkownika.
     */
    suspend fun getAllSongsInUserPlaylists(userId: Long): Set<Long> = coroutineScope {
        try {
            val response = RetrofitClient.playlistApi.getUserPlaylists(userId)
            if (response.isSuccessful) {
                val playlists = response.body() ?: emptyList()
                
                // Pobieramy utwory dla każdej playlisty równolegle
                val deferreds = playlists.mapNotNull { playlist ->
                    playlist.id?.let { pid ->
                        async {
                            val songsResp = RetrofitClient.playlistApi.getSongsInPlaylist(pid)
                            if (songsResp.isSuccessful) {
                                songsResp.body()?.map { it.id } ?: emptyList()
                            } else emptyList()
                        }
                    }
                }
                
                deferreds.awaitAll().flatten().toSet()
            } else emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    /**
     * Markuje piosenki jako będące w playlistach na podstawie dostarczonego zbioru ID.
     */
    fun enrichSongsWithPlaylistState(songs: List<SongDTO>, songIdsInPlaylists: Set<Long>) {
        songs.forEach { song ->
            song.isInPlaylist = songIdsInPlaylists.contains(song.id)
        }
    }

    fun showPlaylistSelectionDialog(
        context: Context, 
        scope: CoroutineScope, 
        song: SongDTO, 
        onAdded: () -> Unit
    ) {
        scope.launch {
            try {
                val response = RetrofitClient.playlistApi.getAllPlaylists()
                if (response.isSuccessful) {
                    val playlists = response.body() ?: emptyList()
                    if (playlists.isEmpty()) {
                        Toast.makeText(context, "Nie masz jeszcze żadnych playlist", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val names = playlists.map { it.name }.toTypedArray()
                    AlertDialog.Builder(context)
                        .setTitle("Dodaj do playlisty")
                        .setItems(names) { _, which ->
                            val selectedPlaylist = playlists[which]
                            addSongToPlaylist(context, scope, selectedPlaylist.id!!, song, onAdded)
                        }
                        .show()
                } else {
                    Toast.makeText(context, "Błąd pobierania playlist", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSongToPlaylist(
        context: Context, 
        scope: CoroutineScope, 
        playlistId: Long, 
        song: SongDTO, 
        onAdded: () -> Unit
    ) {
        scope.launch {
            try {
                val response = RetrofitClient.playlistApi.addSongToPlaylist(playlistId, song.id)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Dodano do playlisty!", Toast.LENGTH_SHORT).show()
                    song.isInPlaylist = true
                    onAdded()
                } else {
                    Toast.makeText(context, "Błąd dodawania: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
