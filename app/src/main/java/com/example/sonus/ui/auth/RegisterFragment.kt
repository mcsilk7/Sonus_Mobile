package com.example.sonus.ui.auth

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
import com.example.sonus.network.RegisterRequest
import com.example.sonus.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnRegister).setOnClickListener {
            performRegistration()
        }

        view.findViewById<View>(R.id.tvGoToLogin).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun performRegistration() {
        val username = view?.findViewById<EditText>(R.id.etRegisterName)?.text.toString().trim() ?: ""
        val password = view?.findViewById<EditText>(R.id.etRegisterPassword)?.text.toString() ?: ""
        val confirmPassword = view?.findViewById<EditText>(R.id.etRegisterConfirmPassword)?.text.toString() ?: ""
        val registerButton = view?.findViewById<TextView>(R.id.btnRegister)

        if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.toast_fill_all), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), getString(R.string.error_password_short), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        registerButton?.isEnabled = false
        registerButton?.alpha = 0.7f

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.register(RegisterRequest(username, password))
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.toast_register_success), Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    showRegistrationError(getString(R.string.error_registration_failed, errorMessage))
                }
            } catch (exception: java.io.IOException) {
                showRegistrationError(getString(R.string.error_network_check))
            } catch (exception: Exception) {
                showRegistrationError(getString(R.string.error_generic, exception.localizedMessage))
            } finally {
                withContext(Dispatchers.Main) {
                    registerButton?.isEnabled = true
                    registerButton?.alpha = 1.0f
                }
            }
        }
    }

    private suspend fun showRegistrationError(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}
