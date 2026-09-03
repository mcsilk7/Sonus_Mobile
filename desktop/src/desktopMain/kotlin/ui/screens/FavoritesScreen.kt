package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.LibraryViewModel
import ui.theme.*

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
                Text("NO_FAVORITES_IN_ARCHIVE", color = StudioTextFaint)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.favorites.size) { index ->
                    val song = viewModel.favorites[index]
                    FavoriteRow(song.title, song.artist)
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(title: String, artist: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBgPanel)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Favorite, null, tint = StudioAmber, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(artist, color = StudioTextDim, fontSize = 12.sp)
        }
    }
}
