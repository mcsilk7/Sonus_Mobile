package com.example.sonus.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.sonus.*
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        
        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        initViews(view)
        displayUserData(view)
        fetchUserStats(view)
    }

    private fun initViews(view: View) {
        view.findViewById<View>(R.id.btnProfileBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        view.findViewById<View>(R.id.btnProfileSettings).setOnClickListener {
            mainViewModel.setTabPage(3)
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnProfileLogout).setOnClickListener {
            sessionManager.clearSession()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun displayUserData(view: View) {
        val username = sessionManager.getUsername() ?: "Użytkownik"
        val role = sessionManager.getRole() ?: "USER"
        
        view.findViewById<TextView>(R.id.tvProfileName).text = username
        view.findViewById<TextView>(R.id.tvProfileEmail).text = "$username ($role)" 
        
        val avatar = view.findViewById<TextView>(R.id.profileAvatar)
        avatar.text = username.take(1).uppercase()
    }

    private fun fetchUserStats(view: View) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val playlistsDeferred = async { RetrofitClient.playlistApi.getUserPlaylists(userId) }
                val favoritesDeferred = async { RetrofitClient.favoriteApi.getFavorites(userId) }

                val playlistsResponse = playlistsDeferred.await()
                val favoritesResponse = favoritesDeferred.await()

                if (playlistsResponse.isSuccessful) {
                    val count = playlistsResponse.body()?.size ?: 0
                    view.findViewById<TextView>(R.id.tvStatsPlaylistsCount).text = count.toString()
                }

                if (favoritesResponse.isSuccessful) {
                    val favorites = favoritesResponse.body() ?: emptyList()
                    view.findViewById<TextView>(R.id.tvStatsFavoritesCount).text = favorites.size.toString()
                    
                    val totalSeconds = favorites.sumOf { it.songDuration ?: 0 }
                    val totalHours = totalSeconds / 3600
                    view.findViewById<TextView>(R.id.tvStatsHoursCount).text = totalHours.toString()
                }
            } catch (e: Exception) {
                // Ignore stats errors
            }
        }
    }

    private fun showEditProfileDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etUsername = view.findViewById<EditText>(R.id.etEditUsername)
        val btnCancel = view.findViewById<View>(R.id.btnEditCancel)
        val btnSave = view.findViewById<View>(R.id.btnEditSave)

        etUsername.setText(sessionManager.getUsername())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newName = etUsername.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateLocalUsername(newName)
                dialog.dismiss()
                Toast.makeText(requireContext(), "Profil zaktualizowany", Toast.LENGTH_SHORT).show()
            } else {
                etUsername.error = "Nazwa nie może być pusta"
            }
        }
        dialog.show()
    }

    private fun updateLocalUsername(newName: String) {
        val token = sessionManager.getToken() ?: ""
        val userId = sessionManager.getUserId()
        val role = sessionManager.getRole()
        sessionManager.saveSession(token, newName, userId, role)
        
        view?.let { displayUserData(it) }
    }
}
