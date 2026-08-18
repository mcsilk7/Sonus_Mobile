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
        val context = requireContext()
        view.findViewById<TextView>(R.id.tvProfileHeaderTop).text = LabelProvider.getLabel(context, "profile_header_top")
        view.findViewById<TextView>(R.id.tvProfileHeaderMain).text = LabelProvider.getLabel(context, "profile_header_main")
        
        view.findViewById<TextView>(R.id.tvProfileEditLabel).apply {
            text = LabelProvider.getLabel(context, "profile_edit_id")
            setOnClickListener { showEditProfileDialog() }
        }

        view.findViewById<View>(R.id.btnProfileBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnProfileSettings).setOnClickListener {
            mainViewModel.setTabPage(3)
            findNavController().popBackStack()
        }

        view.findViewById<TextView>(R.id.btnProfileLogout).apply {
            text = LabelProvider.getLabel(context, "profile_terminate")
            setOnClickListener {
                sessionManager.clearSession()
                findNavController().navigate(R.id.loginFragment)
            }
        }
        
        view.findViewById<TextView>(R.id.tvProfileAccAccess).text = LabelProvider.getLabel(context, "profile_acc_access")
        view.findViewById<TextView>(R.id.tvProfileDelete).text = LabelProvider.getLabel(context, "profile_delete")
        view.findViewById<TextView>(R.id.tvProfileWipeDesc).text = LabelProvider.getLabel(context, "profile_wipe_desc")
        view.findViewById<TextView>(R.id.tvProfileWipeLabel).text = LabelProvider.getLabel(context, "profile_wipe")
        view.findViewById<TextView>(R.id.tvProfileBuild).text = LabelProvider.getLabel(context, "profile_build")

        view.findViewById<TextView>(R.id.tvProfileUnits).text = LabelProvider.getLabel(context, "profile_units")
        view.findViewById<TextView>(R.id.tvProfileRuntime).text = LabelProvider.getLabel(context, "profile_runtime")
        view.findViewById<TextView>(R.id.tvProfileSignals).text = LabelProvider.getLabel(context, "profile_signals")
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
                Toast.makeText(requireContext(), getString(R.string.toast_profile_updated), Toast.LENGTH_SHORT).show()
            } else {
                etUsername.error = getString(R.string.error_name_empty)
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
