package com.example.sonus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sonus.R
import com.example.sonus.network.LoginRequest
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        view.findViewById<View>(R.id.btnLogin).setOnClickListener {
            performLogin()
        }

        view.findViewById<View>(R.id.tvGoToRegister).setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun performLogin() {
        val username = view?.findViewById<EditText>(R.id.etLoginEmail)?.text.toString().trim() ?: ""
        val password = view?.findViewById<EditText>(R.id.etLoginPassword)?.text.toString() ?: ""
        val loginButton = view?.findViewById<TextView>(R.id.btnLogin)

        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            return
        }

        loginButton?.isEnabled = false
        loginButton?.alpha = 0.7f

        viewLifecycleOwner.lifecycleScope.launch {
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
                    loginButton?.isEnabled = true
                    loginButton?.alpha = 1.0f
                }
            }
        }
    }

    private suspend fun showLoginError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.mainContainerFragment)
    }
}
