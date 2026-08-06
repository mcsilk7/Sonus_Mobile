package com.example.sonus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sonus.network.RegisterRequest
import com.example.sonus.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<View>(R.id.btnRegister).setOnClickListener {
            performRegistration()
        }

        findViewById<View>(R.id.tvGoToLogin).setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val username = findViewById<EditText>(R.id.etRegisterName).text.toString().trim()
        val password = findViewById<EditText>(R.id.etRegisterPassword).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.etRegisterConfirmPassword).text.toString()
        val registerButton = findViewById<TextView>(R.id.btnRegister)

        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Hasła nie są identyczne", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Hasło musi mieć co najmniej 6 znaków", Toast.LENGTH_SHORT).show()
            return
        }

        registerButton.isEnabled = false
        registerButton.alpha = 0.7f

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.register(RegisterRequest(username, password))
                if (response.isSuccessful) {
                    Toast.makeText(this@RegisterActivity, "Rejestracja zakończona sukcesem", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    showRegistrationError("Błąd rejestracji: $errorMessage")
                }
            } catch (exception: java.io.IOException) {
                showRegistrationError("Błąd sieci: sprawdź połączenie z serwerem")
            } catch (exception: Exception) {
                showRegistrationError("Wystąpił błąd: ${exception.localizedMessage}")
            } finally {
                withContext(Dispatchers.Main) {
                    registerButton.isEnabled = true
                    registerButton.alpha = 1.0f
                }
            }
        }
    }

    private suspend fun showRegistrationError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}
