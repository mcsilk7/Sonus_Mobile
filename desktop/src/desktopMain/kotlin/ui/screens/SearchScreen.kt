package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.SearchViewModel
import ui.theme.*

@Composable
fun SearchScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { SearchViewModel(scope) }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = { Text("SCAN_FOR_SIGNALS...", color = StudioTextFaint) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = StudioAmber) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = StudioAmber,
                unfocusedBorderColor = StudioLine,
                textColor = StudioText,
                backgroundColor = StudioBgPanel
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (viewModel.isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = StudioAmber)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.results.size) { index ->
                val song = viewModel.results[index]
                SignalRow(song.title, "Artist: ${song.artist}")
            }
        }
    }
}

@Composable
private fun SignalRow(title: String, meta: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioBgPanel)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(StudioLine))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = StudioText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(meta, color = StudioTextDim, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("03:45", color = StudioTextFaint, fontSize = 12.sp)
    }
}
