package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.*

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            "ODKRYWAJ / DZISIAJ DLA CIEBIE",
            color = StudioTextFaint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(StudioAmberDim)
                .padding(32.dp)
        ) {
            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text("BANER PROMOWANY / NOWY ALBUM", color = StudioText, fontSize = 14.sp)
                Text("\"Echa Przestrzeni\" - Artysta", color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(backgroundColor = StudioText),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Słuchaj", color = StudioBg)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioText)
                    ) {
                        Text("Dodaj do biblioteki", color = StudioText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Szybki dostęp
        Text("SZYBKI DOSTĘP (Ostatnio odtwarzane)", color = StudioTextDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuickAccessItem("Synthwave 80s")
            QuickAccessItem("Podkasty")
            QuickAccessItem("Trening Rock")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Rekomendowane sekcje
        Text("Rekomendowane Sekcje", color = StudioTextDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            RecommendedCard("Najlepsze z Pop")
            RecommendedCard("Polski Hip-Hop")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Kolejka Nagrań
        Text("KOLEJKA NAGRAŃ", color = StudioTextDim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        QueueTable()
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun QuickAccessItem(title: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StudioBgCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = StudioAmber, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = StudioText, fontSize = 14.sp)
    }
}

@Composable
private fun RecommendedCard(title: String) {
    Column(modifier = Modifier.width(160.dp)) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioBgCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = StudioTextFaint, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = StudioText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QueueTable() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp)) {
            Text("#", modifier = Modifier.width(30.dp), color = StudioTextFaint, fontSize = 12.sp)
            Text("Tytuł", modifier = Modifier.weight(1f), color = StudioTextFaint, fontSize = 12.sp)
            Text("Wykonawca", modifier = Modifier.weight(1f), color = StudioTextFaint, fontSize = 12.sp)
            Text("Czas", modifier = Modifier.width(60.dp), color = StudioTextFaint, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(40.dp))
        }
        Divider(color = StudioLine)
        QueueItem(1, "Pacific Coast", "Lazerhawk", "4:20")
        QueueItem(2, "Resonance", "HOME", "3:32")
    }
}

@Composable
private fun QueueItem(index: Int, title: String, artist: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(index.toString(), modifier = Modifier.width(30.dp), color = StudioTextDim, fontSize = 14.sp)
        Text(title, modifier = Modifier.weight(1f), color = StudioText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(artist, modifier = Modifier.weight(1f), color = StudioTextDim, fontSize = 14.sp)
        Text(time, modifier = Modifier.width(60.dp), color = StudioTextDim, fontSize = 14.sp)
        IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = StudioTextFaint)
        }
    }
}
