package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.HomeViewModel
import ui.theme.*
import ui.DesktopDI
import ui.components.NetworkImage
import audio.DesktopPlayer

@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { HomeViewModel(scope) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        HeaderSection()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SectionHeader("TERMINAL_LOG")
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            TerminalLog(viewModel.terminalLogs)
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        SectionHeader("SIGNAL_HISTORY")
        if (viewModel.recentlyPlayed.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "NO_RECENT_SIGNALS_FOUND",
                    color = StudioTextFaint,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.recentlyPlayed) { song ->
                    TapeReelCard(song) {
                        DesktopPlayer.play(song)
                    }
                }
            }
        }
    }
}

@Composable
private fun TapeReelCard(song: com.example.sonus.network.SongDTO, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(StudioBgPanel)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // The "Tape Box" Front
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(StudioLine)
                .padding(2.dp)
        ) {
            val coverUrl = if (song.coverPath?.startsWith("http") == true) {
                song.coverPath
            } else {
                "api/songs/${song.id}/cover"
            }
            
            NetworkImage(
                url = coverUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Sticker Label
            Surface(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopStart),
                color = Color(0xFFF0EBE0), // Off-white sticker
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = "REEL_${song.id.toString().padStart(3, '0')}",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    color = Color(0xFF14120F),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Technical Metadata
        Text(
            text = song.title.uppercase(),
            modifier = Modifier.padding(top = 10.dp),
            color = StudioAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )

        Text(
            text = "SRC: ${song.artist.uppercase()}",
            modifier = Modifier.padding(top = 2.dp),
            color = StudioTextDim,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(StudioAmberDim)
            )
            
            Text(
                text = "LOG_2026.09.04", // Hardcoded date matching Android adapter style
                modifier = Modifier.padding(start = 6.dp),
                color = StudioTextFaint,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    val username = DesktopDI.sessionManager.getUsername() ?: "OPERATOR"
    val role = DesktopDI.sessionManager.getRole() ?: "USER"
    
    Row(verticalAlignment = Alignment.Bottom) {
        Column {
            Text("SESSION_ID: ${username.uppercase()}", color = StudioAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("WELCOME_OPERATOR", color = StudioText, fontSize = 48.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("LINK_STATUS: SECURE", color = StudioAmber, fontSize = 12.sp)
            Text("ROLE: $role", color = StudioTextDim, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Divider(color = StudioLine, modifier = Modifier.padding(top = 8.dp), thickness = 1.dp)
    }
}

@Composable
private fun TerminalLog(logs: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .background(StudioBgPanel)
            .padding(12.dp)
    ) {
        logs.forEach { log ->
            Text(
                "> $log",
                color = StudioAmber,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
