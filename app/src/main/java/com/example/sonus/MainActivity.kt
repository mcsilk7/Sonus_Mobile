package com.example.sonus

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.sonus.network.SessionManager

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
    }

    private fun setupGlobalNavigation(navController: androidx.navigation.NavController) {
        findViewById<View>(R.id.navHome).setOnClickListener {
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment)
            }
            viewModel.setTabPage(0)
        }
        findViewById<View>(R.id.navSearch).setOnClickListener {
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment)
            }
            viewModel.setTabPage(1)
        }
        findViewById<View>(R.id.navLibrary).setOnClickListener {
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment)
            }
            viewModel.setTabPage(2)
        }
        findViewById<View>(R.id.navSettings).setOnClickListener {
            if (navController.currentDestination?.id != R.id.mainContainerFragment) {
                navController.navigate(R.id.mainContainerFragment)
            }
            viewModel.setTabPage(3)
        }
    }

    private fun updateNavIndicators(currentPageIndex: Int) {
        findViewById<View>(R.id.indicatorHome).visibility = 
            if (currentPageIndex == 0) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.indicatorSearch).visibility = 
            if (currentPageIndex == 1) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.indicatorLibrary).visibility = 
            if (currentPageIndex == 2) View.VISIBLE else View.INVISIBLE
        findViewById<View>(R.id.indicatorSettings).visibility = 
            if (currentPageIndex == 3) View.VISIBLE else View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        MiniPlayerHelper.onDestroy(this)
    }
}
