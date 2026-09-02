package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import audio.DesktopPlayer
import ui.theme.*

@Composable
fun PlayerBar() {
    val player = DesktopPlayer
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(StudioBgCard)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Song Info
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(StudioLine)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    player.currentSong?.title ?: "STATION_IDLE", 
                    color = StudioText, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    player.currentSong?.artist ?: "WAITING_FOR_SIGNAL", 
                    color = StudioTextDim, 
                    fontSize = 14.sp
                )
            }
        }

        // Controls
        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, "Prev", tint = StudioText, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = { player.togglePlay() }) {
                    Icon(
                        if (player.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                        "Play", 
                        tint = StudioAmber, 
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowForward, "Next", tint = StudioText, modifier = Modifier.size(32.dp))
                }
            }
            Slider(
                value = player.progress,
                onValueChange = { player.progress = it },
                modifier = Modifier.width(400.dp),
                colors = SliderDefaults.colors(
                    thumbColor = StudioAmber,
                    activeTrackColor = StudioAmber,
                    inactiveTrackColor = StudioLine
                )
            )
        }

        // Volume
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Notifications, "Volume", tint = StudioTextDim)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = player.volume,
                onValueChange = { player.volume = it },
                modifier = Modifier.width(100.dp),
                colors = SliderDefaults.colors(
                    thumbColor = StudioText,
                    activeTrackColor = StudioText,
                    inactiveTrackColor = StudioLine
                )
            )
        }
    }
}
