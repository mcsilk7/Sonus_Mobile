package com.example.sonus.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.sonus.MainViewModel
import com.example.sonus.NetworkHelper
import com.example.sonus.R
import com.example.sonus.network.SessionManager

class SplashFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var tvStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        tvStatus = view.findViewById(R.id.tvSplashStatus)
        tvStatus.text = getString(R.string.sys_init)

        // Artificial delay for system "boot" feel
        Handler(Looper.getMainLooper()).postDelayed({
            checkConnectivity()
        }, 1500)
    }

    private fun checkConnectivity() {
        val context = requireContext()
        val isOnline = NetworkHelper.isNetworkAvailable(context)
        val hasSession = sessionManager.isLoggedIn()

        if (isOnline) {
            mainViewModel.setOfflineMode(false)
            tvStatus.text = getString(R.string.conn_established)
            Handler(Looper.getMainLooper()).postDelayed({
                if (hasSession) {
                    findNavController().navigate(R.id.mainContainerFragment)
                } else {
                    findNavController().navigate(R.id.loginFragment)
                }
            }, 800)
        } else {
            mainViewModel.setOfflineMode(true)
            tvStatus.text = getString(R.string.offline_detected)
            Handler(Looper.getMainLooper()).postDelayed({
                if (hasSession) {
                    // Bypass login if we have a session, allow offline use
                    findNavController().navigate(R.id.mainContainerFragment)
                } else {
                    // No session and offline - we must show login but it might be limited
                    findNavController().navigate(R.id.loginFragment)
                }
            }, 1200)
        }
    }
}
