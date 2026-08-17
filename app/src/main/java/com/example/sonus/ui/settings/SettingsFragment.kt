package com.example.sonus.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sonus.*
import com.example.sonus.network.SessionManager

class SettingsFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        displayUserData(view)

        view.findViewById<View>(R.id.btnProfile).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        view.findViewById<View>(R.id.btnLogout).setOnClickListener {
            sessionManager.clearSession()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun displayUserData(view: View) {
        val username = sessionManager.getUsername() ?: "Użytkownik"
        val role = sessionManager.getRole() ?: "USER"

        view.findViewById<TextView>(R.id.tvSettingsName).text = username
        view.findViewById<TextView>(R.id.tvSettingsEmail).text = "$username ($role)"

        val avatar = view.findViewById<TextView>(R.id.tvSettingsAvatar)
        avatar.text = username.take(1).uppercase()
    }
}
