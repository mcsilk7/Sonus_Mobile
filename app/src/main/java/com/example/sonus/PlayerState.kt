package com.example.sonus

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import com.example.sonus.network.SongDTO

object PlayerState {
    var currentSong: SongDTO? = null
    var currentPlaylist: List<SongDTO> = emptyList()
    var currentIndex: Int = -1

    var isPlaying: Boolean = false
    var isRepeatEnabled: Boolean = false
    var isShuffleEnabled: Boolean = false
    var isLocalSource: Boolean = false
    private var isPreparing: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private val stateListeners = mutableSetOf<PlayerStateListener>()

    interface PlayerStateListener {
        fun onStateChanged()
    }

    fun addStateListener(listener: PlayerStateListener) {
        stateListeners.add(listener)
    }

    fun removeStateListener(listener: PlayerStateListener) {
        stateListeners.remove(listener)
    }

    private fun notifyStateChanged() {
        stateListeners.forEach { it.onStateChanged() }
    }

    fun play(context: Context, song: SongDTO, playlist: List<SongDTO> = emptyList()) {
        Log.d("PlayerState", "play() called for song: ${song.title}")

        // TOP-QUEUE LOGIC: The requested song and following songs always start from index 0
        if (playlist.isNotEmpty()) {
            val index = playlist.indexOfFirst { it.id == song.id }
            currentPlaylist = if (index != -1) {
                playlist.subList(index, playlist.size)
            } else {
                listOf(song)
            }
        } else {
            val index = currentPlaylist.indexOfFirst { it.id == song.id }
            if (index != -1) {
                currentPlaylist = currentPlaylist.subList(index, currentPlaylist.size)
            } else {
                currentPlaylist = listOf(song)
            }
        }
        
        currentIndex = 0

        // AUTO-ARCHIVE: Centralized recently played logic
        RecentlyPlayedManager.addSong(song)
        
        // Start foreground service
        val serviceIntent = Intent(context, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        if (currentSong?.id == song.id && mediaPlayer != null) {
            Log.d("PlayerState", "Same song, current state - playing: $isPlaying, preparing: $isPreparing")
            if (!isPlaying && !isPreparing) {
                resume()
            }
            return
        }

        stop()
        currentSong = song
        
        val isDownloaded = DownloadManager.isSongDownloaded(context, song.id)
        isLocalSource = isDownloaded
        
        val streamUrl = if (isDownloaded) {
            DownloadManager.getSongFile(context, song.id).absolutePath
        } else {
            RetrofitClient.BASE_URL + "api/songs/${song.id}/stream"
        }
        
        Log.d("PlayerState", "Starting playback from: $streamUrl (Local: $isLocalSource)")
        
        val sessionManager = SessionManager(context)
        val token = sessionManager.getToken()

        val headers = mutableMapOf<String, String>()
        if (!isLocalSource) {
            token?.let {
                headers["Authorization"] = "Bearer $it"
                Log.d("PlayerState", "Auth token added to headers")
            }
        }

        try {
            isPreparing = true
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                if (isLocalSource) {
                    setDataSource(streamUrl)
                } else {
                    setDataSource(context, Uri.parse(streamUrl), headers)
                }
                isLooping = isRepeatEnabled
                setOnPreparedListener {
                    Log.d("PlayerState", "MediaPlayer prepared, starting playback")
                    it.start()
                    this@PlayerState.isPlaying = true
                    this@PlayerState.isPreparing = false
                    notifyStateChanged()
                }
                setOnCompletionListener {
                    Log.d("PlayerState", "Playback completed naturally")
                    
                    if (!isLooping) {
                        this@PlayerState.isPlaying = false
                        // CONSUME MODE: Remove finished song from queue
                        consumeCurrentAndPlayNext(context)
                    } else {
                        notifyStateChanged()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("PlayerState", "MediaPlayer error: $what, extra: $extra")
                    this@PlayerState.isPlaying = false
                    this@PlayerState.isPreparing = false
                    notifyStateChanged()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("PlayerState", "Error setting data source", e)
            isPreparing = false
        }
    }

    private fun consumeCurrentAndPlayNext(context: Context) {
        if (currentPlaylist.isEmpty()) return

        val playlist = currentPlaylist.toMutableList()
        if (currentIndex in playlist.indices) {
            playlist.removeAt(currentIndex)
            currentPlaylist = playlist
        }

        if (currentPlaylist.isEmpty()) {
            currentIndex = -1
            currentSong = null
            stop()
            return
        }

        // After removal, the next song is now at the same index (shifted)
        if (currentIndex >= currentPlaylist.size) {
            currentIndex = 0
        }

        if (isShuffleEnabled) {
            currentIndex = (0 until currentPlaylist.size).random()
        }

        play(context, currentPlaylist[currentIndex])
    }

    fun playNext(context: Context) {
        if (currentPlaylist.isEmpty()) return
        
        // When user manually skips, we also consume the current one
        consumeCurrentAndPlayNext(context)
    }

    fun playPrevious(context: Context) {
        if (currentPlaylist.isEmpty()) return
        
        // Since we consume songs, "Previous" in a pure queue doesn't exist.
        // But if user expects to restart the same song:
        play(context, currentPlaylist[currentIndex])
    }

    fun pause() {
        Log.d("PlayerState", "pause() called")
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPlaying = false
                notifyStateChanged()
            }
        }
    }

    fun resume() {
        Log.d("PlayerState", "resume() called")
        mediaPlayer?.let {
            if (!isPreparing) {
                try {
                    it.start()
                    isPlaying = true
                    notifyStateChanged()
                } catch (e: IllegalStateException) {
                    Log.e("PlayerState", "Failed to resume: MediaPlayer in wrong state", e)
                }
            }
        }
    }

    fun togglePlayPause(context: Context) {
        Log.d("PlayerState", "togglePlayPause() - isPlaying: $isPlaying")
        if (isPlaying) {
            pause()
        } else {
            currentSong?.let { play(context, it) }
        }
    }

    fun toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled
        mediaPlayer?.isLooping = isRepeatEnabled
        Log.d("PlayerState", "toggleRepeat() - isRepeatEnabled: $isRepeatEnabled")
        notifyStateChanged()
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        Log.d("PlayerState", "toggleShuffle() - isShuffleEnabled: $isShuffleEnabled")
        notifyStateChanged()
    }

    fun stop() {
        Log.d("PlayerState", "stop() called")
        mediaPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: Exception) {
                Log.e("PlayerState", "Error stopping player", e)
            }
            release()
        }
        mediaPlayer = null
        isPlaying = false
        isPreparing = false
        notifyStateChanged()
    }

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int {
        val playerDuration = mediaPlayer?.duration ?: 0
        val dtoDuration = (currentSong?.duration ?: 0) * 1000
        return if (dtoDuration > playerDuration) dtoDuration else if (playerDuration > 0) playerDuration else dtoDuration
    }

    fun seekTo(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }

    fun moveSong(fromIndex: Int, toIndex: Int) {
        if (fromIndex in currentPlaylist.indices && toIndex in currentPlaylist.indices) {
            val playlist = currentPlaylist.toMutableList()
            val song = playlist.removeAt(fromIndex)
            playlist.add(toIndex, song)
            currentPlaylist = playlist
            
            if (fromIndex == currentIndex) {
                currentIndex = toIndex
            } else if (fromIndex < currentIndex && toIndex >= currentIndex) {
                currentIndex--
            } else if (fromIndex > currentIndex && toIndex <= currentIndex) {
                currentIndex++
            }
            
            notifyStateChanged()
        }
    }

    fun addSongToQueue(song: SongDTO) {
        val playlist = currentPlaylist.toMutableList()
        if (!playlist.any { it.id == song.id }) {
            playlist.add(song)
            currentPlaylist = playlist
            notifyStateChanged()
        }
    }
}
