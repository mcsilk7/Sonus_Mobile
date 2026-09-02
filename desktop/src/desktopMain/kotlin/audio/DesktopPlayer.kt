package audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.network.SongDTO
import kotlinx.coroutines.*
import ui.DesktopDI

object DesktopPlayer {
    var currentSong by mutableStateOf<SongDTO?>(null)
    var isPlaying by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var volume by mutableStateOf(0.8f)

    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun play(song: SongDTO) {
        currentSong = song
        isPlaying = true
        startProgressTracking()
    }

    fun togglePlay() {
        isPlaying = !isPlaying
        if (isPlaying) startProgressTracking() else stopProgressTracking()
    }

    private fun startProgressTracking() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive && isPlaying) {
                delay(1000)
                if (progress < 1f) {
                    progress += 0.01f
                } else {
                    progress = 0f
                    isPlaying = false
                }
            }
        }
    }

    private fun stopProgressTracking() {
        playbackJob?.cancel()
    }
}
