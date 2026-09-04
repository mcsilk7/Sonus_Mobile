package ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
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
    val tabs = listOf("ALL", "PLAYLISTS", "ALBUMS")
    
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "DATA_ARCHIVE",
                color = StudioText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(24.dp))
            if (selectedTab == 0 || selectedTab == 1) {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = StudioBgPanel),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = StudioAmber, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("INIT_NEW_UNIT", color = StudioText, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    text = { Text(title, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AllLibraryContent(viewModel)
                1 -> CollectionsList(viewModel)
                2 -> AlbumsList(viewModel)
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun AllLibraryContent(viewModel: LibraryViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (viewModel.playlists.isNotEmpty()) {
            item {
                Text("PL_DATA_UNITS", color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
            }
            items(viewModel.playlists) { playlist ->
                LibraryItem(
                    title = playlist.name,
                    subtitle = "UNIT_ID: ${playlist.id?.toString(16)?.uppercase() ?: "00"}",
                    icon = Icons.Default.List,
                    onDelete = { playlist.id?.let { viewModel.deletePlaylist(it) } }
                )
            }
        }
        
        if (viewModel.albums.isNotEmpty()) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("ALBUM_ARCHIVE", color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(12.dp))
            }
            items(viewModel.albums) { album ->
                LibraryItem(
                    title = album.title,
                    subtitle = album.artist,
                    icon = Icons.Default.Album
                )
            }
        }
    }
}

@Composable
private fun AlbumsList(viewModel: LibraryViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(viewModel.albums) { album ->
            LibraryItem(
                title = album.title,
                subtitle = album.artist,
                icon = Icons.Default.Album
            )
        }
    }
}

@Composable
private fun CollectionsList(viewModel: LibraryViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(viewModel.playlists) { playlist ->
            LibraryItem(
                title = playlist.name,
                subtitle = "UNIT_ID: ${playlist.id?.toString(16)?.uppercase() ?: "00"}",
                icon = Icons.Default.List,
                onDelete = { playlist.id?.let { viewModel.deletePlaylist(it) } }
            )
        }
    }
}

@Composable
private fun FavoritesList(viewModel: LibraryViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(viewModel.favorites) { song ->
            LibraryItem(song.title, song.artist, Icons.Default.Favorite)
        }
    }
}

@Composable
private fun BufferStats() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(StudioBgPanel)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LOCAL_BUFFER_OCCUPANCY", color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(color = StudioLine, style = Stroke(width = 2.dp.toPx()))
                // Sector Map Simulation
                val padding = 16.dp.toPx()
                val arcSize = size.copy(width = size.width - padding * 2, height = size.height - padding * 2)
                drawArc(
                    color = StudioAmber,
                    startAngle = -90f,
                    sweepAngle = 120f,
                    useCenter = true,
                    topLeft = Offset(padding, padding),
                    size = arcSize
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("42.5 MB", color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("USED / 500 MB", color = StudioTextDim, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("STATUS: LINK_STABLE", color = StudioAmber, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text("LOCATION: SECTOR_7_CACHE", color = StudioTextFaint, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun CreatePlaylistDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("INITIALIZE_NEW_UNIT", color = StudioAmber, fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text("ASSIGN_LABEL_IDENTIFIER", color = StudioTextDim, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    colors = TextFieldDefaults.textFieldColors(textColor = StudioText, cursorColor = StudioAmber, focusedIndicatorColor = StudioAmber),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text("EXECUTE", color = StudioAmber)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ABORT", color = StudioTextDim)
            }
        },
        backgroundColor = StudioBgPanel
    )
}

@Composable
private fun LibraryItem(title: String, subtitle: String, icon: ImageVector, onDelete: (() -> Unit)? = null) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title.uppercase(), color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle.uppercase(), color = StudioTextDim, fontSize = 12.sp)
        }
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = StudioTextFaint, modifier = Modifier.size(20.dp))
            }
        }
    }
}
