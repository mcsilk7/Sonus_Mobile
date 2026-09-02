package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.theme.*

@Composable
fun VpnSetupDialog(
    onConfirm: () -> Unit
) {
    Card(
        backgroundColor = StudioBgPanel,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(450.dp).padding(16.dp),
        elevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = StudioAmber)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Konfiguracja VPN", color = StudioText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Sonus wymaga uprawnień do tunelu WireGuard. " +
                "Skonfiguruj automatyczne logowanie, aby uniknąć haseł w przyszłości.",
                color = StudioTextDim,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Po kliknięciu 'Konfiguruj', system poprosi o hasło administratora (tylko raz).",
                color = StudioTextFaint,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = StudioAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("KONFIGURUJ DOSTĘP", color = StudioBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
