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
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = StudioBgPanel,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = StudioAmber)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Konfiguracja Automatycznego VPN", color = StudioText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Sonus wymaga uprawnień do zarządzania tunelem WireGuard. " +
                    "Czy chcesz skonfigurować automatyczne logowanie VPN, aby nie wpisywać hasła przy każdym uruchomieniu aplikacji?",
                    color = StudioTextDim,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Po kliknięciu 'Konfiguruj', system poprosi Cię o podanie hasła administratora raz, aby nadać niezbędne uprawnienia.",
                    color = StudioTextFaint,
                    fontSize = 12.sp
                )
            }
        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Pomiń", color = StudioTextDim)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(backgroundColor = StudioAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Konfiguruj", color = StudioBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}
