package ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.sonus.network.LoginRequest
import ui.DesktopDI

class LoginViewModel(private val scope: CoroutineScope) {
    var username by mutableStateOf("admin")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(onSuccess: () -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "FILL_ALL_FIELDS"
            return
        }

        isLoading = true
        errorMessage = null
        
        scope.launch(Dispatchers.IO) {
            try {
                val response = DesktopDI.container.apiService.login(LoginRequest(username, password))
                DesktopDI.sessionManager.saveSession(
                    response.token,
                    response.username,
                    response.userId,
                    response.role
                )
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "CONNECTION_ERROR"
            } finally {
                isLoading = false
            }
        }
    }
}
