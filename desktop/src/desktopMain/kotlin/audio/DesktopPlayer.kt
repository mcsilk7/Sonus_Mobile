package audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sonus.Config
import com.example.sonus.network.SongDTO
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import javazoom.jl.player.Player
import kotlinx.coroutines.*
import ui.DesktopDI
import java.io.BufferedInputStream
import java.io.InputStream

object DesktopPlayer {
    var currentSong by mutableStateOf<SongDTO?>(null)
    var isPlaying by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var volume by mutableStateOf(0.8f)

    private var playbackJob: Job? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var player: Player? = null
    
    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 15000
        }
        // Pozwalamy na przekierowania i zachowujemy nagłówki
        followRedirects = true
    }

    fun play(song: SongDTO) {
        if (currentSong?.id == song.id && player != null && isPlaying) {
            stopPlayback()
            return
        }

        stopPlayback()
        currentSong = song
        
        playbackJob = scope.launch(Dispatchers.IO) {
            var inputStream: InputStream? = null
            try {
                val token = DesktopDI.sessionManager.getToken()
                
                // Budujemy URL identycznie jak na Androidzie: api/songs/{id}/stream
                val encodedUrl = URLBuilder(Config.BASE_URL).apply {
                    pathSegments = listOf("api", "songs", "${song.id}", "stream")
                    // Na wszelki wypadek dodajemy token też do query, 
                    // bo niektóre serwery strumieniujące tak preferują
                    if (token != null) {
                        parameters.append("bearer", token)
                    }
                }.buildString()
                
                println("SONUS_PLAYER: Requesting stream: $encodedUrl")
                println("SONUS_PLAYER: Auth Token present: ${token != null}")

                val response = httpClient.get(encodedUrl) {
                    if (token != null) {
                        // Standardowy nagłówek Bearer
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                    // Dodajemy User-Agent, żeby udawać Androida/Przeglądarkę (niektóre serwery tego wymagają)
                    header(HttpHeaders.UserAgent, "SonusDesktop/2.0 (Android-like)")
                    header(HttpHeaders.Accept, "audio/mpeg, audio/*;q=0.9, */*;q=0.8")
                    header(HttpHeaders.Connection, "keep-alive")
                }

                if (response.status.value != 200) {
                    println("SONUS_PLAYER: Server returned error: ${response.status}")
                    val errorBody = response.bodyAsText()
                    if (errorBody.isNotEmpty()) println("SONUS_PLAYER: Error body: $errorBody")
                    throw Exception("HTTP ${response.status.value}: $errorBody")
                }

                println("SONUS_PLAYER: Stream connected. Starting decoder...")
                
                // Konwertujemy ByteReadChannel na InputStream dla JLayer
                inputStream = BufferedInputStream(response.bodyAsChannel().toInputStream())
                player = Player(inputStream)
                
                withContext(Dispatchers.Main) {
                    isPlaying = true
                    progress = 0f
                }
                
                startProgressTracking(song.duration ?: 240)
                
                player?.play()
                
                if (player?.isComplete == true) {
                    withContext(Dispatchers.Main) {
                        stopPlayback()
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    println("SONUS_PLAYER: Playback fatal error: ${e.message}")
                    e.printStackTrace()
                }
                withContext(Dispatchers.Main) {
                    isPlaying = false
                }
            } finally {
                inputStream?.close()
            }
        }
    }

    fun togglePlay() {
        val song = currentSong
        if (isPlaying) {
            stopPlayback()
        } else if (song != null) {
            play(song)
        }
    }

    private fun stopPlayback() {
        println("SONUS_PLAYER: Stopping playback.")
        isPlaying = false
        player?.close()
        player = null
        playbackJob?.cancel()
        progressJob?.cancel()
        progress = 0f
    }

    private fun startProgressTracking(totalDurationSeconds: Int) {
        progressJob?.cancel()
        progressJob = scope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && isPlaying) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000f
                progress = (elapsed / totalDurationSeconds).coerceIn(0f, 1f)
                delay(1000)
            }
        }
    }
}
