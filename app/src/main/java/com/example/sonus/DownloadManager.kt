package com.example.sonus

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DownloadManager {

    sealed class DownloadEvent {
        data class Success(val songId: Long) : DownloadEvent()
        data class Error(val songId: Long, val message: String) : DownloadEvent()
    }

    private val _events = MutableSharedFlow<DownloadEvent>()
    val events: SharedFlow<DownloadEvent> = _events

    private val _downloadProgress = MutableLiveData<Map<Long, Int>>()
    val downloadProgress: LiveData<Map<Long, Int>> = _downloadProgress

    private val currentProgress = mutableMapOf<Long, Int>()
    private val gson = Gson()

    fun isSongDownloaded(context: Context, songId: Long): Boolean {
        val file = getSongFile(context, songId)
        return file.exists() && file.length() > 0
    }

    fun getSongFile(context: Context, songId: Long): File {
        val dir = File(context.filesDir, "signals")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "signal_$songId.mp3")
    }

    private fun getMetadataFile(context: Context, songId: Long): File {
        val dir = File(context.filesDir, "signals")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "signal_$songId.json")
    }

    suspend fun downloadSong(context: Context, song: SongDTO): Boolean = withContext(Dispatchers.IO) {
        val songId = song.id
        try {
            val response = RetrofitClient.songApi.downloadSong(songId)
            if (!response.isSuccessful) {
                _events.emit(DownloadEvent.Error(songId, "HTTP ${response.code()}"))
                return@withContext false
            }
            
            val body = response.body() ?: return@withContext false
            val file = getSongFile(context, songId)
            
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            
            val totalBytes = body.contentLength()
            var bytesDownloaded = 0L
            val buffer = ByteArray(8192)
            var bytesRead: Int

            updateProgress(songId, 0)

            var lastReportedProgress = -1
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                
                if (totalBytes > 0) {
                    val progress = ((bytesDownloaded * 100) / totalBytes).toInt()
                    if (progress != lastReportedProgress) {
                        updateProgress(songId, progress)
                        lastReportedProgress = progress
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            // Save Metadata
            val metaFile = getMetadataFile(context, songId)
            metaFile.writeText(gson.toJson(song))

            updateProgress(songId, -1) // Clear from map
            _events.emit(DownloadEvent.Success(songId))
            true
        } catch (e: Exception) {
            Log.e("DownloadManager", "Error downloading song: ${e.message}")
            _events.emit(DownloadEvent.Error(songId, e.message ?: "Unknown error"))
            false
        }
    }

    fun getDownloadedSongs(context: Context): List<SongDTO> {
        val dir = File(context.filesDir, "signals")
        if (!dir.exists()) {
            Log.d("DownloadManager", "Signals directory does not exist")
            return emptyList()
        }
        
        val files = dir.listFiles() ?: return emptyList()
        Log.d("DownloadManager", "Found ${files.size} files in signals directory")

        val jsonSongs = files.filter { it.name.endsWith(".json") }.mapNotNull { file ->
            try {
                val content = file.readText()
                if (content.isEmpty()) {
                    Log.w("DownloadManager", "Empty metadata file: ${file.name}")
                    return@mapNotNull null
                }
                gson.fromJson(content, SongDTO::class.java)
            } catch (e: Exception) {
                Log.e("DownloadManager", "Failed to parse metadata ${file.name}: ${e.message}")
                null
            }
        }

        // Fallback: If some .mp3 files exist but don't have .json, show them with dummy info
        val mp3Ids = files.filter { it.name.endsWith(".mp3") }.mapNotNull { file ->
            file.name.removePrefix("signal_").removeSuffix(".mp3").toLongOrNull()
        }

        val foundIds = jsonSongs.map { it.id }.toSet()
        val missingSongs = mp3Ids.filter { it !in foundIds }.map { id ->
            SongDTO(id = id, title = "RECOVERED_SIGNAL_$id", artist = "UNKNOWN_SOURCE")
        }

        val total = jsonSongs + missingSongs
        Log.d("DownloadManager", "Returning ${total.size} downloaded songs")
        return total
    }

    fun deleteSong(context: Context, songId: Long) {
        getSongFile(context, songId).delete()
        getMetadataFile(context, songId).delete()
    }

    fun getTotalBytesUsed(context: Context): Long {
        val dir = File(context.filesDir, "signals")
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun updateProgress(songId: Long, progress: Int) {
        if (progress == -1) {
            currentProgress.remove(songId)
        } else {
            currentProgress[songId] = progress
        }
        _downloadProgress.postValue(currentProgress.toMap())
    }

    fun clearAllDownloads(context: Context) {
        val dir = File(context.filesDir, "signals")
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }
}
