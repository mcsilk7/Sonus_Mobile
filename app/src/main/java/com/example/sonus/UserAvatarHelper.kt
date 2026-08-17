package com.example.sonus

import android.view.View
import android.widget.TextView
import androidx.navigation.NavController
import com.example.sonus.network.SessionManager

object UserAvatarHelper {
    fun setupAvatar(view: View, sessionManager: SessionManager, navController: NavController) {
        val userAvatar = view.findViewById<TextView>(R.id.userAvatar) ?: return
        val username = sessionManager.getUsername() ?: "U"
        userAvatar.text = username.take(1).uppercase()
        
        userAvatar.setOnClickListener {
            if (navController.currentDestination?.id != R.id.profileFragment) {
                navController.navigate(R.id.profileFragment)
            }
        }
    }
}
