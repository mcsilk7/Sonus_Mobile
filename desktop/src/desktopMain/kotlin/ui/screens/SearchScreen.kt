package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.SearchViewModel
import ui.theme.*
import ui.components.NetworkImage
import audio.DesktopPlayer

@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            "SCAN_RESULTS",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (viewModel.query.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("WAITING_FOR_SIGNALS...", color = StudioTextFaint)
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
                        title = song.title,
                        meta = "Artist: ${song.artist}",
                        coverPath = coverUrl,
                        onClick = { DesktopPlayer.play(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalRow(title: String, meta: String, coverPath: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBgPanel)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NetworkImage(
            url = coverPath,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(meta, color = StudioTextDim, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("03:45", color = StudioTextFaint, fontSize = 12.sp)
    }
}
