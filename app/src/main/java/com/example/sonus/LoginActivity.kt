package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sonus.network.LoginRequest
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        findViewById<View>(R.id.btnLogin).setOnClickListener {
            performLogin()
        }

        findViewById<View>(R.id.tvGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = findViewById<EditText>(R.id.etLoginEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etLoginPassword).text.toString()
        val loginButton = findViewById<TextView>(R.id.btnLogin)

        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton.isEnabled = false
        loginButton.alpha = 0.7f

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        sessionManager.saveSession(
                            authResponse.token, 
                            authResponse.username, 
                            authResponse.userId,
                            authResponse.role
                        )
                        navigateToMain()
                    } else {
                        showLoginError("Nieprawidłowa odpowiedź serwera")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    showLoginError("Błąd logowania: $errorMessage")
                }
            } catch (exception: java.io.IOException) {
                showLoginError("Błąd sieci: sprawdź połączenie i serwer")
            } catch (exception: Exception) {
                showLoginError("Wystąpił błąd: ${exception.localizedMessage}")
            } finally {
                withContext(Dispatchers.Main) {
                    loginButton.isEnabled = true
                    loginButton.alpha = 1.0f
                }
            }
        }
    }

    private suspend fun showLoginError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
