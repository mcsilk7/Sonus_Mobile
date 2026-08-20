package com.example.sonus

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.sonus.network.NetworkMonitor
import com.example.sonus.network.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

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
                R.id.loginFragment, R.id.registerFragment, R.id.playerFragment -> {
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

    override fun onDestroy() {
        super.onDestroy()
        MiniPlayerHelper.onDestroy(this)
    }
}
