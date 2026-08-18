package com.example.sonus.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sonus.*
import com.example.sonus.network.SessionManager

class SettingsFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var settingsManager: SettingsManager

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
        settingsManager = SettingsManager(requireContext())
        UserAvatarHelper.setupAvatar(view, sessionManager, findNavController())
        displayUserData(view)
        setupVisualSettings(view)
        setupThemeSettings(view)

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
        val context = requireContext()

        view.findViewById<TextView>(R.id.tvSettingsOpName).text = username
        view.findViewById<TextView>(R.id.tvSettingsIdLabel).text = "$username ($role)"

        val avatar = view.findViewById<TextView>(R.id.tvSettingsAvatar)
        avatar.text = username.take(1).uppercase()
        
        view.findViewById<TextView>(R.id.tvSettingsOpName).text = LabelProvider.getLabel(context, "settings_op_name")
        view.findViewById<TextView>(R.id.tvSettingsIdLabel).text = LabelProvider.getLabel(context, "settings_id_label")
        view.findViewById<TextView>(R.id.tvSettingsEditLabel).text = LabelProvider.getLabel(context, "settings_edit")
        view.findViewById<TextView>(R.id.btnLogout).text = LabelProvider.getLabel(context, "settings_logout")
        view.findViewById<TextView>(R.id.tvSettingsBuild).text = LabelProvider.getLabel(context, "settings_build")
    }

    private fun setupVisualSettings(view: View) {
        val btnToggle = view.findViewById<View>(R.id.btnToggleReels)
        val statusView = view.findViewById<View>(R.id.reelsToggleStatus)
        val context = requireContext()

        view.findViewById<TextView>(R.id.tvSettingsVisualsTitle).text = LabelProvider.getLabel(context, "settings_visuals")
        view.findViewById<TextView>(R.id.tvSettingsReelsTitle).text = LabelProvider.getLabel(context, "settings_reels")
        view.findViewById<TextView>(R.id.tvSettingsReelsDesc).text = LabelProvider.getLabel(context, "settings_reels_desc")

        updateToggleUI(statusView, settingsManager.isReelsEnabled())

        btnToggle.setOnClickListener {
            val newState = !settingsManager.isReelsEnabled()
            settingsManager.setReelsEnabled(newState)
            updateToggleUI(statusView, newState)
            
            val message = if (newState) getString(R.string.mechanical_visuals_enabled) else getString(R.string.mechanical_visuals_disabled)
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToggleUI(view: View, isEnabled: Boolean) {
        view.setBackgroundResource(if (isEnabled) R.drawable.bg_settings_toggle_active else R.drawable.bg_settings_toggle)
    }

    private fun setupThemeSettings(view: View) {
        val btnSelect = view.findViewById<View>(R.id.btnSelectTheme)
        val tvCurrent = view.findViewById<TextView>(R.id.tvCurrentThemeName)
        val context = requireContext()

        view.findViewById<TextView>(R.id.tvSettingsThemeEngineTitle).text = LabelProvider.getLabel(context, "settings_theme_engine")
        view.findViewById<TextView>(R.id.tvSettingsThemeSelectLabel).text = LabelProvider.getLabel(context, "settings_select")

        val themeId = settingsManager.getThemeId()
        tvCurrent.text = getString(R.string.active_theme_prefix, getThemeName(themeId))

        btnSelect.setOnClickListener {
            showThemeSelectionDialog()
        }
    }

    private fun getThemeName(id: Int): String {
        val context = requireContext()
        return when (id) {
            1 -> LabelProvider.getLabel(context, "theme_dark")
            2 -> LabelProvider.getLabel(context, "theme_light")
            else -> LabelProvider.getLabel(context, "theme_amber")
        }
    }

    private fun showThemeSelectionDialog() {
        val context = requireContext()
        val themes = arrayOf(
            LabelProvider.getLabel(context, "theme_amber"),
            LabelProvider.getLabel(context, "theme_dark"),
            LabelProvider.getLabel(context, "theme_light")
        )
        
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.select_theme_title))
            .setItems(themes) { _, which ->
                changeTheme(which)
            }
            .show()
    }

    private fun changeTheme(themeId: Int) {
        if (settingsManager.getThemeId() != themeId) {
            settingsManager.setThemeId(themeId)
            activity?.recreate()
        }
    }
}
