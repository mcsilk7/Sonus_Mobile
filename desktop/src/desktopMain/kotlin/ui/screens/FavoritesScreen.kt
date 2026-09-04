package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.LibraryViewModel
import ui.theme.*
import audio.DesktopPlayer

@Composable
fun FavoritesScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { LibraryViewModel(scope) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            "PRIORITY_SIGNALS (Favorites)",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (viewModel.favorites.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("NO_FAVORITES_IN_ARCHIVE", color = StudioTextFaint, fontFamily = FontFamily.Monospace)
            }
        } else {
            FavoritesTable(viewModel)
        }
    }
}

@Composable
private fun FavoritesTable(viewModel: LibraryViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("#", color = StudioTextFaint, fontSize = 12.sp, modifier = Modifier.width(40.dp), fontFamily = FontFamily.Monospace)
            Text("SIGNAL_IDENTIFIER", color = StudioTextFaint, fontSize = 12.sp, modifier = Modifier.weight(2f), fontFamily = FontFamily.Monospace)
            Text("SOURCE_ARTIST", color = StudioTextFaint, fontSize = 12.sp, modifier = Modifier.weight(1.5f), fontFamily = FontFamily.Monospace)
            Text("LENGTH", color = StudioTextFaint, fontSize = 12.sp, modifier = Modifier.width(80.dp), fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.width(100.dp)) // Space for actions
        }

        Divider(color = StudioLine, thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(viewModel.favorites) { index, song ->
                FavoriteRow(
                    index = index + 1,
                    song = song,
                    onPlay = { DesktopPlayer.play(song) }
                )
            }
        }
    }
}

@Composable
private fun FavoriteRow(index: Int, song: com.example.sonus.network.SongDTO, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBgPanel)
            .clickable(onClick = onPlay)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString().padStart(2, '0'),
            color = StudioTextDim,
            fontSize = 13.sp,
            modifier = Modifier.width(40.dp),
            fontFamily = FontFamily.Monospace
        )
        
        Text(
            text = song.title.uppercase(),
            color = StudioAmber,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f),
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )

        Text(
            text = song.artist.uppercase(),
            color = StudioText,
            fontSize = 13.sp,
            modifier = Modifier.weight(1.5f),
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )

        Text(
            text = formatDuration(song.duration ?: 0),
            color = StudioTextDim,
            fontSize = 13.sp,
            modifier = Modifier.width(80.dp),
            fontFamily = FontFamily.Monospace
        )

        Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onPlay, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PlayArrow, null, tint = StudioAmber)
            }
            IconButton(onClick = { /* Toggle logic if needed */ }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Favorite, null, tint = StudioRed)
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
