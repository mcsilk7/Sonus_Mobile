package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
            .height(80.dp)
            .background(StudioBgPanel)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Controls (Left)
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

        // 2. Progress Center
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("01:45", color = StudioTextDim, fontSize = 12.sp)
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
            Text("04:20", color = StudioTextDim, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.width(32.dp))

        // 3. Volume (Right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(180.dp)
        ) {
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
