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
    private var isPreparing: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private val stateListeners = mutableListOf<() -> Unit>()

    fun setOnStateChangedListener(listener: () -> Unit) {
        stateListeners.add(listener)
    }

    private fun notifyStateChanged() {
        stateListeners.forEach { it.invoke() }
    }

    fun play(context: Context, song: SongDTO, playlist: List<SongDTO> = emptyList()) {
        Log.d("PlayerState", "play() called for song: ${song.title}")

        if (playlist.isNotEmpty()) {
            currentPlaylist = playlist
            currentIndex = playlist.indexOfFirst { it.id == song.id }
        } else if (currentPlaylist.isEmpty() || currentPlaylist.none { it.id == song.id }) {
            currentPlaylist = listOf(song)
            currentIndex = 0
        } else {
            currentIndex = currentPlaylist.indexOfFirst { it.id == song.id }
        }
        
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
        
        val streamUrl = RetrofitClient.BASE_URL + "api/songs/${song.id}/stream"
        Log.d("PlayerState", "Starting playback from: $streamUrl")
        
        val sessionManager = SessionManager(context)
        val token = sessionManager.getToken()

        val headers = mutableMapOf<String, String>()
        token?.let {
            headers["Authorization"] = "Bearer $it"
            Log.d("PlayerState", "Auth token added to headers")
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
                setDataSource(context, Uri.parse(streamUrl), headers)
                isLooping = isRepeatEnabled
                setOnPreparedListener {
                    Log.d("PlayerState", "MediaPlayer prepared, starting playback")
                    it.start()
                    this@PlayerState.isPlaying = true
                    this@PlayerState.isPreparing = false
                    notifyStateChanged()
                }
                setOnCompletionListener {
                    Log.d("PlayerState", "Playback completed")
                    if (!isLooping) {
                        this@PlayerState.isPlaying = false
                        playNext(context)
                    }
                    notifyStateChanged()
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

    fun playNext(context: Context) {
        if (currentPlaylist.isEmpty()) return
        currentIndex = (currentIndex + 1) % currentPlaylist.size
        play(context, currentPlaylist[currentIndex])
    }

    fun playPrevious(context: Context) {
        if (currentPlaylist.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else currentPlaylist.size - 1
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
    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun seekTo(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }
}



