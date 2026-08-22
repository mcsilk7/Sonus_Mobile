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
        updateStorageStats(view)

        view.findViewById<View>(R.id.btnProfile).setOnClickListener {
            findNavController().navigate(R.id.profileFragment)
        }

        view.findViewById<View>(R.id.btnManageDownloads).setOnClickListener {
            findNavController().navigate(R.id.downloadedSongsFragment)
        }

        view.findViewById<View>(R.id.btnLogout).setOnClickListener {
            sessionManager.clearSession()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { updateStorageStats(it) }
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
        val btnClearDownloads = view.findViewById<View>(R.id.btnClearDownloads)
        val context = requireContext()

        view.findViewById<TextView>(R.id.tvSettingsVisualsTitle).text = LabelProvider.getLabel(context, "settings_visuals")
        view.findViewById<TextView>(R.id.tvSettingsReelsTitle).text = LabelProvider.getLabel(context, "settings_reels")
        view.findViewById<TextView>(R.id.tvSettingsReelsDesc).text = LabelProvider.getLabel(context, "settings_reels_desc")

        view.findViewById<TextView>(R.id.tvSettingsClearDownloadsTitle).text = LabelProvider.getLabel(context, "settings_clear_downloads")
        view.findViewById<TextView>(R.id.tvSettingsClearDownloadsDesc).text = LabelProvider.getLabel(context, "settings_clear_downloads_desc")
        view.findViewById<TextView>(R.id.tvSettingsClearDownloadsAction).text = LabelProvider.getLabel(context, "profile_wipe")

        view.findViewById<TextView>(R.id.tvSettingsStorageTitle).text = LabelProvider.getLabel(context, "settings_storage_monitor")
        view.findViewById<TextView>(R.id.tvSettingsManageSignalsTitle).text = LabelProvider.getLabel(context, "settings_manage_signals")
        view.findViewById<TextView>(R.id.tvSettingsManageSignalsDesc).text = LabelProvider.getLabel(context, "settings_manage_signals_desc")
        view.findViewById<TextView>(R.id.tvSettingsManageSignalsAction).text = LabelProvider.getLabel(context, "settings_manage_action")

        updateToggleUI(statusView, settingsManager.isReelsEnabled())

        btnToggle.setOnClickListener {
            val newState = !settingsManager.isReelsEnabled()
            settingsManager.setReelsEnabled(newState)
            updateToggleUI(statusView, newState)
            
            val message = if (newState) getString(R.string.mechanical_visuals_enabled) else getString(R.string.mechanical_visuals_disabled)
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        btnClearDownloads.setOnClickListener {
            showClearDownloadsConfirmation()
        }
    }

    private fun showClearDownloadsConfirmation() {
        val context = requireContext()
        val isTechnical = SettingsManager(context).getThemeId() == 0
        
        val title = if (isTechnical) "CONFIRM_WIPE_SEQUENCE" else "Clear Downloads"
        val message = if (isTechnical) "ERASE_ALL_LOCAL_SIGNALS_FROM_DISK?" else "Do you want to delete all offline songs?"
        val confirm = if (isTechnical) "::EXE_WIPE" else "Clear All"
        val cancel = if (isTechnical) "[ ABORT ]" else "Cancel"

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirm) { _, _ ->
                DownloadManager.clearAllDownloads(context)
                Toast.makeText(context, LabelProvider.getLabel(context, "profile_wipe"), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(cancel, null)
            .show()
    }

    private fun updateToggleUI(view: View, isEnabled: Boolean) {
        view.setBackgroundResource(if (isEnabled) R.drawable.bg_settings_toggle_active else R.drawable.bg_settings_toggle)
    }

    private fun updateStorageStats(view: View) {
        val context = requireContext()
        val totalBytes = DownloadManager.getTotalBytesUsed(context)
        val mbUsed = totalBytes / (1024f * 1024f)
        
        val isTechnical = settingsManager.getThemeId() == 0
        val label = if (isTechnical) "DISK_USAGE" else getString(R.string.storage_usage_norm)
        
        val sizeStr = String.format(java.util.Locale.US, "%.1f MB", mbUsed)
        view.findViewById<TextView>(R.id.tvDiskUsage).text = "$label: $sizeStr"
        
        // Mock max capacity for the visual bar (e.g. 500MB)
        val maxMb = 500f
        val progress = (mbUsed / maxMb).coerceIn(0f, 1f)
        view.findViewById<SectorMapView>(R.id.sectorMapView).setProgress(progress)
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
            else -> LabelProvider.getLabel(context, "theme_amber")
        }
    }

    private fun showThemeSelectionDialog() {
        val context = requireContext()
        val themes = arrayOf(
            LabelProvider.getLabel(context, "theme_amber"),
            LabelProvider.getLabel(context, "theme_dark")
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
