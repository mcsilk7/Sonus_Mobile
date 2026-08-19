package com.example.sonus

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sonus.network.RetrofitClient
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

    fun isSongDownloaded(context: Context, songId: Long): Boolean {
        val file = getSongFile(context, songId)
        return file.exists() && file.length() > 0
    }

    fun getSongFile(context: Context, songId: Long): File {
        val dir = File(context.filesDir, "signals")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "signal_$songId.mp3")
    }

    suspend fun downloadSong(context: Context, songId: Long): Boolean = withContext(Dispatchers.IO) {
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

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                
                if (totalBytes > 0) {
                    val progress = ((bytesDownloaded * 100) / totalBytes).toInt()
                    updateProgress(songId, progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            updateProgress(songId, -1) // Clear from map
            _events.emit(DownloadEvent.Success(songId))
            true
        } catch (e: Exception) {
            Log.e("DownloadManager", "Error downloading song: ${e.message}")
            _events.emit(DownloadEvent.Error(songId, e.message ?: "Unknown error"))
            false
        }
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
