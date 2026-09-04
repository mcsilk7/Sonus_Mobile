package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.SearchViewModel
import ui.theme.*
import ui.components.NetworkImage
import audio.DesktopPlayer
import com.example.sonus.network.SongDTO
import com.example.sonus.network.PlaylistDTO

@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "SCAN_RESULTS",
                color = StudioText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (viewModel.query.isEmpty() && viewModel.history.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearHistory() }) {
                    Text("CLEAR_LOGS", color = StudioAmber, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.query.isEmpty()) {
            if (viewModel.history.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("WAITING_FOR_SIGNALS...", color = StudioTextFaint, fontFamily = FontFamily.Monospace)
                }
            } else {
                Text("RECENTLY_SEARCHED", color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.history.size) { index ->
                        val item = viewModel.history[index]
                        HistoryRow(item, onClick = { viewModel.onQueryChange(item) }, onDelete = { viewModel.deleteHistoryItem(item) })
                    }
                }
            }
        } else {
            if (viewModel.isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), color = StudioAmber)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.results.size) { index ->
                    val song = viewModel.results[index]
                    val coverUrl = song.coverPath ?: "api/songs/${song.id}/cover"
                    SignalRow(
                        song = song,
                        coverPath = coverUrl,
                        playlists = viewModel.playlists,
                        onPlay = { DesktopPlayer.play(song) },
                        onFavoriteToggle = { viewModel.toggleFavorite(song) },
                        onAddToPlaylist = { playlistId -> viewModel.addSongToPlaylist(playlistId, song.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(text: String, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(StudioBgPanel)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.History, null, tint = StudioTextFaint, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text.uppercase(), color = StudioText, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, null, tint = StudioTextFaint, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SignalRow(
    song: SongDTO,
    coverPath: String?,
    playlists: List<PlaylistDTO>,
    onPlay: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: (Long) -> Unit
) {
    var showPlaylistMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBgPanel)
            .clickable(onClick = onPlay)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NetworkImage(
            url = coverPath,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(song.artist, color = StudioTextDim, fontSize = 12.sp)
        }

        IconButton(onClick = onFavoriteToggle) {
            Icon(
                if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (song.isFavorite) StudioRed else StudioTextFaint,
                modifier = Modifier.size(20.dp)
            )
        }

        Box {
            IconButton(onClick = { showPlaylistMenu = true }) {
                Icon(Icons.Default.Add, "Add to playlist", tint = StudioTextFaint, modifier = Modifier.size(20.dp))
            }
            DropdownMenu(
                expanded = showPlaylistMenu,
                onDismissRequest = { showPlaylistMenu = false },
                modifier = Modifier.background(StudioBgPanel)
            ) {
                if (playlists.isEmpty()) {
                    DropdownMenuItem(onClick = { showPlaylistMenu = false }) {
                        Text("No playlists found", color = StudioTextDim)
                    }
                } else {
                    playlists.forEach { playlist ->
                        DropdownMenuItem(onClick = {
                            showPlaylistMenu = false
                            playlist.id?.let { onAddToPlaylist(it) }
                        }) {
                            Text(playlist.name, color = StudioText)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Text(
            formatDuration(song.duration ?: 0),
            color = StudioTextFaint,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
