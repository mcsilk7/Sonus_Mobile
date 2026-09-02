package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.*

@Composable
fun RightPanel() {
    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(StudioBgPanel)
            .padding(16.dp)
    ) {
        Text(
            "TERAZ ODTWARZANE",
            color = StudioTextFaint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Album Cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(StudioBgCard),
            contentAlignment = Alignment.Center
        ) {
            Text("OKŁADKA ALBUMU\n(200x200 px)", color = StudioTextDim, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metadata
        Text("Utwór: Night Call", color = StudioText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Artysta: Kavinsky", color = StudioTextDim, fontSize = 14.sp)
        Text("Album: OutRun (2013)", color = StudioTextFaint, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Top Tracks
        Text(
            "[ Top Utwory Wykon. ]",
            color = StudioAmber,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column {
            TopTrackItem("Drive", "4:12")
            TopTrackItem("Pacific", "3:55")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Mini Equalizer
        Text(
            "KOREKTOR DŹWIĘKU",
            color = StudioTextFaint,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        EqualizerSlider("Bass")
        EqualizerSlider("Treble")
    }
}

@Composable
private fun TopTrackItem(title: String, duration: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("* $title", color = StudioTextDim, fontSize = 13.sp)
        Text(duration, color = StudioTextFaint, fontSize = 13.sp)
    }
}

@Composable
private fun EqualizerSlider(label: String) {
    var value by remember { mutableStateOf(0.6f) }
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = StudioTextDim, fontSize = 12.sp)
        }
        Slider(
            value = value,
            onValueChange = { value = it },
            colors = SliderDefaults.colors(
                thumbColor = StudioAmber,
                activeTrackColor = StudioAmber,
                inactiveTrackColor = StudioLine
            ),
            modifier = Modifier.height(24.dp)
        )
    }
}
