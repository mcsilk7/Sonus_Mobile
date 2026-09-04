package ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import audio.DesktopPlayer
import ui.theme.*

@Composable
fun PlayerBar() {
    val player = DesktopPlayer
    
    Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(StudioBgPanel)) {
        // Noise Overlay Simulation
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Simple grain effect could be done with a shader
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.SkipPrevious, "Prev", tint = StudioText, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = { player.togglePlay() }) {
                    Icon(
                        if (player.isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled, 
                        "Play", 
                        tint = StudioAmber, 
                        modifier = Modifier.size(44.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.SkipNext, "Next", tint = StudioText, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            // 3. Progress Center
            Column(modifier = Modifier.weight(1f)) {
                val song = player.currentSong
                if (song != null) {
                    Text(
                        "${song.title.uppercase()} // ${song.artist.uppercase()}", 
                        color = StudioAmber, 
                        fontSize = 11.sp, 
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val totalSeconds = song?.duration ?: 0
                    val currentSeconds = (player.progress * totalSeconds).toInt()
                    
                    Text(formatTime(currentSeconds), color = StudioTextDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(12.dp))
                    Slider(
                        value = player.progress,
                        onValueChange = { player.progress = it },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = StudioAmber,
                            activeTrackColor = StudioAmber,
                            inactiveTrackColor = StudioLine
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(formatTime(totalSeconds), color = StudioTextDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.width(32.dp))

            // 4. Volume & Queue
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(220.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.QueueMusic, "Queue", tint = StudioTextDim, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.VolumeUp, "Volume", tint = StudioTextDim, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = player.volume,
                    onValueChange = { player.volume = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioText,
                        activeTrackColor = StudioText,
                        inactiveTrackColor = StudioLine
                    )
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
