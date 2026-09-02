package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.LibraryViewModel
import ui.theme.*

@Composable
fun LibraryScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { LibraryViewModel(scope) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("COLLECTIONS", "FAVORITES", "OFFLINE_STORAGE")

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            "DATA_ARCHIVE",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = StudioBg,
            contentColor = StudioAmber,
            divider = { Spacer(Modifier.height(1.dp).background(StudioLine)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> CollectionsGrid(viewModel)
            1 -> FavoritesList(viewModel)
            2 -> OfflineStorage()
        }
    }
}

@Composable
private fun CollectionsGrid(viewModel: LibraryViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(viewModel.playlists.size) { index ->
            val playlist = viewModel.playlists[index]
            LibraryItem(playlist.name, "Created: 2026-08-12", Icons.Default.List)
        }
    }
}

@Composable
private fun FavoritesList(viewModel: LibraryViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(viewModel.favorites.size) { index ->
            val song = viewModel.favorites[index]
            LibraryItem(song.title, song.artist, Icons.Default.Favorite)
        }
    }
}

@Composable
private fun OfflineStorage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("LOCAL_BUFFER_EMPTY", color = StudioTextFaint, fontSize = 14.sp)
    }
}

@Composable
private fun LibraryItem(title: String, subtitle: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioBgPanel)
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = StudioAmber, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = StudioTextDim, fontSize = 12.sp)
        }
    }
}
