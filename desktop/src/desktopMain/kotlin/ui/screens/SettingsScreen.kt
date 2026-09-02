package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.*

@Composable
fun SettingsScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            "STATION_CONFIGURATION",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSection("INTERFACE_PROTOCOL") {
            SettingsToggle("DARK_MODE_OVERRIDE", true)
            SettingsToggle("HI_RES_COVERS", false)
            SettingsToggle("TERMINAL_LOG_ENABLED", true)
        }

        SettingsSection("NETWORK_LINK") {
            SettingsSlider("BUFFER_SIZE", 0.5f)
            SettingsToggle("AUTO_VPN_CONNECT", true)
        }

        SettingsSection("OPERATOR_DATA") {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(backgroundColor = StudioRed),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("TERMINATE_SESSION", color = StudioBg)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(title, color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Divider(color = StudioLine, modifier = Modifier.padding(vertical = 12.dp))
        content()
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = StudioText, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(checkedThumbColor = StudioAmber)
        )
    }
}

@Composable
private fun SettingsSlider(label: String, value: Float) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, color = StudioText, fontSize = 16.sp)
        Slider(
            value = value,
            onValueChange = {},
            colors = SliderDefaults.colors(thumbColor = StudioAmber, activeTrackColor = StudioAmber)
        )
    }
}
