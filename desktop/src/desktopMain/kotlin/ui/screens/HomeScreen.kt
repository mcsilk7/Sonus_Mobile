package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.HomeViewModel
import ui.theme.*

@Composable
fun HomeScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { HomeViewModel(scope) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            "COMMAND_CENTER",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text("COLLECTIONS", color = StudioAmber, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(180.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(viewModel.playlists.size) { index ->
                val playlist = viewModel.playlists[index]
                AlbumCard(playlist.name, "Songs: ${playlist.songCount ?: 0}")
            }
        }
    }
}

@Composable
private fun AlbumCard(title: String, artist: String) {
    Column(modifier = Modifier.width(180.dp)) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(StudioBgCard)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(artist, color = StudioTextDim, fontSize = 14.sp)
    }
}
