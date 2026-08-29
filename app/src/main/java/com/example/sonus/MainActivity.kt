package com.example.sonus

import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.sonus.network.NetworkMonitor
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private val vpnPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startVpn()
        }
    }

    private fun checkVpnPermission() {
        val vpnIntent = VpnService.prepare(this)
        android.util.Log.d("SonusVPN", "VpnService.prepare() returned: $vpnIntent")
        
        if (vpnIntent != null) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Wymagana Autoryzacja VPN")
                .setMessage("Aplikacja Sonus musi utworzyć bezpieczny tunel, aby połączyć się z Twoim serwerem muzycznym. Kliknij OK i zaakceptuj prośbę systemową.")
                .setPositiveButton("OK") { _, _ ->
                    try {
                        vpnPermissionLauncher.launch(vpnIntent)
                    } catch (e: Exception) {
                        android.util.Log.e("SonusVPN", "Launch failed", e)
                    }
                }
                .setCancelable(false)
                .show()
        } else {
            startVpn()
        }
    }

    private fun startVpn() {
        lifecycleScope.launch {
            try {
                com.example.sonus.network.WireGuardManager.startVpn()
                android.widget.Toast.makeText(this@MainActivity, "VPN: Połączono pomyślnie", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("SonusVPN", "StartVpn Error", e)
                val errorMsg = e.message ?: e.toString()
                android.widget.Toast.makeText(this@MainActivity, "Błąd VPN: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        
        // Delay VPN request to avoid Realme/Oppo background block
        findViewById<View>(android.R.id.content).postDelayed({
            checkVpnPermission()
        }, 1500)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Mini Player
        MiniPlayerHelper.setupMiniPlayer(this)

        // Observe UI states from ViewModel
        viewModel.isBottomNavVisible.observe(this) { visible ->
            findViewById<View>(R.id.bottomNav).visibility = if (visible) View.VISIBLE else View.GONE
        }

        viewModel.isMiniPlayerVisible.observe(this) { visible ->
            val miniPlayer = findViewById<View>(R.id.miniPlayer)
            if (!visible) {
                miniPlayer.visibility = View.GONE
            } else if (PlayerState.currentSong != null) {
                miniPlayer.visibility = View.VISIBLE
            }
        }

        // Current Tab Observation (Sync Indicator)
        viewModel.currentTabPage.observe(this) { page ->
            updateNavIndicators(page)
        }

        // Control visibility based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.registerFragment, R.id.playerFragment -> {
                    viewModel.setBottomNavVisibility(false)
                    viewModel.setMiniPlayerVisibility(false)
                }
                else -> {
                    viewModel.setBottomNavVisibility(true)
                    viewModel.setMiniPlayerVisibility(true)
                }
            }
        }

        // Global Navigation Click Listeners (Bottom Nav)
        // Since we are not using standard BottomNavigationView menu, we handle clicks manually
        setupGlobalNavigation(navController)
        checkForUpdates()

        // Observe Network Errors for Terminal
        lifecycleScope.launch {
            NetworkMonitor.networkErrors.collect { error ->
                viewModel.addTerminalLog(error)
            }
        }
    }

    private fun setupGlobalNavigation(navController: androidx.navigation.NavController) {
        val navOptions = androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.mainContainerFragment, false)
            .setLaunchSingleTop(true)
            .build()

        findViewById<View>(R.id.navHome).setOnClickListener {
            viewModel.setTabPage(0)
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment, null, navOptions)
            }
        }
        findViewById<View>(R.id.navSearch).setOnClickListener {
            viewModel.setTabPage(1)
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment, null, navOptions)
            }
        }
        findViewById<View>(R.id.navLibrary).setOnClickListener {
            viewModel.setLibraryFilter(0) // Reset to ALL when clicking from bottom nav
            viewModel.setTabPage(2)
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment, null, navOptions)
            }
        }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            viewModel.setTabPage(3)
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment, null, navOptions)
            }
        }
    }

    private fun updateNavIndicators(currentPageIndex: Int) {
        val accent = ContextCompat.getColor(this, R.color.studio_amber)
        val dark = ContextCompat.getColor(this, R.color.studio_text_dim)

        // Indicators
        findViewById<View>(R.id.indicatorHome).visibility = if (currentPageIndex == 0) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.indicatorSearch).visibility = if (currentPageIndex == 1) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.indicatorLibrary).visibility = if (currentPageIndex == 2) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.indicatorSettings).visibility = if (currentPageIndex == 3) View.VISIBLE else View.INVISIBLE

        // Text Labels and Content
        val tvHome = findViewById<TextView>(R.id.tvNavHome)
        val tvSearch = findViewById<TextView>(R.id.tvNavSearch)
        val tvLibrary = findViewById<TextView>(R.id.tvNavLibrary)
        val tvSettings = findViewById<TextView>(R.id.tvNavSettings)

        tvHome.text = LabelProvider.getLabel(this, "nav_home")
        tvSearch.text = LabelProvider.getLabel(this, "nav_search")
        tvLibrary.text = LabelProvider.getLabel(this, "nav_library")
        tvSettings.text = LabelProvider.getLabel(this, "nav_settings")

        tvHome.setTextColor(if (currentPageIndex == 0) accent else dark)
        tvSearch.setTextColor(if (currentPageIndex == 1) accent else dark)
        tvLibrary.setTextColor(if (currentPageIndex == 2) accent else dark)
        tvSettings.setTextColor(if (currentPageIndex == 3) accent else dark)
    }

    private fun checkForUpdates() {
        lifecycleScope.launch {
            try {
                val response = com.example.sonus.network.RetrofitClient.githubApi.getLatestRelease()
                if (response.isSuccessful) {
                    val release = response.body()
                    if (release != null) {
                        val currentVersion = BuildConfig.VERSION_NAME
                        val newVersion = release.tagName.removePrefix("v")
                        
                        if (newVersion != currentVersion) {
                            viewModel.addTerminalLog("SYSTEM_UPDATE_AVAILABLE: v$newVersion")
                            viewModel.addTerminalLog("INITIATING_OPERATOR_CHOICE...")
                            
                            androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
                                .setTitle("Dostępna Aktualizacja v$newVersion")
                                .setMessage("Nowa wersja systemu jest gotowa do instalacji. Pobrać teraz?")
                                .setPositiveButton("::DOWNLOAD") { _, _ ->
                                    viewModel.addTerminalLog("UPGRADE_SEQUENCE_STARTED")
                                    com.example.sonus.network.UpdateManager.checkAndDownloadUpdate(this@MainActivity, release)
                                }
                                .setNegativeButton("[ ABORT ]", null)
                                .show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SonusUpdate", "Update check failed", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MiniPlayerHelper.onDestroy(this)
        
        // Rozłącz VPN tylko jeśli aplikacja jest naprawdę zamykana (nie przy obrocie ekranu)
        if (isFinishing) {
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                com.example.sonus.network.WireGuardManager.stopVpn()
            }
        }
    }
}
