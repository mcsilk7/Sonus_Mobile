package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.viewmodel.LoginViewModel
import ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { LoginViewModel(scope) }

    Box(
        modifier = Modifier.fillMaxSize().background(StudioBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(400.dp)
                .background(StudioBgPanel, RoundedCornerShape(16.dp))
                .border(1.dp, StudioLine, RoundedCornerShape(16.dp))
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SONUS",
                color = StudioAmber,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "STATION_LOGIN_v2.0",
                color = StudioTextDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = viewModel.username,
                onValueChange = { viewModel.username = it },
                label = { Text("COMMANDER_ID", color = StudioTextDim) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = StudioAmber,
                    unfocusedBorderColor = StudioLine,
                    textColor = StudioText
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("ACCESS_KEY", color = StudioTextDim) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.isLoading,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = StudioAmber,
                    unfocusedBorderColor = StudioLine,
                    textColor = StudioText
                )
            )

            if (viewModel.errorMessage != null) {
                Text(
                    viewModel.errorMessage!!,
                    color = StudioRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login(onLoginSuccess) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(backgroundColor = StudioAmber),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = StudioBg, modifier = Modifier.size(24.dp))
                } else {
                    Text("INITIALIZE_SESSION", color = StudioBg, fontWeight = FontWeight.Bold)
                }
            }
            
            TextButton(
                onClick = {},
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("REGISTER_NEW_OPERATOR", color = StudioTextFaint, fontSize = 12.sp)
            }
        }
    }
}
