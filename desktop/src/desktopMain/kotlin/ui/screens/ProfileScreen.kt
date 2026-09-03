package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.DesktopDI
import ui.theme.*

@Composable
fun ProfileScreen() {
    val username = DesktopDI.sessionManager.getUsername() ?: "OPERATOR"
    val userId = DesktopDI.sessionManager.getUserId()
    val role = DesktopDI.sessionManager.getRole() ?: "GUEST"

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "OPERATOR_PROFILE",
            color = StudioText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 48.dp)
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(StudioBgPanel),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = StudioAmber, modifier = Modifier.size(64.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(username, color = StudioText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("ID: $userId", color = StudioTextDim, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            backgroundColor = StudioBgPanel,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                ProfileInfoRow("ACCESS_LEVEL", role)
                ProfileInfoRow("ENCRYPTION", "AES-256-GCM")
                ProfileInfoRow("STATUS", "SECURE_LINK_ACTIVE")
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = StudioTextFaint, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text(value, color = StudioAmber, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
